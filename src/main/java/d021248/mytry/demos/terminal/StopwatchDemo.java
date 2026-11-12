package d021248.mytry.demos.terminal;

/// ASCII stopwatch — counts elapsed time since the program started.
///
/// Uses the shared {@link SegmentDisplay} for rendering and
/// {@link Terminal#animate} for the animation loop.
/// Press Ctrl+C to stop.
///
public final class StopwatchDemo {

  public static void main() throws InterruptedException {
    long start = System.currentTimeMillis();
    Terminal.animate(
        -1,
        250,
        () -> {
          long ms = System.currentTimeMillis() - start;
          long h = ms / 3_600_000;
          long m = (ms % 3_600_000) / 60_000;
          long s = (ms % 60_000) / 1_000;
          String t = "%02d:%02d:%02d".formatted(h, m, s);
          return "=== Stopwatch (Ctrl+C to stop) ===\n\n" + SegmentDisplay.render(t);
        });
  }
}
