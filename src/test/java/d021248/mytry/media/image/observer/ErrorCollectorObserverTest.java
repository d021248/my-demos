package d021248.mytry.media.image.observer;

import static org.assertj.core.api.Assertions.assertThat;

import d021248.mytry.media.image.GeoLocation;
import d021248.mytry.media.image.ImageAnalysisResult;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ErrorCollectorObserverTest {

  private static ImageAnalysisResult dummyResult(Path path) {
    return new ImageAnalysisResult(
        path, new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB), Map.of(), Optional.empty());
  }

  @Test
  @DisplayName("New observer has no errors")
  void newObserverHasNoErrors() {
    assertThat(new ErrorCollectorObserver().isEmpty()).isTrue();
  }

  @Test
  @DisplayName("onError adds an entry")
  void onErrorAddsEntry() {
    var observer = new ErrorCollectorObserver();
    observer.onError(Paths.get("/test/path.jpg"), new Exception("boom"));
    assertThat(observer.isEmpty()).isFalse();
  }

  @Test
  @DisplayName("errorInfos() returns all recorded errors")
  void errorInfosReturnsAllErrors() {
    var observer = new ErrorCollectorObserver();
    observer.onError(Paths.get("/a.jpg"), new Exception("1"));
    observer.onError(Paths.get("/b.jpg"), new Exception("2"));
    observer.onError(Paths.get("/c.jpg"), new Exception("3"));
    assertThat(observer.errorInfos()).hasSize(3);
  }

  @Test
  @DisplayName("onSuccess does not affect the error collection")
  void onSuccessDoesNotAffectErrors() {
    var observer = new ErrorCollectorObserver();
    observer.onError(Paths.get("/err.jpg"), new Exception("oops"));
    observer.onSuccess(dummyResult(Paths.get("/ok.jpg")));
    assertThat(observer.errorInfos()).hasSize(1);
  }

  @Test
  @DisplayName("ErrorInfo.toString() contains the file name")
  void errorInfoToStringContainsFileName() {
    var observer = new ErrorCollectorObserver();
    observer.onError(Paths.get("/test.jpg"), new Exception("Test message"));
    var info = observer.errorInfos().iterator().next();
    assertThat(info.toString()).contains("test.jpg");
  }

  @Test
  @DisplayName("ErrorInfo carries path and cause")
  void errorInfoCarriesPathAndCause() {
    var observer = new ErrorCollectorObserver();
    var path = Paths.get("/test.jpg");
    var cause = new Exception("oops");
    observer.onError(path, cause);
    var info = observer.errorInfos().iterator().next();
    assertThat(info.path()).isEqualTo(path);
    assertThat(info.cause()).isSameAs(cause);
  }

  @Test
  @DisplayName("GeoLocation in result is accessible through ErrorInfo path")
  void dummyResultWithGeoLocation() {
    var result =
        new ImageAnalysisResult(
            Paths.get("/geo.jpg"),
            new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB),
            Map.of(),
            Optional.of(new GeoLocation(48.8566, 2.3522)));
    assertThat(result.location()).isPresent();
    assertThat(result.location().get().latitude()).isEqualTo(48.8566);
  }
}
