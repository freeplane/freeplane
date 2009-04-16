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
package org.freeplane.core.filter;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.ButtonModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.extension.IExtension;
import org.freeplane.core.filter.condition.ConditionFactory;
import org.freeplane.core.filter.condition.DefaultConditionRenderer;
import org.freeplane.core.filter.condition.ICondition;
import org.freeplane.core.filter.condition.NoFilteringCondition;
import org.freeplane.core.filter.condition.SelectedViewCondition;
import org.freeplane.core.filter.condition.SelectedViewSnapshotCondition;
import org.freeplane.core.frame.IMapSelectionListener;
import org.freeplane.core.model.MapModel;
import org.freeplane.core.model.MindIcon;
import org.freeplane.core.resources.FpStringUtils;
import org.freeplane.core.resources.FreeplaneResourceBundle;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.components.FreeplaneToolBar;
import org.freeplane.n3.nanoxml.IXMLParser;
import org.freeplane.n3.nanoxml.IXMLReader;
import org.freeplane.n3.nanoxml.StdXMLReader;
import org.freeplane.n3.nanoxml.XMLElement;
import org.freeplane.n3.nanoxml.XMLParserFactory;
import org.freeplane.n3.nanoxml.XMLWriter;

/**
 * @author Dimitry Polivaev
 */
public class FilterController implements IMapSelectionListener, IExtension {
	private class FilterChangeListener extends AbstractAction implements ListDataListener {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		/*
		 * (non-Javadoc)
		 * @see
		 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent
		 * )
		 */
		public FilterChangeListener() {
		}

		public void actionPerformed(final ActionEvent arg0) {
			applyFilter(false);
		}

		public void contentsChanged(final ListDataEvent e) {
			if (e.getIndex0() == -1) {
				applyFilter(false);
			}
		}

		public void intervalAdded(final ListDataEvent e) {
		}

		public void intervalRemoved(final ListDataEvent e) {
		}
	}

	static final String FREEPLANE_FILTER_EXTENSION_WITHOUT_DOT = "mmfilter";
	private static final ICondition NO_FILTERING = NoFilteringCondition.createCondition();

	public static FilterController getController(final Controller controller) {
		return (FilterController) controller.getExtension(FilterController.class);
	}

	public static void install(final Controller controller) {
		controller.addExtension(FilterController.class, new FilterController(controller));
	}

	private final ButtonModel applyToVisibleNodeOnly;
	private ConditionFactory conditionFactory;
	private DefaultConditionRenderer conditionRenderer = null;
	private final Controller controller;
	final private FilterChangeListener filterChangeListener;
	private DefaultComboBoxModel filterConditions;
	private JToolBar filterToolbar;
	private final FilterHistory history;
	private Filter inactiveFilter;
	final private String pathToFilterFile;
	private ICondition selectedViewCondition;
	private final ButtonModel showAncestors;
	private final ButtonModel showDescendants;

	public FilterController(final Controller controller) {
		this.controller = controller;
		history = new FilterHistory(controller);
		controller.getMapViewManager().addMapSelectionListener(this);
		ShowFilterToolbarAction showFilterToolbar = new ShowFilterToolbarAction(this);
		controller.addAction(showFilterToolbar);
		UnfoldFilteredAncestorsAction unfoldFilteredAncestors = new UnfoldFilteredAncestorsAction(this);
		controller.addAction(unfoldFilteredAncestors);
		ApplyNoFilteringAction applyNoFiltering = new ApplyNoFilteringAction(this);
		controller.addAction(applyNoFiltering);
		ApplySelectedViewConditionAction applySelectedViewCondition = new ApplySelectedViewConditionAction(this);
		controller.addAction(applySelectedViewCondition);
		EditFilterAction editFilter = new EditFilterAction(this);
		controller.addAction(editFilter);
		UndoFilterAction undoFilter = new UndoFilterAction(this);
		controller.addAction(undoFilter);
		RedoFilterAction redoFilter = new RedoFilterAction(this);
		controller.addAction(redoFilter);
		ReapplyFilterAction reapplyFilter = new ReapplyFilterAction(this);
		controller.addAction(reapplyFilter);		
		ShowAncestorsAction showAncestorsAction = new ShowAncestorsAction(this);
		controller.addAction(showAncestorsAction);
		ShowDescendantsAction showDescendantsAction = new ShowDescendantsAction(this);
		controller.addAction(showDescendantsAction);
		
		filterChangeListener = new FilterChangeListener();
		showAncestors = new JToggleButton.ToggleButtonModel();
		showAncestors.setSelected(true);
		showAncestors.addActionListener(filterChangeListener);
		showDescendants = new JToggleButton.ToggleButtonModel();
		showDescendants.setSelected(false);
		showDescendants.addActionListener(filterChangeListener);
		applyToVisibleNodeOnly = new JToggleButton.ToggleButtonModel();
		applyToVisibleNodeOnly.setSelected(false);
		pathToFilterFile = ResourceController.getResourceController().getFreeplaneUserDirectory() + File.separator
		        + "auto." + FilterController.FREEPLANE_FILTER_EXTENSION_WITHOUT_DOT;
		
	}

	private void addStandardConditions() {
		final ICondition noFiltering = NO_FILTERING;
		filterConditions.insertElementAt(noFiltering, 0);
		if (selectedViewCondition == null) {
			selectedViewCondition = SelectedViewCondition.CreateCondition(controller);
		}
		filterConditions.insertElementAt(selectedViewCondition, 1);
		if (filterConditions.getSelectedItem() == null) {
			filterConditions.setSelectedItem(noFiltering);
		}
	}
	
	void applySelectedViewCondition(){
		if(getFilterConditions().getSelectedItem() != selectedViewCondition){
			getFilterConditions().setSelectedItem(selectedViewCondition);
		}
		else{
			applyFilter(true);
		}
	}

	/**
	 */
	public void afterMapChange(final MapModel oldMap, final MapModel newMap) {
		history.clear();
		if (newMap != null) {
			final Filter filter = newMap.getFilter();
			updateSettingsFromFilter(filter);
		}
		else {
			filterConditions.setSelectedItem(filterConditions.getElementAt(0));
		}
	}

	public void afterMapClose(final MapModel pOldMapView) {
	}

	void applyFilter(final boolean force) {
		final Filter filter = createFilter();
		filter.applyFilter(getController().getMap(), force);
		applyToVisibleNodeOnly.setSelected(filter.getCondition() != null);
		history.add(filter);
	}

	public void beforeMapChange(final MapModel oldMap, final MapModel newMap) {
	}

	private Filter createFilter() {
		final ICondition selectedCondition = getSelectedCondition();
		final ICondition filterCondition;
		if (selectedCondition == null || selectedCondition.equals(NO_FILTERING)) {
			filterCondition = null;
		}
		else if (selectedCondition.equals(selectedViewCondition)) {
			filterCondition = new SelectedViewSnapshotCondition(controller);
		}
		else {
			filterCondition = selectedCondition;
		}
		final Filter filter = new Filter(controller, filterCondition, showAncestors.isSelected(), showDescendants
		    .isSelected(), applyToVisibleNodeOnly.isSelected());
		return filter;
	}

	private JToolBar createFilterToolbar() {
		final JToolBar filterToolbar = new FreeplaneToolBar();
		filterToolbar.setVisible(false);
		filterToolbar.setFocusable(false);
		final JButton undoBtn = new JButton(controller.getAction("UndoFilterAction"));
//		undoBtn.setText(null);
		filterToolbar.add(undoBtn);
		final JButton redoBtn = new JButton(controller.getAction("RedoFilterAction"));
//		redoBtn.setText(null);
		filterToolbar.add(redoBtn);
		final JButton btnUnfoldAncestors = new JButton(controller.getAction("UnfoldFilteredAncestorsAction"));
		filterToolbar.add(btnUnfoldAncestors);
		final JCheckBox showAncestorsBox = new JCheckBox(FreeplaneResourceBundle.getText("ShowAncestorsAction.text"));
		showAncestorsBox.setModel(showAncestors);
		filterToolbar.add(showAncestorsBox);
		final JCheckBox showDescendantsBox = new JCheckBox(FreeplaneResourceBundle.getText("ShowDescendantsAction.text"));
		showDescendantsBox.setModel(showDescendants);
		filterToolbar.add(showDescendantsBox);
		final JCheckBox applyToVisibleBox = new JCheckBox(FreeplaneResourceBundle.getText("ApplyToVisibleAction.text"));
		applyToVisibleBox.setModel(applyToVisibleNodeOnly);
		filterToolbar.add(applyToVisibleBox);
		final JButton btnEdit = new JButton(controller.getAction("EditFilterAction"));
		filterToolbar.add(btnEdit);
		final JComboBox activeFilterConditionComboBox = new JComboBox(getFilterConditions()) {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public Dimension getMaximumSize() {
				return getPreferredSize();
			}
		};
		filterToolbar.add(activeFilterConditionComboBox);
		final JButton applyBtn = new JButton(controller.getAction("ReapplyFilterAction"));
		filterToolbar.add(applyBtn);
		activeFilterConditionComboBox.setFocusable(false);
		activeFilterConditionComboBox.setRenderer(this.getConditionRenderer());
		return filterToolbar;
	}

	public Filter createTransparentFilter() {
		if (inactiveFilter == null) {
			inactiveFilter = Filter.createTransparentFilter(controller);
		}
		return inactiveFilter;
	}

	protected ButtonModel getApplyToVisibleNodeOnly() {
		return applyToVisibleNodeOnly;
	}

	public ConditionFactory getConditionFactory() {
		if (conditionFactory == null) {
			conditionFactory = new ConditionFactory();
		}
		return conditionFactory;
	}

	DefaultConditionRenderer getConditionRenderer() {
		if (conditionRenderer == null) {
			conditionRenderer = new DefaultConditionRenderer();
		}
		return conditionRenderer;
	}

	public Controller getController() {
		return controller;
	}

	public DefaultComboBoxModel getFilterConditions() {
		if (filterConditions == null) {
			initConditions();
		}
		return filterConditions;
	}

	/**
	 */
	public JToolBar getFilterToolbar() {
		if (filterToolbar == null) {
			filterToolbar = createFilterToolbar();
			MindIcon.factory("AttributeExist", new ImageIcon(ResourceController.getResourceController().getResource(
			    "/images/showAttributes.gif")));
			MindIcon.factory("encrypted");
			MindIcon.factory("decrypted");
		}
		return filterToolbar;
	}

	public FilterHistory getHistory() {
		return history;
	}

	ICondition getSelectedCondition() {
		return (ICondition) getFilterConditions().getSelectedItem();
	}

	public ButtonModel getShowAncestors() {
		return showAncestors;
	}

	public ButtonModel getShowDescendants() {
		return showDescendants;
	}

	private void initConditions() {
		filterConditions = new DefaultComboBoxModel();
		try {
			loadConditions(filterConditions, pathToFilterFile);
		}
		catch (final Exception e) {
			e.printStackTrace();
		}
		addStandardConditions();
		filterConditions.setSelectedItem(filterConditions.getElementAt(0));
		filterConditions.addListDataListener(filterChangeListener);
	}

	public boolean isMapChangeAllowed(final MapModel oldMap, final MapModel newMap) {
		return true;
	}

	void loadConditions(final DefaultComboBoxModel filterConditionModel, final String pathToFilterFile)
	        throws IOException {
		filterConditionModel.removeAllElements();
		try {
			final IXMLParser parser = XMLParserFactory.createDefaultXMLParser();
			final IXMLReader reader = new StdXMLReader(new FileInputStream(pathToFilterFile));
			parser.setReader(reader);
			final XMLElement loader = (XMLElement) parser.parse();
			final Vector conditions = loader.getChildren();
			for (int i = 0; i < conditions.size(); i++) {
				filterConditionModel.addElement(getConditionFactory().loadCondition((XMLElement) conditions.get(i)));
			}
		}
		catch (final FileNotFoundException e) {
		}
		catch (final Exception e) {
			e.printStackTrace();
		}
	}

	public void saveConditions() {
		try {
			saveConditions(getFilterConditions(), pathToFilterFile);
		}
		catch (final Exception e) {
		}
	}

	void saveConditions(final DefaultComboBoxModel filterConditionModel, final String pathToFilterFile)
	        throws IOException {
		final XMLElement saver = new XMLElement();
		saver.setName("filter_conditions");
		final Writer writer = new FileWriter(pathToFilterFile);
		for (int i = 0; i < filterConditionModel.getSize(); i++) {
			final ICondition cond = (ICondition) filterConditionModel.getElementAt(i);
			cond.toXml(saver);
		}
		final XMLWriter xmlWriter = new XMLWriter(writer);
		xmlWriter.write(saver);
		writer.close();
	}

	public void setFilterConditions(final DefaultComboBoxModel newConditionModel) {
		filterConditions.removeListDataListener(filterChangeListener);
		filterConditions.removeAllElements();
		for (int i = 0; i < newConditionModel.getSize(); i++) {
			filterConditions.addElement(newConditionModel.getElementAt(i));
		}
		filterConditions.setSelectedItem(newConditionModel.getSelectedItem());
		addStandardConditions();
		filterConditions.addListDataListener(filterChangeListener);
		applyFilter(false);
	}

	/**
	 */
	public void showFilterToolbar(final boolean show) {
		if (show == getFilterToolbar().isVisible()) {
			return;
		}
		getFilterToolbar().setVisible(show);
		final MapModel map = getController().getMap();
		final Filter filter = map.getFilter();
		if (show) {
			filter.applyFilter(map, false);
		}
		else {
			createTransparentFilter().applyFilter(map, false);
		}
	}

	void updateSettingsFromHistory() {
		final Filter filter = history.getCurrentFilter();
		updateSettingsFromFilter(filter);
	}

	private void updateSettingsFromFilter(final Filter filter) {
	    filterConditions.removeListDataListener(filterChangeListener);
		showAncestors.removeActionListener(filterChangeListener);
		showDescendants.removeActionListener(filterChangeListener);
		filterConditions.setSelectedItem(filter.getCondition());
		showAncestors.setSelected(filter.areAncestorsShown());
		showDescendants.setSelected(filter.areDescendantsShown());
		applyToVisibleNodeOnly.setSelected(filter.appliesToVisibleNodesOnly());
		filterConditions.addListDataListener(filterChangeListener);
		showAncestors.addActionListener(filterChangeListener);
		showDescendants.addActionListener(filterChangeListener);
    }

	public void applyNoFiltering() {
	    getFilterConditions().setSelectedItem(NO_FILTERING);
    }
}
