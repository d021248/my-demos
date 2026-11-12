package d021248.mytry.media.image;

import d021248.mytry.media.image.observer.ErrorCollectorObserver;
import d021248.mytry.media.image.observer.ImageAnalysisObserver;
import d021248.mytry.media.image.observer.ImageDisplayObserver;
import d021248.mytry.media.image.observer.ImagePrintObserver;
import d021248.mytry.media.image.observer.ImageScanStatsObserver;
import d021248.mytry.media.image.ui.JFrameImageDisplayService;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;

/// Scans and analyses images in a directory tree.
///
/// Usage: `java d021248.mytry.media.image.ImageScanApp [directory]`
/// Falls back to the configured default path when no argument is given.
public final class ImageScanApp {
  private static final Logger LOGGER = Logger.getLogger(ImageScanApp.class.getName());

  public static void main(String[] args) throws IOException {
    final var root =
        args.length > 0 ? Path.of(args[0]) : Path.of(System.getProperty("user.home"), "images");
    validateDirectory(root);

    final var scanStatsObserver = new ImageScanStatsObserver(20);
    final var errorCollectorObserver = new ErrorCollectorObserver();

    final List<ImageAnalysisObserver> observers = new ArrayList<>();
    JFrameImageDisplayService displayService = null;
    if (!GraphicsEnvironment.isHeadless()) {
      displayService = new JFrameImageDisplayService();
      observers.add(new ImageDisplayObserver(displayService));
    }
    observers.add(new ImagePrintObserver());
    observers.add(scanStatsObserver);
    observers.add(errorCollectorObserver);

    ImageBatchAnalyzer.analyzeAllImages(root, observers);

    scanStatsObserver.logSummary();
    printErrors(errorCollectorObserver);
    LOGGER.info(() -> "Done: all images scanned under %s".formatted(root));

    // Dispose the window on the EDT — runs after any pending display tasks, then
    // the
    // EDT stops (no more displayable windows) and the JVM can exit cleanly.
    if (displayService != null) {
      final var ds = displayService;
      SwingUtilities.invokeLater(ds::dispose);
    }
  }

  private static void validateDirectory(Path root) {
    if (root == null || !Files.exists(root) || !Files.isDirectory(root)) {
      throw new IllegalArgumentException("Not a valid directory: %s".formatted(root));
    }
    LOGGER.info(() -> "Scanning: %s".formatted(root));
  }

  private static void printErrors(ErrorCollectorObserver errorCollector) {
    if (errorCollector.isEmpty()) {
      return;
    }
    LOGGER.warning("Files that could not be analysed:");
    errorCollector.errorInfos().forEach(err -> LOGGER.warning(err.toString()));
  }
}
