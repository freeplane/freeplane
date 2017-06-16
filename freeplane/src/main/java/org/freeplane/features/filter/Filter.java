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

import java.util.Collection;
import java.util.HashMap;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.features.filter.condition.ICondition;
import org.freeplane.features.map.IMapSelection;
import org.freeplane.features.map.MapChangeEvent;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;

/**
 * @author Dimitry Polivaev
 */
public class Filter {
	static Filter createTransparentFilter() {
		final ResourceController resourceController = ResourceController.getResourceController();
		return new Filter(null, resourceController.getBooleanProperty("filter.showAncestors"), resourceController.getBooleanProperty("filter.showDescendants"), false);
	}
	
	public interface FilterInfoAccessor{
		public FilterInfo getFilterInfo(NodeModel node);
	}
	
	static public FilterInfoAccessor DEFAULT_FILTER_INFO_ACCESSOR = new FilterInfoAccessor() {
		
		@Override
		public FilterInfo getFilterInfo(NodeModel node) {
			return node.getFilterInfo();
		}
	};
	
	static public Filter createOneTimeFilter(final ICondition condition, final boolean areAncestorsShown,
            final boolean areDescendantsShown, final boolean applyToVisibleNodesOnly) {
		
		FilterInfoAccessor oneTimeFilterAccessor = new FilterInfoAccessor() {
			HashMap<NodeModel, FilterInfo> filterInfos = new HashMap<>();
			
			@Override
			public FilterInfo getFilterInfo(NodeModel node) {
				FilterInfo filterInfo = filterInfos.get(node);
				if(filterInfo == null) {
					filterInfo = new FilterInfo();
					filterInfos.put(node, filterInfo);
				}
				return filterInfo;
			}
		};
		return new Filter(condition, areAncestorsShown, areDescendantsShown, applyToVisibleNodesOnly, oneTimeFilterAccessor);
	}

	final private boolean appliesToVisibleNodesOnly;
	final private ICondition condition;
	final int options;

	final private FilterInfoAccessor accessor;

	public Filter(final ICondition condition, final boolean areAncestorsShown,
            final boolean areDescendantsShown, final boolean applyToVisibleNodesOnly) {
		this(condition, areAncestorsShown, areDescendantsShown, applyToVisibleNodesOnly, DEFAULT_FILTER_INFO_ACCESSOR);
	}
	public Filter(final ICondition condition, final boolean areAncestorsShown,
	              final boolean areDescendantsShown, final boolean applyToVisibleNodesOnly,
	              FilterInfoAccessor accessor) {
		super();
		this.condition = condition;
		this.accessor = accessor;
		int options = FilterInfo.FILTER_INITIAL_VALUE | FilterInfo.FILTER_SHOW_MATCHED;
		if (areAncestorsShown) {
			options += FilterInfo.FILTER_SHOW_ANCESTOR;
		}
		options += FilterInfo.FILTER_SHOW_ECLIPSED;
		if (areDescendantsShown) {
			options += FilterInfo.FILTER_SHOW_DESCENDANT;
		}
		this.options = options;
		appliesToVisibleNodesOnly = condition != null && applyToVisibleNodesOnly;
	}

	void addFilterResult(final NodeModel node, final int flag) {
		getFilterInfo(node).add(flag);
	}

	protected boolean appliesToVisibleNodesOnly() {
		return appliesToVisibleNodesOnly;
	}

	static private Icon filterIcon;

	void displayFilterStatus() {
		if (filterIcon == null) {
			filterIcon = ResourceController.getResourceController().getIcon("/images/filter.png");
		}
		if (getCondition() != null) {
			Controller.getCurrentController().getViewController().addStatusInfo("filter", null, filterIcon);
		}
		else {
			Controller.getCurrentController().getViewController().removeStatus("filter");
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * freeplane.controller.filter.Filter#applyFilter(freeplane.modes.MindMap)
	 */
	public void applyFilter(Object source, final MapModel map, final boolean force) {
		if (map == null) {
			return;
		}
		try {
			displayFilterStatus();
			Controller.getCurrentController().getViewController().setWaitingCursor(true);
			final Filter oldFilter = map.getFilter();
			if (force || !isConditionStronger(oldFilter)) {
				calculateFilterResults(map);
			}
			map.setFilter(this);
			final IMapSelection selection = Controller.getCurrentController().getSelection();
			final NodeModel selected = selection.getSelected();
			final NodeModel selectedVisible = selected.getVisibleAncestorOrSelf();
			selection.keepNodePosition(selectedVisible, 0.5f, 0.5f);
			refreshMap(source, map);
			selectVisibleNode();
		}
		finally {
			Controller.getCurrentController().getViewController().setWaitingCursor(false);
		}
	}
	public void calculateFilterResults(final MapModel map) {
		final NodeModel root = map.getRootNode();
		resetFilter(root);
		if (filterChildren(root, checkNode(root), false)) {
			addFilterResult(root, FilterInfo.FILTER_SHOW_ANCESTOR);
		}
	}

	private boolean applyFilter(final NodeModel node,
	                            final boolean isAncestorSelected, final boolean isAncestorEclipsed,
	                            boolean isDescendantSelected) {
		final boolean canBeShown = ! shouldRemainInvisible(node);
		final boolean conditionSatisfied = canBeShown && checkNode(node);
		resetFilter(node);
		if (isAncestorSelected && canBeShown) {
			addFilterResult(node, FilterInfo.FILTER_SHOW_DESCENDANT);
		}
		if (conditionSatisfied) {
			isDescendantSelected = true;
			addFilterResult(node, FilterInfo.FILTER_SHOW_MATCHED);
		}
		else {
			addFilterResult(node, FilterInfo.FILTER_SHOW_HIDDEN);
		}
		if (isAncestorEclipsed && canBeShown) {
			addFilterResult(node, FilterInfo.FILTER_SHOW_ECLIPSED);
		}
		if (filterChildren(node, conditionSatisfied || isAncestorSelected, !conditionSatisfied
		        || isAncestorEclipsed)) {
			if(canBeShown)
				addFilterResult(node, FilterInfo.FILTER_SHOW_ANCESTOR);
			isDescendantSelected = true;
		}
		return isDescendantSelected;
	}

	/*
	 * (non-Javadoc)
	 * @see freeplane.controller.filter.Filter#areAncestorsShown()
	 */
	public boolean areAncestorsShown() {
		return 0 != (options & FilterInfo.FILTER_SHOW_ANCESTOR);
	}

	/*
	 * (non-Javadoc)
	 * @see freeplane.controller.filter.Filter#areDescendantsShown()
	 */
	public boolean areDescendantsShown() {
		return 0 != (options & FilterInfo.FILTER_SHOW_DESCENDANT);
	}

	private boolean checkNode(final NodeModel node) {
		if (condition == null) {
			return true;
		}
		if (shouldRemainInvisible(node)) {
			return false;
		}
		return condition.checkNode(node);
	}
	
	private boolean shouldRemainInvisible(final NodeModel node) {
		return condition != null && appliesToVisibleNodesOnly && !node.hasVisibleContent();
	}

	private boolean filterChildren(final NodeModel node,
	                               final boolean isAncestorSelected, final boolean isAncestorEclipsed) {
		boolean isDescendantSelected = false;
		for (final NodeModel child : Controller.getCurrentModeController().getMapController().childrenUnfolded(node)) {
			isDescendantSelected = applyFilter(child, isAncestorSelected, isAncestorEclipsed,
			    isDescendantSelected);
		}
		return isDescendantSelected;
	}

	public ICondition getCondition() {
		return condition;
	}

	public boolean isConditionStronger(final Filter oldFilter) {
		return (!appliesToVisibleNodesOnly || appliesToVisibleNodesOnly == oldFilter.appliesToVisibleNodesOnly)
		        && (condition != null && condition.equals(oldFilter.getCondition()) || condition == null
		                && oldFilter.getCondition() == null);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * freeplane.controller.filter.Filter#isVisible(freeplane.modes.MindMapNode)
	 */
	public boolean isVisible(final NodeModel node) {
		if (condition == null) {
			return true;
		}
		return getFilterInfo(node).isVisible(this.options);
	}
	private void refreshMap(Object source, MapModel map) {
		Controller.getCurrentModeController().getMapController().fireMapChanged(new MapChangeEvent(source, map, Filter.class, null, this));
	}

	private void resetFilter(final NodeModel node) {
		getFilterInfo(node).reset();
	}

	private FilterInfo getFilterInfo(final NodeModel node) {
		return accessor.getFilterInfo(node);
	}

	private void selectVisibleNode() {
		final IMapSelection mapSelection = Controller.getCurrentController().getSelection();
		final Collection<NodeModel> selectedNodes = mapSelection.getSelection();
		final NodeModel[] array = new NodeModel[selectedNodes.size()];
		boolean next = false;
		for(NodeModel node : selectedNodes.toArray(array)){
			if(next){
				if (!node.hasVisibleContent()) {
					mapSelection.toggleSelected(node);
				}
			}
			else
				next = true;
		}
		NodeModel selected = mapSelection.getSelected();
		if (!selected.hasVisibleContent()) {
			if(mapSelection.getSelection().size() > 1){
				mapSelection.toggleSelected(selected);
			}
			else
				mapSelection.selectAsTheOnlyOneSelected(selected.getVisibleAncestorOrSelf());
		}
		mapSelection.setSiblingMaxLevel(mapSelection.getSelected().getNodeLevel(false));
	}
}
