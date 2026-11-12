package d021248.mytry.demos.terminal;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.IntStream;

/// Matrix-style digital rain animation.
///
/// Columns of half-width Katakana characters (U+FF66–U+FF9F) fall from top to
/// bottom. Each column has a bright-white head and a fading green tail.
///
/// Requires a Unicode-capable terminal (e.g. Windows Terminal, iTerm2, most Linux terminals).
///
public final class MatrixRainDemo {

  private static final int WIDTH = 60;
  private static final int HEIGHT = 24;
  private static final int TAIL_LEN = 12;
  private static final long FRAME_MS = 60;
  private static final Random RAND = new Random();

  // Half-width Katakana glyphs (U+FF66–U+FF9F) — single display width.
  private static final String[] CHARS =
      IntStream.rangeClosed(0xFF66, 0xFF9F).mapToObj(Character::toString).toArray(String[]::new);

  private static final int[] heads = new int[WIDTH];
  private static final String[][] grid = new String[HEIGHT][WIDTH];

  static {
    for (String[] row : grid) Arrays.fill(row, " ");
    for (int c = 0; c < WIDTH; c++) {
      heads[c] = -RAND.nextInt(HEIGHT); // stagger column starts
    }
  }

  static String renderFrame() {
    // Advance each column by one row.
    for (int c = 0; c < WIDTH; c++) {
      heads[c]++;
      if (heads[c] - TAIL_LEN > HEIGHT) heads[c] = -RAND.nextInt(HEIGHT / 2 + 1);
      int h = heads[c];
      if (h >= 0 && h < HEIGHT) grid[h][c] = CHARS[RAND.nextInt(CHARS.length)];
    }

    var sb = new StringBuilder();
    for (int row = 0; row < HEIGHT; row++) {
      for (int col = 0; col < WIDTH; col++) {
        int dist = heads[col] - row;
        if (dist == 0) {
          sb.append("\033[1;37m").append(grid[row][col]).append(Terminal.RESET); // bright white
        } else if (dist >= 1 && dist <= 3) {
          sb.append("\033[1;32m").append(grid[row][col]).append(Terminal.RESET); // bright green
        } else if (dist > 3 && dist <= TAIL_LEN) {
          sb.append(Terminal.Color.GREEN.wrap(grid[row][col])); // normal green
        } else {
          sb.append(' ');
        }
      }
      sb.append('\n');
    }
    return sb.toString();
  }

  public static void main() throws InterruptedException {
    Terminal.animate(-1, FRAME_MS, MatrixRainDemo::renderFrame);
  }
}
