/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2010 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is created by Stefan Ott in 2011.
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
package org.freeplane.features.attribute.mindmapmode;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;
import java.util.NoSuchElementException;

import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.EnabledAction;
import org.freeplane.core.ui.components.TypedListCellRenderer;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.attribute.AttributeController;
import org.freeplane.features.attribute.AttributeRegistry;
import org.freeplane.features.attribute.AttributeRegistryElement;
import org.freeplane.features.attribute.NodeAttributeTableModel;
import org.freeplane.features.attribute.mindmapmode.AssignAttributeDialog.ClonedComboBoxModel;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;

@EnabledAction(checkOnNodeChange = true)
public class AddAttributeAction extends AFreeplaneAction {
	/**
	 * @author Stefan Ott
	 * 
	 * This action adds an attribute to all selected nodes
	 */
	private static final long serialVersionUID = 1L;
	private JComboBox attributeNames = null;
	private JComboBox attributeValues = null;
	private final AttributeController attrContr = AttributeController.getController();

	public AddAttributeAction() {
		super("attributes_AddAttributeAction");
	};

	public void actionPerformed(final ActionEvent arg0) {
		final List<NodeModel> nodes = Controller.getCurrentModeController().getMapController().getSelectedNodes();
		final int selection = UITools.showConfirmDialog(Controller.getCurrentController().getSelection().getSelected(),
		    getDialog(), TextUtils.removeMnemonic(TextUtils.getText("attributes_AddAttributeAction.text")),
		    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		//OK button pressed
		if (selection == JOptionPane.OK_OPTION) {
			if (attributeNames.getSelectedItem() == null) {
				UITools.showAttributeEmptyStringErrorMessage();
				return;
			}
			final String name = attributeNames.getSelectedItem().toString();
			if (name.equals("")) {
				UITools.showAttributeEmptyStringErrorMessage();
				return;
			}
			final Object valueSelectedItem = attributeValues.getSelectedItem();
			final String value = valueSelectedItem != null ? valueSelectedItem.toString() : "";
			//Add attributes to nodes
			for (final NodeModel node : nodes) {
				final NodeAttributeTableModel attributes = NodeAttributeTableModel.getModel(node);
				attrContr.performInsertRow(attributes, attributes.getRowCount(), name, value);
			}
		}
	}

	/**
	 * This method creates the input dialog
	 * 
	 * @return : the input dialog
	 */
	private JPanel getDialog() {
		final JPanel dialog = new JPanel();
		dialog.setLayout(new GridBagLayout());
		dialog.setBorder(new EtchedBorder());
		final GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.anchor = GridBagConstraints.CENTER;
		gridBagConstraints.insets = new Insets(20, 10, 2, 10);
		// Size of JComboBoxes
		final String pattern = "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXX";
		final JLabel patternLabel = new JLabel(pattern);
		final Dimension comboBoxMaximumSize = patternLabel.getPreferredSize();
		comboBoxMaximumSize.width += 4;
		comboBoxMaximumSize.height += 10;
		//Label: name
		final JLabel nameLabel = new JLabel("Attribute Name");
		dialog.add(nameLabel, gridBagConstraints);
		gridBagConstraints.gridx++;
		//Label: value
		final JLabel valueLabel = new JLabel("Attribute Value");
		dialog.add(valueLabel, gridBagConstraints);
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy++;
		//Attribute name combo-box
		gridBagConstraints.insets = new Insets(2, 10, 20, 10);
		attributeNames = new JComboBox();
		final MapModel map = Controller.getCurrentController().getMap();
		final AttributeRegistry attributes = AttributeRegistry.getRegistry(map);
		final ComboBoxModel names = attributes.getComboBoxModel();
		attributeNames.setModel(new ClonedComboBoxModel(names));
		attributeNames.setEditable(true);
		attributeNames.setMaximumSize(comboBoxMaximumSize);
		attributeNames.setPreferredSize(comboBoxMaximumSize);
		attributeNames.addItemListener(new ItemListener() {
			public void itemStateChanged(final ItemEvent e) {
				selectedAttributeChanged(e.getItem(), attributeValues);
			}
		});
		dialog.add(attributeNames, gridBagConstraints);
		//Attribute value combo-box
		attributeValues = new JComboBox();
		attributeValues.setRenderer(new TypedListCellRenderer());
		attributeValues.setMaximumSize(comboBoxMaximumSize);
		attributeValues.setPreferredSize(comboBoxMaximumSize);
		gridBagConstraints.gridx++;
		dialog.add(attributeValues, gridBagConstraints);
		//set focus to attributeNames
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				attributeNames.requestFocusInWindow();
			}
		});
		return dialog;
	}

	protected void selectedAttributeChanged(final Object selectedAttributeName, final JComboBox values) {
		final MapModel map = Controller.getCurrentController().getMap();
		final AttributeRegistry attributes = AttributeRegistry.getRegistry(map);
		try {
			final AttributeRegistryElement element = attributes.getElement(selectedAttributeName.toString());
			final ComboBoxModel selectedValues = element.getValues();
			values.setModel(new ClonedComboBoxModel(selectedValues));
			try {
				final Object firstValue = selectedValues.getElementAt(0);
				values.setSelectedItem(firstValue);
			}
			catch (final ArrayIndexOutOfBoundsException ex) {
			}
			values.setEditable(!element.isRestricted());
		}
		catch (final NoSuchElementException ex) {
			values.setEditable(!selectedAttributeName.toString().equals(""));
		}
	}
}
