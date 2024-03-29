package de.topobyte.jterm;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.border.EtchedBorder;

import de.topobyte.jterm.core.Terminal;
import de.topobyte.jterm.core.TerminalClosedListener;
import de.topobyte.jterm.core.TerminalMouseAdapter;
import de.topobyte.jterm.core.TerminalWidget;
import de.topobyte.jterm.themes.Themes;
import de.topobyte.jterm.ui.Statusbar;
import de.topobyte.jterm.ui.Toolbar;
import de.topobyte.jterm.ui.tabs.CustomTabbed;
import de.topobyte.jterm.ui.tabs.HidingTabbed;
import de.topobyte.jterm.ui.tabs.Tabbed;
import de.topobyte.shared.preferences.SharedPreferences;
import de.topobyte.swing.util.SwingUtils;

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

	private int theme = 0;

	public JTerm()
	{
		SwingUtils.setUiScale(SharedPreferences.getUIScale());

		frame = new JFrame("JTerm");
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		String filename = "logo.png";
		BufferedImage image = null;
		try {
			InputStream is = Thread.currentThread().getContextClassLoader()
					.getResourceAsStream(filename);
			image = ImageIO.read(is);
			is.close();
		} catch (Exception e) {
			System.out.println("unable to load icon: " + filename
					+ " exception message: " + e.getMessage());
			e.printStackTrace();
		}
		frame.setIconImage(image);

		JPanel content = new JPanel(new BorderLayout());
		frame.setContentPane(content);

		// tabbed = new HidingTabbed(new TabbedPaneTabbed());
		// tabbed = new CustomTabbed();
		tabbed = new HidingTabbed(new CustomTabbed());

		content.add(tabbed, BorderLayout.CENTER);

		statusbar = new Statusbar();
		statusbar.setVisible(false);
		content.add(statusbar, BorderLayout.SOUTH);

		addTab();

		toolbar = new Toolbar(this);
		toolbar.setVisible(false);
		content.add(toolbar, BorderLayout.NORTH);

		frame.setLocationByPlatform(true);
		frame.setSize(600, 500);
		frame.setVisible(true);

		String keyCtrlShiftT = "ctrl-shift-t";
		String keyCtrlShiftF8 = "ctrl-shift-f8";
		String keyCtrlShiftF9 = "ctrl-shift-f9";
		String keyCtrlShiftF10 = "ctrl-shift-f10";

		InputMap inputMap = tabbed
				.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		inputMap.put(
				KeyStroke.getKeyStroke(KeyEvent.VK_T,
						InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK),
				keyCtrlShiftT);
		inputMap.put(
				KeyStroke.getKeyStroke(KeyEvent.VK_F8,
						InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK),
				keyCtrlShiftF8);
		inputMap.put(
				KeyStroke.getKeyStroke(KeyEvent.VK_F9,
						InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK),
				keyCtrlShiftF9);
		inputMap.put(
				KeyStroke.getKeyStroke(KeyEvent.VK_F10,
						InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK),
				keyCtrlShiftF10);

		ActionMap actionMap = tabbed.getActionMap();
		actionMap.put(keyCtrlShiftT, new AbstractAction() {

			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e)
			{
				addTab();
			}
		});
		actionMap.put(keyCtrlShiftF8, new AbstractAction() {

			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e)
			{
				statusbar.setVisible(!statusbar.isVisible());
			}
		});
		actionMap.put(keyCtrlShiftF9, new AbstractAction() {

			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e)
			{
				toolbar.setVisible(!toolbar.isVisible());
			}
		});
		actionMap.put(keyCtrlShiftF10, new AbstractAction() {

			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e)
			{
				switchTheme();
				// Toggle menu
			}
		});

		frame.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e)
			{
				int status = JOptionPane.showConfirmDialog(frame, "Exit JTerm?",
						"Confirm Exit", JOptionPane.OK_CANCEL_OPTION);
				if (status == JOptionPane.YES_OPTION) {
					System.exit(0);
				}
			}

		});
	}

	protected void addTab()
	{
		final String title = "term";

		String pwd = null;
		if (tabbed.getNumberOfTabs() != 0) {
			int index = tabbed.getSelectedIndex();
			TerminalWidget widget = (TerminalWidget) tabbed
					.getComponentAt(index);
			Terminal terminal = widget.getTerminal();
			pwd = terminal.getPwd();
		}

		TerminalWidget terminalWidget = new TerminalWidget(pwd);
		terminalWidget.setBorder(
				BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

		TerminalMouseAdapter mouseAdapter = new TerminalMouseAdapter(
				terminalWidget, statusbar);
		terminalWidget.addMouseMotionListener(mouseAdapter);

		terminalWidget
				.addTerminalClosedListener(new RemovalListener(terminalWidget));

		terminalWidget.setTheme(theme);

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

	protected void switchTheme()
	{
		theme = (theme + 1) % Themes.THEMES.size();

		for (int i = 0; i < tabbed.getNumberOfTabs(); i++) {
			TerminalWidget widget = (TerminalWidget) tabbed.getComponentAt(i);
			widget.setTheme(theme);
		}
	}

}
