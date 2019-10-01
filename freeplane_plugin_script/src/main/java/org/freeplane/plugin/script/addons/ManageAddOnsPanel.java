package org.freeplane.plugin.script.addons;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;

import org.freeplane.core.resources.components.OptionPanelBuilder;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.icon.IconNotFound;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.main.addons.AddOnProperties;
import org.freeplane.main.addons.AddOnsController;

@SuppressWarnings("serial")
public class ManageAddOnsPanel extends JPanel {
	public final static class AddOnTableModel extends AbstractTableModel {
		private final List<AddOnProperties> addOns;
        private HashMap<AddOnProperties, Icon> icons = new HashMap<AddOnProperties, Icon>();

		private AddOnTableModel(List<AddOnProperties> addOns) {
			this.addOns = new ArrayList<AddOnProperties>(addOns);
		}

		public int getRowCount() {
			return addOns.size();
		}

		public int getColumnCount() {
			return buttonsColumn + 1;
		}

		public Object getValueAt(int row, int col) {
			AddOnProperties addOn = addOns.get(row);
			switch (col) {
				case iconColumn:
					return createIcon(addOn);
				case textColumn:
				    return addOn;
				case buttonsColumn:
					return "";
				default:
					throw new RuntimeException("unexpected column " + col);
			}
		}

		private Icon createIcon(final AddOnProperties addOn) {
			Icon icon = icons.get(addOn);
	        if (icon != null)
	            return icon;
	        icon = IconNotFound.createIconOrReturnNotFoundIcon(addOn.getName() + "-icon.png");
	        icons.put(addOn, icon);
	        return icon;
	    }

        public Class<?> getColumnClass(int col) {
		    if (col == 0) {
		        return ImageIcon.class;
		    } else {
		        return String.class;
		    }
		}

		public boolean isCellEditable(int row, int column) {
			return column == buttonsColumn;
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

	private static final AddonRenderer ADDON_RENDERER = new AddonRenderer();
	private AddOnTableModel tableModel;
    private final static int iconColumn = 0;
    private final static int textColumn = 1;
    private final static int buttonsColumn = 2;

	public ManageAddOnsPanel(List<AddOnProperties> addOns) {
	    super();
		final JComponent panel = this;
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
					final AddOnProperties addon = (AddOnProperties) tableModel.getValueAt(row, textColumn);
					final AddOnDetailsPanel detailsPanel = new AddOnDetailsPanel(addon);
					detailsPanel.setOpaque(false);
					descriptionScrollPane.setViewportView(detailsPanel);
				}
			}
		});
		
		panel.add(tableScrollPane);
		panel.add(descriptionScrollPane);
    }

	private JTable createTable(final AddOnTableModel tableModel) {
		final JTable table = new JTable(tableModel);
		table.setTableHeader(null);
//FIXME: Java 6
//		table.setAutoCreateRowSorter(true);
		final int rowHeight = UITools.getDefaultLabelFont().getSize() * 5;
        table.setRowHeight(rowHeight);
		table.setBackground(Color.white);
		table.setShowVerticalLines(false);
		final TableColumnModel columnModel = table.getColumnModel();
		JButton[] buttons = new JButton[] { createButton(AddOnProperties.OP_CONFIGURE) //
		        , createButton(AddOnProperties.OP_DEACTIVATE) //
		        , createButton(AddOnProperties.OP_ACTIVATE) //
		        , createButton(AddOnProperties.OP_DEINSTALL) //
		};
		columnModel.getColumn(iconColumn).setMinWidth(rowHeight);
		columnModel.getColumn(iconColumn).setPreferredWidth(rowHeight);
		columnModel.getColumn(textColumn).setPreferredWidth(10000);
		columnModel.getColumn(buttonsColumn).setMinWidth(getPreferredWidth(buttons));
		columnModel.getColumn(buttonsColumn).setPreferredWidth(getPreferredWidth(buttons));
		Action[] actions = new Action[] { createConfigureAction(tableModel) //
		        , createDeactivateAction(tableModel) //
		        , createActivateAction(tableModel) //
		        , createDeinstallAction(tableModel) //
		};
		table.getColumnModel().getColumn(textColumn).setCellRenderer(ADDON_RENDERER);
		new ButtonsInCellRenderer(table, buttons, actions, buttonsColumn);
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
					JOptionPane.showMessageDialog(ManageAddOnsPanel.this, getText("cannot.configure", addOn.getTranslatedName()), "Freeplane", JOptionPane.ERROR_MESSAGE);
				}
				else {
					OptionPanelBuilder optionPanelBuilder = new OptionPanelBuilder();
					optionPanelBuilder.load(new StringReader(addOn.getPreferencesXml()));
					MModeController.createShowPreferencesAction(optionPanelBuilder).actionPerformed(e);
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
					JOptionPane.showMessageDialog(ManageAddOnsPanel.this, getText("cannot.deactivate", addOn.getTranslatedName()), "Freeplane", JOptionPane.ERROR_MESSAGE);
				}
				else {
					addOn.setActive(false);
					saveAddOn(addOn);
					JOptionPane.showMessageDialog(ManageAddOnsPanel.this, getText("deactivation.success", addOn.getTranslatedName()), "Freeplane", JOptionPane.INFORMATION_MESSAGE);
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
					JOptionPane.showMessageDialog(ManageAddOnsPanel.this, getText("cannot.activate", addOn.getTranslatedName()), "Freeplane", JOptionPane.ERROR_MESSAGE);
				}
				else {
					addOn.setActive(true);
					saveAddOn(addOn);
					JOptionPane.showMessageDialog(ManageAddOnsPanel.this, getText("activation.success", addOn.getTranslatedName()), "Freeplane", JOptionPane.INFORMATION_MESSAGE);
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
					int result = JOptionPane.showConfirmDialog(ManageAddOnsPanel.this,
					    getText("really.deinstall", TextUtils.getText(addOn.getNameKey())), getText("deinstall"),
					    JOptionPane.OK_CANCEL_OPTION);
					if (result == JOptionPane.OK_OPTION) {
					    deinstall(tableModel, addOn);
						repaint();
						UITools.informationMessage(getText("deinstallation.success", addOn.getTranslatedName()));
					}
				}
			}

            private void deinstall(final AddOnTableModel tableModel, final AddOnProperties addOn) {
                try {
                    AddOnsController.getController().deinstall(addOn);
                    tableModel.removeAddOn(addOn);
                }
                finally {
                    Controller.getCurrentController().getViewController().setWaitingCursor(false);
                }
            }
		};
	}

	private JButton createButton(final String name) {
		final JButton button = new JButton(getText(name));
		button.setName(name);
		return button;
	}

	private void saveAddOn(final AddOnProperties addOn) {
	    try {
	        AddOnsController.getController().save(addOn);
        }
        catch (IOException e) {
			UITools.errorMessage("Cannot save add-on settings: " + e.getMessage());
        }
    }

	public AddOnTableModel getTableModel() {
	    return tableModel;
    }

	private static String getText(String key, Object... parameters) {
		return ManageAddOnsDialog.getText(key, parameters);
	}
}
