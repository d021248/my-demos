package d021248.mytry.algorithms.visualization;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Locale;
import java.util.Random;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.WindowConstants;

/// Q-Learning Maze Solver
/// Watch a reinforcement learning agent learn to navigate a maze.

@SuppressWarnings({"this-escape", "serial"})
public final class QLearningVisualizer extends JFrame {
  private static final long serialVersionUID = 1L;
  private final DrawPanel drawPanel;
  private final ControlPanel controlPanel;

  private Timer timer;
  private final int gridSize = 20;
  private final int[][] maze = new int[gridSize][gridSize];
  private double[][] qTable;
  private int agentX, agentY;
  private int goalX, goalY;
  private int episode = 0;
  private int steps = 0;
  private double epsilon = 0.3; // Exploration rate
  private final double alpha = 0.1; // Learning rate
  private final double gamma = 0.9; // Discount factor
  private final Random rand = new Random();

  private static final int EMPTY = 0;
  private static final int WALL = 1;

  private static final int UP = 0;
  private static final int RIGHT = 1;
  private static final int DOWN = 2;
  private static final int LEFT = 3;

  public QLearningVisualizer() {
    super("Q-Learning Maze Solver");

    drawPanel = new DrawPanel();
    controlPanel = new ControlPanel();

    setLayout(new BorderLayout());
    add(drawPanel, BorderLayout.CENTER);
    add(controlPanel, BorderLayout.SOUTH);

    setSize(900, 950);
    setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    setLocationRelativeTo(null);

    generateMaze();
    initializeQLearning();

    timer =
        new Timer(
            20,
            e -> {
              stepQLearning();
              drawPanel.repaint();
            });

    setVisible(true);
  }

  private void generateMaze() {
    // Clear maze
    for (int i = 0; i < gridSize; i++) {
      for (int j = 0; j < gridSize; j++) {
        maze[i][j] = EMPTY;
      }
    }

    // Add random walls
    for (int i = 0; i < gridSize; i++) {
      for (int j = 0; j < gridSize; j++) {
        if (rand.nextDouble() < 0.2) {
          maze[i][j] = WALL;
        }
      }
    }

    // Set start and goal
    agentX = 1;
    agentY = 1;
    goalX = gridSize - 2;
    goalY = gridSize - 2;

    maze[agentY][agentX] = EMPTY;
    maze[goalY][goalX] = EMPTY;

    // Add border walls
    for (int i = 0; i < gridSize; i++) {
      maze[0][i] = WALL;
      maze[gridSize - 1][i] = WALL;
      maze[i][0] = WALL;
      maze[i][gridSize - 1] = WALL;
    }
  }

  private void initializeQLearning() {
    qTable = new double[gridSize * gridSize][4]; // State x Action
    episode = 0;
    steps = 0;
    epsilon = 0.3;
    resetAgent();
  }

  private void resetAgent() {
    agentX = 1;
    agentY = 1;
    steps = 0;
  }

  private void stepQLearning() {
    if (agentX == goalX && agentY == goalY) {
      episode++;
      epsilon = Math.max(0.01, epsilon * 0.995); // Decay exploration
      resetAgent();
      return;
    }

    if (steps > 500) {
      episode++;
      resetAgent();
      return;
    }

    int state = agentY * gridSize + agentX;

    // Choose action (epsilon-greedy)
    int action;
    if (rand.nextDouble() < epsilon) {
      action = rand.nextInt(4); // Explore
    } else {
      action = getBestAction(state); // Exploit
    }

    // Take action
    int newX = agentX;
    int newY = agentY;

    switch (action) {
      case UP -> newY--;
      case RIGHT -> newX++;
      case DOWN -> newY++;
      case LEFT -> newX--;
    }

    // Check if valid move
    if (newX < 0 || newX >= gridSize || newY < 0 || newY >= gridSize || maze[newY][newX] == WALL) {
      // Invalid move - negative reward
      int newState = state;
      double reward = -1;
      updateQValue(state, action, newState, reward);
      return;
    }

    // Valid move
    int newState = newY * gridSize + newX;
    double reward = (newX == goalX && newY == goalY) ? 100 : -0.1;

    updateQValue(state, action, newState, reward);

    agentX = newX;
    agentY = newY;
    steps++;
  }

  private void updateQValue(int state, int action, int newState, double reward) {
    double oldQ = qTable[state][action];
    double maxFutureQ = getMaxQ(newState);
    double newQ = oldQ + alpha * (reward + gamma * maxFutureQ - oldQ);
    qTable[state][action] = newQ;
  }

  private int getBestAction(int state) {
    int bestAction = 0;
    double bestQ = qTable[state][0];

    for (int a = 1; a < 4; a++) {
      if (qTable[state][a] > bestQ) {
        bestQ = qTable[state][a];
        bestAction = a;
      }
    }

    return bestAction;
  }

  private double getMaxQ(int state) {
    double maxQ = qTable[state][0];
    for (int a = 1; a < 4; a++) {
      maxQ = Math.max(maxQ, qTable[state][a]);
    }
    return maxQ;
  }

  class DrawPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    DrawPanel() {
      setBackground(new Color(20, 20, 30));
      setPreferredSize(new Dimension(850, 850));

      addMouseListener(
          new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
              int cellSize = Math.min(getWidth(), getHeight()) / gridSize;
              int x = e.getX() / cellSize;
              int y = e.getY() / cellSize;

              if (x >= 0 && x < gridSize && y >= 0 && y < gridSize) {
                if (maze[y][x] == WALL) {
                  maze[y][x] = EMPTY;
                } else if (maze[y][x] == EMPTY) {
                  maze[y][x] = WALL;
                }
                repaint();
              }
            }
          });
    }

    @Override
    protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      Graphics2D g2 = (Graphics2D) g;
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

      int cellSize = Math.min(getWidth(), getHeight()) / gridSize;

      // Draw maze and Q-values
      for (int i = 0; i < gridSize; i++) {
        for (int j = 0; j < gridSize; j++) {
          int x = j * cellSize;
          int y = i * cellSize;

          if (maze[i][j] == WALL) {
            g2.setColor(new Color(60, 60, 60));
            g2.fillRect(x, y, cellSize, cellSize);
          } else {
            // Draw Q-value heatmap
            int state = i * gridSize + j;
            double maxQ = getMaxQ(state);
            int brightness = (int) (Math.min(Math.max(maxQ, 0), 10) / 10 * 100);
            g2.setColor(new Color(0, brightness, brightness / 2));
            g2.fillRect(x, y, cellSize, cellSize);

            // Draw arrows for policy
            if (episode > 50) {
              int bestAction = getBestAction(state);
              g2.setColor(new Color(255, 255, 255, 100));
              drawArrow(g2, x + cellSize / 2, y + cellSize / 2, bestAction, cellSize / 4);
            }
          }

          g2.setColor(new Color(80, 80, 80));
          g2.drawRect(x, y, cellSize, cellSize);
        }
      }

      // Draw goal
      g2.setColor(Color.GREEN);
      g2.fillOval(goalX * cellSize + 5, goalY * cellSize + 5, cellSize - 10, cellSize - 10);

      // Draw agent
      g2.setColor(Color.YELLOW);
      g2.fillOval(agentX * cellSize + 3, agentY * cellSize + 3, cellSize - 6, cellSize - 6);
      g2.setColor(Color.ORANGE);
      g2.drawOval(agentX * cellSize + 3, agentY * cellSize + 3, cellSize - 6, cellSize - 6);

      // Draw info
      g2.setColor(Color.WHITE);
      g2.drawString(
          "Episode: "
              + episode
              + " | Steps: "
              + steps
              + " | Exploration: "
              + String.format(Locale.ROOT, "%.2f", epsilon),
          10,
          20);
    }

    private void drawArrow(Graphics2D g2, int x, int y, int direction, int length) {
      int dx = 0, dy = 0;
      switch (direction) {
        case UP -> dy = -length;
        case RIGHT -> dx = length;
        case DOWN -> dy = length;
        case LEFT -> dx = -length;
      }

      g2.drawLine(x, y, x + dx, y + dy);

      // Arrowhead
      double angle = Math.atan2(dy, dx);
      int arrowSize = 5;
      g2.drawLine(
          x + dx,
          y + dy,
          x + dx - (int) (arrowSize * Math.cos(angle - Math.PI / 6)),
          y + dy - (int) (arrowSize * Math.sin(angle - Math.PI / 6)));
      g2.drawLine(
          x + dx,
          y + dy,
          x + dx - (int) (arrowSize * Math.cos(angle + Math.PI / 6)),
          y + dy - (int) (arrowSize * Math.sin(angle + Math.PI / 6)));
    }
  }

  class ControlPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    ControlPanel() {
      setLayout(new FlowLayout());

      JButton startBtn = new JButton("Start Learning");
      JButton stopBtn = new JButton("Stop");
      JButton resetBtn = new JButton("Reset");
      JButton newMazeBtn = new JButton("New Maze");

      startBtn.addActionListener(
          e -> {
            timer.start();
            startBtn.setEnabled(false);
            stopBtn.setEnabled(true);
          });

      stopBtn.addActionListener(
          e -> {
            timer.stop();
            startBtn.setEnabled(true);
            stopBtn.setEnabled(false);
          });

      resetBtn.addActionListener(
          e -> {
            timer.stop();
            initializeQLearning();
            startBtn.setEnabled(true);
            stopBtn.setEnabled(false);
            drawPanel.repaint();
          });

      newMazeBtn.addActionListener(
          e -> {
            timer.stop();
            generateMaze();
            initializeQLearning();
            startBtn.setEnabled(true);
            stopBtn.setEnabled(false);
            drawPanel.repaint();
          });

      stopBtn.setEnabled(false);

      add(startBtn);
      add(stopBtn);
      add(resetBtn);
      add(newMazeBtn);
      add(new JLabel("  (Click to toggle walls)"));
    }
  }

  public static void main() {
    SwingUtilities.invokeLater(QLearningVisualizer::new);
  }
}
