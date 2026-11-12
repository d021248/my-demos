package d021248.mytry.demos.quiz;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;

// https://blogs.oracle.com/javamagazine/post/quiz-yourself-treesets-linkedlists-and-arraydeques-in-java
public final class SetsListsDequeuesQuiz {

  public static void main() {
    quiz();
  }

  public static void quiz() {
    Set<String> ss = new TreeSet<>();
    Queue<String> q = new LinkedList<>();
    Deque<String> dq = new ArrayDeque<>(1);

    ss.add("John");
    ss.add("Alan");

    q.offer("Bert");
    q.offer("Kathy");

    dq.add("Jane");
    dq.addFirst("Alex"); // line n1

    ss.addAll(q);
    dq.addAll(ss); // line n2

    System.out.print(dq);
  }
}
