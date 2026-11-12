package d021248.mytry.media.audio;

import java.nio.file.Path;
import java.time.Year;
import java.util.Objects;
import java.util.Optional;

/// Immutable MP3 metadata extracted from ID3 tags.
///
/// This record encapsulates all relevant metadata from an MP3 file,
/// providing a type-safe, immutable representation with validation.
///
/// ## Features:
///
/// - Immutable data carrier using Java records
/// - Optional fields for missing metadata
/// - CSV and JSON export capabilities
/// - Null-safe construction with validation
///
/// @param filePath    the path to the MP3 file
/// @param albumArtist the album artist (may differ from track artist)
/// @param artist      the track artist
/// @param album       the album name
/// @param title       the track title
/// @param track       the track number
/// @param year        the release year
/// @param genre       the music genre
/// @param composer    the composer
/// @param publisher   the publisher/label
/// @param comment     additional comments
///
public record Mp3Metadata(
    Path filePath,
    Optional<String> albumArtist,
    Optional<String> artist,
    Optional<String> album,
    Optional<String> title,
    Optional<String> track,
    Optional<Year> year,
    Optional<String> genre,
    Optional<String> composer,
    Optional<String> publisher,
    Optional<String> comment) {

  /// Compact constructor with validation.
  public Mp3Metadata {
    Objects.requireNonNull(filePath, "File path cannot be null");

    // Ensure all Optional fields are non-null
    albumArtist = Objects.requireNonNull(albumArtist, "albumArtist cannot be null");
    artist = Objects.requireNonNull(artist, "artist cannot be null");
    album = Objects.requireNonNull(album, "album cannot be null");
    title = Objects.requireNonNull(title, "title cannot be null");
    track = Objects.requireNonNull(track, "track cannot be null");
    year = Objects.requireNonNull(year, "year cannot be null");
    genre = Objects.requireNonNull(genre, "genre cannot be null");
    composer = Objects.requireNonNull(composer, "composer cannot be null");
    publisher = Objects.requireNonNull(publisher, "publisher cannot be null");
    comment = Objects.requireNonNull(comment, "comment cannot be null");
  }

  /// Get the primary artist, falling back to album artist if track artist is not
  /// available.
  public Optional<String> primaryArtist() {
    return artist.or(() -> albumArtist);
  }

  /// Export metadata as CSV line.
  /// Format: AlbumArtist; Album; Track; Title; Artist; Genre; Composer; Year;
  /// Publisher; Comment
  public String toCsv() {
    return String.join(
        "; ",
        albumArtist.or(() -> artist).orElse(""),
        album.orElse(""),
        track.orElse(""),
        title.orElse(""),
        artist.orElse(""),
        genre.orElse(""),
        composer.orElse(""),
        year.map(Year::toString).orElse(""),
        publisher.orElse(""),
        sanitize(comment.orElse("")));
  }

  /// Export metadata as JSON object (simple representation).
  public String toJson() {
    return """
				{
				  "filePath": "%s",
				  "albumArtist": "%s",
				  "artist": "%s",
				  "album": "%s",
				  "title": "%s",
				  "track": "%s",
				  "year": "%s",
				  "genre": "%s",
				  "composer": "%s",
				  "publisher": "%s",
				  "comment": "%s"
				}"""
        .formatted(
            filePath,
            albumArtist.orElse(""),
            artist.orElse(""),
            album.orElse(""),
            title.orElse(""),
            track.orElse(""),
            year.map(Year::toString).orElse(""),
            genre.orElse(""),
            composer.orElse(""),
            publisher.orElse(""),
            sanitize(comment.orElse("")));
  }

  /// Sanitize string for CSV output by removing semicolons and newlines.
  private static String sanitize(String value) {
    if (value.isBlank()) {
      return "";
    }
    return value.replace(";", "/").replace("\n", " ").replace("\r", " ").trim();
  }

  /// Builder for constructing Mp3Metadata with a fluent API.
  public static Builder builder(Path filePath) {
    return new Builder(filePath);
  }

  /// Fluent builder for Mp3Metadata.
  public static final class Builder {
    private final Path filePath;
    private String albumArtist;
    private String artist;
    private String album;
    private String title;
    private String track;
    private Year year;
    private String genre;
    private String composer;
    private String publisher;
    private String comment;

    private Builder(Path filePath) {
      this.filePath = Objects.requireNonNull(filePath);
    }

    public Builder albumArtist(String albumArtist) {
      this.albumArtist = albumArtist;
      return this;
    }

    public Builder artist(String artist) {
      this.artist = artist;
      return this;
    }

    public Builder album(String album) {
      this.album = album;
      return this;
    }

    public Builder title(String title) {
      this.title = title;
      return this;
    }

    public Builder track(String track) {
      this.track = track;
      return this;
    }

    public Builder year(String yearString) {
      if (yearString != null && !yearString.isBlank()) {
        try {
          this.year = Year.parse(yearString);
        } catch (Exception _) {
          // Invalid year format - leave as null
        }
      }
      return this;
    }

    public Builder year(Year year) {
      this.year = year;
      return this;
    }

    public Builder genre(String genre) {
      this.genre = genre;
      return this;
    }

    public Builder composer(String composer) {
      this.composer = composer;
      return this;
    }

    public Builder publisher(String publisher) {
      this.publisher = publisher;
      return this;
    }

    public Builder comment(String comment) {
      this.comment = comment;
      return this;
    }

    public Mp3Metadata build() {
      return new Mp3Metadata(
          filePath,
          Optional.ofNullable(albumArtist),
          Optional.ofNullable(artist),
          Optional.ofNullable(album),
          Optional.ofNullable(title),
          Optional.ofNullable(track),
          Optional.ofNullable(year),
          Optional.ofNullable(genre),
          Optional.ofNullable(composer),
          Optional.ofNullable(publisher),
          Optional.ofNullable(comment));
    }
  }
}
