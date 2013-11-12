package de.topobyte.jterm;

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
		add(keyCode, new KeyAction(terminal, keyCode));
	}
}
