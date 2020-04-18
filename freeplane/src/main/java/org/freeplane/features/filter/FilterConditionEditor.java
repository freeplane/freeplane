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
package org.freeplane.features.filter;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.lang.ref.WeakReference;
import java.util.Iterator;

import javax.swing.Box;
import javax.swing.ComboBoxEditor;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.ListCellRenderer;
import javax.swing.RootPaneContainer;
import javax.swing.text.JTextComponent;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.resources.TranslatedObject;
import org.freeplane.core.ui.FixedBasicComboBoxEditor;
import org.freeplane.core.ui.components.JComboBoxWithBorder;
import org.freeplane.core.ui.textchanger.TranslatedElementFactory;
import org.freeplane.core.util.collection.ExtendedComboBoxModel;
import org.freeplane.features.filter.condition.ASelectableCondition;
import org.freeplane.features.filter.condition.ConditionNotSatisfiedDecorator;
import org.freeplane.features.filter.condition.IElementaryConditionController;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.mode.Controller;

/**
 * @author Dimitry Polivaev
 * 23.05.2009
 */
public class FilterConditionEditor extends JComponent {
	private class ElementaryConditionChangeListener implements ItemListener {
		@Override
		public void itemStateChanged(final ItemEvent e) {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				setValuesEditor();
			}
		}
	}

	private class FilteredPropertyChangeListener implements ItemListener {
		@Override
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


	/**
	 * Start "Find next" action when pressing enter key in "value" combo box
	 */
	private void setValuesEnterKeyListener()
	{
		if (enterKeyActionListener != null)
		{
			values.getEditor().removeActionListener(enterKeyActionListener);
			values.getEditor().addActionListener(enterKeyActionListener);
		}
	}

	public void setSearchingBusyCursor()
	{
		RootPaneContainer root = (RootPaneContainer)getTopLevelAncestor();
		root.getGlassPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		root.getGlassPane().setVisible(true);
	}

	public void setSearchingDefaultCursor()
	{
		RootPaneContainer root = (RootPaneContainer)getTopLevelAncestor();
		root.getGlassPane().setCursor(Cursor.getDefaultCursor());
		root.getGlassPane().setVisible(false);
	}

	private void setValuesEditor() {
		final Object selectedProperty = filteredPropertiesComponent.getSelectedItem();
		final IElementaryConditionController conditionController = filterController.getConditionFactory()
		    .getConditionController(selectedProperty);
		final TranslatedObject selectedCondition = (TranslatedObject) elementaryConditions.getSelectedItem();
		final boolean canSelectValues = conditionController
		    .canSelectValues(selectedProperty, selectedCondition);
		values.setEnabled(canSelectValues);
		values.setEditable(false);
		values.setModel(conditionController.getValuesForProperty(selectedProperty, selectedCondition));

		final ComboBoxEditor valueEditor = conditionController.getValueEditor(selectedProperty, selectedCondition);
		values.setEditor(valueEditor != null ? valueEditor : new FixedBasicComboBoxEditor());
		setValuesEnterKeyListener();

		final ListCellRenderer valueRenderer = conditionController.getValueRenderer(selectedProperty, selectedCondition);
		values.setRenderer(valueRenderer != null ? valueRenderer : filterController.getConditionRenderer());

		values.setEditable(conditionController.canEditValues(selectedProperty, selectedCondition));
		if (values.getModel().getSize() > 0) {
			values.setSelectedIndex(0);
		}
		caseSensitive.setEnabled(canSelectValues
		        && conditionController.isCaseDependent(selectedProperty, selectedCondition));
        approximateMatching.setEnabled(canSelectValues
                && conditionController.supportsApproximateMatching(selectedProperty, selectedCondition));
        ignoreDiacritics.setEnabled(canSelectValues
                && conditionController.supportsApproximateMatching(selectedProperty, selectedCondition));
	}
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private static final String PROPERTY_FILTER_MATCH_CASE = "filter_match_case";
	private static final String PROPERTY_FILTER_APPROXIMATE_MATCH = "filter_match_approximately";
	private static final String PROPERTY_FILTER_IGNORE_DIACRITICS = "filter_ignore_diacritics";
	final private JToggleButton caseSensitive;
	final private JToggleButton approximateMatching;
	final private JToggleButton ignoreDiacritics;
	final private JComboBox elementaryConditions;
	final private FilterController filterController;
	final private JComboBox filteredPropertiesComponent;
	final private ExtendedComboBoxModel filteredPropertiesModel;
	private WeakReference<Filter> lastFilter;
	final private JComboBox values;
	private ActionListener enterKeyActionListener;
	final private JCheckBox btnDeny;
	public FilterConditionEditor(final FilterController filterController) {
		this(filterController, 5, false);
	}
	public FilterConditionEditor(final FilterController filterController, final int borderWidth, final boolean horizontal) {
		super();
		setLayout(new GridBagLayout());
		final GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.anchor = GridBagConstraints.NORTH;
		gridBagConstraints.insets = new Insets(0, borderWidth, 0, borderWidth);
		this.filterController = filterController;
		//Basic layout
		//Item to search for

		add(Box.createHorizontalGlue(), gridBagConstraints);
		gridBagConstraints.gridx++;

		btnDeny = TranslatedElementFactory.createCheckBox("filter_deny");
		add(btnDeny, gridBagConstraints);
		gridBagConstraints.gridx++;

		filteredPropertiesComponent = new JComboBoxWithBorder();
		filteredPropertiesModel = new ExtendedComboBoxModel();
		filteredPropertiesComponent.setModel(filteredPropertiesModel);
		filteredPropertiesComponent.addItemListener(new FilteredPropertyChangeListener());
		filteredPropertiesComponent.setAlignmentY(Component.TOP_ALIGNMENT);
		filteredPropertiesComponent.setRenderer(filterController.getConditionRenderer());
		add(filteredPropertiesComponent, gridBagConstraints);
		gridBagConstraints.gridx++;

		//Search condition
		elementaryConditions = new JComboBoxWithBorder();
		elementaryConditions.addItemListener(new ElementaryConditionChangeListener());
		elementaryConditions.setAlignmentY(Component.TOP_ALIGNMENT);
		add(elementaryConditions, gridBagConstraints);
		gridBagConstraints.gridx++;
		elementaryConditions.setRenderer(filterController.getConditionRenderer());
		//Search value
		values = new JComboBoxWithBorder();
		values.setPreferredSize(new Dimension(240,20));
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		add(values, gridBagConstraints);
		if(horizontal){
			gridBagConstraints.gridx++;
		}
		else{
			gridBagConstraints.gridy++;
		}
		values.setEditable(true);
		setValuesEnterKeyListener();

		JPanel ignoreCaseAndApproximateMatchingPanel = new JPanel();
		ignoreCaseAndApproximateMatchingPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));

		// Ignore case checkbox
		caseSensitive = TranslatedElementFactory.createToggleButtonWithIcon(PROPERTY_FILTER_MATCH_CASE + ".icon", PROPERTY_FILTER_MATCH_CASE + ".tooltip");
		caseSensitive.setModel(filterController.getCaseSensitiveButtonModel());
		ignoreCaseAndApproximateMatchingPanel.add(caseSensitive);
		caseSensitive.setSelected(ResourceController.getResourceController().getBooleanProperty(
		    PROPERTY_FILTER_MATCH_CASE));

		// add approximate matching checkbox
		approximateMatching = TranslatedElementFactory.createToggleButtonWithIcon(PROPERTY_FILTER_APPROXIMATE_MATCH + ".icon", PROPERTY_FILTER_APPROXIMATE_MATCH+ ".tooltip");
		approximateMatching.setModel(filterController.getApproximateMatchingButtonModel());
		//add(approximateMatching, gridBagConstraints);
		ignoreCaseAndApproximateMatchingPanel.add(approximateMatching);
		approximateMatching.setSelected(ResourceController.getResourceController().getBooleanProperty(
			    PROPERTY_FILTER_APPROXIMATE_MATCH));
		
		
		ignoreDiacritics = TranslatedElementFactory.createToggleButtonWithIcon(PROPERTY_FILTER_IGNORE_DIACRITICS + ".icon", PROPERTY_FILTER_IGNORE_DIACRITICS+ ".tooltip");
		ignoreDiacritics.setModel(filterController.getIgnoreDiacriticsButtonModel());
        //add(approximateMatching, gridBagConstraints);
        ignoreCaseAndApproximateMatchingPanel.add(ignoreDiacritics);
        ignoreDiacritics.setSelected(ResourceController.getResourceController().getBooleanProperty(
                PROPERTY_FILTER_IGNORE_DIACRITICS));
		filterChanged(null);

		add(ignoreCaseAndApproximateMatchingPanel, gridBagConstraints);

	}

	public void focusInputField(final boolean selectAll) {
		if (values.isEnabled()) {
			values.requestFocus();
			final Component editorComponent = values.getEditor().getEditorComponent();
			if (selectAll && editorComponent instanceof JTextComponent) {
				((JTextComponent) editorComponent).selectAll();
			}
			return;
		}
	}

	public boolean isInputFieldFocused(){
		if (values.isFocusOwner())
			return true;
		if (values.isPopupVisible() || values.getEditor().getEditorComponent().isFocusOwner())
			return true;
		return false;
	}

	public ASelectableCondition getCondition() {
		ASelectableCondition newCond;
		Object value;
		if(values.isEditable()){
			value = values.getEditor().getItem();
		}
		else{
			value = values.getSelectedItem();
		}
		if (value == null) {
			value = "";
		}
		final TranslatedObject simpleCond = (TranslatedObject) elementaryConditions.getSelectedItem();
		final boolean matchCase = caseSensitive.isSelected();
		final boolean matchApproximately = approximateMatching.isSelected();
		ResourceController.getResourceController().setProperty(PROPERTY_FILTER_MATCH_CASE, matchCase);
		final Object selectedItem = filteredPropertiesComponent.getSelectedItem();
		newCond = filterController.getConditionFactory().createCondition(selectedItem, simpleCond, value, matchCase, matchApproximately,
		        ignoreDiacritics.isSelected());
		if (values.isEditable()) {
			if (!value.equals("")) {
				DefaultComboBoxModel list = (DefaultComboBoxModel) values.getModel();
				int indexOfValue = list.getIndexOf(value);
				if(indexOfValue > 0)
					list.removeElementAt(indexOfValue);
				if(indexOfValue == -1 || list.getIndexOf(value) != indexOfValue){
					values.insertItemAt(value, 0);
					values.setSelectedIndex(0);
				}
				else if(indexOfValue != -1){
					values.setSelectedIndex(indexOfValue);
				}
				if (values.getItemCount() >= 10) {
					values.removeItemAt(9);
				}
			}
		}
		if (newCond != null && btnDeny.isSelected())
			return new ConditionNotSatisfiedDecorator(newCond);
		else
			return newCond;
	}

	/**
	 */
	public void filterChanged(final Filter newFilter) {
		if (newFilter != null) {
			if (lastFilter != null && lastFilter.get() == newFilter) {
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
			filteredPropertiesComponent.setSelectedIndex(-1);
			filteredPropertiesModel.setExtensionList(null);
		}
		lastFilter = new WeakReference<>(newFilter);
	}

	public void setEnterKeyActionListener(ActionListener enterKeyActionListener) {
		if (enterKeyActionListener == null)
		{
			throw new NullPointerException("null value in setEnterKeyActionListener()!");
		}
		if (this.enterKeyActionListener != null)
		{
			values.getEditor().removeActionListener(this.enterKeyActionListener);
		}
		this.enterKeyActionListener = enterKeyActionListener;
		values.getEditor().addActionListener(enterKeyActionListener);
	}

	@Override
    public void setEnabled(boolean enabled) {
	    super.setEnabled(enabled);
	    for(int i = 0; i < getComponentCount(); i++){
	    	Component c = getComponent(i);
	    	c.setEnabled(enabled);
	    	if (c instanceof JComboBox)
	    		((JComboBox)c).getEditor().getEditorComponent().setEnabled(enabled);
	    }
    }



}
