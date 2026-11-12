package d021248.mytry.demos.terminal;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

/// Mandelbrot set rendered in ASCII using {@link CharPalette#DENSITY}.
///
/// Each pixel's escape-iteration count is mapped to a density character via
/// {@link CharPalette}. The rendering is expressed as a functional pipeline
/// over {@link IntStream}.
///
public final class MandelbrotAsciiDemo {

  private static final int WIDTH = 80, HEIGHT = 24, MAX_ITER = 40;
  private static final CharPalette PALETTE = CharPalette.DENSITY;

  /// Computes the ASCII character for pixel {@code (x, y)}.
  static char mandelbrotChar(int x, int y) {
    double zx = 0, zy = 0;
    // Classic Mandelbrot viewport: real in [-2.5, 1], imag in [-1, 1]
    double cX = (x - WIDTH * 0.6) * (3.5 / WIDTH);
    double cY = (y - HEIGHT / 2.0) * (2.0 / HEIGHT);
    int iter = 0;
    while (zx * zx + zy * zy < 4 && iter < MAX_ITER) {
      double tmp = zx * zx - zy * zy + cX;
      zy = 2.0 * zx * zy + cY;
      zx = tmp;
      iter++;
    }
    return iter == MAX_ITER ? ' ' : PALETTE.map(iter, MAX_ITER - 1);
  }

  public static void main() {
    System.out.println("\n=== Mandelbrot ASCII Art ===\n");
    IntStream.range(0, HEIGHT)
        .forEach(
            y -> {
              String row =
                  IntStream.range(0, WIDTH)
                      .mapToObj(x -> String.valueOf(mandelbrotChar(x, y)))
                      .collect(Collectors.joining());
              System.out.println(row);
            });
  }
}
