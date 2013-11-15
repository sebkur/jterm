package de.topobyte.jterm.ui;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

public class Statusbar extends JPanel
{

	private static final long serialVersionUID = -8373301698998009774L;

	private JLabel label = new JLabel();

	public Statusbar()
	{
		setLayout(new BorderLayout());
		updateText(null);
		add(label, BorderLayout.WEST);
	}

	public void updateText(String text)
	{
		if (text == null || text.length() == 0) {
			text = " ";
		}
		label.setText(text);
	}

}
