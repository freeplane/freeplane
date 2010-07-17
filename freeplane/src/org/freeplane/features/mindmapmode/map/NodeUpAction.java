/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is modified by Dimitry Polivaev in 2008.
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
package org.freeplane.features.mindmapmode.map;

import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.util.Vector;
import java.util.logging.Logger;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.controller.IMapSelection;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.undo.IActor;
import org.freeplane.features.common.map.NodeModel;
import org.freeplane.features.common.styles.MapStyleModel;
import org.freeplane.features.common.styles.MapViewLayout;

class NodeUpAction extends AFreeplaneAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public NodeUpAction(final Controller controller) {
		super("NodeUpAction", controller);
	}

	public void _moveNodes(final NodeModel selected, final List<NodeModel> selecteds, final int direction) {
		final Comparator<Object> comparator = (direction == -1) ? null : new Comparator<Object>() {
			public int compare(final Object o1, final Object o2) {
				final int i1 = ((Integer) o1).intValue();
				final int i2 = ((Integer) o2).intValue();
				return i2 - i1;
			}
		};
		if (!selected.isRoot()) {
			final NodeModel parent = selected.getParentNode();
			final Vector<NodeModel> sortedChildren = getSortedSiblings(parent);
			final TreeSet<Integer> range = new TreeSet<Integer>(comparator);
			for (final NodeModel node : selecteds) {
				if (node.getParent() != parent) {
					Logger.global.warning("Not all selected nodes have the same parent.");
					return;
				}
				range.add(new Integer(sortedChildren.indexOf(node)));
			}
			Integer last = range.iterator().next();
			for (final Integer newInt : range) {
				if (Math.abs(newInt.intValue() - last.intValue()) > 1) {
					Logger.global.warning("Not adjacent nodes. Skipped. ");
					return;
				}
				last = newInt;
			}
			for (final Integer position : range) {
				final NodeModel node = sortedChildren.get(position.intValue());
				moveNodeTo(node, direction);
			}
			final IMapSelection selection = getController().getSelection();
			selection.selectAsTheOnlyOneSelected(selected);
			for (final Integer position : range) {
				final NodeModel node = sortedChildren.get(position.intValue());
				selection.makeTheSelected(node);
			}
			getController().getViewController().obtainFocusForSelected();
		}
	}

	public void actionPerformed(final ActionEvent e) {
		moveNodes(getModeController().getMapController().getSelectedNode(), getModeController().getMapController()
		    .getSelectedNodes(), -1);
	}

	/**
	 * Sorts nodes by their left/right status. The left are first.
	 */
	private Vector<NodeModel> getSortedSiblings(final NodeModel node) {
		final Vector<NodeModel> nodes = new Vector<NodeModel>();
		for (final Iterator<NodeModel> i = getModeController().getMapController().childrenUnfolded(node); i.hasNext();) {
			nodes.add(i.next());
		}
		if(! node.isRoot()){
			return nodes;
		}
		final MapStyleModel mapStyleModel = MapStyleModel.getExtension(node.getMap());
		MapViewLayout layoutType = mapStyleModel.getMapViewLayout();
		if(layoutType.equals(MapViewLayout.OUTLINE)){
			return nodes;
		}

		Collections.sort(nodes, new Comparator<Object>() {
			public int compare(final Object o1, final Object o2) {
				if (o1 instanceof NodeModel) {
					final NodeModel n1 = (NodeModel) o1;
					if (o2 instanceof NodeModel) {
						final NodeModel n2 = (NodeModel) o2;
						final int b1 = n1.isLeft() ? 0 : 1;
						final int b2 = n2.isLeft() ? 0 : 1;
						return b1 - b2;
					}
				}
				throw new IllegalArgumentException("Elements in LeftRightComparator are not comparable.");
			}
		});
		return nodes;
	}

	/**
	 */
	public void moveNodes(final NodeModel selected, final List<NodeModel> selecteds, final int direction) {
		final IActor actor = new IActor() {
			public void act() {
				_moveNodes(selected, selecteds, direction);
			}

			public String getDescription() {
				return "moveNodes";
			}

			public void undo() {
				_moveNodes(selected, selecteds, -direction);
			}
		};
		getModeController().execute(actor, selected.getMap());
	}

	private int moveNodeTo(final NodeModel child, final int direction) {
		final NodeModel parent = child.getParentNode();
		final int index = parent.getIndex(child);
		int newIndex = index;
		final int maxIndex = parent.getChildCount();
		final Vector<NodeModel> sortedNodesIndices = getSortedSiblings(parent);
		int newPositionInVector = sortedNodesIndices.indexOf(child) + direction;
		if (newPositionInVector < 0) {
			newPositionInVector = maxIndex - 1;
		}
		if (newPositionInVector >= maxIndex) {
			newPositionInVector = 0;
		}
		final NodeModel destinationNode = (NodeModel) sortedNodesIndices.get(newPositionInVector);
		newIndex = parent.getIndex(destinationNode);
		((MMapController) getModeController().getMapController()).moveNodeToWithoutUndo(child, parent, newIndex, false,
		    false);
		return newIndex;
	}
}
