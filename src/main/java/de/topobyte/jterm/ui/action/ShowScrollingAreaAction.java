package de.topobyte.jterm.ui.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import de.topobyte.jterm.JTerm;

public class ShowScrollingAreaAction extends AbstractAction
{

	private static final long serialVersionUID = 1L;

	private JTerm jterm;

	public ShowScrollingAreaAction(JTerm jterm)
	{
		this.jterm = jterm;
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		boolean showScrollingArea = jterm.isShowScrollingArea();
		jterm.setShowScrollingArea(!showScrollingArea);
		firePropertyChange(Action.SELECTED_KEY, null, null);
	}

	@Override
	public Object getValue(String key)
	{
		if (key.equals(Action.SELECTED_KEY)) {
			return new Boolean(jterm.isShowScrollingArea());
		}
		return null;
	}

}
