package d021248.mytry.demos.terminal;

/// Maps a numeric value in {@code [0, max]} to a character from a density palette.
///
/// The first character of {@code chars} represents the lowest value
/// (dark/empty), the last represents the highest (bright/full).
/// Pass {@code negative = true} to reverse the mapping.
///
public record CharPalette(String chars) {

  /// Standard 10-level density palette: space = empty, {@code #} = full.
  public static final CharPalette DENSITY = new CharPalette(" .:-=+*%@#");

  /// Image-to-ASCII palette tuned for greyscale photographs.
  public static final CharPalette IMAGE = new CharPalette(" .*:o&8#@");

  /// Maps {@code value} in {@code [0, max]} to a character.
  ///
  /// @param value    numeric intensity; clamped to {@code [0, max]}
  /// @param max      expected maximum value
  /// @param negative {@code true} to reverse the palette (dark↔light)
  /// @return the mapped character
  public char map(double value, double max, boolean negative) {
    double clamped = Math.clamp(value, 0.0, max);
    int idx = (int) (clamped / max * (chars.length() - 1));
    int i = negative ? (chars.length() - 1 - idx) : idx;
    return chars.charAt(i);
  }

  /// Shorthand for {@code map(value, max, false)}.
  public char map(double value, double max) {
    return map(value, max, false);
  }
}
