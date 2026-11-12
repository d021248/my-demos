package d021248.mytry.media.image.observer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import d021248.mytry.media.image.ImageAnalysisResult;
import d021248.mytry.media.image.ui.ImageDisplayService;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ImageDisplayObserverTest {

  private static ImageAnalysisResult result(Path path) {
    return new ImageAnalysisResult(
        path, new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB), Map.of(), Optional.empty());
  }

  @Test
  @DisplayName("onSuccess delegates to the display service without re-reading the file")
  void onSuccessDelegatesToDisplayService() {
    var received = new ArrayList<Path>();
    ImageDisplayService spy = (img, path) -> received.add(path);

    var observer = new ImageDisplayObserver(spy);
    var path = Paths.get("/photos/cat.jpg");
    observer.onSuccess(result(path));

    assertThat(received).containsExactly(path);
  }

  @Test
  @DisplayName("onError does not throw")
  void onErrorDoesNotThrow() {
    var observer = new ImageDisplayObserver((img, path) -> {});
    assertThatCode(() -> observer.onError(Paths.get("/err.jpg"), new Exception("boom")))
        .doesNotThrowAnyException();
  }

  @Test
  @DisplayName("Multiple onSuccess calls are all forwarded")
  void multipleSuccessCallsAreForwarded() {
    var count = new int[1];
    ImageDisplayService counter = (img, path) -> count[0]++;

    var observer = new ImageDisplayObserver(counter);
    observer.onSuccess(result(Paths.get("/a.jpg")));
    observer.onSuccess(result(Paths.get("/b.jpg")));

    assertThat(count[0]).isEqualTo(2);
  }
}
