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
package org.freeplane.features.filter;

import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
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
import javax.swing.JComponent;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.io.xml.XMLLocalParserFactory;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.ButtonModelStateChangeListenerForProperty;
import org.freeplane.core.ui.SelectableAction;
import org.freeplane.core.ui.components.FreeplaneToolBar;
import org.freeplane.core.ui.components.JAutoToggleButton;
import org.freeplane.core.ui.components.JComboBoxWithBorder;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.ui.menubuilders.generic.EntryVisitor;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.filter.condition.ASelectableCondition;
import org.freeplane.features.filter.condition.ConditionFactory;
import org.freeplane.features.filter.condition.ConditionSnapshotFactory;
import org.freeplane.features.filter.condition.DefaultConditionRenderer;
import org.freeplane.features.filter.condition.ICondition;
import org.freeplane.features.filter.condition.NoFilteringCondition;
import org.freeplane.features.filter.condition.SelectedViewCondition;
import org.freeplane.features.highlight.HighlightController;
import org.freeplane.features.highlight.NodeHighlighter;
import org.freeplane.features.map.CloneOfSelectedViewCondition;
import org.freeplane.features.map.IMapSelectionListener;
import org.freeplane.features.map.MapController.Direction;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.MapNavigationUtils;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.ui.FrameController;
import org.freeplane.features.ui.ToggleToolbarAction;
import org.freeplane.features.ui.UIComponentVisibilityDispatcher;
import org.freeplane.features.ui.ViewController;
import org.freeplane.n3.nanoxml.IXMLParser;
import org.freeplane.n3.nanoxml.IXMLReader;
import org.freeplane.n3.nanoxml.StdXMLReader;
import org.freeplane.n3.nanoxml.XMLElement;
import org.freeplane.n3.nanoxml.XMLWriter;

/**
 * @author Dimitry Polivaev
 */
public class FilterController implements IMapSelectionListener, IExtension {
	public static int TOOLBAR_SIDE = ViewController.TOP;
	@SuppressWarnings("serial")
    @SelectableAction(checkOnPopup = true)
	private class ToggleFilterToolbarAction extends ToggleToolbarAction {
	    private ToggleFilterToolbarAction(String actionName, String toolbarName) {
		    super(actionName, toolbarName);
	    }

	    @Override
		public void actionPerformed(ActionEvent event) {
	    	final JComponent toolbar = getToolbar();
	    	if(toolbar == null)
	    		return;
	    	final boolean visible = isVisible();
			if(visible && ! quickEditor.isInputFieldFocused() && (EventQueue.getCurrentEvent() instanceof KeyEvent))
	    		quickEditor.focusInputField(true);
	    	else {
	    		changeFocusWhenVisibilityChanges(toolbar);
	    		super.actionPerformed(event);
	    	}
		}

	    private void changeFocusWhenVisibilityChanges(final JComponent toolBar) {
	    	quickEditor.addAncestorListener(new AncestorListener() {
	    		public void ancestorAdded(final AncestorEvent event) {
	    			quickEditor.focusInputField(true);
	    			quickEditor.removeAncestorListener(this);
	    		}
	    		public void ancestorMoved(final AncestorEvent event) {
	    		}
	    		public void ancestorRemoved(final AncestorEvent event) {
	    			final Component selectedComponent = Controller.getCurrentController().getMapViewManager().getSelectedComponent();
	    			if(selectedComponent != null)
	    				selectedComponent.requestFocusInWindow();
	    			quickEditor.removeAncestorListener(this);
	    		}
	    	});
	    }
    }

	private class FilterChangeListener implements ListDataListener, ChangeListener {
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
	private static final ASelectableCondition NO_FILTERING = NoFilteringCondition.createCondition();

	public static FilterController getController(Controller controller) {
		return controller.getExtension(FilterController.class);
	}

	public static FilterController getCurrentFilterController() {
		return getController(Controller.getCurrentController());
	}

	public static void install() {
		final Controller controller = Controller.getCurrentController();
		final FilterController extension = new FilterController();
		controller.addExtension(FilterController.class, extension);
		controller.getExtension(HighlightController.class).addNodeHighlighter(new NodeHighlighter() {
			@Override
			public boolean isNodeHighlighted(NodeModel node, boolean isPrinting) {
				return !isPrinting && FilterController.getController(controller).isNodeHighlighted(node);
			}
			
			@Override
			public void configure(Graphics2D g, boolean isPrinting) {
				g.setColor(Color.MAGENTA);
			}
		});

	}

	private final ButtonModel applyToVisibleNodeOnly;
	private ConditionFactory conditionFactory;
	private DefaultConditionRenderer conditionRenderer = null;
// // 	private final Controller controller;
	final private FilterChangeListener filterChangeListener;
	private DefaultComboBoxModel filterConditions;
	private final FilterMenuBuilder filterMenuBuilder;
	private JToolBar filterToolbar;
	private final FilterHistory history;
	private Filter inactiveFilter;
	final private String pathToFilterFile;
	private ASelectableCondition selectedViewCondition;
	private ASelectableCondition cloneOfSelectedViewCondition;
	private final ButtonModel showAncestors;
	private final ButtonModel approximateMatchingButtonModel;
	private final ButtonModel caseSensitiveButtonModel;
	private final ButtonModel showDescendants;
	private final ButtonModel highlightNodes;
	private ASelectableCondition highlightCondition;
	private JComboBox activeFilterConditionComboBox;
	private final FilterConditionEditor quickEditor;
	static final int USER_DEFINED_CONDITION_START_INDEX = 3;
	final private QuickFilterAction quickFilterAction;

	public FilterController() {
		Controller controller = Controller.getCurrentController();
		filterMenuBuilder = new FilterMenuBuilder(controller, this);
		history = new FilterHistory();
		filterChangeListener = new FilterChangeListener();
		showAncestors = new JToggleButton.ToggleButtonModel();
		final Filter transparentFilter = createTransparentFilter();
		showAncestors.setSelected(transparentFilter.areAncestorsShown());
		showAncestors.addChangeListener(filterChangeListener);
		showAncestors.addChangeListener(new ButtonModelStateChangeListenerForProperty("filter.showAncestors"));
		showDescendants = new JToggleButton.ToggleButtonModel();
		showDescendants.setSelected(transparentFilter.areDescendantsShown());
		showDescendants.addChangeListener(filterChangeListener);
		showDescendants.addChangeListener(new ButtonModelStateChangeListenerForProperty("filter.showDescendants"));
		highlightNodes = new JToggleButton.ToggleButtonModel();
		highlightNodes.setSelected(false);
		applyToVisibleNodeOnly = new JToggleButton.ToggleButtonModel();
		applyToVisibleNodeOnly.setSelected(false);
		approximateMatchingButtonModel = new JToggleButton.ToggleButtonModel();
		approximateMatchingButtonModel.setSelected(false);
		caseSensitiveButtonModel = new JToggleButton.ToggleButtonModel();
		caseSensitiveButtonModel.setSelected(false);

		controller.getMapViewManager().addMapSelectionListener(this);
        final AFreeplaneAction showFilterToolbar = new ToggleFilterToolbarAction("ShowFilterToolbarAction", "/filter_toolbar");
		quickEditor = new FilterConditionEditor(this, 0, true);
		quickEditor.setEnterKeyActionListener( new ActionListener()  {

			public void actionPerformed(ActionEvent e) {
				((QuickFindAction)Controller.getCurrentController().getAction("QuickFindAction.FORWARD")).executeAction(true);
				if(getHighlightNodes().isSelected()){
					setHighlightCondition( quickEditor.getCondition());
				}
			}

		}
		);
		controller.addAction(showFilterToolbar);
		controller.addAction(new ApplyNoFilteringAction(this));
		controller.addAction(new ApplySelectedViewConditionAction(this));
		controller.addAction(new EditFilterAction(this));
		controller.addAction(new UndoFilterAction(this));
		controller.addAction(new RedoFilterAction(this));
		controller.addAction(new ReapplyFilterAction(this));
		controller.addAction(new ShowAncestorsAction(this));
		controller.addAction(new ShowDescendantsAction(this));
		controller.addAction(new ApplyToVisibleAction(this));
		quickFilterAction = new QuickFilterAction(this, quickEditor);
		controller.addAction(quickFilterAction);
		controller.addAction(new QuickFindAction(this, quickEditor, Direction.BACK));
		controller.addAction(new QuickFindAction(this, quickEditor, Direction.FORWARD));
		controller.addAction(new QuickFindAllAction(this, quickEditor));
		controller.addAction(new QuickHighlightAction(this, quickEditor));

		final FindAction find = new FindAction();
		controller.addAction(find);
		controller.addAction(find.getFindNextAction());
		controller.addAction(find.getFindPreviousAction());
		pathToFilterFile = ResourceController.getResourceController().getFreeplaneUserDirectory() + File.separator
		        + "auto." + FilterController.FREEPLANE_FILTER_EXTENSION_WITHOUT_DOT;
	}

	private void addStandardConditions() {
		final ASelectableCondition noFiltering = NO_FILTERING;
		filterConditions.insertElementAt(noFiltering, 0);
		if (selectedViewCondition == null) {
			selectedViewCondition = SelectedViewCondition.CreateCondition();
		}
		filterConditions.insertElementAt(selectedViewCondition, 1);
		if (filterConditions.getSelectedItem() == null) {
			filterConditions.setSelectedItem(noFiltering);
		}

		if (cloneOfSelectedViewCondition == null)
			cloneOfSelectedViewCondition = CloneOfSelectedViewCondition.CreateCondition();
		filterConditions.insertElementAt(cloneOfSelectedViewCondition, 2);
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
			quickEditor.setEnabled(true);
			quickEditor.mapChanged(newMap);
			final Filter filter = newMap.getFilter();
			updateSettingsFromFilter(filter);
			quickFilterAction.setSelected(isFilterActive());

		}
		else {
			filterConditions.setSelectedItem(filterConditions.getElementAt(0));
			filterToolbar.setEnabled(false);
			quickEditor.setEnabled(false);
			activeFilterConditionComboBox.setEnabled(false);
		}
	}

	void applyFilter(final boolean force) {
		quickFilterAction.setSelected(isFilterActive());
		final ASelectableCondition selectedCondition = getSelectedCondition();
		final Filter filter = createFilter(selectedCondition);
		final ICondition condition = condition(filter);
		if(condition != selectedCondition && condition instanceof ASelectableCondition)
			getFilterConditions().setSelectedItem(condition);
		else
			applyFilter(filter, Controller.getCurrentController().getMap(), force);
	}

	public void applyFilter(final Filter filter, MapModel map, final boolean force) {
	    filter.applyFilter(this, map, force);
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

	private Filter createFilter(final ASelectableCondition selectedCondition) {

		final ASelectableCondition filterCondition;
		if (selectedCondition == null || selectedCondition.equals(NO_FILTERING)) {
			filterCondition = null;
		}
		else if (selectedCondition instanceof ConditionSnapshotFactory) {
			filterCondition =  ((ConditionSnapshotFactory)selectedCondition).createSnapshotCondition();
		}
		else {
			filterCondition = selectedCondition;
		}
		final Filter filter = new Filter(filterCondition, showAncestors.isSelected(), showDescendants
		    .isSelected(), applyToVisibleNodeOnly.isSelected());
		return filter;
	}

	private JToolBar createFilterToolbar() {
		final JToolBar filterToolbar = new FreeplaneToolBar("filter_toolbar", SwingConstants.HORIZONTAL);
		filterToolbar.setVisible(ResourceController.getResourceController()
		    .getBooleanProperty("filter_toolbar_visible"));
		Controller controller = Controller.getCurrentController();
		FrameController frameController = (FrameController) controller.getViewController();
		UIComponentVisibilityDispatcher.install(frameController, filterToolbar, "filter_toolbar_visible");
		final JButton undoBtn = new JButton(controller.getAction("UndoFilterAction"));
		final JButton redoBtn = new JButton(controller.getAction("RedoFilterAction"));
		final JToggleButton showAncestorsBox = new JAutoToggleButton(controller.getAction("ShowAncestorsAction"),
		    showAncestors);
		showAncestorsBox.setSelected(showAncestors.isSelected());
		final JToggleButton showDescendantsBox = new JAutoToggleButton(controller.getAction("ShowDescendantsAction"),
		    showDescendants);
		final JToggleButton applyToVisibleBox = new JAutoToggleButton(controller.getAction("ApplyToVisibleAction"),
		    applyToVisibleNodeOnly);
		final JButton btnEdit = new JButton(controller.getAction("EditFilterAction"));
		activeFilterConditionComboBox = new JComboBoxWithBorder(getFilterConditions());
		final JButton applyBtn = new JButton(controller.getAction("ReapplyFilterAction"));
		final JButton filterSelectedBtn = new JButton(controller.getAction("ApplySelectedViewConditionAction"));
		final JButton noFilteringBtn = new JButton(controller.getAction("ApplyNoFilteringAction"));
		final JButton applyFindPreviousBtn = new JButton(controller.getAction("QuickFindAction.BACK"));
		final JButton applyFindNextBtn = new JButton(controller.getAction("QuickFindAction.FORWARD"));
		final JAutoToggleButton applyQuickFilterBtn = new JAutoToggleButton(controller.getAction("QuickFilterAction"));
		final JButton applyQuickSelectBtn = new JButton(controller.getAction("QuickFindAllAction"));
		final JToggleButton applyQuickHighlightBtn = new JAutoToggleButton(controller.getAction("QuickHighlightAction"));

		filterToolbar.addSeparator();
		filterToolbar.add(undoBtn);
		filterToolbar.add(redoBtn);
		filterToolbar.add(showAncestorsBox);
		filterToolbar.add(showDescendantsBox);
		filterToolbar.add(applyToVisibleBox);
		filterToolbar.add(activeFilterConditionComboBox);
		filterToolbar.add(applyBtn);
		filterToolbar.add(filterSelectedBtn);
		filterToolbar.add(noFilteringBtn);
		filterToolbar.add(btnEdit);
		filterToolbar.addSeparator();
		filterToolbar.add(quickEditor);
		filterToolbar.add(applyFindPreviousBtn);
		filterToolbar.add(applyFindNextBtn);
		filterToolbar.add(applyQuickSelectBtn);
		filterToolbar.add(applyQuickFilterBtn);
		filterToolbar.add(applyQuickHighlightBtn);
		final DefaultConditionRenderer toolbarConditionRenderer = new DefaultConditionRenderer(TextUtils.getText("filter_no_filtering"), false);
		activeFilterConditionComboBox.setRenderer(toolbarConditionRenderer);
		return filterToolbar;
	}

	public Filter createTransparentFilter() {
		if (inactiveFilter == null) {
			inactiveFilter = Filter.createTransparentFilter();
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
			conditionRenderer = new DefaultConditionRenderer(TextUtils.getText("filter_no_filtering"), true);
		}
		return conditionRenderer;
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

	ASelectableCondition getSelectedCondition() {
		return (ASelectableCondition) getFilterConditions().getSelectedItem();
	}

	public ButtonModel getShowAncestors() {
		return showAncestors;
	}

	public ButtonModel getShowDescendants() {
		return showDescendants;
	}

	public ButtonModel getHighlightNodes() {
		return highlightNodes;
	}

	public ASelectableCondition getHighlightCondition() {
    	return highlightCondition;
    }

	void setHighlightCondition(final ASelectableCondition condition) {
		if(condition != null){
			this.highlightCondition = condition;
			getHighlightNodes().setSelected(true);
		}
		else{
			this.highlightCondition = null;
		}
		final Component mapViewComponent = Controller.getCurrentController().getMapViewManager().getMapViewComponent();
		if(mapViewComponent != null)
			mapViewComponent.repaint();
	}

	private void initConditions() {
		filterConditions = new DefaultComboBoxModel();
		addStandardConditions();
		filterConditions.setSelectedItem(filterConditions.getElementAt(0));
		filterConditions.addListDataListener(filterChangeListener);
	}

	public void loadDefaultConditions() {
	    try {
			loadConditions(getFilterConditions(), pathToFilterFile, false);
		}
		catch (final Exception e) {
			LogUtils.severe(e);
		}
    }

	void loadConditions(final DefaultComboBoxModel filterConditionModel, final String pathToFilterFile,
			final boolean showPopupOnError)
	        throws IOException {
		try {
			final IXMLParser parser = XMLLocalParserFactory.createLocalXMLParser();
			File filterFile = new File(pathToFilterFile);
			final IXMLReader reader = new StdXMLReader(new BufferedInputStream(new FileInputStream(filterFile)));
			parser.setReader(reader);
			reader.setSystemID(filterFile.toURL().toString());
			final XMLElement loader = (XMLElement) parser.parse();
			final Vector<XMLElement> conditions = loader.getChildren();
			for (int i = 0; i < conditions.size(); i++) {
				final ASelectableCondition condition = getConditionFactory().loadCondition(conditions.get(i));
				if(condition != null){
					filterConditionModel.addElement(condition);
				}
			}
		}
		catch (final FileNotFoundException e) {
		}
		catch (final AccessControlException e) {
		}
		catch (final Exception e) {
			LogUtils.warn(e);
			if (showPopupOnError) {
				UITools.errorMessage(TextUtils.getText("filters_not_loaded"));
			}
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
			final ASelectableCondition cond = (ASelectableCondition) filterConditionModel.getElementAt(i);
			if (cond != null && !(cond instanceof NoFilteringCondition)) {
				cond.toXml(saver);
			}
		}
		final XMLWriter xmlWriter = new XMLWriter(writer);
		xmlWriter.write(saver, true);
		writer.close();
	}

	void setFilterConditions(final DefaultComboBoxModel newConditionModel) {
		filterConditions.removeListDataListener(filterChangeListener);
		filterConditions.removeAllElements();
		for (int i = 0; i < newConditionModel.getSize(); i++) {
			filterConditions.addElement(newConditionModel.getElementAt(i));
		}
		filterConditions.setSelectedItem(newConditionModel.getSelectedItem());
		addStandardConditions();
		filterConditions.addListDataListener(filterChangeListener);
		applyFilter(false);
		filterMenuBuilder.updateMenus();
	}

	private void updateSettingsFromFilter(final Filter filter) {
		getFilterConditions().removeListDataListener(filterChangeListener);
		showAncestors.removeChangeListener(filterChangeListener);
		showDescendants.removeChangeListener(filterChangeListener);
		filterConditions.setSelectedItem(condition(filter));
		showAncestors.setSelected(filter.areAncestorsShown());
		showDescendants.setSelected(filter.areDescendantsShown());
		applyToVisibleNodeOnly.setSelected(filter.appliesToVisibleNodesOnly());
		filterConditions.addListDataListener(filterChangeListener);
		showAncestors.addChangeListener(filterChangeListener);
		showDescendants.addChangeListener(filterChangeListener);
		quickFilterAction.setSelected(isFilterActive());
	}

	private ICondition condition(final Filter filter) {
	    final ICondition condition = filter.getCondition();
		if (condition == null)
			return NO_FILTERING;
		else
			return condition;
    }

	void updateSettingsFromHistory() {
		final Filter filter = history.getCurrentFilter();
		updateSettingsFromFilter(filter);
	}

	NodeModel findNext(final NodeModel from, final NodeModel end, final Direction direction,
	                   final ASelectableCondition condition) {
		NodeModel next = from;
		for (;;) {
			do {
				switch (direction) {
					case FORWARD:
					case FORWARD_N_FOLD:
						next = MapNavigationUtils.findNext(direction, next, end);
						break;
					case BACK:
					case BACK_N_FOLD:
						next = MapNavigationUtils.findPrevious(direction, next, end);
						break;
				}
				if (next == null) {
					return null;
				}
			} while (!next.hasVisibleContent());
			if (next == from) {
				break;
			}
			if (condition == null || condition.checkNode(next)) {
				return next;
			}
		}
		return null;
	}

	public void redo() {
		history.redo();
		updateSettingsFromHistory();
    }

	public void undo() {
		history.undo();
		updateSettingsFromHistory();
    }

	public boolean isNodeHighlighted(NodeModel node) {
		return highlightCondition != null && highlightCondition.checkNode(node);
    }

	public ButtonModel getApproximateMatchingButtonModel() {
		return approximateMatchingButtonModel;
	}

	public ButtonModel getCaseSensitiveButtonModel() {
		return caseSensitiveButtonModel;
	}

	public void apply(ASelectableCondition condition) {
		final DefaultComboBoxModel filterConditions = getFilterConditions();
		if(condition.equals(filterConditions.getSelectedItem()))
			applyFilter(true);
		else
			filterConditions.setSelectedItem(condition);
    }

	public EntryVisitor getMenuBuilder() {
		return filterMenuBuilder;
	}

	public boolean isFilterActive() {
		final ASelectableCondition selectedCondition = getSelectedCondition();
		return NO_FILTERING != selectedCondition && null != selectedCondition;
	}

}
