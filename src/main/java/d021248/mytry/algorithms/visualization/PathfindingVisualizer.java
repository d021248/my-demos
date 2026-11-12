package d021248.mytry.algorithms.visualization;

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
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.Stack;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/// Interactive Pathfinding Visualizer
/// Demonstrates A*, Dijkstra, BFS, and DFS algorithms on a maze.
///
/// Features:
/// - Click to toggle walls
/// - Right-click to set start/end points
/// - Watch algorithms explore the maze in real-time
/// - Compare different pathfinding strategies

@SuppressWarnings({"this-escape", "serial"})
public final class PathfindingVisualizer extends JFrame {
  private static final long serialVersionUID = 1L;
  private static final int GRID_SIZE = 40;
  private static final int CELL_SIZE = 15;

  private enum CellType {
    EMPTY,
    WALL,
    START,
    END,
    VISITED,
    PATH,
    FRONTIER
  }

  private static class Cell {
    int x, y;
    CellType type;
    int g, h, f; // for A*
    Cell parent;

    Cell(int x, int y) {
      this.x = x;
      this.y = y;
      this.type = CellType.EMPTY;
    }

    int distance() {
      return g;
    }

    int heuristic(Cell end) {
      return Math.abs(x - end.x) + Math.abs(y - end.y); // Manhattan distance
    }
  }

  interface Algorithm {
    String name();

    void findPath(Cell start, Cell end);
  }

  private final Cell[][] grid = new Cell[GRID_SIZE][GRID_SIZE];
  private Cell start, end;

  private GridPanel gridPanel;
  private ControlPanel controlPanel;

  public PathfindingVisualizer() {
    super("Pathfinding Visualizer - Click to draw walls, Right-click for start/end");
    initializeGrid();

    gridPanel = new GridPanel();
    controlPanel = new ControlPanel();

    setLayout(new BorderLayout());
    add(gridPanel, BorderLayout.CENTER);
    add(controlPanel, BorderLayout.SOUTH);

    setSize(GRID_SIZE * CELL_SIZE + 20, GRID_SIZE * CELL_SIZE + 120);
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    setLocationRelativeTo(null);

    setVisible(true);
  }

  private void initializeGrid() {
    for (int y = 0; y < GRID_SIZE; y++) {
      for (int x = 0; x < GRID_SIZE; x++) {
        grid[y][x] = new Cell(x, y);
      }
    }
    start = grid[GRID_SIZE / 4][GRID_SIZE / 2];
    start.type = CellType.START;
    end = grid[GRID_SIZE * 3 / 4][GRID_SIZE / 2];
    end.type = CellType.END;
  }

  private void generateRandomMaze() {
    clearGrid();
    Random rand = new Random();
    for (int y = 0; y < GRID_SIZE; y++) {
      for (int x = 0; x < GRID_SIZE; x++) {
        if (grid[y][x] != start && grid[y][x] != end && rand.nextDouble() < 0.3) {
          grid[y][x].type = CellType.WALL;
        }
      }
    }
    gridPanel.repaint();
  }

  private void clearGrid() {
    for (int y = 0; y < GRID_SIZE; y++) {
      for (int x = 0; x < GRID_SIZE; x++) {
        Cell c = grid[y][x];
        if (c.type != CellType.START && c.type != CellType.END) {
          c.type = CellType.EMPTY;
        }
        c.parent = null;
        c.g = c.h = c.f = 0;
      }
    }
  }

  private void clearPath() {
    for (int y = 0; y < GRID_SIZE; y++) {
      for (int x = 0; x < GRID_SIZE; x++) {
        Cell c = grid[y][x];
        if (c.type == CellType.VISITED || c.type == CellType.PATH || c.type == CellType.FRONTIER) {
          c.type = CellType.EMPTY;
        }
        c.parent = null;
        c.g = c.h = c.f = 0;
      }
    }
  }

  private List<Cell> getNeighbors(Cell cell) {
    List<Cell> neighbors = new ArrayList<>();
    int[][] dirs = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}};
    for (int[] dir : dirs) {
      int nx = cell.x + dir[0];
      int ny = cell.y + dir[1];
      if (nx >= 0 && nx < GRID_SIZE && ny >= 0 && ny < GRID_SIZE) {
        Cell n = grid[ny][nx];
        if (n.type != CellType.WALL) {
          neighbors.add(n);
        }
      }
    }
    return neighbors;
  }

  private void reconstructPath(Cell current) {
    while (current.parent != null) {
      if (current.type != CellType.START && current.type != CellType.END) {
        current.type = CellType.PATH;
      }
      current = current.parent;
    }
  }

  class AStar implements Algorithm {
    public String name() {
      return "A* Algorithm";
    }

    public void findPath(Cell start, Cell end) {
      PriorityQueue<Cell> openSet = new PriorityQueue<>(Comparator.comparingInt(c -> c.f));
      Set<Cell> closedSet = new HashSet<>();

      start.g = 0;
      start.h = start.heuristic(end);
      start.f = start.h;
      openSet.add(start);

      Timer timer = new Timer(20, null);
      timer.addActionListener(
          e -> {
            if (openSet.isEmpty()) {
              timer.stop();
              JOptionPane.showMessageDialog(PathfindingVisualizer.this, "No path found!");
              return;
            }

            Cell current = openSet.poll();

            if (current == end) {
              timer.stop();
              reconstructPath(current);
              gridPanel.repaint();
              JOptionPane.showMessageDialog(PathfindingVisualizer.this, "Path found!");
              return;
            }

            closedSet.add(current);
            if (current.type != CellType.START && current.type != CellType.END) {
              current.type = CellType.VISITED;
            }

            for (Cell neighbor : getNeighbors(current)) {
              if (closedSet.contains(neighbor)) continue;

              int tentativeG = current.g + 1;

              if (!openSet.contains(neighbor)) {
                openSet.add(neighbor);
                if (neighbor.type != CellType.START && neighbor.type != CellType.END) {
                  neighbor.type = CellType.FRONTIER;
                }
              } else if (tentativeG >= neighbor.g) {
                continue;
              }

              neighbor.parent = current;
              neighbor.g = tentativeG;
              neighbor.h = neighbor.heuristic(end);
              neighbor.f = neighbor.g + neighbor.h;
            }

            gridPanel.repaint();
          });
      timer.start();
    }
  }

  class Dijkstra implements Algorithm {
    public String name() {
      return "Dijkstra's Algorithm";
    }

    public void findPath(Cell start, Cell end) {
      PriorityQueue<Cell> queue = new PriorityQueue<>(Comparator.comparingInt(Cell::distance));
      Set<Cell> visited = new HashSet<>();

      start.g = 0;
      queue.add(start);

      Timer timer = new Timer(20, null);
      timer.addActionListener(
          e -> {
            if (queue.isEmpty()) {
              timer.stop();
              JOptionPane.showMessageDialog(PathfindingVisualizer.this, "No path found!");
              return;
            }

            Cell current = queue.poll();

            if (visited.contains(current)) return;
            visited.add(current);

            if (current == end) {
              timer.stop();
              reconstructPath(current);
              gridPanel.repaint();
              JOptionPane.showMessageDialog(PathfindingVisualizer.this, "Path found!");
              return;
            }

            if (current.type != CellType.START && current.type != CellType.END) {
              current.type = CellType.VISITED;
            }

            for (Cell neighbor : getNeighbors(current)) {
              if (visited.contains(neighbor)) continue;

              int newDist = current.g + 1;
              if (neighbor.g == 0 || newDist < neighbor.g) {
                neighbor.g = newDist;
                neighbor.parent = current;
                queue.add(neighbor);
                if (neighbor.type != CellType.START && neighbor.type != CellType.END) {
                  neighbor.type = CellType.FRONTIER;
                }
              }
            }

            gridPanel.repaint();
          });
      timer.start();
    }
  }

  class BFS implements Algorithm {
    public String name() {
      return "Breadth-First Search";
    }

    public void findPath(Cell start, Cell end) {
      Queue<Cell> queue = new LinkedList<>();
      Set<Cell> visited = new HashSet<>();

      queue.add(start);
      visited.add(start);

      Timer timer = new Timer(20, null);
      timer.addActionListener(
          e -> {
            if (queue.isEmpty()) {
              timer.stop();
              JOptionPane.showMessageDialog(PathfindingVisualizer.this, "No path found!");
              return;
            }

            Cell current = queue.poll();

            if (current == end) {
              timer.stop();
              reconstructPath(current);
              gridPanel.repaint();
              JOptionPane.showMessageDialog(PathfindingVisualizer.this, "Path found!");
              return;
            }

            if (current.type != CellType.START && current.type != CellType.END) {
              current.type = CellType.VISITED;
            }

            for (Cell neighbor : getNeighbors(current)) {
              if (!visited.contains(neighbor)) {
                visited.add(neighbor);
                neighbor.parent = current;
                queue.add(neighbor);
                if (neighbor.type != CellType.START && neighbor.type != CellType.END) {
                  neighbor.type = CellType.FRONTIER;
                }
              }
            }

            gridPanel.repaint();
          });
      timer.start();
    }
  }

  class DFS implements Algorithm {
    public String name() {
      return "Depth-First Search";
    }

    public void findPath(Cell start, Cell end) {
      Stack<Cell> stack = new Stack<>();
      Set<Cell> visited = new HashSet<>();

      stack.push(start);

      Timer timer = new Timer(20, null);
      timer.addActionListener(
          e -> {
            if (stack.isEmpty()) {
              timer.stop();
              JOptionPane.showMessageDialog(PathfindingVisualizer.this, "No path found!");
              return;
            }

            Cell current = stack.pop();

            if (visited.contains(current)) return;
            visited.add(current);

            if (current == end) {
              timer.stop();
              reconstructPath(current);
              gridPanel.repaint();
              JOptionPane.showMessageDialog(PathfindingVisualizer.this, "Path found!");
              return;
            }

            if (current.type != CellType.START && current.type != CellType.END) {
              current.type = CellType.VISITED;
            }

            for (Cell neighbor : getNeighbors(current)) {
              if (!visited.contains(neighbor)) {
                neighbor.parent = current;
                stack.push(neighbor);
                if (neighbor.type != CellType.START && neighbor.type != CellType.END) {
                  neighbor.type = CellType.FRONTIER;
                }
              }
            }

            gridPanel.repaint();
          });
      timer.start();
    }
  }

  class GridPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private boolean drawing = false;
    private boolean settingStart = false;

    GridPanel() {
      setPreferredSize(new Dimension(GRID_SIZE * CELL_SIZE, GRID_SIZE * CELL_SIZE));
      setBackground(Color.WHITE);

      MouseAdapter ma =
          new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
              handleMouse(e);
              drawing = true;
              settingStart = SwingUtilities.isRightMouseButton(e);
            }

            @Override
            public void mouseDragged(MouseEvent e) {
              if (drawing && !settingStart) {
                handleMouse(e);
              }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
              drawing = false;
              settingStart = false;
            }

            private void handleMouse(MouseEvent e) {
              int x = e.getX() / CELL_SIZE;
              int y = e.getY() / CELL_SIZE;
              if (x >= 0 && x < GRID_SIZE && y >= 0 && y < GRID_SIZE) {
                Cell cell = grid[y][x];

                if (SwingUtilities.isRightMouseButton(e)) {
                  if (cell != start && cell != end) {
                    if (e.isControlDown()) {
                      // Ctrl + Right-click = set end
                      end.type = CellType.EMPTY;
                      end = cell;
                      cell.type = CellType.END;
                    } else {
                      // Right-click = set start
                      start.type = CellType.EMPTY;
                      start = cell;
                      cell.type = CellType.START;
                    }
                  }
                } else {
                  // Left-click = toggle wall
                  if (cell != start && cell != end) {
                    cell.type = (cell.type == CellType.WALL) ? CellType.EMPTY : CellType.WALL;
                  }
                }
                repaint();
              }
            }
          };

      addMouseListener(ma);
      addMouseMotionListener(ma);
    }

    @Override
    protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      Graphics2D g2 = (Graphics2D) g;
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

      for (int y = 0; y < GRID_SIZE; y++) {
        for (int x = 0; x < GRID_SIZE; x++) {
          Cell cell = grid[y][x];
          Color color =
              switch (cell.type) {
                case WALL -> Color.BLACK;
                case START -> new Color(0, 200, 0);
                case END -> new Color(200, 0, 0);
                case VISITED -> new Color(135, 206, 250);
                case PATH -> new Color(255, 215, 0);
                case FRONTIER -> new Color(147, 112, 219);
                default -> Color.WHITE;
              };

          g2.setColor(color);
          g2.fillRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE);

          g2.setColor(Color.LIGHT_GRAY);
          g2.drawRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
        }
      }
    }
  }

  class ControlPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    ControlPanel() {
      setLayout(new FlowLayout());

      JButton astarBtn = new JButton("A* Search");
      JButton dijkstraBtn = new JButton("Dijkstra");
      JButton bfsBtn = new JButton("BFS");
      JButton dfsBtn = new JButton("DFS");
      JButton mazeBtn = new JButton("Random Maze");
      JButton clearBtn = new JButton("Clear Path");
      JButton resetBtn = new JButton("Reset Grid");

      astarBtn.addActionListener(
          e -> {
            clearPath();
            new AStar().findPath(start, end);
          });
      dijkstraBtn.addActionListener(
          e -> {
            clearPath();
            new Dijkstra().findPath(start, end);
          });
      bfsBtn.addActionListener(
          e -> {
            clearPath();
            new BFS().findPath(start, end);
          });
      dfsBtn.addActionListener(
          e -> {
            clearPath();
            new DFS().findPath(start, end);
          });
      mazeBtn.addActionListener(e -> generateRandomMaze());
      clearBtn.addActionListener(
          e -> {
            clearPath();
            gridPanel.repaint();
          });
      resetBtn.addActionListener(
          e -> {
            clearGrid();
            gridPanel.repaint();
          });

      add(astarBtn);
      add(dijkstraBtn);
      add(bfsBtn);
      add(dfsBtn);
      add(new JSeparator(SwingConstants.VERTICAL));
      add(mazeBtn);
      add(clearBtn);
      add(resetBtn);

      JLabel legend = new JLabel("  |  🟢 Start  🔴 End  ⬛ Wall  🟦 Visited  🟨 Path  🟪 Frontier");
      add(legend);
    }
  }

  public static void main() {
    SwingUtilities.invokeLater(PathfindingVisualizer::new);
  }
}
