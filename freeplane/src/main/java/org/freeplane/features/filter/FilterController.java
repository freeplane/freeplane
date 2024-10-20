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
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.security.AccessControlException;
import java.util.Collection;
import java.util.Vector;
import java.util.stream.Stream;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.JToolTip;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.io.xml.XMLLocalParserFactory;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.ButtonModelStateChangeListenerForProperty;
import org.freeplane.core.ui.SelectableAction;
import org.freeplane.core.ui.components.FreeplaneToolBar;
import org.freeplane.core.ui.components.IconListComponent;
import org.freeplane.core.ui.components.JAutoToggleButton;
import org.freeplane.core.ui.components.ObjectIcon;
import org.freeplane.core.ui.components.ToolbarLayout;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.ui.components.resizer.UIComponentVisibilityDispatcher;
import org.freeplane.core.ui.menubuilders.generic.EntryVisitor;
import org.freeplane.core.ui.menubuilders.menu.JUnitPanel;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.filter.Filter.FilteredElement;
import org.freeplane.features.filter.FilterConditionEditor.Variant;
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
import org.freeplane.features.map.IMapSelection;
import org.freeplane.features.map.MapChangeEvent;
import org.freeplane.features.map.MapController.Direction;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.MapNavigationUtils;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.styles.ConditionalStyleModel;
import org.freeplane.features.ui.IMapViewChangeListener;
import org.freeplane.features.ui.ToggleToolbarAction;
import org.freeplane.features.ui.ViewController;
import org.freeplane.n3.nanoxml.IXMLParser;
import org.freeplane.n3.nanoxml.IXMLReader;
import org.freeplane.n3.nanoxml.StdXMLReader;
import org.freeplane.n3.nanoxml.XMLElement;
import org.freeplane.n3.nanoxml.XMLWriter;
import org.freeplane.view.swing.map.NodeTooltipManager;

/**
 * @author Dimitry Polivaev
 */
public class FilterController implements IExtension, IMapViewChangeListener {
	public static final Color HIGHLIGHT_COLOR = Color.MAGENTA;
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
	    	JComponent editorPanel = quickEditor.getPanel();
			editorPanel.addAncestorListener(new AncestorListener() {
	    		@Override
				public void ancestorAdded(final AncestorEvent event) {
	    			quickEditor.focusInputField(true);
	    			editorPanel.removeAncestorListener(this);
	    		}
	    		@Override
				public void ancestorMoved(final AncestorEvent event) {
	    		}
	    		@Override
				public void ancestorRemoved(final AncestorEvent event) {
	    			final Component selectedComponent = Controller.getCurrentController().getMapViewManager().getSelectedComponent();
	    			if(selectedComponent != null)
	    				selectedComponent.requestFocusInWindow();
	    			editorPanel.removeAncestorListener(this);
	    		}
	    	});
	    }
    }

	private class FilterChangeListener implements ChangeListener, ActionListener {
	    private boolean changeInProgress = false;
		/*
		 * (non-Javadoc)
		 * @see
		 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent
		 * )
		 */
	    public FilterChangeListener() {
	    }

	    @Override
	    public void stateChanged(final ChangeEvent e) {
	        if(changeInProgress)
	            return;
	        try {
	            changeInProgress = true;
	            Object source = e.getSource();
	            FilteredElement oldFilteredElement = filteredElement;
	            if(source == applyToNodes) {
	                applyToConnectors.setSelected(false);
	                applyToNodesAndConnectors.setSelected(false);
	                filteredElement = FilteredElement.NODE;
	            }
	            else if(source == applyToNodesAndConnectors) {
	                changeInProgress = true;
	                applyToNodes.setSelected(false);
	                applyToConnectors.setSelected(false);
	                filteredElement = FilteredElement.NODE_AND_CONNECTOR;
	            }
	            else if(source == applyToConnectors) {
	                changeInProgress = true;
	                applyToNodes.setSelected(false);
	                applyToNodesAndConnectors.setSelected(false);
	                filteredElement = FilteredElement.CONNECTOR;
	            }
	            if(oldFilteredElement != filteredElement)
	                ResourceController.getResourceController().setProperty("filter.filteredElement", filteredElement.name());
	        }
		    finally {
		        changeInProgress = false;
            }
			applyFilter(false);
		}

        @Override
        public void actionPerformed(ActionEvent e) {
            applyFilter(true);
        }
	}

	public static final String FREEPLANE_FILTER_EXTENSION_WITHOUT_DOT = "mmfilter";
	private static final ASelectableCondition NO_FILTERING = NoFilteringCondition.createCondition();

	public static FilterController getController(Controller controller) {
		return controller.getExtension(FilterController.class);
	}

	public static FilterController getCurrentFilterController() {
		return getController(Controller.getCurrentController());
	}

    public static Filter getFilter(MapModel map) {
        IMapSelection selection = Controller.getCurrentController().getSelection();
        if(selection != null && selection.getMap() == map) {
            return selection.getFilter();
        }
        Filter fallbackFilter = map.getExtension(Filter.class);
        if(fallbackFilter == null) {
            fallbackFilter  = Filter.createTransparentFilter();
            map.putExtension(Filter.class, fallbackFilter);
        }
        return fallbackFilter;
    }

    public static void setFilter(MapModel map, Filter filter) {
        IMapSelection selection = Controller.getCurrentController().getSelection();
        if(selection != null && selection.getMap() == map)
            selection.setFilter(filter);
        else
            map.putExtension(Filter.class, filter);
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
			public void configure(NodeModel node, Graphics2D g, boolean isPrinting) {
				g.setColor(HIGHLIGHT_COLOR);
			}
		});

	}

    private final ButtonModel applyToVisibleElementsOnly;
    private FilteredElement filteredElement;
    private final ButtonModel applyToNodes;
    private final ButtonModel applyToNodesAndConnectors;
    private final ButtonModel applyToConnectors;
	private ConditionFactory conditionFactory;
	private DefaultConditionRenderer conditionRenderer = null;
// // 	private final Controller controller;
	final private FilterChangeListener filterChangeListener;
	private DefaultComboBoxModel filterConditions;
	private final FilterMenuBuilder filterMenuBuilder;
	private JComponent filterToolbar;
	private final FilterHistory history;
	final private String pathToFilterFile;
	private ASelectableCondition selectedViewCondition;
	private ASelectableCondition cloneOfSelectedViewCondition;
    private final ButtonModel hideMatchingNodes;
    private final ButtonModel showAncestors;
	private final ButtonModel approximateMatchingButtonModel;
	private final ButtonModel ignoreDiacriticsButtonModel;
	private final ButtonModel caseSensitiveButtonModel;
	private final ButtonModel showDescendants;
	private final ButtonModel highlightNodes;
	private ASelectableCondition highlightCondition;
	private ConditionalStyleModel highlightedConditionContext;
	private JComboBox activeFilterConditionComboBox;
	private final FilterConditionEditor quickEditor;

	static final int USER_DEFINED_CONDITION_START_INDEX = 3;
	final private QuickFilterAction quickFilterAction;
	private int mapChangeCounter;
    private boolean applyFilterRunning;

	public FilterController() {
		Controller controller = Controller.getCurrentController();
		filterMenuBuilder = new FilterMenuBuilder(controller, this);
		history = new FilterHistory();
		filterChangeListener = new FilterChangeListener();
		showAncestors = new JToggleButton.ToggleButtonModel();
		final Filter transparentFilter = Filter.createTransparentFilter();
		hideMatchingNodes = new JToggleButton.ToggleButtonModel();
		hideMatchingNodes.setSelected(transparentFilter.areMatchingElementsHidden());
		hideMatchingNodes.addChangeListener(filterChangeListener);
		showAncestors.setSelected(transparentFilter.areAncestorsShown());
		showAncestors.addChangeListener(filterChangeListener);
		showAncestors.addChangeListener(new ButtonModelStateChangeListenerForProperty("filter.showAncestors"));
		showDescendants = new JToggleButton.ToggleButtonModel();
		showDescendants.setSelected(transparentFilter.areDescendantsShown());
		showDescendants.addChangeListener(filterChangeListener);
		showDescendants.addChangeListener(new ButtonModelStateChangeListenerForProperty("filter.showDescendants"));
		highlightNodes = new JToggleButton.ToggleButtonModel();
		highlightNodes.setSelected(false);
        applyToVisibleElementsOnly = new JToggleButton.ToggleButtonModel();
        applyToVisibleElementsOnly.setSelected(false);
        applyToNodes = new JToggleButton.ToggleButtonModel();
        filteredElement = transparentFilter.getFilteredElement();
        applyToNodes.setSelected(filteredElement == FilteredElement.NODE);
        applyToNodesAndConnectors = new JToggleButton.ToggleButtonModel();
        applyToNodesAndConnectors.setSelected(filteredElement == FilteredElement.NODE_AND_CONNECTOR);
        applyToConnectors = new JToggleButton.ToggleButtonModel();
        applyToConnectors.setSelected(filteredElement == FilteredElement.CONNECTOR);
        applyToNodes.addChangeListener(filterChangeListener);
        applyToNodesAndConnectors.addChangeListener(filterChangeListener);
        applyToConnectors.addChangeListener(filterChangeListener);
        approximateMatchingButtonModel = new JToggleButton.ToggleButtonModel();
        approximateMatchingButtonModel.setSelected(false);
        ignoreDiacriticsButtonModel = new JToggleButton.ToggleButtonModel();
        ignoreDiacriticsButtonModel.setSelected(false);
		caseSensitiveButtonModel = new JToggleButton.ToggleButtonModel();
		caseSensitiveButtonModel.setSelected(false);

		controller.getMapViewManager().addMapViewChangeListener(this);

        final AFreeplaneAction showFilterToolbar = new ToggleFilterToolbarAction("ShowFilterToolbarAction", "/filter_toolbar");
		quickEditor = new FilterConditionEditor(this, 0, Variant.FILTER_TOOLBAR);
		quickEditor.setEnterKeyActionListener( new ActionListener()  {

			@Override
			public void actionPerformed(ActionEvent e) {
				((QuickFindAction)Controller.getCurrentController().getAction("QuickFindAction.FORWARD")).executeAction(true);
				if(getHighlightNodes().isSelected()){
					setHighlightCondition( quickEditor.getCondition(), null);
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
		controller.addAction(new SelectFilteredNodesAction(this));
		controller.addAction(new HideMatchingNodesAction(this));
		controller.addAction(new ShowAncestorsAction(this));
		controller.addAction(new ShowDescendantsAction(this));
        controller.addAction(new ApplyToVisibleAction(this));
        controller.addAction(new SelectFilteredElementAction(applyToNodes, FilteredElement.NODE));
        controller.addAction(new SelectFilteredElementAction(applyToNodesAndConnectors, FilteredElement.NODE_AND_CONNECTOR));
        controller.addAction(new SelectFilteredElementAction(applyToConnectors, FilteredElement.CONNECTOR));
		quickFilterAction = new QuickFilterAction(this, quickEditor);
		controller.addAction(quickFilterAction);
		controller.addAction(new QuickAndFilterAction(this, quickEditor));
		controller.addAction(new QuickOrFilterAction(this, quickEditor));
        controller.addAction(new QuickFindAction(this, quickEditor, Direction.BACK_VISIBLE));
        controller.addAction(new QuickFindAction(this, quickEditor, Direction.BACK));
        controller.addAction(new QuickFindAction(this, quickEditor, Direction.BACK_REMOVE_FILTER));
        controller.addAction(new QuickFindAction(this, quickEditor, Direction.FORWARD_VISIBLE));
        controller.addAction(new QuickFindAction(this, quickEditor, Direction.FORWARD));
        controller.addAction(new QuickFindAction(this, quickEditor, Direction.FORWARD_REMOVE_FILTER));
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
			selectedViewCondition = SelectedViewCondition.createCondition();
		}
		filterConditions.insertElementAt(selectedViewCondition, 1);
		if (filterConditions.getSelectedItem() == null) {
			filterConditions.setSelectedItem(noFiltering);
		}

		if (cloneOfSelectedViewCondition == null)
			cloneOfSelectedViewCondition = CloneOfSelectedViewCondition.createCondition();
		filterConditions.insertElementAt(cloneOfSelectedViewCondition, 2);
	}

	/**
	 */
	@Override
	public void afterViewChange(Component oldView, Component newView) {
		updateUILater();
	}

	private void updateUILater() {
        if(filterToolbar == null){
            return;
        }
		mapChangeCounter++;
		Controller.getCurrentController().getViewController().invokeLater(new Runnable() {
			@Override
			public void run() {
				mapChangeCounter--;
				if (0 == mapChangeCounter)
					updateUI();
			}
		});
	}

	private void updateUI() {
		final IMapSelection selection = Controller.getCurrentController().getSelection();
		if (selection != null) {
			filterToolbar.setEnabled(true);
			activeFilterConditionComboBox.setEnabled(true);
			quickEditor.setEnabled(true);
			final Filter filter = selection.getFilter();
			quickEditor.filterChanged(filter);
			updateSettingsFromFilter(filter);
			quickFilterAction.setSelected(isFilterActive());
			history.clear();
			history.add(filter);

		}
		else {
			filterConditions.setSelectedItem(filterConditions.getElementAt(0));
			filterToolbar.setEnabled(false);
			quickEditor.setEnabled(false);
			activeFilterConditionComboBox.setEnabled(false);
		}
	}

	void applyFilter(final boolean force) {
	    if(applyFilterRunning)
	        return;
	    applyFilterRunning = true;
	    try {
	        quickFilterAction.setSelected(isFilterActive());
	        final ASelectableCondition selectedCondition = getSelectedCondition();
	        final Filter filter = createFilter(selectedCondition);
	        final ICondition condition = condition(filter);
	        if(condition != selectedCondition && condition instanceof ASelectableCondition)
	            getFilterConditions().setSelectedItem(condition);
	        applyFilter(force, filter);
	        history.add(filter);
	    }
	    finally {
	        applyFilterRunning = false;
	    }
	}

	public void applyNoFiltering(MapModel map) {
	    final IMapSelection selection = Controller.getCurrentController().getSelection();
	    if(selection != null && selection.getMap() == map) {
	        getFilterConditions().setSelectedItem(NO_FILTERING);
	    }
	    else {
            final Filter filter = new Filter(NO_FILTERING, hideMatchingNodes.isSelected(), showAncestors.isSelected(),
                    showDescendants.isSelected(), false,
                    filteredElement,
                    null);
            map.putExtension(Filter.class, filter);
            filter.calculateFilterResults(map);
	    }
	}

    public void applyFilter(MapModel map, boolean force, Filter filter) {
        final IMapSelection selection = Controller.getCurrentController().getSelection();
        if(selection != null && selection.getMap() == map) {
            applyFilter(force, filter);
            updateSettingsFromFilter(filter);
            history.add(filter);
        }
        else {
            Filter oldFilter = map.putExtension(Filter.class, filter);
            if (oldFilter == null || force || !filter.canUseFilterResultsFrom(oldFilter)) {
                filter.calculateFilterResults(map);
        		NodeModel selectionRoot = selection.getSelectionRoot();
				if(! selectionRoot.isRoot())
        			filter.resetFilter(selectionRoot);
            }
        }
    }

    public void applyFilter(final boolean force, final Filter filter) {
        final IMapSelection selection = Controller.getCurrentController().getSelection();
        if (selection != null) {
            try {
            	filter.displayFilterStatus();
            	Controller.getCurrentController().getViewController().setWaitingCursor(true);
            	final Filter oldFilter = selection.getFilter();
            	selection.setFilter(filter);
            	MapModel map = selection.getSelected().getMap();
                if (force || !filter.canUseFilterResultsFrom(oldFilter)) {
            		filter.calculateFilterResults(map);
            		NodeModel selectionRoot = selection.getSelectionRoot();
					if(! selectionRoot.isRoot())
            			filter.resetFilter(selectionRoot);
            	}
                else {
                    filter.useFilterResultsFrom(oldFilter);
                }
            	refreshMap(this, map);
            	selectVisibleNodes(selection);
            }
            finally {
            	Controller.getCurrentController().getViewController().setWaitingCursor(false);
            }
        }
    }
    private void refreshMap(Object source, MapModel map) {
        Controller.getCurrentModeController().getMapController().fireMapChanged(new MapChangeEvent(source, map, Filter.class, null, this, false));
    }

    public void selectVisibleNodes(final IMapSelection selection ) {
        Filter filter = selection.getFilter();
        final NodeModel selectedVisible = selection.getSelected().getVisibleAncestorOrSelf(filter);
        selection.preserveNodeLocationOnScreen(selectedVisible, 0.5f, 0.5f);
        final Collection<NodeModel> selectedNodes = selection.getSelection();
        final NodeModel[] array = new NodeModel[selectedNodes.size()];
        boolean next = false;
        NodeModel selectionRoot = selection.getSelectionRoot();
        for(NodeModel node : selectedNodes.toArray(array)){
            if(next){
                if (!node.hasVisibleContent(filter) && selectionRoot != node) {
                    selection.toggleSelected(node);
                }
            }
            else
                next = true;
        }
        NodeModel selected = selection.getSelected();
        if (!selected.hasVisibleContent(filter) && selectionRoot != selected) {
            if(selection.getSelection().size() > 1){
                selection.toggleSelected(selected);
            } else {
                NodeModel visibleAncestorOrSelf = selected.getVisibleAncestorOrSelf(filter);
                if(selectionRoot.isDescendantOf(visibleAncestorOrSelf))
                        selection.selectAsTheOnlyOneSelected(selectionRoot);
                else
                    selection.selectAsTheOnlyOneSelected(visibleAncestorOrSelf);
            }
        }
        selection.setSiblingMaxLevel(selection.getSelected().getNodeLevel(filter));
    }
	void applySelectedViewCondition() {
		if (getFilterConditions().getSelectedItem() != selectedViewCondition) {
			getFilterConditions().setSelectedItem(selectedViewCondition);
		}
		else {
			applyFilter(true);
		}
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
		IMapSelection selection = Controller.getCurrentController().getSelection();
        final Filter baseFilter = selection != null ? selection.getFilter() : null;
        final Filter filter = new Filter(filterCondition, hideMatchingNodes.isSelected(), showAncestors.isSelected(), showDescendants
		    .isSelected(), applyToVisibleElementsOnly.isSelected(),
		    filteredElement, baseFilter);
		return filter;
	}


    private JComponent createFilterToolbar() {
		Controller controller = Controller.getCurrentController();
		final AbstractButton undoBtn = FreeplaneToolBar.createButton(controller.getAction("UndoFilterAction"));
		final AbstractButton redoBtn = FreeplaneToolBar.createButton(controller.getAction("RedoFilterAction"));
        final AbstractButton showAncestorsBox = new JAutoToggleButton(controller.getAction("ShowAncestorsAction"), showAncestors);
		showAncestorsBox.setSelected(showAncestors.isSelected());
		final AbstractButton showDescendantsBox = new JAutoToggleButton(controller.getAction("ShowDescendantsAction"), showDescendants);
        final AbstractButton hideMatchingNodesBox = new JAutoToggleButton(controller.getAction("HideMatchingNodesAction"), hideMatchingNodes);
        hideMatchingNodesBox.setSelected(hideMatchingNodes.isSelected());
        final AbstractButton applyToVisibleBox = new JAutoToggleButton(controller.getAction("ApplyToVisibleAction"), applyToVisibleElementsOnly);
        final AbstractButton applyToNodesBox = new JAutoToggleButton(controller.getAction("SelectFilteredElementAction.NODE"), applyToNodes);
        applyToNodesBox.setSelected(applyToNodes.isSelected());
        final AbstractButton applyToNodesAndConnectorsBox = new JAutoToggleButton(controller.getAction("SelectFilteredElementAction.NODE_AND_CONNECTOR"), applyToNodesAndConnectors);
        applyToNodesAndConnectorsBox.setSelected(applyToNodesAndConnectors.isSelected());
        final AbstractButton applyToConnectorsBox = new JAutoToggleButton(controller.getAction("SelectFilteredElementAction.CONNECTOR"), applyToConnectors);
        applyToConnectorsBox.setSelected(applyToConnectors.isSelected());
		final AbstractButton btnEdit = FreeplaneToolBar.createButton(controller.getAction("EditFilterAction"));
        getFilterConditions();

		final AbstractButton reapplyFilterBtn = FreeplaneToolBar.createButton(controller.getAction("ReapplyFilterAction"));
		final AbstractButton selectFilteredNodesBtn = FreeplaneToolBar.createButton(controller.getAction("SelectFilteredNodesAction"));
		final AbstractButton filterSelectedBtn = FreeplaneToolBar.createButton(controller.getAction("ApplySelectedViewConditionAction"));
		final AbstractButton noFilteringBtn = FreeplaneToolBar.createButton(controller.getAction("ApplyNoFilteringAction"));
		final AbstractButton applyFindPreviousVisibleBtn = FreeplaneToolBar.createButton(controller.getAction("QuickFindAction.BACK_VISIBLE"));
        final AbstractButton applyFindPreviousBtn = FreeplaneToolBar.createButton(controller.getAction("QuickFindAction.BACK"));
        final AbstractButton applyFindPreviousRemovindFilterBtn = FreeplaneToolBar.createButton(controller.getAction("QuickFindAction.BACK_REMOVE_FILTER"));
        final AbstractButton applyFindNextVisibleBtn = FreeplaneToolBar.createButton(controller.getAction("QuickFindAction.FORWARD_VISIBLE"));
        final AbstractButton applyFindNextBtn = FreeplaneToolBar.createButton(controller.getAction("QuickFindAction.FORWARD"));
        final AbstractButton applyFindNextRemovindFilterBtn = FreeplaneToolBar.createButton(controller.getAction("QuickFindAction.FORWARD_REMOVE_FILTER"));
		final AbstractButton applyQuickFilterBtn = FreeplaneToolBar.createButton(controller.getAction("QuickFilterAction"));
        final AbstractButton applyAndFilterBtn = FreeplaneToolBar.createButton(controller.getAction("QuickAndFilterAction"));
        final AbstractButton applyOrFilterBtn = FreeplaneToolBar.createButton(controller.getAction("QuickOrFilterAction"));
        final AbstractButton applyQuickSelectBtn = FreeplaneToolBar.createButton(controller.getAction("QuickFindAllAction"));
		final AbstractButton applyQuickHighlightBtn = FreeplaneToolBar.createButton(controller.getAction("QuickHighlightAction"));

		GridBagConstraints constraints = new GridBagConstraints();
		constraints.anchor = GridBagConstraints.NORTHWEST;
		constraints.fill = GridBagConstraints.HORIZONTAL;

		JComponent searchOptionPanel = quickEditor.getOptionPanel();

		constraints.weightx = 1;
		searchOptionPanel.add(new JUnitPanel(), constraints);

		constraints.weightx = 0;
		searchOptionPanel.add(applyQuickHighlightBtn, constraints);
		searchOptionPanel.add(applyQuickSelectBtn, constraints);
		searchOptionPanel.add(applyQuickFilterBtn, constraints);
		searchOptionPanel.add(applyAndFilterBtn, constraints);
		searchOptionPanel.add(applyOrFilterBtn, constraints);

		JComponent searchPanel = new FreeplaneToolBar("searchPanel", JToolBar.HORIZONTAL);

		constraints.gridwidth = 1;
		constraints.gridy = 0;
		searchPanel.add(applyFindNextVisibleBtn, constraints);
        searchPanel.add(applyFindNextBtn, constraints);
        searchPanel.add(applyFindNextRemovindFilterBtn, constraints);
		constraints.gridy = 1;
		searchPanel.add(applyFindPreviousVisibleBtn, constraints);
        searchPanel.add(applyFindPreviousBtn, constraints);
        searchPanel.add(applyFindPreviousRemovindFilterBtn, constraints);

		FreeplaneToolBar filterOptionPanel = new FreeplaneToolBar("filterOptionPanel", JToolBar.HORIZONTAL);

		constraints.gridy = 0;
		constraints.gridwidth = 11;
		constraints.gridheight = 1;
		filterOptionPanel.add(activeFilterConditionComboBox, constraints);

		constraints.gridy =1;
		constraints.gridwidth =1;

        filterOptionPanel.add(applyToNodesBox, constraints);
        filterOptionPanel.add(applyToNodesAndConnectorsBox, constraints);
        filterOptionPanel.add(applyToConnectorsBox, constraints);
		filterOptionPanel.add(showAncestorsBox, constraints);
		filterOptionPanel.add(showDescendantsBox, constraints);
		filterOptionPanel.add(hideMatchingNodesBox, constraints);

		constraints.weightx = 1;
		filterOptionPanel.add(new JUnitPanel(), constraints);

		constraints.weightx = 0;
        filterOptionPanel.add(applyToVisibleBox, constraints);
		filterOptionPanel.add(reapplyFilterBtn, constraints);
		filterOptionPanel.add(selectFilteredNodesBtn, constraints);
		filterOptionPanel.add(filterSelectedBtn, constraints);

		constraints.gridwidth = 1;

		constraints.gridy = 0;
		filterOptionPanel.add(undoBtn, constraints);
		filterOptionPanel.add(redoBtn, constraints);
		constraints.gridy = 1;
		filterOptionPanel.add(noFilteringBtn, constraints);
		filterOptionPanel.add(btnEdit, constraints);


		final DefaultConditionRenderer toolbarConditionRenderer = new DefaultConditionRenderer(TextUtils.getText("filter_no_filtering"), false);
		activeFilterConditionComboBox.setRenderer(toolbarConditionRenderer);

		final JPanel filterToolbar = new JPanel();
		filterToolbar.setLayout(ToolbarLayout.horizontal());
		filterToolbar.add(new JSeparator(SwingConstants.VERTICAL));
		filterToolbar.add(quickEditor.getPanel());
		filterToolbar.add(searchPanel);
		filterToolbar.add(new JSeparator(SwingConstants.VERTICAL));
		filterToolbar.add(filterOptionPanel);

		filterToolbar.setVisible(ResourceController.getResourceController()
		    .getBooleanProperty("filter_toolbar_visible"));
		UIComponentVisibilityDispatcher.install(filterToolbar, "filter_toolbar_visible");

		return filterToolbar;
	}

   JComboBox createActiveFilterConditionBox() {
        JComboBox box = new JComboBox(getFilterConditions()){
				{
					setMaximumRowCount(10);
				}
		    @Override
			public String getToolTipText() {
		        return "tooltip";
		    }

            @Override
            public JToolTip createToolTip() {
                JToolTip tip = new JToolTip() {
                    @Override
                    public void setTipText(String tipText) {
                        ASelectableCondition selectedItem = getSelectedFilterCondition();
                        IconListComponent renderer = selectedItem.getListCellRendererComponent(getFontMetrics(getFont()));
                        MouseAdapter mouseListener = new MouseAdapter() {

                            @Override
                            public void mouseClicked(MouseEvent e) {
                                ASelectableCondition removedCondition = getConditionUnderMouse(e);
                                if(removedCondition != null && removedCondition != NO_FILTERING) {
                                    removeCondition(removedCondition);
                                }
                            }

                            private ASelectableCondition getConditionUnderMouse(MouseEvent e) {
                                IconListComponent component = getComponent(e);
                                Icon icon = component.getIconAt(e.getPoint());
                                if(icon instanceof ObjectIcon<?>) {
                                    ASelectableCondition condition = ((ObjectIcon<ASelectableCondition>)icon).getObject();
                                    return condition;
                                }
                                else
                                    return null;
                            }

                            private IconListComponent getComponent(MouseEvent e) {
                                return (IconListComponent) e.getComponent();
                            }

                            private void removeCondition(ASelectableCondition removedCondition) {
                                ASelectableCondition selectedFilterCondition = getSelectedFilterCondition();
                                ASelectableCondition newCondition = selectedFilterCondition.removeCondition(removedCondition);
                                if(newCondition != selectedFilterCondition) {
                                    apply(newCondition == null ? NO_FILTERING : newCondition);
                                    NodeTooltipManager.getSharedInstance().hideTipWindow();
                                }
                            }

                            @Override
                            public void mouseEntered(MouseEvent e) {
                                highlightCondition(e);
                            }
                            @Override
                            public void mouseExited(MouseEvent e) {
                                getComponent(e).highlightRemovedIcon(null);
                            }

                            private void highlightCondition(MouseEvent e) {
                                IconListComponent component = getComponent(e);
                                if(getSelectedCondition() == NO_FILTERING)
                                    component.highlightRemovedIcon(null);
                                else {
                                    Icon icon = component.getIconAt(e.getPoint());
                                    component.highlightRemovedIcon(icon);
                                }
                            }

                            @Override
                            public void mouseMoved(MouseEvent e) {
                                highlightCondition(e);
                            }

                        };
                        renderer.addMouseListener(mouseListener);
                        renderer.addMouseMotionListener(mouseListener);
                        renderer.setBorder(BorderFactory.createRaisedBevelBorder());
                        add(renderer);
                        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    }
                    @Override
                    public Dimension getPreferredSize() {
                        if(getComponentCount() == 0)
                            return super.getPreferredSize();
                        final Component renderer = getComponent(0);
                        return renderer.getPreferredSize();
                    }

                    @Override
                    public void doLayout() {
                        if(getComponentCount() == 0) {
                            super.doLayout();
                            return;
                        }
                        final Component renderer = getComponent(0);
                        renderer.setLocation(0, 0);
                        renderer.setSize(getSize());
                    }
                };
                tip.setComponent(this);
                return tip;
            }

            @Override
            public Point getToolTipLocation(MouseEvent event) {
                int height = getHeight();
                return new Point(height / 5, height / 5);
            }


        };
        NodeTooltipManager.getSharedInstance().registerComponent(box);
        box.setPrototypeDisplayValue("XXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
        box.addActionListener(filterChangeListener);
        return box;
    }

    protected ButtonModel getApplyToVisibleElementsOnly() {
        return applyToVisibleElementsOnly;
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
	public JComponent getFilterToolbar() {
		if (filterToolbar == null) {
			filterToolbar = createFilterToolbar();
		}
		return filterToolbar;
	}

	public FilterConditionEditor getQuickEditor() {
		return quickEditor;
	}

	ASelectableCondition getSelectedCondition() {
		return (ASelectableCondition) getFilterConditions().getSelectedItem();
	}

    public ButtonModel getShowAncestors() {
        return showAncestors;
    }

    public ButtonModel getHideMatchingNodes() {
        return hideMatchingNodes;
    }

	public ButtonModel getShowDescendants() {
		return showDescendants;
	}

	public ButtonModel getHighlightNodes() {
		return highlightNodes;
	}

	void setHighlightCondition(final ASelectableCondition condition, ConditionalStyleModel highlightedConditionContext) {
		this.highlightedConditionContext = highlightedConditionContext;
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
		if(activeFilterConditionComboBox == null)
		    activeFilterConditionComboBox = createActiveFilterConditionBox();

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
	        int savedConditionLimit = ResourceController.getResourceController().getIntProperty("savedConditionLimit");
			saveConditions(getFilterConditions(), pathToFilterFile, savedConditionLimit);
		}
		catch (final Exception e) {
			LogUtils.warn(e);
		}
	}

	void saveConditions(final DefaultComboBoxModel filterConditionModel, final String pathToFilterFile, int savedConditionLimit)
	        throws IOException {
		final XMLElement saver = new XMLElement();
		saver.setName("filter_conditions");
        int savedConditionNumber = Math.min(savedConditionLimit, filterConditionModel.getSize());
        for (int i = 0; i < savedConditionNumber; i++) {
            final ASelectableCondition cond = (ASelectableCondition) filterConditionModel.getElementAt(i);
            if (cond != null && cond.canBePersisted()) {
                cond.toXml(saver);
            }
        }
		try(final Writer writer = new FileWriter(pathToFilterFile)) {
		    final XMLWriter xmlWriter = new XMLWriter(writer);
		    xmlWriter.write(saver, true);
		}
	}

	void setFilterConditions(final DefaultComboBoxModel newConditionModel) {
		filterConditions.removeAllElements();
		for (int i = 0; i < newConditionModel.getSize(); i++) {
			filterConditions.addElement(newConditionModel.getElementAt(i));
		}
		filterConditions.setSelectedItem(newConditionModel.getSelectedItem());
		addStandardConditions();
		applyFilter(false);
		filterMenuBuilder.updateMenus();
	}

	private void updateSettingsFromFilter(final Filter filter) {
	    applyFilterRunning = true;
	    try {
	        ICondition condition = condition(filter);
	        if(condition instanceof ASelectableCondition)
	            filterConditions.setSelectedItem(condition);
	        else
	            filterConditions.setSelectedItem(NO_FILTERING);
	        hideMatchingNodes.setSelected(filter.areMatchingElementsHidden());
	        showAncestors.setSelected(filter.areAncestorsShown());
	        showDescendants.setSelected(filter.areDescendantsShown());
	        applyToVisibleElementsOnly.setSelected(filter.appliesToVisibleElementsOnly());
	        applyToNodes.setSelected(filter.getFilteredElement() == FilteredElement.NODE);
            applyToNodesAndConnectors.setSelected(filter.getFilteredElement() == FilteredElement.NODE_AND_CONNECTOR);
            applyToConnectors.setSelected(filter.getFilteredElement() == FilteredElement.CONNECTOR);
	        quickFilterAction.setSelected(isFilterActive());
	    }
	    finally {
	        applyFilterRunning = false;
	    }

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


	NodeModel findNextInSubtree(final NodeModel start, NodeModel subtreeRoot, Direction direction,
			final ICondition condition, Filter filter) {
		NodeModel next = findNext(start, subtreeRoot, direction, condition, filter);
		if(next == null && subtreeRoot != null && subtreeRoot != start) {
			if(condition == null || condition.checkNode(subtreeRoot))
				next = subtreeRoot;
			else
				next = findNext(subtreeRoot, subtreeRoot, direction, condition, filter);
		}
		return next;
	}

	NodeModel findNext(final NodeModel from, final NodeModel end, final Direction direction,
	                   final ICondition condition, Filter filter) {
		NodeModel next = from;
		for (;;) {
		    do {
		        if (direction.isForward())
		            next = MapNavigationUtils.findNext(direction, next, end);
		        else
		            next = MapNavigationUtils.findPrevious(direction, next, end);
		        if (next == null) {
		            return null;
				}
			} while (! direction.removesFilter() && !next.hasVisibleContent(filter));
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

	private boolean isNodeHighlighted(NodeModel node) {
		try {
			if(highlightedConditionContext != null)
				highlightedConditionContext.setDisabled(true);
			return highlightCondition != null && highlightCondition.checkNode(node);
		}
		finally {
			if(highlightedConditionContext != null)
				highlightedConditionContext.setDisabled(false);
		}
    }

	public ButtonModel getApproximateMatchingButtonModel() {
		return approximateMatchingButtonModel;
	}

    public ButtonModel getIgnoreDiacriticsButtonModel() {
        return ignoreDiacriticsButtonModel;
    }



	public ButtonModel getCaseSensitiveButtonModel() {
		return caseSensitiveButtonModel;
	}

	public void apply(ASelectableCondition condition) {
		final DefaultComboBoxModel filterConditions = getFilterConditions();
		if(condition.equals(filterConditions.getSelectedItem()))
			applyFilter(true);
        else {
            activeFilterConditionComboBox.setEditable(true);
            if(filterConditions.getSize() > 3)
                filterConditions.removeElement(condition);
            filterConditions.setSelectedItem(condition);
            filterConditions.insertElementAt(condition, 3);
            activeFilterConditionComboBox.setEditable(false);
        }
    }

	public EntryVisitor getMenuBuilder() {
		return filterMenuBuilder;
	}

	public boolean isFilterActive() {
		final ASelectableCondition selectedCondition = getSelectedCondition();
		return NO_FILTERING != selectedCondition && null != selectedCondition;
	}

    public void mapRootNodeChanged(MapModel map) {
        IMapSelection selection = Controller.getCurrentController().getSelection();
        if(selection != null && map.equals(selection.getMap()))
            updateUILater();
    }

    private ASelectableCondition getSelectedFilterCondition() {
        return getSelectedCondition();
    }
}
