/// Terminal art and animation demos.
///
/// Produces visual output entirely in the terminal: text rendered as ASCII art
/// using {@code java.awt} off-screen rendering, image-to-ASCII conversion,
/// an ASCII digital clock, a progress indicator, emoji art, a falling-emoji rain
/// effect, and a Mandelbrot set rendered in ASCII.
///
/// ## Shared infrastructure (since 1.1)
///
/// - {@link d021248.mytry.demos.terminal.Terminal} — central ANSI escape codes,
///   {@link d021248.mytry.demos.terminal.Terminal.Color} enum, and
///   {@link d021248.mytry.demos.terminal.Terminal#animate} animation loop.
/// - {@link d021248.mytry.demos.terminal.CharPalette} — reusable value-to-character
///   density mapping (used by {@code AsciiArtFromImage} and {@code MandelbrotAsciiDemo}).
/// - {@link d021248.mytry.demos.terminal.AsciiImageConverter} — pure, Swing-free
///   image-to-ASCII conversion (testable without UI).
///
/// @since 1.0
package d021248.mytry.demos.terminal;
