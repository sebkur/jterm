package de.topobyte.jterm;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;

public class TerminalWidget extends JComponent
{

	private static final long serialVersionUID = 6198367149770918349L;

	private Terminal terminal;

	private int charWidth = 7;
	private int charHeight = 11;

	private Screen screen;
	private Screen screenNormal;
	private Screen screenSpecial;

	private int historyPos = 0;
	private History history;

	private int fg = 16;
	private int bg = 17;
	private boolean highlighted = false;
	private boolean reverse = false;
	private boolean fgBright = false;
	private boolean bgBright = false;

	private Palette palette = new Palette();

	private boolean decCkm = false;
	private boolean cursorVisible = true;

	public TerminalWidget()
	{
		terminal = new Terminal();

		history = new History();

		screenNormal = new Screen(1, terminal.getNumberOfRows());
		screenSpecial = new Screen(1, terminal.getNumberOfRows());
		screen = screenNormal;

		terminal.start();

		setFocusable(true);
		setFocusTraversalKeysEnabled(false);
		addKeyListener(new TerminalKeyAdapter(terminal));

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

		TerminalReader terminalReader = new TerminalReader(terminal) {

			@Override
			public void chunkHandled()
			{
				repaint();
			}

			@Override
			public void handleAscii(byte b)
			{
				char c = (char) b;
				State before = state;
				boolean handled = TerminalWidget.this.handle(c);
				if (!handled) {
					System.out.println("STATE: " + before + " Byte: " + b
							+ "..." + c);
				}
			}

			@Override
			public void handleUtf8(char c)
			{
				TerminalWidget.this.handle(c);
			}

		};

		Thread t = new Thread(terminalReader);
		t.start();
	}

	@Override
	public void paint(Graphics graphics)
	{
		super.paint(graphics);

		Graphics2D g = (Graphics2D) graphics;

		/*
		 * Background
		 */

		g.setColor(Color.BLACK);
		g.fillRect(0, 0, getWidth(), getHeight());

		/*
		 * Raster
		 */

		int width = charWidth * terminal.getNumberOfCols();
		int height = charHeight * terminal.getNumberOfRows();

		g.setColor(Color.GRAY.darker().darker());

		for (int i = 0; i <= terminal.getNumberOfRows(); i++) {
			int y = i * charHeight;
			g.drawLine(0, y, width, y);
		}
		for (int i = 0; i <= terminal.getNumberOfCols(); i++) {
			int x = i * charWidth;
			g.drawLine(x, 0, x, height);
		}

		/*
		 * Screen
		 */

		g.setColor(Color.GREEN);

		Font font = new Font(Font.MONOSPACED, Font.PLAIN, 10);
		g.setFont(font);

		List<Row> rows = screen.getRows();
		for (int i = 0; i < rows.size(); i++) {
			Row row = rows.get(i);
			List<Pixel> pixels = row.getPixels();
			int y = charHeight * i;
			for (int k = 0; k < pixels.size(); k++) {
				int x = charWidth * k;
				Pixel pixel = pixels.get(k);
				g.setColor(palette.getColor(pixel.getIndexFG()));
				String s = String.format("%c", pixel.getChar());
				g.drawString(s, x, y + charHeight);
			}
		}

		/*
		 * Cursor
		 */

		g.setColor(new Color(0x99ff0000, true));
		g.fillRect((screen.getCurrentColumn() - 1) * charWidth,
				(screen.getCurrentRow() - 1) * charHeight, charWidth,
				charHeight);

		/*
		 * Scrolling region
		 */

		g.setColor(new Color(0x99ff0000, true));
		g.drawRect(0, (screen.getScrollTop() - 1) * charHeight,
				terminal.getNumberOfCols() * charWidth,
				screen.getScrollBottom() * charHeight);
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
				System.out.println("CHAR: backspace");
				screen.setCurrentColumn(screen.getCurrentColumn() - 1);
				return true;
			}
			case '\7': {
				System.out.println("CHAR: BELL");
				return true;
			}
			case '\r': {
				System.out.println("CHAR: carriage return");
				screen.setCurrentColumn(1);
				return true;
			}
			case '\n': {
				System.out.println("CHAR: line feed");
				if (screen.getCurrentRow() < screen.getScrollBottom()) {
					screen.setCurrentRow(screen.getCurrentRow() + 1);
				} else {
					insertLines(1);
				}
				return true;
			}
			case '\t': {
				System.out.println("CHAR: tab");
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

	private boolean handleEscaped(char c)
	{
		switch (c) {
		case 'c': { // Reset (RIS)
			System.out.println(String.format("||Reset||"));
			reset();
			return true;
		}
		case 'H': { // Reset (RIS)
			System.out.println(String.format("||Home Position||"));
			cursorGoto(1, 1);
			return true;
		}
		case 'D': { // Index (IND)
			System.out.println(String.format("||TODO: Index||"));
			twIndex();
			return true;
		}
		case 'M': { // Reverse Index (RI)
			System.out.println(String.format("||Reverse Index||"));
			twReverseIndex();
			return true;
		}
		case 'E': { // Next Line (NEL)
			System.out.println(String.format("||TODO: Next Line||"));
			break;
		}
		case '7': { // Save Cursor (DECSC)
			System.out.println(String.format("||TODO: Save Cursor||"));
			break;
		}
		case '8': { // Restore Cursor (DECRC)
			System.out.println(String.format("||TODO: Restore Cursor||"));
			break;
		}
		case '=': { // Application Keypad Mode (DECKPAM)
			System.out.println(String.format("\n\n\n"));
			System.out.println(String
					.format("||TODO: Application Keypad Mode||"));
			break;
		}
		case '>': { // Numeric Keypad Mode (DECKPNM)
			System.out.println(String.format("\n\n\n"));
			System.out.println(String.format("||TODO: Numeric Keypad Mode||"));
			break;
		}
		case 'N': { // Single Shift 2 (SS2)
			System.out.println(String.format("||TODO: Single Shift 2||"));
			break;
		}
		case 'O': { // Single Shift 3 (SS3)
			System.out.println(String.format("||TODO: Single Shift 3||"));
			break;
		}
		default: {
			System.out.println(String.format("||UNKNOWN ESC<c>%c||", c));
		}
		}
		return false;
	}

	private void reset()
	{
		// TODO Auto-generated method stub

	}

	private void twIndex()
	{
		// TODO Auto-generated method stub

	}

	private void twReverseIndex()
	{
		System.out.println("ReverseIndex");
		// check whether we're at the top margin
		if (screen.getCurrentRow() == screen.getScrollTop()) {
			// yes we are. scroll down
			System.out.println("-> scroll down");
			insertLinesBefore(1);
		} else {
			// no. just move the cursor up
			System.out.println("-> move cursor up");
			screen.setCurrentRow(screen.getCurrentRow() - 1);
		}
	}

	private void printCsi(Csi csi)
	{
		System.out.println("Handle CSI. prefix: '" + csi.prefix
				+ "', suffix1: '" + csi.suffix1
				+ "', suffix2: '" + csi.suffix2 + "'");
		for (int i = 0; i < csi.nums.size(); i++) {
			int num = csi.nums.get(i);
			System.out.println("CSI number: " + num);
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
			System.out.println("CSI case 1");
		} else if ((csi.suffix1 == 'h' || csi.suffix1 == 'l')
				&& csi.prefix == '?') { // DECSET / DECRST
			boolean set = csi.prefix == 'h'; // SET / RESET
			for (int i = 0; i < csi.nums.size(); i++) {
				int n = csi.nums.get(i);
				switch (n) {
				case 1: { // DECCKM
					// set: cursor keys transmit control (application) functions
					// reset: cursor keys transmit ANSI control sequences
					decCkm = set;
					return true;
				}
				case 2: { // VT52 Mode (DECANM)
					if (csi.suffix1 == 'h') { // only 'h'
						// TODO: ?
					}
					System.out.println(String.format("|TODO: DECANM|"));
					break;
				}
				case 3: { // Column Mode (DECCOLM)
							// set: 132 cols/line; reset: 80 cols/line
					System.out.println(String.format("|TODO: DECCOLM|"));
					break;
				}
				case 4: { // Scroll Mode (DECSCLM)
							// set: smooth (6 lines/sec); reset: jump (fast as
							// possible)
					System.out.println(String.format("|TODO: DECSCLM|"));
					break;
				}
				case 5: { // Screen Mode (DECSCNM)
							// set: reverse screen (white screen, black chars)
							// reset: normal screen (black screen, white chars)
					System.out.println(String.format("|TODO: DECSCNM|"));
					break;
				}
				case 6: { // Origin Mode (DECOM)
							// set: relative to scrolling region; reset:
							// absolute
					System.out.println(String.format("|TODO: DECOM|"));
					break;
				}
				case 7: { // Wrap Mode (DECAWM)
							// set: auto wrap (goto next line; scroll if
							// neccessary)
							// reset: diable auto wrap (overwrite chars at end
							// of line)
					System.out.println(String.format("|TODO: DECAWM|"));
					break;
				}
				case 8: { // Auto Repeat Mode (DECARM)
							// enable / disable auto repeat of pressed keys
					System.out.println(String.format("|TODO: DECARM|"));
					break;
				}
				case 12: {
					String s = set ? "START" : "STOP";
					System.out.println(String.format("||%s BLINKING||", s));
					break;
				}
				case 25: {
					String s = set ? "SHOW" : "HIDE";
					System.out.println(String.format("||%s CURSOR||", s));
					cursorVisible = set;
					break;
				}
				case 1049: {
					String s = set ? "alternate" : "normal";
					System.out.println(String.format("||%s SCREEN||", s));
					if (set) {
						useAlternateScreen();
					} else {
						useNormalScreen();
					}
					break;
				}
				default: {
					break;
				}
				}
			}
		} else if (csi.suffix1 == 'H') { // goto
			int r = 1, c = 1; // appropriate default values
			if (csi.nums.size() >= 1) {
				r = csi.nums.get(0);
			}
			if (csi.nums.size() >= 2) {
				c = csi.nums.get(1);
			}

			System.out.println(String.format("||GOTO:%d,%d||", r, c));

			cursorGoto(r, c);
			return true;
		} else if (csi.suffix1 == 'd') { // this is bogus?
			int r = 1, c = 1; // appropriate default values
			if (csi.nums.size() == 1) {
				r = csi.nums.get(0);
			}
			if (csi.nums.size() >= 2) {
				r = csi.nums.get(0);
				c = csi.nums.get(0);
			}

			System.out.println(String.format("||GOTO:%d,%d||", r, c));

			screen.setCurrentRow(r);
			screen.setCurrentColumn(c >= 1 ? c : 1);
		} else if (csi.suffix1 == '@') { // insert n blank characters
			int n = getValueOrDefault(csi, 1);

			System.out.println(String.format("||insert %d blank chars||", n));

			insertBlankCharacters(n);
		} else if (csi.suffix1 == 'X') { // erase n characters
			int n = getValueOrDefault(csi, 1);

			System.out.println(String.format("||erase %d chars||", n));

			eraseCharacters(n);
		} else if (csi.suffix1 == 'P') { // delete n characters
			int n = getValueOrDefault(csi, 1);

			System.out.println(String.format("||delete %d chars||", n));

			deleteCharacters(n);
		} else if (csi.suffix1 == 'G') { // cursor character absolute
			int n = getValueOrDefault(csi, 1);

			System.out.println(String.format("||cursor char absolute %d||", n));

			cursorCharacterAbsolute(n);
		} else if (csi.suffix1 == 'A') { // cursor up n times
			int n = getValueOrDefault(csi, 1);

			System.out.println(String.format("||cursor %d up||", n));

			cursorUp(n);
		} else if (csi.suffix1 == 'B') { // cursor down n times
			int n = getValueOrDefault(csi, 1);

			System.out.println(String.format("||cursor %d down||", n));

			cursorDown(n);
		} else if (csi.suffix1 == 'C') { // cursor forward n times
			int n = getValueOrDefault(csi, 1);
			System.out.println(String.format("||cursor %d forward||", n));
			screen.setCurrentColumn(screen.getCurrentColumn() + n);
			return true;
		} else if (csi.suffix1 == 'D') { // cursor backward n times
			int n = getValueOrDefault(csi, 1);
			System.out.println(String.format("||cursor %d backwards (%d)||", n,
					screen.getCurrentColumn()));
			int col = screen.getCurrentColumn() - n;
			if (col < 1) {
				col = 1;
			}
			screen.setCurrentColumn(col);
			return true;
		} else if (csi.suffix1 == 'K') { // erase in line
			int n = getValueOrDefault(csi, 0);
			switch (n) {
			case 0:
				// erase to the right
				Row row = screen.getRows().get(screen.getCurrentRow() - 1);
				List<Pixel> pixels = row.getPixels();
				int ccol = screen.getCurrentColumn();
				while (pixels.size() >= ccol) {
					pixels.remove(pixels.size() - 1);
				}
				return true;
			case 1:
				// erase to the left
				break;
			case 2:
				// erase line
				break;
			}
		} else if (csi.suffix1 == 'L') { // insert n lines
			int n = getValueOrDefault(csi, 1);

			System.out.println(String.format("IL: CSI.L"));

			insertLinesBefore(n);
		} else if (csi.suffix1 == 'S') { // scroll up n lines
			int n = getValueOrDefault(csi, 1);

			System.out.println(String.format("||scroll %d up||", n));

			scrollUp(n);
		} else if (csi.suffix1 == 'T') { // scroll down n lines
			int n = getValueOrDefault(csi, 1);

			System.out.println(String.format("||scroll %d down||", n));

			scrollDown(n);
		} else if (csi.suffix1 == 'M') { // delete n lines
			int n = getValueOrDefault(csi, 1);

			System.out.println(String.format("||delete %d lines||", n));

			deleteLines(n);
		} else if (csi.suffix1 == 'J') { // erase in display
			int n = getValueOrDefault(csi, 1);
			switch (n) { // 0: below, 1: above, 2: all, 3: saved lines (xterm)
			case 2: {

				System.out.println(String.format("||ERASE IN DISPLAY, ALL||"));

				eraseAll();
				break;
			}
			default: {

				System.out.println(String.format(
						"||TODO: ERASE IN DISPLAY %d\n||", n));

			}
			}
		} else if (csi.suffix1 == 'r' && csi.prefix == '\0') { // set scrolling
																// region
			int t = 1;
			int b = terminal.getNumberOfRows();
			if (csi.nums.size() >= 2) {
				t = csi.nums.get(0);
				b = csi.nums.get(1);
			}

			System.out
					.println(String.format("||SCROLLING AREA: %d:%d||", t, b));

			screen.setScrollTop(t);
			screen.setScrollBottom(b);
		} else if (csi.suffix1 == 'c' && csi.prefix == '>') { // send device
																// attributes
																// (secondary
																// DA)

			System.out.println(String
					.format("||TODO: DEVICE ATTRIBUTES, PLEASE||"));

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

	private void eraseAll()
	{
		// TODO Auto-generated method stub

	}

	private void deleteLines(int n)
	{
		// TODO Auto-generated method stub

	}

	private void scrollDown(int n)
	{
		// TODO Auto-generated method stub

	}

	private void scrollUp(int n)
	{
		// TODO Auto-generated method stub

	}

	private void insertLinesBefore(int n)
	{
		// ignore if not within scrolling region
		for (int i = 0; i < n; i++) {
			if (screen.getCurrentRow() >= screen.getScrollTop()
					&& screen.getCurrentRow() <= screen.getScrollBottom()) {
				// ok, we're in the scrolling region
				if (screen.getRows().size() >= screen.getScrollBottom()) {
					System.out.println("Remove line "
							+ (screen.getScrollBottom() - 1));
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
					Row drow = screen.getRows().remove(
							screen.getScrollTop() - 1);
					if (screen == screenNormal && screen.getScrollTop() == 1) {
						history.push(drow);
					}
					historyPos += 1;
				} else {
					for (int x = 0; x < n; x++) {
						// check whether we have to retain a row at the bottom
						// of the scrolling region
						if (screen.getRows().size() >= screen.getScrollBottom()) {
							// yes, there are too many rows
							screen.getRows().remove(
									screen.getScrollBottom() - 1);
						}
						// insert new row
						screen.getRows().add(screen.getCurrentRow(), new Row());
						screen.setCurrentRow(screen.getCurrentRow() + 1);
					}
				}
			}
		}
	}

	private void cursorDown(int n)
	{
		// TODO Auto-generated method stub

	}

	private void cursorUp(int n)
	{
		// TODO Auto-generated method stub

	}

	private void cursorCharacterAbsolute(int n)
	{
		// TODO Auto-generated method stub

	}

	private void deleteCharacters(int n)
	{
		// TODO Auto-generated method stub

	}

	private void eraseCharacters(int n)
	{
		// TODO Auto-generated method stub

	}

	private void insertBlankCharacters(int n)
	{
		// TODO Auto-generated method stub

	}

	private void cursorGoto(int r, int c)
	{
		screen.setCurrentColumn(c >= 1 ? c : 1);
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

	private void useNormalScreen()
	{
		System.out.println("Switch to normal Screen!");
	}

	private void useAlternateScreen()
	{
		System.out.println("Switch to alternate Screen!");
	}

	private void setColors(int code)
	{
		if (code == 0) {
			fg = 16;
			bg = 17;
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
			fg = 16;
			fgBright = false;
		} else if (code >= 40 && code <= 47) {
			int c = code - 40;
			bg = c;
			bgBright = false;
		} else if (code == 49) {
			bg = 17;
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
			System.out.println("color: " + code);
		}
	}

	private int getValueOrDefault(Csi csi, int v)
	{
		if (csi.nums.size() > 0) {
			return csi.nums.get(0);
		}
		return v;
	}

	private void setCharset(char c)
	{
		System.out.println("TODO: Set Charset: '" + c + "'");
	}

	private void add(char c)
	{
		if (screen.getCurrentColumn() > terminal.getNumberOfCols()) {
			appendRow();
			screen.setCurrentColumn(1);
		}

		int r = screen.getCurrentRow();

		while (screen.getRows().size() < r) {
			screen.getRows().add(new Row());
		}

		Row row = screen.getRows().get(r - 1);
		List<Pixel> pixels = row.getPixels();

		if (pixels.size() < screen.getCurrentColumn() - 1) {
			int fill = screen.getCurrentColumn() - pixels.size() - 1;
			for (int x = 0; x < fill; x++) {
				pixels.add(createPixel(c));
			}
		} else if (pixels.size() == screen.getCurrentColumn() - 1) {
			// No padding needed, cursor is exactly at insertion position
			pixels.add(createPixel(c));
		} else {
			// Overwriting
			if (screen.getCurrentColumn() - 1 < pixels.size()) {
				pixels.get(screen.getCurrentColumn() - 1).setChar(c);
			}
		}

		screen.setCurrentColumn(screen.getCurrentColumn() + 1);
	}

	private Pixel createPixel(char c)
	{
		Pixel pixel = new Pixel(0, c);
		pixel.setFg(fg);
		pixel.setBg(bg);
		pixel.setBgBright(bgBright);
		pixel.setFgBright(fgBright);
		pixel.setHighlighted(highlighted);
		pixel.setReverse(reverse);
		return pixel;
	}

	private void appendRow()
	{
		int r = screen.getCurrentRow();
		if (r < terminal.getNumberOfRows()) {
			screen.setCurrentRow(r + 1);
		}

		List<Row> rows = screen.getRows();
		rows.add(new Row());

		if (rows.size() > terminal.getNumberOfRows()) {
			System.out.println("pushing row to history");
			Row row = rows.remove(0);
			history.push(row);
		}
	}

	void sendCursor(int keyCode)
	{
		char letter = 'A';
		// @formatter:off
		switch (keyCode) {
			case KeyEvent.VK_UP:    letter = 'A'; break;
			case KeyEvent.VK_DOWN:  letter = 'B'; break;
			case KeyEvent.VK_RIGHT: letter = 'C'; break;
			case KeyEvent.VK_LEFT:  letter = 'D'; break;
			case KeyEvent.VK_HOME:  letter = 'H'; break;
			case KeyEvent.VK_END:   letter = 'F'; break;
		}
		// @formatter:on
		String message;
		if (decCkm) {
			message = String.format("\033O%c", letter);
		} else {
			message = String.format("\033[%c", letter);
		}
		terminal.write(message);
	}

}
