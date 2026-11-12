package d021248.mytry.algorithms.graphstream;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Queue;
import javax.swing.Timer;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.swing_viewer.ViewPanel;
import org.graphstream.ui.view.Viewer;

/// Animated GraphStream visualisation of the Calkin-Wilf rational-number tree,
/// revealing nodes level-by-level via a Swing timer.
public final class CalkinWilfGraphStreamTree {

  private static int DELAY = 50; // Delay in milliseconds

  public static void main() {
    System.setProperty("org.graphstream.ui", "swing");
    int maxLevel = 8; // Change this for deeper trees
    Graph graph = new SingleGraph("Calkin-Wilf Tree");
    graph.setAttribute(
        "ui.stylesheet",
        "graph { fill-color: #232946; } "
            + "node { size: 24px; fill-color: #eebbc3; text-size: 16; text-color: #232946; } "
            + "edge { fill-color: #b8c1ec; size: 2px; arrow-shape: none; }");
    graph.setAttribute("ui.quality");
    graph.setAttribute("ui.antialias");

    Viewer viewer = graph.display();

    viewer.enableAutoLayout();
    ViewPanel view = (ViewPanel) viewer.getDefaultView();
    view.enableMouseOptions();
    //

    // Mouse wheel zoom and ctrl+wheel for node/edge size, as in
    // CollatzGraphAnimated
    final double[] nodeSize = {24};
    final double[] textSize = {16};
    Runnable updateStylesheet =
        () -> {
          String stylesheet =
              String.format(
                  Locale.ROOT,
                  "graph { fill-color: #232946; } "
                      + "node { size: %.1fpx; fill-color: #eebbc3; text-size: %.1f; text-color: #232946; } "
                      + "node.highlight { fill-color: #ffad05; } "
                      + "edge { fill-color: #b8c1ec; size: 2px; arrow-shape: none; }",
                  nodeSize[0],
                  textSize[0]);
          graph.setAttribute("ui.stylesheet", stylesheet);
        };
    updateStylesheet.run();

    view.addMouseWheelListener(
        e -> {
          if (e.isControlDown()) {
            double factor = e.getWheelRotation() < 0 ? 1.1 : 0.9;
            nodeSize[0] *= factor;
            textSize[0] *= factor;
            updateStylesheet.run();
          } else {
            double factor = e.getWheelRotation() < 0 ? 0.9 : 1.1;
            view.getCamera().setViewPercent(view.getCamera().getViewPercent() * factor);
          }
        });

    // --- Animation logic ---
    class TreeNode {
      String id;
      long num, den;

      int level;
      TreeNode parent;

      TreeNode(String id, long num, long den, int level, TreeNode parent) {
        this.id = id;
        this.num = num;
        this.den = den;
        this.level = level;
        this.parent = parent;
      }
    }
    Queue<TreeNode> queue = new LinkedList<>();
    queue.add(new TreeNode("1/ 1", 1, 1, 1, null));
    final int[] nodesAdded = {0};
    final int maxNodes = (int) Math.pow(2, maxLevel) - 1;
    final String[] lastHighlight = {null};

    Timer timer =
        new Timer(
            DELAY,
            new ActionListener() {
              @Override
              public void actionPerformed(ActionEvent e) {
                if (queue.isEmpty() || nodesAdded[0] >= maxNodes) {
                  ((Timer) e.getSource()).stop();
                  return;
                }
                TreeNode tn = queue.poll();
                if (graph.getNode(tn.id) == null) {
                  Node node = graph.addNode(tn.id);
                  node.setAttribute("ui.label", tn.id);
                }
                if (tn.parent != null) {
                  String edgeId = tn.parent.id + "-" + tn.id;
                  if (graph.getEdge(edgeId) == null) {
                    graph.addEdge(edgeId, tn.parent.id, tn.id, true);
                  }
                }
                // Highlight
                if (lastHighlight[0] != null && graph.getNode(lastHighlight[0]) != null) {
                  graph.getNode(lastHighlight[0]).removeAttribute("ui.class");
                }
                graph.getNode(tn.id).setAttribute("ui.class", "highlight");
                lastHighlight[0] = tn.id;
                nodesAdded[0]++;
                if (tn.level < maxLevel) {
                  queue.add(
                      new TreeNode(
                          tn.num + "/" + (tn.num + tn.den),
                          tn.num,
                          tn.num + tn.den,
                          tn.level + 1,
                          tn));
                  queue.add(
                      new TreeNode(
                          (tn.num + tn.den) + "/" + tn.den,
                          tn.num + tn.den,
                          tn.den,
                          tn.level + 1,
                          tn));
                }
              }
            });
    timer.setInitialDelay(0);
    timer.start();
  }
}
