package de.topobyte.jterm.core;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;

import javax.swing.AbstractAction;

public class KeyAction extends AbstractAction
{

	private static final long serialVersionUID = 1L;

	private TerminalWidget terminal;
	private int keyCode;
	private int mask;

	public KeyAction(TerminalWidget terminal, int keyCode, int mask)
	{
		this.terminal = terminal;
		this.keyCode = keyCode;
		this.mask = mask;
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if (mask == 0) {
			switch (keyCode) {
			case KeyEvent.VK_UP:
			case KeyEvent.VK_DOWN:
			case KeyEvent.VK_RIGHT:
			case KeyEvent.VK_LEFT:
			case KeyEvent.VK_HOME:
			case KeyEvent.VK_END:
				sendCursor(keyCode);
				break;
			case KeyEvent.VK_INSERT:
			case KeyEvent.VK_DELETE:
			case KeyEvent.VK_PAGE_UP:
			case KeyEvent.VK_PAGE_DOWN:
				sendKeypad(keyCode);
				break;
			case KeyEvent.VK_F1:
			case KeyEvent.VK_F2:
			case KeyEvent.VK_F3:
			case KeyEvent.VK_F4:
			case KeyEvent.VK_F5:
				int fn = keyCode - KeyEvent.VK_F1 + 1;
				int fc = fn + 10;
				terminal.getTerminal().write(String.format("\033[%d~", fc));
				break;
			case KeyEvent.VK_F6:
			case KeyEvent.VK_F7:
			case KeyEvent.VK_F8:
			case KeyEvent.VK_F9:
			case KeyEvent.VK_F10:
				fn = keyCode - KeyEvent.VK_F1 + 1;
				fc = fn + 11;
				terminal.getTerminal().write(String.format("\033[%d~", fc));
				break;
			case KeyEvent.VK_F11:
			case KeyEvent.VK_F12:
				fn = keyCode - KeyEvent.VK_F1 + 1;
				fc = fn + 12;
				terminal.getTerminal().write(String.format("\033[%d~", fc));
				break;
			case KeyEvent.VK_ENTER:
				terminal.getTerminal().write(String.format("%c", 015));
				break;
			}
		} else if (mask == InputEvent.SHIFT_DOWN_MASK) {
			switch (keyCode) {
			case KeyEvent.VK_INSERT:
				pasteClipboard();
				break;
			case KeyEvent.VK_UP:
			case KeyEvent.VK_DOWN:
			case KeyEvent.VK_PAGE_UP:
			case KeyEvent.VK_PAGE_DOWN:
				scroll(keyCode);
				break;
			}
		}
	}

	private void scroll(int keyCode)
	{
		History history = terminal.getHistory();
		switch (keyCode) {
		case KeyEvent.VK_UP:
			if (history.getPos() > 0) {
				history.setPos(history.getPos() - 1);
			}
			break;
		case KeyEvent.VK_DOWN:
			if (history.getPos() < history.getLength()) {
				history.setPos(history.getPos() + 1);
			}
			break;
		case KeyEvent.VK_PAGE_UP:
			if (history.getPos() > 0) {
				int newPos = history.getPos()
						- terminal.getTerminal().getNumberOfRows();
				history.setPos(newPos >= 0 ? newPos : 0);
			}
			break;
		case KeyEvent.VK_PAGE_DOWN:
			int n = history.getLength();
			if (history.getPos() < n) {
				int newPos = history.getPos()
						+ terminal.getTerminal().getNumberOfRows();
				history.setPos(newPos <= n ? newPos : n);
			}
			break;
		}
		terminal.repaint();
	}

	// See: http://invisible-island.net/xterm/ctlseqs/ctlseqs.html
	// Section: PC-Style Function Keys

	private void sendCursor(int keyCode) // xterm doc
	// depends on DECCKM
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
		if (terminal.decCkm) {
			message = String.format("\033O%c", letter);
		} else {
			message = String.format("\033[%c", letter);
		}
		terminal.ensureBottomLineVisible();
		terminal.getTerminal().write(message);
	}

	private void sendKeypad(int keyCode)
	{
		char letter = '2';
		// @formatter:off
			switch (keyCode) {
			case KeyEvent.VK_INSERT:    letter = '2'; break;
			case KeyEvent.VK_DELETE:    letter = '3'; break;
			case KeyEvent.VK_PAGE_UP:   letter = '5'; break;
			case KeyEvent.VK_PAGE_DOWN: letter = '6'; break;
			}
			// @formatter:on
		String message = String.format("\033[%c~", letter);
		terminal.getTerminal().write(message);
	}

	private void pasteClipboard()
	{
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Clipboard clipboard = toolkit.getSystemClipboard();
		Transferable transferable = clipboard.getContents(null);
		try {
			Object data = transferable.getTransferData(DataFlavor.stringFlavor);
			String text = (String) data;
			terminal.getTerminal().write(text);
		} catch (UnsupportedFlavorException e) {
			System.out.println("Paste Clipboard: Unsupported flavor");
		} catch (IOException e) {
			System.out
					.println("Paste Clipboard: IOException: " + e.getMessage());
		}
	}
}
