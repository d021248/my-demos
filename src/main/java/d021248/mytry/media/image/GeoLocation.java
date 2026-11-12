package d021248.mytry.media.image;

import java.util.Locale;

/// GPS coordinates extracted from image EXIF data.
public record GeoLocation(double latitude, double longitude) {

  @Override
  public String toString() {
    return String.format(Locale.ROOT, "%.6f, %.6f", latitude, longitude);
  }
}
