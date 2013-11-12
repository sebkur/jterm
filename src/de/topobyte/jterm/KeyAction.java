package de.topobyte.jterm;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;

public class KeyAction extends AbstractAction
{

	private static final long serialVersionUID = 1L;

	private TerminalWidget terminal;
	private int keyCode;

	public KeyAction(TerminalWidget terminal, int keyCode)
	{
		this.terminal = terminal;
		this.keyCode = keyCode;
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		switch (keyCode) {
		case KeyEvent.VK_UP:
		case KeyEvent.VK_DOWN:
		case KeyEvent.VK_RIGHT:
		case KeyEvent.VK_LEFT:
		case KeyEvent.VK_HOME:
		case KeyEvent.VK_END:
			terminal.sendCursor(keyCode);
			break;
		}
	}
}
