# streetmap

OSM road-network viewer built with [GraphStream](https://graphstream-project.org/) and Java 25.

## Structure

```
src/main/java/.../streetmap/
  StreetMapViewer.java     Swing viewer — loads any DGS road-network graph
  DgsMapGenerator.java     Fetches OSM data via Overpass API, writes DGS
  MapSource.java           Sealed type: File(path) | Builtin(name)
  package-info.java

src/main/resources/.../streetmap/
  streetmap.css            GraphStream edge/node stylesheet
  data/
    BadWiessee_Generated.dgs   Road network of Bad Wiessee (default, 2 km radius)
    LeHavre.dgs                Road network of Le Havre (original demo)
    <City>_Generated.dgs       Additional cities generated via DgsMapGenerator
```

## Run

```bash
# Opens a file picker (*.dgs); cancelling loads Bad Wiessee as default
mvn exec:java -Dexec.mainClass=d021248.mytry.algorithms.graphstream.streetmap.StreetMapViewer

# Skip the picker — load a specific file directly
mvn exec:java -Dexec.mainClass=d021248.mytry.algorithms.graphstream.streetmap.StreetMapViewer \
  -Dexec.args="src/main/resources/d021248/mytry/algorithms/graphstream/streetmap/data/LeHavre.dgs"

# Generate a map from OSM (default city: Königsbrunn)
mvn exec:java -Dexec.mainClass=d021248.mytry.algorithms.graphstream.streetmap.DgsMapGenerator

# Generate for any city by name (uses Nominatim; falls back to KNOWN_CITIES offline)
mvn exec:java -Dexec.mainClass=d021248.mytry.algorithms.graphstream.streetmap.DgsMapGenerator \
  -Dexec.args="Munich"
```

## Controls

| Gesture | Action |
|---|---|
| Right-click + drag | Pan |
| Scroll wheel | Zoom in / out |
| Left-click node | Select |

## Edge colours

Edges are colour-coded by speed limit using a blue → yellow → red gradient:

| Colour | Speed |
|---|---|
| Blue | ≤ 7 km/h (living street) |
| Yellow | ~50 km/h (residential) |
| Red | 130 km/h (motorway) |

Special road types (bridge, tunnel, tollway) get additional CSS styling.

Waterways (rivers, streams, canals) are drawn in **blue** above the road network.
Named waterways show their OSM name as a label (e.g. *Zeiselbach* in Bad Wiessee).
Street names are stored in the DGS graph as the `name` attribute on road edges.

## Adding a city

1. Run the generator — writes `<City>_Generated.dgs` to `resources/data/`:
   ```bash
   # single-word city
   mvn exec:java -Dexec.mainClass=...DgsMapGenerator -Dexec.args="Munich"

   # city name with spaces — wrap in extra quotes so Maven keeps it as one arg
   mvn exec:java -Dexec.mainClass=...DgsMapGenerator -Dexec.args='"Bad Wiessee"'
   ```
2. Add offline coordinates to `KNOWN_CITIES` for proxy-restricted environments.
3. Open in the viewer with `MapSource.Builtin("Munich_Generated")` or via the file picker.

## Java 25 features used

| Feature | Where |
|---|---|
| Sealed interface + nested records | `MapSource` |
| Record patterns in switch | `StreetMapViewer` — loading and title |
| Switch expressions | `DgsMapGenerator` — speed defaults per highway type |
| `var` inference | Throughout |
| Unnamed catch variable `_` | `DgsMapGenerator` |
| `///` Markdown Javadoc | All public types |
