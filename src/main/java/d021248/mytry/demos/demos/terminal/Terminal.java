package d021248.mytry.demos.terminal;

import java.util.function.BooleanSupplier;

/// Central ANSI terminal utilities: escape codes, colours, and animation loop.
///
/// All demo classes in this package use these constants and helpers instead
/// of scattering raw escape-sequence literals throughout the code.
///
public final class Terminal {

  private Terminal() {}

  // ── Escape sequences ──────────────────────────────────────────────────────

  /// Clears the screen and moves the cursor to the home position.
  public static final String CLEAR = "\033[H\033[2J";

  /// Resets all colour and style attributes.
  public static final String RESET = "\033[0m";

  /// Clears the screen and flushes {@code stdout}.
  public static void clear() {
    System.out.print(CLEAR);
    System.out.flush();
  }

  // ── Foreground colours ────────────────────────────────────────────────────

  /// ANSI foreground colour codes.
  public enum Color {
    RED("\u001B[31m"),
    GREEN("\u001B[32m"),
    YELLOW("\u001B[33m"),
    BLUE("\u001B[34m"),
    MAGENTA("\u001B[35m"),
    CYAN("\u001B[36m");

    private final String code;

    Color(String code) {
      this.code = code;
    }

    /// Wraps {@code s} in this colour and appends the {@link Terminal#RESET} sequence.
    public String wrap(String s) {
      return code + s + RESET;
    }
  }

  // ── Animation loop ────────────────────────────────────────────────────────

  /// Produces one frame of terminal output as a {@code String}.
  @FunctionalInterface
  public interface Frame {
    String render();
  }

  /// Runs a terminal animation loop.
  ///
  /// @param frames   number of frames to render; {@code -1} loops indefinitely
  /// @param delayMs  delay in milliseconds between frames
  /// @param frame    frame renderer
  /// @throws InterruptedException if the thread is interrupted during sleep
  /// Runs a terminal animation loop with an optional stop condition.
  ///
  /// @param frames  number of frames; {@code -1} loops indefinitely
  /// @param delayMs delay in milliseconds between frames
  /// @param frame   frame renderer
  /// @param until   stop condition — the loop exits when this returns {@code true}
  public static void animate(int frames, long delayMs, Frame frame, BooleanSupplier until)
      throws InterruptedException {
    for (int i = 0; (frames < 0 || i < frames) && !until.getAsBoolean(); i++) {
      clear();
      System.out.print(frame.render());
      Thread.sleep(delayMs);
    }
  }

  /// Runs a terminal animation loop (no stop condition).
  ///
  /// @param frames  number of frames; {@code -1} loops indefinitely
  /// @param delayMs delay in milliseconds between frames
  /// @param frame   frame renderer
  public static void animate(int frames, long delayMs, Frame frame) throws InterruptedException {
    animate(frames, delayMs, frame, () -> false);
  }
}
