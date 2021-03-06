package hu.ryan.fpprocessor;

import java.awt.EventQueue;

import javax.swing.UIManager;

import hu.ryan.fpprocessor.ui.ProcessorFrame;

public class Main {

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Config.init();
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					ProcessorFrame frame = new ProcessorFrame();
					frame.setVisible(true); 
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}
