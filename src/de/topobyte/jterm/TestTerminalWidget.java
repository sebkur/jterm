package de.topobyte.jterm;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class TestTerminalWidget {

	public static void main(String[] args) {
		System.loadLibrary("terminal");

		JFrame frame = new JFrame("JTerm");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JPanel content = new JPanel(new BorderLayout());
		frame.setContentPane(content);

		TerminalWidget terminalWidget = new TerminalWidget();
		content.add(terminalWidget, BorderLayout.CENTER);

		frame.setLocationByPlatform(true);
		frame.setSize(600, 500);
		frame.setVisible(true);
	}

}
