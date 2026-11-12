package d021248.mytry.media.image.observer;

import d021248.mytry.media.image.ImageAnalysisResult;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

/// Observer that logs live progress and a final summary for image batch scans.
///
/// Tracks successes, errors, GPS coverage, and throughput rate.
/// Logs a progress line every `logEvery` successful analyses; call
/// `logSummary()` once the scan is complete for the overall report.
public final class ImageScanStatsObserver implements ImageAnalysisObserver {
  private static final Logger LOGGER = Logger.getLogger(ImageScanStatsObserver.class.getName());

  private final int logEvery;
  private final long startNanos = System.nanoTime();
  private final AtomicInteger successCount = new AtomicInteger();
  private final AtomicInteger errorCount = new AtomicInteger();
  private final AtomicInteger gpsCount = new AtomicInteger();
  private final AtomicLong totalPixels = new AtomicLong();
  private volatile int discoveredCount = -1;

  /// @param logEvery log a progress line every this many successful analyses
  public ImageScanStatsObserver(int logEvery) {
    this.logEvery = logEvery;
  }

  @Override
  public void onSuccess(ImageAnalysisResult result) {
    final var n = successCount.incrementAndGet();
    if (result.location().isPresent()) {
      gpsCount.incrementAndGet();
    }
    totalPixels.addAndGet((long) result.image().getWidth() * result.image().getHeight());
    if (n % logEvery == 0) {
      logProgress(n);
    }
  }

  @Override
  public void onError(Path path, Exception cause) {
    errorCount.incrementAndGet();
  }

  @Override
  public void onDiscovered(int totalFiles) {
    discoveredCount = totalFiles;
  }

  /// Logs the final summary — call once after `analyzeAllImages` returns.
  public void logSummary() {
    final var success = successCount.get();
    final var errors = errorCount.get();
    final var gps = gpsCount.get();
    final var elapsed = elapsedSeconds();
    final var rate = elapsed > 0 ? success / elapsed : 0;
    final var avgMpx = success > 0 ? totalPixels.get() / (double) success / 1_000_000 : 0;
    final var processed = success + errors;
    final var discovered = discoveredCount;
    final var discoveredLabel = discovered < 0 ? "?" : String.valueOf(discovered);
    final var coverageNote = buildCoverageNote(processed, discovered);
    LOGGER.info(
        () ->
            ("[Stats] DONE  discovered=%s  success=%d  errors=%d (%.1f%%)  "
                    + "gps=%d (%.1f%%)  avg=%.1fMPx  %.1fs  %.1f img/s%s")
                .formatted(
                    discoveredLabel,
                    success,
                    errors,
                    processed > 0 ? 100.0 * errors / processed : 0,
                    gps,
                    success > 0 ? 100.0 * gps / success : 0,
                    avgMpx,
                    elapsed,
                    rate,
                    coverageNote));
  }

  private static String buildCoverageNote(int processed, int discovered) {
    if (discovered < 0) {
      return "";
    }
    if (discovered == processed) {
      return "  coverage=ALL (%d/%d)".formatted(processed, discovered);
    }
    return "  coverage=INCOMPLETE (%d/%d, missed %d)"
        .formatted(processed, discovered, discovered - processed);
  }

  private void logProgress(int n) {
    final var elapsed = elapsedSeconds();
    final var rate = elapsed > 0 ? n / elapsed : 0;
    LOGGER.info(
        () ->
            "[Stats] %4d analysed | %3d errors | %.1f s | %.1f img/s"
                .formatted(n, errorCount.get(), elapsed, rate));
  }

  private double elapsedSeconds() {
    return (System.nanoTime() - startNanos) / 1_000_000_000.0;
  }

  /// Returns the number of successfully analysed images.
  public int getSuccessCount() {
    return successCount.get();
  }

  /// Returns the number of images that failed analysis.
  public int getErrorCount() {
    return errorCount.get();
  }
}
