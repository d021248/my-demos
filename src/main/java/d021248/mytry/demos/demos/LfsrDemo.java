package d021248.mytry.demos;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Supplier;

// https://en.wikipedia.org/wiki/Linear-feedback_shift_register
public final class LfsrDemo {

  public enum TAPS implements Supplier<List<Integer>> {
    P_02(2, List.of(2, 1)),
    P_03(3, List.of(3, 2)),
    P_04(4, List.of(4, 3)),
    P_05(5, List.of(5, 3)),
    P_06(6, List.of(6, 5)),
    P_07(7, List.of(7, 6)),
    P_08(8, List.of(8, 6, 5, 4)),
    P_09(9, List.of(9, 5)),
    P_10(10, List.of(10, 7)),
    P_11(11, List.of(11, 9)),
    P_12(12, List.of(12, 11, 10, 4)),
    P_13(13, List.of(13, 12, 11, 8)),
    P_14(14, List.of(14, 13, 12, 2)),
    P_15(15, List.of(15, 14)),
    P_16(16, List.of(16, 15, 13, 4)),
    P_17(17, List.of(17, 14)),
    P_18(18, List.of(18, 11)),
    P_19(19, List.of(19, 18, 17, 14)),
    P_20(20, List.of(20, 17)),
    P_21(21, List.of(21, 19)),
    P_22(22, List.of(22, 21)),
    P_23(23, List.of(23, 18)),
    P_24(24, List.of(24, 23, 22, 17));

    private final int bitLength;
    private final List<Integer> taps;

    TAPS(int bitLength, List<Integer> taps) {
      this.bitLength = bitLength;
      this.taps = taps;
    }

    @Override
    public List<Integer> get() {
      return taps;
    }

    public int getBitLength() {
      return bitLength;
    }
  }

  private long state = 0;
  private int period = 0;
  private int bitLength;
  private List<Integer> taps;

  public LfsrDemo(long seed, int bitLength, List<Integer> taps) {
    this.bitLength = bitLength;
    this.taps = taps;
    // Ensure the seed is within the bit length
    this.state = seed & ((1 << bitLength) - 1);
  }

  public LfsrDemo(long seed, TAPS taps) {
    this(seed, taps.getBitLength(), taps.get());
  }

  public int getNextBit() {
    // Calculate the new bit using the specified taps
    int newBit = 0;
    for (int tap : taps) {
      newBit ^= (state >> (tap - 1)) & 1;
    }
    // Update the state
    state = ((state << 1) | newBit) & ((1 << bitLength) - 1);

    // Increment the period
    period++;

    return newBit;
  }

  public long getState() {
    return state;
  }

  public int getPeriod() {
    return period;
  }

  public static void main() {
    Map<String, String> allValuesSorted = new TreeMap<>();

    // Example seed, bit length, and taps
    // int seed = 0xACE1; // Seed value

    long seed = 1;
    TAPS taps = TAPS.P_20;
    LfsrDemo lfsr = new LfsrDemo(seed, taps);

    String formatString = "%" + taps.getBitLength() + "s";
    do {
      String value = formatString.formatted(Long.toBinaryString(lfsr.getState())).replace(' ', '0');
      allValuesSorted.put(value, "" + lfsr.getPeriod());
      lfsr.getNextBit();
    } while (lfsr.getState() != seed);

    System.out.println("Map: " + allValuesSorted); // the map contains all the values
    // and the period they were reached

    System.out.println("Period: " + lfsr.getPeriod());
  }
}
