package de.topobyte.jterm.ui.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

public class GarbageCollectionAction extends AbstractAction
{

	private static final long serialVersionUID = 1L;

	@Override
	public void actionPerformed(ActionEvent e)
	{
		Runtime runtime = Runtime.getRuntime();
		runtime.gc();
	}

}
