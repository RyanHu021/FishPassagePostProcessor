package fpprocessor;

import java.awt.EventQueue;

import javax.swing.UIManager;

import fpprocessor.ui.ProcessorFrame;

public class Main {

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Config.init();
					ProgramLogger.log(getClass(), ProgramLogger.INFO, "Initalized configuration file");
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					ProcessorFrame frame = new ProcessorFrame();
					ProgramLogger.log(getClass(), ProgramLogger.INFO, "Loaded GUI");
					frame.setVisible(true); 
				} catch (Exception e) {
					ProgramLogger.showErrorDialog(getClass(), null, "Unable to start Fish Passage Post-Processor: " + e.getClass());
					e.printStackTrace();
				}
			}
		});
	}
}
