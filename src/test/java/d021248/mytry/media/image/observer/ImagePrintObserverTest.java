package d021248.mytry.media.image.observer;

import static org.assertj.core.api.Assertions.assertThatCode;

import d021248.mytry.media.image.ImageAnalysisResult;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ImagePrintObserverTest {

  private static ImageAnalysisResult result(Path path) {
    return new ImageAnalysisResult(
        path,
        new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB),
        Map.of("Width", "1920"),
        Optional.empty());
  }

  @Test
  @DisplayName("onSuccess does not throw")
  void onSuccessDoesNotThrow() {
    var observer = new ImagePrintObserver();
    assertThatCode(() -> observer.onSuccess(result(Paths.get("/test/image.jpg"))))
        .doesNotThrowAnyException();
  }

  @Test
  @DisplayName("onError does not throw")
  void onErrorDoesNotThrow() {
    var observer = new ImagePrintObserver();
    assertThatCode(() -> observer.onError(Paths.get("/test/error.jpg"), new Exception("boom")))
        .doesNotThrowAnyException();
  }

  @Test
  @DisplayName("Multiple onSuccess calls do not throw")
  void multipleOnSuccessCallsDoNotThrow() {
    var observer = new ImagePrintObserver();
    assertThatCode(
            () -> {
              observer.onSuccess(result(Paths.get("/img1.jpg")));
              observer.onSuccess(result(Paths.get("/img2.jpg")));
              observer.onSuccess(result(Paths.get("/img3.jpg")));
            })
        .doesNotThrowAnyException();
  }
}
