package d021248.mytry.algorithms.visualization;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.WindowConstants;

/// Neural Network Visualizer
/// Watch a simple neural network learn XOR and other patterns.

@SuppressWarnings({"this-escape", "serial"})
public final class NeuralNetworkVisualizer extends JFrame {
  private static final long serialVersionUID = 1L;
  private final DrawPanel drawPanel;
  private final ControlPanel controlPanel;

  private Problem currentProblem = Problem.XOR;
  private Timer timer;
  private NeuralNetwork network;
  private int epoch = 0;
  private double error = 1.0;
  private final List<Double> errorHistory = new ArrayList<>();
  private int mouseX = -1;
  private int mouseY = -1;
  private final List<TrainingExample> customTrainingData = new ArrayList<>();

  private enum Problem {
    XOR("XOR Gate"),
    AND("AND Gate"),
    OR("OR Gate"),
    CIRCLE("Circle Classification");

    final String displayName;

    Problem(String displayName) {
      this.displayName = displayName;
    }
  }

  record TrainingExample(double[] inputs, double[] outputs) {}

  class NeuralNetwork {
    private final int inputSize;
    private final int hiddenSize;
    private final int outputSize;
    private double[][] weightsIH; // input to hidden
    private double[][] weightsHO; // hidden to output
    private double[] biasH;
    private double[] biasO;
    private double learningRate = 0.5;

    // For visualization
    private double[] lastInput;
    private double[] lastHidden;
    private double[] lastOutput;

    NeuralNetwork(int inputSize, int hiddenSize, int outputSize) {
      this.inputSize = inputSize;
      this.hiddenSize = hiddenSize;
      this.outputSize = outputSize;

      weightsIH = new double[inputSize][hiddenSize];
      weightsHO = new double[hiddenSize][outputSize];
      biasH = new double[hiddenSize];
      biasO = new double[outputSize];

      randomizeWeights();
    }

    void randomizeWeights() {
      Random rand = new Random();
      for (int i = 0; i < inputSize; i++) {
        for (int j = 0; j < hiddenSize; j++) {
          weightsIH[i][j] = rand.nextGaussian() * 0.5;
        }
      }
      for (int i = 0; i < hiddenSize; i++) {
        for (int j = 0; j < outputSize; j++) {
          weightsHO[i][j] = rand.nextGaussian() * 0.5;
        }
        biasH[i] = rand.nextGaussian() * 0.5;
      }
      for (int i = 0; i < outputSize; i++) {
        biasO[i] = rand.nextGaussian() * 0.5;
      }
    }

    double[] forward(double[] inputs) {
      lastInput = inputs.clone();

      // Hidden layer
      lastHidden = new double[hiddenSize];
      for (int i = 0; i < hiddenSize; i++) {
        double sum = biasH[i];
        for (int j = 0; j < inputSize; j++) {
          sum += inputs[j] * weightsIH[j][i];
        }
        lastHidden[i] = sigmoid(sum);
      }

      // Output layer
      lastOutput = new double[outputSize];
      for (int i = 0; i < outputSize; i++) {
        double sum = biasO[i];
        for (int j = 0; j < hiddenSize; j++) {
          sum += lastHidden[j] * weightsHO[j][i];
        }
        lastOutput[i] = sigmoid(sum);
      }

      return lastOutput;
    }

    void train(TrainingExample[] examples) {
      for (TrainingExample ex : examples) {
        // Forward pass
        double[] output = forward(ex.inputs);

        // Backpropagation
        // Output layer error
        double[] outputError = new double[outputSize];
        for (int i = 0; i < outputSize; i++) {
          outputError[i] = (ex.outputs[i] - output[i]) * sigmoidDerivative(output[i]);
        }

        // Hidden layer error
        double[] hiddenError = new double[hiddenSize];
        for (int i = 0; i < hiddenSize; i++) {
          double error = 0;
          for (int j = 0; j < outputSize; j++) {
            error += outputError[j] * weightsHO[i][j];
          }
          hiddenError[i] = error * sigmoidDerivative(lastHidden[i]);
        }

        // Update weights and biases
        for (int i = 0; i < hiddenSize; i++) {
          for (int j = 0; j < outputSize; j++) {
            weightsHO[i][j] += learningRate * outputError[j] * lastHidden[i];
          }
        }
        for (int i = 0; i < outputSize; i++) {
          biasO[i] += learningRate * outputError[i];
        }

        for (int i = 0; i < inputSize; i++) {
          for (int j = 0; j < hiddenSize; j++) {
            weightsIH[i][j] += learningRate * hiddenError[j] * ex.inputs[i];
          }
        }
        for (int i = 0; i < hiddenSize; i++) {
          biasH[i] += learningRate * hiddenError[i];
        }
      }
    }

    private double sigmoid(double x) {
      return 1.0 / (1.0 + Math.exp(-x));
    }

    private double sigmoidDerivative(double y) {
      return y * (1.0 - y);
    }
  }

  public NeuralNetworkVisualizer() {
    super("Neural Network Visualizer");

    drawPanel = new DrawPanel();
    controlPanel = new ControlPanel();

    setLayout(new BorderLayout());
    add(drawPanel, BorderLayout.CENTER);
    add(controlPanel, BorderLayout.SOUTH);

    setSize(1000, 900);
    setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    setLocationRelativeTo(null);

    reset();

    timer =
        new Timer(
            50,
            e -> {
              TrainingExample[] examples = getTrainingData();
              network.train(examples);

              // Calculate error
              error = 0;
              for (TrainingExample ex : examples) {
                double[] output = network.forward(ex.inputs);
                for (int i = 0; i < output.length; i++) {
                  error += Math.pow(ex.outputs[i] - output[i], 2);
                }
              }
              error /= examples.length;
              errorHistory.add(error);
              if (errorHistory.size() > 200) errorHistory.remove(0);

              epoch++;
              drawPanel.repaint();
            });

    setVisible(true);
  }

  private void reset() {
    network = new NeuralNetwork(2, 4, 1);
    epoch = 0;
    error = 1.0;
    errorHistory.clear();
    customTrainingData.clear();
  }

  private TrainingExample[] getTrainingData() {
    List<TrainingExample> allExamples = new ArrayList<>();

    // Add base examples
    TrainingExample[] baseExamples =
        switch (currentProblem) {
          case XOR ->
              new TrainingExample[] {
                new TrainingExample(new double[] {0, 0}, new double[] {0}),
                new TrainingExample(new double[] {0, 1}, new double[] {1}),
                new TrainingExample(new double[] {1, 0}, new double[] {1}),
                new TrainingExample(new double[] {1, 1}, new double[] {0})
              };
          case AND ->
              new TrainingExample[] {
                new TrainingExample(new double[] {0, 0}, new double[] {0}),
                new TrainingExample(new double[] {0, 1}, new double[] {0}),
                new TrainingExample(new double[] {1, 0}, new double[] {0}),
                new TrainingExample(new double[] {1, 1}, new double[] {1})
              };
          case OR ->
              new TrainingExample[] {
                new TrainingExample(new double[] {0, 0}, new double[] {0}),
                new TrainingExample(new double[] {0, 1}, new double[] {1}),
                new TrainingExample(new double[] {1, 0}, new double[] {1}),
                new TrainingExample(new double[] {1, 1}, new double[] {1})
              };
          case CIRCLE -> {
            List<TrainingExample> examples = new ArrayList<>();
            Random rand = new Random(42);
            for (int i = 0; i < 100; i++) {
              double x = rand.nextDouble();
              double y = rand.nextDouble();
              double dist = Math.sqrt(Math.pow(x - 0.5, 2) + Math.pow(y - 0.5, 2));
              double label = dist < 0.3 ? 1.0 : 0.0;
              examples.add(new TrainingExample(new double[] {x, y}, new double[] {label}));
            }
            yield examples.toArray(new TrainingExample[0]);
          }
        };

    allExamples.addAll(Arrays.asList(baseExamples));
    allExamples.addAll(customTrainingData);

    return allExamples.toArray(new TrainingExample[0]);
  }

  class DrawPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    DrawPanel() {
      setBackground(new Color(20, 20, 30));
      setPreferredSize(new Dimension(950, 800));

      // Mouse motion for live testing
      addMouseMotionListener(
          new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
              mouseX = e.getX();
              mouseY = e.getY();
              repaint();
            }
          });

      // Mouse click to add custom training data
      addMouseListener(
          new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
              int startX = 550;
              int size = 400;

              if (e.getX() >= startX
                  && e.getX() <= startX + size
                  && e.getY() >= 100
                  && e.getY() <= 100 + size) {

                double x = (double) (e.getX() - startX) / size;
                double y = (double) (e.getY() - 100) / size;

                // Left click = class 1, Right click = class 0
                double label = (e.getButton() == MouseEvent.BUTTON1) ? 1.0 : 0.0;
                customTrainingData.add(
                    new TrainingExample(new double[] {x, y}, new double[] {label}));
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

      // Draw network architecture
      drawNetwork(g2);

      // Draw decision boundary
      drawDecisionBoundary(g2);

      // Draw error graph
      drawErrorGraph(g2);

      // Draw info
      g2.setColor(Color.WHITE);
      g2.drawString(
          "Epoch: " + epoch + " | Error: " + String.format(Locale.ROOT, "%.4f", error), 10, 20);

      // Show mouse hover prediction
      if (mouseX >= 550 && mouseX <= 950 && mouseY >= 100 && mouseY <= 500) {
        double x = (double) (mouseX - 550) / 400;
        double y = (double) (mouseY - 100) / 400;
        double[] output = network.forward(new double[] {x, y});
        g2.drawString(
            String.format(Locale.ROOT, "Hover: (%.2f, %.2f) → %.3f", x, y, output[0]), 10, 40);
      }

      if (!customTrainingData.isEmpty()) {
        g2.drawString(
            "Custom points: " + customTrainingData.size() + " (Left-click=1, Right-click=0)",
            10,
            760);
      }
    }

    private void drawNetwork(Graphics2D g2) {
      int startX = 50;
      int startY = 100;
      int layerSpacing = 200;
      int neuronRadius = 20;

      // Node positions
      int[][] positions = new int[3][];
      positions[0] = new int[] {startY + 100, startY + 200}; // Input layer
      positions[1] =
          new int[] {startY + 50, startY + 120, startY + 190, startY + 260}; // Hidden layer
      positions[2] = new int[] {startY + 150}; // Output layer

      // Draw connections with weights
      g2.setStroke(new BasicStroke(1));

      // Input to Hidden
      for (int i = 0; i < 2; i++) {
        for (int j = 0; j < 4; j++) {
          double weight = network.weightsIH[i][j];
          int alpha = (int) (Math.min(Math.abs(weight) * 255, 255));
          g2.setColor(
              weight > 0 ? new Color(100, 200, 255, alpha) : new Color(255, 100, 100, alpha));
          g2.drawLine(startX, positions[0][i], startX + layerSpacing, positions[1][j]);
        }
      }

      // Hidden to Output
      for (int i = 0; i < 4; i++) {
        double weight = network.weightsHO[i][0];
        int alpha = (int) (Math.min(Math.abs(weight) * 255, 255));
        g2.setColor(weight > 0 ? new Color(100, 200, 255, alpha) : new Color(255, 100, 100, alpha));
        g2.drawLine(
            startX + layerSpacing, positions[1][i], startX + 2 * layerSpacing, positions[2][0]);
      }

      // Draw neurons
      // Input layer
      for (int i = 0; i < 2; i++) {
        double activation = network.lastInput != null ? network.lastInput[i] : 0;
        drawNeuron(g2, startX, positions[0][i], neuronRadius, activation);
        g2.setColor(Color.LIGHT_GRAY);
        g2.drawString("x" + (i + 1), startX - 30, positions[0][i] + 5);
      }

      // Hidden layer
      for (int i = 0; i < 4; i++) {
        double activation = network.lastHidden != null ? network.lastHidden[i] : 0;
        drawNeuron(g2, startX + layerSpacing, positions[1][i], neuronRadius, activation);
      }

      // Output layer
      double activation = network.lastOutput != null ? network.lastOutput[0] : 0;
      drawNeuron(g2, startX + 2 * layerSpacing, positions[2][0], neuronRadius, activation);
      g2.setColor(Color.LIGHT_GRAY);
      g2.drawString("output", startX + 2 * layerSpacing + 30, positions[2][0] + 5);
    }

    private void drawNeuron(Graphics2D g2, int x, int y, int radius, double activation) {
      int brightness = (int) (activation * 255);
      g2.setColor(new Color(brightness, brightness, brightness));
      g2.fillOval(x - radius, y - radius, radius * 2, radius * 2);
      g2.setColor(Color.WHITE);
      g2.drawOval(x - radius, y - radius, radius * 2, radius * 2);
    }

    private void drawDecisionBoundary(Graphics2D g2) {
      int startX = 550;
      int startY = 100;
      int size = 400;
      int resolution = 40;

      g2.setColor(Color.GRAY);
      g2.drawString("Decision Boundary", startX + size / 2 - 50, startY - 10);

      for (int i = 0; i < resolution; i++) {
        for (int j = 0; j < resolution; j++) {
          double x = (double) i / resolution;
          double y = (double) j / resolution;
          double[] output = network.forward(new double[] {x, y});

          int brightness = (int) (output[0] * 255);
          g2.setColor(new Color(brightness, brightness / 2, 255 - brightness, 100));
          g2.fillRect(
              startX + i * size / resolution,
              startY + j * size / resolution,
              size / resolution + 1,
              size / resolution + 1);
        }
      }

      // Draw training points
      TrainingExample[] examples = getTrainingData();
      for (TrainingExample ex : examples) {
        int x = startX + (int) (ex.inputs[0] * size);
        int y = startY + (int) (ex.inputs[1] * size);
        g2.setColor(ex.outputs[0] > 0.5 ? Color.CYAN : Color.ORANGE);
        g2.fillOval(x - 5, y - 5, 10, 10);
        g2.setColor(Color.WHITE);
        g2.drawOval(x - 5, y - 5, 10, 10);
      }

      // Draw custom training points
      for (TrainingExample ex : customTrainingData) {
        int x = startX + (int) (ex.inputs[0] * size);
        int y = startY + (int) (ex.inputs[1] * size);
        g2.setColor(ex.outputs[0] > 0.5 ? Color.CYAN : Color.ORANGE);
        g2.fillRect(x - 6, y - 6, 12, 12);
        g2.setColor(Color.YELLOW);
        g2.drawRect(x - 6, y - 6, 12, 12);
      }

      // Draw mouse hover indicator
      if (mouseX >= startX
          && mouseX <= startX + size
          && mouseY >= startY
          && mouseY <= startY + size) {
        g2.setColor(Color.WHITE);
        g2.drawOval(mouseX - 8, mouseY - 8, 16, 16);
      }

      g2.setColor(Color.WHITE);
      g2.drawRect(startX, startY, size, size);
    }

    private void drawErrorGraph(Graphics2D g2) {
      if (errorHistory.isEmpty()) return;

      int startX = 50;
      int startY = 550;
      int width = 900;
      int height = 200;

      g2.setColor(Color.GRAY);
      g2.drawString("Training Error", startX, startY - 10);
      g2.drawRect(startX, startY, width, height);

      g2.setColor(Color.CYAN);
      g2.setStroke(new BasicStroke(2));

      for (int i = 1; i < errorHistory.size(); i++) {
        int x1 = startX + (i - 1) * width / 200;
        int y1 = startY + height - (int) (errorHistory.get(i - 1) * height);
        int x2 = startX + i * width / 200;
        int y2 = startY + height - (int) (errorHistory.get(i) * height);
        g2.drawLine(x1, y1, x2, y2);
      }
    }
  }

  class ControlPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    ControlPanel() {
      setLayout(new FlowLayout());

      JComboBox<Problem> problemCombo = new JComboBox<>(Problem.values());
      problemCombo.setRenderer(
          new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
              super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
              if (value instanceof Problem p) {
                setText(p.displayName);
              }
              return this;
            }
          });

      JButton startBtn = new JButton("Start Training");
      JButton stopBtn = new JButton("Stop");
      JButton stepBtn = new JButton("Step");
      JButton resetBtn = new JButton("Reset Network");
      JButton clearCustomBtn = new JButton("Clear Custom Points");

      JSlider learningRateSlider = new JSlider(1, 100, 50);
      learningRateSlider.setPreferredSize(new Dimension(150, 40));

      problemCombo.addActionListener(
          e -> {
            currentProblem = (Problem) problemCombo.getSelectedItem();
            reset();
            drawPanel.repaint();
          });

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

      stepBtn.addActionListener(
          e -> {
            TrainingExample[] examples = getTrainingData();
            network.train(examples);

            error = 0;
            for (TrainingExample ex : examples) {
              double[] output = network.forward(ex.inputs);
              for (int i = 0; i < output.length; i++) {
                error += Math.pow(ex.outputs[i] - output[i], 2);
              }
            }
            error /= examples.length;
            errorHistory.add(error);
            if (errorHistory.size() > 200) errorHistory.remove(0);

            epoch++;
            drawPanel.repaint();
          });

      resetBtn.addActionListener(
          e -> {
            timer.stop();
            reset();
            startBtn.setEnabled(true);
            stopBtn.setEnabled(false);
            drawPanel.repaint();
          });

      clearCustomBtn.addActionListener(
          e -> {
            customTrainingData.clear();
            drawPanel.repaint();
          });

      learningRateSlider.addChangeListener(
          e -> {
            network.learningRate = learningRateSlider.getValue() / 100.0;
          });

      stopBtn.setEnabled(false);

      add(new JLabel("Problem: "));
      add(problemCombo);
      add(startBtn);
      add(stopBtn);
      add(stepBtn);
      add(resetBtn);
      add(clearCustomBtn);
      add(new JLabel(" | LR: "));
      add(learningRateSlider);
    }
  }

  public static void main() {
    SwingUtilities.invokeLater(NeuralNetworkVisualizer::new);
  }
}
