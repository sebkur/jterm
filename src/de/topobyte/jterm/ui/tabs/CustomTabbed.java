package de.topobyte.jterm.ui.tabs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.BorderFactory;

public class CustomTabbed extends Tabbed
{

	private static final long serialVersionUID = 1L;

	private CustomTabbedTitleBar titlebar;
	private CustomTabbedContainer container;

	public CustomTabbed()
	{
		container = new CustomTabbedContainer();
		titlebar = new CustomTabbedTitleBar(container);

		setLayout(new BorderLayout());
		add(titlebar, BorderLayout.NORTH);
		add(container, BorderLayout.CENTER);

		titlebar.setBorder(BorderFactory.createLineBorder(Color.BLACK));

		setFocusable(true);
		titlebar.setFocusable(false);

		addFocusListener(new FocusAdapter() {

			@Override
			public void focusGained(FocusEvent e)
			{
				container.requestFocus();
			}
		});
	}

	@Override
	public int getNumberOfTabs()
	{
		return container.getNumberOfPages();
	}

	@Override
	public void addTab(String title, Component component)
	{
		container.addTab(title, component);
	}

	@Override
	public void removeTab(Component component)
	{
		container.removeTab(component);
	}

	@Override
	public int getSelectedIndex()
	{
		return container.getSelectedIndex();
	}

	@Override
	public void setSelectedIndex(int index)
	{
		container.setSelectedIndex(index);
	}

	@Override
	public void setSelectedComponent(Component component)
	{
		container.setSelectedComponent(component);
	}

	@Override
	public Component getComponentAt(int index)
	{
		return container.getComponentAt(index);
	}
}
