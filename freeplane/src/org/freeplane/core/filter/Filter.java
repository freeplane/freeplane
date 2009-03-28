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

import java.util.List;
import java.util.ListIterator;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.filter.condition.ICondition;
import org.freeplane.core.modecontroller.IMapSelection;
import org.freeplane.core.model.MapModel;
import org.freeplane.core.model.NodeModel;

/**
 * @author Dimitry Polivaev
 */
public class Filter {
	private ICondition condition = null;
	final private Controller controller;
	private int options = 0;

	/**
	 */
	public Filter(final Controller controller, final ICondition condition, final boolean areAnchestorsShown,
	                     final boolean areDescendantsShown) {
		super();
		this.controller = controller;
		this.condition = condition;
		options = FilterInfo.FILTER_INITIAL_VALUE | FilterInfo.FILTER_SHOW_MATCHED;
		if (areAnchestorsShown) {
			options += FilterInfo.FILTER_SHOW_ANCESTOR;
		}
		options += FilterInfo.FILTER_SHOW_ECLIPSED;
		if (areDescendantsShown) {
			options += FilterInfo.FILTER_SHOW_DESCENDANT;
		}
	}

	void addFilterResult(final NodeModel node, final int flag) {
		node.getFilterInfo().add(flag);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * freeplane.controller.filter.Filter#applyFilter(freeplane.modes.MindMap)
	 */
	public void applyFilter(final MapModel map, boolean force) {
		if(map == null){
			return;
		}
		try {
			controller.getViewController().setWaitingCursor(true);
			final Filter oldFilter = map.getFilter();
			map.setFilter(this);
			if (force || oldFilter == null || ! condition.equals(oldFilter.getCondition())) {
				final NodeModel root = map.getRootNode();
				resetFilter(root);
				if (filterChildren(root, condition.checkNode(root), false)) {
					addFilterResult(root, FilterInfo.FILTER_SHOW_ANCESTOR);
				}
			}
			refreshMap();
			selectVisibleNode();
		}
		finally {
			controller.getViewController().setWaitingCursor(false);
		}
	}

	private void refreshMap() {
		controller.getModeController().getMapController().refreshMap();
	}
	private void selectVisibleNode() {
		final IMapSelection mapSelection = controller.getSelection();
		final List<NodeModel> selectedNodes = mapSelection.getSelection();
		final int lastSelectedIndex = selectedNodes.size() - 1;
		if (lastSelectedIndex == -1) {
			return;
		}
		final ListIterator<NodeModel> iterator = selectedNodes.listIterator(lastSelectedIndex);
		while (iterator.hasPrevious()) {
			final NodeModel previous = iterator.previous();
			if (!previous.isVisible()) {
				mapSelection.toggleSelected(previous);
			}
		}
		NodeModel selected = mapSelection.getSelected();
		if (!selected.isVisible()) {
			selected = getNearestVisibleParent(selected);
			mapSelection.selectAsTheOnlyOneSelected(selected);
		}
		mapSelection.setSiblingMaxLevel(selected.getNodeLevel(false));
	}

	private NodeModel getNearestVisibleParent(final NodeModel selectedNode) {
		if (selectedNode.isVisible()) {
			return selectedNode;
		}
		return getNearestVisibleParent(selectedNode.getParentNode());
	}

	private boolean applyFilter(final NodeModel node, final boolean isAncestorSelected,
	                            final boolean isAncestorEclipsed, boolean isDescendantSelected) {
		final boolean conditionSatisfied = condition.checkNode(node);
		resetFilter(node);
		if (isAncestorSelected) {
			addFilterResult(node, FilterInfo.FILTER_SHOW_DESCENDANT);
		}
		if (conditionSatisfied) {
			isDescendantSelected = true;
			addFilterResult(node, FilterInfo.FILTER_SHOW_MATCHED);
		}
		else {
			addFilterResult(node, FilterInfo.FILTER_SHOW_HIDDEN);
		}
		if (isAncestorEclipsed) {
			addFilterResult(node, FilterInfo.FILTER_SHOW_ECLIPSED);
		}
		if (filterChildren(node, conditionSatisfied || isAncestorSelected, !conditionSatisfied || isAncestorEclipsed)) {
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

	/**
	 * @param c
	 */
	private boolean filterChildren(final NodeModel parent, final boolean isAncestorSelected,
	                               final boolean isAncestorEclipsed) {
		final ListIterator iterator = controller.getModeController().getMapController().childrenUnfolded(parent);
		boolean isDescendantSelected = false;
		while (iterator.hasNext()) {
			final NodeModel node = (NodeModel) iterator.next();
			isDescendantSelected = applyFilter(node, isAncestorSelected, isAncestorEclipsed, isDescendantSelected);
		}
		return isDescendantSelected;
	}

	public Object getCondition() {
		return condition;
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
		final int filterResult = node.getFilterInfo().get();
		return ((options & FilterInfo.FILTER_SHOW_ANCESTOR) != 0 || (options & FilterInfo.FILTER_SHOW_ECLIPSED) >= (filterResult & FilterInfo.FILTER_SHOW_ECLIPSED))
		        && ((options & filterResult & ~FilterInfo.FILTER_SHOW_ECLIPSED) != 0);
	}

	private void resetFilter(final NodeModel node) {
		node.getFilterInfo().reset();
	}

}
