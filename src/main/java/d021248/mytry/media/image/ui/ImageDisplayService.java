package d021248.mytry.media.image.ui;

import java.awt.image.BufferedImage;
import java.nio.file.Path;

/// Interface for displaying images. Allows for headless or alternative UI
/// implementations.
public interface ImageDisplayService {
  void displayImage(BufferedImage image, Path path);
}
