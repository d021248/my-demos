/// Street-map viewer and OSM data generator for GraphStream.
///
/// - `StreetMapViewer` — Swing window that renders a DGS road-network graph;
///   edges are colour-coded by speed limit and optionally styled as bridge/tunnel/tollway.
///   Accepts a DGS file path via the first command-line argument (defaults to Bad Wiessee).
/// - `DgsMapGenerator` — fetches a city's road network from the Overpass API
///   and writes it as a DGS file ready for `StreetMapViewer`.
///
/// @since 1.0
package d021248.mytry.algorithms.graphstream.streetmap;
