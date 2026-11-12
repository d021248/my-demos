package d021248.mytry.algorithms.graphstream;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.view.Viewer;

public final class GraphStreamBasicsDemo {

  public static void main() {

    System.setProperty("org.graphstream.ui", "swing");

    Graph graph = new SingleGraph("Tutorial 1");
    graph.addNode("A");
    graph.addNode("B");
    graph.addNode("C");
    graph.addEdge("AB", "A", "B");
    graph.addEdge("BC", "B", "C");
    graph.addEdge("CA", "C", "A");

    Viewer viewer = graph.display();
    viewer.enableAutoLayout();
    viewer.getDefaultView().enableMouseOptions();

    System.out.println("Hello World!");
  }
}
