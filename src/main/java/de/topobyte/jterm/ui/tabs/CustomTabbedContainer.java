package de.topobyte.jterm.ui.tabs;

import java.awt.CardLayout;
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

	private Component current = null;
	private List<Component> components = new ArrayList<>();
	private List<String> titles = new ArrayList<>();
	private List<String> ids = new ArrayList<>();

	private CardLayout cardLayout;
	private int i = 0;

	private String id()
	{
		return "" + i;
	}

	private void increment()
	{
		i++;
	}

	public CustomTabbedContainer()
	{
		cardLayout = new CardLayout();
		setLayout(cardLayout);

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
		inputMap.put(
				KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN,
						InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK),
				keyCtrlShiftPageDown);
		inputMap.put(
				KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP,
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

	private List<TabListener> listeners = new ArrayList<>();

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
		if (c == current) {
			return;
		}

		String id = ids.get(index);
		cardLayout.show(this, id);
		current = c;
		c.requestFocus();

		fireTabListeners();
	}

	public void addTab(String title, Component component)
	{
		add(component, id());
		components.add(component);
		titles.add(title);
		ids.add(id());
		increment();

		if (components.size() == 1) {
			current = component;
		}

		fireTabListeners();
	}

	public void removeTab(Component component)
	{
		int index = components.indexOf(component);
		if (index < 0) {
			return;
		}
		components.remove(component);
		titles.remove(index);
		ids.remove(index);
		remove(component);

		if (components.size() == 0) {
			return;
		}

		int newIndex = index == 0 ? 0 : index - 1;

		String id = ids.get(newIndex);
		cardLayout.show(this, id);
		current = components.get(newIndex);

		current.requestFocus();

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

	public int getSelectedIndex()
	{
		return getActivePage();
	}

	public void setSelectedIndex(int index)
	{
		setTab(index);
	}

	public void setSelectedComponent(Component component)
	{
		int index = components.indexOf(component);
		if (index < 0) {
			return;
		}
		setSelectedIndex(index);
	}

	public Component getComponentAt(int index)
	{
		return components.get(index);
	}

	public String getTitleAt(int index)
	{
		return titles.get(index);
	}
}
