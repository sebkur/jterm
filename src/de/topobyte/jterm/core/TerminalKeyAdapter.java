package de.topobyte.jterm.core;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class TerminalKeyAdapter extends KeyAdapter
{

	private Terminal terminal;

	public TerminalKeyAdapter(Terminal terminal)
	{
		this.terminal = terminal;
	}

	@Override
	public void keyTyped(KeyEvent e)
	{
		// System.out.println("key typed: " + e.getKeyChar());
		String message = String.format("%c", e.getKeyChar());
		if (e.getKeyChar() == '\010') {
			byte ec = terminal.getEraseCharacter();
			message = String.format("%c", ec);
		}
		terminal.write(message);
	}

}
