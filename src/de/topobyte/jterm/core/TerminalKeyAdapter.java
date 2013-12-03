package de.topobyte.jterm.core;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class TerminalKeyAdapter extends KeyAdapter
{

	private TerminalWidget terminalWidget;
	private Terminal terminal;

	public TerminalKeyAdapter(TerminalWidget terminalWidget)
	{
		this.terminalWidget = terminalWidget;
		this.terminal = terminalWidget.getTerminal();
	}

	@Override
	public void keyTyped(KeyEvent e)
	{
		// System.out.println("key typed: " + e.getKeyChar());
		if (e.getKeyChar() == 10) {
			return;
		}
		String message = String.format("%c", e.getKeyChar());
		if (e.getKeyChar() == '\010') {
			// Backspace character. Get erase character and use it if != 0
			byte ec = terminal.getEraseCharacter();
			if (ec != 0) {
				message = String.format("%c", ec);
			}
		} else if (e.getKeyChar() == 127) {
			return;
		}
		terminalWidget.ensureBottomLineVisible();
		terminal.write(message);
	}
}
