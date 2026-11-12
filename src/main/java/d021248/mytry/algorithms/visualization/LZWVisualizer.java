package d021248.mytry.algorithms.visualization;

import d021248.mytry.core.lang.gatherers.compress.lzw.LZWToken;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

/// LZW Compression Visualizer — step-by-step animation of the LZW sliding-dictionary algorithm.
///
/// The visualizer uses a compact alphabet (unique characters of the input) so the dictionary
/// stays small and readable. Each animation step corresponds to one token emission.
///
/// Panels (top → bottom):
///
/// 1. Stat bar — step, dict size, compression ratio
/// 2. Input String — character cells (past / accumulated / trigger / future)
/// 3. Dictionary — all entries as colour-coded badges (initial / compound / NEW)
/// 4. Current Token — what is being emitted and added
/// 5. Token Stream — all tokens emitted so far
/// 6. Controls — input field, speed, play/pause/step/reset

@SuppressWarnings({"this-escape", "serial"})
public final class LZWVisualizer extends JFrame {
  private static final long serialVersionUID = 1L;

  // ── Colour palette ────────────────────────────────────────────────────
  private static final Color C_BG = new Color(20, 20, 33);
  private static final Color C_PANEL = new Color(29, 29, 46);
  private static final Color C_BORDER = new Color(55, 55, 80);
  private static final Color C_ACCUM = new Color(38, 92, 165); // current accumulated
  private static final Color C_TRIG = new Color(162, 87, 18); // trigger char
  private static final Color C_MATCH = new Color(45, 178, 78); // new dict entry highlight
  private static final Color C_PAST = new Color(50, 50, 68); // already emitted
  private static final Color C_FUTURE = new Color(38, 38, 55); // not yet seen
  private static final Color C_DICT_INIT = new Color(55, 55, 75); // initial single-char entries
  private static final Color C_DICT_COMP = new Color(25, 90, 100); // compound entries (teal)
  private static final Color C_LIT = new Color(82, 198, 232); // single-char token badge
  private static final Color C_MAT = new Color(242, 188, 48); // compound token badge
  private static final Color C_FG = Color.WHITE;
  private static final Color C_DIM = new Color(128, 128, 156);

  // ── Layout constants ──────────────────────────────────────────────────
  private static final int CW = 32;
  private static final int CH = 36;
  private static final Font MONO = new Font("Monospaced", Font.BOLD, 14);
  private static final Font SMALL = new Font("Monospaced", Font.PLAIN, 11);
  private static final Font LABELF = new Font("Arial", Font.BOLD, 11);
  private static final Font HEADF = new Font("Arial", Font.BOLD, 15);

  // ── Step model ────────────────────────────────────────────────────────

  /// A single entry in the growing dictionary.
  record DictEntry(int code, String value, boolean isNewAtThisStep) {}

  /// One animation step = one token emission.
  record Step(
      String input,
      int accStart, // where accumulated string starts in input
      String emitted, // accumulated string that was emitted
      boolean isFinal, // true = finisher (no trigger char, no new entry)
      char triggerChar, // only valid when !isFinal
      LZWToken token,
      String newEntry, // new dict entry added (null when isFinal)
      int newCode, // code for newEntry (-1 when isFinal)
      int alphabetSize, // number of initial single-char entries
      List<DictEntry> dictSnapshot, // dict state after this step
      List<LZWToken> tokensSoFar) {

    /// Position of the trigger character in `input`.
    int triggerPos() {
      return isFinal ? -1 : accStart + emitted.length();
    }
  }

  // ── State ─────────────────────────────────────────────────────────────
  private final List<Step> steps = new ArrayList<>();
  private int cursor = 0;
  private Timer animTimer;

  // ── UI refs ───────────────────────────────────────────────────────────
  private InputRowPanel inputRowPanel;
  private DictPanel dictPanel;
  private TokenInfoPanel tokenInfoPanel;
  private TokenStreamPanel tokenStreamPanel;
  private JLabel stepLbl;
  private JLabel ratioLbl;
  private JTextField inputField;
  private JSlider speedSlider;
  private JButton playPauseBtn;

  // ── Constructor ───────────────────────────────────────────────────────
  public LZWVisualizer() {
    super("LZW Compression Visualizer");
    buildUi();
    loadInput("abracadabra!");
    setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    pack();
    setLocationRelativeTo(null);
    setVisible(true);
  }

  // ── UI construction ───────────────────────────────────────────────────
  private void buildUi() {
    getContentPane().setBackground(C_BG);
    setLayout(new BorderLayout());

    JPanel statBar = row(new FlowLayout(FlowLayout.LEFT, 14, 8));
    statBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, C_BORDER));
    stepLbl = hlabel("Step — / —");
    ratioLbl = hlabel("Dict: —   Tokens: —   Ratio: —");
    statBar.add(hlabel("📚  LZW Compression Visualizer"));
    statBar.add(Box.createHorizontalStrut(40));
    statBar.add(stepLbl);
    statBar.add(Box.createHorizontalStrut(20));
    statBar.add(ratioLbl);
    add(statBar, BorderLayout.NORTH);

    JPanel main = row(null);
    main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
    main.setBorder(new EmptyBorder(10, 16, 10, 16));

    inputRowPanel = new InputRowPanel();
    dictPanel = new DictPanel();
    tokenInfoPanel = new TokenInfoPanel();
    tokenStreamPanel = new TokenStreamPanel();

    main.add(section("Input String", inputRowPanel, 68));
    main.add(Box.createVerticalStrut(8));
    main.add(section("Dictionary (grows as new substrings are found)", dictPanel, 120));
    main.add(Box.createVerticalStrut(8));
    main.add(section("Current Token", tokenInfoPanel, 56));
    main.add(Box.createVerticalStrut(8));
    main.add(section("Token Stream", tokenStreamPanel, 52));
    add(main, BorderLayout.CENTER);

    add(buildControls(), BorderLayout.SOUTH);
  }

  private JPanel buildControls() {
    JPanel p = row(new FlowLayout(FlowLayout.CENTER, 10, 10));
    p.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, C_BORDER));

    inputField = new JTextField("abracadabra!", 18);
    inputField.setFont(MONO);
    inputField.setBackground(new Color(36, 36, 55));
    inputField.setForeground(C_FG);
    inputField.setCaretColor(C_FG);
    inputField.setBorder(BorderFactory.createLineBorder(C_BORDER));
    inputField.addActionListener(e -> loadInput(inputField.getText().trim()));

    speedSlider = new JSlider(1, 20, 5);
    speedSlider.setBackground(C_BG);
    speedSlider.setPreferredSize(new Dimension(110, 26));

    playPauseBtn = btn("▶ Play");
    playPauseBtn.addActionListener(e -> togglePlay());

    JButton stepBtn = btn("⏭ Step");
    stepBtn.addActionListener(e -> doStep());

    JButton resetBtn = btn("↺ Reset");
    resetBtn.addActionListener(e -> loadInput(inputField.getText().trim()));

    p.add(dim("Input:"));
    p.add(inputField);
    p.add(dim("Speed:"));
    p.add(speedSlider);
    p.add(playPauseBtn);
    p.add(stepBtn);
    p.add(resetBtn);
    return p;
  }

  // ── Algorithm ─────────────────────────────────────────────────────────
  private void loadInput(String input) {
    if (input.isEmpty()) return;
    stopAnimation();
    steps.clear();
    cursor = 0;

    // Build compact alphabet from unique chars
    String alphabet =
        input
            .chars()
            .distinct()
            .sorted()
            .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
            .toString();

    LinkedHashMap<String, Integer> dict = new LinkedHashMap<>();
    int code = 0;
    for (char ch : alphabet.toCharArray()) {
      dict.put(String.valueOf(ch), code++);
    }
    int alphabetSize = dict.size();

    String current = "";
    int accStart = 0;
    List<LZWToken> allTokens = new ArrayList<>();

    for (int i = 0; i <= input.length(); i++) {
      // Finisher: emit remaining accumulated string
      if (i == input.length()) {
        if (!current.isEmpty()) {
          LZWToken tok = new LZWToken(dict.get(current), current);
          allTokens.add(tok);
          List<DictEntry> snap = dictSnapshot(dict, alphabetSize, null);
          steps.add(
              new Step(
                  input,
                  accStart,
                  current,
                  true,
                  '\0',
                  tok,
                  null,
                  -1,
                  alphabetSize,
                  snap,
                  List.copyOf(allTokens)));
        }
        break;
      }

      char c = input.charAt(i);
      String wc = current + c;

      if (dict.containsKey(wc)) {
        current = wc;
      } else {
        if (!current.isEmpty()) {
          LZWToken tok = new LZWToken(dict.get(current), current);
          allTokens.add(tok);
          int newCode = dict.size();
          dict.put(wc, newCode);

          List<DictEntry> snap = dictSnapshot(dict, alphabetSize, wc);
          steps.add(
              new Step(
                  input,
                  accStart,
                  current,
                  false,
                  c,
                  tok,
                  wc,
                  newCode,
                  alphabetSize,
                  snap,
                  List.copyOf(allTokens)));
        }
        current = String.valueOf(c);
        accStart = i;
      }
    }
    updateDisplay();
  }

  private List<DictEntry> dictSnapshot(
      LinkedHashMap<String, Integer> dict, int alphabetSize, String newKey) {
    List<DictEntry> list = new ArrayList<>(dict.size());
    for (Map.Entry<String, Integer> e : dict.entrySet()) {
      list.add(new DictEntry(e.getValue(), e.getKey(), e.getKey().equals(newKey)));
    }
    return Collections.unmodifiableList(list);
  }

  // ── Animation ─────────────────────────────────────────────────────────
  private void togglePlay() {
    if (animTimer != null && animTimer.isRunning()) {
      stopAnimation();
    } else {
      int delay = Math.max(80, 1200 - speedSlider.getValue() * 55);
      animTimer =
          new Timer(
              delay,
              e -> {
                if (cursor < steps.size() - 1) {
                  cursor++;
                  updateDisplay();
                } else {
                  stopAnimation();
                }
              });
      animTimer.start();
      playPauseBtn.setText("⏸ Pause");
    }
  }

  private void stopAnimation() {
    if (animTimer != null) animTimer.stop();
    playPauseBtn.setText("▶ Play");
  }

  private void doStep() {
    stopAnimation();
    if (cursor < steps.size() - 1) {
      cursor++;
      updateDisplay();
    }
  }

  // ── Display ───────────────────────────────────────────────────────────
  private void updateDisplay() {
    if (steps.isEmpty()) return;
    Step s = steps.get(cursor);
    stepLbl.setText("Step %d / %d".formatted(cursor + 1, steps.size()));

    int charsEncoded = 0;
    for (LZWToken t : s.tokensSoFar()) {
      charsEncoded += t.entry().length();
    }
    double ratio = s.tokensSoFar().size() / (double) Math.max(1, charsEncoded);
    ratioLbl.setText(
        String.format(
            Locale.ROOT,
            "Dict: %d   Tokens: %d   Ratio: %.2f",
            s.dictSnapshot().size(),
            s.tokensSoFar().size(),
            ratio));

    inputRowPanel.repaint();
    dictPanel.repaint();
    tokenInfoPanel.repaint();
    tokenStreamPanel.update(s.tokensSoFar(), s.alphabetSize());
  }

  // ═══════════════════════════════════════════════════════════════════════
  // Panel: Input Row
  // ═══════════════════════════════════════════════════════════════════════
  class InputRowPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    InputRowPanel() {
      setBackground(C_PANEL);
    }

    @Override
    protected void paintComponent(Graphics g0) {
      super.paintComponent(g0);
      if (steps.isEmpty()) return;
      Graphics2D g = aa(g0);
      Step s = steps.get(cursor);
      int n = s.input().length();
      int ox = Math.max(8, (getWidth() - n * CW) / 2);
      int oy = 18;

      // Highlight window (accumulated + trigger)
      int winLen = s.emitted().length() + (s.isFinal() ? 0 : 1);
      if (winLen > 0) {
        g.setColor(new Color(100, 130, 220, 28));
        g.fillRoundRect(ox + s.accStart() * CW - 2, oy - 3, winLen * CW + 4, CH + 6, 8, 8);
      }

      for (int i = 0; i < n; i++) {
        boolean inAccum = i >= s.accStart() && i < s.accStart() + s.emitted().length();
        boolean isTrig = !s.isFinal() && i == s.triggerPos();
        Color bg;
        if (inAccum) bg = C_ACCUM;
        else if (isTrig) bg = C_TRIG;
        else if (i < s.accStart()) bg = C_PAST;
        else bg = C_FUTURE;
        cell(g, ox + i * CW, oy, CW - 3, CH, bg, String.valueOf(s.input().charAt(i)));
      }

      // Labels
      g.setFont(SMALL);
      int ae = s.accStart() + s.emitted().length();
      if (s.accStart() < ae) {
        g.setColor(C_ACCUM.brighter());
        String lb = "accumulated";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(
            lb,
            ox + s.accStart() * CW + ((ae - s.accStart()) * CW - fm.stringWidth(lb)) / 2,
            oy + CH + 14);
      }
      if (!s.isFinal()) {
        g.setColor(C_TRIG.brighter());
        g.drawString(
            "trigger",
            ox + s.triggerPos() * CW + (CW - g.getFontMetrics().stringWidth("trigger")) / 2,
            oy + CH + 14);
      }
    }
  }

  // ═══════════════════════════════════════════════════════════════════════
  // Panel: Dictionary
  // ═══════════════════════════════════════════════════════════════════════
  class DictPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    DictPanel() {
      setBackground(C_PANEL);
    }

    @Override
    protected void paintComponent(Graphics g0) {
      super.paintComponent(g0);
      if (steps.isEmpty()) return;
      Graphics2D g = aa(g0);
      Step s = steps.get(cursor);

      g.setFont(SMALL);
      FontMetrics fm = g.getFontMetrics();
      int badgeH = 22;
      int padX = 6;
      int padY = 2;
      int x = 10;
      int y = 14;
      int rowH = badgeH + padY + 4;

      for (DictEntry e : s.dictSnapshot()) {
        String lbl = e.code() + ":" + e.value();
        int w = fm.stringWidth(lbl) + padX * 2;

        if (x + w > getWidth() - 10) {
          x = 10;
          y += rowH;
        }

        Color bg;
        if (e.isNewAtThisStep()) bg = C_MATCH;
        else if (e.code() < s.alphabetSize()) bg = C_DICT_INIT;
        else bg = C_DICT_COMP;

        g.setColor(bg);
        g.fillRoundRect(x, y, w, badgeH, 6, 6);
        if (e.isNewAtThisStep()) {
          g.setColor(C_MATCH.brighter());
          g.setStroke(new BasicStroke(1.8f));
          g.drawRoundRect(x, y, w, badgeH, 6, 6);
          g.setStroke(new BasicStroke(1f));
        }
        g.setColor(C_FG);
        g.drawString(lbl, x + padX, y + (badgeH + fm.getAscent() - fm.getDescent()) / 2);

        // NEW label above latest entry
        if (e.isNewAtThisStep()) {
          g.setFont(new Font("Arial", Font.BOLD, 9));
          g.setColor(C_MATCH.brighter());
          g.drawString("NEW", x, y - 2);
          g.setFont(SMALL);
        }

        x += w + 5;
      }

      // Legend (bottom right)
      int ly = getHeight() - 14;
      int lx = getWidth() - 200;
      g.setFont(new Font("Arial", Font.PLAIN, 10));
      drawLegendDot(g, lx, ly, C_DICT_INIT, "single-char");
      drawLegendDot(g, lx + 80, ly, C_DICT_COMP, "compound");
      drawLegendDot(g, lx + 155, ly, C_MATCH, "new");
    }

    private void drawLegendDot(Graphics2D g, int x, int y, Color c, String label) {
      g.setColor(c);
      g.fillRoundRect(x, y - 8, 10, 10, 4, 4);
      g.setColor(C_DIM);
      g.drawString(label, x + 14, y);
    }
  }

  // ═══════════════════════════════════════════════════════════════════════
  // Panel: Token Info
  // ═══════════════════════════════════════════════════════════════════════
  class TokenInfoPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    TokenInfoPanel() {
      setBackground(C_PANEL);
    }

    @Override
    protected void paintComponent(Graphics g0) {
      super.paintComponent(g0);
      if (steps.isEmpty()) return;
      Graphics2D g = aa(g0);
      Step s = steps.get(cursor);

      boolean isCompound = s.token().entry().length() > 1;
      Color col = isCompound ? C_MAT : C_LIT;

      String main;
      if (s.isFinal()) {
        main =
            "▶  EMIT  [%d : \"%s\"]   (final — no new dict entry)"
                .formatted(s.token().code(), s.token().entry());
      } else {
        main =
            "▶  EMIT  [%d : \"%s\"]     +  add  \"%s\" → code %d"
                .formatted(s.token().code(), s.token().entry(), s.newEntry(), s.newCode());
      }

      int bh = 34;
      int by = (getHeight() - bh) / 2;
      g.setColor(col.darker().darker());
      g.fillRoundRect(16, by, getWidth() - 32, bh, 10, 10);
      g.setColor(col);
      g.setStroke(new BasicStroke(1.5f));
      g.drawRoundRect(16, by, getWidth() - 32, bh, 10, 10);
      g.setFont(HEADF);
      g.setColor(C_FG);
      FontMetrics fm = g.getFontMetrics();
      g.drawString(
          main,
          (getWidth() - fm.stringWidth(main)) / 2,
          by + (bh + fm.getAscent() - fm.getDescent()) / 2);
    }
  }

  // ═══════════════════════════════════════════════════════════════════════
  // Panel: Token Stream
  // ═══════════════════════════════════════════════════════════════════════
  class TokenStreamPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private List<LZWToken> tokens = List.of();
    private int alphabetSize = 0;

    TokenStreamPanel() {
      setBackground(C_PANEL);
    }

    void update(List<LZWToken> t, int alphabetSize) {
      this.tokens = new ArrayList<>(t);
      this.alphabetSize = alphabetSize;
      repaint();
    }

    @Override
    protected void paintComponent(Graphics g0) {
      super.paintComponent(g0);
      Graphics2D g = aa(g0);
      g.setFont(SMALL);
      FontMetrics fm = g.getFontMetrics();
      int x = 10;
      int cy = getHeight() / 2;
      int hi = tokens.size() - 1;
      for (int i = 0; i < tokens.size(); i++) {
        LZWToken t = tokens.get(i);
        boolean hilit = (i == hi);
        boolean isCompound = t.entry().length() > 1;
        Color baseCol = isCompound ? C_MAT : C_LIT;
        Color bg = hilit ? baseCol : baseCol.darker().darker();

        String lbl = "[" + t.code() + ":" + t.entry() + "]";
        int tw = fm.stringWidth(lbl) + 12, th = 26, ty = cy - th / 2;

        g.setColor(bg);
        g.fillRoundRect(x, ty, tw, th, 8, 8);
        if (hilit) {
          g.setColor(bg.brighter());
          g.setStroke(new BasicStroke(1.8f));
          g.drawRoundRect(x, ty, tw, th, 8, 8);
          g.setStroke(new BasicStroke(1f));
        }
        g.setColor(C_FG);
        g.drawString(lbl, x + 6, cy + (fm.getAscent() - fm.getDescent()) / 2);
        x += tw + 5;
        if (x > getWidth() - 40) {
          g.setColor(C_DIM);
          g.drawString("+" + (tokens.size() - i - 1) + " more", x, cy + 5);
          break;
        }
      }
    }
  }

  // ── Draw helpers ──────────────────────────────────────────────────────
  private void cell(Graphics2D g, int x, int y, int w, int h, Color bg, String ch) {
    g.setColor(bg);
    g.fillRoundRect(x, y, w, h, 6, 6);
    g.setColor(bg.brighter());
    g.drawRoundRect(x, y, w, h, 6, 6);
    g.setFont(MONO);
    g.setColor(C_FG);
    FontMetrics fm = g.getFontMetrics();
    g.drawString(
        ch, x + (w - fm.stringWidth(ch)) / 2, y + (h + fm.getAscent() - fm.getDescent()) / 2);
  }

  private Graphics2D aa(Graphics g) {
    Graphics2D g2 = (Graphics2D) g;
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setRenderingHint(
        RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    return g2;
  }

  private JPanel row(LayoutManager lm) {
    JPanel p = lm != null ? new JPanel(lm) : new JPanel();
    p.setBackground(C_BG);
    return p;
  }

  private JLabel hlabel(String t) {
    JLabel l = new JLabel(t);
    l.setFont(HEADF);
    l.setForeground(C_FG);
    return l;
  }

  private JLabel dim(String t) {
    JLabel l = new JLabel(t);
    l.setFont(LABELF);
    l.setForeground(C_DIM);
    return l;
  }

  private JButton btn(String t) {
    JButton b = new JButton(t);
    b.setFont(LABELF);
    b.setBackground(new Color(46, 54, 84));
    b.setForeground(C_FG);
    b.setFocusPainted(false);
    b.setBorderPainted(false);
    b.setOpaque(true);
    return b;
  }

  private JPanel section(String title, JPanel content, int h) {
    JPanel p = row(new BorderLayout(0, 4));
    JLabel l = new JLabel(title);
    l.setFont(LABELF);
    l.setForeground(C_DIM);
    content.setBorder(BorderFactory.createLineBorder(C_BORDER));
    content.setPreferredSize(new Dimension(900, h));
    p.add(l, BorderLayout.NORTH);
    p.add(content, BorderLayout.CENTER);
    return p;
  }

  // ── Entry point ───────────────────────────────────────────────────────
  public static void main() {
    SwingUtilities.invokeLater(LZWVisualizer::new);
  }
}
