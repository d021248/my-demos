package d021248.mytry.demos.libgdx;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/// LibGDX Space Shooter demo.
///
/// Controls: Arrow keys / WASD — move ship | SPACE — fire | R — restart (game over) | ESC —
/// quit
///
/// Features: scrolling starfield, player ship, bullet firing with cooldown, enemy waves that
/// accelerate over time, explosions, score + lives HUD.
public final class SpaceShooterGame extends ApplicationAdapter {

  // ── window ────────────────────────────────────────────────────────────────
  private static final int W = 800;
  private static final int H = 600;

  // ── player ────────────────────────────────────────────────────────────────
  private static final float SHIP_HALF = 14f;
  private static final float PLAYER_SPEED = 260f;
  private static final float BULLET_SPEED = 520f;
  private static final float BULLET_COOLDOWN = 0.18f;

  // ── enemies ───────────────────────────────────────────────────────────────
  private static final float ENEMY_HALF = 12f;
  private static final float ENEMY_BASE_SPEED = 90f;
  private static final float SPAWN_INTERVAL_START = 1.4f;
  private static final float SPAWN_INTERVAL_MIN = 0.35f;

  // ── stars ─────────────────────────────────────────────────────────────────
  private static final int STAR_COUNT = 120;

  // ── rendering ─────────────────────────────────────────────────────────────
  private ShapeRenderer shapes;
  private SpriteBatch batch;
  private BitmapFont font;

  // ── game state ────────────────────────────────────────────────────────────
  private Vector2 playerPos;
  private float bulletCooldownTimer;
  private final List<Vector2> bullets = new ArrayList<>();
  private final List<Vector2> enemies = new ArrayList<>();
  private final List<Explosion> explosions = new ArrayList<>();
  private final float[] starX = new float[STAR_COUNT];
  private final float[] starY = new float[STAR_COUNT];
  private final float[] starSpeed = new float[STAR_COUNT];
  private final float[] starSize = new float[STAR_COUNT];

  private int score;
  private int lives;
  private float spawnTimer;
  private float spawnInterval;
  private float elapsed;
  private boolean gameOver;

  // ── explosion helper ──────────────────────────────────────────────────────
  private record Explosion(float x, float y, float life, float maxLife) {
    Explosion tick(float dt) {
      return new Explosion(x, y, life - dt, maxLife);
    }

    boolean dead() {
      return life <= 0;
    }

    float alpha() {
      return life / maxLife;
    }
  }

  // ── lifecycle ─────────────────────────────────────────────────────────────

  @Override
  public void create() {
    shapes = new ShapeRenderer();
    batch = new SpriteBatch();
    font = new BitmapFont();
    font.setColor(Color.WHITE);
    resetGame();
  }

  private void resetGame() {
    playerPos = new Vector2(W / 2f, 60f);
    bullets.clear();
    enemies.clear();
    explosions.clear();
    score = 0;
    lives = 3;
    elapsed = 0;
    spawnInterval = SPAWN_INTERVAL_START;
    spawnTimer = spawnInterval;
    bulletCooldownTimer = 0;
    gameOver = false;
    initStars();
  }

  private void initStars() {
    for (int i = 0; i < STAR_COUNT; i++) {
      starX[i] = MathUtils.random(0, W);
      starY[i] = MathUtils.random(0, H);
      starSpeed[i] = MathUtils.random(30f, 120f);
      starSize[i] = MathUtils.random(0.8f, 2.5f);
    }
  }

  // ── update ────────────────────────────────────────────────────────────────

  @Override
  public void render() {
    float dt = Math.min(Gdx.graphics.getDeltaTime(), 0.05f);

    if (!gameOver) {
      update(dt);
    } else if (Gdx.input.isKeyJustPressed(Keys.R)) {
      resetGame();
    } else if (Gdx.input.isKeyJustPressed(Keys.ESCAPE)) {
      Gdx.app.exit();
    }

    draw();
  }

  private void update(float dt) {
    elapsed += dt;
    movePlayer(dt);
    fireBullet(dt);
    moveStars(dt);
    spawnEnemies(dt);
    moveBullets(dt);
    moveEnemies(dt);
    tickExplosions(dt);
    checkCollisions();
    if (Gdx.input.isKeyJustPressed(Keys.ESCAPE)) Gdx.app.exit();
  }

  private void movePlayer(float dt) {
    float dx = 0, dy = 0;
    if (Gdx.input.isKeyPressed(Keys.LEFT) || Gdx.input.isKeyPressed(Keys.A)) dx = -1;
    if (Gdx.input.isKeyPressed(Keys.RIGHT) || Gdx.input.isKeyPressed(Keys.D)) dx = 1;
    if (Gdx.input.isKeyPressed(Keys.UP) || Gdx.input.isKeyPressed(Keys.W)) dy = 1;
    if (Gdx.input.isKeyPressed(Keys.DOWN) || Gdx.input.isKeyPressed(Keys.S)) dy = -1;
    playerPos.x = MathUtils.clamp(playerPos.x + dx * PLAYER_SPEED * dt, SHIP_HALF, W - SHIP_HALF);
    playerPos.y = MathUtils.clamp(playerPos.y + dy * PLAYER_SPEED * dt, SHIP_HALF, H - SHIP_HALF);
  }

  private void fireBullet(float dt) {
    bulletCooldownTimer -= dt;
    if (Gdx.input.isKeyPressed(Keys.SPACE) && bulletCooldownTimer <= 0) {
      bullets.add(new Vector2(playerPos.x, playerPos.y + SHIP_HALF));
      bulletCooldownTimer = BULLET_COOLDOWN;
    }
  }

  private void moveStars(float dt) {
    for (int i = 0; i < STAR_COUNT; i++) {
      starY[i] -= starSpeed[i] * dt;
      if (starY[i] < 0) {
        starY[i] = H;
        starX[i] = MathUtils.random(0, W);
      }
    }
  }

  private void spawnEnemies(float dt) {
    // gradually narrow spawn interval and increase speed
    spawnInterval = Math.max(SPAWN_INTERVAL_MIN, SPAWN_INTERVAL_START - elapsed * 0.04f);
    spawnTimer -= dt;
    if (spawnTimer <= 0) {
      spawnTimer = spawnInterval;
      enemies.add(new Vector2(MathUtils.random(ENEMY_HALF, W - ENEMY_HALF), H + ENEMY_HALF));
    }
  }

  private void moveBullets(float dt) {
    bullets.replaceAll(b -> new Vector2(b.x, b.y + BULLET_SPEED * dt));
    bullets.removeIf(b -> b.y > H + 10);
  }

  private void moveEnemies(float dt) {
    float speed = ENEMY_BASE_SPEED + elapsed * 3.5f;
    enemies.replaceAll(e -> new Vector2(e.x, e.y - speed * dt));

    Iterator<Vector2> it = enemies.iterator();
    while (it.hasNext()) {
      Vector2 e = it.next();
      if (e.y < -ENEMY_HALF * 2) {
        it.remove();
        lives--;
        if (lives <= 0) gameOver = true;
      }
    }
  }

  private void tickExplosions(float dt) {
    explosions.replaceAll(ex -> ex.tick(dt));
    explosions.removeIf(Explosion::dead);
  }

  private void checkCollisions() {
    Rectangle bulletRect = new Rectangle();
    Rectangle enemyRect = new Rectangle();
    float bw = 4, bh = 10;
    float ew = ENEMY_HALF * 2, eh = ENEMY_HALF * 2;

    Iterator<Vector2> bi = bullets.iterator();
    while (bi.hasNext()) {
      Vector2 b = bi.next();
      bulletRect.set(b.x - bw / 2, b.y, bw, bh);
      Iterator<Vector2> ei = enemies.iterator();
      while (ei.hasNext()) {
        Vector2 e = ei.next();
        enemyRect.set(e.x - ew / 2, e.y - eh / 2, ew, eh);
        if (bulletRect.overlaps(enemyRect)) {
          bi.remove();
          ei.remove();
          score += 10;
          explosions.add(new Explosion(e.x, e.y, 0.4f, 0.4f));
          break;
        }
      }
    }

    // player hit
    Rectangle playerRect =
        new Rectangle(
            playerPos.x - SHIP_HALF, playerPos.y - SHIP_HALF, SHIP_HALF * 2, SHIP_HALF * 2);
    Iterator<Vector2> ei = enemies.iterator();
    while (ei.hasNext()) {
      Vector2 e = ei.next();
      enemyRect.set(e.x - ew / 2, e.y - eh / 2, ew, eh);
      if (playerRect.overlaps(enemyRect)) {
        ei.remove();
        lives--;
        explosions.add(new Explosion(playerPos.x, playerPos.y, 0.6f, 0.6f));
        if (lives <= 0) gameOver = true;
      }
    }
  }

  // ── draw ──────────────────────────────────────────────────────────────────

  private void draw() {
    Gdx.gl.glClearColor(0.04f, 0.04f, 0.10f, 1f);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

    shapes.begin(ShapeType.Filled);
    drawStars();
    if (!gameOver) {
      drawPlayer();
      drawBullets();
      drawEnemies();
    }
    drawExplosions();
    shapes.end();

    batch.begin();
    drawHud();
    if (gameOver) drawGameOver();
    batch.end();
  }

  private void drawStars() {
    for (int i = 0; i < STAR_COUNT; i++) {
      float brightness = 0.3f + (starSpeed[i] - 30f) / 90f * 0.7f;
      shapes.setColor(brightness, brightness, brightness, 1f);
      shapes.circle(starX[i], starY[i], starSize[i]);
    }
  }

  private void drawPlayer() {
    float x = playerPos.x, y = playerPos.y;
    // engine glow
    shapes.setColor(0.3f, 0.6f, 1.0f, 0.5f);
    shapes.ellipse(x - 6, y - SHIP_HALF - 10, 12, 14);
    // ship body (upward-pointing triangle)
    shapes.setColor(0.4f, 0.8f, 1.0f, 1f);
    shapes.triangle(x, y + SHIP_HALF, x - SHIP_HALF, y - SHIP_HALF, x + SHIP_HALF, y - SHIP_HALF);
    // cockpit highlight
    shapes.setColor(0.8f, 0.95f, 1.0f, 1f);
    shapes.triangle(x, y + SHIP_HALF * 0.8f, x - 4, y, x + 4, y);
  }

  private void drawBullets() {
    shapes.setColor(1f, 0.95f, 0.3f, 1f);
    for (Vector2 b : bullets) {
      shapes.rect(b.x - 2, b.y, 4, 12);
    }
  }

  private void drawEnemies() {
    for (Vector2 e : enemies) {
      float x = e.x, y = e.y;
      // enemy body: downward-pointing triangle, red-orange
      shapes.setColor(1.0f, 0.25f, 0.15f, 1f);
      shapes.triangle(
          x, y - ENEMY_HALF, x - ENEMY_HALF, y + ENEMY_HALF, x + ENEMY_HALF, y + ENEMY_HALF);
      // wing accents
      shapes.setColor(0.9f, 0.5f, 0.1f, 1f);
      shapes.triangle(x - ENEMY_HALF, y, x - ENEMY_HALF - 6, y - 6, x - ENEMY_HALF + 2, y - 6);
      shapes.triangle(x + ENEMY_HALF, y, x + ENEMY_HALF + 6, y - 6, x + ENEMY_HALF - 2, y - 6);
    }
  }

  private void drawExplosions() {
    for (Explosion ex : explosions) {
      float a = ex.alpha();
      float r = (1 - a) * 28 + 6;
      shapes.setColor(1f, 0.6f * a + 0.2f, 0f, a * 0.9f);
      shapes.circle(ex.x, ex.y, r);
      shapes.setColor(1f, 1f, 0.5f, a);
      shapes.circle(ex.x, ex.y, r * 0.45f);
    }
  }

  private void drawHud() {
    font.draw(batch, "Score: " + score, 10, H - 8);
    font.draw(batch, "Lives: " + lives, W - 90, H - 8);
    font.draw(batch, "SPACE=fire  WASD/Arrows=move  ESC=quit", 10, 18);
  }

  private void drawGameOver() {
    font.getData().setScale(2.5f);
    font.setColor(1f, 0.3f, 0.3f, 1f);
    font.draw(batch, "GAME OVER", W / 2f - 100, H / 2f + 30);
    font.getData().setScale(1.4f);
    font.setColor(Color.WHITE);
    font.draw(batch, "Score: " + score, W / 2f - 50, H / 2f - 10);
    font.getData().setScale(1f);
    font.setColor(Color.LIGHT_GRAY);
    font.draw(batch, "Press R to restart  |  ESC to quit", W / 2f - 140, H / 2f - 50);
    font.setColor(Color.WHITE);
  }

  // ── cleanup ───────────────────────────────────────────────────────────────

  @Override
  public void dispose() {
    shapes.dispose();
    batch.dispose();
    font.dispose();
  }
}
