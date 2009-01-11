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

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
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

import javax.swing.JScrollPane;

import org.freeplane.core.modecontroller.IMapSelection;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.model.MapModel;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.util.Tools;
import org.freeplane.view.swing.map.MapView;
import org.freeplane.view.swing.map.MapViewScrollPane;
import org.freeplane.view.swing.map.NodeView;


/**
 * Manages the list of MapViews. As this task is very complex, I exported it
 * from Controller to this class to keep Controller simple. The information
 * exchange between controller and this class is managed by observer pattern
 * (the controller observes changes to the map mapViews here).
 */
public class MapViewController {
	static private class MapViewChangeObserverCompound{
		final private HashSet<IMapChangeListener> mapListeners = new HashSet();
		final private HashSet<IMapViewChangeListener> viewListeners = new HashSet();

		void addListener(final IMapChangeListener listener) {
			mapListeners.add(listener);
		}

		void addListener(final IMapViewChangeListener listener) {
			viewListeners.add(listener);
		}

		void afterMapClose(final MapView pOldMap) {
			for (final Iterator<IMapChangeListener> iter = mapListeners.iterator(); iter.hasNext();) {
				final IMapChangeListener observer = (IMapChangeListener) iter.next();
				observer.afterMapClose(getModel(pOldMap));
			}
			for (final Iterator<IMapViewChangeListener> iter = viewListeners.iterator(); iter.hasNext();) {
				final IMapViewChangeListener observer = iter.next();
				observer.afterViewClose(pOldMap);
			}
		}

		void afterMapChange(final MapView oldMap, final MapView newMap) {
			for (final Iterator<IMapChangeListener> iter = mapListeners.iterator(); iter.hasNext();) {
				final IMapChangeListener observer = (IMapChangeListener) iter.next();
				observer.afterMapChange(getModel(oldMap), getModel(newMap));
			}
			for (final Iterator<IMapViewChangeListener> iter = viewListeners.iterator(); iter.hasNext();) {
				final IMapViewChangeListener observer = iter.next();
				observer.afterViewChange(oldMap, newMap);
			}
		}

		void beforeMapChange(final MapView oldMap, final MapView newMap) {
			for (final Iterator<IMapChangeListener> iter = mapListeners.iterator(); iter.hasNext();) {
				final IMapChangeListener observer = (IMapChangeListener) iter.next();
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

		boolean isMapChangeAllowed(final MapModel oldMap, final MapModel newMap) {
			boolean returnValue = true;
			for (final Iterator iter = new Vector(mapListeners).iterator(); iter.hasNext();) {
				final IMapChangeListener observer = (IMapChangeListener) iter.next();
				returnValue = observer.isMapChangeAllowed(oldMap, newMap);
				if (!returnValue) {
					break;
				}
			}
			return returnValue;
		}

		void removeListener(final IMapChangeListener listener) {
			mapListeners.remove(listener);
		}
		void removeListener(final IMapViewChangeListener listener) {
			viewListeners.remove(listener);
		}

		void mapViewCreated(MapView mapView) {
			for (final Iterator<IMapViewChangeListener> iter = viewListeners.iterator(); iter.hasNext();) {
				final IMapViewChangeListener observer = iter.next();
				observer.afterViewCreated(mapView);
			}
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
	MapViewController() {
	}

	public void addMapChangeListener(final IMapChangeListener pListener) {
		listener.addListener(pListener);
	}

	public void addMapViewChangeListener(final IMapViewChangeListener pListener) {
		listener.addListener(pListener);
	}

	public void removeMapViewChangeListener(final IMapViewChangeListener pListener) {
		listener.removeListener(pListener);
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
		if (!listener.isMapChangeAllowed(getModel(oldMapView), getModel(newMapView))) {
			return false;
		}
		listener.beforeMapChange(oldMapView, newMapView);
		mapView = newMapView;
		if (mapView != null) {
			lastModeName = mapView.getModel().getModeController().getModeName();
		}
		listener.afterMapChange(oldMapView, newMapView);
		return true;
	}

	private MapModel getModel(final MapView mapView) {
	    return mapView == null ? null : mapView.getModel();
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
			if (modeName.equals(getModel(mapMod).getModeController().getModeName())) {
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
			if (getModel(mapView) != null) {
				final URL mapViewUrl = getModel(mapView).getURL();
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
		final boolean closingNotCancelled = getModel(mapView).getModeController()
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
	public Map<String, MapModel> getMaps() {
		final HashMap<String, MapModel> returnValue = new HashMap();
		for (final Iterator iterator = mapViewVector.iterator(); iterator.hasNext();) {
			final MapView mapView = (MapView) iterator.next();
			returnValue.put(mapView.getName(), getModel(mapView));
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
		listener.mapViewCreated(mapView);
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

	public void removeIMapViewChangeListener(final IMapChangeListener pListener) {
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

	JScrollPane createScrollPane() {
	    return new MapViewScrollPane();
    }

	IMapSelection getMapSelection() {
		final MapView mapView = getMapView();
		return mapView == null ? null : mapView.getMapSelection();
    }

	float getZoom() {
	    return getMapView().getZoom();
    }

	MapModel getModel() {
		final MapView mapView = getMapView();
		return mapView == null ? null : getModel(mapView);
    }

	void setZoom(float zoom) {
		getMapView().setZoom(zoom);
    }

	public Component getSelectedComponent() {
		final MapView mapView = getMapView();
		return mapView == null ? null : mapView.getSelected();
    }

	public Color getTextColor(NodeModel node) {
		final MapView mapView = getMapView();
		if(mapView == null){
			return null;
		}
		final NodeView nodeView = mapView.getNodeView(node);
		if(nodeView == null){
			return null;
		}
		return nodeView.getTextColor();
    }

	public Color getBackgroundColor(NodeModel node) {
		final MapView mapView = getMapView();
		if(mapView == null){
			return null;
		}
		final NodeView nodeView = mapView.getNodeView(node);
		if(nodeView == null){
			return null;
		}
		return nodeView.getTextBackground();
    }

	public Font getFont(NodeModel node) {
		final MapView mapView = getMapView();
		if(mapView == null){
			return null;
		}
		final NodeView nodeView = mapView.getNodeView(node);
		if(nodeView == null){
			return null;
		}
		return nodeView.getMainView().getFont();
    }

	public void scrollNodeToVisible(NodeModel node) {
	    NodeView nodeView = mapView.getNodeView(node);
	    if(nodeView != null){
		    mapView.scrollNodeToVisible(nodeView);
	    }
    }

	public Component getComponent(NodeModel node) {
	    return mapView.getNodeView(node).getMainView();
    }

	public void updateMapView() {
	    mapView.getRoot().updateAll();
	    
    }
}
