package de.topobyte.jterm;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.List;

import javax.swing.JComponent;

public class TerminalWidget extends JComponent {

	private static final long serialVersionUID = 6198367149770918349L;

	private Terminal terminal;

	private int charWidth = 7;
	private int charHeight = 11;

	private Screen screen;

	public TerminalWidget() {
		terminal = new Terminal();

		screen = new Screen(terminal.getNumberOfRows(),
				terminal.getNumberOfCols());

		terminal.start();

		setFocusable(true);
		addKeyListener(new TerminalKeyAdapter(terminal));

		byte[] bytes = terminal.read();
		for (int i = 0; i < bytes.length; i++) {
			byte b = bytes[i];
			char c = (char) b;
			System.out.println("Byte: " + b + "..." + c);

			handle(c);
		}
	}

	private void handle(char c) {
		int r = screen.getCurrentRow();
		List<Row> rows = screen.getRows();
		if (rows.size() < r) {
			rows.add(new Row());
		}
		Row row = rows.get(r - 1);
		row.getPixels().add(new Pixel(0, c));
	}

	@Override
	public void paint(Graphics graphics) {
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

}
