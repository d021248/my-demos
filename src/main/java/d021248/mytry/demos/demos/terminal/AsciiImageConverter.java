package d021248.mytry.demos.terminal;

import java.awt.Color;
import java.awt.image.BufferedImage;

/// Pure, Swing-free image-to-ASCII converter.
///
/// Converts a {@link BufferedImage} to a multi-line ASCII string by sampling
/// each pixel's luminance and mapping it to a character from
/// {@link CharPalette#IMAGE}.
///
/// This class contains no UI code; the Swing-based file picker lives in
/// {@link AsciiArtFromImage}.
///
public final class AsciiImageConverter {

  private static final CharPalette PALETTE = CharPalette.IMAGE;

  private final boolean negative;

  /// Creates a converter with the default (positive) palette direction.
  public AsciiImageConverter() {
    this(false);
  }

  /// Creates a converter.
  ///
  /// @param negative {@code true} to invert the palette (useful for dark backgrounds)
  public AsciiImageConverter(boolean negative) {
    this.negative = negative;
  }

  /// Converts the given image to an ASCII string.
  ///
  /// One ASCII character is emitted per source pixel.
  ///
  /// @param image source image
  /// @return multi-line ASCII representation
  public String convert(BufferedImage image) {
    var sb = new StringBuilder((image.getWidth() + 1) * image.getHeight());
    for (int y = 0; y < image.getHeight(); y++) {
      if (y > 0) sb.append('\n');
      for (int x = 0; x < image.getWidth(); x++) {
        var pixel = new Color(image.getRGB(x, y));
        // ITU-R BT.601 luminance (corrected: green/blue were swapped in original)
        double luminance =
            pixel.getRed() * 0.2989 + pixel.getGreen() * 0.5870 + pixel.getBlue() * 0.1140;
        sb.append(PALETTE.map(luminance, 255.0, negative));
      }
    }
    return sb.toString();
  }
}
