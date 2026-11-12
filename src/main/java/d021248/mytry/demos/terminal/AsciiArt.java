package d021248.mytry.demos.terminal;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/// Renders a text string as ASCII art by drawing it off-screen with {@code java.awt}
/// and mapping each dark pixel to a fill character.
public final class AsciiArt {

  /// {@code getRGB} value for a pure-black pixel in {@code TYPE_INT_RGB} images.
  private static final int BLACK_RGB = 0xFF000000;

  public static void main() {
    new AsciiArt()
        .drawString(
            "Rock'n'Roll",
            "#",
            new Settings(new Font("Times New Roman", Font.PLAIN, 32), 200, 200));
  }

  /// Renders {@code text} as ASCII art and returns it as a {@code String}.
  ///
  /// @param text     the text to render
  /// @param artChar  character used for filled (dark) pixels
  /// @param settings rendering settings
  /// @return multi-line ASCII art string
  public String renderToString(String text, String artChar, Settings settings) {
    var image = createImage(settings.width(), settings.height());
    var g2d = configureGraphics(image, settings);
    g2d.drawString(text, 6, (int) (settings.height() * 0.67));
    var sb = new StringBuilder();
    for (int y = 0; y < settings.height(); y++) {
      var row = new StringBuilder();
      for (int x = 0; x < settings.width(); x++) {
        row.append(image.getRGB(x, y) == BLACK_RGB ? ' ' : artChar);
      }
      if (!row.toString().isBlank()) sb.append(row).append('\n');
    }
    return sb.toString();
  }

  /// Renders {@code text} as ASCII art to {@code stdout}.
  public void drawString(String text, String artChar, Settings settings) {
    System.out.print(renderToString(text, artChar, settings));
  }

  private static BufferedImage createImage(int width, int height) {
    return new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
  }

  private static Graphics2D configureGraphics(BufferedImage image, Settings settings) {
    var g2d = (Graphics2D) image.getGraphics();
    g2d.setFont(settings.font());
    g2d.setRenderingHint(
        RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    return g2d;
  }

  /// Rendering settings for ASCII art generation.
  public record Settings(Font font, int width, int height) {}
}
