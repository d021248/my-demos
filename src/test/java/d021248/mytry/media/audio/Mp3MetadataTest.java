package d021248.mytry.media.audio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Path;
import java.time.Year;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/// Unit tests for `Mp3Metadata` record.
@DisplayName("Mp3Metadata")
class Mp3MetadataTest {

  @Test
  @DisplayName("builder with all fields populates every component")
  void testBuilderWithAllFields() {
    var metadata =
        Mp3Metadata.builder(Path.of("/test/song.mp3"))
            .artist("The Beatles")
            .albumArtist("The Beatles")
            .album("Abbey Road")
            .title("Come Together")
            .track("1")
            .year("1969")
            .genre("Rock")
            .composer("Lennon-McCartney")
            .publisher("Apple Records")
            .comment("Remastered")
            .build();

    assertThat(metadata.artist()).contains("The Beatles");
    assertThat(metadata.album()).contains("Abbey Road");
    assertThat(metadata.title()).contains("Come Together");
    assertThat(metadata.year()).contains(Year.of(1969));
  }

  @Test
  @DisplayName("omitted fields are empty Optionals")
  void testBuilderWithMissingFields() {
    var metadata = Mp3Metadata.builder(Path.of("/test/song.mp3")).title("Unknown Song").build();

    assertThat(metadata.artist()).isEmpty();
    assertThat(metadata.album()).isEmpty();
    assertThat(metadata.title()).contains("Unknown Song");
  }

  @Test
  @DisplayName("primaryArtist prefers artist over albumArtist")
  void testPrimaryArtist() {
    var artistOnly = Mp3Metadata.builder(Path.of("/test/song.mp3")).artist("Artist A").build();
    assertThat(artistOnly.primaryArtist()).contains("Artist A");

    var albumArtistOnly =
        Mp3Metadata.builder(Path.of("/test/song.mp3")).albumArtist("Album Artist").build();
    assertThat(albumArtistOnly.primaryArtist()).contains("Album Artist");

    var both =
        Mp3Metadata.builder(Path.of("/test/song.mp3"))
            .artist("Artist A")
            .albumArtist("Album Artist")
            .build();
    assertThat(both.primaryArtist()).contains("Artist A");
  }

  @Test
  @DisplayName("toCsv includes all present fields")
  void testToCsv() {
    var csv =
        Mp3Metadata.builder(Path.of("/test/song.mp3"))
            .albumArtist("The Beatles")
            .album("Abbey Road")
            .track("1")
            .title("Come Together")
            .artist("The Beatles")
            .genre("Rock")
            .year("1969")
            .build()
            .toCsv();

    assertThat(csv).contains("The Beatles", "Abbey Road", "Come Together", "1969");
  }

  @Test
  @DisplayName("toCsv sanitizes semicolons and newlines in comment field")
  void testCsvSanitization() {
    var csv =
        Mp3Metadata.builder(Path.of("/test/song.mp3"))
            .comment("Contains; semicolons\nand newlines")
            .build()
            .toCsv();

    assertThat(csv).doesNotContain("\n").contains("; ");
  }

  @Test
  @DisplayName("toJson contains key-value pairs for present fields")
  void testToJson() {
    var json =
        Mp3Metadata.builder(Path.of("/test/song.mp3"))
            .title("Test Song")
            .artist("Test Artist")
            .build()
            .toJson();

    assertThat(json).contains("\"title\": \"Test Song\"", "\"artist\": \"Test Artist\"");
  }

  @Test
  @DisplayName("builder rejects null file path")
  void testRequiresFilePath() {
    assertThatThrownBy(() -> Mp3Metadata.builder(null)).isInstanceOf(NullPointerException.class);
  }

  @Test
  @DisplayName("invalid year string yields empty Optional")
  void testInvalidYear() {
    var metadata = Mp3Metadata.builder(Path.of("/test/song.mp3")).year("not-a-year").build();

    assertThat(metadata.year()).isEmpty();
  }

  @Test
  @DisplayName("Year object is stored and retrieved without conversion")
  void testYearObject() {
    var metadata = Mp3Metadata.builder(Path.of("/test/song.mp3")).year(Year.of(2023)).build();

    assertThat(metadata.year()).contains(Year.of(2023));
  }
}
