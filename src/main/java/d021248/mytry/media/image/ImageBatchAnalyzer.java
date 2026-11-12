package d021248.mytry.media.image;

import d021248.mytry.media.image.observer.ImageAnalysisObserver;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/// Scans a directory tree for image files and analyses each one concurrently
/// using one virtual thread per image.
///
/// Configuration of recognised extensions (priority order):
/// 1. `IMAGE_EXTENSIONS` environment variable — comma-separated list
/// 2. `img-config.properties` on the classpath — key `image.extensions`
/// 3. Built-in fallback set: jpg, jpeg, png, bmp, gif, tiff, webp
public final class ImageBatchAnalyzer {
  private static final Logger LOGGER = Logger.getLogger(ImageBatchAnalyzer.class.getName());
  private static final Set<String> IMAGE_EXTENSIONS = loadImageExtensions();

  // Give up waiting after this many seconds — prevents hanging on corrupt/stuck
  // JPEG files.
  private static final int TOTAL_TIMEOUT_SECS = 60;
  // Max simultaneous image decodes; each decoded image can occupy 10–50 MB of
  // heap.
  // Override via -Dimage.analyzer.max.concurrent=N for benchmarking.
  private static final int MAX_CONCURRENT = resolveMaxConcurrent();

  private static int resolveMaxConcurrent() {
    final var prop = System.getProperty("image.analyzer.max.concurrent");
    if (prop != null) {
      try {
        final var v = Integer.parseInt(prop.strip());
        if (v > 0) return v;
      } catch (NumberFormatException _) {
        LOGGER.warning(() -> "Invalid image.analyzer.max.concurrent value: %s".formatted(prop));
      }
    }
    // I/O+CPU mix: virtual threads park during reads, so 2×cores outperforms
    // 1×cores
    return Math.max(4, Runtime.getRuntime().availableProcessors() * 2);
  }

  private ImageBatchAnalyzer() {}

  private static Set<String> parseExtensions(String csv) {
    return Arrays.stream(csv.split(","))
        .map(String::strip)
        .filter(s -> !s.isEmpty())
        .collect(Collectors.toUnmodifiableSet());
  }

  private static Set<String> loadImageExtensions() {
    final var env = System.getenv("IMAGE_EXTENSIONS");
    if (env != null && !env.isBlank()) {
      return parseExtensions(env);
    }
    final var props = new Properties();
    try (InputStream in =
        ImageBatchAnalyzer.class.getClassLoader().getResourceAsStream("img-config.properties")) {
      if (in != null) {
        props.load(in);
        final var prop = props.getProperty("image.extensions");
        if (prop != null && !prop.isBlank()) {
          return parseExtensions(prop);
        }
      }
    } catch (IOException e) {
      LOGGER.warning(
          () -> "Could not load image extensions from config: %s".formatted(e.getMessage()));
    }
    return Set.of("jpg", "jpeg", "png", "bmp", "gif", "tiff", "webp");
  }

  private static boolean isImageFile(Path path) {
    final var name = path.getFileName().toString();
    final var dot = name.lastIndexOf('.');
    if (dot == -1 || dot == name.length() - 1) {
      return false;
    }
    return IMAGE_EXTENSIONS.contains(name.substring(dot + 1).toLowerCase(Locale.ROOT));
  }

  private static void process(
      Path path, List<ImageAnalysisObserver> observers, Semaphore loadSlots) {
    try {
      loadSlots.acquire(); // virtual thread parks here — no platform thread blocked
    } catch (InterruptedException _) {
      Thread.currentThread().interrupt();
      return;
    }
    try {
      try {
        var result = new ImageAnalyzer(path).analyze();
        observers.forEach(obs -> obs.onSuccess(result));
      } catch (Exception e) {
        observers.forEach(obs -> obs.onError(path, e));
      }
    } finally {
      loadSlots.release();
    }
  }

  /// Analyses all images under `rootDir` using the configured `MAX_CONCURRENT`
  /// limit.
  ///
  /// @param rootDir   root directory to scan recursively
  /// @param observers observers notified on each success or error
  /// @throws IOException if walking `rootDir` fails
  public static void analyzeAllImages(Path rootDir, List<ImageAnalysisObserver> observers)
      throws IOException {
    analyzeAllImages(rootDir, observers, MAX_CONCURRENT);
  }

  /// Analyses all images under `rootDir` with an explicit concurrency limit.
  ///
  /// A `StructuredTaskScope` is opened with `awaitAllSuccessfulOrThrow` and a
  /// `TOTAL_TIMEOUT_SECS` deadline. One virtual-thread subtask is forked per
  /// image;
  /// `scope.join()` waits until every subtask completes or the timeout fires — at
  /// which
  /// point the scope cancels remaining subtasks (interrupts them) before
  /// returning.
  ///
  /// Each `ImageAnalysisResult` (including its `BufferedImage`) is passed to
  /// observers
  /// and then released — it is never accumulated, so the GC can reclaim decoded
  /// image
  /// memory as soon as each observer call returns.
  ///
  /// @param rootDir       root directory to scan recursively
  /// @param observers     observers notified on each success or error
  /// @param maxConcurrent max simultaneous image decodes (useful for benchmarking)
  /// @throws IOException if walking `rootDir` fails
  public static void analyzeAllImages(
      Path rootDir, List<ImageAnalysisObserver> observers, int maxConcurrent) throws IOException {
    // Per-call semaphore — not static, so concurrent analyzeAllImages calls don't
    // interfere.
    final var loadSlots = new Semaphore(maxConcurrent);
    final var discoveredCount = new AtomicInteger();
    try (var scope =
        StructuredTaskScope.open(
            StructuredTaskScope.Joiner.<Void>awaitAllSuccessfulOrThrow(),
            cfg -> cfg.withTimeout(Duration.ofSeconds(TOTAL_TIMEOUT_SECS)))) {
      try (var stream = Files.walk(rootDir)) {
        stream
            .filter(Files::isRegularFile)
            .filter(ImageBatchAnalyzer::isImageFile)
            .forEach(
                path -> {
                  discoveredCount.incrementAndGet();
                  scope.fork(
                      () -> {
                        process(path, observers, loadSlots);
                        return null;
                      });
                });
      }
      observers.forEach(obs -> obs.onDiscovered(discoveredCount.get()));
      scope.join();
    } catch (StructuredTaskScope.FailedException e) {
      // process() catches all exceptions — only reachable if an Error escapes
      LOGGER.warning(() -> "Unexpected subtask failure: %s".formatted(e.getCause()));
    } catch (InterruptedException _) {
      Thread.currentThread().interrupt();
    }
  }
}
