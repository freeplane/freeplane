/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
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
package org.freeplane.view.swing.addins.nodehistory;

import java.util.LinkedList;
import java.util.ListIterator;
import java.util.logging.Logger;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.extension.IExtension;
import org.freeplane.core.frame.IMapViewManager;
import org.freeplane.core.modecontroller.INodeSelectionListener;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.features.common.link.LinkController;
import org.freeplane.view.swing.map.MapView;

/**
 * @author foltin
 */
public class NodeHistory implements IExtension {
	static public void install(final Controller controller) {
		controller.addExtension(NodeHistory.class, new NodeHistory(controller));
	}

	static public void install(final ModeController modeController) {
		final Controller controller = modeController.getController();
		final NodeHistory history = (NodeHistory) controller.getExtension(NodeHistory.class);
		modeController.getMapController().addNodeSelectionListener(history.getMapSelectionListener());
		LinkController.getController(modeController).addNodeSelectionListener(history.getLinkSelectionListener());
		final MenuBuilder menuBuilder = modeController.getUserInputListenerFactory().getMenuBuilder();
		history.backAction = new BackAction(controller, history);
		menuBuilder.addAnnotatedAction(history.backAction);
		modeController.addAction(history.backAction);
		history.forwardAction = new ForwardAction(controller, history);
		menuBuilder.addAnnotatedAction(history.forwardAction);
		modeController.addAction(history.forwardAction);
	}

	private BackAction backAction;
	final private Controller controller;
	private NodeHolder currentNodeHolder;
	private ForwardAction forwardAction;
	private ListIterator<NodeHolder> nodeIterator;
	private final LinkedList<NodeHolder> nodes;

	private NodeHistory(final Controller controller) {
		this.controller = controller;
		nodes = new LinkedList<NodeHolder>();
		nodeIterator = nodes.listIterator();
	}

	boolean canGoBack() {
		return nodeIterator.previousIndex() > 0;
	}

	boolean canGoForward() {
		return nodeIterator.hasNext();
	}

	private INodeSelectionListener getLinkSelectionListener() {
		return new INodeSelectionListener() {
			public void onDeselect(final NodeModel node) {
				onNodeSelect(node);
				currentNodeHolder.setReachedByLink(true);
			}

			public void onSelect(final NodeModel node) {
				onNodeSelect(node);
				currentNodeHolder.setReachedByLink(true);
			}
		};
	}

	private INodeSelectionListener getMapSelectionListener() {
		return new INodeSelectionListener() {
			public void onDeselect(final NodeModel node) {
			}

			public void onSelect(final NodeModel node) {
				onNodeSelect(node);
			}
		};
	}

	private void go(final boolean back) {
		final NodeHolder lastNodeHolder = currentNodeHolder;
		if (back) {
			if (canGoBack()) {
				nodeIterator.previous();
				nodeIterator.previous();
				currentNodeHolder = nodeIterator.next();
			}
			else {
				backAction.setEnabled(false);
				return;
			}
		}
		else {
			if (canGoForward()) {
				currentNodeHolder = nodeIterator.next();
			}
			else {
				forwardAction.setEnabled(false);
				return;
			}
		}
		if (lastNodeHolder.equals(currentNodeHolder)) {
			go(back);
			return;
		}
		final NodeModel toBeSelected = currentNodeHolder.getNode();
		if(removed(toBeSelected)){
			currentNodeHolder = lastNodeHolder;
			go(back);
			return;
		}
		boolean changeModule = false;
		MapView newModule = null;
		if (lastNodeHolder.getHoldMapView() != currentNodeHolder.getHoldMapView()) {
			changeModule = true;
			newModule = currentNodeHolder.getMapView();
			if (newModule == null) {
				nodeIterator.remove();
				go(back);
				return;
			}
		}
		final boolean fChangeModule = changeModule;
		final MapView newView;
		if (fChangeModule) {
			newView = newModule;
			final Controller controller = newView.getModeController().getController();
			final IMapViewManager mapViewManager = controller.getMapViewManager();
			final boolean res = mapViewManager.changeToMapView(newView);
			if (!res) {
				Logger.global.warning("Can't change to map mapView " + newView);
				return;
			}
		}
		else {
			newView = currentNodeHolder.getHoldMapView();
		}
		if (!toBeSelected.isRoot()) {
			newView.getModeController().getMapController().setFolded(toBeSelected.getParentNode(), false);
		}
		newView.getModeController().getMapController().select(toBeSelected);
	}

	private boolean removed(final NodeModel toBeSelected) {
	    if (toBeSelected == null) {
	    	return true;
	    }
	    if(toBeSelected.isRoot()){
	    	return false;
	    }
	    return removed(toBeSelected.getParentNode());
    }

	private void go(final boolean back, final boolean fast) {
		NodeHolder lastCurrentNodeHolder;
		do {
			lastCurrentNodeHolder = currentNodeHolder;
			go(back);
		} while (fast && lastCurrentNodeHolder != currentNodeHolder && !currentNodeHolder.isReachedByLink());
	}

	public void goBack(final boolean fast) {
		go(true, fast);
	}

	public void goForward(final boolean fast) {
		go(false, fast);
	}

	private void onNodeSelect(final NodeModel pNode) {
		if (currentNodeHolder != null
		        && currentNodeHolder.isIdentical(((MapView) controller.getViewController().getMapView())
		            .getNodeView(pNode))) {
			return;
		}
		while (canGoForward()) {
			nodeIterator.next();
			nodeIterator.remove();
		}
		if (nodes.size() > 100) {
			nodes.removeFirst();
			nodeIterator = nodes.listIterator(nodes.size());
		}
		currentNodeHolder = new NodeHolder(((MapView) controller.getViewController().getMapView()).getNodeView(pNode));
		nodeIterator.add(currentNodeHolder);
	}
}
