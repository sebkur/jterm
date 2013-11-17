package de.topobyte.jterm;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import de.topobyte.jterm.core.TerminalClosedListener;
import de.topobyte.jterm.core.TerminalMouseAdapter;
import de.topobyte.jterm.core.TerminalWidget;
import de.topobyte.jterm.ui.Statusbar;
import de.topobyte.jterm.ui.Toolbar;
import de.topobyte.jterm.ui.tabs.CustomTabbed;
import de.topobyte.jterm.ui.tabs.Tabbed;

public class JTerm
{

	public static void main(String[] args)
	{
		System.loadLibrary("terminal");

		new JTerm();
	}

	private JFrame frame;
	private Tabbed tabbed;
	private Toolbar toolbar;
	private Statusbar statusbar;

	public JTerm()
	{
		frame = new JFrame("JTerm");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JPanel content = new JPanel(new BorderLayout());
		frame.setContentPane(content);

		// tabbed = new TabbedPaneTabbed();
		tabbed = new CustomTabbed();
		content.add(tabbed, BorderLayout.CENTER);

		statusbar = new Statusbar();
		content.add(statusbar, BorderLayout.SOUTH);

		addTab();

		toolbar = new Toolbar(this);
		content.add(toolbar, BorderLayout.NORTH);

		frame.setLocationByPlatform(true);
		frame.setSize(600, 500);
		frame.setVisible(true);

		String keyCtrlShiftT = "ctrl-shift-t";

		InputMap inputMap = tabbed
				.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		inputMap.put(
				KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_DOWN_MASK
						| InputEvent.SHIFT_DOWN_MASK), keyCtrlShiftT);
		ActionMap actionMap = tabbed.getActionMap();
		actionMap.put(keyCtrlShiftT, new AbstractAction() {

			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e)
			{
				addTab();
			}
		});

	}

	protected void addTab()
	{
		final String title = "term";
		TerminalWidget terminalWidget = new TerminalWidget();

		TerminalMouseAdapter mouseAdapter = new TerminalMouseAdapter(
				terminalWidget, statusbar);
		terminalWidget.addMouseMotionListener(mouseAdapter);

		terminalWidget.addTerminalClosedListener(new RemovalListener(
				terminalWidget));

		if (tabbed.getNumberOfTabs() != 0) {
			terminalWidget.setDrawScrollingArea(isShowScrollingArea());
		}

		tabbed.addTab(title, terminalWidget);
		tabbed.setSelectedComponent(terminalWidget);
	}

	public class RemovalListener implements TerminalClosedListener
	{

		private TerminalWidget terminalWidget;

		public RemovalListener(TerminalWidget terminalWidget)
		{
			this.terminalWidget = terminalWidget;
		}

		@Override
		public void terminalClosed()
		{
			tabbed.removeTab(terminalWidget);
			if (tabbed.getNumberOfTabs() == 0) {
				System.exit(0);
			}
		}
	}

	public boolean isShowScrollingArea()
	{
		TerminalWidget widget = (TerminalWidget) tabbed.getComponentAt(0);
		return widget.isDrawScrollingArea();
	}

	public void setShowScrollingArea(boolean showScrollingArea)
	{
		for (int i = 0; i < tabbed.getNumberOfTabs(); i++) {
			TerminalWidget widget = (TerminalWidget) tabbed.getComponentAt(i);
			widget.setDrawScrollingArea(showScrollingArea);
			if (tabbed.getSelectedIndex() == i) {
				widget.repaint();
			}
		}
	}

}
