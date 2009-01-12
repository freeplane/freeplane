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
package org.freeplane.view.swing.addins.mindmapmode.nodehistory;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.Map;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.model.NodeModel;
import org.freeplane.view.swing.map.MapView;
import org.freeplane.view.swing.map.NodeView;



/**
 * @author Dimitry Polivaev
 * 13.12.2008
 */
class NodeHolder {
	public WeakReference<MapView> mMapView;
	public String mNodeId;

	public NodeHolder(final NodeView pNode) {
		mNodeId = pNode.getModel().createID();
		final MapView mapView = pNode.getMap();
		mMapView = new WeakReference(mapView);
	}

	@Override
	public boolean equals(final Object obj) {
		if (!(obj instanceof NodeHolder)) {
			return false;
		}
		final NodeHolder nodeHolder = (NodeHolder) obj;
		return nodeHolder.mMapView.get() == mMapView.get() && nodeHolder.mNodeId.equals(mNodeId);
	}

	MapView getMapView() {
		final MapView mapView = mMapView.get();
		final Map mapViews = Controller.getController().getMapViewManager().getMaps();
		for (final Iterator iter = mapViews.values().iterator(); iter.hasNext();) {
			final MapView m = (MapView) iter.next();
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
			modeController = mapView.getModel().getModeController();
		}
		return modeController;
	}

	/** @return null, if node not found. */
	public NodeModel getNode() {
		final MapView modeController = mMapView.get();
		if (modeController != null) {
			return modeController.getModel().getNodeForID(mNodeId);
		}
		return null;
	}

	@Override
	public int hashCode() {
		final MapView mapView = mMapView.get();
		return mapView != null ? mapView.hashCode() * 37 : 0 + mNodeId.hashCode();
	}

	public boolean isIdentical(final NodeView pNode) {
		final String id = pNode.getModel().createID();
		final MapView mapView = pNode.getMap();
		return mapView == mMapView.get() && id.equals(mNodeId);
	}
}
