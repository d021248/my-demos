package d021248.mytry.demos;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Locale;
import javax.imageio.ImageIO;

/// IRobotDemo - Demonstrates Java Robot class for automated keyboard input and
/// screen capture.
///
/// **Platform:** Cross-platform - launches appropriate text editor based on OS.
public final class IRobotDemo {

  public static void main() throws IOException, InterruptedException, AWTException {
    String os = System.getProperty("os.name").toLowerCase(Locale.ROOT);

    // Modern pattern-matching switch (Java 21+) — compatible with Spotless
    String[] command =
        switch (os) {
          case String s when s.contains("win") -> new String[] {"notepad.exe"};
          case String s when s.contains("mac") -> new String[] {"open", "-a", "TextEdit"};
          default -> new String[] {"gedit"}; // Linux / WSL2
        };

    try {
      System.out.println("Launching text editor: " + String.join(" ", command));
      ProcessBuilder pb = new ProcessBuilder(command);
      pb.start();

      Thread.sleep(2000);
      Robot r = new Robot();
      write(r, "Van Halen rules");
    } catch (IOException e) {
      System.err.println("⚠️  Could not launch text editor: " + String.join(" ", command));
      System.err.println("Error: " + e.getMessage());
      System.err.println("\nContinuing with screen capture only...\n");
    }

    // Screen capture with modernized path output
    Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
    Robot robot = new Robot();
    BufferedImage img = robot.createScreenCapture(new Rectangle(size));
    File savePath = new File("screen.jpg");
    ImageIO.write(img, "JPG", savePath);
    System.out.println("Screenshot saved to: " + savePath.getAbsolutePath());
  }

  public static void write(Robot r, String s) throws InterruptedException {
    // Modern for-each loop over the character array
    for (char c : s.toCharArray()) {
      int k = KeyEvent.getExtendedKeyCodeForChar(c);

      // Guard against unknown characters
      if (k != KeyEvent.VK_UNDEFINED) {
        r.keyPress(k);
        r.keyRelease(k); // Important: release the key after pressing it!
      }
      Thread.sleep(200);
    }
  }
}
