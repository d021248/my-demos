package d021248.mytry.algorithms.graphstream;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import javax.swing.Timer;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.swing_viewer.ViewPanel;
import org.graphstream.ui.view.Viewer;

public final class BarabasiAlbertGraphStreamDemo {

  private static final int MAX_NODES = 150;
  private static final int M = 2; // edges per new node
  private static final int DELAY = 50; // ms per tick

  public static void main() {
    System.setProperty("org.graphstream.ui", "swing");

    Graph graph = new SingleGraph("Barabási–Albert Scale-Free Network");
    graph.setAttribute("ui.quality");
    graph.setAttribute("ui.antialias");
    graph.setAttribute(
        "ui.stylesheet",
        "graph { fill-color: #1a1a2e; } "
            + "node { size: 12px; fill-color: #4F8A8B; text-size: 0; text-color: white; } "
            + "edge { fill-color: rgba(184,193,236,120); size: 1px; arrow-shape: none; } "
            + "node.hub { text-size: 14; }");

    Viewer viewer = graph.display();
    viewer.enableAutoLayout();
    ViewPanel view = (ViewPanel) viewer.getDefaultView();
    view.enableMouseOptions();

    // per-node degree tracked in parallel to nodeIds list
    List<String> nodeIds = new ArrayList<>();
    List<Integer> degrees = new ArrayList<>();
    Random rng = new Random();

    // size scale factor controlled by Ctrl+scroll
    final double[] sizeScale = {1.0};

    view.addMouseWheelListener(
        e -> {
          if (e.isControlDown()) {
            double factor = e.getWheelRotation() < 0 ? 1.1 : 0.9;
            sizeScale[0] *= factor;
            // reapply per-node styles at new scale
            for (int i = 0; i < nodeIds.size(); i++) {
              Node n = graph.getNode(nodeIds.get(i));
              if (n != null) {
                applyNodeStyle(n, degrees.get(i), sizeScale[0]);
              }
            }
          } else {
            double factor = e.getWheelRotation() < 0 ? 0.9 : 1.1;
            view.getCamera().setViewPercent(view.getCamera().getViewPercent() * factor);
          }
        });

    // seed: 3 fully connected nodes
    for (int i = 0; i < 3; i++) {
      String id = String.valueOf(i);
      graph.addNode(id).setAttribute("ui.label", id);
      nodeIds.add(id);
      degrees.add(0);
    }
    addEdge(graph, "0", "1", degrees, nodeIds);
    addEdge(graph, "1", "2", degrees, nodeIds);
    addEdge(graph, "0", "2", degrees, nodeIds);
    for (int i = 0; i < 3; i++) {
      applyNodeStyle(graph.getNode(nodeIds.get(i)), degrees.get(i), sizeScale[0]);
    }

    final int[] nextId = {3};

    Timer timer =
        new Timer(
            DELAY,
            new ActionListener() {
              @Override
              public void actionPerformed(ActionEvent e) {
                if (nextId[0] >= MAX_NODES) {
                  ((Timer) e.getSource()).stop();
                  labelTopHubs(graph, nodeIds, degrees, 3);
                  return;
                }

                String newId = String.valueOf(nextId[0]++);
                graph.addNode(newId);
                nodeIds.add(newId);
                degrees.add(0);

                int totalDegree = degrees.stream().mapToInt(Integer::intValue).sum();
                // fallback when totalDegree==0 (only seed, all degrees 0): pick uniformly
                List<String> chosen = new ArrayList<>();
                for (int i = 0; i < M; i++) {
                  String target = pickTarget(nodeIds, degrees, totalDegree, chosen, rng);
                  if (target != null) {
                    String edgeId = newId + "-" + target;
                    if (graph.getEdge(edgeId) == null) {
                      graph.addEdge(edgeId, newId, target);
                      int idx = nodeIds.indexOf(target);
                      degrees.set(idx, degrees.get(idx) + 1);
                      int newIdx = nodeIds.size() - 1;
                      degrees.set(newIdx, degrees.get(newIdx) + 1);
                      totalDegree += 2;
                      applyNodeStyle(graph.getNode(target), degrees.get(idx), sizeScale[0]);
                      applyNodeStyle(graph.getNode(newId), degrees.get(newIdx), sizeScale[0]);
                    }
                    chosen.add(target);
                  }
                }
              }
            });
    timer.setInitialDelay(0);
    timer.start();
  }

  private static String pickTarget(
      List<String> nodeIds,
      List<Integer> degrees,
      int totalDegree,
      List<String> exclude,
      Random rng) {
    if (nodeIds.size() <= exclude.size()) return null;
    int size = nodeIds.size() - 1; // exclude the just-added last node
    if (size == 0) return null;

    if (totalDegree == 0) {
      // uniform fallback
      for (int attempts = 0; attempts < 100; attempts++) {
        String candidate = nodeIds.get(rng.nextInt(size));
        if (!exclude.contains(candidate)) return candidate;
      }
      return null;
    }

    double pick = rng.nextDouble() * totalDegree;
    double cumulative = 0;
    for (int i = 0; i < size; i++) {
      String id = nodeIds.get(i);
      if (exclude.contains(id)) continue;
      cumulative += degrees.get(i);
      if (cumulative >= pick) return id;
    }
    // fallback: return first non-excluded
    for (int i = 0; i < size; i++) {
      if (!exclude.contains(nodeIds.get(i))) return nodeIds.get(i);
    }
    return null;
  }

  private static void addEdge(
      Graph graph, String a, String b, List<Integer> degrees, List<String> nodeIds) {
    graph.addEdge(a + "-" + b, a, b);
    int ia = nodeIds.indexOf(a), ib = nodeIds.indexOf(b);
    degrees.set(ia, degrees.get(ia) + 1);
    degrees.set(ib, degrees.get(ib) + 1);
  }

  private static void applyNodeStyle(Node node, int degree, double scale) {
    if (node == null) return;
    // size: 8px base + 2px per degree, capped at 48px
    double size = Math.min(8 + degree * 2.0, 48) * scale;
    // color: teal (79,138,139) → amber (251,212,109) → red (232,69,69)
    int[] rgb = degreeToColor(degree);
    node.setAttribute(
        "ui.style",
        String.format(
            Locale.ROOT, "size: %.1fpx; fill-color: rgb(%d,%d,%d);", size, rgb[0], rgb[1], rgb[2]));
  }

  private static int[] degreeToColor(int degree) {
    // 0..5: teal→amber, 6+: amber→red
    if (degree <= 5) {
      double t = degree / 5.0;
      return lerp(new int[] {79, 138, 139}, new int[] {251, 212, 109}, t);
    } else {
      double t = Math.min((degree - 5) / 15.0, 1.0);
      return lerp(new int[] {251, 212, 109}, new int[] {232, 69, 69}, t);
    }
  }

  private static int[] lerp(int[] a, int[] b, double t) {
    return new int[] {
      (int) (a[0] + (b[0] - a[0]) * t),
      (int) (a[1] + (b[1] - a[1]) * t),
      (int) (a[2] + (b[2] - a[2]) * t)
    };
  }

  private static void labelTopHubs(
      Graph graph, List<String> nodeIds, List<Integer> degrees, int topN) {
    // find top-N by degree
    List<Integer> indices = new ArrayList<>();
    for (int i = 0; i < nodeIds.size(); i++) indices.add(i);
    indices.sort((a, b) -> Integer.compare(degrees.get(b), degrees.get(a)));

    for (int rank = 0; rank < Math.min(topN, indices.size()); rank++) {
      int idx = indices.get(rank);
      Node node = graph.getNode(nodeIds.get(idx));
      if (node != null) {
        node.setAttribute("ui.label", "hub:" + degrees.get(idx));
        node.setAttribute("ui.class", "hub");
      }
    }
  }
}
