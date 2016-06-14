package de.topobyte.jterm.ui.tabs;

import java.awt.BorderLayout;
import java.awt.Component;

public class HidingTabbed extends Tabbed
{

	private static final long serialVersionUID = -3793793795046133712L;

	private Tabbed tabbed;

	private Component oneC = null;
	private String oneS = null;

	public HidingTabbed(Tabbed tabbed)
	{
		this.tabbed = tabbed;
		setLayout(new BorderLayout());
		if (tabbed.getNumberOfTabs() == 1) {
			add(tabbed.getComponentAt(0), BorderLayout.CENTER);
		} else if (tabbed.getNumberOfTabs() > 1) {
			add(tabbed, BorderLayout.CENTER);
		}
	}

	@Override
	public void addTab(String title, Component component)
	{
		if (tabbed.getNumberOfTabs() == 0) {
			if (oneC == null) {
				add(component, BorderLayout.CENTER);
				oneC = component;
				oneS = title;
			} else {
				remove(oneC);
				add(tabbed, BorderLayout.CENTER);
				tabbed.addTab(oneS, oneC);
				oneC = null;
				oneS = null;
				tabbed.addTab(title, component);
			}
		} else {
			tabbed.addTab(title, component);
		}
	}

	@Override
	public void removeTab(Component component)
	{
		if (oneC != null) {
			remove(oneC);
			oneC = null;
			oneS = null;
		}
		tabbed.removeTab(component);
		if (tabbed.getNumberOfTabs() == 1) {
			remove(tabbed);
			oneC = tabbed.getComponentAt(0);
			oneS = tabbed.getTitleAt(0);
			tabbed.removeTab(oneC);
			add(oneC, BorderLayout.CENTER);
		}
	}

	@Override
	public int getNumberOfTabs()
	{
		if (oneC != null) {
			return 1;
		}
		return tabbed.getNumberOfTabs();
	}

	@Override
	public int getSelectedIndex()
	{
		if (oneC != null) {
			return 0;
		}
		return tabbed.getSelectedIndex();
	}

	@Override
	public void setSelectedIndex(int index)
	{
		if (oneC != null) {
			return;
		}
		tabbed.setSelectedIndex(index);
	}

	@Override
	public void setSelectedComponent(Component component)
	{
		if (oneC != null) {
			return;
		}
		tabbed.setSelectedComponent(component);
	}

	@Override
	public Component getComponentAt(int index)
	{
		if (oneC != null) {
			return oneC;
		}
		return tabbed.getComponentAt(index);
	}

	@Override
	public String getTitleAt(int index)
	{
		return tabbed.getTitleAt(index);
	}

}
