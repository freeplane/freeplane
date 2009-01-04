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
package org.freeplane.features.mindmapmode.addins.nodehistory;

import java.util.LinkedList;
import java.util.ListIterator;
import java.util.logging.Logger;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.extension.IExtension;
import org.freeplane.core.frame.MapViewManager;
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
	final private ModeController modeController;
	private ListIterator<NodeHolder> nodeIterator;
	private final LinkedList<NodeHolder> nodes;

	public NodeHistory(final ModeController modeController) {
		this.modeController = modeController;
		nodes = new LinkedList<NodeHolder>();
		nodeIterator = nodes.listIterator();
		modeController.getMapController().addNodeSelectionListener(this);
		final MenuBuilder menuBuilder = modeController.getUserInputListenerFactory()
		    .getMenuBuilder();
		final BackAction backAction = new BackAction(this);
		menuBuilder.addAnnotatedAction(backAction);
		modeController.addAnnotatedAction(backAction);
		final ForwardAction forwardAction = new ForwardAction(this);
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
		final MapView fNewModule = newModule;
		if (fChangeModule) {
			final MapViewManager mapViewManager = Controller.getController().getMapViewManager();
			final boolean res = mapViewManager.changeToMapView(fNewModule);
			if (!res) {
				Logger.global.warning("Can't change to map mapView " + fNewModule);
				return;
			}
		}
		if (!toBeSelected.isRoot()) {
			modeController.getMapController().setFolded(toBeSelected.getParentNode(), false);
		}
		modeController.getMapController().select(toBeSelected);
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
		if (currentNodeHolder != null && currentNodeHolder.isIdentical(Controller.getController().getMapView().getNodeView(pNode))) {
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
		currentNodeHolder = new NodeHolder(Controller.getController().getMapView().getNodeView(pNode));
		nodeIterator.add(currentNodeHolder);
	}

	public void register() {
		modeController.getMapController().addNodeSelectionListener(this);
	}
}
