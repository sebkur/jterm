package de.topobyte.jterm;

public class TestTerminal {
	public static void main(String[] args) {
		System.loadLibrary("terminal");

		Terminal terminal = new Terminal();
		terminal.test();
		
		terminal.write(null);
		terminal.write("jterm will rock");
	}
}
