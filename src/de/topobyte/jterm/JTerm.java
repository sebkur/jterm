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
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;

import de.topobyte.jterm.core.TerminalClosedListener;
import de.topobyte.jterm.core.TerminalMouseAdapter;
import de.topobyte.jterm.core.TerminalWidget;
import de.topobyte.jterm.ui.Statusbar;
import de.topobyte.jterm.ui.Toolbar;

public class JTerm
{

	public static void main(String[] args)
	{
		System.loadLibrary("terminal");

		new JTerm();
	}

	private JFrame frame;
	private JTabbedPane tabbed;
	private Toolbar toolbar;
	private Statusbar statusbar;

	public JTerm()
	{
		frame = new JFrame("JTerm");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JPanel content = new JPanel(new BorderLayout());
		frame.setContentPane(content);

		toolbar = new Toolbar();
		content.add(toolbar, BorderLayout.NORTH);

		tabbed = new JTabbedPane();
		content.add(tabbed, BorderLayout.CENTER);
		tabbed.setFocusable(false);

		statusbar = new Statusbar();
		content.add(statusbar, BorderLayout.SOUTH);

		addTab();

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
		tabbed.add(title, terminalWidget);

		TerminalMouseAdapter mouseAdapter = new TerminalMouseAdapter(
				terminalWidget, statusbar);
		terminalWidget.addMouseMotionListener(mouseAdapter);

		terminalWidget.addTerminalClosedListener(new RemovalListener(
				terminalWidget));
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
			tabbed.remove(terminalWidget);
			if (tabbed.getComponentCount() == 0) {
				System.exit(0);
			}
		}
	}

}
