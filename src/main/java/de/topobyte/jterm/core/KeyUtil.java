package de.topobyte.jterm.core;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.KeyStroke;

import javax.swing.InputMap;

public class KeyUtil
{

	private InputMap inputMap;
	private ActionMap actionMap;

	public KeyUtil(InputMap inputMap, ActionMap actionMap)
	{
		this.inputMap = inputMap;
		this.actionMap = actionMap;
	}

	public void add(int keyCode, int mask, Action action)
	{
		String key = "auto" + mask + "_" + keyCode;
		inputMap.put(KeyStroke.getKeyStroke(keyCode, mask), key);
		actionMap.put(key, action);
	}

}
