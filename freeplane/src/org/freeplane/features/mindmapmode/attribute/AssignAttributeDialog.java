/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Dimitry Polivaev
 *
 *  This file author is Dimitry Polivaev
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
package org.freeplane.features.mindmapmode.attribute;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.swing.AbstractListModel;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListDataListener;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.frame.IMapSelectionListener;
import org.freeplane.core.modecontroller.IMapSelection;
import org.freeplane.core.model.MapModel;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.resources.FpStringUtils;
import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.features.common.attribute.Attribute;
import org.freeplane.features.common.attribute.AttributeController;
import org.freeplane.features.common.attribute.AttributeRegistry;
import org.freeplane.features.common.attribute.AttributeRegistryElement;
import org.freeplane.features.common.attribute.NodeAttributeTableModel;

class AssignAttributeDialog extends JDialog implements IAttributesListener, IMapSelectionListener {
	private class AddAction extends IteratingAction {
		private String name;
		private String value;

		@Override
		public void actionPerformed(final ActionEvent e) {
			if (attributeNames.getSelectedItem() == null) {
				showEmptyStringErrorMessage();
				return;
			}
			name = attributeNames.getSelectedItem().toString();
			if (name.equals("")) {
				showEmptyStringErrorMessage();
				return;
			}
			final Object valueSelectedItem = attributeValues.getSelectedItem();
			value = valueSelectedItem != null ? valueSelectedItem.toString() : "";
			super.actionPerformed(e);
			if (valueSelectedItem == null) {
				selectedAttributeChanged(name, attributeValues);
			}
		}

		@Override
		protected void performAction(final NodeModel model) {
			attributeController.createAttributeTableModel(model);
			final NodeAttributeTableModel attributes = NodeAttributeTableModel.getModel(model);
			attributeController.performInsertRow(attributes, attributes.getRowCount(), name, value);
		}
	}

	private static class ClonedComboBoxModel extends AbstractListModel implements ComboBoxModel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private Object selectedItem;
		final private AbstractListModel sharedListModel;

		public ClonedComboBoxModel(final ComboBoxModel sharedListModel) {
			super();
			this.sharedListModel = (AbstractListModel) sharedListModel;
		}

		@Override
		public void addListDataListener(final ListDataListener l) {
			super.addListDataListener(l);
			sharedListModel.addListDataListener(l);
		}

		public Object getElementAt(final int index) {
			return sharedListModel.getElementAt(index);
		}

		public Object getSelectedItem() {
			return selectedItem;
		}

		public int getSize() {
			return sharedListModel.getSize();
		}

		@Override
		public void removeListDataListener(final ListDataListener l) {
			super.removeListDataListener(l);
			sharedListModel.removeListDataListener(l);
		}

		public void setSelectedItem(final Object anItem) {
			selectedItem = anItem;
			fireContentsChanged(this, -1, -1);
		}
	}

	private class DeleteAttributeAction extends IteratingAction {
		private String name;

		@Override
		public void actionPerformed(final ActionEvent e) {
			final Object selectedItem = attributeNames.getSelectedItem();
			if (selectedItem == null) {
				showEmptyStringErrorMessage();
				return;
			}
			name = selectedItem.toString();
			if (name.equals("")) {
				showEmptyStringErrorMessage();
				return;
			}
			super.actionPerformed(e);
		}

		@Override
		protected void performAction(final NodeModel model) {
			final NodeAttributeTableModel attributes = NodeAttributeTableModel.getModel(model);
			for (int i = attributes.getRowCount() - 1; i >= 0; i--) {
				if (attributes.getAttribute(i).getName().equals(name)) {
					attributeController.performRemoveRow(attributes, i);
				}
			}
		}
	}

	private class DeleteValueAction extends IteratingAction {
		private String name;
		private String value;

		@Override
		public void actionPerformed(final ActionEvent e) {
			if (attributeNames.getSelectedItem() == null) {
				showEmptyStringErrorMessage();
				return;
			}
			name = attributeNames.getSelectedItem().toString();
			if (name.equals("")) {
				showEmptyStringErrorMessage();
				return;
			}
			final Object valueSelectedItem = attributeValues.getSelectedItem();
			value = valueSelectedItem != null ? valueSelectedItem.toString() : "";
			super.actionPerformed(e);
		}

		@Override
		protected void performAction(final NodeModel model) {
			final NodeAttributeTableModel attributes = NodeAttributeTableModel.getModel(model);
			for (int i = attributes.getRowCount() - 1; i >= 0; i--) {
				final Attribute attribute = attributes.getAttribute(i);
				if (attribute.getName().equals(name) && attribute.getValue().equals(value)) {
					attributeController.performRemoveRow(attributes, i);
				}
			}
		}
	}

	private abstract class IteratingAction implements ActionListener {
		public void actionPerformed(final ActionEvent e) {
			try {
				if (selectedBtn.getModel().isSelected()) {
					final Collection<NodeModel> selecteds = mapSelection.getSelection();
					final Iterator<NodeModel> iterator = selecteds.iterator();
					while (iterator.hasNext()) {
						final NodeModel selectedNodeView = iterator.next();
						performAction(selectedNodeView);
					}
					return;
				}
				final NodeModel nodeView = controller.getMap().getRootNode();
				iterate(nodeView);
			}
			catch (final NullPointerException ex) {
			}
		}

		private void iterate(final NodeModel node) {
			if (node.isVisible()) {
				if (!node.isRoot() || !skipRootBtn.isSelected()) {
					performAction(node);
				}
			}
			if (node.isFolded()) {
				return;
			}
			final Iterator<NodeModel> iterator = node.getChildren().iterator();
			while (iterator.hasNext()) {
				iterate(iterator.next());
			}
		}

		abstract protected void performAction(NodeModel model);

		protected void showEmptyStringErrorMessage() {
			JOptionPane.showMessageDialog(AssignAttributeDialog.this, ResourceBundles
			    .getText("attributes_adding_empty_attribute_error"), ResourceBundles.getText("error"),
			    JOptionPane.ERROR_MESSAGE);
		}
	}

	private class ReplaceValueAction extends IteratingAction {
		private String name;
		private String replacingName;
		private String replacingValue;
		private String value;

		@Override
		public void actionPerformed(final ActionEvent e) {
			if (attributeNames.getSelectedItem() == null) {
				showEmptyStringErrorMessage();
				return;
			}
			if (replacingAttributeNames.getSelectedItem() == null) {
				showEmptyStringErrorMessage();
				return;
			}
			name = attributeNames.getSelectedItem().toString();
			if (name.equals("")) {
				showEmptyStringErrorMessage();
				return;
			}
			replacingName = replacingAttributeNames.getSelectedItem().toString();
			if (replacingName.equals("")) {
				showEmptyStringErrorMessage();
				return;
			}
			final Object valueSelectedItem = attributeValues.getSelectedItem();
			value = valueSelectedItem != null ? valueSelectedItem.toString() : "";
			final Object replacingValueSelectedItem = replacingAttributeValues.getSelectedItem();
			replacingValue = replacingValueSelectedItem != null ? replacingValueSelectedItem.toString() : "";
			super.actionPerformed(e);
		}

		@Override
		protected void performAction(final NodeModel model) {
			final NodeAttributeTableModel attributes = NodeAttributeTableModel.getModel(model);
			for (int i = attributes.getRowCount() - 1; i >= 0; i--) {
				final Attribute attribute = attributes.getAttribute(i);
				if (attribute.getName().equals(name) && attribute.getValue().equals(value)) {
					attributeController.performRemoveRow(attributes, i);
					attributeController.performInsertRow(attributes, i, replacingName, replacingValue);
				}
			}
		}
	}

	private static final Dimension maxButtonDimension = new Dimension(1000, 1000);
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	final private AttributeController attributeController;
	final private JComboBox attributeNames;
	final private JComboBox attributeValues;
	final private Controller controller;
	private IMapSelection mapSelection;
	final private JComboBox replacingAttributeNames;
	final private JComboBox replacingAttributeValues;
	final private JRadioButton selectedBtn;
	final private JCheckBox skipRootBtn;
	final private JRadioButton visibleBtn;

	public AssignAttributeDialog(final AttributeController attributeController, final Frame frame) {
		super(frame, FpStringUtils.removeMnemonic(ResourceBundles.getText("attributes_assign_dialog")), false);
		this.attributeController = attributeController;
		controller = attributeController.getModeController().getController();
		mapSelection = controller.getSelection();
		final Border actionBorder = new MatteBorder(2, 2, 2, 2, Color.BLACK);
		final Border emptyBorder = new EmptyBorder(5, 5, 5, 5);
		final Border btnBorder = new EmptyBorder(2, 2, 2, 2);
		selectedBtn = new JRadioButton();
		MenuBuilder.setLabelAndMnemonic(selectedBtn, ResourceBundles.getText("attributes_for_selected"));
		selectedBtn.setSelected(true);
		visibleBtn = new JRadioButton();
		MenuBuilder.setLabelAndMnemonic(visibleBtn, ResourceBundles.getText("attributes_for_visible"));
		final ButtonGroup group = new ButtonGroup();
		group.add(selectedBtn);
		group.add(visibleBtn);
		skipRootBtn = new JCheckBox();
		MenuBuilder.setLabelAndMnemonic(skipRootBtn, ResourceBundles.getText("attributes_skip_root"));
		skipRootBtn.setSelected(true);
		final Box selectionBox = Box.createHorizontalBox();
		selectionBox.setBorder(emptyBorder);
		selectionBox.add(Box.createHorizontalGlue());
		selectionBox.add(selectedBtn);
		selectionBox.add(Box.createHorizontalGlue());
		selectionBox.add(visibleBtn);
		selectionBox.add(Box.createHorizontalGlue());
		selectionBox.add(skipRootBtn);
		selectionBox.add(Box.createHorizontalGlue());
		getContentPane().add(selectionBox, BorderLayout.NORTH);
		final JButton addBtn = new JButton();
		MenuBuilder.setLabelAndMnemonic(addBtn, ResourceBundles.getText("filter_add"));
		addBtn.addActionListener(new AddAction());
		addBtn.setMaximumSize(AssignAttributeDialog.maxButtonDimension);
		final JButton deleteAttributeBtn = new JButton();
		MenuBuilder.setLabelAndMnemonic(deleteAttributeBtn, ResourceBundles.getText("attribute_delete"));
		deleteAttributeBtn.addActionListener(new DeleteAttributeAction());
		deleteAttributeBtn.setMaximumSize(AssignAttributeDialog.maxButtonDimension);
		final JButton deleteAttributeValueBtn = new JButton();
		MenuBuilder.setLabelAndMnemonic(deleteAttributeValueBtn, ResourceBundles.getText("attribute_delete_value"));
		deleteAttributeValueBtn.addActionListener(new DeleteValueAction());
		deleteAttributeValueBtn.setMaximumSize(AssignAttributeDialog.maxButtonDimension);
		final JButton replaceBtn = new JButton();
		MenuBuilder.setLabelAndMnemonic(replaceBtn, ResourceBundles.getText("attribute_replace"));
		replaceBtn.addActionListener(new ReplaceValueAction());
		replaceBtn.setMaximumSize(AssignAttributeDialog.maxButtonDimension);
		UITools.addEscapeActionToDialog(this);
		final String pattern = "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXX";
		final JLabel patternLabel = new JLabel(pattern);
		final Dimension comboBoxMaximumSize = patternLabel.getPreferredSize();
		comboBoxMaximumSize.width += 4;
		comboBoxMaximumSize.height += 4;
		attributeNames = new JComboBox();
		attributeNames.setMaximumSize(comboBoxMaximumSize);
		attributeNames.setPreferredSize(comboBoxMaximumSize);
		attributeNames.addItemListener(new ItemListener() {
			public void itemStateChanged(final ItemEvent e) {
				selectedAttributeChanged(e.getItem(), attributeValues);
			}
		});
		attributeValues = new JComboBox();
		attributeValues.setMaximumSize(comboBoxMaximumSize);
		attributeValues.setPreferredSize(comboBoxMaximumSize);
		replacingAttributeNames = new JComboBox();
		replacingAttributeNames.setMaximumSize(comboBoxMaximumSize);
		replacingAttributeNames.setPreferredSize(comboBoxMaximumSize);
		replacingAttributeNames.addItemListener(new ItemListener() {
			public void itemStateChanged(final ItemEvent e) {
				selectedAttributeChanged(e.getItem(), replacingAttributeValues);
			}
		});
		replacingAttributeValues = new JComboBox();
		replacingAttributeValues.setMaximumSize(comboBoxMaximumSize);
		replacingAttributeValues.setPreferredSize(comboBoxMaximumSize);
		final Box addDeleteBtnBox = Box.createVerticalBox();
		addDeleteBtnBox.setBorder(btnBorder);
		addDeleteBtnBox.add(Box.createVerticalGlue());
		addDeleteBtnBox.add(addBtn);
		addDeleteBtnBox.add(deleteAttributeBtn);
		addDeleteBtnBox.add(deleteAttributeValueBtn);
		addDeleteBtnBox.add(Box.createVerticalGlue());
		final Box addDeleteBox = Box.createHorizontalBox();
		addDeleteBox.setBorder(actionBorder);
		addDeleteBox.add(Box.createHorizontalGlue());
		addDeleteBox.add(addDeleteBtnBox);
		addDeleteBox.add(Box.createHorizontalStrut(5));
		addDeleteBox.add(attributeNames);
		addDeleteBox.add(Box.createHorizontalStrut(5));
		addDeleteBox.add(attributeValues);
		addDeleteBox.add(Box.createHorizontalStrut(5));
		final Box outerReplaceBox = Box.createVerticalBox();
		outerReplaceBox.setBorder(actionBorder);
		final Box replaceBox = Box.createHorizontalBox();
		replaceBox.setBorder(btnBorder);
		replaceBox.add(Box.createHorizontalGlue());
		replaceBox.add(replaceBtn);
		replaceBox.add(Box.createHorizontalStrut(5));
		replaceBox.add(replacingAttributeNames);
		replaceBox.add(Box.createHorizontalStrut(5));
		replaceBox.add(replacingAttributeValues);
		replaceBox.add(Box.createHorizontalStrut(5));
		outerReplaceBox.add(Box.createVerticalGlue());
		outerReplaceBox.add(replaceBox);
		outerReplaceBox.add(Box.createVerticalGlue());
		final Box actionBox = Box.createVerticalBox();
		actionBox.add(Box.createVerticalGlue());
		actionBox.add(addDeleteBox);
		actionBox.add(Box.createVerticalStrut(5));
		actionBox.add(outerReplaceBox);
		actionBox.add(Box.createVerticalGlue());
		getContentPane().add(actionBox, BorderLayout.CENTER);
		final JButton closeBtn = new JButton();
		MenuBuilder.setLabelAndMnemonic(closeBtn, ResourceBundles.getText("CloseAction.text"));
		closeBtn.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				dispose();
			}
		});
		final Box bottomBox = Box.createHorizontalBox();
		bottomBox.setBorder(emptyBorder);
		bottomBox.add(Box.createHorizontalGlue());
		bottomBox.add(closeBtn);
		bottomBox.add(Box.createHorizontalGlue());
		getContentPane().add(bottomBox, BorderLayout.SOUTH);
		pack();
		comboBoxMaximumSize.width = 1000;
		attributeNames.setMaximumSize(comboBoxMaximumSize);
		attributeValues.setMaximumSize(comboBoxMaximumSize);
		replacingAttributeNames.setMaximumSize(comboBoxMaximumSize);
		replacingAttributeValues.setMaximumSize(comboBoxMaximumSize);
		afterMapChange(null, controller.getMap());
		controller.getMapViewManager().addMapSelectionListener(this);
	}

	public void afterMapChange(final MapModel oldMap, final MapModel newMap) {
		if (oldMap != null) {
			final AttributeRegistry attributes = AttributeRegistry.getRegistry(oldMap);
			if (attributes != null) {
				attributes.removeAttributesListener(this);
			}
		}
		if (newMap == null) {
			setVisible(false);
			return;
		}
		mapSelection = controller.getSelection();
		final AttributeRegistry attributes = AttributeRegistry.getRegistry(newMap);
		if (attributes == null) {
			setVisible(false);
			return;
		}
		attributes.addAttributesListener(this);
		attributesChanged();
	}

	public void afterMapClose(final MapModel pOldMapView) {
	}

	private void attributesChanged() {
		final MapModel map = controller.getMap();
		final AttributeRegistry attributes = AttributeRegistry.getRegistry(map);
		final ComboBoxModel names = attributes.getComboBoxModel();
		attributeNames.setModel(new ClonedComboBoxModel(names));
		attributeNames.setEditable(!attributes.isRestricted());
		replacingAttributeNames.setModel(new ClonedComboBoxModel(names));
		replacingAttributeNames.setEditable(!attributes.isRestricted());
		if (attributes.size() > 0) {
			final Object first = names.getElementAt(0);
			attributeNames.setSelectedItem(first);
			replacingAttributeNames.setSelectedItem(first);
			selectedAttributeChanged(attributeNames.getSelectedItem(), attributeValues);
			selectedAttributeChanged(replacingAttributeNames.getSelectedItem(), replacingAttributeValues);
		}
		else {
			attributeValues.setModel(new DefaultComboBoxModel());
			attributeValues.setEditable(false);
			replacingAttributeValues.setModel(new DefaultComboBoxModel());
			replacingAttributeValues.setEditable(false);
		}
	}

	public void attributesChanged(final ChangeEvent e) {
		attributesChanged();
	}

	public void beforeMapChange(final MapModel oldMap, final MapModel newMap) {
	}

	private void selectedAttributeChanged(final Object selectedAttributeName, final JComboBox values) {
		final MapModel map = controller.getMap();
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
