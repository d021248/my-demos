package d021248.mytry.algorithms.graphstream.streetmap;

/// Source for a DGS road-network graph.
///
/// Use pattern matching to dispatch on the two cases:
///
/// ```java
/// switch (source) {
///     case MapSource.File(var path)    -> graph.read(path);
///     case MapSource.Builtin(var name) -> graph.read(builtinUrl(name));
/// }
/// ```
public sealed interface MapSource permits MapSource.File, MapSource.Builtin {

  /// A DGS file at an explicit file-system path.
  record File(String path) implements MapSource {
    public File {
      if (path == null || path.isBlank()) throw new IllegalArgumentException("path is blank");
    }
  }

  /// A built-in DGS file bundled as a classpath resource under `data/<name>.dgs`.
  record Builtin(String name) implements MapSource {
    public Builtin {
      if (name == null || name.isBlank()) throw new IllegalArgumentException("name is blank");
    }
  }
}
