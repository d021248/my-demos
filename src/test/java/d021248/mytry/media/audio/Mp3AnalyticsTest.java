package d021248.mytry.media.audio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/// Tests for `Mp3Analytics` custom collector.
///
/// **Educational Focus:** These tests demonstrate:
///
/// - How custom collectors work
/// - Single-pass vs multi-pass stream processing
/// - Parallel stream compatibility
/// - Immutability of collected results
@DisplayName("Mp3Analytics")
class Mp3AnalyticsTest {

  @Test
  @DisplayName("empty stream produces zero counts and no year info")
  void testEmptyStream() {
    var analytics = List.<Mp3Metadata>of().stream().collect(Mp3Analytics.collector());

    assertThat(analytics.metadata()).isEmpty();
    assertThat(analytics.artistCounts()).isEmpty();
    assertThat(analytics.genreCounts()).isEmpty();
    assertThat(analytics.hasYearInfo()).isFalse();
    assertThat(analytics.yearStats().getCount()).isZero();
  }

  @Test
  @DisplayName("single track is counted correctly across all dimensions")
  void testSingleTrack() {
    var metadata =
        Mp3Metadata.builder(Path.of("/test/song.mp3"))
            .title("Test Song")
            .artist("Test Artist")
            .album("Test Album")
            .genre("Rock")
            .year("2024")
            .build();

    var analytics = List.of(metadata).stream().collect(Mp3Analytics.collector());

    assertThat(analytics.metadata()).hasSize(1);
    assertThat(analytics.artistCounts()).hasSize(1).containsEntry("Test Artist", 1L);
    assertThat(analytics.genreCounts()).hasSize(1).containsEntry("Rock", 1L);
    assertThat(analytics.hasYearInfo()).isTrue();
    assertThat(analytics.yearStats().getMin()).isEqualTo(2024);
    assertThat(analytics.yearStats().getMax()).isEqualTo(2024);
  }

  @Test
  @DisplayName("multiple tracks accumulate artist and genre counts correctly")
  void testMultipleTracks() {
    var tracks =
        List.of(
            createTrack("Song 1", "Artist A", "Rock", 2020),
            createTrack("Song 2", "Artist A", "Rock", 2021),
            createTrack("Song 3", "Artist B", "Pop", 2022),
            createTrack("Song 4", "Artist B", "Pop", 2023),
            createTrack("Song 5", "Artist C", "Jazz", 2024));

    var analytics = tracks.stream().collect(Mp3Analytics.collector());

    assertThat(analytics.metadata()).hasSize(5);
    assertThat(analytics.artistCounts())
        .containsEntry("Artist A", 2L)
        .containsEntry("Artist B", 2L)
        .containsEntry("Artist C", 1L);
    assertThat(analytics.genreCounts())
        .containsEntry("Rock", 2L)
        .containsEntry("Pop", 2L)
        .containsEntry("Jazz", 1L);
    assertThat(analytics.yearStats().getCount()).isEqualTo(5);
    assertThat(analytics.yearStats().getMin()).isEqualTo(2020);
    assertThat(analytics.yearStats().getMax()).isEqualTo(2024);
    assertThat(analytics.yearStats().getAverage())
        .isCloseTo(2022.0, org.assertj.core.data.Offset.offset(0.01));
  }

  @Test
  @DisplayName("topArtists returns entries sorted by count descending")
  void testTopArtists() {
    var tracks =
        List.of(
            createTrack("Song 1", "Artist A", "Rock", 2020),
            createTrack("Song 2", "Artist A", "Rock", 2021),
            createTrack("Song 3", "Artist A", "Rock", 2022),
            createTrack("Song 4", "Artist B", "Pop", 2023),
            createTrack("Song 5", "Artist B", "Pop", 2024),
            createTrack("Song 6", "Artist C", "Jazz", 2024));

    var topArtists = tracks.stream().collect(Mp3Analytics.collector()).topArtists(2);

    assertThat(topArtists).hasSize(2);
    assertThat(topArtists.get(0).getKey()).isEqualTo("Artist A");
    assertThat(topArtists.get(0).getValue()).isEqualTo(3L);
    assertThat(topArtists.get(1).getKey()).isEqualTo("Artist B");
    assertThat(topArtists.get(1).getValue()).isEqualTo(2L);
  }

  @Test
  @DisplayName("topGenres returns entries sorted by count descending")
  void testTopGenres() {
    var tracks =
        List.of(
            createTrack("Song 1", "Artist A", "Rock", 2020),
            createTrack("Song 2", "Artist B", "Rock", 2021),
            createTrack("Song 3", "Artist C", "Rock", 2022),
            createTrack("Song 4", "Artist D", "Pop", 2023),
            createTrack("Song 5", "Artist E", "Pop", 2024));

    var topGenres = tracks.stream().collect(Mp3Analytics.collector()).topGenres(2);

    assertThat(topGenres).hasSize(2);
    assertThat(topGenres.get(0).getKey()).isEqualTo("Rock");
    assertThat(topGenres.get(0).getValue()).isEqualTo(3L);
    assertThat(topGenres.get(1).getKey()).isEqualTo("Pop");
    assertThat(topGenres.get(1).getValue()).isEqualTo(2L);
  }

  @Test
  @DisplayName("tracks with no artist or genre produce empty count maps")
  void testMissingMetadata() {
    var tracks =
        List.of(
            createTrackWithMissingData("Song 1"),
            createTrackWithMissingData("Song 2"),
            createTrackWithMissingData("Song 3"));

    var analytics = tracks.stream().collect(Mp3Analytics.collector());

    assertThat(analytics.metadata()).hasSize(3);
    assertThat(analytics.artistCounts()).isEmpty();
    assertThat(analytics.genreCounts()).isEmpty();
    assertThat(analytics.hasYearInfo()).isFalse();
  }

  @Test
  @DisplayName("parallel stream produces the same result as sequential")
  void testParallelStream() {
    var tracks =
        List.of(
            createTrack("Song 1", "Artist A", "Rock", 2020),
            createTrack("Song 2", "Artist A", "Rock", 2021),
            createTrack("Song 3", "Artist B", "Pop", 2022),
            createTrack("Song 4", "Artist B", "Pop", 2023),
            createTrack("Song 5", "Artist C", "Jazz", 2024),
            createTrack("Song 6", "Artist C", "Jazz", 2025),
            createTrack("Song 7", "Artist D", "Metal", 2026),
            createTrack("Song 8", "Artist D", "Metal", 2027));

    var sequential = tracks.stream().collect(Mp3Analytics.collector());
    var parallel = tracks.parallelStream().collect(Mp3Analytics.collector());

    assertThat(parallel.metadata()).hasSize(sequential.metadata().size());
    assertThat(parallel.artistCounts()).isEqualTo(sequential.artistCounts());
    assertThat(parallel.genreCounts()).isEqualTo(sequential.genreCounts());
    assertThat(parallel.yearStats().getCount()).isEqualTo(sequential.yearStats().getCount());
    assertThat(parallel.yearStats().getMin()).isEqualTo(sequential.yearStats().getMin());
    assertThat(parallel.yearStats().getMax()).isEqualTo(sequential.yearStats().getMax());
  }

  @Test
  @DisplayName("collected lists and maps are immutable")
  void testImmutability() {
    var analytics =
        List.of(createTrack("Song 1", "Artist A", "Rock", 2020)).stream()
            .collect(Mp3Analytics.collector());

    var extra = createTrack("New Song", "New Artist", "Pop", 2024);
    var metadata = analytics.metadata();
    var artistCounts = analytics.artistCounts();
    var genreCounts = analytics.genreCounts();
    assertThatThrownBy(() -> metadata.add(extra)).isInstanceOf(UnsupportedOperationException.class);
    assertThatThrownBy(() -> artistCounts.put("New Artist", 1L))
        .isInstanceOf(UnsupportedOperationException.class);
    assertThatThrownBy(() -> genreCounts.put("New Genre", 1L))
        .isInstanceOf(UnsupportedOperationException.class);
  }

  private Mp3Metadata createTrack(String title, String artist, String genre, int year) {
    return Mp3Metadata.builder(Path.of("/test/" + title + ".mp3"))
        .title(title)
        .artist(artist)
        .genre(genre)
        .year(String.valueOf(year))
        .build();
  }

  private Mp3Metadata createTrackWithMissingData(String title) {
    return Mp3Metadata.builder(Path.of("/test/" + title + ".mp3")).title(title).build();
  }
}
