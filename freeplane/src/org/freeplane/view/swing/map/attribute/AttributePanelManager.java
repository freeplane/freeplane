/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2011 dimitry
 *
 *  This file author is dimitry
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.freeplane.view.swing.map.attribute;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.controller.INodeSelectionListener;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.common.attribute.AttributeController;
import org.freeplane.features.common.format.FormatController;
import org.freeplane.features.common.format.IFormattedObject;
import org.freeplane.features.common.map.ModeController;
import org.freeplane.features.common.map.NodeModel;
import org.freeplane.view.swing.map.NodeView;

/**
 * @author Dimitry Polivaev
 * Jan 9, 2011
 */
public class AttributePanelManager{
	final private JPanel tablePanel;
	private ModeController modeController;
	private int axis = BoxLayout.Y_AXIS;
	private class TableCreator implements INodeSelectionListener{

		private AttributeView attributeView;
		private JComboBox formatChooser;

		public void onDeselect(NodeModel node) {
			removeOldView();
        }

		private void removeOldView() {
	        if(attributeView != null){
				tablePanel.removeAll();
				tablePanel.revalidate();
				tablePanel.repaint();
				attributeView.viewRemoved();
				attributeView = null;
			}
        }

		public void onSelect(NodeModel node) {
			removeOldView();
			final NodeView nodeView = (NodeView) Controller.getCurrentController().getViewController()
			    .getSelectedComponent();
			if (nodeView == null)
				return;
			AttributeController.getController(modeController).createAttributeTableModel(node);
			attributeView = new AttributeView(nodeView, false);
			Box buttonBox = new Box(axis);
			buttonBox.setAlignmentX(0.5f);
			tablePanel.add(buttonBox);
			Dimension btnSize = new Dimension();
			{
				final JButton newAttributeButton = new JButton(TextUtils.getText("attributes_popup_new"));
				newAttributeButton.addActionListener(new ActionListener() {
					public void actionPerformed(final ActionEvent arg0) {
						attributeView.addRow();
					}
				});
				increaseSize(btnSize, newAttributeButton);
				buttonBox.add(newAttributeButton);
			}
			{
				final JButton optimalWidthButton = new JButton(TextUtils.getText("attributes_popup_optimal_width"));
				optimalWidthButton.addActionListener(new ActionListener() {
					public void actionPerformed(final ActionEvent arg0) {
						attributeView.setOptimalColumnWidths();
					}
				});
				increaseSize(btnSize, optimalWidthButton);
				buttonBox.add(optimalWidthButton);
			}
			{
				formatChooser = createFormatChooser();
				formatChooser.setEnabled(false);
				increaseSize(btnSize, formatChooser);
				buttonBox.add(formatChooser);
			}
			for (int i = 0; i < buttonBox.getComponentCount(); i++) {
				buttonBox.getComponent(i).setMaximumSize(btnSize);
			}
			formatChooser.addItemListener(new ItemListener() {
				boolean handlingEvent = false;

				public void itemStateChanged(final ItemEvent e) {
					if (handlingEvent || !formatChooser.isEnabled())
						return;
					handlingEvent = true;
					final String newFormat = (String) e.getItem();
					final AttributeTable table = attributeView.getAttributeTable();
					if (table.getSelectedColumn() == 1 && table.getSelectedRow() != -1) {
						final Object value = table.getValueAt(table.getSelectedRow(), table.getSelectedColumn());
						try {
							final Object newValue = formatValue(newFormat, table, value);
							if (newValue != null)
								attributeView.getCurrentAttributeTableModel().setValueAt(newValue,
								    table.getSelectedRow(), table.getSelectedColumn());
						}
						catch (Exception e2) {
							Controller.getCurrentController().getViewController()
							    .out("Pattern is not applicable: " + e2.getMessage());
							LogUtils.warn("pattern is not applicable", e2);
						}
					}
					handlingEvent = false;
				}

				private Object formatValue(final String newFormat, final AttributeTable table, final Object toFormat) {
					if (formatChooser.getSelectedItem() == null)
						return null;
					return FormatController.format(toFormat, newFormat);
				}
			});

			attributeView.addTableSelectionListener(new ListSelectionListener() {
				public void valueChanged(final ListSelectionEvent event) {
					// update format chooser
					if (!event.getValueIsAdjusting()) {
						final AttributeTable table = attributeView.getAttributeTable();
						if (table.getSelectedColumn() == 1 && table.getSelectedRow() != -1) {
							formatChooser.setEnabled(true);
							final Object value = table.getValueAt(table.getSelectedRow(), table.getSelectedColumn());
							if (value instanceof IFormattedObject) {
								final String format = ((IFormattedObject) value).getPattern();
								formatChooser.setSelectedItem(format);
							}
							else {
								formatChooser.setSelectedItem(null);
							}
						}
						else {
							formatChooser.setEnabled(false);
						}
					}
				}
			});
			tablePanel.add(Box.createVerticalStrut(5));
			JComponent c = attributeView.getContainer();
			tablePanel.add(c);
			tablePanel.add(Box.createGlue());
			tablePanel.revalidate();
			tablePanel.repaint();
		}

		private JComboBox createFormatChooser() {
			final List<String> formatPatterns = new FormatController().getAllPatterns();
			final JComboBox formatChooser = new JComboBox(new Vector<String>(formatPatterns));
			formatChooser.setEditable(true);
			formatChooser.setSelectedItem(null);
			final String NODE_FORMAT = "OptionPanel.nodeformat"; // duplicated from StyleEditorPanel
			formatChooser.setToolTipText(TextUtils.getText(NODE_FORMAT + ".tooltip"));
			formatChooser.setAlignmentX(Component.LEFT_ALIGNMENT);
			return formatChooser;
		}

		private void increaseSize(final Dimension btnSize, final JComponent comp) {
		    final Dimension preferredSize = comp.getPreferredSize();
		    btnSize.width =  Math.max(btnSize.width, preferredSize.width);
		    btnSize.height =  Math.max(btnSize.height, preferredSize.height);
	    }
	}

	public AttributePanelManager(final ModeController modeController){
		this.modeController = modeController;
		tablePanel = new JPanel();
		tablePanel.setLayout(new BoxLayout(tablePanel, axis));
		modeController.getMapController().addNodeSelectionListener(new TableCreator());
	}
	public JPanel getTablePanel() {
    	return tablePanel;
    }
}
