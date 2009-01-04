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
package org.freeplane.core.frame;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.model.MapModel;
import org.freeplane.core.util.Tools;
import org.freeplane.view.swing.map.MapView;

/**
 * Manages the list of MapViews. As this task is very complex, I exported it
 * from Controller to this class to keep Controller simple. The information
 * exchange between controller and this class is managed by observer pattern
 * (the controller observes changes to the map mapViews here).
 */
public class MapViewManager {
	static private class MapViewChangeObserverCompound implements IMapViewChangeListener {
		final private HashSet listeners = new HashSet();

		public void addListener(final IMapViewChangeListener listener) {
			listeners.add(listener);
		}

		public void afterMapClose(final MapView pOldMapView) {
			for (final Iterator iter = new Vector(listeners).iterator(); iter.hasNext();) {
				final IMapViewChangeListener observer = (IMapViewChangeListener) iter.next();
				observer.afterMapClose(pOldMapView);
			}
		}

		public void afterMapViewChange(final MapView oldMapView, final MapView newMapView) {
			for (final Iterator iter = new Vector(listeners).iterator(); iter.hasNext();) {
				final IMapViewChangeListener observer = (IMapViewChangeListener) iter.next();
				observer.afterMapViewChange(oldMapView, newMapView);
			}
		}

		public void beforeMapViewChange(final MapView oldMapView, final MapView newMapView) {
			for (final Iterator iter = new Vector(listeners).iterator(); iter.hasNext();) {
				final IMapViewChangeListener observer = (IMapViewChangeListener) iter.next();
				observer.beforeMapViewChange(oldMapView, newMapView);
			}
		}

		public boolean isMapViewChangeAllowed(final MapView oldMapView, final MapView newMapView) {
			boolean returnValue = true;
			for (final Iterator iter = new Vector(listeners).iterator(); iter.hasNext();) {
				final IMapViewChangeListener observer = (IMapViewChangeListener) iter.next();
				returnValue = observer.isMapViewChangeAllowed(oldMapView, newMapView);
				if (!returnValue) {
					break;
				}
			}
			return returnValue;
		}

		public void removeListener(final IMapViewChangeListener listener) {
			listeners.remove(listener);
		}
	}

	private String lastModeName;
	MapViewChangeObserverCompound listener = new MapViewChangeObserverCompound();
	/** reference to the current mapmapView; null is allowed, too. */
	private MapView mapView;
	/**
	 * A vector of MapView instances. They are ordered according to their screen
	 * order.
	 */
	final private Vector mapViewVector = new Vector();

	/**
	 * Reference to the current mode as the mapView may be null.
	 */
	MapViewManager() {
	}

	public void addIMapViewChangeListener(final IMapViewChangeListener pListener) {
		listener.addListener(pListener);
	}

	private void addToOrChangeInMapViews(final String key, final MapView newOrChangedMapView) {
		String extension = "";
		int count = 1;
		final List mapKeys = getMapKeys();
		while (mapKeys.contains(key + extension)) {
			extension = "<" + (++count) + ">";
		}
		newOrChangedMapView.setName((key + extension));
		newOrChangedMapView.setName((key + extension));
		if (!mapViewVector.contains(newOrChangedMapView)) {
			mapViewVector.add(newOrChangedMapView);
		}
	}

	/**
	 * is null if the old mode should be closed.
	 *
	 * @return true if the set command was sucessful.
	 */
	public boolean changeToMapView(final MapView newMapView) {
		final MapView oldMapView = mapView;
		if (!listener.isMapViewChangeAllowed(oldMapView, newMapView)) {
			return false;
		}
		listener.beforeMapViewChange(oldMapView, newMapView);
		mapView = newMapView;
		listener.afterMapViewChange(oldMapView, newMapView);
		if (mapView != null) {
			lastModeName = mapView.getModel().getModeController().getModeName();
		}
		return true;
	}

	public boolean changeToMapView(final String mapViewDisplayName) {
		MapView mapViewCandidate = null;
		for (final Iterator iterator = mapViewVector.iterator(); iterator.hasNext();) {
			final MapView mapMod = (MapView) iterator.next();
			if (Tools.safeEquals(mapViewDisplayName, mapMod.getName())) {
				mapViewCandidate = mapMod;
				break;
			}
		}
		if (mapViewCandidate == null) {
			throw new IllegalArgumentException("Map mapView " + mapViewDisplayName + " not found.");
		}
		return changeToMapView(mapViewCandidate);
	}

	public boolean changeToMode(final String modeName) {
		if (modeName.equals(lastModeName)) {
			return true;
		}
		MapView mapViewCandidate = null;
		for (final Iterator iterator = mapViewVector.iterator(); iterator.hasNext();) {
			final MapView mapMod = (MapView) iterator.next();
			if (modeName.equals(mapMod.getModel().getModeController().getModeName())) {
				mapViewCandidate = mapMod;
				break;
			}
		}
		final boolean changed = changeToMapView(mapViewCandidate);
		if (changed) {
			lastModeName = modeName;
		}
		return changed;
	}

	/**
	 * Checks, whether or not a given url is already opened. Unlike
	 * tryToChangeToMapView, it does not consider the map+extension identifiers
	 * nor switches to the mapView.
	 *
	 * @return null, if not found, the map+extension identifier otherwise.
	 */
	public String checkIfFileIsAlreadyOpened(final URL urlToCheck) throws MalformedURLException {
		for (final Iterator iter = mapViewVector.iterator(); iter.hasNext();) {
			final MapView mapView = (MapView) iter.next();
			if (mapView.getModel() != null) {
				final URL mapViewUrl = mapView.getModel().getURL();
				if (sameFile(urlToCheck, mapViewUrl)) {
					return mapView.getName();
				}
			}
		}
		return null;
	}

	/**
	 * Close the currently active map, return false if closing canceled.
	 *
	 * @param force
	 *            forces the closing without any save actions.
	 */
	public boolean close(final boolean force) {
		final MapView mapView = getMapView();
		final boolean closingNotCancelled = mapView.getModel().getModeController()
		    .getMapController().close(force);
		if (!closingNotCancelled) {
			return false;
		}
		int index = mapViewVector.indexOf(mapView);
		mapViewVector.remove(mapView);
		if (mapViewVector.isEmpty()) {
			/* Keep the current running mode */
			changeToMapView((MapView) null);
		}
		else {
			if (index >= mapViewVector.size() || index < 0) {
				index = mapViewVector.size() - 1;
			}
			changeToMapView(((MapView) mapViewVector.get(index)));
		}
		listener.afterMapClose(mapView);
		return true;
	}

	/** @return an unmodifiable set of all display names of current opened maps. */
	public List getMapKeys() {
		final LinkedList returnValue = new LinkedList();
		for (final Iterator iterator = mapViewVector.iterator(); iterator.hasNext();) {
			final MapView mapView = (MapView) iterator.next();
			returnValue.add(mapView.getName());
		}
		return Collections.unmodifiableList(returnValue);
	}

	public MapView getMapView() {
		return mapView;
	}

	/**
	 * @return a map of String to MapView elements.
	 * @deprecated use getMapViewVector instead (and get the displayname as
	 *             MapView.getDisplayName().
	 */
	@Deprecated
	public Map getMapViews() {
		final HashMap returnValue = new HashMap();
		for (final Iterator iterator = mapViewVector.iterator(); iterator.hasNext();) {
			final MapView mapView = (MapView) iterator.next();
			returnValue.put(mapView.getName(), mapView);
		}
		return Collections.unmodifiableMap(returnValue);
	}

	public List getMapViewVector() {
		return Collections.unmodifiableList(mapViewVector);
	}

	public int getViewNumber() {
		return mapViewVector.size();
	}

	public void newMapView(final MapModel map, final ModeController modeController) {
		final MapView mapView = new MapView(map);
		addToOrChangeInMapViews(mapView.getName(), mapView);
		changeToMapView(mapView);
	}

	void nextMapView() {
		int index;
		final int size = mapViewVector.size();
		if (getMapView() != null) {
			index = mapViewVector.indexOf(getMapView());
		}
		else {
			index = size - 1;
		}
		if (index + 1 < size && index >= 0) {
			changeToMapView(((MapView) mapViewVector.get(index + 1)));
		}
		else if (size > 0) {
			changeToMapView(((MapView) mapViewVector.get(0)));
		}
	}

	void previousMapView() {
		int index;
		final int size = mapViewVector.size();
		if (getMapView() != null) {
			index = mapViewVector.indexOf(getMapView());
		}
		else {
			index = 0;
		}
		if (index > 0) {
			changeToMapView(((MapView) mapViewVector.get(index - 1)));
		}
		else {
			if (size > 0) {
				changeToMapView(((MapView) mapViewVector.get(size - 1)));
			}
		}
	}

	public void removeIMapViewChangeListener(final IMapViewChangeListener pListener) {
		listener.removeListener(pListener);
	}

	private boolean sameFile(final URL urlToCheck, final URL mapViewUrl) {
		if (mapViewUrl == null) {
			return false;
		}
		if (urlToCheck.getProtocol().equals("file") && mapViewUrl.getProtocol().equals("file")) {
			return (new File(urlToCheck.getFile())).equals(new File(mapViewUrl.getFile()));
		}
		return urlToCheck.sameFile(mapViewUrl);
	}

	/**
	 * This is the question whether the map is already opened. If this is the
	 * case, the map is automatically opened + returns true. Otherwise does
	 * nothing + returns false.
	 */
	public boolean tryToChangeToMapView(final String mapView) {
		if (mapView != null && getMapKeys().contains(mapView)) {
			changeToMapView(mapView);
			return true;
		}
		else {
			return false;
		}
	}

	public void updateMapViewName() {
		getMapView().rename();
		addToOrChangeInMapViews(getMapView().getName(), getMapView());
		changeToMapView(getMapView());
	}
}
