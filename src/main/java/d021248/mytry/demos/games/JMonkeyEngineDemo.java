package d021248.mytry.demos.games;

import com.jme3.app.SimpleApplication;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.system.AppSettings;

/// jMonkeyEngine demo: a spinning box and sphere in a lit 3D scene.
///
/// Controls (built-in fly camera):
///   W/A/S/D       — move camera
///   mouse drag    — look around
///   SPACE         — toggle box rotation
///   Escape        — quit
///
/// Run from IDE or:
///   mvn exec:java -Dexec.mainClass=d021248.mytry.demos.JMonkeyEngineDemo
public final class JMonkeyEngineDemo extends SimpleApplication {

  private Geometry box;
  private Geometry sphere;
  private boolean rotating = true;
  private float time = 0f;

  public static void main() {
    AppSettings settings = new AppSettings(true);
    settings.setTitle("jMonkeyEngine Demo");
    settings.setResolution(1024, 768);
    settings.setSamples(4);

    JMonkeyEngineDemo app = new JMonkeyEngineDemo();
    app.setSettings(settings);
    app.setShowSettings(false);
    app.start();
  }

  @Override
  public void simpleInitApp() {
    viewPort.setBackgroundColor(new ColorRGBA(0.05f, 0.05f, 0.15f, 1f));
    flyCam.setMoveSpeed(10f);
    cam.setLocation(new Vector3f(0f, 3f, 8f));
    cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);

    setupLights();
    setupBox();
    setupSphere();
    setupFloor();
    setupInput();
  }

  private void setupLights() {
    AmbientLight ambient = new AmbientLight();
    ambient.setColor(ColorRGBA.White.mult(0.3f));
    rootNode.addLight(ambient);

    DirectionalLight sun = new DirectionalLight();
    sun.setDirection(new Vector3f(-1f, -2f, -1f).normalizeLocal());
    sun.setColor(ColorRGBA.White.mult(1.2f));
    rootNode.addLight(sun);
  }

  private void setupBox() {
    Box mesh = new Box(1f, 1f, 1f);
    box = new Geometry("Box", mesh);

    Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
    mat.setBoolean("UseMaterialColors", true);
    mat.setColor("Ambient", new ColorRGBA(0.2f, 0.4f, 0.8f, 1f));
    mat.setColor("Diffuse", new ColorRGBA(0.3f, 0.5f, 1.0f, 1f));
    mat.setColor("Specular", ColorRGBA.White);
    mat.setFloat("Shininess", 64f);

    box.setMaterial(mat);
    box.setLocalTranslation(0f, 1f, 0f);
    rootNode.attachChild(box);
  }

  private void setupSphere() {
    Sphere mesh = new Sphere(32, 32, 0.6f);
    sphere = new Geometry("Sphere", mesh);

    Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
    mat.setBoolean("UseMaterialColors", true);
    mat.setColor("Ambient", new ColorRGBA(0.8f, 0.3f, 0.2f, 1f));
    mat.setColor("Diffuse", new ColorRGBA(1.0f, 0.4f, 0.3f, 1f));
    mat.setColor("Specular", ColorRGBA.White);
    mat.setFloat("Shininess", 96f);

    sphere.setMaterial(mat);
    sphere.setLocalTranslation(2.5f, 0.6f, 0f);
    rootNode.attachChild(sphere);
  }

  private void setupFloor() {
    Box mesh = new Box(6f, 0.1f, 6f);
    Geometry floor = new Geometry("Floor", mesh);

    Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
    mat.setBoolean("UseMaterialColors", true);
    mat.setColor("Ambient", new ColorRGBA(0.15f, 0.15f, 0.15f, 1f));
    mat.setColor("Diffuse", new ColorRGBA(0.25f, 0.25f, 0.25f, 1f));
    mat.setColor("Specular", ColorRGBA.Black);
    mat.setFloat("Shininess", 0f);

    floor.setMaterial(mat);
    floor.setLocalTranslation(0f, -0.1f, 0f);
    rootNode.attachChild(floor);
  }

  private void setupInput() {
    inputManager.addMapping("ToggleRotation", new KeyTrigger(KeyInput.KEY_SPACE));
    inputManager.addListener(
        (ActionListener)
            (name, pressed, tpf) -> {
              if (pressed) rotating = !rotating;
            },
        "ToggleRotation");
  }

  @Override
  public void simpleUpdate(float tpf) {
    if (!rotating) return;
    time += tpf;

    // box spins on Y axis, bobs gently
    box.setLocalRotation(box.getLocalRotation().fromAngleAxis(time * 0.8f, Vector3f.UNIT_Y));
    box.setLocalTranslation(0f, 1f + FastMath.sin(time * 1.5f) * 0.3f, 0f);

    // sphere orbits the box
    float r = 2.5f;
    sphere.setLocalTranslation(FastMath.cos(time * 0.6f) * r, 0.6f, FastMath.sin(time * 0.6f) * r);
  }
}
