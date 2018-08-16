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
package org.freeplane.view.swing.features.nodehistory;

import java.lang.ref.WeakReference;

import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.view.swing.map.MapView;
import org.freeplane.view.swing.map.MapViewController;
import org.freeplane.view.swing.map.NodeView;

/**
 * @author Dimitry Polivaev
 * 13.12.2008
 */
class NodeHolder {
	private final WeakReference<MapView> mMapView;
	private final String mNodeId;
	private boolean reachedByLink;

	public NodeHolder(final NodeView pNode) {
		mNodeId = pNode.getModel().createID();
		final MapView mapView = pNode.getMap();
		mMapView = new WeakReference<MapView>(mapView);
		reachedByLink = false;
	}

	@Override
	public boolean equals(final Object obj) {
		if (!(obj instanceof NodeHolder)) {
			return false;
		}
		final NodeHolder nodeHolder = (NodeHolder) obj;
		return nodeHolder.mMapView.get() == mMapView.get() && nodeHolder.mNodeId.equals(mNodeId);
	}

	public MapView getHoldMapView() {
		return mMapView.get();
	}

	MapView getMapView() {
		final MapView mapView = mMapView.get();
		final Controller controller = mapView.getModeController().getController();
		final MapViewController mapViewManager = (MapViewController) controller.getMapViewManager();
		for (final MapView m : mapViewManager.getMapViewVector()) {
			if (m == mapView) {
				return mapView;
			}
		}
		return null;
	}

	ModeController getModeController() {
		ModeController modeController = null;
		final MapView mapView = getMapView();
		if (mapView != null) {
			modeController = mapView.getModeController();
		}
		return modeController;
	}

	/** @return null, if node not found. */
	public NodeModel getNode() {
		final MapView modeController = mMapView.get();
		if (modeController != null) {
			return modeController.getModel().getNodeForID_(mNodeId);
		}
		return null;
	}

	@Override
	public int hashCode() {
		final MapView mapView = mMapView.get();
		return mapView != null ? mapView.hashCode() * 37 : 0 + mNodeId.hashCode();
	}

	boolean isIdentical(final NodeView pNode) {
		if(pNode == null || pNode.getModel() == null) return false;
		final String id = pNode.getModel().createID();
		final MapView mapView = pNode.getMap();
		return mapView == mMapView.get() && id.equals(mNodeId);
	}

	protected boolean isReachedByLink() {
		return reachedByLink;
	}

	protected void setReachedByLink(final boolean reachedByLink) {
		this.reachedByLink = reachedByLink;
	}
}
