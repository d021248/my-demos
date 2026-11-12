package d021248.mytry.demos.terminal;

import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/// Animated Julia set rendered in colour ASCII art.
///
/// The complex parameter {@code c} rotates along the circle
/// {@code |c| = 0.7885}, producing a continuously morphing fractal.
/// Each pixel is coloured by its escape-iteration band via {@link Terminal.Color}.
///
/// Demonstrates: complex-number iteration, {@link CharPalette}, lambda
/// state with a mutable {@code double[]}, and {@link Terminal#animate}.
///
public final class JuliaSetDemo {

  private static final int WIDTH = 80;
  private static final int HEIGHT = 22;
  private static final int MAX_ITER = 60;
  private static final double RADIUS = 0.7885; // orbit radius for c
  private static final CharPalette PALETTE = CharPalette.DENSITY;

  static String juliaChar(int col, int row, double cr, double ci) {
    double zx = (col - WIDTH / 2.0) * (3.5 / WIDTH);
    double zy = (row - HEIGHT / 2.0) * (2.0 / HEIGHT);
    int iter = 0;
    while (zx * zx + zy * zy < 4 && iter < MAX_ITER) {
      double tmp = zx * zx - zy * zy + cr;
      zy = 2.0 * zx * zy + ci;
      zx = tmp;
      iter++;
    }
    if (iter == MAX_ITER) return " ";
    char c = PALETTE.map(iter, MAX_ITER - 1);
    return colorFor(iter).wrap(String.valueOf(c));
  }

  static Terminal.Color colorFor(int iter) {
    return switch (iter * 6 / MAX_ITER) {
      case 0 -> Terminal.Color.RED;
      case 1 -> Terminal.Color.YELLOW;
      case 2 -> Terminal.Color.GREEN;
      case 3 -> Terminal.Color.CYAN;
      case 4 -> Terminal.Color.BLUE;
      default -> Terminal.Color.MAGENTA;
    };
  }

  static String renderJulia(double cr, double ci) {
    var sb =
        new StringBuilder(
            String.format(
                Locale.ROOT, "Julia Set  c = %+.4f %+.4fi  (Ctrl+C to stop)\n\n", cr, ci));
    IntStream.range(0, HEIGHT)
        .forEach(
            row -> {
              String line =
                  IntStream.range(0, WIDTH)
                      .mapToObj(col -> juliaChar(col, row, cr, ci))
                      .collect(Collectors.joining());
              sb.append(line).append('\n');
            });
    return sb.toString();
  }

  public static void main() throws InterruptedException {
    // double[] is the idiomatic Java trick to mutate a variable captured in a
    // lambda
    double[] angle = {0.0};
    Terminal.animate(
        -1,
        80,
        () -> {
          angle[0] += 0.04;
          return renderJulia(RADIUS * Math.cos(angle[0]), RADIUS * Math.sin(angle[0]));
        });
  }
}
