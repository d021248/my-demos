package d021248.mytry.demos.terminal;

import java.util.function.IntFunction;

/// ASCII-based progress indicators for console applications.
///
/// Includes: progress bar, spinner, colourful spinner, growing dots, and arrow.
///
public final class AsciiProgressIndicator {

  // Shared loop for frame-based spinners: print frame, sleep, then "Done!".
  private static void spinLoop(int steps, int delayMs, IntFunction<String> frameOf)
      throws InterruptedException {
    for (int i = 0; i < steps; i++) {
      System.out.print("\r" + frameOf.apply(i));
      Thread.sleep(delayMs);
    }
    System.out.print("\rDone!           \n");
  }

  // Progress Bar: [#####-----] 50%
  public static void showProgressBar(int percent) {
    int width = 30;
    int done = percent * width / 100;
    String bar = "[" + "#".repeat(done) + "-".repeat(width - done) + "] " + percent + "%";
    System.out.print("\r" + bar);
    if (percent == 100) {
      System.out.println();
    }
  }

  // Spinner: | / - \ ...
  public static void showSpinner(int steps, int delayMs) throws InterruptedException {
    char[] frames = {'|', '/', '-', '\\'};
    spinLoop(steps, delayMs, i -> String.valueOf(frames[i % frames.length]) + " Loading");
  }

  // Colourful Spinner — uses Terminal.Color enum
  public static void showColorfulSpinner(int cycles, int delayMs) throws InterruptedException {
    String[] frames = {"|", "/", "-", "\\"};
    var colors = Terminal.Color.values();
    spinLoop(
        cycles,
        delayMs,
        i -> colors[i % colors.length].wrap(frames[i % frames.length]) + " Spinning...");
  }

  // Growing Dots: Loading. Loading.. Loading...
  public static void showGrowingDots(int cycles, int delayMs) throws InterruptedException {
    for (int i = 0; i < cycles; i++) {
      String dots = ".".repeat(i % 4);
      System.out.print("\rLoading" + dots + "   ");
      Thread.sleep(delayMs);
    }
    System.out.print("\rDone!      \n");
  }

  // Animated Arrow: ===>
  public static void showArrow(int steps, int delayMs) throws InterruptedException {
    int width = 20;
    for (int i = 0; i < steps; i++) {
      int pos = i % width;
      String arrow = " ".repeat(pos) + ">>>";
      System.out.print("\r" + arrow);
      Thread.sleep(delayMs);
    }
    System.out.print("\rDone!      \n");
  }

  // Demo for all indicators
  public static void main() throws InterruptedException {
    System.out.println("\n=== ASCII Progress Indicators ===\n");
    System.out.println("Progress Bar:");
    for (int i = 0; i <= 100; i += 5) {
      showProgressBar(i);
      Thread.sleep(40);
    }
    System.out.println("Spinner:");
    showSpinner(20, 80);
    System.out.println("Colorful Spinner:");
    showColorfulSpinner(40, 100);
    System.out.println("Growing Dots:");
    showGrowingDots(20, 100);
    System.out.println("Animated Arrow:");
    showArrow(40, 30);
  }
}
