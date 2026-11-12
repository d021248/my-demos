# my-demos

[![Java](https://img.shields.io/badge/Java-25-orange?logo=openjdk)](https://openjdk.org/projects/jdk/25/)
[![Build](https://img.shields.io/badge/Build-Maven-blue?logo=apachemaven)](https://maven.apache.org/)
[![CI](https://github.com/d021248/my-demos/actions/workflows/ci.yml/badge.svg)](https://github.com/d021248/my-demos/actions/workflows/ci.yml)

Interactive Java demos — GraphStream graph visualizations and game/terminal experiments.
Part of the [`d021248`](https://github.com/d021248) learning suite alongside
[my-try](https://github.com/d021248/my-try) · [my-lisp](https://github.com/d021248/my-lisp) · [my-downloads](https://github.com/d021248/my-downloads).

---

## Contents

### `algorithms/graphstream/` — Graph Visualizations

Real-time graph visualizations using the [GraphStream](https://graphstream-project.org/) library.

| Class | What it shows |
|---|---|
| `GraphStreamBasicsDemo` | Nodes, edges, styling, dynamic updates |
| `BarabasiAlbertGraphStreamDemo` | Scale-free network growth (preferential attachment) |
| `CollatzGraph` / `CollatzGraphAnimated` | Collatz conjecture as a directed graph |
| `CalkinWilfGraphStreamTree` | Calkin–Wilf tree — rational numbers as a binary tree |
| `ClassHierarchyDemo` | Live Java class hierarchy visualization |
| `ProjectStructureDemo` | Maven project structure as a graph |
| `streetmap/StreetMapViewer` | Street map viewer from DGS data files |

### `demos/games/` — Game Engine Experiments

| Class | Framework |
|---|---|
| `GameOfLife` / `GameOfLifeFunctional` | Swing (no external dep) |
| `EvolutionApp` | Swing — evolution simulation |
| `MazeGenerator` | Swing |
| `FXGLDemo` | [FXGL](https://github.com/AlmasB/FXGL) |
| `LITIEngineDemo` | [LITIEngine](https://litiengine.com/) |
| `JMonkeyEngineDemo` | [jMonkeyEngine](https://jmonkeyengine.org/) |
| `TransfiniteSimulation` | Transfinite set simulation |

### `demos/libgdx/` — LibGDX Space Shooter

Minimal space shooter built with [LibGDX](https://libgdx.com/). Run via `SpaceShooterLauncher`.

### `demos/terminal/` — Terminal Art

ASCII art, emoji rain, Mandelbrot/Julia sets, matrix rain, a segment display, and a bar chart renderer — all in the terminal.

### `demos/quiz/` — Java Quizzes

Short programs that demonstrate surprising Java behaviour: comment execution tricks, evaluation order, inheritance and polymorphism edge cases.

---

## Build

```bash
mvn compile        # compile (no tests in this repo)
mvn package        # build JAR
```

Java 25 with `--enable-preview`. Spotless (Google Java Format) runs on every compile.

---

## Running

```bash
# GraphStream basics
mvn exec:java -Dexec.mainClass="d021248.mytry.algorithms.graphstream.GraphStreamBasicsDemo"

# Street map viewer
mvn exec:java -Dexec.mainClass="d021248.mytry.algorithms.graphstream.streetmap.StreetMapViewer"

# Game of Life
mvn exec:java -Dexec.mainClass="d021248.mytry.demos.games.GameOfLife"

# Terminal Julia set
mvn exec:java -Dexec.mainClass="d021248.mytry.demos.terminal.JuliaSetDemo"
```

---

## Dependencies

| Library | Version | Purpose |
|---|---|---|
| GraphStream (`gs-core`, `gs-algo`, `gs-ui-swing`) | 2.0 | Graph visualization |
| LibGDX (`gdx`, `gdx-backend-lwjgl3`, `gdx-platform`) | 1.14.2 | 2D game framework |
| FXGL | 25.0.1 | JavaFX game framework |
| LITIEngine | 0.12.0 | 2D Java game engine |
| jMonkeyEngine (`jme3-core`, `jme3-desktop`, `jme3-lwjgl3`) | 3.9.0-stable | 3D game engine |
