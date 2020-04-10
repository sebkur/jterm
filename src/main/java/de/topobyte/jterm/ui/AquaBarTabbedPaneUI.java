package de.topobyte.jterm.ui;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicTabbedPaneUI;

public class AquaBarTabbedPaneUI extends BasicTabbedPaneUI
{

	private static final Insets NO_INSETS = new Insets(0, 0, 0, 0);
	private ColorSet selectedColorSet;
	private ColorSet defaultColorSet;
	private ColorSet hoverColorSet;
	private boolean contentTopBorderDrawn = true;
	private Color lineColor = new Color(158, 158, 158);
	private Color dividerColor = new Color(200, 200, 200);
	private Insets contentInsets = new Insets(10, 10, 10, 10);
	private int lastRollOverTab = -1;

	private int tabHeight = 20;

	public static ComponentUI createUI(JComponent c)
	{
		return new AquaBarTabbedPaneUI();
	}

	public AquaBarTabbedPaneUI()
	{
		selectedColorSet = new ColorSet();
		selectedColorSet.topGradColor1 = new Color(233, 237, 248);
		selectedColorSet.topGradColor2 = new Color(158, 199, 240);

		selectedColorSet.bottomGradColor1 = new Color(112, 173, 239);
		selectedColorSet.bottomGradColor2 = new Color(183, 244, 253);

		defaultColorSet = new ColorSet();
		defaultColorSet.topGradColor1 = new Color(253, 253, 253);
		defaultColorSet.topGradColor2 = new Color(237, 237, 237);

		defaultColorSet.bottomGradColor1 = new Color(222, 222, 222);
		defaultColorSet.bottomGradColor2 = new Color(255, 255, 255);

		hoverColorSet = new ColorSet();
		hoverColorSet.topGradColor1 = new Color(244, 244, 244);
		hoverColorSet.topGradColor2 = new Color(223, 223, 223);

		hoverColorSet.bottomGradColor1 = new Color(211, 211, 211);
		hoverColorSet.bottomGradColor2 = new Color(235, 235, 235);

		maxTabHeight = tabHeight;

		setContentInsets(0);
	}

	public void setContentTopBorderDrawn(boolean b)
	{
		contentTopBorderDrawn = b;
	}

	public void setContentInsets(Insets i)
	{
		contentInsets = i;
	}

	public void setContentInsets(int i)
	{
		contentInsets = new Insets(i, i, i, i);
	}

	@Override
	public int getTabRunCount(JTabbedPane pane)
	{
		return 1;
	}

	@Override
	protected void installDefaults()
	{
		super.installDefaults();

		RollOverAdapter l = new RollOverAdapter();
		tabPane.addMouseListener(l);
		tabPane.addMouseMotionListener(l);

		tabAreaInsets = NO_INSETS;
		tabInsets = new Insets(0, 0, 0, 1);
	}

	protected boolean scrollableTabLayoutEnabled()
	{
		return false;
	}

	@Override
	protected Insets getContentBorderInsets(int tabPlacement)
	{
		return contentInsets;
	}

	@Override
	protected int calculateTabHeight(int tabPlacement, int tabIndex,
			int fontHeight)
	{
		return tabHeight + 1;
	}

	@Override
	protected int calculateTabWidth(int tabPlacement, int tabIndex,
			FontMetrics metrics)
	{
		int w = super.calculateTabWidth(tabPlacement, tabIndex, metrics);
		int wid = metrics.charWidth('M');
		w += wid * 2;
		return w;
	}

	@Override
	protected int calculateMaxTabHeight(int tabPlacement)
	{
		return tabHeight + 1;
	}

	@Override
	protected void paintTabArea(Graphics g, int tabPlacement, int selectedIndex)
	{
		Graphics2D g2d = (Graphics2D) g;
		g2d.setPaint(new GradientPaint(0, 0, defaultColorSet.topGradColor1, 0,
				tabHeight / 2, defaultColorSet.topGradColor2));
		g2d.fillRect(0, 0, tabPane.getWidth(), tabHeight / 2);

		g2d.setPaint(new GradientPaint(0, 10, defaultColorSet.bottomGradColor1,
				0, tabHeight + 1, defaultColorSet.bottomGradColor2));
		g2d.fillRect(0, tabHeight / 2, tabPane.getWidth(), tabHeight / 2 + 1);
		super.paintTabArea(g, tabPlacement, selectedIndex);

		if (contentTopBorderDrawn) {
			g2d.setColor(lineColor);
			g2d.drawLine(0, tabHeight, tabPane.getWidth() - 1, tabHeight);
		}
	}

	@Override
	protected void paintTabBackground(Graphics g, int tabPlacement,
			int tabIndex, int x, int y, int w, int h, boolean isSelected)
	{
		Graphics2D g2d = (Graphics2D) g;
		ColorSet colorSet;

		Rectangle rect = rects[tabIndex];

		if (isSelected) {
			colorSet = selectedColorSet;
		} else if (getRolloverTab() == tabIndex) {
			colorSet = hoverColorSet;
		} else {
			colorSet = defaultColorSet;
		}

		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		int width = rect.width;
		int xpos = rect.x;
		if (tabIndex > 0) {
			width--;
			xpos++;
		}

		g2d.setPaint(new GradientPaint(xpos, 0, colorSet.topGradColor1, xpos,
				tabHeight / 2, colorSet.topGradColor2));
		g2d.fillRect(xpos, 0, width, tabHeight / 2);

		g2d.setPaint(
				new GradientPaint(0, tabHeight / 2, colorSet.bottomGradColor1,
						0, tabHeight + 1, colorSet.bottomGradColor2));
		g2d.fillRect(xpos, tabHeight / 2, width, tabHeight / 2 + 1);

		if (contentTopBorderDrawn) {
			g2d.setColor(lineColor);
			g2d.drawLine(rect.x, tabHeight, rect.x + rect.width - 1, tabHeight);
		}
	}

	@Override
	protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex,
			int x, int y, int w, int h, boolean isSelected)
	{
		Rectangle rect = rects[tabIndex];
		g.setColor(dividerColor);
		g.drawLine(rect.x + rect.width, 0, rect.x + rect.width, tabHeight);
	}

	@Override
	protected int getTabLabelShiftY(int tabPlacement, int tabIndex,
			boolean isSelected)
	{
		return 0;
	}

	private class ColorSet
	{
		Color topGradColor1;
		Color topGradColor2;

		Color bottomGradColor1;
		Color bottomGradColor2;
	}

	private class RollOverAdapter extends MouseAdapter
	{

		public void mouseEntered(MouseEvent e)
		{
			checkRollOver();
		}

		public void mouseExited(MouseEvent e)
		{
			tabPane.repaint();
		}

		private void checkRollOver()
		{
			int currentRollOver = getRolloverTab();
			if (currentRollOver != lastRollOverTab) {
				lastRollOverTab = currentRollOver;
				Rectangle tabsRect = new Rectangle(0, 0, tabPane.getWidth(),
						tabHeight);
				tabPane.repaint(tabsRect);
			}
		}
	}
}