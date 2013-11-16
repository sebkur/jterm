package de.topobyte.jterm;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;

import de.topobyte.jterm.core.TerminalMouseAdapter;
import de.topobyte.jterm.core.TerminalWidget;
import de.topobyte.jterm.ui.Statusbar;
import de.topobyte.jterm.ui.Toolbar;

public class JTermSingle
{

	public static void main(String[] args)
	{
		System.loadLibrary("terminal");

		JFrame frame = new JFrame("JTerm");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JPanel content = new JPanel(new BorderLayout());
		frame.setContentPane(content);

		Toolbar toolbar = new Toolbar();
		content.add(toolbar, BorderLayout.NORTH);

		TerminalWidget terminalWidget = new TerminalWidget();
		content.add(terminalWidget, BorderLayout.CENTER);

		Statusbar statusbar = new Statusbar();
		content.add(statusbar, BorderLayout.SOUTH);

		TerminalMouseAdapter mouseAdapter = new TerminalMouseAdapter(
				terminalWidget, statusbar);
		terminalWidget.addMouseMotionListener(mouseAdapter);

		frame.setLocationByPlatform(true);
		frame.setSize(600, 500);
		frame.setVisible(true);
	}

}
