package de.topobyte.jterm.ui.tabs;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

import de.topobyte.jterm.ui.AquaBarTabbedPaneUI;

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
		tabbed.setUI(new AquaBarTabbedPaneUI());
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
	public int getSelectedIndex()
	{
		return tabbed.getSelectedIndex();
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

	@Override
	public Component getComponentAt(int index)
	{
		return tabbed.getComponentAt(index);
	}

	@Override
	public String getTitleAt(int index)
	{
		return tabbed.getTitleAt(index);
	}

}
