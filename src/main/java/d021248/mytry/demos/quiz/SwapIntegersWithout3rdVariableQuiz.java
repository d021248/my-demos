package d021248.mytry.demos.quiz;

public final class SwapIntegersWithout3rdVariableQuiz {

  public static void main() {
    swap1(-123, 456);
    swap2(999, -314);
    swap3(-777, -123);
  }

  static void swap1(int x, int y) {
    System.out.println("x=" + x + ", y=" + y);

    x -= y; // x = (x-y), y = y
    y += x; // x = (x-y), y = (x-y) + y = x
    x = y - x; // x = x - (x-y) = y

    System.out.println("x=" + x + ", y=" + y);
  }

  static void swap2(int x, int y) {
    System.out.println("x=" + x + ", y=" + y);

    y = x + y - (x = y);

    System.out.println("x=" + x + ", y=" + y);
  }

  static void swap3(int x, int y) {
    System.out.println("x=" + x + ", y=" + y);

    // x = 100110101
    // y = 010101010
    x ^= y; // x = 110011111
    y ^= x; // y = 100110101
    x ^= y; // x = 010101010

    System.out.println("x=" + x + ", y=" + y);
  }
}
