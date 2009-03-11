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
import org.freeplane.view.swing.map.MapView;

/**
 * @author foltin
 */
public class NodeHistory implements IExtension, INodeSelectionListener {
	private NodeHolder currentNodeHolder;
	final private Controller controller;
	private ListIterator<NodeHolder> nodeIterator;
	private final LinkedList<NodeHolder> nodes;

	static public void install(final Controller controller){
		controller.putExtension(NodeHistory.class, new NodeHistory(controller));
	}
	
	private NodeHistory(final Controller controller) {
		this.controller = controller;
		nodes = new LinkedList<NodeHolder>();
		nodeIterator = nodes.listIterator();
	}

	static public void install(ModeController modeController){
		final Controller controller = modeController.getController();
		NodeHistory history = (NodeHistory) controller.getExtension(NodeHistory.class);
		modeController.getMapController().addNodeSelectionListener(history);
		final MenuBuilder menuBuilder = modeController.getUserInputListenerFactory().getMenuBuilder();
		final BackAction backAction = new BackAction(controller, history);
		menuBuilder.addAnnotatedAction(backAction);
		modeController.addAnnotatedAction(backAction);
		final ForwardAction forwardAction = new ForwardAction(controller, history);
		menuBuilder.addAnnotatedAction(forwardAction);
		modeController.addAnnotatedAction(forwardAction);
	}
	
	boolean canGoBack() {
		return nodeIterator.previousIndex() > 0;
	}

	boolean canGoForward() {
		return nodeIterator.hasNext();
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
				return;
			}
		}
		else {
			if (canGoForward()) {
				currentNodeHolder = nodeIterator.next();
			}
			else {
				return;
			}
		}
		if (lastNodeHolder.equals(currentNodeHolder)) {
			go(back);
			return;
		}
		final NodeModel toBeSelected = currentNodeHolder.getNode();
		boolean changeModule = false;
		MapView newModule = null;
		if (lastNodeHolder.mMapView.get() != currentNodeHolder.mMapView.get()) {
			changeModule = true;
			newModule = currentNodeHolder.getMapView();
			if (newModule == null) {
				nodeIterator.remove();
				go(back);
				return;
			}
		}
		final boolean fChangeModule = changeModule;
		final MapView newView = newModule;
		if (fChangeModule) {
			final Controller controller = newView.getModeController().getController();
			final IMapViewManager mapViewManager = controller.getMapViewManager();
			final boolean res = mapViewManager.changeToMapView(newView);
			if (!res) {
				Logger.global.warning("Can't change to map mapView " + newView);
				return;
			}
		}
		if (!toBeSelected.isRoot()) {
			newView.getModeController().getMapController().setFolded(toBeSelected.getParentNode(), false);
		}
		newView.getModeController().getMapController().select(toBeSelected);
	}

	public void goBack() {
		go(true);
	}

	public void goForward() {
		go(false);
	}

	public void onDeselect(final NodeModel pNode) {
	}

	public void onSelect(final NodeModel pNode) {
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
