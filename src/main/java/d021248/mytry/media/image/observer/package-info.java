/// Observer interfaces and implementations for image analysis pipeline events.
///
/// `ImageAnalysisObserver` decouples the batch scanner from its consumers.
/// Implementations receive `onSuccess(ImageAnalysisResult)` per image or
/// `onError(Path, Exception)` per failure.
///
/// Built-in observers: `ImagePrintObserver`, `ImageDisplayObserver`,
/// `ImageScanStatsObserver`, and `ErrorCollectorObserver`.
///
/// @since 1.0
package d021248.mytry.media.image.observer;
