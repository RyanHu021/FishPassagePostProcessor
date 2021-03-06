package hu.ryan.fpprocessor;

import java.awt.Component;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import org.slf4j.LoggerFactory;

public class ProgramLogger {

	public static final int ERROR = 0;
	public static final int WARN = 1;
	public static final int INFO = 2;

	public static void showInfoDialog(Class<?> c, Component parent, String message) {
		JOptionPane.showMessageDialog(parent, message, "Info", JOptionPane.INFORMATION_MESSAGE);
		LoggerFactory.getLogger(c).info(message);
	}

	public static void showWarnDialog(Class<?> c, Component parent, String message) {
		JOptionPane.showMessageDialog(parent, message, "Warning", JOptionPane.WARNING_MESSAGE);
		LoggerFactory.getLogger(c).warn(message);
	}

	public static void showErrorDialog(Class<?> c, Component parent, String message) {
		JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
		LoggerFactory.getLogger(c).error(message);
	}

	public static void log(Class<?> c, int level, String message) {
		switch (level) {
		case ERROR:
			LoggerFactory.getLogger(c).error(message);
			break;
		case WARN:
			LoggerFactory.getLogger(c).warn(message);
			break;
		case INFO:
			LoggerFactory.getLogger(c).info(message);
		}
	}
}
