package d021248.mytry.algorithms.visualization;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;
import java.util.Locale;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.WindowConstants;

/// Recursive Tree and Fractal Animations
/// Beautiful visualizations of recursive patterns including fractal trees,
/// Sierpinski triangles, Koch snowflakes, and more.
///
/// Features:
/// - Multiple fractal types
/// - Animated growth with adjustable depth
/// - Color gradients based on recursion depth
/// - Interactive controls
/// - Rotation and symmetry options

@SuppressWarnings({"this-escape", "serial"})
public final class RecursiveTreeVisualizer extends JFrame {
  private static final long serialVersionUID = 1L;
  private final DrawPanel drawPanel;
  private final ControlPanel controlPanel;

  private FractalType currentType = FractalType.BINARY_TREE;
  private int maxDepth = 10;
  private double angle = 25; // degrees
  private double lengthRatio = 0.7;
  private boolean useColorGradient = true;
  private boolean animating = false;
  private int currentDepth = 0;

  private enum FractalType {
    BINARY_TREE("Binary Tree"),
    TERNARY_TREE("Ternary Tree"),
    SIERPINSKI_TRIANGLE("Sierpinski Triangle"),
    KOCH_SNOWFLAKE("Koch Snowflake"),
    DRAGON_CURVE("Dragon Curve"),
    PYTHAGORAS_TREE("Pythagoras Tree"),
    SPIRAL_TREE("Spiral Tree");

    final String displayName;

    FractalType(String displayName) {
      this.displayName = displayName;
    }
  }

  public RecursiveTreeVisualizer() {
    super("Recursive Tree & Fractal Visualizer");

    drawPanel = new DrawPanel();
    controlPanel = new ControlPanel();

    setLayout(new BorderLayout());
    add(drawPanel, BorderLayout.CENTER);
    add(controlPanel, BorderLayout.SOUTH);

    setSize(900, 750);
    setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    setLocationRelativeTo(null);

    setVisible(true);
  }

  private Color getColorForDepth(int depth) {
    if (!useColorGradient) {
      return new Color(139, 69, 19); // Saddle brown
    }

    float ratio = depth / (float) maxDepth;
    // Gradient from brown/green at trunk to light green/yellow at leaves
    if (currentType == FractalType.SIERPINSKI_TRIANGLE
        || currentType == FractalType.KOCH_SNOWFLAKE
        || currentType == FractalType.DRAGON_CURVE) {
      // Blue gradient for geometric fractals
      return new Color(
          (int) (30 + ratio * 200), (int) (144 + ratio * 100), (int) (255 - ratio * 100));
    } else {
      // Green/brown gradient for trees
      return new Color(
          (int) (139 - ratio * 100), (int) (69 + ratio * 180), (int) (19 + ratio * 50));
    }
  }

  private float getStrokeWidth(int depth) {
    return Math.max(1, (maxDepth - depth + 1) * 1.5f);
  }

  class DrawPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    DrawPanel() {
      setBackground(new Color(245, 245, 220)); // Beige background
      setPreferredSize(new Dimension(900, 650));
    }

    @Override
    protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      Graphics2D g2 = (Graphics2D) g;
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

      int centerX = getWidth() / 2;
      int centerY = getHeight() - 50;
      int depth = animating ? currentDepth : maxDepth;

      switch (currentType) {
        case BINARY_TREE -> drawBinaryTree(g2, centerX, centerY, -90, 100, depth);
        case TERNARY_TREE -> drawTernaryTree(g2, centerX, centerY, -90, 100, depth);
        case SIERPINSKI_TRIANGLE -> drawSierpinskiTriangle(g2, depth);
        case KOCH_SNOWFLAKE -> drawKochSnowflake(g2, depth);
        case DRAGON_CURVE -> drawDragonCurve(g2, depth);
        case PYTHAGORAS_TREE -> drawPythagorasTree(g2, centerX, centerY - 50, 80, 0, depth);
        case SPIRAL_TREE -> drawSpiralTree(g2, centerX, centerY, -90, 100, 0, depth);
      }
    }

    private void drawBinaryTree(
        Graphics2D g2, double x, double y, double angle, double length, int depth) {
      if (depth == 0 || length < 1) return;

      double x2 = x + length * Math.cos(Math.toRadians(angle));
      double y2 = y + length * Math.sin(Math.toRadians(angle));

      g2.setColor(getColorForDepth(maxDepth - depth));
      g2.setStroke(new BasicStroke(getStrokeWidth(maxDepth - depth)));
      g2.draw(new Line2D.Double(x, y, x2, y2));

      double newLength = length * lengthRatio;
      drawBinaryTree(g2, x2, y2, angle - RecursiveTreeVisualizer.this.angle, newLength, depth - 1);
      drawBinaryTree(g2, x2, y2, angle + RecursiveTreeVisualizer.this.angle, newLength, depth - 1);
    }

    private void drawTernaryTree(
        Graphics2D g2, double x, double y, double angle, double length, int depth) {
      if (depth == 0 || length < 1) return;

      double x2 = x + length * Math.cos(Math.toRadians(angle));
      double y2 = y + length * Math.sin(Math.toRadians(angle));

      g2.setColor(getColorForDepth(maxDepth - depth));
      g2.setStroke(new BasicStroke(getStrokeWidth(maxDepth - depth)));
      g2.draw(new Line2D.Double(x, y, x2, y2));

      double newLength = length * lengthRatio;
      double branchAngle = RecursiveTreeVisualizer.this.angle;

      drawTernaryTree(g2, x2, y2, angle - branchAngle, newLength, depth - 1);
      drawTernaryTree(g2, x2, y2, angle, newLength * 0.9, depth - 1);
      drawTernaryTree(g2, x2, y2, angle + branchAngle, newLength, depth - 1);
    }

    private void drawSierpinskiTriangle(Graphics2D g2, int depth) {
      int size = Math.min(getWidth(), getHeight()) - 100;
      int x1 = getWidth() / 2;
      int y1 = 50;
      int x2 = x1 - size / 2;
      int y2 = y1 + (int) (size * Math.sqrt(3) / 2);
      int x3 = x1 + size / 2;
      int y3 = y2;

      sierpinskiRecursive(g2, x1, y1, x2, y2, x3, y3, depth);
    }

    private void sierpinskiRecursive(
        Graphics2D g2, int x1, int y1, int x2, int y2, int x3, int y3, int depth) {
      if (depth == 0) {
        g2.setColor(getColorForDepth(maxDepth - depth));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawPolygon(new int[] {x1, x2, x3}, new int[] {y1, y2, y3}, 3);
        return;
      }

      int x12 = (x1 + x2) / 2;
      int y12 = (y1 + y2) / 2;
      int x23 = (x2 + x3) / 2;
      int y23 = (y2 + y3) / 2;
      int x31 = (x3 + x1) / 2;
      int y31 = (y3 + y1) / 2;

      sierpinskiRecursive(g2, x1, y1, x12, y12, x31, y31, depth - 1);
      sierpinskiRecursive(g2, x12, y12, x2, y2, x23, y23, depth - 1);
      sierpinskiRecursive(g2, x31, y31, x23, y23, x3, y3, depth - 1);
    }

    private void drawKochSnowflake(Graphics2D g2, int depth) {
      int size = Math.min(getWidth(), getHeight()) - 150;
      int centerX = getWidth() / 2;
      int centerY = getHeight() / 2;

      double height = size * Math.sqrt(3) / 2;
      int x1 = centerX - size / 2;
      int y1 = (int) (centerY + height / 3);
      int x2 = centerX + size / 2;
      int y2 = y1;
      int x3 = centerX;
      int y3 = (int) (centerY - 2 * height / 3);

      g2.setColor(getColorForDepth(0));
      g2.setStroke(new BasicStroke(2f));
      kochCurve(g2, x1, y1, x2, y2, depth);
      kochCurve(g2, x2, y2, x3, y3, depth);
      kochCurve(g2, x3, y3, x1, y1, depth);
    }

    private void kochCurve(Graphics2D g2, double x1, double y1, double x2, double y2, int depth) {
      if (depth == 0) {
        g2.draw(new Line2D.Double(x1, y1, x2, y2));
        return;
      }

      double deltaX = x2 - x1;
      double deltaY = y2 - y1;

      double xa = x1 + deltaX / 3;
      double ya = y1 + deltaY / 3;

      double xb = x1 + 2 * deltaX / 3;
      double yb = y1 + 2 * deltaY / 3;

      double xc = xa + (xb - xa) / 2 - (yb - ya) * Math.sqrt(3) / 2;
      double yc = ya + (yb - ya) / 2 + (xb - xa) * Math.sqrt(3) / 2;

      kochCurve(g2, x1, y1, xa, ya, depth - 1);
      kochCurve(g2, xa, ya, xc, yc, depth - 1);
      kochCurve(g2, xc, yc, xb, yb, depth - 1);
      kochCurve(g2, xb, yb, x2, y2, depth - 1);
    }

    private void drawDragonCurve(Graphics2D g2, int depth) {
      int size = 300;
      int centerX = getWidth() / 2 - size / 4;
      int centerY = getHeight() / 2;

      g2.setColor(getColorForDepth(0));
      g2.setStroke(new BasicStroke(1.5f));
      dragonCurveRecursive(g2, centerX, centerY, centerX + size, centerY, depth, 1);
    }

    private void dragonCurveRecursive(
        Graphics2D g2, double x1, double y1, double x2, double y2, int depth, int sign) {
      if (depth == 0) {
        g2.draw(new Line2D.Double(x1, y1, x2, y2));
        return;
      }

      double xm = (x1 + x2) / 2 + sign * (y2 - y1) / 2;
      double ym = (y1 + y2) / 2 - sign * (x2 - x1) / 2;

      dragonCurveRecursive(g2, x1, y1, xm, ym, depth - 1, 1);
      dragonCurveRecursive(g2, x2, y2, xm, ym, depth - 1, -1);
    }

    private void drawPythagorasTree(
        Graphics2D g2, double x, double y, double size, double angle, int depth) {
      if (depth == 0 || size < 1) return;

      double x2 = x - size * Math.sin(Math.toRadians(angle));
      double y2 = y - size * Math.cos(Math.toRadians(angle));
      double x3 = x2 - size * Math.cos(Math.toRadians(angle));
      double y3 = y2 + size * Math.sin(Math.toRadians(angle));
      double x4 = x - size * Math.cos(Math.toRadians(angle));
      double y4 = y + size * Math.sin(Math.toRadians(angle));

      g2.setColor(getColorForDepth(maxDepth - depth));
      g2.setStroke(new BasicStroke(1.5f));
      Polygon square =
          new Polygon(
              new int[] {(int) x, (int) x2, (int) x3, (int) x4},
              new int[] {(int) y, (int) y2, (int) y3, (int) y4},
              4);
      g2.fill(square);
      g2.setColor(Color.BLACK);
      g2.draw(square);

      double newSize = size * lengthRatio;
      drawPythagorasTree(g2, x2, y2, newSize, angle - 45, depth - 1);
      drawPythagorasTree(g2, x3, y3, newSize, angle + 45, depth - 1);
    }

    private void drawSpiralTree(
        Graphics2D g2, double x, double y, double angle, double length, int count, int depth) {
      if (depth == 0 || length < 1) return;

      double x2 = x + length * Math.cos(Math.toRadians(angle));
      double y2 = y + length * Math.sin(Math.toRadians(angle));

      g2.setColor(getColorForDepth(maxDepth - depth));
      g2.setStroke(new BasicStroke(getStrokeWidth(maxDepth - depth)));
      g2.draw(new Line2D.Double(x, y, x2, y2));

      double newLength = length * lengthRatio;
      double spiralAngle = 137.5; // Golden angle for natural spiral

      drawSpiralTree(g2, x2, y2, angle - spiralAngle, newLength, count + 1, depth - 1);
      if (count % 3 == 0) {
        drawSpiralTree(g2, x2, y2, angle + spiralAngle / 2, newLength * 0.8, count + 1, depth - 1);
      }
    }
  }

  class ControlPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    ControlPanel() {
      setLayout(new FlowLayout());

      JComboBox<FractalType> typeCombo = new JComboBox<>(FractalType.values());
      typeCombo.setRenderer(
          new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
              super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
              if (value instanceof FractalType ft) {
                setText(ft.displayName);
              }
              return this;
            }
          });

      JSlider depthSlider = new JSlider(1, 15, maxDepth);
      depthSlider.setMajorTickSpacing(2);
      depthSlider.setPaintTicks(true);
      depthSlider.setPaintLabels(true);
      JLabel depthLabel = new JLabel("Depth: " + maxDepth);

      JSlider angleSlider = new JSlider(10, 60, (int) angle);
      angleSlider.setMajorTickSpacing(10);
      angleSlider.setPaintTicks(true);
      JLabel angleLabel = new JLabel("Angle: " + (int) angle + "°");

      JSlider ratioSlider = new JSlider(50, 95, (int) (lengthRatio * 100));
      JLabel ratioLabel = new JLabel("Ratio: " + String.format(Locale.ROOT, "%.2f", lengthRatio));

      JCheckBox colorCheck = new JCheckBox("Color Gradient", useColorGradient);
      JButton animateBtn = new JButton("▶ Animate");
      JButton resetBtn = new JButton("Reset");

      typeCombo.addActionListener(
          e -> {
            currentType = (FractalType) typeCombo.getSelectedItem();
            drawPanel.repaint();
          });

      depthSlider.addChangeListener(
          e -> {
            maxDepth = depthSlider.getValue();
            depthLabel.setText("Depth: " + maxDepth);
            if (!animating) {
              drawPanel.repaint();
            }
          });

      angleSlider.addChangeListener(
          e -> {
            angle = angleSlider.getValue();
            angleLabel.setText("Angle: " + (int) angle + "°");
            drawPanel.repaint();
          });

      ratioSlider.addChangeListener(
          e -> {
            lengthRatio = ratioSlider.getValue() / 100.0;
            ratioLabel.setText("Ratio: " + String.format(Locale.ROOT, "%.2f", lengthRatio));
            drawPanel.repaint();
          });

      colorCheck.addActionListener(
          e -> {
            useColorGradient = colorCheck.isSelected();
            drawPanel.repaint();
          });

      animateBtn.addActionListener(
          e -> {
            if (!animating) {
              animating = true;
              animateBtn.setText("⏹ Stop");
              currentDepth = 0;

              Timer timer = new Timer(300, null);
              timer.addActionListener(
                  evt -> {
                    if (currentDepth <= maxDepth) {
                      currentDepth++;
                      drawPanel.repaint();
                    } else {
                      timer.stop();
                      animating = false;
                      animateBtn.setText("▶ Animate");
                    }
                  });
              timer.start();
            } else {
              animating = false;
              animateBtn.setText("▶ Animate");
            }
          });

      resetBtn.addActionListener(
          e -> {
            animating = false;
            currentDepth = 0;
            drawPanel.repaint();
          });

      add(new JLabel("Fractal: "));
      add(typeCombo);
      add(new JSeparator(SwingConstants.VERTICAL));
      add(depthLabel);
      add(depthSlider);
      add(new JSeparator(SwingConstants.VERTICAL));
      add(angleLabel);
      add(angleSlider);
      add(new JSeparator(SwingConstants.VERTICAL));
      add(ratioLabel);
      add(ratioSlider);
      add(new JSeparator(SwingConstants.VERTICAL));
      add(colorCheck);
      add(animateBtn);
      add(resetBtn);
    }
  }

  public static void main() {
    SwingUtilities.invokeLater(RecursiveTreeVisualizer::new);
  }
}
