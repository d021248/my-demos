package d021248.mytry.algorithms.graphstream;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.swing_viewer.ViewPanel;
import org.graphstream.ui.view.Viewer;

/// Builds and displays a static directed graph of Collatz sequences for start
/// values 1 through 64 using the GraphStream library.
public final class CollatzGraph {
  public static void main() {
    System.setProperty("org.graphstream.ui", "swing");
    Graph graph = new SingleGraph("Collatz Conjecture");

    // Add Collatz sequences for start values 1..100
    for (int start = 1; start <= 64; start++) {
      int n = start;
      String prev = Integer.toString(n);
      if (graph.getNode(prev) == null) {
        graph.addNode(prev).setAttribute("ui.label", prev);
      }
      // Mark start nodes with a special class
      graph.getNode(prev).setAttribute("ui.class", "start");
      while (n != 1) {
        n = (n % 2 == 0) ? n / 2 : 3 * n + 1;
        String curr = Integer.toString(n);
        if (graph.getNode(curr) == null) graph.addNode(curr).setAttribute("ui.label", curr);
        String edgeId = prev + "-" + curr;
        if (graph.getEdge(edgeId) == null) graph.addEdge(edgeId, prev, curr, true);
        prev = curr;
      }
    }
    graph.setAttribute(
        "ui.stylesheet",
        "node.start { fill-color: #FBD46D; size: 24px; } "
            + "node { size: 20px; text-size: 16; fill-color: #4F8A8B; } "
            + "edge { arrow-shape: arrow; arrow-size: 10px, 5px; }");
    graph.setAttribute("ui.quality");
    graph.setAttribute("ui.antialias");
    Viewer viewer = graph.display();
    viewer.enableAutoLayout();
    ViewPanel view = (ViewPanel) viewer.getDefaultView();
    view.enableMouseOptions();
    // Enable zoom in/out with mouse wheel
    view.addMouseWheelListener(
        e -> {
          double factor = e.getWheelRotation() < 0 ? 0.9 : 1.1;
          view.getCamera().setViewPercent(view.getCamera().getViewPercent() * factor);
        });
  }
}
