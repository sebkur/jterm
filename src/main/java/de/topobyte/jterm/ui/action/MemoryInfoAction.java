package de.topobyte.jterm.ui.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

public class MemoryInfoAction extends AbstractAction
{

	private static final long serialVersionUID = 1L;

	@Override
	public void actionPerformed(ActionEvent e)
	{
		Runtime runtime = Runtime.getRuntime();
		long memory = runtime.totalMemory() - runtime.freeMemory();
		System.out.println("Used memory: " + (memory / 1024 / 1024) + "MB");
	}

}
