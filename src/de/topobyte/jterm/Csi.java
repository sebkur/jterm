package de.topobyte.jterm;

import java.util.ArrayList;
import java.util.List;

public class Csi
{
	public char prefix;
	public char suffix1;
	public char suffix2;
	public boolean firstDigit = true;
	public List<Integer> nums = new ArrayList<Integer>();;
}
