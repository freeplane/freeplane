package org.freeplane.plugin.script.addons;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.AbstractCellEditor;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
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
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.features.ui.IMapViewManager;
import org.freeplane.main.addons.AddOnProperties;
import org.freeplane.main.addons.AddOnsController;
import org.freeplane.plugin.script.ScriptingEngine;
import org.freeplane.plugin.script.addons.ManageAddOnsDialog.AddOnTableModel;

@SuppressWarnings("serial")
public class ManageAddOnsDialog extends JDialog {
	public final static class AddOnTableModel extends AbstractTableModel {
		private final String[] columnNames;
		private final List<AddOnProperties> addOns;

		private AddOnTableModel(String[] columnNames, List<AddOnProperties> addOns) {
			this.columnNames = columnNames;
			this.addOns = addOns;
		}

		public String getColumnName(int col) {
			return columnNames[col];
		}

		public int getRowCount() {
			return addOns.size();
		}

		public int getColumnCount() {
			return columnNames.length;
		}

		public Object getValueAt(int row, int col) {
			AddOnProperties addOn = addOns.get(row);
			switch (col) {
				case 0:
					return addOn.getTranslatedName();
				case 1:
					return addOn.getVersion();
				case 2:
					return addOn.getDescription();
				case 3:
					return getText(addOn.getAddOnType().name());
				case 4:
					return "";
				default:
					throw new RuntimeException("unexpected column " + col);
			}
		}

		public boolean isCellEditable(int row, int column) {
			return true;
		}

		public void setValueAt(Object aValue, int row, int column) {
			fireTableCellUpdated(row, column);
		}

		public AddOnProperties getAddOnAt(int row) {
			return addOns.get(row);
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
		final JTable jTable = createTable(createTableModel(addOns));
		JScrollPane scrollPane = new JScrollPane(jTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
		    JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		add(scrollPane, BorderLayout.CENTER);
		add(createInstaller(), BorderLayout.SOUTH);
		UITools.focusOn(jTable);
		pack();
	}

	private Box createInstaller() {
		final Box installer = Box.createHorizontalBox();
		final JButton install = new JButton();
		MenuBuilder.setLabelAndMnemonic(install, getText("install"));
		install.setEnabled(false);
		install.setMaximumSize(UITools.MAX_BUTTON_DIMENSION);
		final JTextField urlField = new JTextField();
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
				try {
					LogUtils.info("installing add-on from " + urlField.getText());
					Controller.getCurrentController().getViewController().setWaitingCursor(true);
					//					final MapModel oldMap = Controller.getCurrentController().getMap();
					final URL sourceUrl = toURL(urlField.getText());
					URL localUrl = sourceUrl;
					if (!sourceUrl.getProtocol().equals("file")) {
						localUrl = downloadUrl(sourceUrl);
						LogUtils.info("downloaded " + sourceUrl + " to " + localUrl);
					}
					// FIXME: i18n
					setStatusInfo("Installing add-on...");
					Controller.getCurrentModeController().getMapController().newMap(localUrl, false);
					final IMapViewManager mapViewManager = Controller.getCurrentController().getMapViewManager();
					final String key = mapViewManager.checkIfFileIsAlreadyOpened(localUrl);
					// make the map the current map even if it was already opened
					// FIXME: i18n
					if (key == null || !mapViewManager.tryToChangeToMapView(key))
						throw new RuntimeException("Map " + urlField.getText() + " does not seem to be opened");
					final MapModel newMap = mapViewManager.getModel();
					// FIXME: hardcoded, wrong path!
					final File scriptDir = new File(ResourceController.getResourceController()
					    .getFreeplaneUserDirectory(), "scripts");
					final File installScript = new File(scriptDir, "installScriptAddOn.groovy");
					AddOnProperties addOn = (AddOnProperties) ScriptingEngine.executeScript(newMap.getRootNode(),
					    FileUtils.slurpFile(installScript));
					if (addOn != null) {
						addOn.setSourceUrl(sourceUrl);
						// FIXME: i18n
						setStatusInfo("Successfully nstalled " + addOn.getName());
					}
					Controller.getCurrentController().getMapViewManager().close(true);
					//					restartTransaction(oldMap, newMap);
				}
				catch (Exception ex) {
					UITools.errorMessage("Error on installation: " + ex);
				}
				finally {
					Controller.getCurrentController().getViewController().setWaitingCursor(false);
				}
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
		final String[] columnNames = new String[] { getText("name"), getText("version"), getText("description"),
		        getText("type"), getText("actions") };
		return new AddOnTableModel(columnNames, addOns);
	}

	private JTable createTable(final AddOnTableModel tableModel) {
		final JTable table = new JTable(tableModel);
//FIXME: Java 6
//		table.setAutoCreateRowSorter(true);
		table.setRowHeight(36);
		table.setBackground(Color.white);
		final TableColumnModel columnModel = table.getColumnModel();
		columnModel.getColumn(0).setPreferredWidth(150);
		columnModel.getColumn(1).setPreferredWidth(40);
		// FIXME: shorten, set tooltip
		columnModel.getColumn(2).setPreferredWidth(270);
		columnModel.getColumn(3).setPreferredWidth(40);
		columnModel.getColumn(4).setPreferredWidth(300);
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
		new ButtonsInCellRenderer(table, btns, actions, 4);
		return table;
	}

	private AbstractAction createConfigureAction(final AddOnTableModel tableModel) {
		return new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				final int row = Integer.parseInt(e.getActionCommand());
				final AddOnProperties addOn = tableModel.getAddOnAt(row);
				if (!addOn.supportsOperation(AddOnProperties.OP_CONFIGURE)) {
					UITools.errorMessage("Cannot configure " + addOn.getTranslatedName());
				}
				else {
					OptionPanelBuilder optionPanelBuilder = new OptionPanelBuilder();
					optionPanelBuilder.load(new StringReader(addOn.getPreferencesXml()));
					MModeController.createPropertyAction(optionPanelBuilder).actionPerformed(null);
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
					UITools.errorMessage("Cannot deactivate: " + addOn.getTranslatedName() + " is not active");
				}
				else {
					addOn.setActive(false);
					saveAddOn(addOn);
					UITools.informationMessage(addOn.getTranslatedName()
					        + " will be deactivated after a restart");
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
					UITools.errorMessage("Cannot activate: " + addOn.getTranslatedName()
					        + " is already active");
				}
				else {
					addOn.setActive(true);
					saveAddOn(addOn);
					UITools.informationMessage(addOn.getTranslatedName()
					        + " will be activated after a restart");
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
					UITools.errorMessage("Cannot deinstall " + addOn.getTranslatedName());
				}
				else {
					AddOnsController.getController().deInstall(addOn);
					UITools.informationMessage(addOn.getTranslatedName()
					        + " will be deinstalled after a restart");
				}
			}
		};
	}

	private JButton createButton(final String name) {
		final JButton button = new JButton(getText(name));
		button.setName(name);
		return button;
	}

	private URL downloadUrl(final URL url) throws IOException {
		setStatusInfo("Downloading file...");
		BufferedInputStream in = null;
		BufferedOutputStream out = null;
		try {
			LogUtils.info("downloading " + url);
			final File tempFile = createTempFile(url);
			final URLConnection connection = url.openConnection();
			final InputStream inputStream = connection.getInputStream();
			in = new BufferedInputStream(inputStream);
			final FileOutputStream fileOutputStream = new FileOutputStream(tempFile);
			out = new BufferedOutputStream(fileOutputStream);
			int i;
			while ((i = in.read()) != -1) {
				out.write(i);
			}
			out.flush();
			return tempFile.toURI().toURL();
		}
		catch (IOException e) {
			LogUtils.severe("can't download " + url, e);
			throw e;
		}
		finally {
			try {
				setStatusInfo(null);
				// I'm too lazy for another try/catch block
				if (in != null)
					in.close();
				if (out != null)
					out.close();
			}
			catch (Exception e) {
				LogUtils.severe("cannot close streams", e);
			}
		}
	}

	private File createTempFile(final URL url) throws IOException {
		final String fileName = url.getPath().replaceFirst(".*[\\\\/]", "");
		int index = fileName.lastIndexOf('.');
		if (index == -1)
			return File.createTempFile(fileName, ".mm");
		else
			return File.createTempFile(fileName.substring(0, index), ".mm");
	}

	private void setStatusInfo(final String message) {
		Controller.getCurrentController().getViewController().out("Installed " + message + "...");
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
		panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		if (buttons.length > 0)
			panel.add(buttons[0]);
		for (int i = 1; i < buttons.length; i++) {
			panel.add(Box.createHorizontalStrut(BUTTON_SPACER));
			panel.add(buttons[i]);
		}
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
			btn.setVisible(addOn.supportsOperation(btn.getName()));
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
