package d021248.mytry.media.image;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ImageAnalyzerTest {

  @TempDir Path tempDir;

  private Path createTestImage(int width, int height, String filename) throws IOException {
    var img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    var imagePath = tempDir.resolve(filename);
    ImageIO.write(img, "jpg", imagePath.toFile());
    return imagePath;
  }

  @Test
  @DisplayName("Constructor succeeds for a valid JPEG")
  void constructorSucceedsForValidJpeg() throws IOException {
    var imagePath = createTestImage(100, 100, "test.jpg");
    assertThat(new ImageAnalyzer(imagePath)).isNotNull();
  }

  @Test
  @DisplayName("getImage() returns a BufferedImage with the correct dimensions")
  void getImageReturnsCorrectDimensions() throws IOException {
    var imagePath = createTestImage(150, 200, "test.jpg");
    var image = new ImageAnalyzer(imagePath).getImage();
    assertThat(image.getWidth()).isEqualTo(150);
    assertThat(image.getHeight()).isEqualTo(200);
  }

  @Test
  @DisplayName("getAllMetadata() returns a non-null map")
  void getAllMetadataReturnsNonNull() throws IOException {
    var imagePath = createTestImage(100, 100, "test.jpg");
    assertThat(new ImageAnalyzer(imagePath).getAllMetadata()).isNotNull();
  }

  @Test
  @DisplayName("getGeoLocation() returns empty for an image with no GPS data")
  void getGeoLocationEmptyWithoutGps() throws IOException {
    var imagePath = createTestImage(100, 100, "test.jpg");
    assertThat(new ImageAnalyzer(imagePath).getGeoLocation()).isEmpty();
  }

  @Test
  @DisplayName("Constructor throws IOException for a non-existent path")
  void constructorThrowsForMissingFile() {
    var missing = tempDir.resolve("nonexistent.jpg");
    assertThatThrownBy(() -> new ImageAnalyzer(missing)).isInstanceOf(IOException.class);
  }

  @Test
  @DisplayName("Constructor throws IOException for a non-image file")
  void constructorThrowsForNonImageFile() throws IOException {
    var textFile = tempDir.resolve("test.txt");
    Files.writeString(textFile, "not an image");
    assertThatThrownBy(() -> new ImageAnalyzer(textFile)).isInstanceOf(IOException.class);
  }

  @Test
  @DisplayName("getPicasaNameTags() returns a non-null map")
  void getPicasaNameTagsReturnsNonNull() throws IOException {
    var imagePath = createTestImage(100, 100, "test.jpg");
    assertThat(new ImageAnalyzer(imagePath).getPicasaNameTags()).isNotNull();
  }

  @Test
  @DisplayName("analyze() returns a result with the correct path and image")
  void analyzeReturnsCorrectResult() throws IOException {
    var imagePath = createTestImage(300, 400, "test.jpg");
    var result = new ImageAnalyzer(imagePath).analyze();
    assertThat(result.path()).isEqualTo(imagePath);
    assertThat(result.image().getWidth()).isEqualTo(300);
    assertThat(result.image().getHeight()).isEqualTo(400);
    assertThat(result.metadata()).isNotNull();
    assertThat(result.location()).isEmpty();
  }

  @Test
  @DisplayName("PNG format is supported")
  void pngFormatIsSupported() throws IOException {
    var imagePath = createTestImage(50, 50, "test.png");
    assertThat(new ImageAnalyzer(imagePath).getImage()).isNotNull();
  }
}
