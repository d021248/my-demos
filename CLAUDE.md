# CLAUDE.md — my-demos

## Project Overview

Interactive demos — GraphStream graph visualizations and game/terminal experiments.
Java 25, Maven. Not curriculum code; nothing here is tested automatically.

Sibling repos: [my-try](../my-try) (core Java learning), [my-lisp](../my-lisp), [my-downloads](../my-downloads).

---

## Build

```bash
mvn compile                  # compile only (no tests in this repo)
mvn package -DskipTests      # build JAR
```

Java version: **25** (`--enable-preview` active).

---

## Key Conventions

### Language
All code, comments, Javadoc, and `package-info.java` files must be in **English**.

### Demo Classes

Two valid structures:

**A — Behaviour demo** (shows how to *use* an API):
```java
public final class FooDemo {
  public static void main() { }          // no String[] args
  private static void helper() {}       // helpers after main
}
```

**B — Concept demo** (defines the types it demonstrates):
```java
public final class BarDemo {
  record Pair<A, B>(A first, B second) {}  // inner types first
  public static void main() { }
}
```

Rules for both: `final` class, `main()` without `String[] args`, `=== Section ===`
banners for logical blocks.

### Documentation
- Use `///` Markdown Javadoc (Java 23+), not `/** */`
- Every `package` needs a `package-info.java`
- `@since 1.0` on `package-info.java` blocks
- Keep `///` lines under ~100 chars (spotless wraps at ~100)

### Java 25 Style
- `var` where the type is obvious from the right-hand side
- Pattern-matching `instanceof`: `if (x instanceof Foo f)`
- `"...".formatted(...)` over `String.format` or `+` chains
- Never inline FQCNs — always `import`
- Records for immutable data; sealed classes for closed hierarchies
- Text blocks for multi-line strings

---

## Package Map

```
src/main/java/d021248/mytry/
│
├── algorithms/
│   ├── graphstream/         GraphStream visualizations (Barabasi-Albert, Collatz, class hierarchy)
│   │   └── streetmap/       Street map viewer — DGS data files + Swing display
│   └── visualization/       Algorithm visualizers: sorting, pathfinding, cellular automata,
│                            neural network, genetic algorithm, Q-learning, maze, wave physics (16 visualizers)
│
├── core/lang/gatherers/compress/
│   ├── lz77/                LZ77Token — local copy (no cross-repo dep)
│   └── lzw/                 LZWToken — local copy (no cross-repo dep)
│
├── demos/
│   ├── games/               Evolution sim, Game of Life, FXGL, LibGDX, jMonkeyEngine, LITIEngine
│   ├── libgdx/              LibGDX space shooter
│   ├── quiz/                Java quiz questions
│   └── terminal/            Terminal UI: ASCII art, bar charts, Julia set, emoji rain, matrix rain
│
└── media/
    ├── audio/               MP3 analytics: Mp3Demo, Mp3Scanner, Mp3Analytics, Mp3Metadata
    └── image/               Image metadata viewer (EXIF/IPTC/XMP), Swing UI
```

---

## Dependencies

| Artifact | Purpose |
|---|---|
| `gs-core`, `gs-algo`, `gs-ui-swing` 2.0 | GraphStream graph visualization |
| `gdx`, `gdx-backend-lwjgl3`, `gdx-platform` 1.14.2 | LibGDX game framework |
| `fxgl` 25.0.1 | FXGL game framework |
| `litiengine` 0.12.0 | LITIEngine 2D game engine |
| `jme3-core`, `jme3-desktop`, `jme3-lwjgl3` 3.9.0-stable | jMonkeyEngine 3D |
