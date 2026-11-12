package d021248.mytry.algorithms.graphstream;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.layout.springbox.implementations.SpringBox;
import org.graphstream.ui.swing_viewer.ViewPanel;
import org.graphstream.ui.view.Viewer;

public final class ProjectStructureDemo {
  private static final String SRC_ROOT = "src/main/java/d021248/mytry";

  public static void main() {
    System.setProperty("org.graphstream.ui", "swing");
    Graph graph = new SingleGraph("Project Structure");
    Set<String> nodes = new HashSet<>();

    scanAndAdd(graph, nodes, new File(SRC_ROOT), "");

    String styleSheet =
        """
				    node.package { fill-color: #4F8A8B; }
				    node.class { fill-color: #FBD46D; }
				    node { size: 15px; text-size: 10; text-alignment: under; }
				    edge { arrow-shape: arrow; arrow-size: 6px, 3px; }
				""";
    graph.setAttribute("ui.stylesheet", styleSheet);
    graph.setAttribute("ui.quality");
    graph.setAttribute("ui.antialias");

    Viewer viewer = graph.display(false);
    ViewPanel view = (ViewPanel) viewer.getDefaultView();
    view.enableMouseOptions();
    view.resizeFrame(1000, 700);
    // Enable zoom in/out with mouse wheel
    view.addMouseWheelListener(
        e -> {
          double factor = e.getWheelRotation() < 0 ? 0.9 : 1.1;
          view.getCamera().setViewPercent(view.getCamera().getViewPercent() * factor);
        });

    // Use GraphStream's default auto-layout (SpringBox) for dynamic arrangement
    SpringBox layout = new SpringBox();
    viewer.enableAutoLayout(layout);

    // Use hierarchical layout for a more tree-like structure
    graph.setAttribute("ui.layout", "hierarchical");
    // Optionally, disable the LinLog layout and auto-layout for a strict tree
    // viewer.enableAutoLayout(); // Comment this out for strict hierarchy
  }

  private static void scanAndAdd(Graph graph, Set<String> nodes, File file, String parent) {
    if (file.isDirectory()) {
      String pkg =
          file.getPath().replace(File.separatorChar, '.').replaceFirst("^src.main.java.", "");
      if (!pkg.isEmpty() && nodes.add(pkg)) {
        graph.addNode(pkg).setAttribute("ui.label", pkg);
        graph.getNode(pkg).setAttribute("ui.class", "package");
        if (!parent.isEmpty()) {
          String edgeId = parent + "-" + pkg;
          if (graph.getEdge(edgeId) == null) graph.addEdge(edgeId, parent, pkg, true);
        }
      }
      for (File child : file.listFiles()) {
        scanAndAdd(graph, nodes, child, pkg);
      }
    } else if (file.getName().endsWith(".java")) {
      String className = file.getName().replace(".java", "");
      String pkg =
          file.getParent().replace(File.separatorChar, '.').replaceFirst("^src.main.java.", "");
      String nodeId = pkg + "." + className;
      if (nodes.add(nodeId)) {
        graph.addNode(nodeId).setAttribute("ui.label", className);
        graph.getNode(nodeId).setAttribute("ui.class", "class");
        if (graph.getNode(pkg) != null) {
          String edgeId = pkg + "-" + nodeId;
          if (graph.getEdge(edgeId) == null) graph.addEdge(edgeId, pkg, nodeId, true);
        }
      }
    }
  }
}
