package de.topobyte.jterm.core;

import javax.swing.ActionMap;
import javax.swing.InputMap;

public class TerminalWidgetKeyUtil extends KeyUtil
{

	private TerminalWidget terminal;

	public TerminalWidgetKeyUtil(TerminalWidget terminal, InputMap inputMap,
			ActionMap actionMap)
	{
		super(inputMap, actionMap);
		this.terminal = terminal;
	}

	public void addKeyAction(int keyCode)
	{
		add(keyCode, 0, new KeyAction(terminal, keyCode, 0));
	}

	public void addKeyAction(int keyCode, int mask)
	{
		add(keyCode, mask, new KeyAction(terminal, keyCode, mask));
	}
}
