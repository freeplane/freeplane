/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2009 Dimitry Polivaev
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
package org.freeplane.view.swing.map;

import java.awt.EventQueue;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.util.HashSet;

import org.freeplane.features.map.IMapSelectionListener;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.ui.IMapViewChangeListener;

/**
 * @author Dimitry Polivaev
 * Mar 19, 2009
 */
class MapViewChangeObserverCompound {
	final private HashSet<IMapSelectionListener> mapListeners = new HashSet<IMapSelectionListener>();
	final private HashSet<IMapViewChangeListener> viewListeners = new HashSet<IMapViewChangeListener>();

	void addListener(final IMapSelectionListener listener) {
		mapListeners.add(listener);
	}

	void addListener(final IMapViewChangeListener listener) {
		viewListeners.add(listener);
	}

	void afterMapViewChange(final MapView oldMap, final MapView newMap) {
		final MapModel oldModel = getModel(oldMap);
		final MapModel newModel = getModel(newMap);
        if (oldModel != newModel) {
            for (final IMapSelectionListener observer:mapListeners.toArray(new IMapSelectionListener[]{})) {
                observer.afterMapChange(oldModel, newModel);
            }
        }
		for (final IMapViewChangeListener observer : viewListeners.toArray(new IMapViewChangeListener[]{})) {
			observer.afterViewChange(oldMap, newMap);
		}
	}

	void afterMapViewClose(final MapView pOldMap) {
        for (final IMapViewChangeListener observer : viewListeners.toArray(new IMapViewChangeListener[]{})) {
            observer.afterViewClose(pOldMap);
        }
	}

	void beforeMapViewChange(final MapView oldMap, final MapView newMap) {
		final MapModel oldModel = getModel(oldMap);
		final MapModel newModel = getModel(newMap);
		if (oldModel != newModel) {
			for (final IMapSelectionListener observer:mapListeners.toArray(new IMapSelectionListener[]{})) {
				observer.beforeMapChange(getModel(oldMap), getModel(newMap));
			}
		}
	    for (final IMapViewChangeListener observer : viewListeners.toArray(new IMapViewChangeListener[]{})) {
	        observer.beforeViewChange(oldMap, newMap);
	    }
	}

	private MapModel getModel(final MapView view) {
		return view == null ? null : view.getModel();
	}

	void mapViewCreated(final MapView previousMapView, MapView createdMapView) {
		if(! createdMapView.isShowing())
		{
			fireMapViewCreatedAfterItIsDisplayed(previousMapView, createdMapView);
		}
		else if (!createdMapView.isLayoutCompleted()) {
			fireMapViewCreatedLater(previousMapView, createdMapView);
		}
		else {
			fireMapViewCreated(previousMapView, createdMapView);
		}
	}

	private void fireMapViewCreatedLater(final MapView previousView, MapView createdMapView) {
	    EventQueue.invokeLater(new Runnable() {
	    	public void run() {
	    		mapViewCreated(previousView, createdMapView);
	    	}
	    });
    }

	private void fireMapViewCreatedAfterItIsDisplayed(final MapView previousView, MapView createdMapView) {
		HierarchyListener retryEventListener = new HierarchyListener() {
			public void hierarchyChanged(HierarchyEvent e) {
				if (createdMapView.isShowing()) {
				    createdMapView.removeHierarchyListener(this);
					mapViewCreated(previousView, createdMapView);
				}
			}
		};
		createdMapView.addHierarchyListener(retryEventListener);
	}

	private void fireMapViewCreated(final MapView previousMapView, MapView createdMapView) {
	    for (final IMapViewChangeListener observer : viewListeners.toArray(new IMapViewChangeListener[]{})) {
			observer.afterViewCreated(previousMapView, createdMapView);
		}
    }

	void removeListener(final IMapSelectionListener listener) {
		mapListeners.remove(listener);
	}

	void removeListener(final IMapViewChangeListener listener) {
		viewListeners.remove(listener);
	}
}
