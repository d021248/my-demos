package d021248.mytry.demos.games;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.stream.Stream;

/// Modern Java 21 Genetic Evolution Showcase
/// Combines Sealed Classes, Records, and Pattern Matching.
public final class EvolutionApp {

  // === Domain Model ===

  /// A record representing an individual.
  /// It is immutable, which prevents side effects.
  public record Individual(int[] genes, double fitness) {
    @Override
    public String toString() {
      return String.format(
          Locale.ROOT, "Fitness: %.2f | Gene: %s", fitness, Arrays.toString(genes));
    }
  }

  /// Sealed interface for evolution steps.
  /// Only Mutation and Crossover may implement this contract.
  sealed interface EvolutionOp permits Mutation, Crossover {}

  record Mutation(double rate) implements EvolutionOp {}

  record Crossover(Individual partner) implements EvolutionOp {}

  // === Main ===

  public static void main() {
    final int POP_SIZE = 200;
    final int GENOME_LEN = 40;
    final int GENERATIONS = 10000;
    Random rand = new Random();

    List<Individual> population =
        Stream.generate(() -> createRandom(GENOME_LEN, rand)).limit(POP_SIZE).toList();

    System.out.println("=== Starting evolution ===");

    for (int gen = 0; gen <= GENERATIONS; gen++) {
      population =
          population.stream()
              .map(EvolutionApp::evaluate)
              .sorted(Comparator.comparingDouble(Individual::fitness).reversed())
              .toList();

      if (gen % 20 == 0) {
        System.out.println("Generation " + gen + ": " + population.getFirst());
      }

      if (population.getFirst().fitness() == 1.0) {
        System.out.println("Perfect solution found in generation " + gen + "!");
        break;
      }

      List<Individual> nextGen = new ArrayList<>();
      while (nextGen.size() < POP_SIZE) {
        Individual parentA = population.get(rand.nextInt(POP_SIZE / 2));
        Individual parentB = population.get(rand.nextInt(POP_SIZE / 2));
        Individual child = applyStep(parentA, new Crossover(parentB), rand);
        child = applyStep(child, new Mutation(0.05), rand);
        nextGen.add(child);
      }
      population = nextGen;
    }
  }

  // === Helpers ===

  /// Uses Pattern Matching (Java 21) to apply the evolution steps.
  private static Individual applyStep(Individual subject, EvolutionOp op, Random rand) {
    return switch (op) {
      case Mutation m -> {
        int[] genes = subject.genes().clone();
        for (int i = 0; i < genes.length; i++) {
          if (rand.nextDouble() < m.rate()) genes[i] = 1 - genes[i];
        }
        yield new Individual(genes, 0.0);
      }
      case Crossover c -> {
        int split = rand.nextInt(subject.genes().length);
        int[] newGenes = new int[subject.genes().length];
        System.arraycopy(subject.genes(), 0, newGenes, 0, split);
        System.arraycopy(c.partner().genes(), split, newGenes, split, newGenes.length - split);
        yield new Individual(newGenes, 0.0);
      }
    };
  }

  private static Individual evaluate(Individual ind) {
    double score = Arrays.stream(ind.genes()).sum();
    return new Individual(ind.genes(), score / ind.genes().length);
  }

  private static Individual createRandom(int len, Random r) {
    int[] g = new int[len];
    for (int i = 0; i < len; i++) g[i] = r.nextInt(2);
    return new Individual(g, 0.0);
  }
}
