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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.lang.ref.WeakReference;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ComboBoxEditor;
import javax.swing.ComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import javax.swing.text.JTextComponent;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.resources.NamedObject;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.core.util.TextUtils;
import org.freeplane.core.util.collection.ExtendedComboBoxModel;
import org.freeplane.features.common.filter.condition.ASelectableCondition;
import org.freeplane.features.common.filter.condition.IElementaryConditionController;
import org.freeplane.features.common.map.MapModel;

/**
 * @author Dimitry Polivaev
 * 23.05.2009
 */
public class FilterConditionEditor extends JComponent {
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
		caseSensitive.setEnabled(canSelectValues
		        && conditionController.isCaseDependent(selectedProperty, selectedCondition));
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final String PROPERTY_FILTER_MATCH_CASE = "filter_match_case";
	final private JCheckBox caseSensitive;
	final private JComboBox elementaryConditions;
	final private FilterController filterController;
	final private JComboBox filteredPropertiesComponent;
	final private ExtendedComboBoxModel filteredPropertiesModel;
	private WeakReference<MapModel> lastMap;
	final private JComboBox values;

	public FilterConditionEditor(final FilterController filterController) {
		super();
		setLayout(new GridBagLayout());
		final GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.anchor = GridBagConstraints.NORTH;
		final int borderWidth = 5;
		gridBagConstraints.insets = new Insets(0, borderWidth, 0, borderWidth);
		this.filterController = filterController;
		//Basic layout
		setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(), BorderFactory.createEmptyBorder(borderWidth, 0, borderWidth, 0)));
		//Item to search for
		filteredPropertiesComponent = new JComboBox();
		filteredPropertiesModel = new ExtendedComboBoxModel();
		filteredPropertiesComponent.setModel(filteredPropertiesModel);
		filteredPropertiesComponent.addItemListener(new FilteredPropertyChangeListener());
		add(Box.createHorizontalGlue(), gridBagConstraints);
		gridBagConstraints.gridx++;
		filteredPropertiesComponent.setAlignmentY(Component.TOP_ALIGNMENT);
		add(filteredPropertiesComponent, gridBagConstraints);
		gridBagConstraints.gridx++;
		filteredPropertiesComponent.setRenderer(filterController.getConditionRenderer());
		//Search condition
		elementaryConditions = new JComboBox();
		elementaryConditions.addItemListener(new ElementaryConditionChangeListener());
		elementaryConditions.setAlignmentY(Component.TOP_ALIGNMENT);
		add(elementaryConditions, gridBagConstraints);
		gridBagConstraints.gridx++;
		elementaryConditions.setRenderer(filterController.getConditionRenderer());
		//Search value
		values = new JComboBox();
		values.setPreferredSize(new Dimension(280,20));
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		add(values, gridBagConstraints);
		gridBagConstraints.gridy++;
		values.setRenderer(filterController.getConditionRenderer());
		values.setEditable(true);
		// Ignore case checkbox
		caseSensitive = new JCheckBox();
		add(caseSensitive, gridBagConstraints);
		gridBagConstraints.gridx++;
		MenuBuilder.setLabelAndMnemonic(caseSensitive,TextUtils.getText(PROPERTY_FILTER_MATCH_CASE));
		caseSensitive.setSelected(ResourceController.getResourceController().getBooleanProperty(
		    PROPERTY_FILTER_MATCH_CASE));
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
		final boolean matchCase = caseSensitive.isSelected();
		ResourceController.getResourceController().setProperty(PROPERTY_FILTER_MATCH_CASE, matchCase);
		final Object selectedItem = filteredPropertiesComponent.getSelectedItem();
		newCond = filterController.getConditionFactory().createCondition(selectedItem, simpleCond, value, matchCase);
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
