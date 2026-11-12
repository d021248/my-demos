package d021248.mytry.algorithms.visualization;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

/// Decision Tree Visualizer
/// Build and visualize decision trees for classification.

@SuppressWarnings({"this-escape", "serial"})
public final class DecisionTreeVisualizer extends JFrame {
  private static final long serialVersionUID = 1L;
  private final DrawPanel drawPanel;
  private final ControlPanel controlPanel;

  private final List<DataPoint> trainingData = new ArrayList<>();
  private TreeNode root;
  private int maxDepth = 5;

  record DataPoint(double x, double y, int label) {}

  static class TreeNode {
    boolean isLeaf;
    int label; // For leaf nodes
    double splitValue;
    boolean splitOnX; // true = split on X, false = split on Y
    TreeNode left;
    TreeNode right;
    int depth;

    // Bounding box for this node
    double minX, maxX, minY, maxY;
  }

  private static final Color[] CLASS_COLORS = {new Color(255, 100, 100), new Color(100, 100, 255)};

  public DecisionTreeVisualizer() {
    super("Decision Tree Visualizer");

    drawPanel = new DrawPanel();
    controlPanel = new ControlPanel();

    setLayout(new BorderLayout());
    add(drawPanel, BorderLayout.CENTER);
    add(controlPanel, BorderLayout.SOUTH);

    setSize(1200, 900);
    setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    setLocationRelativeTo(null);

    generateRandomData();
    buildTree();

    setVisible(true);
  }

  private void generateRandomData() {
    trainingData.clear();
    Random rand = new Random();

    // Generate two classes with some pattern
    for (int i = 0; i < 100; i++) {
      double x = rand.nextDouble();
      double y = rand.nextDouble();

      // XOR-like pattern
      int label = ((x > 0.5) ^ (y > 0.5)) ? 1 : 0;
      trainingData.add(new DataPoint(x, y, label));
    }
  }

  private void buildTree() {
    root = buildTreeRecursive(trainingData, 0, 0, 1, 0, 1);
  }

  private TreeNode buildTreeRecursive(
      List<DataPoint> data, int depth, double minX, double maxX, double minY, double maxY) {
    TreeNode node = new TreeNode();
    node.depth = depth;
    node.minX = minX;
    node.maxX = maxX;
    node.minY = minY;
    node.maxY = maxY;

    // Check stopping criteria
    if (depth >= maxDepth || data.size() < 5 || isPure(data)) {
      node.isLeaf = true;
      node.label = getMajorityLabel(data);
      return node;
    }

    // Find best split
    BestSplit bestSplit = findBestSplit(data);

    if (bestSplit.gain <= 0) {
      node.isLeaf = true;
      node.label = getMajorityLabel(data);
      return node;
    }

    node.splitValue = bestSplit.value;
    node.splitOnX = bestSplit.onX;

    List<DataPoint> leftData = new ArrayList<>();
    List<DataPoint> rightData = new ArrayList<>();

    for (DataPoint dp : data) {
      double val = node.splitOnX ? dp.x : dp.y;
      if (val <= node.splitValue) {
        leftData.add(dp);
      } else {
        rightData.add(dp);
      }
    }

    if (leftData.isEmpty() || rightData.isEmpty()) {
      node.isLeaf = true;
      node.label = getMajorityLabel(data);
      return node;
    }

    // Recursively build subtrees
    if (node.splitOnX) {
      node.left = buildTreeRecursive(leftData, depth + 1, minX, node.splitValue, minY, maxY);
      node.right = buildTreeRecursive(rightData, depth + 1, node.splitValue, maxX, minY, maxY);
    } else {
      node.left = buildTreeRecursive(leftData, depth + 1, minX, maxX, minY, node.splitValue);
      node.right = buildTreeRecursive(rightData, depth + 1, minX, maxX, node.splitValue, maxY);
    }

    return node;
  }

  record BestSplit(double value, boolean onX, double gain) {}

  private BestSplit findBestSplit(List<DataPoint> data) {
    double bestGain = -1;
    double bestValue = 0;
    boolean bestOnX = true;

    // Try splits on X
    for (DataPoint dp : data) {
      double gain = calculateGain(data, dp.x, true);
      if (gain > bestGain) {
        bestGain = gain;
        bestValue = dp.x;
        bestOnX = true;
      }
    }

    // Try splits on Y
    for (DataPoint dp : data) {
      double gain = calculateGain(data, dp.y, false);
      if (gain > bestGain) {
        bestGain = gain;
        bestValue = dp.y;
        bestOnX = false;
      }
    }

    return new BestSplit(bestValue, bestOnX, bestGain);
  }

  private double calculateGain(List<DataPoint> data, double splitValue, boolean onX) {
    List<DataPoint> left = new ArrayList<>();
    List<DataPoint> right = new ArrayList<>();

    for (DataPoint dp : data) {
      double val = onX ? dp.x : dp.y;
      if (val <= splitValue) {
        left.add(dp);
      } else {
        right.add(dp);
      }
    }

    if (left.isEmpty() || right.isEmpty()) return 0;

    double parentGini = gini(data);
    double leftGini = gini(left);
    double rightGini = gini(right);

    double weightedGini = (left.size() * leftGini + right.size() * rightGini) / data.size();
    return parentGini - weightedGini;
  }

  private double gini(List<DataPoint> data) {
    if (data.isEmpty()) return 0;

    int count0 = 0;
    int count1 = 0;
    for (DataPoint dp : data) {
      if (dp.label == 0) count0++;
      else count1++;
    }

    double p0 = (double) count0 / data.size();
    double p1 = (double) count1 / data.size();
    return 1 - (p0 * p0 + p1 * p1);
  }

  private boolean isPure(List<DataPoint> data) {
    if (data.isEmpty()) return true;
    int firstLabel = data.get(0).label;
    return data.stream().allMatch(dp -> dp.label == firstLabel);
  }

  private int getMajorityLabel(List<DataPoint> data) {
    long count0 = data.stream().filter(dp -> dp.label == 0).count();
    return count0 > data.size() / 2 ? 0 : 1;
  }

  class DrawPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    DrawPanel() {
      setBackground(new Color(20, 20, 30));
      setPreferredSize(new Dimension(1150, 800));

      addMouseListener(
          new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
              if (e.getX() < 600) {
                double x = (double) e.getX() / 600;
                double y = (double) e.getY() / 600;
                int label = (e.getButton() == MouseEvent.BUTTON1) ? 0 : 1;
                trainingData.add(new DataPoint(x, y, label));
                repaint();
              }
            }
          });
    }

    @Override
    protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      Graphics2D g2 = (Graphics2D) g;
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

      // Draw decision regions
      drawDecisionRegions(g2);

      // Draw tree structure
      drawTreeStructure(g2);

      // Draw info
      g2.setColor(Color.WHITE);
      g2.drawString("Left click: Red class | Right click: Blue class", 10, 620);
    }

    private void drawDecisionRegions(Graphics2D g2) {
      int size = 600;

      // Draw decision boundary regions
      if (root != null) {
        drawRegion(g2, root, 0, size, 0, size);
      }

      // Draw training points
      for (DataPoint dp : trainingData) {
        g2.setColor(CLASS_COLORS[dp.label]);
        g2.fillOval((int) (dp.x * size) - 5, (int) (dp.y * size) - 5, 10, 10);
        g2.setColor(Color.WHITE);
        g2.drawOval((int) (dp.x * size) - 5, (int) (dp.y * size) - 5, 10, 10);
      }

      g2.setColor(Color.GRAY);
      g2.drawRect(0, 0, size, size);
    }

    private void drawRegion(Graphics2D g2, TreeNode node, int x, int width, int y, int height) {
      if (node.isLeaf) {
        Color regionColor = CLASS_COLORS[node.label];
        g2.setColor(
            new Color(regionColor.getRed(), regionColor.getGreen(), regionColor.getBlue(), 30));
        g2.fillRect(x, y, width, height);
        return;
      }

      // Draw split line
      g2.setColor(new Color(255, 255, 255, 100));
      g2.setStroke(new BasicStroke(2));

      if (node.splitOnX) {
        int splitX = x + (int) ((node.splitValue - node.minX) / (node.maxX - node.minX) * width);
        g2.drawLine(splitX, y, splitX, y + height);

        // Recursively draw child regions
        if (node.left != null) {
          drawRegion(g2, node.left, x, splitX - x, y, height);
        }
        if (node.right != null) {
          drawRegion(g2, node.right, splitX, x + width - splitX, y, height);
        }
      } else {
        int splitY = y + (int) ((node.splitValue - node.minY) / (node.maxY - node.minY) * height);
        g2.drawLine(x, splitY, x + width, splitY);

        // Recursively draw child regions
        if (node.left != null) {
          drawRegion(g2, node.left, x, width, y, splitY - y);
        }
        if (node.right != null) {
          drawRegion(g2, node.right, x, width, splitY, y + height - splitY);
        }
      }
    }

    private void drawTreeStructure(Graphics2D g2) {
      if (root == null) return;

      int startX = 900;
      int startY = 50;

      g2.setColor(Color.GRAY);
      g2.drawString("Decision Tree Structure", startX - 50, startY - 20);

      drawTreeNode(g2, root, startX, startY, 200);
    }

    private void drawTreeNode(Graphics2D g2, TreeNode node, int x, int y, int spacing) {
      if (node == null) return;

      int nodeRadius = 15;

      // Draw connections to children
      if (!node.isLeaf) {
        g2.setColor(new Color(100, 100, 100));
        g2.setStroke(new BasicStroke(2));

        int childY = y + 80;
        int leftX = x - spacing / 2;
        int rightX = x + spacing / 2;

        if (node.left != null) {
          g2.drawLine(x, y + nodeRadius, leftX, childY - nodeRadius);
          drawTreeNode(g2, node.left, leftX, childY, spacing / 2);
        }
        if (node.right != null) {
          g2.drawLine(x, y + nodeRadius, rightX, childY - nodeRadius);
          drawTreeNode(g2, node.right, rightX, childY, spacing / 2);
        }
      }

      // Draw node
      if (node.isLeaf) {
        g2.setColor(CLASS_COLORS[node.label]);
        g2.fillOval(x - nodeRadius, y - nodeRadius, nodeRadius * 2, nodeRadius * 2);
      } else {
        g2.setColor(new Color(200, 200, 200));
        g2.fillOval(x - nodeRadius, y - nodeRadius, nodeRadius * 2, nodeRadius * 2);
        g2.setColor(Color.BLACK);
        g2.setFont(new Font("Arial", Font.PLAIN, 10));
        String label = node.splitOnX ? "X" : "Y";
        g2.drawString(label, x - 4, y + 4);
      }

      g2.setColor(Color.WHITE);
      g2.drawOval(x - nodeRadius, y - nodeRadius, nodeRadius * 2, nodeRadius * 2);
    }
  }

  class ControlPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    ControlPanel() {
      setLayout(new FlowLayout());

      JButton buildBtn = new JButton("Build Tree");
      JButton clearBtn = new JButton("Clear Data");
      JButton randomBtn = new JButton("Random Data");

      JSlider depthSlider = new JSlider(1, 10, 5);
      depthSlider.setMajorTickSpacing(1);
      depthSlider.setPaintTicks(true);
      depthSlider.setPaintLabels(true);

      buildBtn.addActionListener(
          e -> {
            buildTree();
            drawPanel.repaint();
          });

      clearBtn.addActionListener(
          e -> {
            trainingData.clear();
            root = null;
            drawPanel.repaint();
          });

      randomBtn.addActionListener(
          e -> {
            generateRandomData();
            buildTree();
            drawPanel.repaint();
          });

      depthSlider.addChangeListener(
          e -> {
            maxDepth = depthSlider.getValue();
          });

      add(new JLabel("Max Depth: "));
      add(depthSlider);
      add(buildBtn);
      add(clearBtn);
      add(randomBtn);
    }
  }

  public static void main() {
    SwingUtilities.invokeLater(DecisionTreeVisualizer::new);
  }
}
