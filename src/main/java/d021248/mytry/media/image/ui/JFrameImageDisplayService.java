package d021248.mytry.media.image.ui;

import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

/// JFrame-based implementation of `ImageDisplayService`.
///
/// Image scaling is done on the calling (virtual) thread so the Event Dispatch
/// Thread only receives a pre-built `ImageIcon` — keeping the UI responsive even
/// when hundreds of images arrive concurrently.
@SuppressWarnings({"this-escape", "serial"})
public final class JFrameImageDisplayService extends JFrame implements ImageDisplayService {
  private static final long serialVersionUID = 1L;

  private final JLabel label;

  public JFrameImageDisplayService() {
    super("Image Viewer");
    setSize(800, 600);
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    label = new JLabel("", SwingConstants.CENTER);
    add(label);
    setVisible(true);
  }

  @Override
  public void displayImage(BufferedImage src, Path path) {
    if (src == null) {
      SwingUtilities.invokeLater(() -> showPlaceholder("Could not load: " + path));
      return;
    }
    // Scales off-EDT — getWidth()/getHeight() are simple field reads; slightly
    // stale size is fine.
    final var scaled = scale(src, Math.max(getWidth(), 200), Math.max(getHeight(), 150));
    final var icon = new ImageIcon(scaled);
    final var name = path.getFileName().toString();
    final var text = path.toString();
    SwingUtilities.invokeLater(
        () -> {
          label.setIcon(icon);
          label.setText(text);
          setTitle(name);
        });
  }

  private static BufferedImage scale(BufferedImage src, int maxW, int maxH) {
    final var ratio = Math.min((double) maxW / src.getWidth(), (double) maxH / src.getHeight());
    final var w = Math.max((int) (src.getWidth() * ratio), 1);
    final var h = Math.max((int) (src.getHeight() * ratio), 1);
    final var dst = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
    final var g = dst.createGraphics();
    g.setRenderingHint(
        RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    g.drawImage(src, 0, 0, w, h, null);
    g.dispose();
    return dst;
  }

  private void showPlaceholder(String msg) {
    label.setIcon(null);
    label.setText(msg);
    setTitle(msg);
  }
}
