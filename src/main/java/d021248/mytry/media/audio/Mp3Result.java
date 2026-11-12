package d021248.mytry.media.audio;

import java.nio.file.Path;

/// Sealed result type for MP3 processing operations.
///
/// This sealed interface provides type-safe result handling using Java's
/// sealed types feature. It represents either a successful metadata extraction
/// or a failure with error information.
///
/// ## Pattern Matching Example:
///
/// ```java
/// Mp3Result result = Mp3Scanner.extractMetadata(path);
/// String message = switch (result) {
///     case Mp3Result.Success(var metadata) ->
///         "Found: " + metadata.title();
///     case Mp3Result.Failure(var path, var error, var exception) ->
///         "Error processing " + path + ": " + error;
/// };
/// ```
///
public sealed interface Mp3Result {

  /// Get the file path associated with this result.
  Path filePath();

  /// Check if this result represents a success.
  default boolean isSuccess() {
    return this instanceof Success;
  }

  /// Check if this result represents a failure.
  default boolean isFailure() {
    return this instanceof Failure;
  }

  /// Successful metadata extraction.
  ///
  /// @param metadata the extracted MP3 metadata
  record Success(Mp3Metadata metadata) implements Mp3Result {
    @Override
    public Path filePath() {
      return metadata.filePath();
    }

    @Override
    public String toString() {
      return "Success[%s - %s]"
          .formatted(
              metadata.primaryArtist().orElse("Unknown"), metadata.title().orElse("Unknown"));
    }
  }

  /// Failed metadata extraction.
  ///
  /// @param filePath     the path to the file that failed processing
  /// @param errorMessage a human-readable error description
  /// @param exception    the underlying exception (optional)
  record Failure(Path filePath, String errorMessage, Throwable exception) implements Mp3Result {

    /// Create a failure without an exception.
    public static Failure of(Path filePath, String errorMessage) {
      return new Failure(filePath, errorMessage, null);
    }

    /// Create a failure with an exception.
    public static Failure of(Path filePath, String errorMessage, Throwable exception) {
      return new Failure(filePath, errorMessage, exception);
    }

    @Override
    public String toString() {
      var msg = "Failure[%s: %s]".formatted(filePath.getFileName(), errorMessage);
      return exception != null ? msg + " - " + exception.getMessage() : msg;
    }
  }
}
