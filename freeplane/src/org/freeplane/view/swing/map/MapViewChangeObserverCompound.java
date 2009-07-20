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

import java.util.HashSet;
import java.util.Iterator;

import org.freeplane.core.frame.IMapSelectionListener;
import org.freeplane.core.frame.IMapViewChangeListener;
import org.freeplane.core.model.MapModel;

/**
 * @author Dimitry Polivaev
 * Mar 19, 2009
 */
class MapViewChangeObserverCompound {
	final private HashSet<IMapSelectionListener> mapListeners = new HashSet();
	final private HashSet<IMapViewChangeListener> viewListeners = new HashSet();

	void addListener(final IMapSelectionListener listener) {
		mapListeners.add(listener);
	}

	void addListener(final IMapViewChangeListener listener) {
		viewListeners.add(listener);
	}

	void afterMapViewChange(final MapView oldMap, final MapView newMap) {
		final MapModel oldModel = getModel(oldMap);
		final MapModel newModel = getModel(newMap);
		for (final Iterator<IMapSelectionListener> iter = mapListeners.iterator(); iter.hasNext();) {
			final IMapSelectionListener observer = iter.next();
			if (oldModel != newModel) {
				observer.afterMapChange(oldModel, newModel);
			}
		}
		for (final IMapViewChangeListener observer : viewListeners) {
			observer.afterViewChange(oldMap, newMap);
		}
	}

	void afterMapViewClose(final MapView pOldMap) {
		for (final Iterator<IMapSelectionListener> iter = mapListeners.iterator(); iter.hasNext();) {
			final IMapSelectionListener observer = iter.next();
			observer.afterMapClose(getModel(pOldMap));
		}
		for (final Iterator<IMapViewChangeListener> iter = viewListeners.iterator(); iter.hasNext();) {
			final IMapViewChangeListener observer = iter.next();
			observer.afterViewClose(pOldMap);
		}
	}

	void beforeMapViewChange(final MapView oldMap, final MapView newMap) {
		for (final Iterator<IMapSelectionListener> iter = mapListeners.iterator(); iter.hasNext();) {
			final IMapSelectionListener observer = iter.next();
			observer.beforeMapChange(getModel(oldMap), getModel(newMap));
		}
		for (final Iterator<IMapViewChangeListener> iter = viewListeners.iterator(); iter.hasNext();) {
			final IMapViewChangeListener observer = iter.next();
			observer.beforeViewChange(oldMap, newMap);
		}
	}

	private MapModel getModel(final MapView view) {
		return view == null ? null : view.getModel();
	}

	void mapViewCreated(final MapView mapView) {
		for (final Iterator<IMapViewChangeListener> iter = viewListeners.iterator(); iter.hasNext();) {
			final IMapViewChangeListener observer = iter.next();
			observer.afterViewCreated(mapView);
		}
	}

	void removeListener(final IMapSelectionListener listener) {
		mapListeners.remove(listener);
	}

	void removeListener(final IMapViewChangeListener listener) {
		viewListeners.remove(listener);
	}
}
