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

import org.freeplane.core.resources.ResourceController;
import org.freeplane.features.filter.condition.ICondition;
import org.freeplane.features.filter.hidden.NodeVisibility;
import org.freeplane.features.filter.hidden.NodeVisibilityConfiguration;
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
		return new Filter(null, false, resourceController.getBooleanProperty("filter.showAncestors"), resourceController.getBooleanProperty("filter.showDescendants"), false);
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
		return new Filter(condition, false, areAncestorsShown, areDescendantsShown, applyToVisibleNodesOnly, oneTimeFilterAccessor);
	}

	final private boolean appliesToVisibleNodesOnly;
	final private ICondition condition;
	final int options;

	final private FilterInfoAccessor accessor;
    final private boolean hidesMatchingNodes;

    public Filter(final ICondition condition, final boolean hidesMatchingNodes, final boolean areAncestorsShown,
            final boolean areDescendantsShown, final boolean applyToVisibleNodesOnly) {
		this(condition, hidesMatchingNodes, areAncestorsShown, areDescendantsShown, applyToVisibleNodesOnly, DEFAULT_FILTER_INFO_ACCESSOR);
	}
	public Filter(final ICondition condition, final boolean hidesMatchingNodes, final boolean areAncestorsShown,
	              final boolean areDescendantsShown, final boolean applyToVisibleNodesOnly,
	              FilterInfoAccessor accessor) {
		super();
		this.condition = condition;
        this.hidesMatchingNodes = hidesMatchingNodes;
		this.accessor = accessor;
		int options = FilterInfo.FILTER_SHOW_AS_MATCHED;
		if (areAncestorsShown) {
			options += FilterInfo.FILTER_SHOW_AS_ANCESTOR;
		}
		if (areDescendantsShown) {
			options += FilterInfo.FILTER_SHOW_AS_DESCENDANT;
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
			filterIcon = ResourceController.getResourceController().getIcon("/images/filter.svg");
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
			map.setFilter(this);
			if (force || !isConditionStronger(oldFilter)) {
				calculateFilterResults(map);
			}
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
			addFilterResult(root, FilterInfo.FILTER_SHOW_AS_ANCESTOR);
		}
	}

	public void calculateFilterResults(final NodeModel root) {
		applyFilter(root, false, false, false);
	}

	private boolean applyFilter(final NodeModel node,
	                            final boolean hasMatchingAncestor, final boolean hasHiddenAncestor,
	                            boolean hasMatchingDescendant) {
		final boolean conditionSatisfied =  (condition == null || condition.checkNode(node));
		final boolean matchesCombinedFilter;
		if(appliesToVisibleNodesOnly) {
		    FilterInfo filterInfo = getFilterInfo(node);
            final boolean alreadyMatched = filterInfo.isMatched();
		    if(hidesMatchingNodes)
		        matchesCombinedFilter = conditionSatisfied || alreadyMatched;
		    else
		        matchesCombinedFilter = conditionSatisfied && (alreadyMatched || filterInfo.isNotChecked());
		}
		else {
		    matchesCombinedFilter = conditionSatisfied;
		}
		resetFilter(node);
		if (hasMatchingAncestor) {
			addFilterResult(node, FilterInfo.FILTER_SHOW_AS_DESCENDANT);
		}
		if (matchesCombinedFilter) {
			hasMatchingDescendant = true;
			addFilterResult(node, FilterInfo.FILTER_SHOW_AS_MATCHED);
		}
		else {
			addFilterResult(node, FilterInfo.FILTER_SHOW_AS_HIDDEN);
		}
		if (filterChildren(node, matchesCombinedFilter || hasMatchingAncestor, !matchesCombinedFilter
		        || hasHiddenAncestor)) {
		    addFilterResult(node, FilterInfo.FILTER_SHOW_AS_ANCESTOR);
			hasMatchingDescendant = true;
		}
		return hasMatchingDescendant;
	}

	/*
	 * (non-Javadoc)
	 * @see freeplane.controller.filter.Filter#areAncestorsShown()
	 */
	public boolean areAncestorsShown() {
		return 0 != (options & FilterInfo.FILTER_SHOW_AS_ANCESTOR);
	}
	
	boolean areMatchingNodesHidden() {
	    return hidesMatchingNodes;
	}


	/*
	 * (non-Javadoc)
	 * @see freeplane.controller.filter.Filter#areDescendantsShown()
	 */
	public boolean areDescendantsShown() {
		return 0 != (options & FilterInfo.FILTER_SHOW_AS_DESCENDANT);
	}

	private boolean checkNode(final NodeModel node) {
		return condition == null || ! shouldRemainInvisible(node) && condition.checkNode(node);
	}

	private boolean shouldRemainInvisible(final NodeModel node) {
		return condition != null && appliesToVisibleNodesOnly && !node.hasVisibleContent();
	}

	private boolean filterChildren(final NodeModel node,
	                               final boolean hasMatchingAncestor, final boolean hasHiddenAncestor) {
		boolean hasMatchingDescendant = false;
		for (final NodeModel child : node.getChildren()) {
			hasMatchingDescendant = applyFilter(child, hasMatchingAncestor, hasHiddenAncestor,
			    hasMatchingDescendant);
		}
		return hasMatchingDescendant;
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
		if(node.getExtension(NodeVisibility.class) == NodeVisibility.HIDDEN
				&& node.getMap().getRootNode().getExtension(NodeVisibilityConfiguration.class) != NodeVisibilityConfiguration.SHOW_HIDDEN_NODES)
			return false;
		if (condition == null || node.isRoot()) {
			return true;
		}
		FilterInfo filterInfo = getFilterInfo(node);
        return filterInfo.isNotChecked() || filterInfo.matches(this.options) != hidesMatchingNodes;
	}
	private void refreshMap(Object source, MapModel map) {
		Controller.getCurrentModeController().getMapController().fireMapChanged(new MapChangeEvent(source, map, Filter.class, null, this, false));
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
