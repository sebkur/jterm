package de.topobyte.jterm.ui.tabs;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JTabbedPane;

public class TabbedPaneTabbed extends Tabbed
{

	private static final long serialVersionUID = 1L;

	private JTabbedPane tabbed = new JTabbedPane();

	public TabbedPaneTabbed()
	{
		setLayout(new BorderLayout());
		add(tabbed, BorderLayout.CENTER);
		tabbed.setFocusable(false);
	}

	@Override
	public void addTab(String title, Component component)
	{
		tabbed.add(title, component);
	}

	@Override
	public void removeTab(Component component)
	{
		tabbed.remove(component);
	}

	@Override
	public int getNumberOfTabs()
	{
		return tabbed.getTabCount();
	}

}
