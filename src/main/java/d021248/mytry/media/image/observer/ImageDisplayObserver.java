package d021248.mytry.media.image.observer;

import d021248.mytry.media.image.ImageAnalysisResult;
import d021248.mytry.media.image.ui.ImageDisplayService;
import java.nio.file.Path;
import java.util.logging.Logger;

/// Observer that displays each analysed image via an `ImageDisplayService`.
///
/// Uses the `BufferedImage` already held in `ImageAnalysisResult` — no second
/// disk read.
public final class ImageDisplayObserver implements ImageAnalysisObserver {
  private static final Logger LOGGER = Logger.getLogger(ImageDisplayObserver.class.getName());
  private final ImageDisplayService displayService;

  public ImageDisplayObserver(ImageDisplayService displayService) {
    this.displayService = displayService;
  }

  @Override
  public void onSuccess(ImageAnalysisResult result) {
    displayService.displayImage(result.image(), result.path());
  }

  @Override
  public void onError(Path path, Exception cause) {
    LOGGER.warning(() -> "[Display] %s: %s".formatted(path, cause.getMessage()));
  }
}
