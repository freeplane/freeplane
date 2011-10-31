package org.freeplane.plugin.script.addons;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.AbstractCellEditor;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.resources.components.OptionPanelBuilder;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.FileUtils;
import org.freeplane.core.util.FreeplaneIconUtils;
import org.freeplane.core.util.HtmlUtils;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.features.url.mindmapmode.MFileManager;
import org.freeplane.main.addons.AddOnProperties;
import org.freeplane.main.addons.AddOnsController;
import org.freeplane.plugin.script.ScriptingEngine;
import org.freeplane.plugin.script.ScriptingPermissions;
import org.freeplane.plugin.script.addons.ManageAddOnsDialog.AddOnTableModel;

@SuppressWarnings("serial")
public class ManageAddOnsDialog extends JDialog {
	public final static class AddOnTableModel extends AbstractTableModel {
		private final List<AddOnProperties> addOns;

		private AddOnTableModel(List<AddOnProperties> addOns) {
			this.addOns = new ArrayList<AddOnProperties>(addOns);
		}

		public int getRowCount() {
			return addOns.size();
		}

		public int getColumnCount() {
			return 2;
		}

		public Object getValueAt(int row, int col) {
			AddOnProperties addOn = addOns.get(row);
			switch (col) {
				case 0:
					return "<html><body><b><font size='+1'>" + addOn.getTranslatedName() + " "
					        + addOn.getVersion().replaceAll("^v", "") + "</font></b><br>"
					        + getDescription(addOn) + "</body></html>";
				case 1:
					return "";
				default:
					throw new RuntimeException("unexpected column " + col);
			}
		}

		private String getDescription(AddOnProperties addOn) {
	        return HtmlUtils.toXMLEscapedText(shorten(HtmlUtils.htmlToPlain(addOn.getDescription()), 120));
        }

		private String shorten(String string, int maxLength) {
			if (string.length() <= 3 || string.length() <= maxLength)
				return string;
			return string.substring(0, maxLength - 3) + "...";
		}

		public boolean isCellEditable(int row, int column) {
			return column == 1;
		}

		public void setValueAt(Object aValue, int row, int column) {
			fireTableCellUpdated(row, column);
		}

		public AddOnProperties getAddOnAt(int row) {
			return addOns.get(row);
		}
		
		public void addAddOn(final AddOnProperties addOn) {
			addOns.add(addOn);
		}
	}

	private static final long serialVersionUID = 1L;
	private static final int BUTTON_GAP = 15;

	public ManageAddOnsDialog(final List<AddOnProperties> addOns) {
		super(UITools.getFrame(), TextUtils.getText("ManageAddOnsAction.text"), true);
		// stolen from NewerFileRevisionsFoundDialog - no idea if actually needed
		if (getOwner() != null) {
			final Window[] ownedWindows = getOwner().getOwnedWindows();
			for (Window w : ownedWindows) {
				if (w.isVisible()) {
					w.toBack();
				}
			}
		}
		createPane(addOns);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setBackground(Color.white);
		UITools.addEscapeActionToDialog(this);
	}

	private void createPane(final List<AddOnProperties> addOns) {
		getContentPane().setLayout(new BorderLayout());
		final AddOnTableModel tableModel = createTableModel(addOns);
		final JTable jTable = createTable(tableModel);
		JScrollPane scrollPane = new JScrollPane(jTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
		    JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		add(scrollPane, BorderLayout.CENTER);
		add(createInstaller(tableModel), BorderLayout.SOUTH);
		UITools.focusOn(jTable);
		setPreferredSize(getPreferredSizeForWindow());
		pack();
	}

	private Dimension getPreferredSizeForWindow() {
		final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		return new Dimension((int) screenSize.getWidth() * 3 / 4, (int) screenSize.getHeight() * 2 / 3);
	}

	private Box createInstaller(final AddOnTableModel tableModel) {
		final Box installer = Box.createHorizontalBox();
		final JButton install = new JButton();
		MenuBuilder.setLabelAndMnemonic(install, getText("install"));
		install.setEnabled(false);
		install.setMaximumSize(UITools.MAX_BUTTON_DIMENSION);
		final JTextField urlField = new JTextField();
		urlField.setToolTipText(getText("install.tooltip"));
		urlField.getDocument().addDocumentListener(new DocumentListener() {
			public void insertUpdate(DocumentEvent e) {
				updateImpl(e);
			}

			public void removeUpdate(DocumentEvent e) {
				updateImpl(e);
			}

			public void changedUpdate(DocumentEvent e) {
				updateImpl(e);
			}

			private void updateImpl(DocumentEvent e) {
				install.setEnabled(e.getDocument().getLength() > 0);
			}
		});
		urlField.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				int key = e.getKeyCode();
				if (key == KeyEvent.VK_ENTER) {
					install.requestFocusInWindow();
					install.doClick();
				}
			}
		});
		final JFileChooser fileChooser = new JFileChooser();
		final JButton selectFile = new JButton(FreeplaneIconUtils.createImageIconByResourceKey("OpenAction.icon"));
		selectFile.setToolTipText(getText("select.tooltip"));
		selectFile.setMaximumSize(UITools.MAX_BUTTON_DIMENSION);
		selectFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fileChooser.showOpenDialog(getContentPane());
				final File selectedFile = fileChooser.getSelectedFile();
				if (selectedFile != null)
					urlField.setText(selectedFile.getAbsolutePath());
			}
		});
		install.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				final Controller controller = Controller.getCurrentController();
				try {
					LogUtils.info("installing add-on from " + urlField.getText());
					controller.getViewController().setWaitingCursor(true);
					final URL sourceUrl = toURL(urlField.getText());
					setStatusInfo(getText("status.installing"));
					final ModeController modeController = controller.getModeController(MModeController.MODENAME);
					final MFileManager fileManager = (MFileManager) MFileManager.getController(modeController);
					MapModel newMap = modeController.getMapController().newMap(null);
					fileManager.loadImpl(sourceUrl, newMap);
					AddOnProperties addOn = (AddOnProperties) ScriptingEngine.executeScript(newMap.getRootNode(),
					    getInstallScriptSource(), ScriptingPermissions.getPermissiveScriptingPermissions());
					if (addOn != null) {
						addOn.setSourceUrl(sourceUrl);
						setStatusInfo(getText("status.success", addOn.getName()));
						AddOnsController.getController().registerInstalledAddOn(addOn);
						tableModel.addOns.add(addOn);
						urlField.setText("");
						getContentPane().repaint();
					}
				}
				catch (Exception ex) {
					UITools.errorMessage(getText("error", ex.toString()));
				}
				finally {
					controller.getViewController().setWaitingCursor(false);
				}
			}

			private String getInstallScriptSource() throws IOException {
				final ResourceController resourceController = ResourceController.getResourceController();
				final File scriptDir = new File(resourceController.getInstallationBaseDir(), "scripts");
				final File installScript = new File(scriptDir, "installScriptAddOn.groovy");
				if (!installScript.exists())
					throw new RuntimeException("internal error: installer not found");
				return FileUtils.slurpFile(installScript);
			}

			private URL toURL(String urlText) throws MalformedURLException {
				try {
					return new URL(urlText);
				}
				catch (Exception e2) {
					return new File(urlText).toURI().toURL();
				}
			}
		});
		installer.add(Box.createVerticalStrut(BUTTON_GAP));
		urlField.setColumns(60);
		installer.add(urlField);
		installer.add(Box.createVerticalStrut(BUTTON_GAP));
		installer.add(selectFile);
		installer.add(Box.createVerticalStrut(BUTTON_GAP));
		installer.add(install);
		installer.add(Box.createVerticalStrut(BUTTON_GAP));
		return installer;
	}

	private AddOnTableModel createTableModel(final List<AddOnProperties> addOns) {
		return new AddOnTableModel(addOns);
	}

	private JTable createTable(final AddOnTableModel tableModel) {
		final JTable table = new JTable(tableModel);
		table.setTableHeader(null);
//FIXME: Java 6
//		table.setAutoCreateRowSorter(true);
		table.setRowHeight(62);
		table.setBackground(Color.white);
		final TableColumnModel columnModel = table.getColumnModel();
		columnModel.getColumn(0).setPreferredWidth(750);
		columnModel.getColumn(1).setMinWidth(250);
		JButton[] btns = new JButton[] { createButton(AddOnProperties.OP_CONFIGURE) //
		        , createButton(AddOnProperties.OP_DEACTIVATE) //
		        , createButton(AddOnProperties.OP_ACTIVATE) //
		        , createButton(AddOnProperties.OP_DEINSTALL) //
		};
		Action[] actions = new Action[] { createConfigureAction(tableModel) //
		        , createDeactivateAction(tableModel) //
		        , createActivateAction(tableModel) //
		        , createDeinstallAction(tableModel) //
		};
		new ButtonsInCellRenderer(table, btns, actions, 1);
		table.setFocusable(false);
		return table;
	}

	private AbstractAction createConfigureAction(final AddOnTableModel tableModel) {
		return new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				final int row = Integer.parseInt(e.getActionCommand());
				final AddOnProperties addOn = tableModel.getAddOnAt(row);
				if (!addOn.supportsOperation(AddOnProperties.OP_CONFIGURE)) {
					JOptionPane.showMessageDialog(ManageAddOnsDialog.this, getText("cannot.configure", addOn.getTranslatedName()), "Freeplane", JOptionPane.ERROR_MESSAGE);
				}
				else {
					OptionPanelBuilder optionPanelBuilder = new OptionPanelBuilder();
					optionPanelBuilder.load(new StringReader(addOn.getPreferencesXml()));
					MModeController.createPropertyAction(optionPanelBuilder).actionPerformed(e);
				}
			}
		};
	}

	private AbstractAction createDeactivateAction(final AddOnTableModel tableModel) {
		return new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				final int row = Integer.parseInt(e.getActionCommand());
				final AddOnProperties addOn = tableModel.getAddOnAt(row);
				if (!addOn.supportsOperation(AddOnProperties.OP_DEACTIVATE)) {
					JOptionPane.showMessageDialog(ManageAddOnsDialog.this, getText("cannot.deactivate", addOn.getTranslatedName()), "Freeplane", JOptionPane.ERROR_MESSAGE);
				}
				else {
					addOn.setActive(false);
					saveAddOn(addOn);
					JOptionPane.showMessageDialog(ManageAddOnsDialog.this, getText("deactivation.success", addOn.getTranslatedName()), "Freeplane", JOptionPane.INFORMATION_MESSAGE);
				}
			}
		};
	}

	private AbstractAction createActivateAction(final AddOnTableModel tableModel) {
		return new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				final int row = Integer.parseInt(e.getActionCommand());
				final AddOnProperties addOn = tableModel.getAddOnAt(row);
				if (!addOn.supportsOperation(AddOnProperties.OP_ACTIVATE)) {
					JOptionPane.showMessageDialog(ManageAddOnsDialog.this, getText("cannot.activate", addOn.getTranslatedName()), "Freeplane", JOptionPane.ERROR_MESSAGE);
				}
				else {
					addOn.setActive(true);
					saveAddOn(addOn);
					JOptionPane.showMessageDialog(ManageAddOnsDialog.this, getText("activation.success", addOn.getTranslatedName()), "Freeplane", JOptionPane.INFORMATION_MESSAGE);
				}
			}
		};
	}

	private AbstractAction createDeinstallAction(final AddOnTableModel tableModel) {
		return new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				final int row = Integer.parseInt(e.getActionCommand());
				final AddOnProperties addOn = tableModel.getAddOnAt(row);
				if (!addOn.supportsOperation(AddOnProperties.OP_DEINSTALL)) {
					UITools.errorMessage(getText("cannot.deinstall", addOn.getTranslatedName()));
				}
				else {
					int result = JOptionPane.showConfirmDialog(ManageAddOnsDialog.this,
					    getText("really.deinstall", TextUtils.getText(addOn.getNameKey())), getText("deinstall"),
					    JOptionPane.OK_CANCEL_OPTION);
					if (result == JOptionPane.OK_OPTION) {
						AddOnsController.getController().deInstall(addOn);
						tableModel.addOns.remove(addOn);
						getContentPane().repaint();
						UITools.informationMessage(getText("deinstallation.success", addOn.getTranslatedName()));
					}
				}
			}
		};
	}

	private JButton createButton(final String name) {
		final JButton button = new JButton(getText(name));
		button.setName(name);
		return button;
	}

	private void setStatusInfo(final String message) {
		Controller.getCurrentController().getViewController().out(message);
	}

	private void saveAddOn(final AddOnProperties addOn) {
	    try {
	        AddOnsController.getController().save(addOn);
        }
        catch (IOException e) {
			UITools.errorMessage("Cannot save add-on settings: " + e.getMessage());
        }
    }

	private static String getResourceKey(final String key) {
		return "ManageAddOnsDialog." + key;
	}

	private static String getText(String key) {
		return TextUtils.getText(getResourceKey(key));
	}

	private String getText(String key, Object... parameters) {
		return TextUtils.format(getResourceKey(key), parameters);
    }
}

/**
 * Editor and Renderer for multiple buttons inside a table cell.
 * @author Mag. Stefan Hagmann 
 * @see http://www.bgbaden-frauen.ac.at/frauengasse20/uploads/files/Informatik/java/ButtonsInColumn.java
 */
@SuppressWarnings("serial")
class ButtonsInCellRenderer extends AbstractCellEditor implements TableCellRenderer, TableCellEditor, ActionListener,
        MouseListener {
	private static final int BUTTON_SPACER = 4;
	private final JTable table;
	private final Border border;
	private Border fborder;
	private Object editorValue;
	private boolean isButtonColumnEditor;
	private JPanel panel;
	private final Action[] actions;
	private final JButton[] buttons;

	public ButtonsInCellRenderer(JTable table, JButton[] buttons, Action[] actions, int column) {
		this.table = table;
		this.actions = actions;
		this.buttons = buttons;
		for (JButton btn : buttons) {
			btn.setFocusPainted(false);
			btn.addActionListener(this);
		}
		border = buttons[0].getBorder();
		setFocusBorder(new LineBorder(Color.BLUE));
		TableColumnModel columnModel = table.getColumnModel();
		columnModel.getColumn(column).setCellRenderer(this);
		columnModel.getColumn(column).setCellEditor(this);
		table.addMouseListener(this);
//		panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.add(Box.createHorizontalGlue());
		if (buttons.length > 0)
			panel.add(buttons[0]);
		for (int i = 1; i < buttons.length; i++) {
			panel.add(Box.createHorizontalStrut(BUTTON_SPACER));
			panel.add(buttons[i]);
		}
		panel.add(Box.createHorizontalStrut(BUTTON_SPACER));
	}

	private void setFocusBorder(Border focusBorder) {
		this.fborder = focusBorder;
		for (JButton btn : buttons) {
			btn.setBorder(focusBorder);
		}
	}

	public Object getCellEditorValue() {
		return editorValue;
	}

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
	                                               int row, int column) {
		final ManageAddOnsDialog.AddOnTableModel model = (AddOnTableModel) table.getModel();
		for (JButton btn : buttons) {
//FIXME: Java 6
//			final AddOnProperties addOn = model.getAddOnAt(table.convertRowIndexToModel(row));
			final AddOnProperties addOn = model.getAddOnAt(row);
			if (isSelected) {
				btn.setForeground(table.getSelectionForeground());
				btn.setBackground(table.getSelectionBackground());
				panel.setBackground(table.getSelectionBackground());
			}
			else {
				btn.setForeground(table.getForeground());
				btn.setBackground(UIManager.getColor("Button.background"));
				panel.setBackground(table.getBackground());
			}
			if (hasFocus) {
				btn.setBorder(fborder);
			}
			else {
				btn.setBorder(border);
			}
			btn.setEnabled(addOn.supportsOperation(btn.getName()));
			btn.setVisible(addOn.supportsOperation(btn.getName()));
		}
		return panel;
	}

	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		this.editorValue = value;
		return panel;
	}

	public void actionPerformed(ActionEvent e) {
//FIXME: Java 6
//		int row = table.convertRowIndexToModel(table.getEditingRow());
		int row = table.getEditingRow();
		fireEditingStopped();
		ActionEvent event = new ActionEvent(table, ActionEvent.ACTION_PERFORMED, "" + row);
		for (int i = 0; i < buttons.length; i++) {
			if (e.getSource().equals(buttons[i])) {
				actions[i].actionPerformed(event);
			}
		}
	}

	/*
	 *  When the mouse is pressed the editor is invoked. If you then then drag
	 *  the mouse to another cell before releasing it, the editor is still
	 *  active. Make sure editing is stopped when the mouse is released.
	 */
	public void mousePressed(MouseEvent e) {
		if (table.isEditing() && table.getCellEditor() == this)
			isButtonColumnEditor = true;
	}

	public void mouseReleased(MouseEvent e) {
		if (isButtonColumnEditor && table.isEditing())
			table.getCellEditor().stopCellEditing();
		isButtonColumnEditor = false;
	}

	public void mouseClicked(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}
}
