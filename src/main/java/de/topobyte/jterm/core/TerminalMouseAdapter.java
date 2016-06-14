package de.topobyte.jterm.core;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import de.topobyte.jterm.ui.Statusbar;

public class TerminalMouseAdapter extends MouseAdapter
{

	private TerminalWidget widget;
	private Statusbar statusbar;

	public TerminalMouseAdapter(TerminalWidget widget, Statusbar statusbar)
	{
		this.widget = widget;
		this.statusbar = statusbar;
	}

	@Override
	public void mouseMoved(MouseEvent e)
	{
		super.mouseMoved(e);
		int x = e.getX() / widget.charWidth + 1;
		int y = e.getY() / widget.charHeight + 1;
		statusbar.updateText(x + "," + y);
	}

}
