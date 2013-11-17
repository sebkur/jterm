package de.topobyte.jterm.ui.tabs;

import java.awt.Component;

import javax.swing.JPanel;

public abstract class Tabbed extends JPanel
{

	private static final long serialVersionUID = 1L;

	public abstract void addTab(String title, Component component);

	public abstract void removeTab(Component component);

	public abstract int getNumberOfTabs();

	public abstract void setSelectedIndex(int index);

	public abstract void setSelectedComponent(Component component);

}
