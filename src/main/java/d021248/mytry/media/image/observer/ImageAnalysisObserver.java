package d021248.mytry.media.image.observer;

import d021248.mytry.media.image.ImageAnalysisResult;
import java.nio.file.Path;

/// Observer interface for image analysis pipeline events.
///
/// Implementations receive a callback for each successfully analysed image
/// (`onSuccess`) or for each failure (`onError`), enabling loose coupling
/// between the batch scanner and its consumers.
///
/// The hierarchy is **sealed**: exactly the four bundled implementations are
/// permitted, enabling exhaustive `switch` dispatch over observer kinds.
public sealed interface ImageAnalysisObserver
    permits ErrorCollectorObserver,
        ImageDisplayObserver,
        ImagePrintObserver,
        ImageScanStatsObserver {

  void onSuccess(ImageAnalysisResult result);

  void onError(Path path, Exception cause);

  /// Called once after the directory walk completes, before analysis results arrive.
  ///
  /// Reports the total number of image files discovered in the scanned tree.
  /// Default implementation is a no-op; observers that need the total may override.
  default void onDiscovered(int totalFiles) {}
}
