package d021248.mytry.algorithms.graphstream;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.swing_viewer.ViewPanel;
import org.graphstream.ui.view.Viewer;

public final class ClassHierarchyDemo {
  public static void main() {
    System.setProperty("org.graphstream.ui", "swing");
    Graph graph = new SingleGraph("Class Hierarchy");

    // Add nodes for each class
    graph.addNode("MyTreeScanner").setAttribute("ui.label", "MyTreeScanner");
    graph.addNode("TreeScanner").setAttribute("ui.label", "TreeScanner<Void, Void>");
    graph.addNode("MyTreeScannerTest").setAttribute("ui.label", "MyTreeScannerTest");
    graph.addNode("JavaSourceFromString").setAttribute("ui.label", "JavaSourceFromString");
    graph.addNode("SimpleJavaFileObject").setAttribute("ui.label", "SimpleJavaFileObject");
    graph
        .addNode("CompileSourceInMemoryTest")
        .setAttribute("ui.label", "CompileSourceInMemoryTest");

    // Add edges for inheritance
    graph.addEdge("MyTreeScanner-TreeScanner", "MyTreeScanner", "TreeScanner", true);
    graph.addEdge(
        "JavaSourceFromString-SimpleJavaFileObject",
        "JavaSourceFromString",
        "SimpleJavaFileObject",
        true);

    // Optionally, group by package (color/style)
    graph.getNode("MyTreeScanner").setAttribute("ui.class", "scanner");
    graph.getNode("MyTreeScannerTest").setAttribute("ui.class", "scanner");
    graph.getNode("JavaSourceFromString").setAttribute("ui.class", "memory");
    graph.getNode("CompileSourceInMemoryTest").setAttribute("ui.class", "memory");

    // Add stylesheet for colors
    String styleSheet =
        """
				    node.scanner { fill-color: #4F8A8B; }
				    node.memory { fill-color: #FBD46D; }
				    node { size: 30px; text-size: 18; text-alignment: under; }
				    edge { arrow-shape: arrow; arrow-size: 15px, 8px; }
				""";
    graph.setAttribute("ui.stylesheet", styleSheet);
    graph.setAttribute("ui.quality");
    graph.setAttribute("ui.antialias");

    Viewer viewer = graph.display(false);
    ViewPanel view = (ViewPanel) viewer.getDefaultView();
    view.enableMouseOptions();
    view.resizeFrame(800, 600);

    // Auto-arrange layout
    graph.display(); // Ensures viewer is attached
    graph.setAttribute("ui.layout", "hierarchical");
  }
}
