package d021248.mytry.demos.libgdx;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

/// Desktop entry point for the LibGDX Space Shooter demo.
///
/// Run via Maven:
///
/// ```
///   mvn exec:java -Dexec.mainClass="d021248.mytry.demos.libgdx.SpaceShooterLauncher"
/// ```
public final class SpaceShooterLauncher {

  public static void main() {
    var config = new Lwjgl3ApplicationConfiguration();
    config.setTitle("LibGDX Space Shooter");
    config.setWindowedMode(800, 600);
    config.setForegroundFPS(60);
    config.setResizable(false);
    new Lwjgl3Application(new SpaceShooterGame(), config);
  }
}
