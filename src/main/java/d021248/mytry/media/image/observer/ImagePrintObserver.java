package d021248.mytry.media.image.observer;

import d021248.mytry.media.image.ImageAnalysisResult;
import java.nio.file.Path;
import java.util.logging.Logger;

/// Observer that logs the path of each analysed image using the standard Java logging API.
public final class ImagePrintObserver implements ImageAnalysisObserver {
  private static final Logger LOGGER = Logger.getLogger(ImagePrintObserver.class.getName());

  @Override
  public void onSuccess(ImageAnalysisResult result) {
    LOGGER.info(() -> "Analysed: %s".formatted(result.path()));
  }

  @Override
  public void onError(Path path, Exception cause) {
    LOGGER.warning(() -> "[Error] %s: %s".formatted(path, cause.getMessage()));
  }
}
