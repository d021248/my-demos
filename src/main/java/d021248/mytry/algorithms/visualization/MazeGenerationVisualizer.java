package d021248.mytry.algorithms.visualization;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Stack;
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

/// Maze Generation Visualizer
/// Watch different maze generation algorithms create mazes in real-time.

@SuppressWarnings({"this-escape", "serial"})
public final class MazeGenerationVisualizer extends JFrame {
  private static final long serialVersionUID = 1L;
  private static final int GRID_SIZE = 41; // Must be odd
  private static final int CELL_SIZE = 12;

  private final MazePanel mazePanel;
  private final ControlPanel controlPanel;

  private AlgorithmType currentAlgorithm = AlgorithmType.RECURSIVE_BACKTRACKER;
  private Timer timer;

  private enum CellState {
    WALL,
    PATH,
    CURRENT,
    VISITED,
    FRONTIER
  }

  private enum AlgorithmType {
    RECURSIVE_BACKTRACKER("Recursive Backtracker"),
    PRIMS("Prim's Algorithm"),
    KRUSKALS("Kruskal's Algorithm"),
    FLOOD_FILL("Flood Fill");

    final String displayName;

    AlgorithmType(String displayName) {
      this.displayName = displayName;
    }
  }

  private static class Cell {
    int x;
    int y;
    CellState state;
    int set; // For Kruskal's

    Cell(int x, int y) {
      this.x = x;
      this.y = y;
      this.state = CellState.WALL;
      this.set = -1;
    }
  }

  private final Cell[][] grid = new Cell[GRID_SIZE][GRID_SIZE];

  // Recursive Backtracker
  private final Stack<Cell> stack = new Stack<>();

  // Prim's Algorithm
  private final List<Cell> frontierCells = new ArrayList<>();

  // Kruskal's Algorithm
  private final List<int[]> walls = new ArrayList<>();
  private int nextSet = 0;

  public MazeGenerationVisualizer() {
    super("Maze Generation Visualizer - Watch Algorithms Create Mazes");

    initializeGrid();

    mazePanel = new MazePanel();
    controlPanel = new ControlPanel();

    setLayout(new BorderLayout());
    add(mazePanel, BorderLayout.CENTER);
    add(controlPanel, BorderLayout.SOUTH);

    setSize(GRID_SIZE * CELL_SIZE + 50, GRID_SIZE * CELL_SIZE + 120);
    setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    setLocationRelativeTo(null);

    setVisible(true);
  }

  private void initializeGrid() {
    for (int y = 0; y < GRID_SIZE; y++) {
      for (int x = 0; x < GRID_SIZE; x++) {
        grid[y][x] = new Cell(x, y);
      }
    }
  }

  private void resetMaze() {
    for (int y = 0; y < GRID_SIZE; y++) {
      for (int x = 0; x < GRID_SIZE; x++) {
        grid[y][x].state = CellState.WALL;
        grid[y][x].set = -1;
      }
    }
    stack.clear();
    frontierCells.clear();
    walls.clear();
    nextSet = 0;
  }

  private void startGeneration() {
    resetMaze();

    switch (currentAlgorithm) {
      case RECURSIVE_BACKTRACKER -> startRecursiveBacktracker();
      case PRIMS -> startPrims();
      case KRUSKALS -> startKruskals();
      case FLOOD_FILL -> startFloodFill();
    }

    if (timer != null) {
      timer.stop();
    }

    timer =
        new Timer(
            10,
            e -> {
              boolean done = stepGeneration();
              mazePanel.repaint();
              if (done) {
                timer.stop();
              }
            });
    timer.start();
  }

  private void startRecursiveBacktracker() {
    Cell start = grid[1][1];
    start.state = CellState.CURRENT;
    stack.push(start);
  }

  private boolean stepRecursiveBacktracker() {
    if (stack.isEmpty()) return true;

    Cell current = stack.peek();
    current.state = CellState.VISITED;

    List<Cell> unvisited = getUnvisitedNeighbors(current, 2);

    if (!unvisited.isEmpty()) {
      Cell next = unvisited.get(new Random().nextInt(unvisited.size()));

      // Remove wall between
      int wallX = (current.x + next.x) / 2;
      int wallY = (current.y + next.y) / 2;
      grid[wallY][wallX].state = CellState.PATH;

      next.state = CellState.CURRENT;
      stack.push(next);
    } else {
      stack.pop();
      if (!stack.isEmpty()) {
        stack.peek().state = CellState.CURRENT;
      }
    }

    return false;
  }

  private void startPrims() {
    Cell start = grid[1][1];
    start.state = CellState.PATH;
    addFrontierCells(start);
  }

  private boolean stepPrims() {
    if (frontierCells.isEmpty()) return true;

    Cell current = frontierCells.remove(new Random().nextInt(frontierCells.size()));

    List<Cell> pathNeighbors = new ArrayList<>();
    for (int[] dir : new int[][] {{0, 2}, {2, 0}, {0, -2}, {-2, 0}}) {
      int nx = current.x + dir[0];
      int ny = current.y + dir[1];
      if (nx > 0 && nx < GRID_SIZE && ny > 0 && ny < GRID_SIZE) {
        if (grid[ny][nx].state == CellState.PATH) {
          pathNeighbors.add(grid[ny][nx]);
        }
      }
    }

    if (!pathNeighbors.isEmpty()) {
      Cell pathNeighbor = pathNeighbors.get(new Random().nextInt(pathNeighbors.size()));

      // Connect current to path neighbor
      current.state = CellState.PATH;
      int wallX = (current.x + pathNeighbor.x) / 2;
      int wallY = (current.y + pathNeighbor.y) / 2;
      grid[wallY][wallX].state = CellState.PATH;

      addFrontierCells(current);
    }

    return false;
  }

  private void addFrontierCells(Cell cell) {
    for (int[] dir : new int[][] {{0, 2}, {2, 0}, {0, -2}, {-2, 0}}) {
      int nx = cell.x + dir[0];
      int ny = cell.y + dir[1];
      if (nx > 0 && nx < GRID_SIZE && ny > 0 && ny < GRID_SIZE) {
        Cell neighbor = grid[ny][nx];
        if (neighbor.state == CellState.WALL && !frontierCells.contains(neighbor)) {
          neighbor.state = CellState.FRONTIER;
          frontierCells.add(neighbor);
        }
      }
    }
  }

  private void startKruskals() {
    // Initialize each cell as its own set
    nextSet = 0;
    for (int y = 1; y < GRID_SIZE; y += 2) {
      for (int x = 1; x < GRID_SIZE; x += 2) {
        grid[y][x].state = CellState.PATH;
        grid[y][x].set = nextSet++;
      }
    }

    // Add all walls
    walls.clear();
    for (int y = 1; y < GRID_SIZE - 1; y += 2) {
      for (int x = 1; x < GRID_SIZE - 1; x += 2) {
        if (x + 2 < GRID_SIZE) {
          walls.add(new int[] {x, y, x + 2, y}); // Horizontal
        }
        if (y + 2 < GRID_SIZE) {
          walls.add(new int[] {x, y, x, y + 2}); // Vertical
        }
      }
    }
    Collections.shuffle(walls);
  }

  private boolean stepKruskals() {
    if (walls.isEmpty()) return true;

    int[] wall = walls.remove(0);
    int x1 = wall[0];
    int y1 = wall[1];
    int x2 = wall[2];
    int y2 = wall[3];

    Cell cell1 = grid[y1][x1];
    Cell cell2 = grid[y2][x2];

    if (cell1.set != cell2.set) {
      // Remove wall
      int wallX = (x1 + x2) / 2;
      int wallY = (y1 + y2) / 2;
      grid[wallY][wallX].state = CellState.PATH;
      grid[wallY][wallX].set = cell1.set;

      // Union sets
      int oldSet = cell2.set;
      int newSet = cell1.set;
      for (int y = 0; y < GRID_SIZE; y++) {
        for (int x = 0; x < GRID_SIZE; x++) {
          if (grid[y][x].set == oldSet) {
            grid[y][x].set = newSet;
          }
        }
      }
    }

    return false;
  }

  private void startFloodFill() {
    // Start with all paths
    for (int y = 0; y < GRID_SIZE; y++) {
      for (int x = 0; x < GRID_SIZE; x++) {
        grid[y][x].state = CellState.PATH;
      }
    }

    // Add random walls
    Random rand = new Random();
    for (int y = 0; y < GRID_SIZE; y++) {
      for (int x = 0; x < GRID_SIZE; x++) {
        if (rand.nextDouble() < 0.35) {
          grid[y][x].state = CellState.WALL;
        }
      }
    }
  }

  private boolean stepFloodFill() {
    return true; // Instant generation
  }

  private boolean stepGeneration() {
    return switch (currentAlgorithm) {
      case RECURSIVE_BACKTRACKER -> stepRecursiveBacktracker();
      case PRIMS -> stepPrims();
      case KRUSKALS -> stepKruskals();
      case FLOOD_FILL -> stepFloodFill();
    };
  }

  private List<Cell> getUnvisitedNeighbors(Cell cell, int distance) {
    List<Cell> neighbors = new ArrayList<>();
    int[][] dirs = {{0, distance}, {distance, 0}, {0, -distance}, {-distance, 0}};

    for (int[] dir : dirs) {
      int nx = cell.x + dir[0];
      int ny = cell.y + dir[1];
      if (nx > 0 && nx < GRID_SIZE && ny > 0 && ny < GRID_SIZE) {
        if (grid[ny][nx].state == CellState.WALL) {
          neighbors.add(grid[ny][nx]);
        }
      }
    }
    return neighbors;
  }

  class MazePanel extends JPanel {
    private static final long serialVersionUID = 1L;

    MazePanel() {
      setBackground(Color.WHITE);
      setPreferredSize(new Dimension(GRID_SIZE * CELL_SIZE, GRID_SIZE * CELL_SIZE));
    }

    @Override
    protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      Graphics2D g2 = (Graphics2D) g;

      for (int y = 0; y < GRID_SIZE; y++) {
        for (int x = 0; x < GRID_SIZE; x++) {
          Cell cell = grid[y][x];
          Color color =
              switch (cell.state) {
                case WALL -> Color.BLACK;
                case PATH -> Color.WHITE;
                case CURRENT -> new Color(255, 165, 0); // Orange
                case VISITED -> new Color(144, 238, 144); // Light green
                case FRONTIER -> new Color(173, 216, 230); // Light blue
              };

          g2.setColor(color);
          g2.fillRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
        }
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

      JButton generateBtn = new JButton("▶ Generate");
      JButton resetBtn = new JButton("Reset");

      algoCombo.addActionListener(
          e -> {
            currentAlgorithm = (AlgorithmType) algoCombo.getSelectedItem();
          });

      generateBtn.addActionListener(e -> startGeneration());
      resetBtn.addActionListener(
          e -> {
            resetMaze();
            mazePanel.repaint();
          });

      add(new JLabel("Algorithm: "));
      add(algoCombo);
      add(generateBtn);
      add(resetBtn);

      JLabel legend = new JLabel("  |  🟠 Current  🟢 Visited  🔵 Frontier  ⬛ Wall  ⬜ Path");
      add(legend);
    }
  }

  public static void main() {
    SwingUtilities.invokeLater(MazeGenerationVisualizer::new);
  }
}
