package de.topobyte.jterm.ui.tabs;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;

import javax.swing.JPanel;

public class CustomTabbedTitleBar extends JPanel implements TabListener
{

	private static final long serialVersionUID = 1L;

	private CustomTabbedContainer container;

	public CustomTabbedTitleBar(CustomTabbedContainer container)
	{
		this.container = container;

		container.addTabListener(this);

		setPreferredSize(new Dimension(-1, 16));
	}

	@Override
	public void paint(Graphics graphics)
	{
		super.paint(graphics);

		Graphics2D g = (Graphics2D) graphics;

		int active = container.getActivePage();
		int total = container.getNumberOfPages();

		if (total == 0) {
			return;
		}

		Insets insets = getInsets();
		g.translate(insets.left, insets.top);

		int width = getWidth() - insets.left - insets.right;
		int height = getHeight() - insets.top - insets.bottom;

		int len = width / total;

		g.setColor(Color.WHITE);
		g.fillRect(0, 0, width, height);

		g.setColor(new Color(0xddddff));
		if (active < total - 1) {
			g.fillRect(active * len, 0, len, height);
		} else {
			g.fillRect(active * len, 0, width - active * len, height);
		}

		// g.setColor(Color.BLACK);
		// g.setStroke(new BasicStroke(2.0f));
		// g.drawRect(0, 0, width, height);

		g.setColor(Color.BLACK);
		g.setStroke(new BasicStroke(1.0f));
		for (int i = 1; i < total; i++) {
			int pos = i * len;
			g.drawLine(pos, 0, pos, height);
		}
	}

	@Override
	public void updateTabs()
	{
		/*
		 * int total = container.getNumberOfPages();
		 * 
		 * if (total == 1 || total == 2) { if (total == 1) {
		 * setPreferredSize(new Dimension(0, 0)); } else if (total == 2) {
		 * setPreferredSize(new Dimension(8, 8)); } getParent().invalidate();
		 * getParent().validate(); }
		 */

		repaint();
	}

}
