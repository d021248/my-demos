package d021248.mytry.algorithms.visualization;

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
import java.awt.geom.Ellipse2D;
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

/// Particle Systems Visualizer
/// Beautiful particle effects including fireworks, flocking, and gravity wells.

@SuppressWarnings({"this-escape", "serial"})
public final class ParticleSystemsVisualizer extends JFrame {
  private static final long serialVersionUID = 1L;
  private final ParticlePanel particlePanel;
  private final ControlPanel controlPanel;

  private SystemType currentType = SystemType.FIREWORKS;
  private Timer timer;

  private enum SystemType {
    FIREWORKS("Fireworks"),
    FLOCKING("Flocking (Boids)"),
    GRAVITY_WELLS("Gravity Wells"),
    SNOW("Snowfall");

    final String displayName;

    SystemType(String displayName) {
      this.displayName = displayName;
    }
  }

  private final List<Particle> particles = new ArrayList<>();
  private final List<Boid> boids = new ArrayList<>();
  private final List<GravityWell> wells = new ArrayList<>();

  private static class Particle {
    double x;
    double y;
    double vx;
    double vy;
    double life;
    double maxLife;
    Color color;
    double size;

    Particle(double x, double y, double vx, double vy, Color color) {
      this.x = x;
      this.y = y;
      this.vx = vx;
      this.vy = vy;
      this.color = color;
      this.life = 1.0;
      this.maxLife = 1.0 + Math.random() * 2.0;
      this.size = 2 + Math.random() * 3;
    }

    void update() {
      vx *= 0.98;
      vy += 0.3; // gravity
      x += vx;
      y += vy;
      life -= 0.02;
    }

    boolean isDead() {
      return life <= 0;
    }
  }

  private static class Boid {
    double x;
    double y;
    double vx;
    double vy;
    Color color;

    Boid(double x, double y) {
      this.x = x;
      this.y = y;
      double angle = Math.random() * Math.PI * 2;
      this.vx = Math.cos(angle) * 2;
      this.vy = Math.sin(angle) * 2;
      this.color =
          new Color(
              (int) (150 + Math.random() * 105),
              (int) (150 + Math.random() * 105),
              (int) (150 + Math.random() * 105));
    }

    void update(List<Boid> others, int width, int height) {
      double separationX = 0;
      double separationY = 0;
      double alignmentX = 0;
      double alignmentY = 0;
      double cohesionX = 0;
      double cohesionY = 0;
      int count = 0;

      for (Boid other : others) {
        if (other == this) continue;

        double dist = Math.hypot(other.x - x, other.y - y);

        if (dist < 50) {
          // Separation
          separationX += (x - other.x) / dist;
          separationY += (y - other.y) / dist;
          count++;
        }

        if (dist < 100) {
          // Alignment
          alignmentX += other.vx;
          alignmentY += other.vy;

          // Cohesion
          cohesionX += other.x;
          cohesionY += other.y;
        }
      }

      if (count > 0) {
        separationX /= count;
        separationY /= count;
        alignmentX /= count;
        alignmentY /= count;
        cohesionX = cohesionX / count - x;
        cohesionY = cohesionY / count - y;

        vx += separationX * 0.1 + alignmentX * 0.05 + cohesionX * 0.01;
        vy += separationY * 0.1 + alignmentY * 0.05 + cohesionY * 0.01;
      }

      // Limit speed
      double speed = Math.hypot(vx, vy);
      if (speed > 4) {
        vx = (vx / speed) * 4;
        vy = (vy / speed) * 4;
      }

      x += vx;
      y += vy;

      // Wrap around
      if (x < 0) x = width;
      if (x > width) x = 0;
      if (y < 0) y = height;
      if (y > height) y = 0;
    }
  }

  private static class GravityWell {
    double x;
    double y;
    double strength;
    Color color;

    GravityWell(double x, double y, double strength) {
      this.x = x;
      this.y = y;
      this.strength = strength;
      this.color = strength > 0 ? Color.CYAN : Color.MAGENTA;
    }
  }

  public ParticleSystemsVisualizer() {
    super("Particle Systems - Beautiful Physics Simulations");

    particlePanel = new ParticlePanel();
    controlPanel = new ControlPanel();

    setLayout(new BorderLayout());
    add(particlePanel, BorderLayout.CENTER);
    add(controlPanel, BorderLayout.SOUTH);

    setSize(1000, 750);
    setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    setLocationRelativeTo(null);

    initializeSystem();
    startAnimation();

    setVisible(true);
  }

  private void initializeSystem() {
    particles.clear();
    boids.clear();
    wells.clear();

    switch (currentType) {
      case FIREWORKS -> {
        // Fireworks initialized on demand when created
      }
      case SNOW -> {
        // Snow particles created continuously in updateSystem
      }
      case FLOCKING -> {
        for (int i = 0; i < 100; i++) {
          boids.add(new Boid(Math.random() * 1000, Math.random() * 650));
        }
      }
      case GRAVITY_WELLS -> {
        wells.add(new GravityWell(300, 325, 5));
        wells.add(new GravityWell(700, 325, -5));
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
              updateSystem();
              particlePanel.repaint();
            });
    timer.start();
  }

  private void updateSystem() {
    switch (currentType) {
      case FIREWORKS -> {
        particles.removeIf(Particle::isDead);
        particles.forEach(Particle::update);

        // Randomly create fireworks
        if (Math.random() < 0.02) {
          createFirework(Math.random() * 1000, Math.random() * 400);
        }
      }
      case FLOCKING -> {
        boids.forEach(b -> b.update(boids, particlePanel.getWidth(), particlePanel.getHeight()));
      }
      case GRAVITY_WELLS -> {
        particles.removeIf(Particle::isDead);

        for (Particle p : particles) {
          for (GravityWell well : wells) {
            double dx = well.x - p.x;
            double dy = well.y - p.y;
            double distSq = dx * dx + dy * dy;
            double dist = Math.sqrt(distSq);

            if (dist > 1) {
              double force = well.strength / distSq;
              p.vx += (dx / dist) * force;
              p.vy += (dy / dist) * force;
            }
          }
          p.x += p.vx;
          p.y += p.vy;
          p.life -= 0.005;
        }

        // Add particles
        if (Math.random() < 0.3) {
          double angle = Math.random() * Math.PI * 2;
          double speed = Math.random() * 3;
          particles.add(
              new Particle(
                  500, 325, Math.cos(angle) * speed, Math.sin(angle) * speed, Color.WHITE));
        }
      }
      case SNOW -> {
        particles.forEach(
            p -> {
              p.vy = 1 + Math.random() * 2;
              p.vx = Math.sin(p.y * 0.01) * 0.5;
              p.x += p.vx;
              p.y += p.vy;

              if (p.y > particlePanel.getHeight()) {
                p.y = -10;
                p.x = Math.random() * particlePanel.getWidth();
              }
            });

        while (particles.size() < 200) {
          particles.add(new Particle(Math.random() * 1000, Math.random() * 650, 0, 1, Color.WHITE));
        }
      }
    }
  }

  private void createFirework(double x, double y) {
    Color color =
        new Color(
            (int) (Math.random() * 255), (int) (Math.random() * 255), (int) (Math.random() * 255));

    for (int i = 0; i < 50; i++) {
      double angle = Math.random() * Math.PI * 2;
      double speed = Math.random() * 8;
      particles.add(new Particle(x, y, Math.cos(angle) * speed, Math.sin(angle) * speed, color));
    }
  }

  class ParticlePanel extends JPanel {
    private static final long serialVersionUID = 1L;

    ParticlePanel() {
      setBackground(Color.BLACK);
      setPreferredSize(new Dimension(1000, 650));

      addMouseListener(
          new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
              handleClick(e.getX(), e.getY());
            }
          });
    }

    private void handleClick(int x, int y) {
      switch (currentType) {
        case FIREWORKS -> createFirework(x, y);
        case GRAVITY_WELLS -> wells.add(new GravityWell(x, y, 5));
        case FLOCKING, SNOW -> {
          // No click interaction for these modes
        }
      }
    }

    @Override
    protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      Graphics2D g2 = (Graphics2D) g;
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

      switch (currentType) {
        case FIREWORKS, GRAVITY_WELLS, SNOW -> drawParticles(g2);
        case FLOCKING -> drawBoids(g2);
      }

      // Draw gravity wells
      if (currentType == SystemType.GRAVITY_WELLS) {
        for (GravityWell well : wells) {
          g2.setColor(well.color);
          g2.fill(new Ellipse2D.Double(well.x - 15, well.y - 15, 30, 30));
        }
      }

      // Draw instructions
      g2.setColor(Color.WHITE);
      g2.drawString(
          "Click to "
              + (currentType == SystemType.FIREWORKS
                  ? "create fireworks"
                  : currentType == SystemType.GRAVITY_WELLS ? "add gravity well" : "interact"),
          10,
          20);
    }

    private void drawParticles(Graphics2D g2) {
      for (Particle p : particles) {
        float alpha = (float) Math.max(0, Math.min(1, p.life));
        Color c = p.color;
        g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), (int) (alpha * 255)));
        g2.fill(new Ellipse2D.Double(p.x - p.size / 2, p.y - p.size / 2, p.size, p.size));
      }
    }

    private void drawBoids(Graphics2D g2) {
      for (Boid b : boids) {
        g2.setColor(b.color);

        double angle = Math.atan2(b.vy, b.vx);
        int[] xPoints = new int[3];
        int[] yPoints = new int[3];

        xPoints[0] = (int) (b.x + Math.cos(angle) * 10);
        yPoints[0] = (int) (b.y + Math.sin(angle) * 10);
        xPoints[1] = (int) (b.x + Math.cos(angle + 2.5) * 5);
        yPoints[1] = (int) (b.y + Math.sin(angle + 2.5) * 5);
        xPoints[2] = (int) (b.x + Math.cos(angle - 2.5) * 5);
        yPoints[2] = (int) (b.y + Math.sin(angle - 2.5) * 5);

        g2.fillPolygon(xPoints, yPoints, 3);
      }
    }
  }

  class ControlPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    ControlPanel() {
      setLayout(new FlowLayout());

      JComboBox<SystemType> typeCombo = new JComboBox<>(SystemType.values());
      typeCombo.setRenderer(
          new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
              super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
              if (value instanceof SystemType st) {
                setText(st.displayName);
              }
              return this;
            }
          });

      JButton clearBtn = new JButton("Clear");

      typeCombo.addActionListener(
          e -> {
            currentType = (SystemType) typeCombo.getSelectedItem();
            initializeSystem();
          });

      clearBtn.addActionListener(
          e -> {
            particles.clear();
            boids.clear();
            wells.clear();
          });

      add(new JLabel("System: "));
      add(typeCombo);
      add(clearBtn);
    }
  }

  public static void main() {
    SwingUtilities.invokeLater(ParticleSystemsVisualizer::new);
  }
}
