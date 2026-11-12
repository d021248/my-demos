package d021248.mytry.algorithms.visualization;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.WindowConstants;

/// K-Means Clustering Visualizer
/// Watch data points organize into clusters in real-time.

@SuppressWarnings({"this-escape", "serial"})
public final class KMeansVisualizer extends JFrame {
  private static final long serialVersionUID = 1L;
  private final DrawPanel drawPanel;
  private final ControlPanel controlPanel;

  private Timer timer;
  private final List<Point2D> dataPoints = new ArrayList<>();
  private final List<Point2D> centroids = new ArrayList<>();
  private final Map<Point2D, Integer> assignments = new HashMap<>();
  private int k = 3;
  private int iteration = 0;
  private boolean converged = false;

  record Point2D(double x, double y) {}

  private static final Color[] CLUSTER_COLORS = {
    new Color(255, 100, 100),
    new Color(100, 255, 100),
    new Color(100, 100, 255),
    new Color(255, 255, 100),
    new Color(255, 100, 255),
    new Color(100, 255, 255),
    new Color(255, 150, 100),
    new Color(150, 255, 100)
  };

  public KMeansVisualizer() {
    super("K-Means Clustering Visualizer");

    drawPanel = new DrawPanel();
    controlPanel = new ControlPanel();

    setLayout(new BorderLayout());
    add(drawPanel, BorderLayout.CENTER);
    add(controlPanel, BorderLayout.SOUTH);

    setSize(1000, 900);
    setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    setLocationRelativeTo(null);

    generateRandomClusters();
    initializeCentroids();

    timer =
        new Timer(
            500,
            e -> {
              if (!converged) {
                stepKMeans();
                drawPanel.repaint();
              }
            });

    setVisible(true);
  }

  private void generateRandomClusters() {
    dataPoints.clear();
    Random rand = new Random();

    // Generate 3 natural clusters
    int pointsPerCluster = 50;
    double[][] clusterCenters = {{0.3, 0.3}, {0.7, 0.3}, {0.5, 0.7}};

    for (double[] center : clusterCenters) {
      for (int i = 0; i < pointsPerCluster; i++) {
        double x = center[0] + rand.nextGaussian() * 0.08;
        double y = center[1] + rand.nextGaussian() * 0.08;
        x = Math.max(0.05, Math.min(0.95, x));
        y = Math.max(0.05, Math.min(0.95, y));
        dataPoints.add(new Point2D(x, y));
      }
    }
  }

  private void initializeCentroids() {
    centroids.clear();
    assignments.clear();
    iteration = 0;
    converged = false;

    // K-means++ initialization
    Random rand = new Random();
    centroids.add(dataPoints.get(rand.nextInt(dataPoints.size())));

    while (centroids.size() < k) {
      double[] distances = new double[dataPoints.size()];
      double sum = 0;

      for (int i = 0; i < dataPoints.size(); i++) {
        Point2D point = dataPoints.get(i);
        double minDist = Double.MAX_VALUE;
        for (Point2D centroid : centroids) {
          double dist = distance(point, centroid);
          minDist = Math.min(minDist, dist);
        }
        distances[i] = minDist * minDist;
        sum += distances[i];
      }

      double r = rand.nextDouble() * sum;
      double cumulative = 0;
      for (int i = 0; i < dataPoints.size(); i++) {
        cumulative += distances[i];
        if (cumulative >= r) {
          centroids.add(dataPoints.get(i));
          break;
        }
      }
    }
  }

  private void stepKMeans() {
    // Assignment step
    Map<Point2D, Integer> newAssignments = new HashMap<>();
    for (Point2D point : dataPoints) {
      int closestCentroid = 0;
      double minDist = Double.MAX_VALUE;

      for (int i = 0; i < centroids.size(); i++) {
        double dist = distance(point, centroids.get(i));
        if (dist < minDist) {
          minDist = dist;
          closestCentroid = i;
        }
      }
      newAssignments.put(point, closestCentroid);
    }

    // Update step
    List<Point2D> newCentroids = new ArrayList<>();
    for (int i = 0; i < k; i++) {
      double sumX = 0, sumY = 0;
      int count = 0;

      final int clusterId = i;
      for (Point2D point : dataPoints) {
        if (newAssignments.get(point) == clusterId) {
          sumX += point.x;
          sumY += point.y;
          count++;
        }
      }

      if (count > 0) {
        newCentroids.add(new Point2D(sumX / count, sumY / count));
      } else {
        newCentroids.add(centroids.get(i));
      }
    }

    // Check convergence
    converged = true;
    for (int i = 0; i < k; i++) {
      if (distance(centroids.get(i), newCentroids.get(i)) > 0.001) {
        converged = false;
        break;
      }
    }

    centroids.clear();
    centroids.addAll(newCentroids);
    assignments.clear();
    assignments.putAll(newAssignments);
    iteration++;
  }

  private double distance(Point2D p1, Point2D p2) {
    double dx = p1.x - p2.x;
    double dy = p1.y - p2.y;
    return Math.sqrt(dx * dx + dy * dy);
  }

  class DrawPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    DrawPanel() {
      setBackground(new Color(20, 20, 30));
      setPreferredSize(new Dimension(950, 800));

      addMouseListener(
          new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
              if (!timer.isRunning()) {
                double x = (double) e.getX() / getWidth();
                double y = (double) e.getY() / getHeight();
                dataPoints.add(new Point2D(x, y));
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

      int width = getWidth();
      int height = getHeight();

      // Draw Voronoi regions (simple version)
      if (!centroids.isEmpty()) {
        int resolution = 100;
        for (int i = 0; i < resolution; i++) {
          for (int j = 0; j < resolution; j++) {
            double x = (double) i / resolution;
            double y = (double) j / resolution;
            Point2D point = new Point2D(x, y);

            int closestCentroid = 0;
            double minDist = Double.MAX_VALUE;
            for (int c = 0; c < centroids.size(); c++) {
              double dist = distance(point, centroids.get(c));
              if (dist < minDist) {
                minDist = dist;
                closestCentroid = c;
              }
            }

            Color clusterColor = CLUSTER_COLORS[closestCentroid % CLUSTER_COLORS.length];
            g2.setColor(
                new Color(
                    clusterColor.getRed(), clusterColor.getGreen(), clusterColor.getBlue(), 30));
            g2.fillRect(
                i * width / resolution,
                j * height / resolution,
                width / resolution + 1,
                height / resolution + 1);
          }
        }
      }

      // Draw lines from points to centroids
      g2.setStroke(new BasicStroke(1));
      for (Point2D point : dataPoints) {
        Integer cluster = assignments.get(point);
        if (cluster != null && cluster < centroids.size()) {
          Point2D centroid = centroids.get(cluster);
          Color clusterColor = CLUSTER_COLORS[cluster % CLUSTER_COLORS.length];
          g2.setColor(
              new Color(
                  clusterColor.getRed(), clusterColor.getGreen(), clusterColor.getBlue(), 50));
          g2.drawLine(
              (int) (point.x * width),
              (int) (point.y * height),
              (int) (centroid.x * width),
              (int) (centroid.y * height));
        }
      }

      // Draw data points
      for (Point2D point : dataPoints) {
        Integer cluster = assignments.get(point);
        Color color =
            cluster != null ? CLUSTER_COLORS[cluster % CLUSTER_COLORS.length] : Color.GRAY;
        g2.setColor(color);
        g2.fillOval((int) (point.x * width) - 4, (int) (point.y * height) - 4, 8, 8);
        g2.setColor(Color.WHITE);
        g2.drawOval((int) (point.x * width) - 4, (int) (point.y * height) - 4, 8, 8);
      }

      // Draw centroids
      g2.setStroke(new BasicStroke(3));
      for (int i = 0; i < centroids.size(); i++) {
        Point2D centroid = centroids.get(i);
        Color clusterColor = CLUSTER_COLORS[i % CLUSTER_COLORS.length];

        // Draw X marker
        g2.setColor(Color.BLACK);
        int cx = (int) (centroid.x * width);
        int cy = (int) (centroid.y * height);
        g2.drawLine(cx - 10, cy - 10, cx + 10, cy + 10);
        g2.drawLine(cx - 10, cy + 10, cx + 10, cy - 10);

        g2.setColor(clusterColor);
        g2.drawLine(cx - 8, cy - 8, cx + 8, cy + 8);
        g2.drawLine(cx - 8, cy + 8, cx + 8, cy - 8);
      }

      // Draw info
      g2.setColor(Color.WHITE);
      g2.drawString(
          "Iteration: " + iteration + " | K: " + k + " | Points: " + dataPoints.size(), 10, 20);
      if (converged) {
        g2.setColor(Color.GREEN);
        g2.drawString("CONVERGED!", 10, 40);
      }
    }
  }

  class ControlPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    ControlPanel() {
      setLayout(new FlowLayout());

      JButton startBtn = new JButton("Start");
      JButton stepBtn = new JButton("Step");
      JButton resetBtn = new JButton("Reset");
      JButton newDataBtn = new JButton("New Random Data");
      JButton clearBtn = new JButton("Clear All");

      JSlider kSlider = new JSlider(2, 8, 3);
      kSlider.setMajorTickSpacing(1);
      kSlider.setPaintTicks(true);
      kSlider.setPaintLabels(true);

      startBtn.addActionListener(
          e -> {
            if (timer.isRunning()) {
              timer.stop();
              startBtn.setText("Start");
            } else {
              timer.start();
              startBtn.setText("Pause");
            }
          });

      stepBtn.addActionListener(
          e -> {
            if (!converged) {
              stepKMeans();
              drawPanel.repaint();
            }
          });

      resetBtn.addActionListener(
          e -> {
            timer.stop();
            startBtn.setText("Start");
            initializeCentroids();
            drawPanel.repaint();
          });

      newDataBtn.addActionListener(
          e -> {
            timer.stop();
            startBtn.setText("Start");
            generateRandomClusters();
            initializeCentroids();
            drawPanel.repaint();
          });

      clearBtn.addActionListener(
          e -> {
            timer.stop();
            startBtn.setText("Start");
            dataPoints.clear();
            centroids.clear();
            assignments.clear();
            iteration = 0;
            converged = false;
            drawPanel.repaint();
          });

      kSlider.addChangeListener(
          e -> {
            k = kSlider.getValue();
            timer.stop();
            startBtn.setText("Start");
            initializeCentroids();
            drawPanel.repaint();
          });

      add(new JLabel("K: "));
      add(kSlider);
      add(startBtn);
      add(stepBtn);
      add(resetBtn);
      add(newDataBtn);
      add(clearBtn);
      add(new JLabel("  (Click to add points)"));
    }
  }

  public static void main() {
    SwingUtilities.invokeLater(KMeansVisualizer::new);
  }
}
