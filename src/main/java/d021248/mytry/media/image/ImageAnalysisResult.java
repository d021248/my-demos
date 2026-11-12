package d021248.mytry.media.image;

import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

/// Carrier for one analysed image: pixel data, EXIF metadata, and GPS location.
///
/// The record fields are structurally final, but `BufferedImage` itself is mutable.
/// Observers must not modify `image` after receiving it.
public record ImageAnalysisResult(
    Path path, BufferedImage image, Map<String, String> metadata, Optional<GeoLocation> location) {}
