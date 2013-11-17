package de.topobyte.jterm.ui.tabs;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

public class TabbedPaneTabbed extends Tabbed
{

	private static final long serialVersionUID = 1L;

	private JTabbedPane tabbed = new JTabbedPane(SwingConstants.TOP,
			JTabbedPane.SCROLL_TAB_LAYOUT);

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

	@Override
	public void setSelectedIndex(int index)
	{
		tabbed.setSelectedIndex(index);
	}

	@Override
	public void setSelectedComponent(Component component)
	{
		tabbed.setSelectedComponent(component);
	}

}
