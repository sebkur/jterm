package de.topobyte.jterm;

import java.util.ArrayList;
import java.util.List;

public class Screen {

	private int width;
	private int height;

	// Current position
	private int ccol = 1;
	private int crow = 1;

	private List<Row> rows = new ArrayList<Row>();

	public Screen(int width, int height) {
		this.width = width;
		this.height = height;
	}
	
	public List<Row> getRows()
	{
		return rows;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getCurrentColumn() {
		return ccol;
	}

	public int getCurrentRow() {
		return crow;
	}

	public void setCurrentColumn(int ccol) {
		this.ccol = ccol;
	}

	public void setCurrentRow(int crow) {
		this.crow = crow;
	}

}
