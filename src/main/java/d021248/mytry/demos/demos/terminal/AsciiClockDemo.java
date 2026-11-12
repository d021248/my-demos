package d021248.mytry.demos.terminal;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/// ASCII digital clock rendered with {@link Terminal#animate}.
///
/// Delegates all digit-glyph data and rendering to {@link SegmentDisplay}.
///
public final class AsciiClockDemo {

  private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

  static String renderClock() {
    return "=== ASCII Digital Clock ===\n\n" + SegmentDisplay.render(LocalTime.now().format(FMT));
  }

  public static void main() throws InterruptedException {
    Terminal.animate(-1, 1000, AsciiClockDemo::renderClock);
  }
}
