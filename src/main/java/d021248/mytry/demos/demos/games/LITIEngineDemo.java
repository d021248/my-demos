package d021248.mytry.demos.games;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.IUpdateable;
import de.gurkenlabs.litiengine.gui.screens.GameScreen;
import de.gurkenlabs.litiengine.gui.screens.Resolution;
import de.gurkenlabs.litiengine.input.Input;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

/// LITIengine demo — bouncing balls + paddle, no .litidata file required.
///
/// Controls:
/// LEFT / RIGHT arrows — move paddle
/// SPACE — launch a new ball
/// ESCAPE — quit
///
/// Demonstrates:
/// - Game.init() / Game.start()
/// - Custom GameScreen with Graphics2D rendering
/// - Game.loop().attach(IUpdateable) for game logic
/// - Input.keyboard() for key handling
public final class LITIEngineDemo {

  private static final int W = 800;
  private static final int H = 600;
  private static final int PADDLE_W = 120;
  private static final int PADDLE_H = 14;
  private static final int BALL_R = 10;
  private static final double PADDLE_SPEED = 400;

  public static void main(String[] args) {
    Game.info().setName("LITIengine Bouncing Balls Demo");
    Game.info().setVersion("1.0");

    Game.init(args);

    Game.window().setResolution(Resolution.Ratio4x3.RES_800x600);

    GameLogic logic = new GameLogic();
    Game.loop().attach(logic);

    Input.keyboard().onKeyPressed(KeyEvent.VK_ESCAPE, e -> Game.exit());

    Game.screens().add(new PlayScreen(logic));
    Game.screens().display(PlayScreen.NAME);

    Game.start();
  }

  // -------------------------------------------------------------------------
  // Game state — plain POJO, updated by the game loop, read by the renderer
  // -------------------------------------------------------------------------

  static class Ball {
    double x, y;
    double vx, vy;
    Color color;

    Ball(double x, double y, double vx, double vy, Color color) {
      this.x = x;
      this.y = y;
      this.vx = vx;
      this.vy = vy;
      this.color = color;
    }
  }

  static class GameLogic implements IUpdateable {

    double paddleX = (double) W / 2 - (double) PADDLE_W / 2;
    final double paddleY = H - 50.0;
    final List<Ball> balls = new ArrayList<>();
    int score = 0;
    int lives = 3;
    boolean gameOver = false;
    boolean restart = false;

    GameLogic() {
      spawnBall();
    }

    void spawnBall() {
      double angle = Math.toRadians(-60 - Math.random() * 60);
      double speed = 280 + Math.random() * 120;
      float hue = (float) Math.random();
      Color color = Color.getHSBColor(hue, 0.8f, 1.0f);
      balls.add(
          new Ball(
              paddleX + (double) PADDLE_W / 2 - BALL_R,
              paddleY - BALL_R * 2.0,
              speed * Math.cos(angle),
              speed * Math.sin(angle),
              color));
    }

    void reset() {
      balls.clear();
      paddleX = (double) W / 2 - (double) PADDLE_W / 2;
      score = 0;
      lives = 3;
      gameOver = false;
      spawnBall();
    }

    @Override
    public void update() {
      if (gameOver) {
        if (restart) {
          reset();
        }
        return;
      }

      // tpf from loop tick rate — approximate 1/fps
      double tpf = 1.0 / Game.loop().getTickRate();

      // paddle movement
      if (Input.keyboard().isPressed(KeyEvent.VK_LEFT)) {
        paddleX = Math.max(0, paddleX - PADDLE_SPEED * tpf);
      }
      if (Input.keyboard().isPressed(KeyEvent.VK_RIGHT)) {
        paddleX = Math.min(W - PADDLE_W, paddleX + PADDLE_SPEED * tpf);
      }
      if (Input.keyboard().isPressed(KeyEvent.VK_SPACE)) {
        restart = false; // absorb from any previous state
      }

      // ball physics
      List<Ball> fallen = new ArrayList<>();
      for (Ball b : balls) {
        b.x += b.vx * tpf;
        b.y += b.vy * tpf;

        // wall bounces
        if (b.x < 0) {
          b.x = 0;
          b.vx = Math.abs(b.vx);
        }
        if (b.x > W - BALL_R * 2.0) {
          b.x = W - BALL_R * 2.0;
          b.vx = -Math.abs(b.vx);
        }
        if (b.y < 0) {
          b.y = 0;
          b.vy = Math.abs(b.vy);
        }

        // paddle bounce
        if (b.vy > 0
            && b.x + BALL_R * 2.0 >= paddleX
            && b.x <= paddleX + PADDLE_W
            && b.y + BALL_R * 2.0 >= paddleY
            && b.y <= paddleY + PADDLE_H) {
          b.vy = -Math.abs(b.vy);
          double rel = (b.x + BALL_R - paddleX) / PADDLE_W * 2 - 1;
          b.vx += rel * 60;
          score += 10;
        }

        if (b.y > H + BALL_R * 2) {
          fallen.add(b);
        }
      }

      for (Ball b : fallen) {
        balls.remove(b);
        lives--;
        if (lives <= 0) {
          gameOver = true;
        }
      }
    }
  }

  // -------------------------------------------------------------------------
  // Screen — pure Graphics2D rendering, no environment/tilemap needed
  // -------------------------------------------------------------------------

  static class PlayScreen extends GameScreen {

    static final String NAME = "PLAY";
    private static final Color BG = new Color(20, 20, 38);
    private static final Color PADDLE_COLOR = new Color(100, 149, 237);
    private static final Font HUD_FONT = new Font("SansSerif", Font.BOLD, 18);
    private static final Font HINT_FONT = new Font("SansSerif", Font.PLAIN, 13);
    private static final Font OVER_FONT = new Font("SansSerif", Font.BOLD, 36);

    private final GameLogic logic;

    PlayScreen(GameLogic logic) {
      super(NAME);
      this.logic = logic;

      // space bar spawns a new ball / restarts after game-over
      Input.keyboard()
          .onKeyPressed(
              KeyEvent.VK_SPACE,
              e -> {
                if (logic.gameOver) {
                  logic.restart = true;
                } else {
                  logic.spawnBall();
                }
              });
    }

    @Override
    public void render(Graphics2D g) {
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

      // background
      g.setColor(BG);
      g.fillRect(0, 0, W, H);

      // paddle
      g.setColor(PADDLE_COLOR);
      g.fillRoundRect((int) logic.paddleX, (int) logic.paddleY, PADDLE_W, PADDLE_H, 6, 6);

      // balls
      for (Ball b : List.copyOf(logic.balls)) {
        g.setColor(b.color);
        g.fillOval((int) b.x, (int) b.y, BALL_R * 2, BALL_R * 2);
      }

      // HUD
      g.setFont(HUD_FONT);
      g.setColor(Color.WHITE);
      g.drawString("Score: " + logic.score, 10, 26);

      g.setColor(new Color(255, 99, 71));
      FontMetrics fm = g.getFontMetrics();
      String livesStr = "Lives: " + logic.lives;
      g.drawString(livesStr, W - fm.stringWidth(livesStr) - 10, 26);

      g.setFont(HINT_FONT);
      g.setColor(new Color(128, 128, 128));
      g.drawString("SPACE=new ball  ←→=move  ESC=quit", 10, H - 8);

      // game-over overlay
      if (logic.gameOver) {
        g.setColor(new Color(0, 0, 0, 160));
        g.fillRect(0, 0, W, H);

        g.setFont(OVER_FONT);
        g.setColor(Color.WHITE);
        String over = "Game Over!";
        FontMetrics fmOver = g.getFontMetrics();
        g.drawString(over, (W - fmOver.stringWidth(over)) / 2, H / 2 - 20);

        g.setFont(HUD_FONT);
        FontMetrics fmHud = g.getFontMetrics(HUD_FONT);
        String finalScore = "Final Score: " + logic.score;
        g.drawString(finalScore, (W - fmHud.stringWidth(finalScore)) / 2, H / 2 + 20);

        String restart = "Press SPACE to restart";
        g.setColor(new Color(200, 200, 200));
        g.drawString(restart, (W - fmHud.stringWidth(restart)) / 2, H / 2 + 50);
      }

      // let GuiComponent render any registered sub-components
      super.render(g);
    }
  }
}
