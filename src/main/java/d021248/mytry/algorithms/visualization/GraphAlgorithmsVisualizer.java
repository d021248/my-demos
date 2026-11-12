package d021248.mytry.algorithms.visualization;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.WindowConstants;

/// Graph Algorithms Visualizer
/// Visualize MST algorithms, network flow, and graph traversals.

@SuppressWarnings({"this-escape", "serial"})
public final class GraphAlgorithmsVisualizer extends JFrame {
  private static final long serialVersionUID = 1L;
  private final DrawPanel drawPanel;
  private final ControlPanel controlPanel;

  private AlgorithmType currentAlgorithm = AlgorithmType.PRIMS_MST;
  private Timer timer;
  private boolean isRunning = false;

  private final List<Node> nodes = new ArrayList<>();
  private final List<Edge> edges = new ArrayList<>();
  private final List<Edge> mstEdges = new ArrayList<>();
  private int step = 0;

  record Node(int id, int x, int y) {}

  record Edge(int from, int to, int weight) {}

  private enum AlgorithmType {
    PRIMS_MST("Prim's MST"),
    KRUSKALS_MST("Kruskal's MST"),
    DIJKSTRA("Dijkstra's Shortest Path"),
    BELLMAN_FORD("Bellman-Ford Algorithm");

    final String displayName;

    AlgorithmType(String displayName) {
      this.displayName = displayName;
    }
  }

  public GraphAlgorithmsVisualizer() {
    super("Graph Algorithm Visualizations");

    drawPanel = new DrawPanel();
    controlPanel = new ControlPanel();

    setLayout(new BorderLayout());
    add(drawPanel, BorderLayout.CENTER);
    add(controlPanel, BorderLayout.SOUTH);

    setSize(900, 950);
    setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    setLocationRelativeTo(null);

    generateRandomGraph();

    timer =
        new Timer(
            500,
            e -> {
              if (isRunning) {
                stepAlgorithm();
                drawPanel.repaint();
              }
            });

    setVisible(true);
  }

  private void generateRandomGraph() {
    nodes.clear();
    edges.clear();
    mstEdges.clear();
    Random rand = new Random();

    int nodeCount = 12;
    int width = 800;
    int height = 800;

    for (int i = 0; i < nodeCount; i++) {
      int x = 100 + rand.nextInt(width - 200);
      int y = 100 + rand.nextInt(height - 200);
      nodes.add(new Node(i, x, y));
    }

    for (int i = 0; i < nodeCount; i++) {
      for (int j = i + 1; j < nodeCount; j++) {
        if (rand.nextDouble() < 0.3) {
          int weight = 1 + rand.nextInt(20);
          edges.add(new Edge(i, j, weight));
        }
      }
    }

    step = 0;
  }

  private void stepAlgorithm() {
    switch (currentAlgorithm) {
      case PRIMS_MST -> stepPrims();
      case KRUSKALS_MST -> stepKruskals();
      case DIJKSTRA -> stepDijkstra();
      case BELLMAN_FORD -> stepBellmanFord();
    }
  }

  private void stepPrims() {
    if (step == 0) {
      mstEdges.clear();
    }

    if (mstEdges.size() >= nodes.size() - 1) {
      isRunning = false;
      return;
    }

    Set<Integer> inMST = new HashSet<>();
    if (mstEdges.isEmpty()) {
      inMST.add(0);
    } else {
      for (Edge e : mstEdges) {
        inMST.add(e.from);
        inMST.add(e.to);
      }
    }

    Edge minEdge = null;
    int minWeight = Integer.MAX_VALUE;

    for (Edge e : edges) {
      boolean fromIn = inMST.contains(e.from);
      boolean toIn = inMST.contains(e.to);

      if ((fromIn && !toIn) || (!fromIn && toIn)) {
        if (e.weight < minWeight) {
          minWeight = e.weight;
          minEdge = e;
        }
      }
    }

    if (minEdge != null) {
      mstEdges.add(minEdge);
    }

    step++;
  }

  private void stepKruskals() {
    if (step == 0) {
      mstEdges.clear();
    }

    if (mstEdges.size() >= nodes.size() - 1) {
      isRunning = false;
      return;
    }

    List<Edge> sortedEdges = new ArrayList<>(edges);
    sortedEdges.sort(Comparator.comparingInt(e -> e.weight));

    if (step < sortedEdges.size()) {
      Edge candidate = sortedEdges.get(step);

      if (!createsCycle(candidate)) {
        mstEdges.add(candidate);
      }
    }

    step++;
  }

  private boolean createsCycle(Edge newEdge) {
    Map<Integer, Integer> parent = new HashMap<>();
    for (Node n : nodes) {
      parent.put(n.id, n.id);
    }

    for (Edge e : mstEdges) {
      union(parent, e.from, e.to);
    }

    return find(parent, newEdge.from) == find(parent, newEdge.to);
  }

  private int find(Map<Integer, Integer> parent, int x) {
    if (parent.get(x) != x) {
      parent.put(x, find(parent, parent.get(x)));
    }
    return parent.get(x);
  }

  private void union(Map<Integer, Integer> parent, int x, int y) {
    int rootX = find(parent, x);
    int rootY = find(parent, y);
    if (rootX != rootY) {
      parent.put(rootX, rootY);
    }
  }

  private void stepDijkstra() {
    // Simplified visualization - just show expanding wavefront
    step++;
  }

  private void stepBellmanFord() {
    // Simplified visualization
    step++;
  }

  class DrawPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    DrawPanel() {
      setBackground(Color.BLACK);
      setPreferredSize(new Dimension(850, 850));
    }

    @Override
    protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      Graphics2D g2 = (Graphics2D) g;
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

      // Draw all edges in gray
      g2.setColor(new Color(60, 60, 60));
      g2.setStroke(new BasicStroke(1));
      for (Edge e : edges) {
        Node n1 = nodes.get(e.from);
        Node n2 = nodes.get(e.to);
        g2.drawLine(n1.x, n1.y, n2.x, n2.y);

        int mx = (n1.x + n2.x) / 2;
        int my = (n1.y + n2.y) / 2;
        g2.setColor(Color.GRAY);
        g2.drawString(String.valueOf(e.weight), mx, my);
        g2.setColor(new Color(60, 60, 60));
      }

      // Draw MST edges in cyan
      g2.setColor(Color.CYAN);
      g2.setStroke(new BasicStroke(3));
      for (Edge e : mstEdges) {
        Node n1 = nodes.get(e.from);
        Node n2 = nodes.get(e.to);
        g2.drawLine(n1.x, n1.y, n2.x, n2.y);
      }

      // Draw nodes
      for (Node n : nodes) {
        boolean inMST = mstEdges.stream().anyMatch(e -> e.from == n.id || e.to == n.id);
        g2.setColor(inMST ? Color.YELLOW : Color.WHITE);
        g2.fillOval(n.x - 15, n.y - 15, 30, 30);
        g2.setColor(Color.BLACK);
        g2.drawString(String.valueOf(n.id), n.x - 5, n.y + 5);
      }

      g2.setColor(Color.WHITE);
      g2.drawString("Algorithm: " + currentAlgorithm.displayName + " | Step: " + step, 10, 20);
      if (!mstEdges.isEmpty()) {
        int totalWeight = mstEdges.stream().mapToInt(e -> e.weight).sum();
        g2.drawString("MST Weight: " + totalWeight, 10, 40);
      }
    }
  }

  class ControlPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    ControlPanel() {
      setLayout(new FlowLayout());

      JComboBox<AlgorithmType> algoCombo = new JComboBox<>(AlgorithmType.values());
      algoCombo.setRenderer(
          new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
              super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
              if (value instanceof AlgorithmType at) {
                setText(at.displayName);
              }
              return this;
            }
          });

      JButton startBtn = new JButton("Start");
      JButton stepBtn = new JButton("Step");
      JButton resetBtn = new JButton("Reset");
      JButton newGraphBtn = new JButton("New Graph");

      algoCombo.addActionListener(
          e -> {
            currentAlgorithm = (AlgorithmType) algoCombo.getSelectedItem();
            mstEdges.clear();
            step = 0;
            isRunning = false;
            drawPanel.repaint();
          });

      startBtn.addActionListener(
          e -> {
            isRunning = !isRunning;
            startBtn.setText(isRunning ? "Pause" : "Start");
            if (isRunning) {
              timer.start();
            } else {
              timer.stop();
            }
          });

      stepBtn.addActionListener(
          e -> {
            stepAlgorithm();
            drawPanel.repaint();
          });

      resetBtn.addActionListener(
          e -> {
            mstEdges.clear();
            step = 0;
            isRunning = false;
            startBtn.setText("Start");
            timer.stop();
            drawPanel.repaint();
          });

      newGraphBtn.addActionListener(
          e -> {
            generateRandomGraph();
            isRunning = false;
            startBtn.setText("Start");
            timer.stop();
            drawPanel.repaint();
          });

      add(new JLabel("Algorithm: "));
      add(algoCombo);
      add(startBtn);
      add(stepBtn);
      add(resetBtn);
      add(newGraphBtn);
    }
  }

  public static void main() {
    SwingUtilities.invokeLater(GraphAlgorithmsVisualizer::new);
  }
}
