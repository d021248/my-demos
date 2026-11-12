package d021248.mytry.media.image;

import static org.assertj.core.api.Assertions.assertThat;

import d021248.mytry.media.image.observer.ErrorCollectorObserver;
import d021248.mytry.media.image.observer.ImageScanStatsObserver;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/// Micro-benchmark for `ImageBatchAnalyzer.analyzeAllImages` that sweeps the
/// semaphore limit and logs a throughput table so the optimal value can be read off.
///
/// Not a correctness test — it only asserts that no errors occurred.
/// Run with: `mvn test -Dtest=ImageConcurrencyBenchmarkTest`
class ImageConcurrencyBenchmarkTest {

  private static final int IMAGE_COUNT = 200;
  private static final int IMAGE_SIZE = 256; // pixels; small but realistic decode cost
  private static final int WARMUP_RUNS = 1;
  private static final int[] LEVELS = {1, 2, 4, 8, 16, 22, 32, 48, 64};

  @TempDir Path tempDir;

  @Test
  @DisplayName("Semaphore sweep: log throughput for each concurrency level")
  void semaphoreSweep() throws IOException {
    createTestImages();

    System.out.printf("%n%-12s  %10s  %10s%n", "maxConcurrent", "ms total", "img/s");
    System.out.println("-".repeat(38));

    // warmup
    for (int i = 0; i < WARMUP_RUNS; i++) {
      run(LEVELS[LEVELS.length / 2]);
    }

    long bestMs = Long.MAX_VALUE;
    int bestLevel = -1;

    for (int level : LEVELS) {
      final var ms = run(level);
      final var throughput = IMAGE_COUNT * 1000.0 / ms;
      final var marker = ms < bestMs ? " <--" : "";
      System.out.printf("%-12d  %10d  %10.1f%s%n", level, ms, throughput, marker);
      if (ms < bestMs) {
        bestMs = ms;
        bestLevel = level;
      }
    }

    System.out.printf(
        "%nBest: maxConcurrent=%d  (%d ms, %.1f img/s)%n",
        bestLevel, bestMs, IMAGE_COUNT * 1000.0 / bestMs);

    assertThat(bestLevel).isPositive();
  }

  private long run(int maxConcurrent) throws IOException {
    var errors = new ErrorCollectorObserver();
    var stats = new ImageScanStatsObserver(IMAGE_COUNT);
    final var start = System.currentTimeMillis();
    ImageBatchAnalyzer.analyzeAllImages(tempDir, List.of(stats, errors), maxConcurrent);
    final var elapsed = System.currentTimeMillis() - start;
    assertThat(errors.isEmpty())
        .as("errors at maxConcurrent=%d: %s", maxConcurrent, errors.errorInfos())
        .isTrue();
    assertThat(stats.getSuccessCount()).isEqualTo(IMAGE_COUNT);
    return elapsed;
  }

  private void createTestImages() throws IOException {
    for (int i = 0; i < IMAGE_COUNT; i++) {
      final var img = new BufferedImage(IMAGE_SIZE, IMAGE_SIZE, BufferedImage.TYPE_INT_RGB);
      final Graphics2D g = img.createGraphics();
      g.setColor(new Color(i % 256, (i * 3) % 256, (i * 7) % 256));
      g.fillRect(0, 0, IMAGE_SIZE, IMAGE_SIZE);
      g.dispose();
      ImageIO.write(img, "png", tempDir.resolve("img_%04d.png".formatted(i)).toFile());
    }
  }
}
