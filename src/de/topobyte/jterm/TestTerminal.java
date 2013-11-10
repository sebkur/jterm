package de.topobyte.jterm;

public class TestTerminal {
	public static void main(String[] args) {
		System.loadLibrary("terminal");

		Terminal terminal = new Terminal();
		terminal.test();

		terminal.write(null);
		terminal.write("jterm will rock");

		System.out.println("Got: " + terminal.testStringCreation());
		System.out.println("Got: " + terminal.testStringCreation());
		System.out.println("Got: " + terminal.testStringCreation());

		terminal.start();

		terminal.printInfo();
		
		byte[] bytes = terminal.read();
		for (int i = 0; i < bytes.length; i++) {
			byte b = bytes[i];
			char c = (char) b;
			System.out.println("Byte: " + b + "..." + c);
		}
	}
}
