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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

/// Number Theory Visualizer
/// Beautiful visualizations of prime numbers, Fibonacci, Collatz, and more.

@SuppressWarnings({"this-escape", "serial"})
public final class NumberTheoryVisualizer extends JFrame {
  private static final long serialVersionUID = 1L;
  private final DrawPanel drawPanel;
  private final ControlPanel controlPanel;

  private VisualizationType currentType = VisualizationType.ULAM_SPIRAL;

  private enum VisualizationType {
    ULAM_SPIRAL("Ulam Prime Spiral"),
    SIEVE("Sieve of Eratosthenes"),
    COLLATZ("Collatz Conjecture"),
    FIBONACCI_SPIRAL("Fibonacci Spiral");

    final String displayName;

    VisualizationType(String displayName) {
      this.displayName = displayName;
    }
  }

  private final Set<Integer> primes = new HashSet<>();
  private final List<Integer> collatzSequence = new ArrayList<>();
  private int sieveProgress = 2;
  private int maxNumber = 10000;

  public NumberTheoryVisualizer() {
    super("Number Theory Visualizations");

    drawPanel = new DrawPanel();
    controlPanel = new ControlPanel();

    setLayout(new BorderLayout());
    add(drawPanel, BorderLayout.CENTER);
    add(controlPanel, BorderLayout.SOUTH);

    setSize(900, 900);
    setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    setLocationRelativeTo(null);

    // Initialize data
    generatePrimes(maxNumber);
    generateCollatzSequence(27); // Default Collatz sequence
    sieveProgress = maxNumber; // Show all primes by default

    setVisible(true);
  }

  private void generatePrimes(int max) {
    primes.clear();
    boolean[] isPrime = new boolean[max + 1];
    Arrays.fill(isPrime, true);
    isPrime[0] = isPrime[1] = false;

    for (int i = 2; i * i <= max; i++) {
      if (isPrime[i]) {
        for (int j = i * i; j <= max; j += i) {
          isPrime[j] = false;
        }
      }
    }

    for (int i = 2; i <= max; i++) {
      if (isPrime[i]) {
        primes.add(i);
      }
    }
  }

  private void generateCollatzSequence(int start) {
    collatzSequence.clear();
    long n = start;
    while (n != 1 && collatzSequence.size() < 1000) {
      collatzSequence.add((int) Math.min(n, Integer.MAX_VALUE));
      n = (n % 2 == 0) ? n / 2 : 3 * n + 1;
    }
    collatzSequence.add(1);
  }

  class DrawPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    DrawPanel() {
      setBackground(Color.BLACK);
      setPreferredSize(new Dimension(850, 800));
    }

    @Override
    protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      Graphics2D g2 = (Graphics2D) g;
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

      switch (currentType) {
        case ULAM_SPIRAL -> drawUlamSpiral(g2);
        case SIEVE -> drawSieve(g2);
        case COLLATZ -> drawCollatz(g2);
        case FIBONACCI_SPIRAL -> drawFibonacciSpiral(g2);
      }
    }

    private void drawUlamSpiral(Graphics2D g2) {
      int size = 70; // Grid size for spiral
      int cellSize = Math.min(getWidth(), getHeight()) / size;

      // Start at center
      int x = 0;
      int y = 0;
      int dx = 1; // Start moving right
      int dy = 0;
      int segmentLength = 1;
      int segmentPassed = 0;
      int num = 1;

      for (int i = 0; i < size * size; i++) {
        // Draw if prime
        if (primes.contains(num)) {
          g2.setColor(Color.WHITE);
          int px = (x + size / 2) * cellSize + cellSize / 2;
          int py = (y + size / 2) * cellSize + cellSize / 2;
          int dotSize = Math.max(4, cellSize / 2);
          g2.fillOval(px - dotSize / 2, py - dotSize / 2, dotSize, dotSize);
        }

        num++;
        x += dx;
        y += dy;
        segmentPassed++;

        // Turn logic for spiral
        if (segmentPassed == segmentLength) {
          segmentPassed = 0;
          // Turn counter-clockwise: right->up, up->left, left->down, down->right
          int temp = dx;
          dx = -dy;
          dy = temp;

          // Increase segment length after every 2 turns (after completing a "layer")
          if (dy == 0) {
            segmentLength++;
          }
        }
      }

      g2.setColor(Color.WHITE);
      g2.drawString("Ulam Prime Spiral - Patterns in prime numbers", 10, 20);
    }

    private void drawSieve(Graphics2D g2) {
      int gridSize = 100;
      int cellSize = Math.min(getWidth(), getHeight()) / gridSize;

      for (int i = 2; i < gridSize * gridSize && i < sieveProgress; i++) {
        int x = (i % gridSize) * cellSize;
        int y = (i / gridSize) * cellSize;

        if (primes.contains(i)) {
          g2.setColor(new Color(100, 200, 255));
        } else {
          g2.setColor(new Color(50, 50, 50));
        }

        g2.fillRect(x, y, cellSize - 1, cellSize - 1);
      }

      g2.setColor(Color.WHITE);
      g2.drawString(
          "Sieve of Eratosthenes - Primes: "
              + primes.stream().filter(p -> p < sieveProgress).count(),
          10,
          20);
    }

    private void drawCollatz(Graphics2D g2) {
      if (collatzSequence.isEmpty()) return;

      int width = getWidth();
      int height = getHeight();

      double maxVal = collatzSequence.stream().mapToInt(Integer::intValue).max().orElse(1);
      double xStep = (double) width / collatzSequence.size();

      g2.setColor(Color.CYAN);
      g2.setStroke(new BasicStroke(2));

      for (int i = 1; i < collatzSequence.size(); i++) {
        int x1 = (int) ((i - 1) * xStep);
        int y1 = (int) (height - 100 - (collatzSequence.get(i - 1) / maxVal) * (height - 150));
        int x2 = (int) (i * xStep);
        int y2 = (int) (height - 100 - (collatzSequence.get(i) / maxVal) * (height - 150));

        g2.drawLine(x1, y1, x2, y2);
      }

      g2.setColor(Color.WHITE);
      g2.drawString(
          "Collatz Conjecture - Steps: " + collatzSequence.size() + ", Max: " + (int) maxVal,
          10,
          20);
    }

    private void drawFibonacciSpiral(Graphics2D g2) {
      int cx = getWidth() / 2;
      int cy = getHeight() / 2;

      long fib1 = 1;
      long fib2 = 1;
      int x = cx;
      int y = cy;
      int dir = 0; // 0=right, 1=up, 2=left, 3=down

      g2.setStroke(new BasicStroke(2));

      for (int i = 0; i < 15; i++) {
        long fib = fib1 + fib2;
        int size = (int) Math.min(fib, 200);

        float hue = i / 15f;
        g2.setColor(Color.getHSBColor(hue, 0.8f, 0.9f));

        switch (dir % 4) {
          case 0 -> { // right
            g2.drawArc(x, y - size, size, size, 270, 90);
            x += size;
          }
          case 1 -> { // up
            g2.drawArc(x - size, y - size, size, size, 0, 90);
            y -= size;
          }
          case 2 -> { // left
            g2.drawArc(x - size, y, size, size, 90, 90);
            x -= size;
          }
          case 3 -> { // down
            g2.drawArc(x, y, size, size, 180, 90);
            y += size;
          }
        }

        dir++;
        fib1 = fib2;
        fib2 = fib;
      }

      g2.setColor(Color.WHITE);
      g2.drawString("Fibonacci Spiral - Golden Ratio in Nature", 10, 20);
    }
  }

  class ControlPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private final JTextField collatzInput;
    private final JButton collatzBtn;
    private final JLabel collatzLabel;

    ControlPanel() {
      setLayout(new FlowLayout());

      JComboBox<VisualizationType> typeCombo = new JComboBox<>(VisualizationType.values());
      typeCombo.setRenderer(
          new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
              super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
              if (value instanceof VisualizationType vt) {
                setText(vt.displayName);
              }
              return this;
            }
          });

      collatzInput = new JTextField("27", 10);
      collatzBtn = new JButton("Generate Collatz");
      collatzLabel = new JLabel("Collatz Start: ");

      typeCombo.addActionListener(
          e -> {
            currentType = (VisualizationType) typeCombo.getSelectedItem();
            updateCollatzControlsVisibility();
            drawPanel.repaint();
          });

      collatzBtn.addActionListener(
          e -> {
            try {
              int num = Integer.parseInt(collatzInput.getText());
              generateCollatzSequence(num);
              currentType = VisualizationType.COLLATZ;
              typeCombo.setSelectedItem(VisualizationType.COLLATZ);
              drawPanel.repaint();
            } catch (NumberFormatException ex) {
              JOptionPane.showMessageDialog(this, "Please enter a valid number");
            }
          });

      add(new JLabel("Visualization: "));
      add(typeCombo);
      add(new JSeparator(SwingConstants.VERTICAL));
      add(collatzLabel);
      add(collatzInput);
      add(collatzBtn);

      updateCollatzControlsVisibility();
    }

    private void updateCollatzControlsVisibility() {
      boolean showCollatz = (currentType == VisualizationType.COLLATZ);
      collatzLabel.setVisible(showCollatz);
      collatzInput.setVisible(showCollatz);
      collatzBtn.setVisible(showCollatz);
    }
  }

  public static void main() {
    SwingUtilities.invokeLater(NumberTheoryVisualizer::new);
  }
}
