package d021248.mytry.demos.terminal;

import java.util.Random;

/// Random Emoji Rain demo using {@link Terminal#animate}.
///
public final class EmojiRainDemo {

  private static final String[] EMOJIS = Emojis.RAIN_SET;
  private static final Random RAND = new Random();
  private static final int WIDTH = 40, HEIGHT = 20;

  static String renderFrame() {
    var sb = new StringBuilder();
    for (int y = 0; y < HEIGHT; y++) {
      for (int x = 0; x < WIDTH; x++) {
        sb.append(RAND.nextDouble() < 0.07 ? EMOJIS[RAND.nextInt(EMOJIS.length)] : ' ');
      }
      sb.append('\n');
    }
    return sb.toString();
  }

  public static void main() throws InterruptedException {
    System.out.println("\n=== Random Emoji Rain ===\n");
    Terminal.animate(40, 120, EmojiRainDemo::renderFrame);
    System.out.println("Emoji rain finished!");
  }
}
