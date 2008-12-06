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
package org.freeplane.map.tree.mindmapmode;

import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.util.Vector;
import java.util.logging.Logger;

import javax.swing.Action;

import org.freeplane.controller.Freeplane;
import org.freeplane.map.tree.MapModel;
import org.freeplane.map.tree.NodeModel;
import org.freeplane.map.tree.view.MapView;
import org.freeplane.map.tree.view.NodeView;
import org.freeplane.modes.ModeControllerAction;
import org.freeplane.modes.mindmapmode.MModeController;

import deprecated.freemind.modes.mindmapmode.actions.instance.ActionInstance;
import deprecated.freemind.modes.mindmapmode.actions.instance.MoveNodesActionInstance;
import deprecated.freemind.modes.mindmapmode.actions.instance.NodeListMemberActionInstance;
import deprecated.freemind.modes.mindmapmode.actions.undo.ActionPair;
import deprecated.freemind.modes.mindmapmode.actions.undo.IActor;

class NodeUpAction extends ModeControllerAction implements IActor {
	public NodeUpAction(final MModeController modeController) {
		super(modeController, "node_up");
		modeController.getActionFactory().registerActor(this,
		    getDoActionClass());
	}

	public void _moveNodes(final NodeModel selected, final List selecteds,
	                       final int direction) {
		final Comparator comparator = (direction == -1) ? null
		        : new Comparator() {
			        public int compare(final Object o1, final Object o2) {
				        final int i1 = ((Integer) o1).intValue();
				        final int i2 = ((Integer) o2).intValue();
				        return i2 - i1;
			        }
		        };
		if (!selected.isRoot()) {
			final NodeModel parent = selected.getParentNode();
			final Vector sortedChildren = getSortedSiblings(parent);
			final TreeSet range = new TreeSet(comparator);
			for (final Iterator i = selecteds.iterator(); i.hasNext();) {
				final NodeModel node = (NodeModel) i.next();
				if (node.getParent() != parent) {
					Logger.global
					    .warning("Not all selected nodes have the same parent.");
					return;
				}
				range.add(new Integer(sortedChildren.indexOf(node)));
			}
			Integer last = (Integer) range.iterator().next();
			for (final Iterator i = range.iterator(); i.hasNext();) {
				final Integer newInt = (Integer) i.next();
				if (Math.abs(newInt.intValue() - last.intValue()) > 1) {
					Logger.global.warning("Not adjacent nodes. Skipped. ");
					return;
				}
				last = newInt;
			}
			for (final Iterator i = range.iterator(); i.hasNext();) {
				final Integer position = (Integer) i.next();
				final NodeModel node = (NodeModel) sortedChildren.get(position
				    .intValue());
				moveNodeTo(node, parent, direction);
			}
			final MapView mapView = getModeController().getMapView();
			final NodeView selectedNodeView = mapView.getNodeView(selected);
			mapView.selectAsTheOnlyOneSelected(selectedNodeView);
			mapView.scrollNodeToVisible(selectedNodeView);
			for (final Iterator i = range.iterator(); i.hasNext();) {
				final Integer position = (Integer) i.next();
				final NodeModel node = (NodeModel) sortedChildren.get(position
				    .intValue());
				final NodeView nodeView = mapView.getNodeView(node);
				mapView.makeTheSelected(nodeView);
			}
			Freeplane.getController().getViewController()
			    .obtainFocusForSelected();
		}
	}

	public void act(final ActionInstance action) {
		if (action instanceof MoveNodesActionInstance) {
			final MoveNodesActionInstance moveAction = (MoveNodesActionInstance) action;
			final NodeModel selected = getModeController().getMapController()
			    .getNodeFromID(moveAction.getNode());
			final Vector selecteds = new Vector();
			for (final Iterator i = moveAction.getListNodeListMemberList()
			    .iterator(); i.hasNext();) {
				final NodeListMemberActionInstance node = (NodeListMemberActionInstance) i
				    .next();
				selecteds.add(getModeController().getMapController()
				    .getNodeFromID(node.getNode()));
			}
			_moveNodes(selected, selecteds, moveAction.getDirection());
		}
	}

	public void actionPerformed(final ActionEvent e) {
		moveNodes(getModeController().getSelectedNode(), getModeController()
		    .getSelectedNodes(), -1);
	}

	private MoveNodesActionInstance createMoveNodesAction(
	                                                      final NodeModel selected,
	                                                      final List selecteds,
	                                                      final int direction) {
		final MoveNodesActionInstance moveAction = new MoveNodesActionInstance();
		moveAction.setDirection(direction);
		moveAction.setNode(selected.createID());
		for (final Iterator i = selecteds.iterator(); i.hasNext();) {
			final NodeModel node = (NodeModel) i.next();
			final NodeListMemberActionInstance nodeListMember = new NodeListMemberActionInstance();
			nodeListMember.setNode(node.createID());
			moveAction.addNodeListMember(nodeListMember);
		}
		return moveAction;
	}

	public Class getDoActionClass() {
		return MoveNodesActionInstance.class;
	}

	/**
	 * Sorts nodes by their left/right status. The left are first.
	 */
	private Vector getSortedSiblings(final NodeModel node) {
		final Vector nodes = new Vector();
		for (final Iterator i = node.getModeController().getMapController()
		    .childrenUnfolded(node); i.hasNext();) {
			nodes.add(i.next());
		}
		Collections.sort(nodes, new Comparator() {
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
				throw new IllegalArgumentException(
				    "Elements in LeftRightComparator are not comparable.");
			}
		});
		return nodes;
	}

	/**
	 */
	public void moveNodes(final NodeModel selected, final List selecteds,
	                      final int direction) {
		final MoveNodesActionInstance doAction = createMoveNodesAction(
		    selected, selecteds, direction);
		final MoveNodesActionInstance undoAction = createMoveNodesAction(
		    selected, selecteds, -direction);
		getMModeController().getActionFactory().startTransaction(
		    (String) getValue(Action.NAME));
		getMModeController().getActionFactory().executeAction(
		    new ActionPair(doAction, undoAction));
		getMModeController().getActionFactory().endTransaction(
		    (String) getValue(Action.NAME));
	}

	/**
	 * The direction is used if side left and right are present. then the next
	 * suitable place on the same side# is searched. if there is no such place,
	 * then the side is changed.
	 *
	 * @return returns the new index.
	 */
	public int moveNodeTo(final NodeModel child, final NodeModel newParent,
	                      final int direction) {
		final MapModel map = Freeplane.getController().getMap();
		final int index = map.getIndexOfChild(newParent, child);
		int newIndex = index;
		final int maxIndex = newParent.getChildCount();
		final Vector sortedNodesIndices = getSortedSiblings(newParent);
		int newPositionInVector = sortedNodesIndices.indexOf(child) + direction;
		if (newPositionInVector < 0) {
			newPositionInVector = maxIndex - 1;
		}
		if (newPositionInVector >= maxIndex) {
			newPositionInVector = 0;
		}
		final NodeModel destinationNode = (NodeModel) sortedNodesIndices
		    .get(newPositionInVector);
		newIndex = map.getIndexOfChild(newParent, destinationNode);
		((MMapController) map.getModeController().getMapController())
		    .moveNodeToWithoutUndo(child, newParent, newIndex);
		return newIndex;
	}
}
