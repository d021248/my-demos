package d021248.mytry.algorithms.visualization;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

/// Algorithm Visualization Launcher
/// Main menu to launch all visualization demos.

@SuppressWarnings({"this-escape", "serial"})
public final class VisualizationLauncher extends JFrame {
  private static final long serialVersionUID = 1L;

  public VisualizationLauncher() {
    super("Algorithm Visualization Suite");

    setLayout(new BorderLayout());

    JPanel titlePanel = new JPanel();
    JLabel titleLabel = new JLabel("🎨 Algorithm Visualization Suite");
    titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
    titlePanel.add(titleLabel);

    JPanel buttonsPanel = new JPanel();
    buttonsPanel.setLayout(new GridLayout(0, 1, 10, 10));
    buttonsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    // Core Algorithm Visualizations
    buttonsPanel.add(
        createLauncherButton(
            "🔍 Pathfinding Visualizer",
            "Watch A*, Dijkstra, BFS, and DFS find paths through mazes",
            PathfindingVisualizer::new));

    buttonsPanel.add(
        createLauncherButton(
            "🏁 Sorting Algorithm Race",
            "See Bubble Sort, Quick Sort, and Merge Sort compete side-by-side",
            SortingRaceVisualizer::new));

    buttonsPanel.add(
        createLauncherButton(
            "🌳 Recursive Tree Animations",
            "Beautiful fractal trees, Sierpinski triangles, and Koch snowflakes",
            RecursiveTreeVisualizer::new));

    // Physics & Simulations
    buttonsPanel.add(
        createLauncherButton(
            "🌊 Wave Physics Simulator",
            "Ripple effects, gravity simulations, double pendulums, and springs",
            WavePhysicsVisualizer::new));

    buttonsPanel.add(
        createLauncherButton(
            "✨ Particle Systems",
            "Fireworks, flocking behavior (Boids), gravity wells, and snowfall",
            ParticleSystemsVisualizer::new));

    // Advanced Algorithms
    buttonsPanel.add(
        createLauncherButton(
            "🧬 Genetic Algorithm Evolution",
            "Watch populations evolve through selection, crossover, and mutation",
            GeneticAlgorithmVisualizer::new));

    buttonsPanel.add(
        createLauncherButton(
            "🕸️ Graph Algorithms",
            "Minimum Spanning Trees (Prim's & Kruskal's) and shortest paths",
            GraphAlgorithmsVisualizer::new));

    buttonsPanel.add(
        createLauncherButton(
            "🧱 Maze Generation",
            "Step-by-step maze creation with recursive backtracking and Prim's",
            MazeGenerationVisualizer::new));

    // Mathematical Patterns
    buttonsPanel.add(
        createLauncherButton(
            "🔢 Number Theory",
            "Ulam Spiral, Collatz Conjecture, Fibonacci Spiral, Prime Sieves",
            NumberTheoryVisualizer::new));

    buttonsPanel.add(
        createLauncherButton(
            "🦠 Cellular Automata",
            "Conway's Life, Brian's Brain, Langton's Ant, Wireworld",
            CellularAutomataVisualizer::new));

    // AI & Machine Learning
    buttonsPanel.add(
        createLauncherButton(
            "🧠 Neural Network Trainer",
            "Watch a neural network learn XOR, AND, OR gates, and circle classification",
            NeuralNetworkVisualizer::new));

    buttonsPanel.add(
        createLauncherButton(
            "🎯 K-Means Clustering",
            "See data points organize into clusters with beautiful Voronoi regions",
            KMeansVisualizer::new));

    buttonsPanel.add(
        createLauncherButton(
            "🌳 Decision Tree Builder",
            "Build and visualize decision trees for classification problems",
            DecisionTreeVisualizer::new));

    buttonsPanel.add(
        createLauncherButton(
            "🎮 Q-Learning Maze Solver",
            "Reinforcement learning agent learns to navigate mazes",
            QLearningVisualizer::new));

    // Compression
    buttonsPanel.add(
        createLauncherButton(
            "🗜 LZ77 Compression",
            "Sliding window: search buffer, lookahead buffer, match arcs, emitted token stream",
            LZ77Visualizer::new));

    buttonsPanel.add(
        createLauncherButton(
            "📚 LZW Compression",
            "Growing dictionary: watch substrings get added and reused as compact codes",
            LZWVisualizer::new));

    JScrollPane scrollPane = new JScrollPane(buttonsPanel);
    scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

    add(titlePanel, BorderLayout.NORTH);
    add(scrollPane, BorderLayout.CENTER);

    setSize(700, 800);
    setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    setLocationRelativeTo(null);

    setVisible(true);
  }

  private JButton createLauncherButton(String title, String description, Runnable action) {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(BorderFactory.createEtchedBorder());

    JLabel titleLabel = new JLabel(title);
    titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
    titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));

    JLabel descLabel = new JLabel(description);
    descLabel.setFont(new Font("Arial", Font.PLAIN, 12));
    descLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

    JButton launchBtn = new JButton("Launch");
    launchBtn.setFont(new Font("Arial", Font.BOLD, 14));
    launchBtn.addActionListener(e -> SwingUtilities.invokeLater(action));

    JPanel textPanel = new JPanel(new GridLayout(2, 1));
    textPanel.add(titleLabel);
    textPanel.add(descLabel);

    panel.add(textPanel, BorderLayout.CENTER);
    panel.add(launchBtn, BorderLayout.EAST);

    JButton button = new JButton();
    button.setLayout(new BorderLayout());
    button.add(panel);
    button.setBorderPainted(false);
    button.setContentAreaFilled(false);
    button.addActionListener(e -> SwingUtilities.invokeLater(action));

    return button;
  }

  public static void main() {
    SwingUtilities.invokeLater(VisualizationLauncher::new);
  }
}
