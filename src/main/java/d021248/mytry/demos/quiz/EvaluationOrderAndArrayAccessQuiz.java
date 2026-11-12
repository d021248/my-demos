package d021248.mytry.demos.quiz;

// https://blogs.oracle.com/javamagazine/post/quiz-yourself-evaluation-order-the-assignment-operator-and-array-access-in-java
public final class EvaluationOrderAndArrayAccessQuiz {

  public static void main() {
    quiz();
    resolve();
  }

  public static void quiz() {
    // what is printed here?
    int[] a = {3, 2, 1, 0, 4};
    int[] b = {2, 4, 2, 2, 1};
    System.out.println(a[(a = b)[2]]); // line n1
    System.out.println(a[(a = b)[2]]); // line n2
    System.out.println();
  }

  public static void resolve() {
    int[] a = {-1, -1, 1, -1, -1};
    int[] b = {-1, -1, 2, -1, -1};
    System.out.println(a[(a = b)[2]]); // line n1
    System.out.println(a[(a = b)[2]]); // line n2
    System.out.println();
  }
}
