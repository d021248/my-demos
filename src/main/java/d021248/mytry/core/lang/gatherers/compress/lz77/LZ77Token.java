package d021248.mytry.core.lang.gatherers.compress.lz77;

/// Sealed token type produced by the LZ77 compressor.
/// A {@link Literal} carries a single unmatched character;
/// a {@link Match} carries a back-reference offset and match length into the search buffer.
public sealed interface LZ77Token {

  // A new character that had no match in the search buffer
  record Literal(char value) implements LZ77Token {
    @Override
    public String toString() {
      return value + "";
    }
  }

  // A back-reference into the search buffer:
  // offset = how many characters back the match starts,
  // length = how many consecutive characters match
  record Match(int offset, int length) implements LZ77Token {
    @Override
    public String toString() {
      return "(offset=" + offset + ", length=" + length + ")";
    }
  }
}
