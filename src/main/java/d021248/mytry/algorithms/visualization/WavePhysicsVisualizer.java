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
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.WindowConstants;

/// Wave and Physics Simulations
/// Interactive physics demonstrations including ripples, gravity, pendulums, and
/// springs.

@SuppressWarnings({"this-escape", "serial"})
public final class WavePhysicsVisualizer extends JFrame {
  private static final long serialVersionUID = 1L;
  private final DrawPanel drawPanel;
  private final ControlPanel controlPanel;

  private SimulationType currentType = SimulationType.RIPPLE;
  private Timer timer;

  private enum SimulationType {
    RIPPLE("Ripple Effect"),
    GRAVITY("Gravity Simulator"),
    DOUBLE_PENDULUM("Double Pendulum"),
    SPRINGS("Spring Physics");

    final String displayName;

    SimulationType(String displayName) {
      this.displayName = displayName;
    }
  }

  // Ripple simulation
  private final List<Ripple> ripples = new ArrayList<>();

  // Gravity simulation
  private final List<Particle> particles = new ArrayList<>();

  // Pendulum simulation
  private DoublePendulum pendulum;

  // Spring simulation
  private final List<SpringBall> springBalls = new ArrayList<>();

  private static class Ripple {
    double x;
    double y;
    double radius;
    double maxRadius;
    double speed;

    Ripple(double x, double y) {
      this.x = x;
      this.y = y;
      this.radius = 0;
      this.maxRadius = 300;
      this.speed = 3;
    }

    void update() {
      radius += speed;
    }

    boolean isExpired() {
      return radius > maxRadius;
    }

    double getIntensity() {
      return 1 - (radius / maxRadius);
    }
  }

  private static class Particle {
    double x;
    double y;
    double vx;
    double vy;
    double radius;
    Color color;

    Particle(double x, double y) {
      this.x = x;
      this.y = y;
      this.vx = (Math.random() - 0.5) * 4;
      this.vy = 0;
      this.radius = 5 + Math.random() * 10;
      this.color =
          new Color(
              (int) (Math.random() * 255),
              (int) (Math.random() * 255),
              (int) (Math.random() * 255));
    }

    void update(int width, int height) {
      vy += 0.5; // gravity
      x += vx;
      y += vy;

      // Bounce off walls
      if (x - radius < 0 || x + radius > width) {
        vx *= -0.8;
        x = Math.max(radius, Math.min(width - radius, x));
      }

      // Bounce off floor
      if (y + radius > height) {
        vy *= -0.8;
        y = height - radius;
      }
    }
  }

  private static class DoublePendulum {
    double angle1;
    double angle2;
    double angularVel1;
    double angularVel2;
    double length1 = 150;
    double length2 = 150;
    double mass1 = 20;
    double mass2 = 20;
    double gravity = 1;

    final List<Point2D.Double> trail = new ArrayList<>();

    DoublePendulum() {
      angle1 = Math.PI / 2;
      angle2 = Math.PI / 2;
      angularVel1 = 0;
      angularVel2 = 0;
    }

    void update() {
      double num1 = -gravity * (2 * mass1 + mass2) * Math.sin(angle1);
      double num2 = -mass2 * gravity * Math.sin(angle1 - 2 * angle2);
      double num3 = -2 * Math.sin(angle1 - angle2) * mass2;
      double num4 =
          angularVel2 * angularVel2 * length2
              + angularVel1 * angularVel1 * length1 * Math.cos(angle1 - angle2);
      double den = length1 * (2 * mass1 + mass2 - mass2 * Math.cos(2 * angle1 - 2 * angle2));
      double angularAcc1 = (num1 + num2 + num3 * num4) / den;

      num1 = 2 * Math.sin(angle1 - angle2);
      num2 = angularVel1 * angularVel1 * length1 * (mass1 + mass2);
      num3 = gravity * (mass1 + mass2) * Math.cos(angle1);
      num4 = angularVel2 * angularVel2 * length2 * mass2 * Math.cos(angle1 - angle2);
      den = length2 * (2 * mass1 + mass2 - mass2 * Math.cos(2 * angle1 - 2 * angle2));
      double angularAcc2 = (num1 * (num2 + num3 + num4)) / den;

      angularVel1 += angularAcc1;
      angularVel2 += angularAcc2;
      angle1 += angularVel1;
      angle2 += angularVel2;

      // Damping
      angularVel1 *= 0.999;
      angularVel2 *= 0.999;
    }

    Point2D.Double getSecondBobPosition(int centerX, int centerY) {
      double x1 = centerX + length1 * Math.sin(angle1);
      double y1 = centerY + length1 * Math.cos(angle1);
      double x2 = x1 + length2 * Math.sin(angle2);
      double y2 = y1 + length2 * Math.cos(angle2);
      return new Point2D.Double(x2, y2);
    }
  }

  private static class SpringBall {
    double x;
    double y;
    double vx;
    double vy;
    double anchorX;
    double anchorY;
    double springK = 0.05;
    double damping = 0.95;
    Color color;

    SpringBall(double anchorX, double anchorY) {
      this.anchorX = anchorX;
      this.anchorY = anchorY;
      this.x = anchorX;
      this.y = anchorY + 100;
      this.vx = 0;
      this.vy = 0;
      this.color =
          new Color(
              (int) (100 + Math.random() * 155),
              (int) (100 + Math.random() * 155),
              (int) (100 + Math.random() * 155));
    }

    void update() {
      double dx = anchorX - x;
      double dy = anchorY - y;

      vx += dx * springK;
      vy += dy * springK;
      vy += 0.5; // gravity

      vx *= damping;
      vy *= damping;

      x += vx;
      y += vy;
    }
  }

  public WavePhysicsVisualizer() {
    super("Wave & Physics Simulations");

    drawPanel = new DrawPanel();
    controlPanel = new ControlPanel();

    setLayout(new BorderLayout());
    add(drawPanel, BorderLayout.CENTER);
    add(controlPanel, BorderLayout.SOUTH);

    setSize(900, 750);
    setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    setLocationRelativeTo(null);

    initializeSimulation();
    startAnimation();

    setVisible(true);
  }

  private void initializeSimulation() {
    ripples.clear();
    particles.clear();
    springBalls.clear();

    switch (currentType) {
      case GRAVITY -> {
        for (int i = 0; i < 50; i++) {
          particles.add(new Particle(Math.random() * 800, Math.random() * 200));
        }
      }
      case DOUBLE_PENDULUM -> {
        pendulum = new DoublePendulum();
      }
      case SPRINGS -> {
        for (int i = 0; i < 10; i++) {
          springBalls.add(new SpringBall(100 + i * 80, 50));
        }
      }
      case RIPPLE -> {
        /* ripples list already cleared above; fresh ripples are added on click */
      }
    }
  }

  private void startAnimation() {
    if (timer != null) {
      timer.stop();
    }

    timer =
        new Timer(
            16,
            e -> {
              updateSimulation();
              drawPanel.repaint();
            });
    timer.start();
  }

  private void updateSimulation() {
    switch (currentType) {
      case RIPPLE -> {
        ripples.removeIf(Ripple::isExpired);
        ripples.forEach(Ripple::update);
      }
      case GRAVITY -> {
        particles.forEach(p -> p.update(drawPanel.getWidth(), drawPanel.getHeight()));
      }
      case DOUBLE_PENDULUM -> {
        if (pendulum != null) {
          pendulum.update();
          Point2D.Double pos = pendulum.getSecondBobPosition(450, 100);
          pendulum.trail.add(pos);
          if (pendulum.trail.size() > 500) {
            pendulum.trail.remove(0);
          }
        }
      }
      case SPRINGS -> {
        springBalls.forEach(SpringBall::update);
      }
    }
  }

  class DrawPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    DrawPanel() {
      setBackground(Color.BLACK);
      setPreferredSize(new Dimension(900, 650));

      addMouseListener(
          new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
              handleClick(e.getX(), e.getY());
            }
          });

      addMouseMotionListener(
          new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
              if (currentType == SimulationType.RIPPLE) {
                handleClick(e.getX(), e.getY());
              }
            }
          });
    }

    private void handleClick(int x, int y) {
      switch (currentType) {
        case RIPPLE -> ripples.add(new Ripple(x, y));
        case GRAVITY -> particles.add(new Particle(x, y));
        case SPRINGS -> {
          for (SpringBall ball : springBalls) {
            double dist = Math.hypot(ball.x - x, ball.y - y);
            if (dist < 30) {
              ball.vx = (ball.x - x) * 2;
              ball.vy = (ball.y - y) * 2;
            }
          }
        }
        case DOUBLE_PENDULUM -> {
          /* clicks have no effect on the double pendulum */
        }
      }
    }

    @Override
    protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      Graphics2D g2 = (Graphics2D) g;
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

      switch (currentType) {
        case RIPPLE -> drawRipples(g2);
        case GRAVITY -> drawParticles(g2);
        case DOUBLE_PENDULUM -> drawDoublePendulum(g2);
        case SPRINGS -> drawSprings(g2);
      }
    }

    private void drawRipples(Graphics2D g2) {
      for (Ripple ripple : ripples) {
        float intensity = (float) ripple.getIntensity();
        g2.setColor(new Color(0.3f, 0.6f, 1.0f, intensity * 0.8f));
        g2.setStroke(new BasicStroke(3 * intensity));
        g2.draw(
            new Ellipse2D.Double(
                ripple.x - ripple.radius,
                ripple.y - ripple.radius,
                ripple.radius * 2,
                ripple.radius * 2));
      }

      g2.setColor(Color.WHITE);
      g2.drawString("Click or drag to create ripples", 10, 20);
    }

    private void drawParticles(Graphics2D g2) {
      for (Particle p : particles) {
        g2.setColor(p.color);
        g2.fill(new Ellipse2D.Double(p.x - p.radius, p.y - p.radius, p.radius * 2, p.radius * 2));
      }

      g2.setColor(Color.WHITE);
      g2.drawString("Click to add particles - " + particles.size() + " particles", 10, 20);
    }

    private void drawDoublePendulum(Graphics2D g2) {
      if (pendulum == null) return;

      int cx = 450;
      int cy = 100;

      // Draw trail
      g2.setStroke(new BasicStroke(1));
      for (int i = 1; i < pendulum.trail.size(); i++) {
        float alpha = i / (float) pendulum.trail.size();
        g2.setColor(new Color(1.0f, 0.5f, 0.0f, alpha * 0.5f));
        Point2D.Double p1 = pendulum.trail.get(i - 1);
        Point2D.Double p2 = pendulum.trail.get(i);
        g2.draw(new Line2D.Double(p1.x, p1.y, p2.x, p2.y));
      }

      // Calculate positions
      double x1 = cx + pendulum.length1 * Math.sin(pendulum.angle1);
      double y1 = cy + pendulum.length1 * Math.cos(pendulum.angle1);
      double x2 = x1 + pendulum.length2 * Math.sin(pendulum.angle2);
      double y2 = y1 + pendulum.length2 * Math.cos(pendulum.angle2);

      // Draw arms
      g2.setColor(Color.WHITE);
      g2.setStroke(new BasicStroke(3));
      g2.draw(new Line2D.Double(cx, cy, x1, y1));
      g2.draw(new Line2D.Double(x1, y1, x2, y2));

      // Draw bobs
      g2.setColor(Color.RED);
      g2.fill(new Ellipse2D.Double(x1 - 15, y1 - 15, 30, 30));
      g2.setColor(Color.YELLOW);
      g2.fill(new Ellipse2D.Double(x2 - 15, y2 - 15, 30, 30));

      // Draw pivot
      g2.setColor(Color.GRAY);
      g2.fill(new Ellipse2D.Double(cx - 5, cy - 5, 10, 10));

      g2.setColor(Color.WHITE);
      g2.drawString("Double Pendulum - Chaos Theory in Action", 10, 20);
    }

    private void drawSprings(Graphics2D g2) {
      for (SpringBall ball : springBalls) {
        // Draw spring
        g2.setColor(Color.GRAY);
        g2.setStroke(new BasicStroke(2));
        g2.draw(new Line2D.Double(ball.anchorX, ball.anchorY, ball.x, ball.y));

        // Draw anchor
        g2.setColor(Color.WHITE);
        g2.fill(new Ellipse2D.Double(ball.anchorX - 5, ball.anchorY - 5, 10, 10));

        // Draw ball
        g2.setColor(ball.color);
        g2.fill(new Ellipse2D.Double(ball.x - 20, ball.y - 20, 40, 40));
      }

      g2.setColor(Color.WHITE);
      g2.drawString("Click near balls to push them!", 10, 20);
    }
  }

  class ControlPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    ControlPanel() {
      setLayout(new FlowLayout());

      JComboBox<SimulationType> typeCombo = new JComboBox<>(SimulationType.values());
      typeCombo.setRenderer(
          new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
              super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
              if (value instanceof SimulationType st) {
                setText(st.displayName);
              }
              return this;
            }
          });

      JButton resetBtn = new JButton("Reset");
      JButton clearBtn = new JButton("Clear");

      typeCombo.addActionListener(
          e -> {
            currentType = (SimulationType) typeCombo.getSelectedItem();
            initializeSimulation();
          });

      resetBtn.addActionListener(e -> initializeSimulation());
      clearBtn.addActionListener(
          e -> {
            ripples.clear();
            particles.clear();
            if (pendulum != null) {
              pendulum.trail.clear();
            }
          });

      add(new JLabel("Simulation: "));
      add(typeCombo);
      add(resetBtn);
      add(clearBtn);
    }
  }

  public static void main() {
    SwingUtilities.invokeLater(WavePhysicsVisualizer::new);
  }
}
