package d021248.mytry.demos.games;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class TransfiniteSimulation {

  // A record representing a set that can contain other sets
  public record SetNode(Set<SetNode> elements, String level) {
    @Override
    public String toString() {
      return "V[" + level + "]: " + elements.size() + " elements";
    }
  }

  public static void main() {
    // Step 0: The empty set (base case)
    SetNode v0 = new SetNode(Collections.emptySet(), "0");

    System.out.println("Base case: " + v0);

    // Steps 1 & 2: Successor steps (power set)
    SetNode v1 = successor(v0, "1");
    SetNode v2 = successor(v1, "2");
    SetNode v3 = successor(v2, "3");

    System.out.println("Successor: " + v1);
    System.out.println("Successor: " + v2);
    System.out.println("Successor: " + v3);

    // Limit step (simulation of V-Omega)
    // In set theory: the union of ALL previous sets
    List<SetNode> history = List.of(v0, v1, v2, v3);
    SetNode vOmega = limit(history, "omega");

    System.out.println("Limit step: " + vOmega);
  }

  /// Successor logic: builds the power set P(V_alpha).
  private static SetNode successor(SetNode prev, String label) {
    // In set theory: V_{alpha+1} = P(V_alpha)
    // Simulated here by taking the previous set as an element
    Set<SetNode> newElements = new HashSet<>(prev.elements());
    newElements.add(prev);
    return new SetNode(newElements, label);
  }

  /// Limit logic: union of all previous sets.
  private static SetNode limit(List<SetNode> history, String label) {
    Set<SetNode> union =
        history.stream().flatMap(node -> node.elements().stream()).collect(Collectors.toSet());
    return new SetNode(union, label);
  }
}
