package de.topobyte.jterm;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.List;

import javax.swing.JComponent;

public class TerminalWidget extends JComponent
{

	private static final long serialVersionUID = 6198367149770918349L;

	private Terminal terminal;

	private int charWidth = 7;
	private int charHeight = 11;

	private Screen screen;
	private History history;

	public TerminalWidget()
	{
		terminal = new Terminal();

		history = new History();

		screen = new Screen(1, terminal.getNumberOfRows());

		terminal.start();

		setFocusable(true);
		setFocusTraversalKeysEnabled(false);
		addKeyListener(new TerminalKeyAdapter(terminal));

		Thread t = new Thread(new Runnable() {
			@Override
			public void run()
			{
				while (true) {
					byte[] bytes = terminal.read();
					if (bytes == null) {
						break;
					}
					for (int i = 0; i < bytes.length; i++) {
						byte b = bytes[i];
						char c = (char) b;
						System.out.println("Byte: " + b + "..." + c);

						handle(c);
					}
					repaint();
				}
			}
		});
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
				String s = String.format("%c", pixel.getChar());
				g.drawString(s, x, y + charHeight);
			}
		}

		g.setColor(new Color(0x99ff0000, true));
		g.fillRect((screen.getCurrentColumn() - 1) * charWidth,
				(screen.getCurrentRow() - 1) * charHeight, charWidth,
				charHeight);

	}

	private enum State {
		NORMAL, ESC, TITLE, LEFT_BRACKET, RIGHT_BRACKET, CSI, CSI_PREFIX, CSI_NUM, CSI_SUFFIX
	};

	private State state = State.NORMAL;
	private Csi currentCsi = new Csi();

	private void handle(char c)
	{
		switch (state) {
		case CSI_SUFFIX: {
			handleSecondSuffix(c);
			break;
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
				break;
			}
			case ';': {
				handleSemicolon();
				break;
			}
			default: {
				handleSuffix(c);
				break;
			}
			}
			break;
		}
		case LEFT_BRACKET: // TODO: G0 (vt102 uses registers for charsets)
		case RIGHT_BRACKET: { // TODO: G1
			// handle Language setting
			state = State.NORMAL;
			setCharset(c);
			break;
		}
		case ESC: {
			if (c <= 15) {
				break;
			}
			switch (c) {
			case ']': {
				state = State.TITLE;
				break;
			}
			case '[': {
				state = State.CSI;
				currentCsi = new Csi();
				break;
			}
			case '(': {
				state = State.LEFT_BRACKET;
				break;
			}
			case ')': {
				state = State.RIGHT_BRACKET;
				break;
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
				handleEscaped(c);
				break;
			}
			default: {
				// goto to STATE_Normal so that we don't become scrambled
				state = State.NORMAL;
				break;
			}
			}
			break;
		}
		case TITLE: {
			if (c == '\07') {
				state = State.NORMAL;
			}
			break;
		}
		case CSI: {
			switch (c) {
			case '!':
			case '?':
			case '>': {
				state = State.CSI_PREFIX;
				currentCsi.prefix = c;
				break;
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
				break;
			}
			case ';': {
				state = State.CSI_NUM;
				handleSemicolon();
				break;
			}
			default: {
				handleSuffix(c);
				break;
			}
			}
			break;
		}
		case NORMAL: {
			switch (c) {
			case '\u001b': {
				state = State.ESC;
			}
			case '\0': {
				return;
			}
			case '\b': {
				System.out.println("backspace");
				screen.setCurrentColumn(screen.getCurrentColumn() - 1);
				return;
			}
			case '\7': {
				System.out.println("CHAR: BELL");
				return;
			}
			case '\r': {
				System.out.println("CHAR: carriage return");
				screen.setCurrentColumn(1);
				return;
			}
			case '\n': {
				System.out.println("CHAR: line feed");
				appendRow();
				return;
			}
			case '\t': {
				System.out.println("CHAR: tab");
				int x = screen.getCurrentColumn();
				int m = (x - 1) % 8;
				for (int i = 0; i < 8 - m; i++) {
					add(' ');
				}
				return;
			}
			}
			add(c);
		}
		default:
			break;
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

	private void handleSuffix(char c)
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
			handleCsi(currentCsi);
			break;
		}
		}
	}

	private void handleSecondSuffix(char c)
	{
		// TODO: emit an action since a sequence was processed
		currentCsi.suffix2 = c;
		handleCsi(currentCsi);
	}

	private void handleEscaped(char c)
	{
		System.out.println("Handle Escaped: " + c);
	}

	private void handleCsi(Csi csi)
	{
		System.out.println("Handle CSI");
		for (int i = 0; i < csi.nums.size(); i++) {
			int num = csi.nums.get(i);
			System.out.println("CSI number: " + num);
		}

		if ((csi.suffix1 == 'h' || csi.suffix1 == 'l') && csi.prefix == '\0') {
			System.out.println("CSI case 1");
		} else if ((csi.suffix1 == 'h' || csi.suffix1 == 'l')
				&& csi.prefix == '?') { // DECSET / DECRST
			System.out.println("CSI case 2");
		} else if (csi.suffix1 == 'H') { // goto
			// TODO: many cases missing
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
				break;
			case 1:
				// erase to the left
				break;
			case 2:
				// erase line
				break;
			}
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
		System.out.println("Set Charset: '" + c + "'");
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
				pixels.add(new Pixel(0, c));
			}
		} else if (pixels.size() == screen.getCurrentColumn() - 1) {
			// No padding needed, cursor is exactly at insertion position
			pixels.add(new Pixel(0, c));
		} else {
			// Overwriting
			if (screen.getCurrentColumn() - 1 < pixels.size()) {
				pixels.get(screen.getCurrentColumn() - 1).setChar(c);
			}
		}

		screen.setCurrentColumn(screen.getCurrentColumn() + 1);
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
			System.out.println("remove");
			Row row = rows.remove(0);
			history.push(row);
		}
	}

}
