package d021248.mytry.algorithms.graphstream;

import java.util.Locale;
import javax.swing.Timer;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.swing_viewer.ViewPanel;
import org.graphstream.ui.view.Viewer;

/// Builds an animated directed graph of Collatz sequences.
/// Nodes stay yellow as long as they are only start nodes.
/// If they get hit/used by another sequence, they turn blue.
public final class CollatzGraphAnimated {

  private static double nodeSize = 10;
  private static double nodeStartSize = 14;
  private static double textSize = 8;
  private static double arrowSize1 = 5;
  private static double arrowSize2 = 2.5;

  private static int startValue = 1;

  public static void main() {
    System.setProperty("org.graphstream.ui", "swing");

    Graph graph = new SingleGraph("Collatz Conjecture Animated");
    graph.setAttribute("ui.quality");
    graph.setAttribute("ui.antialias");

    Viewer viewer = graph.display();
    viewer.enableAutoLayout();
    ViewPanel view = (ViewPanel) viewer.getDefaultView();
    view.enableMouseOptions();

    updateStylesheet(graph);

    // Zoom & scaling via mouse wheel
    view.addMouseWheelListener(
        e -> {
          if (e.isControlDown()) {
            double factor = e.getWheelRotation() < 0 ? 1.1 : 0.9;
            nodeSize *= factor;
            nodeStartSize *= factor;
            textSize *= factor;
            arrowSize1 *= factor;
            arrowSize2 *= factor;
            updateStylesheet(graph);
          } else {
            double factor = e.getWheelRotation() < 0 ? 0.9 : 1.1;
            view.getCamera().setViewPercent(view.getCamera().getViewPercent() * factor);
          }
        });

    final int maxStart = 1000;

    Timer timer =
        new Timer(
            50,
            e -> { // shortened to 50 ms for smoother animation
              if (startValue > maxStart) {
                ((Timer) e.getSource()).stop();
                return;
              }

              long n = startValue;
              String prevId = Long.toString(n);

              Node startNode = graph.getNode(prevId);
              if (startNode == null) {
                // Node not yet seen — it is a new start node (yellow)
                startNode = graph.addNode(prevId);
                startNode.setAttribute("ui.label", prevId);
                startNode.setAttribute("ui.class", "start");
              } else {
                // This node already exists — it was visited in a previous Collatz sequence
              }

              // Compute and walk the Collatz chain
              while (n != 1) {
                n = (n % 2 == 0) ? n / 2 : 3 * n + 1;
                String currId = Long.toString(n);

                Node nextNode = graph.getNode(currId);
                if (nextNode == null) {
                  // New intermediate node (styled blue by default CSS)
                  nextNode = graph.addNode(currId);
                  nextNode.setAttribute("ui.label", currId);
                } else {
                  // Node already exists — hitting it from a new sequence removes its start status
                  nextNode.removeAttribute("ui.class");
                }

                String edgeId = prevId + "-" + currId;
                if (graph.getEdge(edgeId) == null) {
                  graph.addEdge(edgeId, prevId, currId, true);
                }
                prevId = currId;
              }

              startValue++;
            });

    timer.setInitialDelay(0);
    timer.start();
  }

  private static void updateStylesheet(Graph graph) {
    String stylesheet =
        String.format(
            Locale.ROOT,
            "node.start { fill-color: #FBD46D; size: %.1fpx; } "
                + "node { size: %.1fpx; text-size: %.1f; fill-color: #4F8A8B; } "
                + "edge { arrow-shape: arrow; arrow-size: %.1fpx, %.1fpx; }",
            nodeStartSize,
            nodeSize,
            textSize,
            arrowSize1,
            arrowSize2);
    graph.setAttribute("ui.stylesheet", stylesheet);
  }
}
