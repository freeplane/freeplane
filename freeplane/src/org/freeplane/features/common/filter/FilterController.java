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
package org.freeplane.features.common.filter;

import java.awt.Dimension;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.security.AccessControlException;
import java.util.Vector;

import javax.swing.ButtonModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.extension.IExtension;
import org.freeplane.core.frame.IMapSelectionListener;
import org.freeplane.core.frame.ToggleToolbarAction;
import org.freeplane.core.frame.ViewController;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.components.FreeplaneToolBar;
import org.freeplane.core.ui.components.JAutoToggleButton;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.common.filter.condition.ConditionFactory;
import org.freeplane.features.common.filter.condition.DefaultConditionRenderer;
import org.freeplane.features.common.filter.condition.ISelectableCondition;
import org.freeplane.features.common.filter.condition.NoFilteringCondition;
import org.freeplane.features.common.filter.condition.SelectedViewCondition;
import org.freeplane.features.common.filter.condition.SelectedViewSnapshotCondition;
import org.freeplane.features.common.map.MapModel;
import org.freeplane.features.common.map.ModeController;
import org.freeplane.features.common.map.NodeModel;
import org.freeplane.features.common.text.TextController.Direction;
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
	private class FilterChangeListener implements ListDataListener, ChangeListener {
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

		public void contentsChanged(final ListDataEvent e) {
			if (e.getIndex0() == -1) {
				applyFilter(false);
			}
		}

		public void intervalAdded(final ListDataEvent e) {
		}

		public void intervalRemoved(final ListDataEvent e) {
		}

		public void stateChanged(final ChangeEvent e) {
			applyFilter(false);
		}
	}

	static final String FREEPLANE_FILTER_EXTENSION_WITHOUT_DOT = "mmfilter";
	private static final ISelectableCondition NO_FILTERING = NoFilteringCondition.createCondition();

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
	private ISelectableCondition selectedViewCondition;
	private final ButtonModel showAncestors;
	private final ButtonModel showDescendants;
	private final ButtonModel unfold;
	private JComboBox activeFilterConditionComboBox;

	public FilterController(final Controller controller) {
		this.controller = controller;
		history = new FilterHistory(controller);
		filterChangeListener = new FilterChangeListener();
		showAncestors = new JToggleButton.ToggleButtonModel();
		showAncestors.setSelected(true);
		showAncestors.addChangeListener(filterChangeListener);
		showDescendants = new JToggleButton.ToggleButtonModel();
		showDescendants.setSelected(false);
		showDescendants.addChangeListener(filterChangeListener);
		applyToVisibleNodeOnly = new JToggleButton.ToggleButtonModel();
		applyToVisibleNodeOnly.setSelected(false);
		unfold = new JToggleButton.ToggleButtonModel();
		unfold.setSelected(true);
		controller.getMapViewManager().addMapSelectionListener(this);
		final AFreeplaneAction showFilterToolbar = new ToggleToolbarAction(controller, "ShowFilterToolbarAction",
		    "/filter_toolbar");
		controller.addAction(showFilterToolbar);
		final UnfoldFilteredAncestorsAction unfoldFilteredAncestors = new UnfoldFilteredAncestorsAction(this);
		controller.addAction(unfoldFilteredAncestors);
		final ApplyNoFilteringAction applyNoFiltering = new ApplyNoFilteringAction(this);
		controller.addAction(applyNoFiltering);
		final ApplySelectedViewConditionAction applySelectedViewCondition = new ApplySelectedViewConditionAction(this);
		controller.addAction(applySelectedViewCondition);
		final EditFilterAction editFilter = new EditFilterAction(this);
		controller.addAction(editFilter);
		final UndoFilterAction undoFilter = new UndoFilterAction(this);
		controller.addAction(undoFilter);
		final RedoFilterAction redoFilter = new RedoFilterAction(this);
		controller.addAction(redoFilter);
		final ReapplyFilterAction reapplyFilter = new ReapplyFilterAction(this);
		controller.addAction(reapplyFilter);
		final ShowAncestorsAction showAncestorsAction = new ShowAncestorsAction(this);
		controller.addAction(showAncestorsAction);
		final ShowDescendantsAction showDescendantsAction = new ShowDescendantsAction(this);
		controller.addAction(showDescendantsAction);
		final ApplyToVisibleAction applyToVisibleAction = new ApplyToVisibleAction(this);
		controller.addAction(applyToVisibleAction);
		pathToFilterFile = ResourceController.getResourceController().getFreeplaneUserDirectory() + File.separator
		        + "auto." + FilterController.FREEPLANE_FILTER_EXTENSION_WITHOUT_DOT;
		final FindAction find = new FindAction(controller);
		controller.addAction(find);
		controller.addAction(new FindNextAction(controller, find));
	}

	private void addStandardConditions() {
		final ISelectableCondition noFiltering = NO_FILTERING;
		filterConditions.insertElementAt(noFiltering, 0);
		if (selectedViewCondition == null) {
			selectedViewCondition = SelectedViewCondition.CreateCondition(controller);
		}
		filterConditions.insertElementAt(selectedViewCondition, 1);
		if (filterConditions.getSelectedItem() == null) {
			filterConditions.setSelectedItem(noFiltering);
		}
	}

	/**
	 */
	public void afterMapChange(final MapModel oldMap, final MapModel newMap) {
		if(filterToolbar == null){
			return;
		}
		history.clear();
		if (newMap != null) {
			filterToolbar.setEnabled(true);
			activeFilterConditionComboBox.setEnabled(true);
			final Filter filter = newMap.getFilter();
			updateSettingsFromFilter(filter);
		}
		else {
			filterConditions.setSelectedItem(filterConditions.getElementAt(0));
			filterToolbar.setEnabled(false);
			activeFilterConditionComboBox.setEnabled(false);
		}
	}

	public void afterMapClose(final MapModel pOldMapView) {
	}

	void applyFilter(final boolean force) {
		final Filter filter = createFilter();
		filter.applyFilter(getController().getModeController(), getController().getMap(), force);
		final boolean isActive = filter.getCondition() != null;
		applyToVisibleNodeOnly.setSelected(isActive);
		history.add(filter);
	}

	public void applyNoFiltering() {
		getFilterConditions().setSelectedItem(NO_FILTERING);
	}

	void applySelectedViewCondition() {
		if (getFilterConditions().getSelectedItem() != selectedViewCondition) {
			getFilterConditions().setSelectedItem(selectedViewCondition);
		}
		else {
			applyFilter(true);
		}
	}

	public void beforeMapChange(final MapModel oldMap, final MapModel newMap) {
	}

	private Filter createFilter() {
		final ISelectableCondition selectedCondition = getSelectedCondition();
		final ISelectableCondition filterCondition;
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
		    .isSelected(), applyToVisibleNodeOnly.isSelected(), unfold.isSelected());
		return filter;
	}

	private JToolBar createFilterToolbar() {
		final JToolBar filterToolbar = new FreeplaneToolBar("filter_toolbar", SwingConstants.HORIZONTAL);
		filterToolbar.setVisible(ResourceController.getResourceController()
		    .getBooleanProperty("filter_toolbar_visible"));
		filterToolbar.putClientProperty(ViewController.VISIBLE_PROPERTY_KEY, "filter_toolbar_visible");
		filterToolbar.setFocusable(false);
		final JButton undoBtn = new JButton(controller.getAction("UndoFilterAction"));
		final JButton redoBtn = new JButton(controller.getAction("RedoFilterAction"));
		final JToggleButton btnUnfoldAncestors = new JAutoToggleButton(controller
		    .getAction("UnfoldFilteredAncestorsAction"), unfold);
		btnUnfoldAncestors.setSelected(unfold.isSelected());
		final JToggleButton showAncestorsBox = new JAutoToggleButton(controller.getAction("ShowAncestorsAction"),
		    showAncestors);
		showAncestorsBox.setSelected(showAncestors.isSelected());
		final JToggleButton showDescendantsBox = new JAutoToggleButton(controller.getAction("ShowDescendantsAction"),
		    showDescendants);
		final JToggleButton applyToVisibleBox = new JAutoToggleButton(controller.getAction("ApplyToVisibleAction"),
		    applyToVisibleNodeOnly);
		final JButton btnEdit = new JButton(controller.getAction("EditFilterAction"));
		activeFilterConditionComboBox = new JComboBox(getFilterConditions()) {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public Dimension getMaximumSize() {
				return getPreferredSize();
			}
		};
		final JButton applyBtn = new JButton(controller.getAction("ReapplyFilterAction"));
		final JButton noFilteringBtn = new JButton(controller.getAction("ApplyNoFilteringAction"));
		filterToolbar.addSeparator();
		filterToolbar.add(undoBtn);
		filterToolbar.add(redoBtn);
		filterToolbar.add(showAncestorsBox);
		filterToolbar.add(showDescendantsBox);
		filterToolbar.add(applyToVisibleBox);
		filterToolbar.add(btnUnfoldAncestors);
		filterToolbar.add(activeFilterConditionComboBox);
		filterToolbar.add(applyBtn);
		filterToolbar.add(noFilteringBtn);
		filterToolbar.add(btnEdit);
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

	protected ButtonModel getUnfoldInvisibleAncestors() {
		return unfold;
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
		}
		return filterToolbar;
	}

	public FilterHistory getHistory() {
		return history;
	}

	ISelectableCondition getSelectedCondition() {
		return (ISelectableCondition) getFilterConditions().getSelectedItem();
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
			LogUtils.severe(e);
		}
		addStandardConditions();
		filterConditions.setSelectedItem(filterConditions.getElementAt(0));
		filterConditions.addListDataListener(filterChangeListener);
	}

	@SuppressWarnings("unchecked")
	void loadConditions(final DefaultComboBoxModel filterConditionModel, final String pathToFilterFile)
	        throws IOException {
		filterConditionModel.removeAllElements();
		try {
			final IXMLParser parser = XMLParserFactory.createDefaultXMLParser();
			final IXMLReader reader = new StdXMLReader(new BufferedInputStream(new FileInputStream(pathToFilterFile)));
			parser.setReader(reader);
			final XMLElement loader = (XMLElement) parser.parse();
			final Vector<XMLElement> conditions = loader.getChildren();
			for (int i = 0; i < conditions.size(); i++) {
				filterConditionModel.addElement(getConditionFactory().loadCondition(conditions.get(i)));
			}
		}
		catch (final FileNotFoundException e) {
		}
		catch (final AccessControlException e) {
		}
		catch (final Exception e) {
			LogUtils.warn(e);
			UITools.errorMessage(TextUtils.getText("filters_not_loaded"));
		}
	}

	public void saveConditions() {
		try {
			saveConditions(getFilterConditions(), pathToFilterFile);
		}
		catch (final Exception e) {
			LogUtils.warn(e);
		}
	}

	void saveConditions(final DefaultComboBoxModel filterConditionModel, final String pathToFilterFile)
	        throws IOException {
		final XMLElement saver = new XMLElement();
		saver.setName("filter_conditions");
		final Writer writer = new FileWriter(pathToFilterFile);
		for (int i = 0; i < filterConditionModel.getSize(); i++) {
			final ISelectableCondition cond = (ISelectableCondition) filterConditionModel.getElementAt(i);
			if (cond != null && !(cond instanceof NoFilteringCondition)) {
				cond.toXml(saver);
			}
		}
		final XMLWriter xmlWriter = new XMLWriter(writer);
		xmlWriter.write(saver, true);
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

	private void updateSettingsFromFilter(final Filter filter) {
		getFilterConditions().removeListDataListener(filterChangeListener);
		showAncestors.removeChangeListener(filterChangeListener);
		showDescendants.removeChangeListener(filterChangeListener);
		filterConditions.setSelectedItem(filter.getCondition());
		showAncestors.setSelected(filter.areAncestorsShown());
		showDescendants.setSelected(filter.areDescendantsShown());
		applyToVisibleNodeOnly.setSelected(filter.appliesToVisibleNodesOnly());
		if (filter.getCondition() != null) {
			unfold.setSelected(filter.unfoldsInvisibleNodes());
		}
		filterConditions.addListDataListener(filterChangeListener);
		showAncestors.addChangeListener(filterChangeListener);
		showDescendants.addChangeListener(filterChangeListener);
	}

	void updateSettingsFromHistory() {
		final Filter filter = history.getCurrentFilter();
		updateSettingsFromFilter(filter);
		final boolean isActive = filter.getCondition() != null;
		applyToVisibleNodeOnly.setSelected(isActive);
	}

	NodeModel findNext(final NodeModel from, final NodeModel end, final Direction direction,
	                   final ISelectableCondition condition) {
		NodeModel next = from;
		for (;;) {
			do {
				switch (direction) {
					case FORWARD:
					case FORWARD_N_FOLD:
						next = getNext(direction, next, end);
						break;
					case BACK:
					case BACK_N_FOLD:
						next = getPrevious(direction, next, end);
						break;
				}
				if (next == null) {
					return null;
				}
			} while (!next.isVisible());
			if (next == from) {
				break;
			}
			if (condition == null || condition.checkNode(getModeController(), next)) {
				return next;
			}
		}
		return null;
	}

	private NodeModel getNext(final Direction direction, NodeModel current, final NodeModel end) {
		if (current.getChildCount() != 0) {
			final NodeModel next = (NodeModel) current.getChildAt(0);
			if (next.equals(end)) {
				return null;
			}
			return next;
		}
		for (;;) {
			final NodeModel parentNode = current.getParentNode();
			if (parentNode == null) {
				return current;
			}
			final int index = parentNode.getIndex(current) + 1;
			final int childCount = parentNode.getChildCount();
			if (index < childCount) {
				if (direction == Direction.FORWARD_N_FOLD) {
					getModeController().getMapController().setFolded(current, true);
				}
				final NodeModel next = (NodeModel) parentNode.getChildAt(index);
				if (next.equals(end)) {
					return null;
				}
				return next;
			}
			current = parentNode;
			if (current.equals(end)) {
				return null;
			}
		}
	}

	private NodeModel getPrevious(final Direction direction, NodeModel current, final NodeModel end) {
		for (;;) {
			final NodeModel parentNode = current.getParentNode();
			if (parentNode == null) {
				break;
			}
			if (direction == Direction.BACK_N_FOLD) {
				getModeController().getMapController().setFolded(current, true);
			}
			final int index = parentNode.getIndex(current) - 1;
			if (index < 0) {
				if (direction == Direction.BACK_N_FOLD) {
					getModeController().getMapController().setFolded(parentNode, true);
				}
				if (parentNode.equals(end)) {
					return null;
				}
				return parentNode;
			}
			current = (NodeModel) parentNode.getChildAt(index);
			if (current.equals(end)) {
				return null;
			}
			break;
		}
		for (;;) {
			if (current.getChildCount() == 0) {
				if (current.equals(end)) {
					return null;
				}
				return current;
			}
			current = (NodeModel) current.getChildAt(current.getChildCount() - 1);
		}
	}

	private ModeController getModeController() {
		return controller.getModeController();
	}



}
