package de.topobyte.jterm.ui;

import javax.swing.JButton;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import de.topobyte.jterm.JTerm;
import de.topobyte.jterm.ui.action.GarbageCollectionAction;
import de.topobyte.jterm.ui.action.MemoryInfoAction;
import de.topobyte.jterm.ui.action.ShowScrollingAreaAction;

public class Toolbar extends JToolBar
{

	private static final long serialVersionUID = -2843429331813214200L;

	public Toolbar(JTerm jterm)
	{
		setFloatable(false);

		JButton buttonMemoryInfo = new JButton(new MemoryInfoAction());
		JButton buttonGarbageCollection = new JButton(
				new GarbageCollectionAction());
		JToggleButton buttonShowScrollingArea = new JToggleButton(
				new ShowScrollingAreaAction(jterm));

		add(buttonMemoryInfo);
		add(buttonGarbageCollection);
		add(buttonShowScrollingArea);

		buttonMemoryInfo.setText("Memory");
		buttonGarbageCollection.setText("Garbage");
		buttonShowScrollingArea.setText("Scrolling Area");

		buttonMemoryInfo.setFocusable(false);
		buttonGarbageCollection.setFocusable(false);
		buttonShowScrollingArea.setFocusable(false);
	}
}
