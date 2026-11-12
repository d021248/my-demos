package d021248.mytry.algorithms.visualization;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.util.Arrays;
import java.util.Random;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.WindowConstants;

/// Sorting Algorithm Race Visualizer
/// Watch Bubble Sort, Quick Sort, and Merge Sort race side-by-side!
///
/// Features:
/// - Real-time visualization of three sorting algorithms
/// - Color-coded bars showing comparisons and swaps
/// - Statistics tracking (comparisons, swaps, time)
/// - Adjustable array size and speed
/// - Different data patterns (random, sorted, reverse, nearly sorted)

@SuppressWarnings({"this-escape", "serial"})
public final class SortingRaceVisualizer extends JFrame {
  private static final long serialVersionUID = 1L;
  private static final int DEFAULT_SIZE = 100;
  private static final int BAR_WIDTH = 6;
  private static final int PANEL_HEIGHT = 300;

  private int arraySize = DEFAULT_SIZE;
  private int speed = 10; // milliseconds between steps

  private final SortPanel bubblePanel;
  private final SortPanel quickPanel;
  private final SortPanel mergePanel;
  private final ControlPanel controlPanel;

  public SortingRaceVisualizer() {
    super("Sorting Algorithm Race - Watch them compete!");

    bubblePanel = new SortPanel("Bubble Sort");
    quickPanel = new SortPanel("Quick Sort");
    mergePanel = new SortPanel("Merge Sort");
    controlPanel = new ControlPanel();

    setLayout(new GridLayout(4, 1));
    add(bubblePanel);
    add(quickPanel);
    add(mergePanel);
    add(controlPanel);

    setSize(arraySize * BAR_WIDTH + 50, PANEL_HEIGHT * 4 + 50);
    setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    setLocationRelativeTo(null);

    resetArrays(DataPattern.RANDOM);

    setVisible(true);
  }

  private enum DataPattern {
    RANDOM,
    SORTED,
    REVERSE,
    NEARLY_SORTED
  }

  private enum BarState {
    NORMAL,
    COMPARING,
    SWAPPING,
    SORTED
  }

  private static class Bar {
    int value;
    BarState state;

    Bar(int value) {
      this.value = value;
      this.state = BarState.NORMAL;
    }
  }

  private void resetArrays(DataPattern pattern) {
    int[] baseArray = generateArray(pattern);

    bubblePanel.setArray(Arrays.copyOf(baseArray, baseArray.length));
    quickPanel.setArray(Arrays.copyOf(baseArray, baseArray.length));
    mergePanel.setArray(Arrays.copyOf(baseArray, baseArray.length));

    bubblePanel.reset();
    quickPanel.reset();
    mergePanel.reset();
  }

  private int[] generateArray(DataPattern pattern) {
    int[] array = new int[arraySize];
    Random rand = new Random();

    switch (pattern) {
      case RANDOM -> {
        for (int i = 0; i < arraySize; i++) {
          array[i] = rand.nextInt(arraySize) + 1;
        }
      }
      case SORTED -> {
        for (int i = 0; i < arraySize; i++) {
          array[i] = i + 1;
        }
      }
      case REVERSE -> {
        for (int i = 0; i < arraySize; i++) {
          array[i] = arraySize - i;
        }
      }
      case NEARLY_SORTED -> {
        for (int i = 0; i < arraySize; i++) {
          array[i] = i + 1;
        }
        // Shuffle 10% of elements
        for (int i = 0; i < arraySize / 10; i++) {
          int idx1 = rand.nextInt(arraySize);
          int idx2 = rand.nextInt(arraySize);
          int temp = array[idx1];
          array[idx1] = array[idx2];
          array[idx2] = temp;
        }
      }
    }
    return array;
  }

  private void startRace() {
    bubblePanel.startSort();
    quickPanel.startSort();
    mergePanel.startSort();
  }

  private void stopRace() {
    bubblePanel.stopSort();
    quickPanel.stopSort();
    mergePanel.stopSort();
  }

  class SortPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private final String algorithmName;
    private Bar[] bars;
    private Timer timer;
    private long startTime;
    private long endTime;
    private int comparisons;
    private int swaps;
    private boolean sorting;

    // Sort state
    private int bubbleI;
    private int bubbleJ;

    private int[] quickStack;
    private int quickTop;

    private int[] mergeAux;

    SortPanel(String algorithmName) {
      this.algorithmName = algorithmName;
      setPreferredSize(new Dimension(600, PANEL_HEIGHT));
      setBorder(BorderFactory.createTitledBorder(algorithmName));
      setBackground(Color.WHITE);
    }

    void setArray(int[] values) {
      bars = new Bar[values.length];
      for (int i = 0; i < values.length; i++) {
        bars[i] = new Bar(values[i]);
      }
      repaint();
    }

    void reset() {
      comparisons = 0;
      swaps = 0;
      sorting = false;
      if (timer != null && timer.isRunning()) {
        timer.stop();
      }
      if (bars != null) {
        for (Bar bar : bars) {
          bar.state = BarState.NORMAL;
        }
      }
      bubbleI = 0;
      bubbleJ = 0;
      quickTop = -1;
      repaint();
    }

    void startSort() {
      if (sorting) return;
      sorting = true;
      startTime = System.currentTimeMillis();

      if (algorithmName.contains("Bubble")) {
        startBubbleSort();
      } else if (algorithmName.contains("Quick")) {
        startQuickSort();
      } else if (algorithmName.contains("Merge")) {
        startMergeSort();
      }
    }

    void stopSort() {
      sorting = false;
      if (timer != null) {
        timer.stop();
      }
    }

    private void startBubbleSort() {
      bubbleI = 0;
      bubbleJ = 0;

      timer =
          new Timer(
              speed,
              e -> {
                if (bubbleI < bars.length) {
                  if (bubbleJ < bars.length - bubbleI - 1) {
                    // Reset previous states
                    for (Bar bar : bars) {
                      if (bar.state != BarState.SORTED) {
                        bar.state = BarState.NORMAL;
                      }
                    }

                    // Compare
                    bars[bubbleJ].state = BarState.COMPARING;
                    bars[bubbleJ + 1].state = BarState.COMPARING;
                    comparisons++;

                    if (bars[bubbleJ].value > bars[bubbleJ + 1].value) {
                      // Swap
                      Bar temp = bars[bubbleJ];
                      bars[bubbleJ] = bars[bubbleJ + 1];
                      bars[bubbleJ + 1] = temp;
                      bars[bubbleJ].state = BarState.SWAPPING;
                      bars[bubbleJ + 1].state = BarState.SWAPPING;
                      swaps++;
                    }

                    bubbleJ++;
                  } else {
                    bars[bars.length - bubbleI - 1].state = BarState.SORTED;
                    bubbleI++;
                    bubbleJ = 0;
                  }
                  repaint();
                } else {
                  timer.stop();
                  endTime = System.currentTimeMillis();
                  for (Bar bar : bars) {
                    bar.state = BarState.SORTED;
                  }
                  repaint();
                }
              });
      timer.start();
    }

    private void startQuickSort() {
      quickStack = new int[bars.length * 2];
      quickTop = -1;

      // Push initial bounds
      pushQuick(0, bars.length - 1);

      timer = new Timer(speed, e -> quickSortStep());
      timer.start();
    }

    private void pushQuick(int low, int high) {
      quickStack[++quickTop] = low;
      quickStack[++quickTop] = high;
    }

    private int[] popQuick() {
      if (quickTop < 1) return null;
      int high = quickStack[quickTop--];
      int low = quickStack[quickTop--];
      return new int[] {low, high};
    }

    private void quickSortStep() {
      int[] bounds = popQuick();
      if (bounds == null) {
        timer.stop();
        endTime = System.currentTimeMillis();
        for (Bar bar : bars) {
          bar.state = BarState.SORTED;
        }
        repaint();
        return;
      }

      int low = bounds[0];
      int high = bounds[1];

      if (low < high) {
        int pi = partition(low, high);

        if (pi - 1 > low) {
          pushQuick(low, pi - 1);
        }
        if (pi + 1 < high) {
          pushQuick(pi + 1, high);
        }
      }
      repaint();
    }

    private int partition(int low, int high) {
      int pivot = bars[high].value;
      bars[high].state = BarState.COMPARING;

      int i = low - 1;
      for (int j = low; j < high; j++) {
        comparisons++;
        bars[j].state = BarState.COMPARING;

        if (bars[j].value < pivot) {
          i++;
          if (i != j) {
            Bar temp = bars[i];
            bars[i] = bars[j];
            bars[j] = temp;
            swaps++;
            bars[i].state = BarState.SWAPPING;
          }
        }
        bars[j].state = BarState.NORMAL;
      }

      Bar temp = bars[i + 1];
      bars[i + 1] = bars[high];
      bars[high] = temp;
      swaps++;

      bars[i + 1].state = BarState.SORTED;
      bars[high].state = BarState.NORMAL;

      return i + 1;
    }

    private void startMergeSort() {
      mergeAux = new int[bars.length];

      // Iterative merge sort
      timer = new Timer(speed * 2, e -> mergeSortStep());
      timer.start();
    }

    private int mergeSize = 1;
    private int mergeLeftStart = 0;

    private void mergeSortStep() {
      if (mergeSize >= bars.length) {
        timer.stop();
        endTime = System.currentTimeMillis();
        for (Bar bar : bars) {
          bar.state = BarState.SORTED;
        }
        repaint();
        return;
      }

      if (mergeLeftStart < bars.length - 1) {
        int mid = Math.min(mergeLeftStart + mergeSize - 1, bars.length - 1);
        int rightEnd = Math.min(mergeLeftStart + 2 * mergeSize - 1, bars.length - 1);

        merge(mergeLeftStart, mid, rightEnd);
        mergeLeftStart += 2 * mergeSize;
      } else {
        mergeLeftStart = 0;
        mergeSize *= 2;
      }

      repaint();
    }

    private void merge(int left, int mid, int right) {
      int i = left;
      int j = mid + 1;
      int k = left;

      // Copy to auxiliary array
      for (int p = left; p <= right; p++) {
        mergeAux[p] = bars[p].value;
        bars[p].state = BarState.COMPARING;
      }

      // Merge
      while (i <= mid && j <= right) {
        comparisons++;
        if (mergeAux[i] <= mergeAux[j]) {
          bars[k].value = mergeAux[i];
          bars[k].state = BarState.SWAPPING;
          i++;
        } else {
          bars[k].value = mergeAux[j];
          bars[k].state = BarState.SWAPPING;
          j++;
        }
        swaps++;
        k++;
      }

      while (i <= mid) {
        bars[k].value = mergeAux[i];
        bars[k].state = BarState.SWAPPING;
        swaps++;
        i++;
        k++;
      }

      while (j <= right) {
        bars[k].value = mergeAux[j];
        bars[k].state = BarState.SWAPPING;
        swaps++;
        j++;
        k++;
      }

      // Reset states
      for (int p = left; p <= right; p++) {
        bars[p].state = BarState.NORMAL;
      }
    }

    @Override
    protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      Graphics2D g2 = (Graphics2D) g;
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

      if (bars == null) return;

      int maxValue = Arrays.stream(bars).mapToInt(b -> b.value).max().orElse(1);
      int barAreaHeight = getHeight() - 60;

      for (int i = 0; i < bars.length; i++) {
        Bar bar = bars[i];
        int barHeight = (int) ((bar.value / (double) maxValue) * barAreaHeight);
        int x = i * BAR_WIDTH + 10;
        int y = getHeight() - barHeight - 40;

        Color color =
            switch (bar.state) {
              case COMPARING -> new Color(255, 165, 0); // Orange
              case SWAPPING -> new Color(255, 0, 0); // Red
              case SORTED -> new Color(0, 200, 0); // Green
              default -> new Color(100, 149, 237); // Cornflower blue
            };

        g2.setColor(color);
        g2.fillRect(x, y, BAR_WIDTH - 1, barHeight);
      }

      // Draw statistics
      g2.setColor(Color.BLACK);
      g2.setFont(new Font("Monospaced", Font.PLAIN, 12));
      String stats =
          "Comparisons: %d  |  Swaps: %d  |  Time: %dms"
              .formatted(
                  comparisons,
                  swaps,
                  sorting && endTime == 0
                      ? (System.currentTimeMillis() - startTime)
                      : (endTime - startTime));
      g2.drawString(stats, 10, getHeight() - 10);
    }
  }

  class ControlPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    ControlPanel() {
      setLayout(new FlowLayout());

      JButton startBtn = new JButton("▶ Start Race");
      JButton stopBtn = new JButton("⏹ Stop");
      JButton resetBtn = new JButton("🔄 Reset");

      JButton randomBtn = new JButton("Random");
      JButton sortedBtn = new JButton("Sorted");
      JButton reverseBtn = new JButton("Reverse");
      JButton nearlySortedBtn = new JButton("Nearly Sorted");

      JSlider sizeSlider = new JSlider(20, 200, DEFAULT_SIZE);
      sizeSlider.setMajorTickSpacing(60);
      sizeSlider.setPaintTicks(true);
      JLabel sizeLabel = new JLabel("Size: " + DEFAULT_SIZE);

      JSlider speedSlider = new JSlider(1, 50, speed);
      speedSlider.setMajorTickSpacing(10);
      speedSlider.setPaintTicks(true);
      JLabel speedLabel = new JLabel("Speed: " + speed + "ms");

      startBtn.addActionListener(e -> startRace());
      stopBtn.addActionListener(e -> stopRace());
      resetBtn.addActionListener(e -> resetArrays(DataPattern.RANDOM));

      randomBtn.addActionListener(e -> resetArrays(DataPattern.RANDOM));
      sortedBtn.addActionListener(e -> resetArrays(DataPattern.SORTED));
      reverseBtn.addActionListener(e -> resetArrays(DataPattern.REVERSE));
      nearlySortedBtn.addActionListener(e -> resetArrays(DataPattern.NEARLY_SORTED));

      sizeSlider.addChangeListener(
          e -> {
            arraySize = sizeSlider.getValue();
            sizeLabel.setText("Size: " + arraySize);
            resetArrays(DataPattern.RANDOM);
          });

      speedSlider.addChangeListener(
          e -> {
            speed = speedSlider.getValue();
            speedLabel.setText("Speed: " + speed + "ms");
          });

      add(startBtn);
      add(stopBtn);
      add(resetBtn);
      add(new JSeparator(SwingConstants.VERTICAL));
      add(randomBtn);
      add(sortedBtn);
      add(reverseBtn);
      add(nearlySortedBtn);
      add(new JSeparator(SwingConstants.VERTICAL));
      add(sizeLabel);
      add(sizeSlider);
      add(new JSeparator(SwingConstants.VERTICAL));
      add(speedLabel);
      add(speedSlider);
    }
  }

  public static void main() {
    SwingUtilities.invokeLater(SortingRaceVisualizer::new);
  }
}
