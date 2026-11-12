package d021248.mytry.media.image;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import d021248.mytry.media.image.observer.ErrorCollectorObserver;
import d021248.mytry.media.image.observer.ImageAnalysisObserver;
import d021248.mytry.media.image.observer.ImagePrintObserver;
import d021248.mytry.media.image.observer.ImageScanStatsObserver;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ImageBatchAnalyzerTest {

  @TempDir Path tempDir;

  @Test
  @DisplayName("Empty directory completes without errors")
  void emptyDirectoryCompletesWithoutErrors() throws IOException {
    var errorCollector = new ErrorCollectorObserver();
    ImageBatchAnalyzer.analyzeAllImages(tempDir, List.of(errorCollector));
    assertThat(errorCollector.isEmpty()).isTrue();
  }

  @Test
  @DisplayName("Non-image files are ignored")
  void nonImageFilesAreIgnored() throws IOException {
    Files.writeString(tempDir.resolve("test.txt"), "not an image");
    Files.writeString(tempDir.resolve("readme.md"), "# README");

    var stats = new ImageScanStatsObserver(1);
    ImageBatchAnalyzer.analyzeAllImages(tempDir, List.of(stats));
    assertThat(stats.getSuccessCount()).isZero();
    assertThat(stats.getErrorCount()).isZero();
  }

  @Test
  @DisplayName("Null root directory throws an exception")
  void nullRootDirectoryThrows() {
    assertThatThrownBy(() -> ImageBatchAnalyzer.analyzeAllImages(null, List.of()))
        .isInstanceOf(Exception.class);
  }

  @Test
  @DisplayName("Multiple observers are all notified")
  void multipleObserversAreNotified() {
    List<ImageAnalysisObserver> observers =
        List.of(new ImagePrintObserver(), new ErrorCollectorObserver());
    assertThatCode(() -> ImageBatchAnalyzer.analyzeAllImages(tempDir, observers))
        .doesNotThrowAnyException();
  }

  @Test
  @DisplayName("Empty observer list is accepted")
  void emptyObserverListIsAccepted() {
    assertThatCode(() -> ImageBatchAnalyzer.analyzeAllImages(tempDir, List.of()))
        .doesNotThrowAnyException();
  }
}
