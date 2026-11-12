/// Algorithm Visualization Demonstrations
///
/// ## Overview
/// This package contains interactive visualizations of various algorithms,
/// making it easier to understand how they work through visual demonstration.
///
/// ## Available Visualizations
///
/// ### 1. Pathfinding Visualizer
/// ({@link d021248.mytry.algorithms.visualization.PathfindingVisualizer})
///
/// Interactive grid-based pathfinding visualization supporting:
///
/// - **A* Algorithm** - Optimal pathfinding with heuristics
/// - **Dijkstra's Algorithm** - Guaranteed shortest path
/// - **Breadth-First Search (BFS)** - Level-by-level exploration
/// - **Depth-First Search (DFS)** - Deep exploration strategy
///
/// **Features:**
///
/// - Click to draw/erase walls
/// - Right-click to set start/end points
/// - Watch algorithms explore the maze in real-time
/// - Color-coded visualization (visited, frontier, path)
/// - Random maze generation
///
/// ### 2. Sorting Race Visualizer
/// ({@link d021248.mytry.algorithms.visualization.SortingRaceVisualizer})
///
/// Side-by-side comparison of sorting algorithms:
///
/// - **Bubble Sort** - O(n²) comparison-based sort
/// - **Quick Sort** - O(n log n) divide-and-conquer
/// - **Merge Sort** - O(n log n) stable sort
///
/// **Features:**
///
/// - Simultaneous execution to compare performance
/// - Color-coded bars (comparing, swapping, sorted)
/// - Statistics tracking (comparisons, swaps, time)
/// - Adjustable array size and animation speed
/// - Multiple data patterns (random, sorted, reverse, nearly sorted)
///
/// ### 3. Recursive Tree Visualizer
/// ({@link d021248.mytry.algorithms.visualization.RecursiveTreeVisualizer})
///
/// Beautiful recursive pattern and fractal demonstrations:
///
/// - **Binary Tree** - Classic two-branch recursive tree
/// - **Ternary Tree** - Three-branch variation
/// - **Sierpinski Triangle** - Self-similar triangle fractal
/// - **Koch Snowflake** - Recursive snowflake pattern
/// - **Dragon Curve** - Space-filling dragon fractal
/// - **Pythagoras Tree** - Square-based fractal tree
/// - **Spiral Tree** - Natural golden angle spiral
///
/// **Features:**
///
/// - Adjustable recursion depth
/// - Branch angle and length ratio controls
/// - Color gradients based on depth
/// - Animation mode (watch it grow)
/// - High-quality anti-aliased rendering
///
/// ## Quick Start
///
/// ### Launch All Demos
///
/// ```
/// mvn exec:java -Dexec.mainClass="d021248.mytry.algorithms.visualization.VisualizationLauncher"
/// ```
///
/// ### Launch Individual Demos
///
/// ```
/// # Pathfinding
/// mvn exec:java -Dexec.mainClass="d021248.mytry.algorithms.visualization.PathfindingVisualizer"
///
/// # Sorting Race
/// mvn exec:java -Dexec.mainClass="d021248.mytry.algorithms.visualization.SortingRaceVisualizer"
///
/// # Recursive Trees
/// mvn exec:java -Dexec.mainClass="d021248.mytry.algorithms.visualization.RecursiveTreeVisualizer"
/// ```
///
/// ## Educational Value
///
/// These visualizations help understand:
///
/// - **Algorithm Behavior** - See how algorithms explore and make
/// decisions
/// - **Time Complexity** - Observe O(n²) vs O(n log n) performance
/// - **Space Efficiency** - Watch frontier/queue growth in pathfinding
/// - **Recursion** - Understand recursive patterns visually
/// - **Optimization** - Compare optimal (A*) vs brute-force (DFS)
///
/// ## Technical Details
///
/// Built using:
///
/// - Java Swing for GUI
/// - javax.swing.Timer for animations
/// - Graphics2D for rendering
/// - Modern Java features (records, switch expressions, pattern matching)
///
/// @since 1.0
package d021248.mytry.algorithms.visualization;
