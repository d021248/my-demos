package d021248.mytry.demos.terminal;

import java.util.Random;

/// EmojiArt generates simple emoji-based art in the console.
/// It can create random patterns or basic shapes using emoji characters.
public final class EmojiArt {
  private static final String[] EMOJIS = Emojis.ART_SET;
  private static final Random RAND = new Random();

  public static void main() {
    System.out.println("Random Emoji Art:");
    printRandomArt(10, 20);
    System.out.println("\nHeart Shape:");
    printHeart(12);
  }

  // Prints a random grid of emojis
  public static void printRandomArt(int rows, int cols) {
    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < cols; j++) {
        System.out.print(EMOJIS[RAND.nextInt(EMOJIS.length)]);
      }
      System.out.println();
    }
  }

  // Prints a heart shape using emojis — simplified with String.repeat()
  public static void printHeart(int size) {
    String emoji = "\u2764\uFE0F"; // ❤️
    // Upper half: two bumps
    for (int i = size / 2; i < size; i += 2) {
      String indent = " ".repeat((size - i) / 2);
      String bump = emoji.repeat(i);
      String middle = " ".repeat(size - i);
      System.out.println(indent + bump + middle + bump);
    }
    // Lower triangle
    for (int i = size; i >= 1; i--) {
      System.out.println(" ".repeat(size - i) + emoji.repeat(i * 2 - 1));
    }
  }
}
