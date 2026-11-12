package d021248.mytry.demos.games;

import static com.almasb.fxgl.dsl.FXGL.*;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.component.Component;
import java.util.List;
import java.util.Map;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

/// FXGL demo: a paddle + bouncing balls.
///
/// Controls:
///   LEFT / RIGHT arrows — move paddle
///   SPACE              — launch a new ball
///   Q                  — quit
///
/// Each ball that falls off the bottom costs a life.
/// Survive as long as possible.
///
/// Run from IDE (JavaFX module path is resolved automatically).
public final class FXGLDemo extends GameApplication {

  private static final int W = 800;
  private static final int H = 600;
  private static final int PADDLE_W = 120;
  private static final int PADDLE_H = 14;
  private static final int BALL_R = 10;
  private static final double PADDLE_SPEED = 400;
  private static final String VAR_SCORE = "score";
  private static final String VAR_LIVES = "lives";

  private Entity paddle;

  @Override
  protected void initSettings(GameSettings s) {
    s.setTitle("FXGL Bouncing Balls Demo");
    s.setWidth(W);
    s.setHeight(H);
    s.setVersion("1.0");
  }

  @Override
  protected void initGameVars(Map<String, Object> vars) {
    vars.put(VAR_SCORE, 0);
    vars.put(VAR_LIVES, 3);
  }

  @Override
  protected void initGame() {
    getGameScene().setBackgroundColor(Color.color(0.08, 0.08, 0.15));

    paddle =
        entityBuilder()
            .at((double) W / 2 - (double) PADDLE_W / 2, H - 50.0)
            .view(new Rectangle(PADDLE_W, PADDLE_H, Color.CORNFLOWERBLUE))
            .buildAndAttach();

    runOnce(this::spawnBall, Duration.seconds(0.5));
  }

  @Override
  protected void initInput() {
    onKey(KeyCode.LEFT, () -> movePaddle(-PADDLE_SPEED * tpf()));
    onKey(KeyCode.RIGHT, () -> movePaddle(PADDLE_SPEED * tpf()));
    onKeyDown(KeyCode.SPACE, this::spawnBall);
    onKeyDown(KeyCode.Q, () -> getGameController().exit());
  }

  @Override
  protected void initUI() {
    var scoreText = getUIFactoryService().newText("", Color.WHITE, 20);
    scoreText
        .textProperty()
        .bind(getWorldProperties().intProperty(VAR_SCORE).asString("Score: %d"));
    scoreText.setTranslateX(10);
    scoreText.setTranslateY(24);

    var livesText = getUIFactoryService().newText("", Color.TOMATO, 20);
    livesText
        .textProperty()
        .bind(getWorldProperties().intProperty(VAR_LIVES).asString("Lives: %d"));
    livesText.setTranslateX(W - 100);
    livesText.setTranslateY(24);

    var hint =
        getUIFactoryService()
            .newText("SPACE=new ball  ←→=move  Q=quit", Color.color(0.5, 0.5, 0.5), 13);
    hint.setTranslateX(10);
    hint.setTranslateY(H - 8);

    addUINode(scoreText);
    addUINode(livesText);
    addUINode(hint);
  }

  @Override
  protected void onUpdate(double tpf) {
    getGameWorld()
        .getEntitiesByComponent(BallComponent.class)
        .forEach(e -> e.getComponent(BallComponent.class).tick(tpf));

    List<Entity> fallen =
        getGameWorld().getEntitiesByComponent(BallComponent.class).stream()
            .filter(e -> e.getY() > H + BALL_R * 2)
            .toList();

    for (Entity ball : fallen) {
      ball.removeFromWorld();
      inc(VAR_LIVES, -1);
      if (getWorldProperties().getInt(VAR_LIVES) <= 0) {
        showMessage(
            "Game Over!\nScore: " + getWorldProperties().getInt(VAR_SCORE),
            () -> getGameController().startNewGame());
      }
    }
  }

  private void spawnBall() {
    double angle = Math.toRadians(-60 - Math.random() * 60);
    double speed = 280 + Math.random() * 120;
    Color color = Color.hsb(Math.random() * 360, 0.8, 1.0);

    entityBuilder()
        .at(paddle.getX() + (double) PADDLE_W / 2 - BALL_R, paddle.getY() - (double) BALL_R * 2)
        .view(new Circle(BALL_R, color))
        .with(new BallComponent(speed * Math.cos(angle), speed * Math.sin(angle)))
        .buildAndAttach();
  }

  private void movePaddle(double dx) {
    paddle.setX(Math.clamp(paddle.getX() + dx, 0, W - PADDLE_W));
  }

  /// Per-ball movement + wall/paddle bounce logic.
  private class BallComponent extends Component {

    private double vx;
    private double vy;

    BallComponent(double vx, double vy) {
      this.vx = vx;
      this.vy = vy;
    }

    void tick(double tpf) {
      entity.translate(vx * tpf, vy * tpf);

      double x = entity.getX();
      double y = entity.getY();

      if (x < 0) {
        entity.setX(0);
        vx = Math.abs(vx);
      }
      if (x > W - (double) BALL_R * 2) {
        entity.setX(W - (double) BALL_R * 2);
        vx = -Math.abs(vx);
      }
      if (y < 0) {
        entity.setY(0);
        vy = Math.abs(vy);
      }

      double px = paddle.getX();
      double py = paddle.getY();
      if (vy > 0
          && x + (double) BALL_R * 2 >= px
          && x <= px + PADDLE_W
          && y + (double) BALL_R * 2 >= py
          && y <= py + PADDLE_H) {
        vy = -Math.abs(vy);
        double rel = (x + BALL_R - px) / PADDLE_W * 2 - 1;
        vx += rel * 60;
        inc(VAR_SCORE, 10);
      }
    }
  }

  public static void main(String[] args) {
    launch(args);
  }
}
