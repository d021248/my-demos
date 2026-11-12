package d021248.mytry.media.audio;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/// Unit tests for `Mp3Result` sealed interface.
@DisplayName("Mp3Result")
class Mp3ResultTest {

  @Test
  @DisplayName("Success result is identifiable via isSuccess/isFailure predicates")
  void testSuccessResult() {
    var metadata = Mp3Metadata.builder(Path.of("/test/song.mp3")).title("Test").build();
    var result = new Mp3Result.Success(metadata);

    assertThat(result.isSuccess()).isTrue();
    assertThat(result.isFailure()).isFalse();
    assertThat(result.filePath()).isEqualTo(Path.of("/test/song.mp3"));
  }

  @Test
  @DisplayName("Failure result is identifiable and carries path and error message")
  void testFailureResult() {
    var result = Mp3Result.Failure.of(Path.of("/test/bad.mp3"), "Invalid file");

    assertThat(result.isSuccess()).isFalse();
    assertThat(result.isFailure()).isTrue();
    assertThat(result.filePath()).isEqualTo(Path.of("/test/bad.mp3"));
    assertThat(result.errorMessage()).isEqualTo("Invalid file");
    assertThat(result.exception()).isNull();
  }

  @Test
  @DisplayName("Failure with exception captures the exception")
  void testFailureWithException() {
    var exception = new RuntimeException("Test error");
    var result = Mp3Result.Failure.of(Path.of("/test/bad.mp3"), "Failed to read", exception);

    assertThat(result.errorMessage()).isEqualTo("Failed to read");
    assertThat(result.exception()).isSameAs(exception);
  }

  @Test
  @DisplayName("sealed type supports exhaustive pattern matching via switch expression")
  void testPatternMatching() {
    Mp3Result successResult =
        new Mp3Result.Success(Mp3Metadata.builder(Path.of("/test/song.mp3")).title("Test").build());
    Mp3Result failureResult = Mp3Result.Failure.of(Path.of("/test/bad.mp3"), "Error");

    var successMessage =
        switch (successResult) {
          case Mp3Result.Success s -> "Success: " + s.metadata().title().orElse("Unknown");
          case Mp3Result.Failure f -> "Failure: " + f.errorMessage();
        };
    var failureMessage =
        switch (failureResult) {
          case Mp3Result.Success s -> "Success: " + s.metadata().title().orElse("Unknown");
          case Mp3Result.Failure f -> "Failure: " + f.errorMessage();
        };

    assertThat(successMessage).startsWith("Success:");
    assertThat(failureMessage).startsWith("Failure:");
  }

  @Test
  @DisplayName("Success toString includes the artist and title values")
  void testSuccessToString() {
    var metadata =
        Mp3Metadata.builder(Path.of("/test/song.mp3")).artist("Artist").title("Title").build();
    var str = new Mp3Result.Success(metadata).toString();

    assertThat(str).contains("Success", "Artist", "Title");
  }

  @Test
  @DisplayName("Failure toString includes the file name and error message")
  void testFailureToString() {
    var str = Mp3Result.Failure.of(Path.of("/test/bad.mp3"), "Invalid format").toString();

    assertThat(str).contains("Failure", "bad.mp3", "Invalid format");
  }
}
