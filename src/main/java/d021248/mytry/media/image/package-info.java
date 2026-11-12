/// Image metadata extraction, analysis, and batch scanning.
///
/// ## Core types
///
/// - `ImageAnalyzer` — loads an image file once, exposes pixel data and EXIF metadata
/// - `ImageBatchAnalyzer` — walks a directory tree and analyses each image on a virtual thread
/// - `ImageAnalysisResult` — immutable record carrying path, image, metadata, and GPS location
/// - `GeoLocation` — GPS coordinate record (latitude, longitude)
///
/// ## Observer pattern
///
/// `ImageAnalysisObserver` (see sub-package `observer`) decouples the scanner from its consumers.
/// Implementations receive `onSuccess(ImageAnalysisResult)` per image or `onError(Path, Exception)`
/// per failure.
///
/// ## Usage
///
/// ```java
/// var observers = List.of(new ImagePrintObserver(), new ErrorCollectorObserver());
/// ImageBatchAnalyzer.analyzeAllImages(Path.of("/photos"), observers);
/// ```
///
/// @since 1.0
package d021248.mytry.media.image;
