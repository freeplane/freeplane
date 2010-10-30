/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2009 Dimitry
 *
 *  This file author is Dimitry
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
package org.freeplane.features.common.filter;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.lang.ref.WeakReference;
import java.util.Iterator;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ComboBoxEditor;
import javax.swing.ComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import javax.swing.text.JTextComponent;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.resources.NamedObject;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.core.util.TextUtils;
import org.freeplane.core.util.collection.ExtendedComboBoxModel;
import org.freeplane.features.common.filter.condition.IElementaryConditionController;
import org.freeplane.features.common.filter.condition.ASelectableCondition;
import org.freeplane.features.common.map.MapModel;

/**
 * @author Dimitry Polivaev
 * 23.05.2009
 */
public class FilterConditionEditor extends Box {
	private class ElementaryConditionChangeListener implements ItemListener {
		public void itemStateChanged(final ItemEvent e) {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				setValuesEditor();
			}
		}
	}

	private class FilteredPropertyChangeListener implements ItemListener {
		public void itemStateChanged(final ItemEvent e) {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				final Object selectedProperty = filteredPropertiesComponent.getSelectedItem();
				final IElementaryConditionController conditionController = filterController.getConditionFactory()
				.getConditionController(selectedProperty);
				final ComboBoxModel simpleConditionComboBoxModel = conditionController
				.getConditionsForProperty(selectedProperty);
				elementaryConditions.setModel(simpleConditionComboBoxModel);
				elementaryConditions.setEnabled(simpleConditionComboBoxModel.getSize() > 0);
				setValuesEditor();
				return;
			}
		}
	}

	private void setValuesEditor() {
		final Object selectedProperty = filteredPropertiesComponent.getSelectedItem();
		final IElementaryConditionController conditionController = filterController.getConditionFactory()
		    .getConditionController(selectedProperty);
		final NamedObject selectedCondition = (NamedObject) elementaryConditions.getSelectedItem();
		final boolean canSelectValues = conditionController
		    .canSelectValues(selectedProperty, selectedCondition);
		values.setEnabled(canSelectValues);
		values.setEditable(false);
		values.setModel(conditionController.getValuesForProperty(selectedProperty, selectedCondition));
		final ComboBoxEditor valueEditor = conditionController.getValueEditor(selectedProperty, selectedCondition);
		values.setEditor(valueEditor != null ? valueEditor : new BasicComboBoxEditor());
		values.setEditable(conditionController.canEditValues(selectedProperty, selectedCondition));
		if (values.getModel().getSize() > 0) {
			values.setSelectedIndex(0);
		}
		caseInsensitive.setEnabled(canSelectValues
		        && conditionController.isCaseDependent(selectedProperty, selectedCondition));
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final String PROPERTY_FILTER_IGNORE_CASE = "filter_ignore_case";
	final private JCheckBox caseInsensitive;
	final private JComboBox elementaryConditions;
	final private FilterController filterController;
	final private JComboBox filteredPropertiesComponent;
	final private ExtendedComboBoxModel filteredPropertiesModel;
	private WeakReference<MapModel> lastMap;
	final private JComboBox values;

	public FilterConditionEditor(final FilterController filterController) {
		super(BoxLayout.PAGE_AXIS);
		this.filterController = filterController;
		setBorder(new EtchedBorder());
		Box top = new Box(BoxLayout.LINE_AXIS);
		Box middle = new Box(BoxLayout.LINE_AXIS);
		Box bottom = new Box(BoxLayout.LINE_AXIS);
		this.add(Box.createRigidArea(new Dimension(15,15)));
		this.add(top);
		this.add(middle);
		this.add(bottom);
		this.add(Box.createRigidArea(new Dimension(15,15)));
		// Ignore case checkbox
		caseInsensitive = new JCheckBox();
		caseInsensitive.setAlignmentX(Component.LEFT_ALIGNMENT);
		top.add(Box.createRigidArea(new Dimension(15,15)));
		top.add(caseInsensitive);
		top.add(Box.createHorizontalGlue());
		MenuBuilder.setLabelAndMnemonic(caseInsensitive,TextUtils.getText(PROPERTY_FILTER_IGNORE_CASE));
		caseInsensitive.setSelected(ResourceController.getResourceController().getBooleanProperty(
		    PROPERTY_FILTER_IGNORE_CASE));
		middle.add(Box.createRigidArea(new Dimension(15,15)));		
		//Item to search for
		filteredPropertiesComponent = new JComboBox();
		filteredPropertiesModel = new ExtendedComboBoxModel();
		filteredPropertiesComponent.setModel(filteredPropertiesModel);
		filteredPropertiesComponent.addItemListener(new FilteredPropertyChangeListener());
		bottom.add(Box.createRigidArea(new Dimension(15,15)));
		bottom.add(Box.createHorizontalGlue());
		bottom.add(filteredPropertiesComponent);
		bottom.add(Box.createRigidArea(new Dimension(10,5)));
		filteredPropertiesComponent.setRenderer(filterController.getConditionRenderer());
		//Search condition
		elementaryConditions = new JComboBox();
		elementaryConditions.addItemListener(new ElementaryConditionChangeListener());
		//bottom.add(Box.createHorizontalGlue());
		bottom.add(elementaryConditions);
		bottom.add(Box.createRigidArea(new Dimension(10,5)));
		elementaryConditions.setRenderer(filterController.getConditionRenderer());
		//Search value
		values = new JComboBox();
		values.setPreferredSize(new Dimension(300,20));
		bottom.add(values);
		bottom.add(Box.createRigidArea(new Dimension(15,15)));
		values.setRenderer(filterController.getConditionRenderer());
		values.setEditable(true);
		mapChanged(Controller.getCurrentController().getMap());
	}

	public void focusInputField() {
		if (values.isEnabled()) {
			values.requestFocus();
			final Component editorComponent = values.getEditor().getEditorComponent();
			if (editorComponent instanceof JTextComponent) {
				((JTextComponent) editorComponent).selectAll();
			}
			return;
		}
	}

	public ASelectableCondition getCondition() {
		ASelectableCondition newCond;
		Object value = values.getSelectedItem();
		if (value == null) {
			value = "";
		}
		final NamedObject simpleCond = (NamedObject) elementaryConditions.getSelectedItem();
		final boolean ignoreCase = caseInsensitive.isSelected();
		ResourceController.getResourceController().setProperty(PROPERTY_FILTER_IGNORE_CASE, ignoreCase);
		final Object selectedItem = filteredPropertiesComponent.getSelectedItem();
		newCond = filterController.getConditionFactory().createCondition(selectedItem, simpleCond, value, ignoreCase);
		if (values.isEditable()) {
			final Object item = values.getSelectedItem();
			if (item != null && !item.equals("")) {
				values.removeItem(item);
				values.insertItemAt(item, 0);
				values.setSelectedIndex(0);
				if (values.getItemCount() >= 10) {
					values.removeItemAt(9);
				}
			}
		}
		return newCond;
	}

	/**
	 */
	public void mapChanged(final MapModel newMap) {
		if (newMap != null) {
			if (lastMap != null && lastMap.get() == newMap) {
				return;
			}
			filteredPropertiesModel.removeAllElements();
			final Iterator<IElementaryConditionController> conditionIterator = filterController.getConditionFactory()
			    .conditionIterator();
			while (conditionIterator.hasNext()) {
				final IElementaryConditionController next = conditionIterator.next();
				filteredPropertiesModel.addExtensionList(next.getFilteredProperties());
				filteredPropertiesModel.setSelectedItem(filteredPropertiesModel.getElementAt(0));
			}
		}
		else {
			values.setSelectedIndex(-1);
			filteredPropertiesComponent.setSelectedIndex(0);
			filteredPropertiesModel.setExtensionList(null);
		}
		lastMap = new WeakReference<MapModel>(newMap);
	}

}
