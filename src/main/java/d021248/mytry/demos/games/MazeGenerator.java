package d021248.mytry.demos.games;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.Arrays;
import java.util.Random;
import javax.swing.JFrame;
import javax.swing.JPanel;

/// Generates and prints a random maze using depth-first search.
/// Walls are represented by '#', paths by ' '.
public final class MazeGenerator {
  private final int width, height;
  private final char[][] maze;
  private final Random rand = new Random();

  public MazeGenerator(int width, int height) {
    this.width = width | 1; // ensure odd
    this.height = height | 1; // ensure odd
    maze = new char[this.height][this.width];
    for (char[] row : maze) {
      Arrays.fill(row, '#');
    }
    generate(1, 1);
  }

  private void generate(int x, int y) {
    maze[y][x] = ' ';
    int[] dirs = {0, 1, 2, 3};
    for (int i = 0; i < dirs.length; i++) {
      int j = rand.nextInt(dirs.length);
      int tmp = dirs[i];
      dirs[i] = dirs[j];
      dirs[j] = tmp;
    }
    for (int dir : dirs) {
      var offsets =
          switch (dir) {
            case 0 -> new int[] {2, 0}; // right
            case 1 -> new int[] {-2, 0}; // left
            case 2 -> new int[] {0, 2}; // down
            case 3 -> new int[] {0, -2}; // up
            default -> new int[] {0, 0};
          };
      int dx = offsets[0];
      int dy = offsets[1];
      int nx = x + dx, ny = y + dy;
      if (ny > 0 && ny < height && nx > 0 && nx < width && maze[ny][nx] == '#') {
        maze[y + dy / 2][x + dx / 2] = ' ';
        generate(nx, ny);
      }
    }
  }

  public void printMaze() {
    for (char[] row : maze) {
      System.out.println(new String(row));
    }
  }

  // --- Swing visualization ---
  static class MazePanel extends JPanel {
    private final MazeGenerator mg;
    private final int cellSize;

    public MazePanel(MazeGenerator mg, int cellSize) {
      this.mg = mg;
      this.cellSize = cellSize;
      setPreferredSize(new Dimension(mg.width * cellSize, mg.height * cellSize));
    }

    @Override
    protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      for (int y = 0; y < mg.height; y++) {
        for (int x = 0; x < mg.width; x++) {
          g.setColor(mg.maze[y][x] == '#' ? Color.BLACK : Color.WHITE);
          g.fillRect(x * cellSize, y * cellSize, cellSize, cellSize);
        }
      }
    }
  }

  public static void main() {
    MazeGenerator mg = new MazeGenerator(41, 21);
    JFrame frame = new JFrame("Maze Generator");
    MazePanel panel = new MazePanel(mg, 15);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.add(panel);
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}
