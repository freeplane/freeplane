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
import org.freeplane.core.map.MapModel;
import org.freeplane.core.map.NodeModel;
import org.freeplane.view.swing.map.MapView;
import org.freeplane.view.swing.map.NodeView;

/**
 * @author Dimitry Polivaev
 */
public class DefaultFilter implements IFilter {
	static void addFilterResult(final NodeModel node, final int flag) {
		node.getFilterInfo().add(flag);
	}

	static private NodeView getNearestVisibleParent(final NodeView selectedNode) {
		if (selectedNode.getModel().isVisible()) {
			return selectedNode;
		}
		return DefaultFilter.getNearestVisibleParent(selectedNode.getParentView());
	}

	static public void resetFilter(final NodeModel node) {
		node.getFilterInfo().reset();
	}

	static public void selectVisibleNode(final MapView mapView) {
		final List<NodeView> selectedNodes = mapView.cloneSelection();
		final int lastSelectedIndex = selectedNodes.size() - 1;
		if (lastSelectedIndex == -1) {
			return;
		}
		final ListIterator<NodeView> iterator = selectedNodes.listIterator(lastSelectedIndex);
		while (iterator.hasPrevious()) {
			final NodeView previous = iterator.previous();
			if (!previous.getModel().isVisible()) {
				mapView.toggleSelected(previous);
			}
		}
		NodeView selected = mapView.getSelected();
		if (!selected.getModel().isVisible()) {
			selected = DefaultFilter.getNearestVisibleParent(selected);
			mapView.selectAsTheOnlyOneSelected(selected);
		}
		mapView.setSiblingMaxLevel(selected.getModel().getNodeLevel());
	}

	private ICondition condition = null;
	private int options = 0;

	/**
	 */
	public DefaultFilter(final ICondition condition, final boolean areAnchestorsShown,
	                     final boolean areDescendantsShown) {
		super();
		this.condition = condition;
		options = IFilter.FILTER_INITIAL_VALUE | IFilter.FILTER_SHOW_MATCHED;
		if (areAnchestorsShown) {
			options += IFilter.FILTER_SHOW_ANCESTOR;
		}
		options += IFilter.FILTER_SHOW_ECLIPSED;
		if (areDescendantsShown) {
			options += IFilter.FILTER_SHOW_DESCENDANT;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * freemind.controller.filter.Filter#applyFilter(freemind.modes.MindMap)
	 */
	public void applyFilter() {
		if (condition != null) {
			try {
				final Controller c = Controller.getController();
				c.getViewController().setWaitingCursor(true);
				final MapModel map = c.getModel();
				final MapView mapView = c.getMapView();
				final NodeModel root = map.getRootNode();
				DefaultFilter.resetFilter(root);
				if (filterChildren(root, condition.checkNode(root), false)) {
					DefaultFilter.addFilterResult(root, IFilter.FILTER_SHOW_ANCESTOR);
				}
				DefaultFilter.selectVisibleNode(mapView);
			}
			finally {
				Controller.getController().getViewController().setWaitingCursor(false);
			}
		}
	}

	private boolean applyFilter(final NodeModel node, final boolean isAncestorSelected,
	                            final boolean isAncestorEclipsed, boolean isDescendantSelected) {
		DefaultFilter.resetFilter(node);
		if (isAncestorSelected) {
			DefaultFilter.addFilterResult(node, IFilter.FILTER_SHOW_DESCENDANT);
		}
		final boolean conditionSatisfied = condition.checkNode(node);
		if (conditionSatisfied) {
			isDescendantSelected = true;
			DefaultFilter.addFilterResult(node, IFilter.FILTER_SHOW_MATCHED);
		}
		else {
			DefaultFilter.addFilterResult(node, IFilter.FILTER_SHOW_HIDDEN);
		}
		if (isAncestorEclipsed) {
			DefaultFilter.addFilterResult(node, IFilter.FILTER_SHOW_ECLIPSED);
		}
		if (filterChildren(node, conditionSatisfied || isAncestorSelected, !conditionSatisfied
		        || isAncestorEclipsed)) {
			DefaultFilter.addFilterResult(node, IFilter.FILTER_SHOW_ANCESTOR);
			isDescendantSelected = true;
		}
		return isDescendantSelected;
	}

	/*
	 * (non-Javadoc)
	 * @see freemind.controller.filter.Filter#areAncestorsShown()
	 */
	public boolean areAncestorsShown() {
		return 0 != (options & IFilter.FILTER_SHOW_ANCESTOR);
	}

	/*
	 * (non-Javadoc)
	 * @see freemind.controller.filter.Filter#areDescendantsShown()
	 */
	public boolean areDescendantsShown() {
		return 0 != (options & IFilter.FILTER_SHOW_DESCENDANT);
	}

	/*
	 * (non-Javadoc)
	 * @see freemind.controller.filter.Filter#areEclipsedShown()
	 */
	public boolean areEclipsedShown() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see freemind.controller.filter.Filter#areHiddenShown()
	 */
	public boolean areHiddenShown() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see freemind.controller.filter.Filter#areMatchedShown()
	 */
	public boolean areMatchedShown() {
		return true;
	}

	/**
	 * @param c
	 */
	private boolean filterChildren(final NodeModel parent, final boolean isAncestorSelected,
	                               final boolean isAncestorEclipsed) {
		final ListIterator iterator = parent.getModeController().getMapController()
		    .childrenUnfolded(parent);
		boolean isDescendantSelected = false;
		while (iterator.hasNext()) {
			final NodeModel node = (NodeModel) iterator.next();
			isDescendantSelected = applyFilter(node, isAncestorSelected, isAncestorEclipsed,
			    isDescendantSelected);
		}
		return isDescendantSelected;
	}

	public Object getCondition() {
		return condition;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * freemind.controller.filter.Filter#isVisible(freemind.modes.MindMapNode)
	 */
	public boolean isVisible(final NodeModel node) {
		if (condition == null) {
			return true;
		}
		final int filterResult = node.getFilterInfo().get();
		return ((options & IFilter.FILTER_SHOW_ANCESTOR) != 0 || (options & IFilter.FILTER_SHOW_ECLIPSED) >= (filterResult & IFilter.FILTER_SHOW_ECLIPSED))
		        && ((options & filterResult & ~IFilter.FILTER_SHOW_ECLIPSED) != 0);
	}
}
