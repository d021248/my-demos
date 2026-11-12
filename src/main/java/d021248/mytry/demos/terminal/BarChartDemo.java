package d021248.mytry.demos.terminal;

import java.util.Locale;

/// ASCII bar chart demo.
///
/// Shows two visualisations:
///
/// 1. A static bar chart of the first 15 Fibonacci numbers.
/// 2. An animated sine-wave equalizer — 16 bands each following a
///    {@link Math#sin} wave with a different frequency.
///
/// Demonstrates: {@link Terminal.Color} gradient colouring,
/// {@link Terminal#animate}, and mutable lambda state via {@code int[]}.
///
public final class BarChartDemo {

  private static final int BAR_MAX = 40;
  private static final String BLOCK = "\u2588"; // █ full block
  private static final String SEP = "\u2502"; // │ vertical separator

  // ── bar builder ─────────────────────────────────────────────────────────────

  static String bar(String label, double value, double max) {
    int len = (int) Math.min(value / max * BAR_MAX, BAR_MAX);
    Terminal.Color color =
        value / max > 0.75
            ? Terminal.Color.RED
            : value / max > 0.45 ? Terminal.Color.YELLOW : Terminal.Color.GREEN;
    return String.format(
        Locale.ROOT,
        "  %-12s %s %s %5.1f",
        label,
        SEP,
        color.wrap(BLOCK.repeat(len) + " ".repeat(BAR_MAX - len)),
        value);
  }

  // ── static: Fibonacci ───────────────────────────────────────────────────────

  static String renderFibonacci() {
    var sb = new StringBuilder("=== Fibonacci Numbers ===\n\n");
    int a = 1, b = 1;
    double max = 610; // fib(15)
    for (int i = 1; i <= 15; i++) {
      sb.append(bar("fib(" + i + ")", a, max)).append('\n');
      int next = a + b;
      a = b;
      b = next;
    }
    return sb.toString();
  }

  // ── animated: sine-wave equalizer ───────────────────────────────────────────

  static String renderEqualizer(int tick) {
    var sb = new StringBuilder("=== Sine-Wave Equalizer (Ctrl+C to stop) ===\n\n");
    for (int i = 0; i < 16; i++) {
      double freq = 1.0 + i * 0.2;
      double value = (Math.sin(tick * 0.15 * freq + i * 0.7) + 1.0) * 50.0;
      sb.append(bar("band " + i, value, 100.0)).append('\n');
    }
    return sb.toString();
  }

  public static void main() throws InterruptedException {
    // Static chart first
    System.out.println(renderFibonacci());
    Thread.sleep(3000);

    // int[] is the idiomatic Java trick for mutating a lambda-captured variable
    int[] tick = {0};
    Terminal.animate(-1, 80, () -> renderEqualizer(tick[0]++));
  }
}
