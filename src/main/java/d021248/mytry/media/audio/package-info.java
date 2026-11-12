/// MP3 metadata extraction and music library analysis with Java 25 features.
///
/// ## Overview
///
/// This package provides a modern, type-safe API for extracting and processing
/// MP3 metadata using the mp3agic library. It demonstrates advanced Java 25
/// features including records, sealed types, pattern matching, `StructuredTaskScope`,
/// and virtual threads.
///
/// ## Core Classes
///
/// - `Mp3Scanner` — scans directories in parallel via virtual threads (`StructuredTaskScope`)
/// - `Mp3Metadata` — immutable record with CSV/JSON export and a fluent builder
/// - `Mp3Result` — sealed type for type-safe success/failure handling
/// - `Mp3Analytics` — single-pass custom collector for artist/genre/year statistics
/// - `Mp3Demo` — comprehensive feature demonstration
///
/// ## Usage Examples
///
/// ### Extract Single File
///
/// ```java
/// Mp3Result result = Mp3Scanner.extractMetadata(Path.of("/path/to/song.mp3"));
///
/// String info = switch (result) {
///     case Mp3Result.Success(var metadata) ->
///         metadata.artist().orElse("Unknown") + " - " + metadata.title().orElse("Unknown");
///     case Mp3Result.Failure(var path, var error, _) ->
///         "Error: " + error;
/// };
/// ```
///
/// ### Scan Directory
///
/// ```java
/// try (var scanner = new Mp3Scanner()) {
///     List<Mp3Result> results = scanner.scanDirectory(Path.of("/music"));
///
///     results.stream()
///             .filter(Mp3Result::isSuccess)
///             .map(r -> (Mp3Result.Success) r)
///             .map(Mp3Result.Success::metadata)
///             .forEach(m -> System.out.println(m.toCsv()));
/// }
/// ```
///
/// ### Collect Analytics
///
/// ```java
/// var analytics = results.stream()
///         .filter(Mp3Result::isSuccess)
///         .map(r -> (Mp3Result.Success) r)
///         .map(Mp3Result.Success::metadata)
///         .collect(Mp3Analytics.collector());
///
/// analytics.topArtists(10)
///          .forEach(e -> System.out.printf("%-40s %d tracks%n", e.getKey(), e.getValue()));
/// ```
///
/// ## Configuration
///
/// Uses `ProjectPaths` for path configuration.
/// Override defaults using environment variables:
///
/// - `MEDIA_ROOT` — root directory for music files
/// - `OUTPUT_DIR` — output directory for CSV exports
///
/// ## Dependencies
///
/// - mp3agic 0.9.1 — MP3 file parsing and ID3 tag reading
///
/// @since 1.0
package d021248.mytry.media.audio;
