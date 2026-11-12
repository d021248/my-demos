package d021248.mytry.demos.terminal;

/// Central registry of emoji code points used across terminal demos.
///
/// All emoji are defined via {@link Character#toString(int)} so source files
/// remain encoding-independent regardless of the Java compiler's
/// {@code -encoding} flag.
///
public final class Emojis {

  private Emojis() {}

  // ── Faces ──────────────────────────────────────────────────────────────────
  public static final String GRINNING = Character.toString(0x1F600); // 😀
  public static final String TEARS_JOY = Character.toString(0x1F602); // 😂
  public static final String SUNGLASSES = Character.toString(0x1F60E); // 😎
  public static final String ROBOT = Character.toString(0x1F916); // 🤖
  public static final String ALIEN = Character.toString(0x1F47E); // 👾

  // ── Nature ─────────────────────────────────────────────────────────────────
  public static final String RAINBOW = Character.toString(0x1F308); // 🌈
  public static final String CHERRY_BLOSSOM = Character.toString(0x1F338); // 🌸
  public static final String FOUR_LEAF = Character.toString(0x1F340); // 🍀
  public static final String STAR = Character.toString(0x2B50); // ⭐
  public static final String FIRE = Character.toString(0x1F525); // 🔥

  // ── Objects ────────────────────────────────────────────────────────────────
  public static final String PIZZA = Character.toString(0x1F355); // 🍕
  public static final String ROCKET = Character.toString(0x1F680); // 🚀
  public static final String OCTOPUS = Character.toString(0x1F419); // 🐙
  public static final String UNICORN = Character.toString(0x1F984); // 🦄
  public static final String BULB = Character.toString(0x1F4A1); // 💡
  public static final String DICE = Character.toString(0x1F3B2); // 🎲
  public static final String PARTY = Character.toString(0x1F389); // 🎉
  public static final String CAT = Character.toString(0x1F431); // 🐱
  public static final String MUSIC = Character.toString(0x1F3B5); // 🎵
  public static final String SOCCER = Character.toString(0x26BD); // ⚽

  /// Heart emoji (U+2764 + U+FE0F variation selector).
  public static final String HEART = "\u2764\uFE0F"; // ❤️

  // ── Curated demo sets ──────────────────────────────────────────────────────

  /// Set used by {@link EmojiArt}.
  public static final String[] ART_SET = {
    GRINNING, CHERRY_BLOSSOM, STAR, FIRE, FOUR_LEAF, PIZZA, CAT, MUSIC, SOCCER, ROCKET
  };

  /// Extended set used by {@link EmojiRainDemo}.
  public static final String[] RAIN_SET = {
    GRINNING,
    TEARS_JOY,
    SUNGLASSES,
    ROBOT,
    ALIEN,
    RAINBOW,
    PIZZA,
    ROCKET,
    OCTOPUS,
    UNICORN,
    BULB,
    DICE,
    PARTY,
    FIRE,
    STAR
  };
}
