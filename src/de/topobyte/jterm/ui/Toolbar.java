package de.topobyte.jterm.ui;

import javax.swing.JButton;
import javax.swing.JToolBar;

import de.topobyte.jterm.ui.action.GarbageCollectionAction;
import de.topobyte.jterm.ui.action.MemoryInfoAction;

public class Toolbar extends JToolBar
{

	private static final long serialVersionUID = -2843429331813214200L;

	public Toolbar()
	{
		setFloatable(false);
		
		JButton buttonMemoryInfo = new JButton(new MemoryInfoAction());
		JButton buttonGarbageCollection = new JButton(new GarbageCollectionAction());
		
		add(buttonMemoryInfo);
		add(buttonGarbageCollection);
		
		buttonMemoryInfo.setText("Memory");
		buttonGarbageCollection.setText("Garbage");
		
		buttonMemoryInfo.setFocusable(false);
		buttonGarbageCollection.setFocusable(false);
	}
}
