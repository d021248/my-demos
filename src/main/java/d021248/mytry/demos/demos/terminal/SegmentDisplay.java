package d021248.mytry.demos.terminal;

/// Package-private 7-segment display renderer.
///
/// Renders a time string (digits + colons) as 5-row ASCII art.
/// Shared by {@link AsciiClockDemo} and {@link StopwatchDemo}.
final class SegmentDisplay {

  private SegmentDisplay() {}

  private static final int ROWS = 5;

  private record Glyph(String[] lines) {
    // @formatter:off
    static final Glyph ZERO = new Glyph(new String[] {" ___ ", "|   |", "|   |", "|   |", "|___|"});
    static final Glyph ONE = new Glyph(new String[] {"     ", "    |", "    |", "    |", "    |"});
    static final Glyph TWO = new Glyph(new String[] {" ___ ", "    |", "|___|", "|    ", "|___ "});
    static final Glyph THREE =
        new Glyph(new String[] {" ___ ", "    |", " ___|", "    |", " ___|"});
    static final Glyph FOUR = new Glyph(new String[] {"     ", "|   |", "|___|", "    |", "    |"});
    static final Glyph FIVE = new Glyph(new String[] {" ___ ", "|    ", "|___|", "    |", " ___|"});
    static final Glyph SIX = new Glyph(new String[] {" ___ ", "|    ", "|___|", "|   |", "|___|"});
    static final Glyph SEVEN =
        new Glyph(new String[] {" ___ ", "    |", "    |", "    |", "    |"});
    static final Glyph EIGHT =
        new Glyph(new String[] {" ___ ", "|   |", "|___|", "|   |", "|___|"});
    static final Glyph NINE = new Glyph(new String[] {" ___ ", "|   |", "|___|", "    |", " ___|"});
    static final Glyph COLON =
        new Glyph(new String[] {"     ", "  o  ", "     ", "  o  ", "     "});
    // @formatter:on

    static final Glyph[] MAP = {ZERO, ONE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE};

    static Glyph forChar(char c) {
      return switch (c) {
        case ':' -> COLON;
        case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> MAP[c - '0'];
        default -> COLON;
      };
    }
  }

  /// Renders {@code timeStr} (e.g. {@code "12:34:56"}) as a 5-line ASCII segment display.
  static String render(String timeStr) {
    var rows = new StringBuilder[ROWS];
    for (int i = 0; i < ROWS; i++) rows[i] = new StringBuilder();
    for (char c : timeStr.toCharArray()) {
      var glyph = Glyph.forChar(c);
      for (int i = 0; i < ROWS; i++) rows[i].append(glyph.lines()[i]).append("  ");
    }
    var sb = new StringBuilder();
    for (var row : rows) sb.append(row).append('\n');
    return sb.toString();
  }
}
