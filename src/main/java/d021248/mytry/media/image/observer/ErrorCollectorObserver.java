package d021248.mytry.media.image.observer;

import d021248.mytry.media.image.ImageAnalysisResult;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/// Observer that collects errors during batch image analysis for later reporting.
public final class ErrorCollectorObserver implements ImageAnalysisObserver {

  /// Carries the path and cause of a single analysis failure.
  public record ErrorInfo(Path path, Exception cause) {

    @Override
    public String toString() {
      return "%s: %s: %s".formatted(path, cause.getClass().getSimpleName(), cause.getMessage());
    }
  }

  // Keyed by path — one error entry per file; first error for a given path is
  // kept.
  private final ConcurrentHashMap<Path, ErrorInfo> errors = new ConcurrentHashMap<>();

  @Override
  public void onSuccess(ImageAnalysisResult result) {
    /* errors only */
  }

  @Override
  public void onError(Path path, Exception cause) {
    errors.putIfAbsent(path, new ErrorInfo(path, cause));
  }

  /// Returns true if no errors have been recorded.
  public boolean isEmpty() {
    return errors.isEmpty();
  }

  /// Returns all recorded errors, sorted by path for deterministic output.
  public List<ErrorInfo> errorInfos() {
    return errors.values().stream().sorted(Comparator.comparing(ErrorInfo::path)).toList();
  }
}
