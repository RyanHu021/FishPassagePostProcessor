package hu.ryan.fpprocessor.ui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

import hu.ryan.fpprocessor.Config;
import hu.ryan.fpprocessor.ProgramLogger;
import hu.ryan.fpprocessor.data.Condition;
import hu.ryan.fpprocessor.data.ConditionsManager;
import hu.ryan.fpprocessor.data.PropertyType;
import hu.ryan.fpprocessor.graphics.RenderItem;

public class ProcessorFrame extends JFrame {

	private static final long serialVersionUID = 3741432415089980546L;
	// public static final String DEFAULT_FC_DIRECTORY =
	// System.getProperty("user.home") + System.getProperty("file.separator") +
	// "Documents" + System.getProperty("file.separator");
	public static final String DEFAULT_FC_DIRECTORY = System.getProperty("user.home")
			+ "\\Documents\\IB Comp Sci\\HL IA Project\\";
	private InitPanel initPanel;
	private NewConditionPanel newPanel;
	private ViewConditionPanel viewPanel;
	private ConditionsManager cm;
	private Condition current;
	private JFileChooser fc;
	private DefaultTableModel conditionsTable;
	private RenderQueueFrame queueFrame;

	public ProcessorFrame() {
		setMinimumSize(new Dimension(750, 500));
		initComponents();
		cm = new ConditionsManager();
	}

	private void initComponents() {
		setTitle("Fish Passage Post-Processor");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);

		fc = new JFileChooser();

		initPanel = new InitPanel();
		initPanel.getBtnExport().addActionListener(new InitPanelBtnExportActionListener());
		initPanel.getBtnDelete().addActionListener(new InitPanelBtnDeleteActionListener());
		initPanel.getBtnNew().addActionListener(new InitPanelBtnNewActionListener());
		initPanel.getBtnView().addActionListener(new InitPanelBtnViewActionListener());
		initPanel.getBtnImport().addActionListener(new InitPanelBtnImportActionListener());
		conditionsTable = (DefaultTableModel) initPanel.getTable().getModel();
		// resetViewPanel(new Condition());
		setContentPane(initPanel);
	}

	public void resetNewPanel() {
		current = new Condition();
		newPanel = new NewConditionPanel();
		newPanel.getChckbc().addActionListener(new NewConditionPanelChckbcActionListener());
		newPanel.getBtnConfirm().addActionListener(new NewConditionPanelBtnConfirmActionListener());
		newPanel.getBtnCancel().addActionListener(new NewConditionPanelBtnCancelActionListener());
		newPanel.getBtnWorld().addActionListener(new NewConditionPanelBtnWorldActionListener());
		newPanel.getBtnImage().addActionListener(new NewConditionPanelBtnImageActionListener());
		newPanel.getBtnData().addActionListener(new NewConditionPanelBtnDataActionListener());
		newPanel.getBtnMesh().addActionListener(new NewConditionPanelBtnMeshActionListener());
	}

	public void resetViewPanel(Condition condition) {
		viewPanel = new ViewConditionPanel(condition);
		viewPanel.getBtnClose().addActionListener(new ViewPanelBtnCloseActionListener());
		viewPanel.getBtnWord().addActionListener(new ViewPanelBtnWordActionListener());
		viewPanel.getBtnPDF().addActionListener(new ViewPanelBtnPDFActionListener());
		viewPanel.getBtnExport().addActionListener(new ViewPanelBtnExportActionListener());
		viewPanel.getBtnAll().addActionListener(new ViewPanelBtnAllActionListener());
		viewPanel.getBtnSelTime().addActionListener(new ViewPanelBtnSelTimeActionListener());
		viewPanel.getBtnShow().addActionListener(new ViewConditionPanelBtnShowActionListener());
	}

	private boolean isInteger(String s) {
		try {
			Integer.parseInt(s);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	private class InitPanelBtnNewActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			resetNewPanel();
			setContentPane(newPanel);
			revalidate();
		}
	}

	private class InitPanelBtnImportActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			fc.setDialogTitle("Import Condition");
			fc.setCurrentDirectory(new File(DEFAULT_FC_DIRECTORY));
			fc.resetChoosableFileFilters();
			fc.setAcceptAllFileFilterUsed(false);
			FileNameExtensionFilter filter = new FileNameExtensionFilter("Condition File (.fpcondition)",
					"fpcondition");
			fc.addChoosableFileFilter(filter);

			if (fc.showOpenDialog(newPanel) == JFileChooser.APPROVE_OPTION) {
				try {
					FileInputStream file = new FileInputStream(fc.getSelectedFile());
					ObjectInputStream in = new ObjectInputStream(file);
					current = (Condition) in.readObject();
					boolean conditionAdded = cm.addCondition(current.getName(), current);
					if (!conditionAdded) {
						ProgramLogger.showErrorDialog(this.getClass(), initPanel,
								"Duplicate condition name: " + current.getName());
						in.close();
						file.close();
						return;
					}
					in.close();
					file.close();
					String withImage = current.hasImage() ? "Yes" : "No";
					conditionsTable.addRow(new Object[] { current.getName(), current.getElementsSize(),
							current.getNodesSize(), current.getNode(0).getTimestampsSize(), withImage });
					ProgramLogger.log(getClass(), ProgramLogger.INFO,
							"Imported condition " + current.getName() + " from " + fc.getSelectedFile().getAbsolutePath());
				} catch (IOException | ClassNotFoundException e1) {
					ProgramLogger.showErrorDialog(this.getClass(), initPanel,
							"Could not import condition " + fc.getSelectedFile().getAbsolutePath());
				}
			}
		}
	}

	private class InitPanelBtnViewActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			try {
				current = cm.getCondition(
						String.valueOf(initPanel.getTable().getValueAt(initPanel.getTable().getSelectedRow(), 0)));
			} catch (ArrayIndexOutOfBoundsException e1) {
				ProgramLogger.showInfoDialog(getClass(), initPanel, "No condition selected");
				return;
			}
			resetViewPanel(current);
			setContentPane(viewPanel);
			revalidate();
		}
	}

	private class InitPanelBtnDeleteActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			try {
				current = cm.getCondition(
						String.valueOf(initPanel.getTable().getValueAt(initPanel.getTable().getSelectedRow(), 0)));
			} catch (ArrayIndexOutOfBoundsException e1) {
				ProgramLogger.showInfoDialog(getClass(), initPanel, "No condition selected");
				return;
			}
			cm.removeCondition(current.getName());
			conditionsTable.removeRow(initPanel.getTable().getSelectedRow());
		}
	}

	private class InitPanelBtnExportActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (conditionsTable.getRowCount() < 1) {
				ProgramLogger.showInfoDialog(getClass(), initPanel, "No conditions");
				return;
			}
			ExportOptionsPanel panel = new ExportOptionsPanel();
			int result = JOptionPane.showConfirmDialog(initPanel, panel, "Export Options",
					JOptionPane.OK_CANCEL_OPTION);
			if (result == JOptionPane.OK_OPTION) {
				if (queueFrame == null || !queueFrame.isVisible()) {
					queueFrame = new RenderQueueFrame();
				}
				if (isInteger(panel.getTxtFieldTime().getText())) {
					int time = Integer.parseInt(panel.getTxtFieldTime().getText());
					if (time >= 0 && time < cm.getCondition((String) conditionsTable.getValueAt(0, 0)).getNode(0)
							.getTimestampsSize()) {
						for (int i = 0; i < conditionsTable.getRowCount(); i++) {
							current = cm.getCondition((String) conditionsTable.getValueAt(i, 0));
							for (PropertyType type : PropertyType.values()) {
								String title = current.getName() + ", " + panel.getTxtFieldFlow().getText() + ", "
										+ type.getName() + " (" + type.getUnit() + ")";
								RenderItem item = new RenderItem(current, time, type, title);
								queueFrame.add(item);
							}

						}
						queueFrame.setVisible(true);
					} else {
						ProgramLogger.showWarnDialog(getClass(), panel, "Invalid parameters");
					}
				} else {
					ProgramLogger.showWarnDialog(getClass(), panel, "Invalid parameters");
				}
			}
		}
	}

	private class NewConditionPanelBtnMeshActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			fc.setDialogTitle("Open Mesh File");
			fc.setCurrentDirectory(new File(DEFAULT_FC_DIRECTORY));
			fc.resetChoosableFileFilters();
			fc.setAcceptAllFileFilterUsed(false);
			FileNameExtensionFilter filter = new FileNameExtensionFilter(
					"Hierarchical Data Format Version 5 (.hdf5, .h5)", "hdf5", "h5");
			fc.addChoosableFileFilter(filter);

			if (fc.showOpenDialog(newPanel) == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				String nodePath, elementPath;
				if (newPanel.getRdbtnEC().isSelected()) {
					nodePath = Config.getHdfECNodePath();
					elementPath = Config.getHdfECElementPath();
				} else if (newPanel.getRdbtnNC().isSelected()) {
					nodePath = Config.getHdfNCNodePath();
					elementPath = Config.getHdfNCElementPath();
				} else if (newPanel.getRdbtnPC().isSelected()) {
					nodePath = Config.getHdfPCNodePath();
					elementPath = Config.getHdfPCElementPath();
				} else {
					String[] paths = newPanel.getTxtFieldOther().getText().split(",");
					try {
						nodePath = paths[0];
						elementPath = paths[1];
					} catch (ArrayIndexOutOfBoundsException e1) {
						ProgramLogger.showErrorDialog(this.getClass(), newPanel, "Invalid Mesh Paths");
						return;
					}
				}
				boolean meshRead = cm.readMeshFile(file, nodePath, elementPath, current);
				if (!meshRead) {
					ProgramLogger.showErrorDialog(cm.getClass(), newPanel,
							"Could not read mesh file " + file.getAbsolutePath());
					return;
				}
				newPanel.getLblSelMesh().setText("<html>Selected Mesh File: " + file.getAbsolutePath() + "</html>");
				newPanel.getLblNodes().setText("Nodes: " + current.getNodesSize());
				newPanel.getLblElements().setText("Elements: " + current.getElementsSize());
				newPanel.getBtnMesh().setEnabled(false);
				newPanel.getBtnData().setEnabled(true);
			}
		}
	}

	private class NewConditionPanelBtnDataActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			fc.setDialogTitle("Open Data File");
			fc.setCurrentDirectory(new File(DEFAULT_FC_DIRECTORY));
			fc.resetChoosableFileFilters();
			fc.setAcceptAllFileFilterUsed(false);
			FileNameExtensionFilter filter = new FileNameExtensionFilter(
					"Hierarchical Data Format Version 5 (.hdf5, .h5)", "hdf5", "h5");
			fc.addChoosableFileFilter(filter);

			if (fc.showOpenDialog(newPanel) == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				boolean dataRead = cm.readDataFile(file, Config.getHdfDepthPath(), Config.getHdfWSELPath(),
						Config.getHdfShearStressPath(), Config.getHdfVelocityPath(), current);
				if (!dataRead) {
					ProgramLogger.showErrorDialog(cm.getClass(), newPanel,
							"Could not read data file " + file.getAbsolutePath());
					return;
				}
				newPanel.getLblSelData().setText("<html>Selected Data File: " + file.getAbsolutePath() + "</html>");
				newPanel.getLblTimestamps().setText("Timestamps: " + current.getNode(0).getTimestampsSize());
				newPanel.getBtnData().setEnabled(false);
				newPanel.getChckbc().setEnabled(true);
				newPanel.getBtnConfirm().setEnabled(true);
			}
		}
	}

	private class NewConditionPanelBtnImageActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			fc.setDialogTitle("Open Map Image");
			fc.setCurrentDirectory(new File(DEFAULT_FC_DIRECTORY));
			fc.resetChoosableFileFilters();
			fc.setAcceptAllFileFilterUsed(false);
			FileNameExtensionFilter filter = new FileNameExtensionFilter("Image (.jpg, .jpeg, .png, .tif, .tiff, .gif)",
					"jpg", "jpeg", "png", "tif", "tiff", "gif");
			fc.addChoosableFileFilter(filter);

			if (fc.showOpenDialog(newPanel) == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				try {
					ImageIO.read(file);
				} catch (IOException e1) {
					ProgramLogger.showErrorDialog(cm.getClass(), newPanel, "Invalid image " + file.getAbsolutePath());
					return;
				}
				current.setMapImage(file);
				newPanel.getLblSelImage().setText("<html>Selected Map Image: " + file.getAbsolutePath() + "</html>");
				newPanel.getLblImage().setText("With Image: Yes");
				newPanel.getBtnImage().setEnabled(false);
				newPanel.getChckbc().setEnabled(false);
				newPanel.getBtnWorld().setEnabled(true);
			}
		}
	}

	private class NewConditionPanelBtnWorldActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			fc.setDialogTitle("Open Map Image");
			fc.setCurrentDirectory(new File(DEFAULT_FC_DIRECTORY));
			fc.resetChoosableFileFilters();
			fc.setAcceptAllFileFilterUsed(false);
			FileNameExtensionFilter filter = new FileNameExtensionFilter("World File (.jgw, .pgw, .tfw, .gfw)", "jgw",
					"pgw", "tfw", "gfw");
			fc.addChoosableFileFilter(filter);

			if (fc.showOpenDialog(newPanel) == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				try {
					cm.readGeorefFile(file, current);
				} catch (IOException | NumberFormatException e1) {
					ProgramLogger.showErrorDialog(cm.getClass(), newPanel, "Invalid world file " + file.getAbsolutePath());
					return;
				}
				newPanel.getLblSelWorld().setText("<html>Selected World File: " + file.getAbsolutePath() + "</html>");
				newPanel.getBtnWorld().setEnabled(false);
				newPanel.getBtnConfirm().setEnabled(true);
			}
		}
	}

	private class NewConditionPanelChckbcActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			JCheckBox cb = (JCheckBox) e.getSource();
			if (cb.isSelected()) {
				newPanel.getLblImage().setText("With Image: Yes");
				newPanel.getBtnImage().setEnabled(true);
				newPanel.getBtnConfirm().setEnabled(false);
			} else {
				newPanel.getLblImage().setText("With Image: No");
				newPanel.getBtnImage().setEnabled(false);
				newPanel.getBtnConfirm().setEnabled(true);
			}
		}
	}

	private class NewConditionPanelBtnCancelActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			current = null;
			setContentPane(initPanel);
			revalidate();
		}
	}

	private class NewConditionPanelBtnConfirmActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String name = newPanel.getTxtFieldName().getText();
			current.setName(name);
			boolean conditionAdded = cm.addCondition(name, current);
			if (!conditionAdded) {
				ProgramLogger.showErrorDialog(this.getClass(), newPanel,
						"Duplicate condition name: " + current.getName());
				return;
			}
			String withImage = current.hasImage() ? "Yes" : "No";
			conditionsTable.addRow(new Object[] { name, current.getElementsSize(), current.getNodesSize(),
					current.getNode(0).getTimestampsSize(), withImage });
			setContentPane(initPanel);
			revalidate();
		}
	}

	private class ViewConditionPanelBtnShowActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (isInteger(viewPanel.getTxtFieldNode().getText()) && isInteger(viewPanel.getTxtFieldTime().getText())) {
				int node = Integer.parseInt(viewPanel.getTxtFieldNode().getText());
				int time = Integer.parseInt(viewPanel.getTxtFieldTime().getText());
				if (node >= 0 && node < current.getNodesSize() && time >= 0
						&& time < current.getNode(0).getTimestampsSize()) {
					viewPanel.getLblSelNode().setText("<html>Selected " + current.getNode(node) + "</html>");
					viewPanel.getLblSelTime().setText("Selected Timestamp: " + time);
					String depth = current.getNode(node).getTimestamp(time).getDepth() == -999 ? "N/A"
							: String.valueOf(current.getNode(node).getTimestamp(time).getDepth()) + " "
									+ PropertyType.DEPTH.getUnit();
					String wSEL = current.getNode(node).getTimestamp(time).getWSEL() == -999 ? "N/A"
							: String.valueOf(current.getNode(node).getTimestamp(time).getWSEL() + " "
									+ PropertyType.WSEL.getUnit());
					String shearStress = current.getNode(node).getTimestamp(time).getShearStress() == -999 ? "N/A"
							: String.valueOf(current.getNode(node).getTimestamp(time).getShearStress()) + " "
									+ PropertyType.SHEAR_STRESS.getUnit();
					String velocity = current.getNode(node).getTimestamp(time).getVelocity().getX() == -999 ? "N/A"
							: String.valueOf(current.getNode(node).getTimestamp(time).getVelocity().getMagnitude())
									+ " " + PropertyType.VELOCITY.getUnit();
					viewPanel.getLblDepth().setText("Depth: " + depth);
					viewPanel.getLblWSEL().setText("WSEL: " + wSEL);
					viewPanel.getLblShearStress().setText("Shear Stress: " + shearStress);
					viewPanel.getLblVel().setText("Velocity: " + velocity);
				} else {
					ProgramLogger.showWarnDialog(getClass(), viewPanel, "Invalid Parameters");
				}
			} else {
				ProgramLogger.showWarnDialog(getClass(), viewPanel, "Invalid Parameters");
			}
		}
	}

	private class ViewPanelBtnSelTimeActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (queueFrame == null || !queueFrame.isVisible()) {
				queueFrame = new RenderQueueFrame();
			}

			List<PropertyType> toRender = new ArrayList<PropertyType>();
			if (viewPanel.getCbDepth().isSelected()) {
				toRender.add(PropertyType.DEPTH);
			}
			if (viewPanel.getCbWSEL().isSelected()) {
				toRender.add(PropertyType.WSEL);
			}
			if (viewPanel.getCbSStress().isSelected()) {
				toRender.add(PropertyType.SHEAR_STRESS);
			}
			if (viewPanel.getCbVel().isSelected()) {
				toRender.add(PropertyType.VELOCITY);
			}

			if (isInteger(viewPanel.getTxtFieldTime().getText())) {
				int time = Integer.parseInt(viewPanel.getTxtFieldTime().getText());
				if (time >= 0 && time < current.getNode(0)
						.getTimestampsSize()) {
					for (PropertyType type : toRender) {
						RenderItem item = new RenderItem(current, time, type);
						queueFrame.add(item);
					}
					queueFrame.setVisible(true);
				} else {
					ProgramLogger.showWarnDialog(getClass(), viewPanel, "Invalid parameters");
				}
			} else {
				ProgramLogger.showWarnDialog(getClass(), viewPanel, "Invalid parameters");
			}
		}
	}

	private class ViewPanelBtnAllActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (queueFrame == null || !queueFrame.isVisible()) {
				queueFrame = new RenderQueueFrame();
			}
			
			for (int i = 0; i < current.getNode(0).getTimestampsSize(); i++) {
				for (PropertyType type : PropertyType.values()) {
					RenderItem item = new RenderItem(current, i, type);
					queueFrame.add(item);
				}
				queueFrame.setVisible(true);
			}
		}
	}

	private class ViewPanelBtnExportActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			fc.setDialogTitle("Export Condition");
			fc.setSelectedFile(new File(DEFAULT_FC_DIRECTORY + current.getName() + ".fpcondition"));
			fc.resetChoosableFileFilters();
			fc.setAcceptAllFileFilterUsed(false);
			FileNameExtensionFilter filter = new FileNameExtensionFilter("Condition File (.fpcondition)",
					"fpcondition");
			fc.addChoosableFileFilter(filter);

			if (fc.showSaveDialog(newPanel) == JFileChooser.APPROVE_OPTION) {
				try {
					FileOutputStream file = new FileOutputStream(fc.getSelectedFile());
					ObjectOutputStream out = new ObjectOutputStream(file);
					out.writeObject(current);
					out.close();
					file.close();
					ProgramLogger.log(getClass(), ProgramLogger.INFO,
							"Exported condition " + current.getName() + " to " + fc.getSelectedFile().getAbsolutePath());
				} catch (IOException e1) {
					ProgramLogger.showErrorDialog(this.getClass(), viewPanel,
							"Could not export condition " + current.getName());
				}
			}
		}
	}

	private class ViewPanelBtnPDFActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
		}
	}

	private class ViewPanelBtnWordActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
		}
	}

	private class ViewPanelBtnCloseActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			current = null;
			setContentPane(initPanel);
			revalidate();
		}
	}
}
