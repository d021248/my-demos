package d021248.mytry.algorithms.graphstream.streetmap;

import java.awt.Component;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.graphstream.graph.ElementNotFoundException;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.stream.GraphParseException;
import org.graphstream.ui.swing_viewer.SwingViewer;
import org.graphstream.ui.swing_viewer.ViewPanel;
import org.graphstream.ui.view.Viewer.ThreadingModel;

/// Swing viewer for road-network graphs loaded from DGS files.
///
/// Edges are colour-coded by speed limit (blue = slow, yellow = medium, red = fast)
/// and optionally styled as bridge, tunnel, or tollway.
///
/// ## Usage
///
/// ```
/// # built-in Bad Wiessee map
/// mvn exec:java -Dexec.mainClass=...StreetMapViewer
///
/// # any DGS file
/// mvn exec:java -Dexec.mainClass=...StreetMapViewer -Dexec.args="path/to/map.dgs"
/// ```
public final class StreetMapViewer {

  private static final MapSource DEFAULT_SOURCE = new MapSource.Builtin("BadWiessee_Generated");

  public static void main(String[] args) {
    System.setProperty("org.graphstream.ui", "swing");
    SwingUtilities.invokeLater(
        () -> {
          var source = args.length > 0 ? new MapSource.File(args[0]) : pickSource();
          try {
            new StreetMapViewer(source);
          } catch (Exception e) {
            e.printStackTrace();
          }
        });
  }

  private static MapSource pickSource() {
    var chooser = new JFileChooser();
    chooser.setDialogTitle("Select a DGS map file");
    chooser.setFileFilter(new FileNameExtensionFilter("DGS graph files (*.dgs)", "dgs"));
    var dataDir =
        Path.of("src/main/resources/d021248/mytry/algorithms/graphstream/streetmap/data").toFile();
    if (dataDir.isDirectory()) chooser.setCurrentDirectory(dataDir);
    return chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION
        ? new MapSource.File(chooser.getSelectedFile().getAbsolutePath())
        : DEFAULT_SOURCE;
  }

  /// Creates and shows the street-map window for the given source.
  ///
  /// @param source a `MapSource.File` (file-system path) or
  ///               `MapSource.Builtin` (classpath resource `data/<name>.dgs`)
  public StreetMapViewer(MapSource source)
      throws ElementNotFoundException, IOException, GraphParseException {

    var graph = new MultiGraph("Street Map");
    graph.setAttribute("ui.quality");
    graph.setAttribute("ui.antialias");

    switch (source) {
      case MapSource.File(var path) -> graph.read(path);
      case MapSource.Builtin(var name) -> {
        var url = StreetMapViewer.class.getResource("data/" + name + ".dgs");
        if (url == null) throw new IOException("Built-in map not found: " + name);
        // FileSourceFactory only handles plain paths and http(s)://; convert file: URL
        // to path
        try {
          graph.read(Path.of(url.toURI()).toString());
        } catch (URISyntaxException e) {
          throw new IOException("Invalid classpath URL for map: " + name, e);
        }
      }
    }

    graph
        .edges()
        .forEach(
            edge -> {
              var classes = new ArrayList<String>();

              if (edge.hasAttribute("isWaterway")) {
                // Inline style avoids StyleGroupSet CSS-class lookup and any CSS parse issues.
                edge.setAttribute(
                    "ui.style",
                    "fill-mode:plain;fill-color:#1a6fa8;"
                        + "stroke-mode:plain;stroke-color:#58a6ff;stroke-width:1px;"
                        + "size:3px;z-index:4;arrow-size:4px,2px;text-mode:hidden;");
                if (edge.hasAttribute("name")) {
                  var nameVal = edge.getAttribute("name");
                  if (nameVal != null) edge.setAttribute("ui.label", nameVal.toString());
                }
              } else {
                // Combine a road-type class (bridge/tunnel/tollway) with a speed-width class
                // (fast/medium) so CSS rules for both apply independently.
                if (edge.hasAttribute("isTollway")) classes.add("tollway");
                else if (edge.hasAttribute("isTunnel")) classes.add("tunnel");
                else if (edge.hasAttribute("isBridge")) classes.add("bridge");

                double rawSpeed = edge.hasAttribute("speedMax") ? edge.getNumber("speedMax") : 50.0;
                edge.setAttribute("ui.color", (float) (rawSpeed / 130.0));

                if (rawSpeed >= 100) classes.add("fast");
                else if (rawSpeed >= 50) classes.add("medium");
              }

              if (!classes.isEmpty()) edge.setAttribute("ui.class", String.join(",", classes));
            });

    try (var in = StreetMapViewer.class.getResourceAsStream("streetmap.css")) {
      if (in == null) throw new IOException("streetmap.css not found on classpath");
      graph.setAttribute("ui.stylesheet", new String(in.readAllBytes(), StandardCharsets.UTF_8));
    }

    SwingViewer viewer;
    try {
      viewer = new SwingViewer(graph, ThreadingModel.GRAPH_IN_GUI_THREAD);
    } catch (Throwable t) {
      // replayGraph can hide the root cause — unwrap it
      var cause = t.getCause() != null ? t.getCause() : t;
      System.err.println(
          "=== Viewer init failed: " + cause.getClass().getName() + ": " + cause.getMessage());
      cause.printStackTrace(System.err);
      System.err.flush();
      throw new RuntimeException("Viewer init failed", cause);
    }
    var view = (ViewPanel) viewer.addDefaultView(false);
    view.enableMouseOptions();

    ((Component) view)
        .addMouseWheelListener(
            e -> {
              e.consume();
              var cam = view.getCamera();
              double newZoom = cam.getViewPercent() * (e.getWheelRotation() < 0 ? 0.9 : 1.1);
              if (newZoom > 0.05 && newZoom < 5.0) {
                cam.setViewPercent(newZoom);
              }
            });

    var title =
        switch (source) {
          case MapSource.File(var path) -> "Street Map — " + Path.of(path).getFileName();
          case MapSource.Builtin(var name) -> "Street Map — " + name;
        };

    var frame = new JFrame(title);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setSize(1024, 768);
    frame.add(view);
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);

    view.getCamera().resetView();
  }
}
