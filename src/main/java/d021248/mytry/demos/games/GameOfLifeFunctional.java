package d021248.mytry.demos.games;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

/// Core logic for Conway's Game of Life using functional patterns.
public final class GameOfLifeFunctional {
  // Functional memoizer: wraps a function in a cached version of itself
  static <I, O> Function<I, O> memoize(Function<I, O> f) {
    Map<I, O> cache = new HashMap<>();
    return x -> cache.computeIfAbsent(x, f); // use cache if computed, otherwise compute
  }

  public static void main() {
    Stream.iterate(
            // Generation 0: the grid as X -> Y -> Boolean
            (Function<Integer, Function<Integer, Boolean>>)
                x -> y -> (x == 1 && y == 0) || (x == 2 && y == 1) || (x >= 0 && x <= 2 && y == 2),

            // The mathematical evolution operator — each new grid is immediately memoized
            grid ->
                memoize(
                    x ->
                        memoize(
                            y ->
                                Stream.of(-1, 0, 1)
                                            .flatMap(
                                                dx ->
                                                    Stream.of(-1, 0, 1)
                                                        .map(dy -> new int[] {dx, dy}))
                                            .filter(d -> d[0] != 0 || d[1] != 0)
                                            .filter(d -> grid.apply(x + d[0]).apply(y + d[1]))
                                            .count()
                                        == 3
                                    || (grid.apply(x).apply(y)
                                        && Stream.of(-1, 0, 1)
                                                .flatMap(
                                                    dx ->
                                                        Stream.of(-1, 0, 1)
                                                            .map(dy -> new int[] {dx, dy}))
                                                .filter(d -> d[0] != 0 || d[1] != 0)
                                                .filter(d -> grid.apply(x + d[0]).apply(y + d[1]))
                                                .count()
                                            == 2))))
        .forEach(
            state -> {
              // print current grid state
              System.out.print("\033[H\033[2J"); // clear console
              Stream.iterate(0, y -> y + 1)
                  .limit(10)
                  .forEach(
                      y -> {
                        Stream.iterate(0, x -> x + 1)
                            .limit(10)
                            .forEach(x -> System.out.print(state.apply(x).apply(y) ? "██" : "░░"));
                        System.out.println();
                      });

              // separator line between simulation steps
              System.out.println("====================");

              try {
                Thread.sleep(300);
              } catch (Exception e) {
              }
            });
  }
}
