package d021248.mytry.media.image;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.GpsDirectory;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/// Utility for loading an image and extracting its EXIF metadata.
///
/// Reads the file once into a byte array and passes the same bytes to
/// both `ImageIO` and `ImageMetadataReader`, avoiding a second disk read.
/// Use `analyze()` to get a fully populated `ImageAnalysisResult`.
public final class ImageAnalyzer {
  private static final Logger LOGGER = Logger.getLogger(ImageAnalyzer.class.getName());

  private final Path path;
  private final BufferedImage image;
  private final Metadata metadata;

  /// Constructs an `ImageAnalyzer` for the given image file.
  ///
  /// Metadata extraction failures (e.g. the `metadata-extractor` self-suppression
  /// bug on certain JPEGs) are logged and silently degraded to empty metadata —
  /// the image is still usable.
  ///
  /// @param imagePath path to the image file
  /// @throws IOException if the file cannot be read or the format is unsupported
  public ImageAnalyzer(Path imagePath) throws IOException {
    this.path = imagePath;
    final var imageBytes = Files.readAllBytes(imagePath);
    BufferedImage img = null;
    try (var imgStream = new ByteArrayInputStream(imageBytes)) {
      img = ImageIO.read(imgStream);
    } catch (Exception e) {
      // JDK bug: certain JPEGs trigger IAE "Self-suppression not permitted" inside
      // ImageIO
      throw new IOException("Failed to decode image: %s".formatted(imagePath), e);
    }
    if (img == null) {
      throw new IOException("Unsupported image format: %s".formatted(imagePath));
    }
    this.image = img;
    Metadata meta;
    try (var metaStream = new ByteArrayInputStream(imageBytes)) {
      meta = ImageMetadataReader.readMetadata(metaStream);
    } catch (Exception e) {
      // metadata-extractor has a known self-suppression bug for certain JPEGs;
      // degrade gracefully so the image is still displayed without metadata
      LOGGER.fine(() -> "Metadata unavailable for %s: %s".formatted(imagePath, e.getMessage()));
      meta = new Metadata();
    }
    this.metadata = meta;
  }

  /// Returns the decoded `BufferedImage`.
  public BufferedImage getImage() {
    return image;
  }

  /// Returns all EXIF/metadata tags as an unmodifiable map.
  /// Entries with a `null` name or description are silently skipped.
  public Map<String, String> getAllMetadata() {
    var tags = new HashMap<String, String>();
    for (var dir : metadata.getDirectories()) {
      for (var tag : dir.getTags()) {
        if (tag.getTagName() != null && tag.getDescription() != null) {
          tags.put(tag.getTagName(), tag.getDescription());
        }
      }
    }
    return Map.copyOf(tags);
  }

  /// Returns the GPS location if present in the EXIF data.
  public Optional<GeoLocation> getGeoLocation() {
    for (var gpsDir : metadata.getDirectoriesOfType(GpsDirectory.class)) {
      var loc = gpsDir.getGeoLocation();
      if (loc != null) {
        return Optional.of(new GeoLocation(loc.getLatitude(), loc.getLongitude()));
      }
    }
    return Optional.empty();
  }

  /// Returns Picasa face/name tags if present in the metadata.
  public Map<String, String> getPicasaNameTags() {
    var tags = new HashMap<String, String>();
    for (var dir : metadata.getDirectories()) {
      if (dir.getName().toLowerCase(Locale.ROOT).contains("picasa")) {
        for (var tag : dir.getTags()) {
          if (tag.getTagName() != null && tag.getDescription() != null) {
            tags.put(tag.getTagName(), tag.getDescription());
          }
        }
      }
    }
    return Map.copyOf(tags);
  }

  /// Bundles all extracted data into an `ImageAnalysisResult`.
  public ImageAnalysisResult analyze() {
    return new ImageAnalysisResult(path, image, getAllMetadata(), getGeoLocation());
  }
}
