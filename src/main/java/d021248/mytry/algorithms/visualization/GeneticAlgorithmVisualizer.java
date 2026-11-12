package d021248.mytry.algorithms.visualization;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.WindowConstants;

/// Genetic Algorithm Visualizer
/// Watch evolution in action as solutions evolve to solve optimization problems.

@SuppressWarnings({"this-escape", "serial"})
public final class GeneticAlgorithmVisualizer extends JFrame {
  private static final long serialVersionUID = 1L;
  private final EvolutionPanel evolutionPanel;
  private final ControlPanel controlPanel;

  private Timer timer;
  private int generation = 0;
  private final int populationSize = 100;
  private final double mutationRate = 0.01;
  private final int geneLength = 100;

  private List<Individual> population = new ArrayList<>();
  private Individual bestEver;

  private static class Individual implements Comparable<Individual> {
    double[] genes;
    double fitness;

    Individual(int length) {
      genes = new double[length];
      for (int i = 0; i < length; i++) {
        genes[i] = Math.random();
      }
      calculateFitness();
    }

    Individual(double[] genes) {
      this.genes = genes.clone();
      calculateFitness();
    }

    void calculateFitness() {
      // Fitness function: maximize sum of genes
      fitness = 0;
      for (double gene : genes) {
        fitness += gene;
      }
    }

    void mutate(double rate) {
      for (int i = 0; i < genes.length; i++) {
        if (Math.random() < rate) {
          genes[i] = Math.random();
        }
      }
      calculateFitness();
    }

    @Override
    public int compareTo(Individual other) {
      return Double.compare(other.fitness, this.fitness);
    }
  }

  public GeneticAlgorithmVisualizer() {
    super("Genetic Algorithm Visualizer - Evolution in Action");

    initializePopulation();

    evolutionPanel = new EvolutionPanel();
    controlPanel = new ControlPanel();

    setLayout(new BorderLayout());
    add(evolutionPanel, BorderLayout.CENTER);
    add(controlPanel, BorderLayout.SOUTH);

    setSize(1000, 700);
    setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    setLocationRelativeTo(null);

    setVisible(true);
  }

  private void initializePopulation() {
    population.clear();
    for (int i = 0; i < populationSize; i++) {
      population.add(new Individual(geneLength));
    }
    generation = 0;
    bestEver = null;
  }

  private void evolve() {
    Collections.sort(population);

    // Track best
    if (bestEver == null || population.get(0).fitness > bestEver.fitness) {
      bestEver = new Individual(population.get(0).genes);
    }

    List<Individual> newPopulation = new ArrayList<>();

    // Elitism - keep top 10%
    int eliteCount = populationSize / 10;
    for (int i = 0; i < eliteCount; i++) {
      newPopulation.add(new Individual(population.get(i).genes));
    }

    // Create rest through crossover and mutation
    while (newPopulation.size() < populationSize) {
      Individual parent1 = selectParent();
      Individual parent2 = selectParent();
      Individual child = crossover(parent1, parent2);
      child.mutate(mutationRate);
      newPopulation.add(child);
    }

    population = newPopulation;
    generation++;
  }

  private Individual selectParent() {
    // Tournament selection
    int tournamentSize = 5;
    Individual best = population.get((int) (Math.random() * population.size()));

    for (int i = 1; i < tournamentSize; i++) {
      Individual competitor = population.get((int) (Math.random() * population.size()));
      if (competitor.fitness > best.fitness) {
        best = competitor;
      }
    }
    return best;
  }

  private Individual crossover(Individual parent1, Individual parent2) {
    double[] childGenes = new double[geneLength];
    int crossoverPoint = (int) (Math.random() * geneLength);

    for (int i = 0; i < geneLength; i++) {
      childGenes[i] = i < crossoverPoint ? parent1.genes[i] : parent2.genes[i];
    }

    return new Individual(childGenes);
  }

  private void startEvolution() {
    if (timer != null) {
      timer.stop();
    }

    timer =
        new Timer(
            50,
            e -> {
              evolve();
              evolutionPanel.repaint();
            });
    timer.start();
  }

  private void stopEvolution() {
    if (timer != null) {
      timer.stop();
    }
  }

  class EvolutionPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    EvolutionPanel() {
      setBackground(Color.BLACK);
      setPreferredSize(new Dimension(1000, 600));
    }

    @Override
    protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      Graphics2D g2 = (Graphics2D) g;
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

      if (population.isEmpty()) return;

      int panelHeight = getHeight();
      int panelWidth = getWidth();

      // Draw population fitness distribution
      int barWidth = panelWidth / populationSize;
      double maxFitness = population.get(0).fitness;

      for (int i = 0; i < population.size(); i++) {
        Individual ind = population.get(i);
        int barHeight = (int) ((ind.fitness / geneLength) * (panelHeight - 200));

        float hue = (float) (ind.fitness / geneLength);
        g2.setColor(Color.getHSBColor(hue * 0.3f, 0.8f, 0.9f));
        g2.fillRect(i * barWidth, panelHeight - 200 - barHeight, barWidth - 1, barHeight);
      }

      // Draw statistics
      g2.setColor(Color.WHITE);
      g2.setFont(new Font("Monospaced", Font.BOLD, 16));
      g2.drawString("Generation: " + generation, 20, 30);
      g2.drawString(
          String.format(Locale.ROOT, "Best Fitness: %.2f / %.0f", maxFitness, (double) geneLength),
          20,
          55);
      g2.drawString(
          String.format(Locale.ROOT, "Average Fitness: %.2f", getAverageFitness()), 20, 80);
      if (bestEver != null) {
        g2.drawString(String.format(Locale.ROOT, "Best Ever: %.2f", bestEver.fitness), 20, 105);
      }

      // Draw best individual's genes
      g2.drawString("Best Individual Genes:", 20, panelHeight - 170);
      if (!population.isEmpty()) {
        Individual best = population.get(0);
        int geneBarWidth = Math.max(1, (panelWidth - 40) / geneLength);
        for (int i = 0; i < geneLength; i++) {
          int geneHeight = (int) (best.genes[i] * 100);
          float hue = (float) best.genes[i];
          g2.setColor(Color.getHSBColor(hue * 0.3f, 0.9f, 0.9f));
          g2.fillRect(
              20 + i * geneBarWidth, panelHeight - 150 - geneHeight, geneBarWidth - 1, geneHeight);
        }
      }
    }

    private double getAverageFitness() {
      return population.stream().mapToDouble(i -> i.fitness).average().orElse(0);
    }
  }

  class ControlPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    ControlPanel() {
      setLayout(new FlowLayout());

      JButton startBtn = new JButton("▶ Start Evolution");
      JButton stopBtn = new JButton("⏹ Stop");
      JButton resetBtn = new JButton("🔄 Reset");
      JButton stepBtn = new JButton("➡ Step");

      startBtn.addActionListener(e -> startEvolution());
      stopBtn.addActionListener(e -> stopEvolution());
      resetBtn.addActionListener(
          e -> {
            stopEvolution();
            initializePopulation();
            evolutionPanel.repaint();
          });
      stepBtn.addActionListener(
          e -> {
            evolve();
            evolutionPanel.repaint();
          });

      add(startBtn);
      add(stopBtn);
      add(stepBtn);
      add(resetBtn);
    }
  }

  public static void main() {
    SwingUtilities.invokeLater(GeneticAlgorithmVisualizer::new);
  }
}
