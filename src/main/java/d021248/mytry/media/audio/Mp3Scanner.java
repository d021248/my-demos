package d021248.mytry.media.audio;

import com.mpatric.mp3agic.Mp3File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.StructuredTaskScope;
import java.util.logging.Logger;

/// Modern MP3 metadata scanner using virtual threads via `StructuredTaskScope`.
///
/// One virtual thread is forked per file; a configurable deadline prevents
/// indefinite hangs on corrupt or network-mounted files.
///
/// ## Usage Example:
///
/// ```java
/// try (var scanner = new Mp3Scanner()) {
///     List<Mp3Result> results = scanner.scanDirectory(Path.of("/music"));
///
///     results.stream()
///             .filter(Mp3Result::isSuccess)
///             .map(r -> (Mp3Result.Success) r)
///             .forEach(s -> System.out.println(s.metadata().toCsv()));
/// }
/// ```
///
public final class Mp3Scanner implements AutoCloseable {

  private static final Logger LOGGER = Logger.getLogger(Mp3Scanner.class.getName());
  // Give up after this many seconds — prevents hanging on corrupt or network-mounted files.
  private static final int SCAN_TIMEOUT_SECS = 120;

  public Mp3Scanner() {}

  /// Extract metadata from a single MP3 file.
  ///
  /// @param filePath path to the MP3 file
  /// @return result containing either metadata or error information
  public static Mp3Result extractMetadata(Path filePath) {
    if (!Files.isRegularFile(filePath)) {
      return Mp3Result.Failure.of(filePath, "Not a regular file");
    }
    if (!filePath.toString().toLowerCase(Locale.ROOT).endsWith(".mp3")) {
      return Mp3Result.Failure.of(filePath, "Not an MP3 file");
    }
    try {
      var mp3File = new Mp3File(filePath.toFile());
      if (!mp3File.hasId3v2Tag()) {
        return Mp3Result.Failure.of(filePath, "No ID3v2 tag found");
      }
      var tag = mp3File.getId3v2Tag();
      var metadata =
          Mp3Metadata.builder(filePath)
              .albumArtist(tag.getAlbumArtist())
              .artist(tag.getArtist())
              .album(tag.getAlbum())
              .title(tag.getTitle())
              .track(tag.getTrack())
              .year(tag.getYear())
              .genre(tag.getGenreDescription())
              .composer(tag.getComposer())
              .publisher(tag.getPublisher())
              .comment(tag.getComment())
              .build();
      return new Mp3Result.Success(metadata);
    } catch (Exception e) {
      return Mp3Result.Failure.of(filePath, "Failed to read MP3 file", e);
    }
  }

  /// Scan a directory for MP3 files and extract metadata.
  ///
  /// @param directory the directory to scan
  /// @return list of results (both successes and failures)
  /// @throws IOException if directory cannot be read
  public List<Mp3Result> scanDirectory(Path directory) throws IOException {
    return scanDirectory(directory, Integer.MAX_VALUE);
  }

  /// Scan a directory for MP3 files with a depth limit.
  ///
  /// One virtual thread is forked per file via `StructuredTaskScope`. The scope
  /// is given a `SCAN_TIMEOUT_SECS` deadline; on timeout all remaining subtasks
  /// are cancelled before returning.
  ///
  /// @param directory the directory to scan
  /// @param maxDepth  maximum directory depth to traverse
  /// @return list of results (both successes and failures)
  /// @throws IOException if directory cannot be read
  public List<Mp3Result> scanDirectory(Path directory, int maxDepth) throws IOException {
    final var tasks = new ArrayList<StructuredTaskScope.Subtask<Mp3Result>>();
    try (var scope =
        StructuredTaskScope.open(
            StructuredTaskScope.Joiner.<Mp3Result>awaitAllSuccessfulOrThrow(),
            cfg -> cfg.withTimeout(Duration.ofSeconds(SCAN_TIMEOUT_SECS)))) {
      try (var paths = Files.walk(directory, maxDepth)) {
        paths
            .filter(Files::isRegularFile)
            .filter(p -> p.toString().toLowerCase(Locale.ROOT).endsWith(".mp3"))
            .forEach(p -> tasks.add(scope.fork(() -> extractMetadata(p))));
      }
      scope.join();
    } catch (StructuredTaskScope.FailedException e) {
      // extractMetadata never throws — only reachable if an Error escapes
      LOGGER.warning(() -> "Unexpected subtask failure: %s".formatted(e.getCause()));
    } catch (InterruptedException _) {
      Thread.currentThread().interrupt();
      return List.of();
    }
    return tasks.stream()
        .filter(t -> t.state() == StructuredTaskScope.Subtask.State.SUCCESS)
        .map(StructuredTaskScope.Subtask::get)
        .toList();
  }

  @Override
  public void close() {
    /* no resources held — AutoCloseable enables try-with-resources */
  }

  /// Statistics about scanning results.
  public record ScanStatistics(
      int totalFiles, int successCount, int failureCount, long durationMillis) {

    public double successRate() {
      return totalFiles == 0 ? 0.0 : (double) successCount / totalFiles * 100;
    }

    @Override
    public String toString() {
      return String.format(
          Locale.ROOT,
          """
					Scan Statistics:
					  Total files:    %d
					  Successful:     %d (%.1f%%)
					  Failed:         %d
					  Duration:       %d ms
					""",
          totalFiles,
          successCount,
          successRate(),
          failureCount,
          durationMillis);
    }
  }
}
