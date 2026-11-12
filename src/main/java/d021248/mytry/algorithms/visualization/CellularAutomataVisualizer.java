package d021248.mytry.algorithms.visualization;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Random;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
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

/// Cellular Automata Visualizer
/// Demonstrates emergent complexity from simple rules.

@SuppressWarnings({"this-escape", "serial"})
public final class CellularAutomataVisualizer extends JFrame {
  private static final long serialVersionUID = 1L;
  private final DrawPanel drawPanel;
  private final ControlPanel controlPanel;

  private AutomataType currentType = AutomataType.CONWAY;
  private Timer timer;
  private int[][] grid;
  private int gridSize = 100;
  private int generation = 0;

  // Langton's Ant
  private int antX, antY, antDir; // 0=N, 1=E, 2=S, 3=W

  private enum AutomataType {
    CONWAY("Conway's Game of Life"),
    BRIANS_BRAIN("Brian's Brain"),
    LANGTONS_ANT("Langton's Ant"),
    WIREWORLD("Wireworld");

    final String displayName;

    AutomataType(String displayName) {
      this.displayName = displayName;
    }
  }

  public CellularAutomataVisualizer() {
    super("Cellular Automata Visualizations");

    drawPanel = new DrawPanel();
    controlPanel = new ControlPanel();

    setLayout(new BorderLayout());
    add(drawPanel, BorderLayout.CENTER);
    add(controlPanel, BorderLayout.SOUTH);

    setSize(900, 950);
    setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    setLocationRelativeTo(null);

    grid = new int[gridSize][gridSize];
    randomize();

    timer =
        new Timer(
            100,
            e -> {
              update();
              drawPanel.repaint();
            });
    timer.start();

    setVisible(true);
  }

  private void randomize() {
    Random rand = new Random();
    for (int i = 0; i < gridSize; i++) {
      for (int j = 0; j < gridSize; j++) {
        // Default randomization: for most automata give a simple on/off
        // For Wireworld seed conductors and occasionally electron heads
        if (currentType == AutomataType.WIREWORLD) {
          double r = rand.nextDouble();
          if (r < 0.45) {
            grid[i][j] = 1; // conductor
          } else if (r < 0.5) {
            grid[i][j] = 2; // electron head (small chance)
          } else {
            grid[i][j] = 0; // empty
          }
        } else {
          grid[i][j] = rand.nextDouble() < 0.3 ? 1 : 0;
        }
      }
    }

    if (currentType == AutomataType.LANGTONS_ANT) {
      antX = gridSize / 2;
      antY = gridSize / 2;
      antDir = 0;
      for (int i = 0; i < gridSize; i++) {
        Arrays.fill(grid[i], 0);
      }
    }

    generation = 0;
  }

  private void update() {
    generation++;

    switch (currentType) {
      case CONWAY -> updateConway();
      case BRIANS_BRAIN -> updateBriansBrain();
      case LANGTONS_ANT -> updateLangtonsAnt();
      case WIREWORLD -> updateWireworld();
    }
  }

  private void updateConway() {
    int[][] newGrid = new int[gridSize][gridSize];

    for (int i = 0; i < gridSize; i++) {
      for (int j = 0; j < gridSize; j++) {
        int neighbors = countNeighbors(i, j);

        if (grid[i][j] == 1) {
          newGrid[i][j] = (neighbors == 2 || neighbors == 3) ? 1 : 0;
        } else {
          newGrid[i][j] = (neighbors == 3) ? 1 : 0;
        }
      }
    }

    grid = newGrid;
  }

  private void updateBriansBrain() {
    // 0 = dead, 1 = alive, 2 = dying
    int[][] newGrid = new int[gridSize][gridSize];

    for (int i = 0; i < gridSize; i++) {
      for (int j = 0; j < gridSize; j++) {
        if (grid[i][j] == 1) {
          newGrid[i][j] = 2; // alive -> dying
        } else if (grid[i][j] == 2) {
          newGrid[i][j] = 0; // dying -> dead
        } else {
          int neighbors = countNeighbors(i, j);
          newGrid[i][j] = (neighbors == 2) ? 1 : 0;
        }
      }
    }

    grid = newGrid;
  }

  private void updateLangtonsAnt() {
    if (antX < 0 || antX >= gridSize || antY < 0 || antY >= gridSize) {
      return;
    }

    // At white square, turn right; at black square, turn left
    if (grid[antY][antX] == 0) {
      antDir = (antDir + 1) % 4; // turn right
      grid[antY][antX] = 1;
    } else {
      antDir = (antDir + 3) % 4; // turn left
      grid[antY][antX] = 0;
    }

    // Move forward
    switch (antDir) {
      case 0 -> antY--;
      case 1 -> antX++;
      case 2 -> antY++;
      case 3 -> antX--;
    }
  }

  private void updateWireworld() {
    // 0 = empty, 1 = conductor, 2 = electron head, 3 = electron tail
    int[][] newGrid = new int[gridSize][gridSize];

    for (int i = 0; i < gridSize; i++) {
      for (int j = 0; j < gridSize; j++) {
        switch (grid[i][j]) {
          case 0 -> newGrid[i][j] = 0;
          case 1 -> {
            int heads = countElectronHeads(i, j);
            newGrid[i][j] = (heads == 1 || heads == 2) ? 2 : 1;
          }
          case 2 -> newGrid[i][j] = 3;
          case 3 -> newGrid[i][j] = 1;
        }
      }
    }

    grid = newGrid;
  }

  private int countNeighbors(int x, int y) {
    int count = 0;
    for (int dx = -1; dx <= 1; dx++) {
      for (int dy = -1; dy <= 1; dy++) {
        if (dx == 0 && dy == 0) continue;
        int nx = (x + dx + gridSize) % gridSize;
        int ny = (y + dy + gridSize) % gridSize;
        if (grid[nx][ny] == 1) count++;
      }
    }
    return count;
  }

  private int countElectronHeads(int x, int y) {
    int count = 0;
    for (int dx = -1; dx <= 1; dx++) {
      for (int dy = -1; dy <= 1; dy++) {
        if (dx == 0 && dy == 0) continue;
        int nx = (x + dx + gridSize) % gridSize;
        int ny = (y + dy + gridSize) % gridSize;
        if (grid[nx][ny] == 2) count++;
      }
    }
    return count;
  }

  class DrawPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    DrawPanel() {
      setBackground(Color.BLACK);
      setPreferredSize(new Dimension(850, 850));

      addMouseListener(
          new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
              int cellSize = Math.min(getWidth(), getHeight()) / gridSize;
              int x = e.getX() / cellSize;
              int y = e.getY() / cellSize;

              if (x >= 0 && x < gridSize && y >= 0 && y < gridSize) {
                grid[y][x] = (grid[y][x] + 1) % getMaxState();
                repaint();
              }
            }
          });
    }

    private int getMaxState() {
      return switch (currentType) {
        case CONWAY, LANGTONS_ANT -> 2;
        case BRIANS_BRAIN -> 3;
        case WIREWORLD -> 4;
      };
    }

    @Override
    protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      Graphics2D g2 = (Graphics2D) g;

      int cellSize = Math.min(getWidth(), getHeight()) / gridSize;

      for (int i = 0; i < gridSize; i++) {
        for (int j = 0; j < gridSize; j++) {
          Color c = getCellColor(grid[i][j]);
          g2.setColor(c);
          g2.fillRect(j * cellSize, i * cellSize, cellSize - 1, cellSize - 1);
        }
      }

      // Draw Langton's ant
      if (currentType == AutomataType.LANGTONS_ANT
          && antX >= 0
          && antX < gridSize
          && antY >= 0
          && antY < gridSize) {
        g2.setColor(Color.RED);
        int[] dx = {0, 5, 0, -5};
        int[] dy = {-5, 0, 5, 0};
        g2.fillOval(antX * cellSize + dx[antDir], antY * cellSize + dy[antDir], 5, 5);
      }

      g2.setColor(Color.WHITE);
      g2.drawString("Generation: " + generation, 10, 20);
    }

    private Color getCellColor(int state) {
      return switch (currentType) {
        case CONWAY -> state == 1 ? Color.WHITE : Color.BLACK;
        case BRIANS_BRAIN ->
            switch (state) {
              case 1 -> Color.WHITE;
              case 2 -> new Color(100, 150, 255);
              default -> Color.BLACK;
            };
        case LANGTONS_ANT -> state == 1 ? Color.WHITE : Color.BLACK;
        case WIREWORLD ->
            switch (state) {
              case 1 -> new Color(255, 200, 0);
              case 2 -> Color.CYAN;
              case 3 -> Color.RED;
              default -> Color.BLACK;
            };
      };
    }
  }

  class ControlPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    ControlPanel() {
      setLayout(new FlowLayout());

      JComboBox<AutomataType> typeCombo = new JComboBox<>(AutomataType.values());
      typeCombo.setRenderer(
          new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
              super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
              if (value instanceof AutomataType at) {
                setText(at.displayName);
              }
              return this;
            }
          });

      JButton randomBtn = new JButton("Randomize");
      JButton clearBtn = new JButton("Clear");
      JButton pauseBtn = new JButton("Pause");

      JSlider speedSlider = new JSlider(10, 500, 100);
      speedSlider.setInverted(true);

      typeCombo.addActionListener(
          e -> {
            currentType = (AutomataType) typeCombo.getSelectedItem();
            randomize();
            drawPanel.repaint();
          });

      randomBtn.addActionListener(
          e -> {
            randomize();
            drawPanel.repaint();
          });

      clearBtn.addActionListener(
          e -> {
            for (int i = 0; i < gridSize; i++) {
              Arrays.fill(grid[i], 0);
            }
            generation = 0;
            drawPanel.repaint();
          });

      pauseBtn.addActionListener(
          e -> {
            if (timer.isRunning()) {
              timer.stop();
              pauseBtn.setText("Resume");
            } else {
              timer.start();
              pauseBtn.setText("Pause");
            }
          });

      speedSlider.addChangeListener(e -> timer.setDelay(speedSlider.getValue()));

      add(new JLabel("Type: "));
      add(typeCombo);
      add(randomBtn);
      add(clearBtn);
      add(pauseBtn);
      add(new JSeparator(SwingConstants.VERTICAL));
      add(new JLabel("Speed: "));
      add(speedSlider);
    }
  }

  public static void main() {
    SwingUtilities.invokeLater(CellularAutomataVisualizer::new);
  }
}
