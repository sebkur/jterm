package de.topobyte.jterm.ui.tabs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

public class CustomTabbedContainer extends JPanel
{

	private static final long serialVersionUID = 1L;

	private JPanel dummy = new JPanel();
	private Component current = null;
	private List<Component> components = new ArrayList<Component>();

	public CustomTabbedContainer()
	{
		setLayout(new BorderLayout());
		add(dummy, BorderLayout.CENTER);

		setFocusable(true);

		addFocusListener(new FocusAdapter() {

			@Override
			public void focusGained(FocusEvent e)
			{
				current.requestFocus();
			}
		});

		String keyCtrlShiftPageDown = "ctrl-shift-page-down";
		String keyCtrlShiftPageUp = "ctrl-shift-page-up";
		String keyCtrlPageDown = "ctrl-page-down";
		String keyCtrlPageUp = "ctrl-page-up";

		InputMap inputMap = getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN,
				InputEvent.CTRL_DOWN_MASK), keyCtrlPageDown);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP,
				InputEvent.CTRL_DOWN_MASK), keyCtrlPageUp);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN,
				InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK),
				keyCtrlShiftPageDown);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP,
				InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK),
				keyCtrlShiftPageUp);

		ActionMap actionMap = getActionMap();
		actionMap.put(keyCtrlPageDown, new AbstractAction() {

			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e)
			{
				pageDown();
			}
		});
		actionMap.put(keyCtrlPageUp, new AbstractAction() {

			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e)
			{
				pageUp();
			}
		});
		actionMap.put(keyCtrlShiftPageDown, new AbstractAction() {

			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e)
			{
				movePageDown();
			}
		});
		actionMap.put(keyCtrlShiftPageUp, new AbstractAction() {

			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e)
			{
				movePageUp();
			}
		});
	}

	private List<TabListener> listeners = new ArrayList<TabListener>();

	public void addTabListener(TabListener listener)
	{
		listeners.add(listener);
	}

	public void removeTabListener(TabListener listener)
	{
		listeners.remove(listener);
	}

	public void fireTabListeners()
	{
		for (TabListener listener : listeners) {
			listener.updateTabs();
		}
	}

	public int getActivePage()
	{
		return components.indexOf(current);
	}

	public int getNumberOfPages()
	{
		return components.size();
	}

	protected void pageDown()
	{
		nextTab();
	}

	protected void pageUp()
	{
		previousTab();
	}

	private void previousTab()
	{
		if (components.size() < 2) {
			return;
		}
		int index = components.indexOf(current);
		if (index > 0) {
			index = index - 1;
			setTab(index);
		}
	}

	private void nextTab()
	{
		if (components.size() < 2) {
			return;
		}
		int index = components.indexOf(current);
		if (index < components.size() - 1) {
			index = index + 1;
			setTab(index);
		}
	}

	private void setTab(int index)
	{
		Component c = components.get(index);
		remove(current);
		current = c;
		add(current, BorderLayout.CENTER);
		validate();
		repaint();

		fireTabListeners();
	}

	public void addTab(String title, Component component)
	{
		components.add(component);

		if (components.size() == 1) {
			current = component;
			remove(dummy);
			add(component, BorderLayout.CENTER);
		}

		fireTabListeners();
	}

	public void removeTab(Component component)
	{
		int index = components.indexOf(component);
		components.remove(component);
		remove(component);

		int newIndex = index == 0 ? 0 : index - 1;

		if (current == component) {
			if (components.size() == 0) {
				current = dummy;
				add(dummy, BorderLayout.CENTER);
			} else {
				current = components.get(newIndex);
				add(current, BorderLayout.CENTER);
			}
		}
		validate();
		repaint();

		fireTabListeners();
	}

	protected void movePageDown()
	{
		// TODO implement
	}

	protected void movePageUp()
	{
		// TODO implement
	}
}
