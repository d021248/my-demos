package d021248.mytry.demos.games;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.Random;
import javax.swing.JFrame;
import javax.swing.JPanel;

/// Simple console-based Conway's Game of Life implementation.
public final class GameOfLife {
  private final int width, height;
  private boolean[][] grid;
  private final Random rand = new Random();

  public GameOfLife(int width, int height) {
    this.width = width;
    this.height = height;
    grid = new boolean[height][width];
    randomize();
  }

  public void randomize() {
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        grid[y][x] = rand.nextBoolean();
      }
    }
  }

  public void step() {
    boolean[][] next = new boolean[height][width];
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        int neighbors = countNeighbors(x, y);
        next[y][x] =
            (grid[y][x] && (neighbors == 2 || neighbors == 3)) || (!grid[y][x] && neighbors == 3);
      }
    }
    grid = next;
  }

  private int countNeighbors(int x, int y) {
    int count = 0;
    for (int dy = -1; dy <= 1; dy++) {
      for (int dx = -1; dx <= 1; dx++) {
        if (!(dx == 0 && dy == 0)) {
          int nx = x + dx, ny = y + dy;
          if (nx >= 0 && nx < width && ny >= 0 && ny < height && grid[ny][nx]) {
            count++;
          }
        }
      }
    }
    return count;
  }

  public void print() {
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        System.out.print(grid[y][x] ? 'O' : '.');
      }
      System.out.println();
    }
  }

  // --- Swing visualization ---
  static class LifePanel extends JPanel {
    private final GameOfLife gol;
    private final int cellSize;

    public LifePanel(GameOfLife gol, int cellSize) {
      this.gol = gol;
      this.cellSize = cellSize;
      setPreferredSize(new Dimension(gol.width * cellSize, gol.height * cellSize));
    }

    @Override
    protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      for (int y = 0; y < gol.height; y++) {
        for (int x = 0; x < gol.width; x++) {
          g.setColor(gol.grid[y][x] ? Color.GREEN : Color.BLACK);
          g.fillRect(x * cellSize, y * cellSize, cellSize, cellSize);
        }
      }
    }
  }

  public static void main() throws InterruptedException {
    GameOfLife gol = new GameOfLife(60, 30);
    JFrame frame = new JFrame("Game of Life");
    LifePanel panel = new LifePanel(gol, 15);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.add(panel);
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
    while (true) {
      gol.step();
      panel.repaint();
      Thread.sleep(100);
    }
  }
}
