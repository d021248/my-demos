package d021248.mytry.algorithms.visualization;

import d021248.mytry.core.lang.gatherers.compress.lz77.LZ77Token;
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
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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

/// LZ77 Compression Visualizer — step-by-step animation of the sliding-window algorithm.
///
/// Panels (top → bottom):
///  1. Stat bar        — step counter and compression ratio
///  2. Input String    — full input with window overlay (past / search / lookahead / match /
///     future)
/// 3. Sliding Window — enlarged search buffer and lookahead, match arc with
/// label
/// 4. Current Token — LITERAL or MATCH badge for this step
/// 5. Token Stream — all emitted tokens so far
/// 6. Controls — input field, speed slider, play / pause / step / reset

@SuppressWarnings({"this-escape", "serial"})
public final class LZ77Visualizer extends JFrame {
  private static final long serialVersionUID = 1L;

  // ── Colour palette (dark theme) ───────────────────────────────────────────
  private static final Color C_BG = new Color(20, 20, 33);
  private static final Color C_PANEL = new Color(29, 29, 46);
  private static final Color C_BORDER = new Color(55, 55, 80);
  private static final Color C_SEARCH = new Color(38, 92, 165);
  private static final Color C_LOOK = new Color(162, 87, 18);
  private static final Color C_MATCH = new Color(45, 178, 78);
  private static final Color C_PAST = new Color(50, 50, 68);
  private static final Color C_FUTURE = new Color(38, 38, 55);
  private static final Color C_LIT = new Color(82, 198, 232);
  private static final Color C_MAT = new Color(242, 188, 48);
  private static final Color C_FG = Color.WHITE;
  private static final Color C_DIM = new Color(128, 128, 156);

  // ── Layout constants ──────────────────────────────────────────────────────
  private static final int CW = 32;
  private static final int CH = 36;
  private static final Font MONO = new Font("Monospaced", Font.BOLD, 14);
  private static final Font SMALL = new Font("Monospaced", Font.PLAIN, 11);
  private static final Font LABELF = new Font("Arial", Font.BOLD, 11);
  private static final Font HEADF = new Font("Arial", Font.BOLD, 15);

  // ── Algorithm params ─────────────────────────────────────────────────────
  private int searchSize = 8;
  private int lookaheadSize = 4;

  // ── Step model ────────────────────────────────────────────────────────────
  record Step(
      String input,
      int consumed, // chars that have left the lookahead
      int searchBufLen, // current search-buffer length
      int lookaheadLen, // current lookahead length
      int matchStartInBuf, // index in searchBuf where match starts (-1 = literal)
      int matchLength, // 0 = literal
      LZ77Token token,
      List<LZ77Token> tokensSoFar) {
    int searchStart() {
      return consumed - searchBufLen;
    }

    int lookaheadStart() {
      return consumed;
    }

    int lookaheadEnd() {
      return consumed + lookaheadLen;
    }

    /// Position in input where the search-buffer match starts; -1 for a literal.
    int matchInInput() {
      return matchStartInBuf < 0 ? -1 : searchStart() + matchStartInBuf;
    }

    boolean isMatch() {
      return matchLength > 0;
    }
  }

  // ── State ─────────────────────────────────────────────────────────────────
  private final List<Step> steps = new ArrayList<>();
  private int cursor = 0;
  private Timer animTimer;

  // ── UI refs ───────────────────────────────────────────────────────────────
  private InputRowPanel inputRowPanel;
  private WinDetailPanel winDetailPanel;
  private TokenInfoPanel tokenInfoPanel;
  private TokenStreamPanel tokenStreamPanel;
  private JLabel stepLbl;
  private JLabel ratioLbl;
  // private JTextField inputField;
  private JSlider speedSlider;
  private JButton playPauseBtn;

  // ── Constructor ───────────────────────────────────────────────────────────
  public LZ77Visualizer() {
    super("LZ77 Compression Visualizer");
    buildUi();
    loadInput("abracadabra!");
    setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    pack();
    setLocationRelativeTo(null);
    setVisible(true);
  }

  // ── UI construction ───────────────────────────────────────────────────────
  private void buildUi() {
    getContentPane().setBackground(C_BG);
    setLayout(new BorderLayout());

    // stat bar
    JPanel statBar = row(new FlowLayout(FlowLayout.LEFT, 14, 8));
    statBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, C_BORDER));
    stepLbl = hlabel("Step — / —");
    ratioLbl = hlabel("Tokens: —   Ratio: —");
    statBar.add(hlabel("🗜  LZ77 Compression Visualizer"));
    statBar.add(Box.createHorizontalStrut(50));
    statBar.add(stepLbl);
    statBar.add(Box.createHorizontalStrut(30));
    statBar.add(ratioLbl);
    add(statBar, BorderLayout.NORTH);

    // main content
    JPanel main = row(null);
    main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
    main.setBorder(new EmptyBorder(10, 16, 10, 16));

    inputRowPanel = new InputRowPanel();
    winDetailPanel = new WinDetailPanel();
    tokenInfoPanel = new TokenInfoPanel();
    tokenStreamPanel = new TokenStreamPanel();

    main.add(section("Input String", inputRowPanel, 65));
    main.add(Box.createVerticalStrut(8));
    main.add(section("Sliding Window", winDetailPanel, 125));
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
    JTextField inputField;
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

  // ── Algorithm ─────────────────────────────────────────────────────────────
  private void loadInput(String input) {
    if (input.isEmpty()) return;
    stopAnimation();
    steps.clear();
    cursor = 0;

    StringBuilder search = new StringBuilder();
    StringBuilder look = new StringBuilder();
    int pos = 0;
    int consumed = 0;
    List<LZ77Token> all = new ArrayList<>();

    while (pos < input.length() || !look.isEmpty()) {
      while (pos < input.length() && look.length() < lookaheadSize)
        look.append(input.charAt(pos++));
      if (look.isEmpty()) break;

      String sb = search.toString();
      String la = look.toString();
      int bestOff = -1;
      int bestLen = 0;
      for (int len = la.length(); len >= 1; len--) {
        int idx = sb.lastIndexOf(la.substring(0, len));
        if (idx != -1) {
          bestOff = sb.length() - idx;
          bestLen = len;
          break;
        }
      }

      LZ77Token tok;
      int msib;
      int advance;
      if (bestLen > 1) {
        tok = new LZ77Token.Match(bestOff, bestLen);
        msib = sb.length() - bestOff;
        advance = bestLen;
      } else {
        tok = new LZ77Token.Literal(la.charAt(0));
        msib = -1;
        advance = 1;
      }
      all.add(tok);
      steps.add(
          new Step(
              input,
              consumed,
              search.length(),
              look.length(),
              msib,
              bestLen > 1 ? bestLen : 0,
              tok,
              List.copyOf(all)));

      for (int i = 0; i < advance; i++) {
        char c = look.charAt(0);
        if (search.length() >= searchSize) search.deleteCharAt(0);
        search.append(c);
        look.deleteCharAt(0);
        consumed++;
      }
    }
    updateDisplay();
  }

  // ── Animation ─────────────────────────────────────────────────────────────
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
                } else stopAnimation();
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

  // ── Display ───────────────────────────────────────────────────────────────
  private void updateDisplay() {
    if (steps.isEmpty()) return;
    Step s = steps.get(cursor);
    stepLbl.setText("Step %d / %d".formatted(cursor + 1, steps.size()));

    int charsEncoded = 0;
    for (LZ77Token t : s.tokensSoFar())
      charsEncoded += (t instanceof LZ77Token.Match m) ? m.length() : 1;
    double ratio = s.tokensSoFar().size() / (double) Math.max(1, charsEncoded);
    ratioLbl.setText(
        String.format(Locale.ROOT, "Tokens: %d   Ratio: %.2f", s.tokensSoFar().size(), ratio));

    inputRowPanel.repaint();
    winDetailPanel.repaint();
    tokenInfoPanel.repaint();
    tokenStreamPanel.update(s.tokensSoFar());
  }

  // ═════════════════════════════════════════════════════════════════════════
  // Panel: Input Row — full input string with colour-coded window overlay
  // ═════════════════════════════════════════════════════════════════════════
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
      int oy = 20;

      // Subtle window bracket
      if (s.searchStart() < s.lookaheadEnd()) {
        g.setColor(new Color(100, 130, 220, 28));
        g.fillRoundRect(
            ox + s.searchStart() * CW - 2,
            oy - 3,
            (s.lookaheadEnd() - s.searchStart()) * CW + 4,
            CH + 6,
            8,
            8);
      }

      // Character cells
      for (int i = 0; i < n; i++) {
        int mx = s.matchInInput();
        boolean inSM = s.isMatch() && mx >= 0 && i >= mx && i < mx + s.matchLength();
        boolean inLM =
            s.isMatch() && i >= s.lookaheadStart() && i < s.lookaheadStart() + s.matchLength();
        Color bg;
        if (inSM) bg = C_MATCH;
        else if (inLM) bg = C_MATCH;
        else if (i >= s.searchStart() && i < s.lookaheadStart()) bg = C_SEARCH;
        else if (i >= s.lookaheadStart() && i < s.lookaheadEnd()) bg = C_LOOK;
        else if (i < s.searchStart()) bg = C_PAST;
        else bg = C_FUTURE;
        cell(g, ox + i * CW, oy, CW - 3, CH, bg, String.valueOf(s.input().charAt(i)));
      }

      // Small region labels below cells
      g.setFont(SMALL);
      FontMetrics fm = g.getFontMetrics();
      int ss = s.searchStart();
      int sl = s.lookaheadStart();
      int le = s.lookaheadEnd();
      if (ss < sl) {
        g.setColor(C_SEARCH.brighter());
        String lb = "search buffer";
        g.drawString(lb, ox + ss * CW + ((sl - ss) * CW - fm.stringWidth(lb)) / 2, oy + CH + 13);
      }
      if (sl < le) {
        g.setColor(C_LOOK.brighter());
        String lb = "lookahead";
        g.drawString(lb, ox + sl * CW + ((le - sl) * CW - fm.stringWidth(lb)) / 2, oy + CH + 13);
      }
    }
  }

  // ═════════════════════════════════════════════════════════════════════════
  // Panel: Window Detail — enlarged search/lookahead with match arc
  // ═════════════════════════════════════════════════════════════════════════
  class WinDetailPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    WinDetailPanel() {
      setBackground(C_PANEL);
    }

    @Override
    protected void paintComponent(Graphics g0) {
      super.paintComponent(g0);
      if (steps.isEmpty()) return;
      Graphics2D g = aa(g0);
      Step s = steps.get(cursor);
      int maxW = (searchSize + lookaheadSize) * CW + 14;
      int ox = Math.max(8, (getWidth() - maxW) / 2);
      int laOX = ox + searchSize * CW + 14;
      int oy = 26;

      // Section labels
      g.setFont(LABELF);
      g.setColor(C_SEARCH.brighter());
      g.drawString("Search Buffer", ox, oy - 7);
      g.setColor(C_LOOK.brighter());
      g.drawString("Lookahead", laOX, oy - 7);

      // Divider
      g.setColor(C_BORDER);
      g.fillRect(ox + searchSize * CW + 6, oy - 9, 2, CH + 18);

      // Search-buffer cells
      String sb = s.input().substring(s.searchStart(), s.lookaheadStart());
      for (int i = 0; i < searchSize; i++) {
        if (i < sb.length()) {
          boolean inM =
              s.isMatch()
                  && s.matchStartInBuf() >= 0
                  && i >= s.matchStartInBuf()
                  && i < s.matchStartInBuf() + s.matchLength();
          cell(
              g,
              ox + i * CW,
              oy,
              CW - 2,
              CH,
              inM ? C_MATCH : C_SEARCH,
              String.valueOf(sb.charAt(i)));
        } else {
          emptyCell(g, ox + i * CW, oy);
        }
      }

      // Lookahead cells
      String la = s.input().substring(s.lookaheadStart(), s.lookaheadEnd());
      for (int i = 0; i < lookaheadSize; i++) {
        if (i < la.length()) {
          boolean inM = s.isMatch() && i < s.matchLength();
          cell(
              g,
              laOX + i * CW,
              oy,
              CW - 2,
              CH,
              inM ? C_MATCH : C_LOOK,
              String.valueOf(la.charAt(i)));
        } else {
          emptyCell(g, laOX + i * CW, oy);
        }
      }

      // Match arc + label
      if (s.isMatch() && s.matchStartInBuf() >= 0) {
        int ml = s.matchLength();
        int srcX = ox + s.matchStartInBuf() * CW + (ml * CW) / 2;
        int dstX = laOX + (ml * CW) / 2;
        int arcY = oy + CH + 8;

        g.setColor(C_MATCH.brighter());
        g.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        Path2D path = new Path2D.Float();
        path.moveTo(srcX, arcY);
        path.curveTo(srcX, arcY + 20, dstX, arcY + 20, dstX, arcY);
        g.draw(path);

        // Arrowhead at dstX
        g.fillPolygon(
            new int[] {dstX, dstX - 5, dstX + 5}, new int[] {arcY - 1, arcY + 7, arcY + 7}, 3);

        LZ77Token.Match m = (LZ77Token.Match) s.token();
        String copied = s.input().substring(s.lookaheadStart(), s.lookaheadStart() + m.length());
        String lbl =
            "offset=%d  length=%d  →  copies \"%s\"".formatted(m.offset(), m.length(), copied);
        g.setFont(SMALL);
        g.setColor(C_MATCH.brighter());
        FontMetrics fm = g.getFontMetrics();
        g.drawString(lbl, (srcX + dstX) / 2 - fm.stringWidth(lbl) / 2, arcY + 32);
      }
    }
  }

  // ═════════════════════════════════════════════════════════════════════════
  // Panel: Token Info — prominent badge for the current token
  // ═════════════════════════════════════════════════════════════════════════
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

      String text;
      Color col;
      if (s.isMatch()) {
        LZ77Token.Match m = (LZ77Token.Match) s.token();
        String copied = s.input().substring(s.lookaheadStart(), s.lookaheadStart() + m.length());
        text =
            "▶  MATCH   offset=%d   length=%d   →  copies \"%s\""
                .formatted(m.offset(), m.length(), copied);
        col = C_MAT;
      } else {
        char c = ((LZ77Token.Literal) s.token()).value();
        text = "▶  LITERAL  '%c'   →  no match found in search buffer".formatted(c);
        col = C_LIT;
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
          text,
          (getWidth() - fm.stringWidth(text)) / 2,
          by + (bh + fm.getAscent() - fm.getDescent()) / 2);
    }
  }

  // ═════════════════════════════════════════════════════════════════════════
  // Panel: Token Stream — flowing list of all emitted tokens
  // ═════════════════════════════════════════════════════════════════════════
  class TokenStreamPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private List<LZ77Token> tokens = List.of();

    TokenStreamPanel() {
      setBackground(C_PANEL);
    }

    void update(List<LZ77Token> t) {
      tokens = new ArrayList<>(t);
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
        LZ77Token t = tokens.get(i);
        boolean hilit = (i == hi);
        String lbl;
        Color bg;
        if (t instanceof LZ77Token.Literal lit) {
          lbl = "'" + lit.value() + "'";
          bg = hilit ? C_LIT : C_LIT.darker().darker();
        } else {
          LZ77Token.Match m = (LZ77Token.Match) t;
          lbl = "(" + m.offset() + "," + m.length() + ")";
          bg = hilit ? C_MAT : C_MAT.darker().darker();
        }
        int tw = fm.stringWidth(lbl) + 12;
        int th = 26;
        int ty = cy - th / 2;
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
        if (x > getWidth() - 50) {
          g.setFont(SMALL);
          g.setColor(C_DIM);
          g.drawString("  +" + (tokens.size() - i - 1) + " more", x, cy + 5);
          break;
        }
      }
    }
  }

  // ── Drawing helpers ───────────────────────────────────────────────────────
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

  private void emptyCell(Graphics2D g, int x, int y) {
    g.setColor(new Color(40, 40, 58));
    g.fillRoundRect(x, y, CW - 2, CH, 6, 6);
    g.setColor(new Color(56, 56, 75));
    g.drawRoundRect(x, y, CW - 2, CH, 6, 6);
    g.setFont(MONO);
    g.setColor(C_DIM);
    FontMetrics fm = g.getFontMetrics();
    String dot = "·";
    g.drawString(
        dot,
        x + (CW - 2 - fm.stringWidth(dot)) / 2,
        y + (CH + fm.getAscent() - fm.getDescent()) / 2);
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

  // ── Entry point ───────────────────────────────────────────────────────────
  public static void main() {
    SwingUtilities.invokeLater(LZ77Visualizer::new);
  }
}
