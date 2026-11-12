package d021248.mytry.demos.quiz;

public final class InheritanceAndPolymorphismQuiz {
  public static void main() {
    quiz();
    resolve();
  }

  public static void quiz() {
    // What is printed here?
    Parent obj = new Child();
    obj.print();
    ((Child) obj).print();
    System.out.println();
  }

  public static void resolve() {
    Parent obj = new Child();
    obj.print(); // Calls Child's print() due to dynamic dispatch
    ((Child) obj).print(); // Also calls Child's print()
    System.out.println();
  }

  static class Parent {
    void print() {
      System.out.println("Parent");
    }
  }

  static class Child extends Parent {
    @Override
    void print() {
      System.out.println("Child");
    }
  }
}
