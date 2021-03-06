package de.topobyte.jterm.core;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;

import de.topobyte.jterm.themes.Palette;
import de.topobyte.jterm.themes.Themes;

public class TerminalWidget extends JComponent implements TerminalClosedListener
{

	private static final long serialVersionUID = 6198367149770918349L;

	private boolean DEBUG_RENDERING_TIME = false;
	private boolean DEBUG_NEWLINES = false;
	private boolean DEBUG_HISTORY = false;
	private boolean DEBUG_SET_COLUMN = false;
	private boolean DEBUG_CURSOR = false;
	private boolean DEBUG_ERASE_DELETE = false;
	private boolean DEBUG_SCROLLING_AREA = false;

	private Color colorRaster = new Color(0x333333);
	private Color colorCursor = new Color(0x99ff0000, true);
	private Color colorScrollingRegion = new Color(0x99ff0000, true);

	private boolean drawFrame = false;
	private boolean drawRaster = false;
	private boolean drawScrollingArea = false;

	private Terminal terminal;

	private String fontname = "DejaVu Sans Mono";
	// private String fontname = "Andale Mono";
	private int fontsize = 12;

	private Font font;
	private Font fontBold;

	int charWidth = 7;
	int charHeight = 11;
	private int descent = 3;

	private Cache<Pixel, BufferedImage> cache = new Cache<>(300);

	private Screen screen;
	private Screen screenNormal;
	private Screen screenAlternate;

	private History history;

	private static final int DEFAULT_FG = 16;
	private static final int DEFAULT_BG = 17;

	private int fg = DEFAULT_FG;
	private int bg = DEFAULT_BG;
	private boolean highlighted = false;
	private boolean reverse = false;
	private boolean fgBright = false;
	private boolean bgBright = false;

	private int theme = 0;
	private Palette palette = Themes.THEMES.get(theme);

	boolean decCkm = false; // Cursor Keys Mode
	boolean decAwm = false; // Auto Wrap Mode
	private boolean cursorVisible = true;

	private Semaphore mutex = new Semaphore(1);

	public TerminalWidget(String pwd)
	{
		terminal = new Terminal();

		history = new History(500);

		screenNormal = new Screen(1, terminal.getNumberOfRows());
		screenAlternate = new Screen(1, terminal.getNumberOfRows());
		screen = screenNormal;

		font = new Font(fontname, Font.PLAIN, fontsize);
		fontBold = new Font(fontname, Font.BOLD, fontsize);
		FontMetrics metrics = getFontMetrics(font);
		charHeight = metrics.getHeight();
		charWidth = metrics.charWidth('O');
		descent = metrics.getDescent();

		terminal.start(pwd);

		setFocusable(true);
		setFocusTraversalKeysEnabled(false);
		addKeyListener(new TerminalKeyAdapter(this));

		InputMap inputMap = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		ActionMap actionMap = getActionMap();

		TerminalWidgetKeyUtil keyUtil = new TerminalWidgetKeyUtil(this,
				inputMap, actionMap);
		keyUtil.addKeyAction(KeyEvent.VK_UP);
		keyUtil.addKeyAction(KeyEvent.VK_DOWN);
		keyUtil.addKeyAction(KeyEvent.VK_LEFT);
		keyUtil.addKeyAction(KeyEvent.VK_RIGHT);
		keyUtil.addKeyAction(KeyEvent.VK_HOME);
		keyUtil.addKeyAction(KeyEvent.VK_END);
		keyUtil.addKeyAction(KeyEvent.VK_INSERT);
		keyUtil.addKeyAction(KeyEvent.VK_DELETE);
		keyUtil.addKeyAction(KeyEvent.VK_PAGE_UP);
		keyUtil.addKeyAction(KeyEvent.VK_PAGE_DOWN);
		keyUtil.addKeyAction(KeyEvent.VK_F1);
		keyUtil.addKeyAction(KeyEvent.VK_F2);
		keyUtil.addKeyAction(KeyEvent.VK_F3);
		keyUtil.addKeyAction(KeyEvent.VK_F4);
		keyUtil.addKeyAction(KeyEvent.VK_F5);
		keyUtil.addKeyAction(KeyEvent.VK_F6);
		keyUtil.addKeyAction(KeyEvent.VK_F7);
		keyUtil.addKeyAction(KeyEvent.VK_F8);
		keyUtil.addKeyAction(KeyEvent.VK_F9);
		keyUtil.addKeyAction(KeyEvent.VK_F10);
		keyUtil.addKeyAction(KeyEvent.VK_F11);
		keyUtil.addKeyAction(KeyEvent.VK_F12);
		keyUtil.addKeyAction(KeyEvent.VK_ENTER);
		keyUtil.addKeyAction(KeyEvent.VK_INSERT, InputEvent.SHIFT_DOWN_MASK);
		keyUtil.addKeyAction(KeyEvent.VK_UP, InputEvent.SHIFT_DOWN_MASK);
		keyUtil.addKeyAction(KeyEvent.VK_DOWN, InputEvent.SHIFT_DOWN_MASK);
		keyUtil.addKeyAction(KeyEvent.VK_PAGE_UP, InputEvent.SHIFT_DOWN_MASK);
		keyUtil.addKeyAction(KeyEvent.VK_PAGE_DOWN, InputEvent.SHIFT_DOWN_MASK);

		addComponentListener(new ComponentAdapter() {

			@Override
			public void componentResized(ComponentEvent e)
			{
				sizeChanged();
			}

		});

		TerminalReader terminalReader = new TerminalReader(terminal) {

			@Override
			public void chunkHandled()
			{
				repaint();
			}

			@Override
			public void handleAscii(byte b)
			{
				while (true) {
					try {
						mutex.acquire();
						break;
					} catch (InterruptedException e) {
						continue;
					}
				}
				char c = (char) b;
				State before = state;
				boolean handled = TerminalWidget.this.handle(c);
				if (!handled) {
					log("STATE: " + before + " Byte: " + b + "..." + c);
				}
				mutex.release();
			}

			@Override
			public void handleUtf8(char c)
			{
				while (true) {
					try {
						mutex.acquire();
						break;
					} catch (InterruptedException e) {
						continue;
					}
				}
				TerminalWidget.this.handle(c);
				mutex.release();
			}

		};

		terminalReader.addTerminalClosedListener(this);

		Thread t = new Thread(terminalReader);
		t.start();
	}

	public boolean isDrawFrame()
	{
		return drawFrame;
	}

	public void setDrawFrame(boolean drawFrame)
	{
		this.drawFrame = drawFrame;
	}

	public boolean isDrawRaster()
	{
		return drawRaster;
	}

	public void setDrawRaster(boolean drawRaster)
	{
		this.drawRaster = drawRaster;
	}

	public boolean isDrawScrollingArea()
	{
		return drawScrollingArea;
	}

	public void setDrawScrollingArea(boolean drawScrollingArea)
	{
		this.drawScrollingArea = drawScrollingArea;
	}

	public Terminal getTerminal()
	{
		return terminal;
	}

	History getHistory()
	{
		return history;
	}

	private void log(String message)
	{
		System.out.println(message);
	}

	protected void sizeChanged()
	{
		Insets insets = getInsets();
		int w = getWidth() - insets.left - insets.right;
		int h = getHeight() - insets.top - insets.bottom;
		int cols = w / charWidth;
		int rows = h / charHeight;
		if (terminal.getNumberOfCols() != cols
				|| terminal.getNumberOfRows() != rows) {
			setTerminalSize(cols, rows);
		}
	}

	private void setTerminalSize(int cols, int rows)
	{
		log("Setting size: " + cols + " x " + rows);
		while (true) {
			try {
				mutex.acquire();
				break;
			} catch (InterruptedException e) {
				continue;
			}
		}

		boolean smaller = rows < terminal.getNumberOfRows();
		boolean bigger = rows > terminal.getNumberOfRows();

		int nRowsOld = terminal.getNumberOfRows();

		terminal.setNumberOfCols(cols);
		terminal.setNumberOfRows(rows);

		terminal.setSize(cols, rows);

		Screen screen = screenNormal;
		if (smaller) {
			if (screen.getCurrentRow() > rows) {
				screen.setCurrentRow(rows);
			}
		}
		if (smaller && (screen.getScrollTop() == 1
				&& screen.getScrollBottom() == nRowsOld)) {
			// the terminal has become smaller
			int rem = screen.getRows().size() - rows;
			for (int i = 0; i < rem; i++) {
				Row row = screen.getRows().remove(0);
				history.push(row);
			}
			if (screen.getCurrentRow() > rows) {
				screen.setCurrentRow(screen.getCurrentRow() - rem);
			}
			screen.setScrollTop(1);
			screen.setScrollBottom(rows);
		}
		if (bigger && (screen.getScrollTop() == 1
				&& screen.getScrollBottom() == nRowsOld)) {
			// the terminal has become larger
			int add = rows - screen.getRows().size();
			int hlen = history.getLength();
			int addReal = hlen >= add ? add : hlen;
			for (int a = 0; a < addReal; a++) {
				Row row = history.pop();
				screen.getRows().add(0, row);
				screen.setCurrentRow(screen.getCurrentRow() + 1);
			}
			screen.setScrollTop(1);
			screen.setScrollBottom(rows);
		}

		if (screenAlternate.getScrollTop() == 1
				&& screenAlternate.getScrollBottom() == nRowsOld) {
			screenAlternate.setScrollBottom(rows);
		}

		mutex.release();
	}

	private List<TerminalClosedListener> listeners = new ArrayList<>();

	public void addTerminalClosedListener(TerminalClosedListener listener)
	{
		listeners.add(listener);
	}

	public void removeTerminalClosedListener(TerminalClosedListener listener)
	{
		listeners.add(listener);
	}

	@Override
	public void terminalClosed()
	{
		fireTerminalClosedListeners();
	}

	private void fireTerminalClosedListeners()
	{
		for (TerminalClosedListener listener : listeners) {
			listener.terminalClosed();
		}
	}

	@Override
	public void paint(Graphics graphics)
	{
		super.paint(graphics);

		long start = System.currentTimeMillis();
		Graphics2D g = (Graphics2D) graphics;

		/*
		 * Background
		 */

		while (true) {
			try {
				mutex.acquire();
				break;
			} catch (InterruptedException e) {
				continue;
			}
		}

		Insets insets = getInsets();
		int ih = insets.left + insets.right;
		int iv = insets.top + insets.bottom;

		g.setColor(palette.getColor(DEFAULT_BG));
		g.fillRect(insets.left, insets.top, getWidth() - ih, getHeight() - iv);

		if (drawFrame) {
			g.setColor(palette.getColor(DEFAULT_FG));
			g.drawRect(insets.left, insets.right,
					terminal.getNumberOfCols() * charWidth,
					terminal.getNumberOfRows() * charHeight);
		}

		/*
		 * Screen
		 */

		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		GraphicsConfiguration gc = getGraphicsConfiguration();

		CacheStats stats = new CacheStats();
		List<Row> rows = screen.getRows();

		int hn = 0;
		if (screen == screenAlternate) {
			for (int i = 0; i < rows.size(); i++) {
				Row row = rows.get(i);
				drawRow(g, row, i, insets, gc, stats);
			}
		} else {
			int hlen = history.getLength();
			int slen = rows.size();
			int hpos = history.getPos();
			hn = hlen - hpos;
			int rc = 0;
			for (int i = 0; i < hn && i < rows.size(); i++) {
				Row row = history.get(hpos + i);
				drawRow(g, row, i, insets, gc, stats);
				rc++;
			}
			for (int i = hn; i < rows.size(); i++) {
				if (i - hn >= slen) {
					break;
				}
				Row row = rows.get(i - hn);
				drawRow(g, row, i, insets, gc, stats);
				rc++;
			}
		}

		/*
		 * Raster
		 */

		if (drawRaster) {
			int width = charWidth * terminal.getNumberOfCols();
			int height = charHeight * terminal.getNumberOfRows();

			g.setColor(colorRaster);

			for (int i = 0; i <= terminal.getNumberOfRows(); i++) {
				int y = insets.top + i * charHeight;
				g.drawLine(insets.left, y, width - ih, y);
			}
			for (int i = 0; i <= terminal.getNumberOfCols(); i++) {
				int x = insets.left + i * charWidth;
				g.drawLine(x, insets.top, x, height - iv);
			}
		}

		/*
		 * Cursor
		 */

		if (cursorVisible
				&& screen.getCurrentRow() + hn <= screen.getRows().size()) {
			g.setColor(colorCursor);
			g.fillRect(
					insets.left + (screen.getCurrentColumn() - 1) * charWidth,
					insets.top + (screen.getCurrentRow() - 1) * charHeight,
					charWidth, charHeight);
		}

		/*
		 * Scrolling region
		 */

		if (drawScrollingArea) {
			g.setColor(colorScrollingRegion);
			g.setStroke(new BasicStroke(2.0f));
			g.drawRect(insets.left,
					insets.top + (screen.getScrollTop() - 1) * charHeight,
					terminal.getNumberOfCols() * charWidth,
					screen.getScrollBottom() * charHeight);
		}

		mutex.release();

		if (DEBUG_RENDERING_TIME) {
			long end = System.currentTimeMillis();
			System.out.println("Time for paint(): " + (end - start));
			System.out.println("Pixels painted: " + stats.total);
			System.out.println(
					"Hit rate: " + (stats.hits / (double) stats.total));
			System.out.println("Cache size: " + cache.size());
		}

		g.dispose();
	}

	private void drawRow(Graphics g, Row row, int i, Insets insets,
			GraphicsConfiguration gc, CacheStats stats)
	{
		List<Pixel> pixels = row.getPixels();
		int y = charHeight * i;
		for (int k = 0; k < pixels.size(); k++) {
			stats.total++;
			Pixel pixel = pixels.get(k);
			String s = String.format("%c", pixel.getChar());

			BufferedImage image = cache.get(pixel);
			if (image != null) {
				stats.hits++;
				cache.refresh(pixel);
			} else {
				image = gc.createCompatibleImage(charWidth, charHeight);

				cache.put(pixel, image);

				Graphics2D h = image.createGraphics();
				h.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
						RenderingHints.VALUE_ANTIALIAS_ON);
				h.setFont(pixel.isHighlighted() ? fontBold : font);
				h.setColor(palette.getColor(pixel.getIndexBG()));
				h.fillRect(0, 0, charWidth, charHeight);
				h.setColor(palette.getColor(pixel.getIndexFG()));
				h.drawString(s, 0, 0 + charHeight - descent);
				h.dispose();
			}

			int x = charWidth * k;
			g.drawImage(image, x + insets.left, y + insets.top, null);
			image.flush();
		}

	}

	// @formatter:off
	private enum State {
		NORMAL, ESC, TITLE, 
		LEFT_BRACKET, RIGHT_BRACKET, 
		CSI, CSI_PREFIX, 
		CSI_NUM, CSI_SUFFIX
	};
	// @formatter:on

	private State state = State.NORMAL;
	private Csi currentCsi = new Csi();

	private boolean handle(char c)
	{
		switch (state) {
		case CSI_SUFFIX: {
			return handleSecondSuffix(c);
		}
		case CSI_NUM:
		case CSI_PREFIX: {
			switch (c) {
			case '0':
			case '1':
			case '2':
			case '3':
			case '4':
			case '5':
			case '6':
			case '7':
			case '8':
			case '9': {
				handleNumber(c);
				return true;
			}
			case ';': {
				handleSemicolon();
				return true;
			}
			default: {
				return handleSuffix(c);
			}
			}
		}
		case LEFT_BRACKET: // TODO: G0 (vt102 uses registers for charsets)
		case RIGHT_BRACKET: { // TODO: G1
			// handle Language setting
			state = State.NORMAL;
			setCharset(c);
			return true;
		}
		case ESC: {
			if (c <= 15) {
				return false;
			}
			switch (c) {
			case ']': {
				state = State.TITLE;
				return true;
			}
			case '[': {
				state = State.CSI;
				currentCsi = new Csi();
				return true;
			}
			case '(': {
				state = State.LEFT_BRACKET;
				return true;
			}
			case ')': {
				state = State.RIGHT_BRACKET;
				return true;
			}
			case 'D': // Index (IND)
			case 'M': // Reverse Index (RI)
			case 'E': // Next Line (NEL)
			case '7': // Save Cursor (DECSC)
			case '8': // Restore Cursor (DECRC)
			case '=': // Application Keypad Mode (DECKPAM)
			case '>': // Numeric Keypad Mode (DECKPNM)
			case 'N': // Single Shift 2 (SS2)
			case 'O': // Single Shift 3 (SS3)
			case 'c': // Reset (RIS)
			case 'H': { // Home Position ()
				state = State.NORMAL;
				return handleEscaped(c);
			}
			default: {
				// goto to STATE_Normal so that we don't become scrambled
				state = State.NORMAL;
				return false;
			}
			}
		}
		case TITLE: {
			if (c == '\07') {
				state = State.NORMAL;
			}
			return true;
		}
		case CSI: {
			switch (c) {
			case '!':
			case '?':
			case '>': {
				state = State.CSI_PREFIX;
				currentCsi.prefix = c;
				return true;
			}
			case '0':
			case '1':
			case '2':
			case '3':
			case '4':
			case '5':
			case '6':
			case '7':
			case '8':
			case '9': {
				state = State.CSI_NUM;
				handleNumber(c);
				return true;
			}
			case ';': {
				state = State.CSI_NUM;
				handleSemicolon();
				return true;
			}
			default: {
				return handleSuffix(c);
			}
			}
		}
		case NORMAL: {
			switch (c) {
			case '\u001b': {
				state = State.ESC;
				return true;
			}
			case '\0': {
				return false;
			}
			case '\b': {
				// log("CHAR: backspace");
				if (screen.getCurrentColumn() > 1) {
					setCurrentColumn("a", screen.getCurrentColumn() - 1);
				}
				return true;
			}
			case '\7': {
				log("CHAR: BELL");
				return true;
			}
			case '\r': {
				if (DEBUG_NEWLINES) {
					log("CHAR: carriage return");
				}
				setCurrentColumn("b", 1);
				return true;
			}
			case '\n': {
				if (DEBUG_NEWLINES) {
					log("CHAR: line feed");
				}
				if (screen.getCurrentRow() < screen.getScrollBottom()) {
					screen.setCurrentRow(screen.getCurrentRow() + 1);
				} else {
					insertLines(1);
				}
				return true;
			}
			case '\t': {
				log("CHAR: tab");
				int x = screen.getCurrentColumn();
				int m = (x - 1) % 8;
				for (int i = 0; i < 8 - m; i++) {
					add(' ');
				}
				return true;
			}
			}
			add(c);
			return true;
		}
		default:
			return false;
		}
	}

	private void setCurrentColumn(String pos, int col)
	{
		if (DEBUG_SET_COLUMN) {
			System.out.println("Set column (" + pos + "): "
					+ screen.getCurrentColumn() + " -> " + col);
		}
		screen.setCurrentColumn(col);
	}

	private void handleNumber(char c)
	{
		int n = c - 48;
		if (currentCsi.firstDigit) {
			currentCsi.firstDigit = false;
			currentCsi.nums.add(n);
		} else {
			int num = currentCsi.nums.get(currentCsi.nums.size() - 1);
			int v = num * 10 + n;
			currentCsi.nums.set(currentCsi.nums.size() - 1, v);
		}
	}

	private void handleSemicolon()
	{
		if (currentCsi.firstDigit) {
			int n = 0; // TODO: right default values
			currentCsi.nums.add(n);
		} else {
			currentCsi.firstDigit = true;
		}
	}

	private boolean handleSuffix(char c)
	{
		currentCsi.suffix1 = c;
		switch (c) {
		case '\"':
		case '\'': {
			state = State.CSI_SUFFIX;
			break;
		}
		default: {
			// TODO: emit an action since a sequence was processed
			state = State.NORMAL;
			return handleCsi(currentCsi);
		}
		}
		return false;
	}

	private boolean handleSecondSuffix(char c)
	{
		// TODO: emit an action since a sequence was processed
		currentCsi.suffix2 = c;
		return handleCsi(currentCsi);
	}

	private Cursor storedCursor = null;

	private boolean handleEscaped(char c)
	{
		switch (c) {
		case 'c': { // Reset (RIS)
			log(String.format("Reset"));
			reset();
			return true;
		}
		case 'H': { // Reset (RIS)
			log(String.format("Home Position"));
			cursorGoto(1, 1);
			return true;
		}
		case 'D': { // Index (IND)
			log(String.format("TODO: Index"));
			twIndex();
			return true;
		}
		case 'M': { // Reverse Index (RI)
			twReverseIndex();
			return true;
		}
		case 'E': { // Next Line (NEL)
			log(String.format("TODO: Next Line"));
			return true;
		}
		case '7': { // Save Cursor (DECSC)
			if (DEBUG_CURSOR) {
				log(String.format("save cursor: %d,%d", screen.getCurrentRow(),
						screen.getCurrentColumn()));
			}
			storedCursor = screen.getCurrentCursor();
			return true;
		}
		case '8': { // Restore Cursor (DECRC)
			if (DEBUG_CURSOR) {
				log(String.format("restore cursor: %d,%d",
						screen.getCurrentRow(), screen.getCurrentColumn()));
			}
			if (storedCursor != null) {
				screen.setCursor(storedCursor);
			}
			return true;
		}
		case '=': { // Application Keypad Mode (DECKPAM)
			log(String.format("TODO: Application Keypad Mode"));
			return true;
		}
		case '>': { // Numeric Keypad Mode (DECKPNM)
			log(String.format("TODO: Numeric Keypad Mode"));
			return true;
		}
		case 'N': { // Single Shift 2 (SS2)
			log(String.format("TODO: Single Shift 2"));
			return true;
		}
		case 'O': { // Single Shift 3 (SS3)
			log(String.format("TODO: Single Shift 3"));
			return true;
		}
		default: {
			log(String.format("UNKNOWN ESC<c>%c", c));
		}
		}
		return false;
	}

	private void reset()
	{
		useNormalScreen();
		screen.getRows().clear();
		setCurrentColumn("c", 1);
		screen.setCurrentRow(1);
		screen.setScrollTop(1);
		screen.setScrollBottom(terminal.getNumberOfRows());
	}

	private void twIndex()
	{
		log("TODO: twIndex");
	}

	private void twReverseIndex()
	{
		StringBuffer buffer = new StringBuffer();
		buffer.append("ReverseIndex");
		// check whether we're at the top margin
		if (screen.getCurrentRow() == screen.getScrollTop()) {
			// yes we are. scroll down
			buffer.append(" -> scroll down");
			insertLinesBefore(1);
		} else {
			// no. just move the cursor up
			buffer.append(" -> move cursor up");
			screen.setCurrentRow(screen.getCurrentRow() - 1);
		}
		log(buffer.toString());
	}

	private void printCsi(Csi csi)
	{
		log("Handle CSI. prefix: '" + csi.prefix + "', suffix1: '" + csi.suffix1
				+ "', suffix2: '" + csi.suffix2 + "'");
		for (int i = 0; i < csi.nums.size(); i++) {
			int num = csi.nums.get(i);
			log("CSI number: " + num);
		}
	}

	private boolean handleCsi(Csi csi)
	{
		boolean value = handleCsiIntern(csi);
		if (!value) {
			printCsi(csi);
		}
		return value;
	}

	private boolean handleCsiIntern(Csi csi)
	{
		if ((csi.suffix1 == 'h' || csi.suffix1 == 'l') && csi.prefix == '\0') {
			log("CSI case 1: all not handled yet in jterm");
			return true;
		} else if ((csi.suffix1 == 'h' || csi.suffix1 == 'l')
				&& csi.prefix == '?') { // DECSET / DECRST
			boolean set = csi.suffix1 == 'h'; // SET / RESET
			for (int i = 0; i < csi.nums.size(); i++) {
				int n = csi.nums.get(i);
				switch (n) {
				case 1: { // DECCKM
					// set: cursor keys transmit control (application) functions
					// reset: cursor keys transmit ANSI control sequences
					decCkm = set;
					log("DEC CKM: " + decCkm);
					return true;
				}
				case 2: { // VT52 Mode (DECANM)
					if (csi.suffix1 == 'h') { // only 'h'
						// TODO: ?
					}
					log(String.format("TODO: DECANM"));
					return true;
				}
				case 3: { // Column Mode (DECCOLM)
							// set: 132 cols/line; reset: 80 cols/line
					log(String.format("TODO: DECCOLM"));
					return true;
				}
				case 4: { // Scroll Mode (DECSCLM)
							// set: smooth (6 lines/sec); reset: jump (fast as
							// possible)
					String s = set ? "SMOOTH" : "JUMP";
					log(String.format("TODO: DECSCLM : %s", s));
					return true;
				}
				case 5: { // Screen Mode (DECSCNM)
							// set: reverse screen (white screen, black chars)
							// reset: normal screen (black screen, white chars)
					log(String.format("TODO: DECSCNM"));
					return true;
				}
				case 6: { // Origin Mode (DECOM)
							// set: relative to scrolling region; reset:
							// absolute
					log(String.format("TODO: DECOM"));
					return true;
				}
				case 7: { // Wrap Mode (DECAWM)
					// set: auto wrap (goto next line; scroll if neccessary)
					// reset: diable auto wrap (overwrite chars at end of line)
					decAwm = set;
					return true;
				}
				case 8: { // Auto Repeat Mode (DECARM)
							// enable / disable auto repeat of pressed keys
					log(String.format("TODO: DECARM"));
					break;
				}
				case 12: {
					String s = set ? "Start" : "Stop";
					log(String.format("TODO: %s Blinking", s));
					return true;
				}
				case 25: {
					String s = set ? "Show" : "Hide";
					log(String.format("%s Cursor", s));
					cursorVisible = set;
					return true;
				}
				case 1049: {
					if (set) {
						useAlternateScreen();
					} else {
						useNormalScreen();
					}
					return true;
				}
				default: {
					break;
				}
				}
			}
		} else if (csi.suffix1 == 'H') { // goto
			int r = getValueOrDefault(csi, 1);
			int c = getValueOrDefault(csi, 1, 1);
			// log(String.format("GOTO:%d,%d", r, c));

			cursorGoto(r, c);
			return true;
		} else if (csi.suffix1 == 'd') { // If col not present, don't change it
			// This behavior has been guessed
			int r = 1, c = screen.getCurrentColumn();
			if (csi.nums.size() == 1) {
				r = csi.nums.get(0);
			}
			if (csi.nums.size() >= 2) {
				r = csi.nums.get(0);
				c = csi.nums.get(0);
			}

			if (DEBUG_CURSOR) {
				log(String.format("FROM: %d,%d GOTO:%d,%d",
						screen.getCurrentRow(), screen.getCurrentColumn(), r,
						c));
			}

			screen.setCurrentRow(r);
			setCurrentColumn("d", c >= 1 ? c : 1);
			return true;
		} else if (csi.suffix1 == '@') { // insert n blank characters
			int n = getValueOrDefault(csi, 1);
			insertBlankCharacters(n);
			return true;
		} else if (csi.suffix1 == 'X') { // erase n characters
			int n = getValueOrDefault(csi, 1);
			eraseCharacters(n);
			return true;
		} else if (csi.suffix1 == 'P') { // delete n characters
			int n = getValueOrDefault(csi, 1);
			deleteCharacters(n);
			return true;
		} else if (csi.suffix1 == 'G') { // cursor character absolute
			int n = getValueOrDefault(csi, 1);
			cursorCharacterAbsolute(n);
			return true;
		} else if (csi.suffix1 == 'A') { // cursor up n times
			int n = getValueOrDefault(csi, 1);
			cursorUp(n);
			return true;
		} else if (csi.suffix1 == 'B') { // cursor down n times
			int n = getValueOrDefault(csi, 1);
			cursorDown(n);
			return true;
		} else if (csi.suffix1 == 'C') { // cursor forward n times
			int n = getValueOrDefault(csi, 1);
			cursorForward(n);
			return true;
		} else if (csi.suffix1 == 'D') { // cursor backward n times
			int n = getValueOrDefault(csi, 1);
			cursorBackward(n);
			return true;
		} else if (csi.suffix1 == 'f') { // cursor position
			int r = getValueOrDefault(csi, 1);
			int c = getValueOrDefault(csi, 1, 1);
			cursorGoto(r, c);
			return true;
		} else if (csi.suffix1 == 'K') { // erase in line
			int n = getValueOrDefault(csi, 0);
			switch (n) {
			case 0:
				// erase to the right
				eraseToTheRight();
				return true;
			case 1:
				// erase to the left
				eraseToTheLeft();
				return true;
			case 2:
				// erase line
				eraseLine();
				return true;
			}
		} else if (csi.suffix1 == 'L') { // insert n lines
			int n = getValueOrDefault(csi, 1);

			log(String.format("IL: CSI.L"));

			insertLinesBefore(n);
			return true;
		} else if (csi.suffix1 == 'S') { // scroll up n lines
			int n = getValueOrDefault(csi, 1);
			scrollUp(n);
			return true;
		} else if (csi.suffix1 == 'T') { // scroll down n lines
			int n = getValueOrDefault(csi, 1);
			scrollDown(n);
			return true;
		} else if (csi.suffix1 == 'M') { // delete n lines
			int n = getValueOrDefault(csi, 1);
			deleteLines(n);
			return true;
		} else if (csi.suffix1 == 'J') { // erase in display
			int n = getValueOrDefault(csi, 0);
			switch (n) { // 0: below, 1: above, 2: all, 3: saved lines (xterm)
			case 0: {
				// from cursor to end of screen, including cursor position
				eraseFromCursorToEnd();
				return true;
			}
			case 1: {
				// from beginning of screen to cursor, including cursor position
				eraseFromBeginningToCursor();
				return true;
			}
			case 2: {
				eraseAll();
				return true;
			}
			default: {
				log(String.format("TODO: ERASE IN DISPLAY %d\n", n));
			}
			}
		} else if (csi.suffix1 == 'r' && csi.prefix == '\0') {
			// set scrolling region
			int t = 1;
			int b = terminal.getNumberOfRows();
			if (csi.nums.size() >= 2) {
				t = csi.nums.get(0);
				b = csi.nums.get(1);
			}

			if (DEBUG_SCROLLING_AREA) {
				log(String.format("SCROLLING AREA: %d:%d", t, b));
			}

			setScrollingRegionSafe(screen, t, b);
			return true;
		} else if (csi.suffix1 == 'c' && csi.prefix == '>') {
			// send device attributes (secondary DA)
			log(String.format("TODO: DEVICE ATTRIBUTES, PLEASE"));
		} else if (csi.suffix1 == 'm') {
			int n = csi.nums.size();
			if (n == 0) {
				setColors(0);
			} else {
				for (int i = 0; i < csi.nums.size(); i++) {
					setColors(csi.nums.get(i));
				}
			}
			return true;
		}
		return false;
	}

	private void setScrollingRegionSafe(Screen screen, int t, int b)
	{
		t = Math.max(t, 1);
		b = Math.min(b, terminal.getNumberOfRows());
		screen.setScrollTop(t);
		screen.setScrollBottom(b);
	}

	private void useNormalScreen()
	{
		log("Switch to normal Screen!");
		screen = screenNormal;
	}

	private void useAlternateScreen()
	{
		log("Switch to alternate Screen!");
		screen = screenAlternate;
	}

	private void addPixel(Pixel pixel)
	{
		int row = screen.getCurrentRow();
		List<Pixel> pixels = screen.getRows().get(row - 1).getPixels();
		pixels.add(pixel);
	}

	private Pixel newPixel(char c)
	{
		return new PixelDense(c);
	}

	private Pixel createPixel(char c)
	{
		Pixel pixel = newPixel(c);
		setColors(pixel);
		return pixel;
	}

	private void changePixel(Pixel pixel, char c)
	{
		pixel.setChar(c);
		setColors(pixel);
	}

	private void setColors(Pixel pixel)
	{
		pixel.setFg(fg);
		pixel.setBg(bg);
		pixel.setBgBright(bgBright);
		pixel.setFgBright(fgBright);
		pixel.setHighlighted(highlighted);
		pixel.setReverse(reverse);
	}

	private Pixel createOpaquePixel(char c)
	{
		Pixel pixel = newPixel(c);
		pixel.setFg(DEFAULT_FG);
		pixel.setBg(DEFAULT_BG);
		pixel.setBgBright(false);
		pixel.setFgBright(false);
		pixel.setHighlighted(false);
		pixel.setReverse(false);
		return pixel;
	}

	private void setColors(int code)
	{
		if (code == 0) {
			fg = DEFAULT_FG;
			bg = DEFAULT_BG;
			highlighted = false;
			reverse = false;
			fgBright = false;
			bgBright = false;
		} else if (code == 1) {
			highlighted = true;
		} else if (code == 2) {
			highlighted = false;
		} else if (code == 7) {
			reverse = true;
		} else if (code == 27) {
			reverse = false;
		} else if (code >= 30 && code <= 37) {
			int c = code - 30;
			fg = c;
			fgBright = false;
		} else if (code == 39) {
			fg = DEFAULT_FG;
			fgBright = false;
		} else if (code >= 40 && code <= 47) {
			int c = code - 40;
			bg = c;
			bgBright = false;
		} else if (code == 49) {
			bg = DEFAULT_BG;
			bgBright = false;
		} else if (code >= 90 && code <= 97) { // should be bright
			int c = code - 90;
			fg = c;
			fgBright = true;
		} else if (code >= 100 && code <= 107) { // should be bright
			int c = code - 100;
			bg = c;
			bgBright = true;
		} else {
			log("color: " + code);
		}
	}

	private void setCharset(char c)
	{
		// log("Set Charset: '" + c + "'");
		screen.setCharacterSet(c);
	}

	private int getValueOrDefault(Csi csi, int v)
	{
		if (csi.nums.size() > 0) {
			return csi.nums.get(0);
		}
		return v;
	}

	private int getValueOrDefault(Csi csi, int pos, int v)
	{
		if (csi.nums.size() > pos) {
			return csi.nums.get(pos);
		}
		return v;
	}

	private void add(char c)
	{
		if (screen.getCharacterSet() == '0') {
			Character replacement = Charmap.get(c);
			c = replacement == null ? c : replacement;
		}

		if (screen.getCurrentColumn() > terminal.getNumberOfCols()) {
			if (screen.getCurrentRow() >= screen.getScrollTop()
					&& screen.getCurrentRow() <= screen.getScrollBottom()) {
				if (screen.getCurrentRow() == screen.getScrollBottom()) {
					insertLines(1);
				}
				if (screen.getCurrentRow() != screen.getScrollBottom()) {
					screen.setCurrentRow(screen.getCurrentRow() + 1);
				}
			} else {
				if (screen.getCurrentRow() != terminal.getNumberOfRows()) {
					screen.setCurrentRow(screen.getCurrentRow() + 1);
				}
			}
			setCurrentColumn("j", 1);
		}

		int row = screen.getCurrentRow();
		int col = screen.getCurrentColumn();

		while (screen.getRows().size() < row) {
			screen.getRows().add(new Row());
		}

		if (col < 1 || col > terminal.getNumberOfCols()) {
			StringBuilder buffer = new StringBuilder();
			if (col < 1) {
				buffer.append("request to go to a col < 1");
			} else {
				buffer.append("request to go to a col beyond end");
			}
			buffer.append(", DECAWM: " + decAwm);
			buffer.append(", crow: " + screen.getCurrentRow());
			buffer.append(", scrollTop: " + screen.getScrollTop());
			buffer.append(", scrollBottom: " + screen.getScrollBottom());
			log(buffer.toString());
			if (decAwm) {
				if (col < 1) {
					// TODO: is this behaviour desired at all?
					// TODO: check scrolling region
					screen.setCurrentRow(screen.getCurrentRow() - 1);
					setCurrentColumn("x1", terminal.getNumberOfCols() - 1);
				}
				if (col >= terminal.getNumberOfCols()) {
					if (screen.getCurrentRow() == screen.getScrollBottom()) {
						// we're on the last line, have to scroll
						screen.getRows().add(screen.getCurrentRow(), new Row());
						Row drow = screen.getRows()
								.remove(screen.getScrollTop() - 1);
						if (screen == screenNormal
								&& screen.getScrollTop() == 1) {
							history.push(drow);
						}
					} else {
						screen.setCurrentRow(screen.getCurrentRow() + 1);
					}
					setCurrentColumn("x2", 1);
				}
			}
		}

		List<Pixel> pixels = screen.getRows().get(row - 1).getPixels();

		if (pixels.size() < screen.getCurrentColumn() - 1) {
			int fill = screen.getCurrentColumn() - pixels.size() - 1;
			for (int x = 0; x < fill; x++) {
				addPixel(createOpaquePixel(' '));
			}
			addPixel(createPixel(c));
		} else if (pixels.size() == screen.getCurrentColumn() - 1) {
			// No padding needed, cursor is exactly at insertion position
			addPixel(createPixel(c));
		} else {
			// Overwriting
			if (screen.getCurrentColumn() - 1 < pixels.size()) {
				Pixel pixel = pixels.get(screen.getCurrentColumn() - 1);
				changePixel(pixel, c);
			}
		}

		setCurrentColumn("k (" + c + ")", screen.getCurrentColumn() + 1);
	}

	private void cursorForward(int n)
	{
		if (DEBUG_CURSOR) {
			log(String.format("cursor %d forward", n));
		}
		setCurrentColumn("e", screen.getCurrentColumn() + n);
	}

	private void cursorBackward(int n)
	{
		if (DEBUG_CURSOR) {
			log(String.format("cursor %d backwards (%d)", n,
					screen.getCurrentColumn()));
		}
		int col = screen.getCurrentColumn() - n;
		if (col < 1) {
			col = 1;
		}
		setCurrentColumn("f", col);
	}

	private void cursorDown(int n)
	{
		if (DEBUG_CURSOR) {
			log(String.format("cursor %d down", n));
		}
		screen.setCurrentRow(screen.getCurrentRow() + n);
	}

	private void cursorUp(int n)
	{
		if (DEBUG_CURSOR) {
			log(String.format("cursor %d up", n));
		}
		screen.setCurrentRow(screen.getCurrentRow() - n);
	}

	private void cursorCharacterAbsolute(int n)
	{
		if (DEBUG_CURSOR) {
			log(String.format("cursor char absolute %d", n));
		}
		setCurrentColumn("h", n >= 1 ? n : 1);
	}

	private void cursorGoto(int r, int c)
	{
		if (DEBUG_CURSOR) {
			log(String.format("cursor go to row: %d, col: %d", r, c));
		}
		setCurrentColumn("i", c >= 1 ? c : 1);
		screen.setCurrentRow(r);
		if (screen.getCurrentRow() > terminal.getNumberOfRows()) { // if we're
																	// out of
																	// range
			// TODO: this disregards the scrolling region; guessed behavior
			// It's unclear what's supposed to happen if we're moved 'into' the
			// margin
			int x = r - terminal.getNumberOfRows();
			for (int i = 0; i < x; i++) {
				// TODO: why not without loop?
				screen.setCurrentRow(screen.getCurrentRow() - 1);
			}
		}
	}

	private void scrollDown(int n)
	{
		if (DEBUG_CURSOR) {
			log(String.format("scroll down: %d", n));
		}
		for (int i = 0; i < n; i++) {
			screen.getRows().remove(screen.getScrollBottom() - 1);
			int insPos = screen.getScrollTop() - 1;
			if (insPos > screen.getRows().size()) {
				insPos = screen.getRows().size();
			}
			screen.getRows().add(insPos, new Row());
		}
	}

	private void scrollUp(int n)
	{
		if (DEBUG_CURSOR) {
			log(String.format("scroll up: %d", n));
		}
		for (int i = 0; i < n; i++) {
			screen.getRows().remove(screen.getScrollTop() - 1);
			int insPos = screen.getScrollBottom() - 1;
			if (insPos > screen.getRows().size()) {
				insPos = screen.getRows().size();
			}
			screen.getRows().add(insPos, new Row());
		}
	}

	private void eraseAll()
	{
		if (DEBUG_ERASE_DELETE) {
			log("erase all");
		}
		screen.getRows().clear();
		setCurrentColumn("g", 1);
		screen.setCurrentRow(1);
	}

	private void eraseLine()
	{
		log("TODO: erase line");
	}

	private void eraseFromCursorToEnd()
	{
		int pr = screen.getCurrentRow();
		int pc = screen.getCurrentColumn();
		if (DEBUG_ERASE_DELETE) {
			log("Erase from cursor to to end of screen. row: " + pr + ", col: "
					+ pc);
		}

		List<Pixel> row = screen.getRows().get(pr - 1).getPixels();
		if (row.size() >= 1) {
			for (int x = row.size() - 1; x >= pc - 1 && x >= 0; x--) {
				row.remove(x);
			}
		}

		for (int i = pr + 1; i <= screen.getRows().size(); i++) {
			screen.getRows().get(i - 1).getPixels().clear();
		}
	}

	private void eraseFromBeginningToCursor()
	{
		log("TODO: erase from beginning of screen to cursor");
		int pr = screen.getCurrentRow();
		int pc = screen.getCurrentColumn();
		if (DEBUG_ERASE_DELETE) {
			log("Erase from beginning of screen to row: " + pr + ", col: "
					+ pc);
		}
	}

	private void eraseToTheLeft()
	{
		int pr = screen.getCurrentRow();
		int pc = screen.getCurrentColumn();
		if (DEBUG_ERASE_DELETE) {
			log("Erase to the left row: " + pr + ", col: " + pc);
		}
		if (screen.getRows().size() < pr) {
			return;
		}
		boolean reverse = this.reverse;
		this.reverse = false;
		List<Pixel> row = screen.getRows().get(pr - 1).getPixels();
		int len = row.size();
		int del = len > pc ? pc : len;
		for (int i = 0; i < del; i++) {
			changePixel(row.get(i), ' ');
		}
		this.reverse = reverse;
	}

	private void eraseCharacters(int n)
	{
		if (DEBUG_ERASE_DELETE) {
			log(String.format("erase %d chars, row: %d, col: %d", n,
					screen.getCurrentRow(), screen.getCurrentColumn()));
		}
		if (screen.getCurrentRow() <= screen.getRows().size()) {
			Row row = screen.getRows().get(screen.getCurrentRow() - 1);
			for (int i = 0; i < n; i++) {
				int c = screen.getCurrentColumn() - 1 + i;
				if (c < row.getPixels().size()) {
					Pixel pixel = row.getPixels().get(c);
					changePixel(pixel, ' ');
				} else {
					row.getPixels().add(createPixel(' '));
				}
			}
		}
	}

	private void eraseToTheRight()
	{
		int pr = screen.getCurrentRow();
		int pc = screen.getCurrentColumn();
		if (DEBUG_ERASE_DELETE) {
			log("Erase to the right row: " + pr + ", col: " + pc);
		}
		if (screen.getRows().size() < pr) {
			return;
		}
		boolean reverse = this.reverse;
		this.reverse = false;
		List<Pixel> row = screen.getRows().get(pr - 1).getPixels();
		if (row.size() >= 1) {
			for (int x = row.size() - 1; x >= pc - 1 && x >= 0; x--) {
				row.remove(x);
			}
		}
		for (int x = screen.getCurrentColumn(); x <= terminal
				.getNumberOfCols(); x++) {
			row.add(createPixel(' '));
		}
		this.reverse = reverse;
	}

	private void deleteLines(int n)
	{
		if (DEBUG_ERASE_DELETE) {
			log(String.format("delete %d lines", n));
		}
		if (screen.getCurrentRow() >= screen.getScrollTop()
				&& screen.getCurrentRow() <= screen.getScrollBottom()) {
			// ok we're in the scrolling region
			for (int i = 0; i < n; i++) {
				screen.getRows().remove(screen.getCurrentRow() - 1);
				int insPos = screen.getScrollBottom() - 1;
				if (insPos > screen.getRows().size()) {
					insPos = screen.getRows().size();
				}
				screen.getRows().add(insPos, new Row());
			}
		}
	}

	private void deleteCharacters(int n)
	{
		if (DEBUG_ERASE_DELETE) {
			log(String.format("delete %d chars", n));
		}
		if (screen.getCurrentRow() <= screen.getRows().size()) {
			Row row = screen.getRows().get(screen.getCurrentRow() - 1);
			List<Pixel> pixels = row.getPixels();
			for (int i = 0; i < n; i++) {
				// TODO: changed from < to <= (compared to vexterm)
				if (screen.getCurrentColumn() <= pixels.size()) {
					pixels.remove(screen.getCurrentColumn() - 1);
				}
			}
		}
	}

	private void insertLinesBefore(int n)
	{
		// ignore if not within scrolling region
		for (int i = 0; i < n; i++) {
			if (screen.getCurrentRow() >= screen.getScrollTop()
					&& screen.getCurrentRow() <= screen.getScrollBottom()) {
				// ok, we're in the scrolling region
				if (screen.getRows().size() >= screen.getScrollBottom()) {
					log("Remove line " + (screen.getScrollBottom() - 1));
					screen.getRows().remove(screen.getScrollBottom() - 1);
				}
				screen.getRows().add(screen.getCurrentRow() - 1, new Row());
			}
		}
	}

	private void insertLines(int n)
	{
		// ignore if not within scrolling region
		for (int i = 0; i < n; i++) {
			if (screen.getCurrentRow() >= screen.getScrollTop()
					&& screen.getCurrentRow() <= screen.getScrollBottom()) {
				// ok, we're in the scrolling region
				if (screen.getCurrentRow() == screen.getScrollBottom()) {
					// we're on the last line, have to scroll
					for (int s = screen.getRows().size(); s < screen
							.getCurrentRow(); s++) {
						screen.getRows().add(s, new Row());
					}
					screen.getRows().add(screen.getCurrentRow(), new Row());
					Row drow = screen.getRows()
							.remove(screen.getScrollTop() - 1);
					if (screen == screenNormal && screen.getScrollTop() == 1) {
						history.push(drow);
					}
				} else {
					for (int x = 0; x < n; x++) {
						// check whether we have to retain a row at the bottom
						// of the scrolling region
						if (screen.getRows().size() >= screen
								.getScrollBottom()) {
							// yes, there are too many rows
							screen.getRows()
									.remove(screen.getScrollBottom() - 1);
						}
						// insert new row
						screen.getRows().add(screen.getCurrentRow(), new Row());
						screen.setCurrentRow(screen.getCurrentRow() + 1);
					}
				}
			}
		}
	}

	private void insertBlankCharacters(int n)
	{
		log(String.format("insert %d blank chars", n));
		List<Pixel> row = screen.getRows().get(screen.getCurrentRow() - 1)
				.getPixels();
		for (int i = 0; i < n; i++) {
			if (screen.getCurrentColumn() <= row.size()) {
				row.add(screen.getCurrentColumn() - 1, createPixel(' '));
			}
		}
	}

	public void ensureBottomLineVisible()
	{
		if (history.getPos() != history.getLength()) {
			history.setPos(history.getLength());
		}
	}

	public int getTheme()
	{
		return theme;
	}

	public void setTheme(int num)
	{
		this.theme = num;
		palette = Themes.THEMES.get(theme);
		cache.clear();
		repaint();
	}

}
