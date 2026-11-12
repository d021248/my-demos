package d021248.mytry.media.image.observer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import d021248.mytry.media.image.GeoLocation;
import d021248.mytry.media.image.ImageAnalysisResult;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ImageScanStatsObserverTest {

  private static ImageAnalysisResult result(Path path, int w, int h, boolean withGps) {
    var img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
    var loc =
        withGps ? Optional.of(new GeoLocation(48.8566, 2.3522)) : Optional.<GeoLocation>empty();
    return new ImageAnalysisResult(path, img, Map.of(), loc);
  }

  @Test
  @DisplayName("New observer has zero success and error counts")
  void newObserverHasZeroCounts() {
    var observer = new ImageScanStatsObserver(10);
    assertThat(observer.getSuccessCount()).isZero();
    assertThat(observer.getErrorCount()).isZero();
  }

  @Test
  @DisplayName("onSuccess increments success count")
  void onSuccessIncrementsSuccessCount() {
    var observer = new ImageScanStatsObserver(10);
    observer.onSuccess(result(Paths.get("/a.jpg"), 100, 100, false));
    observer.onSuccess(result(Paths.get("/b.jpg"), 200, 200, false));
    assertThat(observer.getSuccessCount()).isEqualTo(2);
    assertThat(observer.getErrorCount()).isZero();
  }

  @Test
  @DisplayName("onError increments error count")
  void onErrorIncrementsErrorCount() {
    var observer = new ImageScanStatsObserver(10);
    observer.onError(Paths.get("/err.jpg"), new Exception("boom"));
    observer.onError(Paths.get("/err2.jpg"), new Exception("oops"));
    assertThat(observer.getErrorCount()).isEqualTo(2);
    assertThat(observer.getSuccessCount()).isZero();
  }

  @Test
  @DisplayName("logSummary does not throw for a fresh observer")
  void logSummaryDoesNotThrowWhenEmpty() {
    var observer = new ImageScanStatsObserver(10);
    // no-op if nothing was processed; must not throw divide-by-zero
    assertThatCode(observer::logSummary).doesNotThrowAnyException();
  }

  @Test
  @DisplayName("logSummary does not throw after successful analyses")
  void logSummaryDoesNotThrowAfterSuccess() {
    var observer = new ImageScanStatsObserver(1);
    observer.onSuccess(result(Paths.get("/img.jpg"), 100, 100, true));
    assertThatCode(observer::logSummary).doesNotThrowAnyException();
  }

  @Test
  @DisplayName("Success and error counts are independent")
  void successAndErrorCountsAreIndependent() {
    var observer = new ImageScanStatsObserver(10);
    observer.onSuccess(result(Paths.get("/ok.jpg"), 10, 10, false));
    observer.onError(Paths.get("/bad.jpg"), new Exception("fail"));
    assertThat(observer.getSuccessCount()).isEqualTo(1);
    assertThat(observer.getErrorCount()).isEqualTo(1);
  }
}
