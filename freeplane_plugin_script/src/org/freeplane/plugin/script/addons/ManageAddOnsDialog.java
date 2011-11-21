package org.freeplane.plugin.script.addons;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;

import org.freeplane.core.resources.components.OptionPanelBuilder;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.main.addons.AddOnProperties;
import org.freeplane.main.addons.AddOnsController;

@SuppressWarnings("serial")
public class ManageAddOnsDialog extends JDialog {
	private static final AddonRenderer ADDON_RENDERER = new AddonRenderer();

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
					return addOn;
				case 1:
					return "";
				default:
					throw new RuntimeException("unexpected column " + col);
			}
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
			final int row = addOns.size();
			addOns.add(addOn);
			fireTableRowsInserted(row, row);
		}

		public void removeAddOn(final AddOnProperties addOn) {
			final int row = addOns.indexOf(addOn);
			if(row == -1)
				return;
			addOns.remove(row);
			fireTableRowsDeleted(row, row);
		}
	}

	private static final long serialVersionUID = 1L;
	private AddOnTableModel tableModel;
	private AddOnInstallerPanel addOnInstallerPanel;
	private JTabbedPane tabbedPane;

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
		tabbedPane = new JTabbedPane();
		tabbedPane.setPreferredSize(getPreferredSizeForWindow());
		final JComponent managementPanel = createManagementPanel(addOns);
		tabbedPane.addTab(getText("tab.manage"), null, managementPanel, getText("tab.manage.tooltip"));
		addOnInstallerPanel = new AddOnInstallerPanel(tableModel, managementPanel);
		tabbedPane.addTab(getText("tab.install"), null, addOnInstallerPanel, getText("tab.install.tooltip"));
		getContentPane().add(tabbedPane, BorderLayout.CENTER);
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton closeButton = new JButton();
				MenuBuilder.setLabelAndMnemonic(closeButton, TextUtils.getRawText("close_btn"));
				closeButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						setVisible(false);
					}
				});
				buttonPane.add(closeButton);
			}
		}
		
		pack();
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		UITools.addEscapeActionToDialog(this);
	}

	private JComponent createManagementPanel(final List<AddOnProperties> addOns) {
		final JComponent panel = new JPanel();
		panel.setLayout(new GridLayout(2,1));
		tableModel = new AddOnTableModel(addOns);
		final JTable jTable = createTable(tableModel);
		JScrollPane tableScrollPane = new JScrollPane(jTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
		    JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		tableScrollPane.getViewport().setBackground(Color.white);
		
		final JPanel emptyPanel = new JPanel();
		emptyPanel.setOpaque(false);
		final JScrollPane descriptionScrollPane = new JScrollPane(emptyPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		descriptionScrollPane.getViewport().setBackground(Color.white);
		jTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if(jTable.getSelectedRowCount() != 1){
					descriptionScrollPane.setViewportView(emptyPanel);
				}
				else{
					final int row = jTable.getSelectedRow();
					final AddOnProperties addon = (AddOnProperties) tableModel.getValueAt(row, 0);
					final AddOnDetailsPanel detailsPanel = new AddOnDetailsPanel(addon);
					detailsPanel.setOpaque(false);
					descriptionScrollPane.setViewportView(detailsPanel);
				}
			}
		});
		
		panel.add(tableScrollPane);
		panel.add(descriptionScrollPane);
		return panel;
	}

	private Dimension getPreferredSizeForWindow() {
		final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		return new Dimension((int) screenSize.getWidth() * 4 / 5, (int) screenSize.getHeight() * 2 / 3);
	}

	private JTable createTable(final AddOnTableModel tableModel) {
		final JTable table = new JTable(tableModel);
		table.setTableHeader(null);
//FIXME: Java 6
//		table.setAutoCreateRowSorter(true);
		table.setRowHeight(62);
		table.setBackground(Color.white);
		table.setShowVerticalLines(false);
		final TableColumnModel columnModel = table.getColumnModel();
		JButton[] buttons = new JButton[] { createButton(AddOnProperties.OP_CONFIGURE) //
		        , createButton(AddOnProperties.OP_DEACTIVATE) //
		        , createButton(AddOnProperties.OP_ACTIVATE) //
		        , createButton(AddOnProperties.OP_DEINSTALL) //
		};
		columnModel.getColumn(0).setPreferredWidth(10000);
		columnModel.getColumn(1).setMinWidth(getPreferredWidth(buttons));
		columnModel.getColumn(1).setPreferredWidth(getPreferredWidth(buttons));
		Action[] actions = new Action[] { createConfigureAction(tableModel) //
		        , createDeactivateAction(tableModel) //
		        , createActivateAction(tableModel) //
		        , createDeinstallAction(tableModel) //
		};
		table.getColumnModel().getColumn(0).setCellRenderer(ADDON_RENDERER);
		new ButtonsInCellRenderer(table, buttons, actions, 1);
		table.setFocusable(false);
		return table;
	}

	private int getPreferredWidth(JButton[] buttons) {
		double maxButtonWidth = 0;
		for (JButton button : buttons) {
			final Dimension size = button.getPreferredSize();
			if (size.getWidth() > maxButtonWidth)
				maxButtonWidth = size.getWidth();
		}
		// activate/deactivate exclude each other -> -1
		int spacer = ButtonsInCellRenderer.BUTTON_SPACER;
		return (int) ((buttons.length - 1) * (maxButtonWidth + spacer)) + spacer;
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
						tableModel.removeAddOn(addOn);
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

	static void setStatusInfo(final String message) {
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

	static String getText(String key, Object... parameters) {
		if (parameters.length == 0)
			return TextUtils.getText(getResourceKey(key));
		else
			return TextUtils.format(getResourceKey(key), parameters);
	}

	public void install(final URL url) {
		if(addOnInstallerPanel.isShowing()){
			addOnInstallerPanel.getUrlField().setText(url.toString());
			tabbedPane.paintImmediately(0, 0, tabbedPane.getWidth(), tabbedPane.getHeight());
			addOnInstallerPanel.getInstallButton().doClick();
		}
		else{
			addOnInstallerPanel.addHierarchyListener(new HierarchyListener() {
				public void hierarchyChanged(HierarchyEvent e) {
					if(addOnInstallerPanel.isShowing()){
						addOnInstallerPanel.removeHierarchyListener(this);
						install(url);
					}
				}
			});
			tabbedPane.setSelectedComponent(addOnInstallerPanel);
			if(! isVisible())
				setVisible(true);
		}
    }
}
