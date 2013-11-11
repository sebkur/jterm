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

		screen = new Screen(terminal.getNumberOfRows(),
				terminal.getNumberOfCols());

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
					for (int i = 0; i < bytes.length; i++) {
						byte b = bytes[i];
						char c = (char) b;
						// System.out.println("Byte: " + b + "..." + c);

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
	}

	private enum State {
		NORMAL, ESC, TITLE, LEFT_BRACKET, RIGHT_BRACKET, CSI, CSI_PREFIX, CSI_NUM, CSI_SUFFIX
	};

	private State state = State.NORMAL;

	private void handle(char c)
	{
		System.out.println(state);
		switch (state) {
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
				// clear_current_csi(terminal);
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
				// handle_escaped(c);
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
		case NORMAL: {
			switch (c) {
			case '\u001b': {
				state = State.ESC;
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
			}
			}
			add(c);
		}
		default:
			break;
		}
	}

	private void add(char c)
	{
		int r = screen.getCurrentRow();

		while (screen.getRows().size() < r) {
			screen.getRows().add(new Row());
		}

		Row row = screen.getRows().get(r - 1);
		row.getPixels().add(new Pixel(0, c));

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
