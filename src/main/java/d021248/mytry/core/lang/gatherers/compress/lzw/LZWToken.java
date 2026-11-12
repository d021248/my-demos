package d021248.mytry.core.lang.gatherers.compress.lzw;

/// A token emitted by the LZW compressor: a dictionary code together with the string it represents.
///
/// Carrying the entry string alongside the code makes tokens self-describing, which is useful for
/// logging, testing, and step-by-step visualization.
public record LZWToken(int code, String entry) {

  @Override
  public String toString() {
    return "[" + code + ":" + entry + "]";
  }
}
