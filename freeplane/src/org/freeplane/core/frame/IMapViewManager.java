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
package org.freeplane.core.frame;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.freeplane.core.modecontroller.IMapSelection;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.model.MapModel;
import org.freeplane.core.model.NodeModel;

/**
 * @author Dimitry Polivaev
 * 12.01.2009
 */
public interface IMapViewManager {
	public abstract void addMapChangeListener(final IMapChangeListener pListener);

	public abstract void addMapViewChangeListener(final IMapViewChangeListener pListener);

	public abstract void removeMapViewChangeListener(final IMapViewChangeListener pListener);

	/**
	 * is null if the old mode should be closed.
	 *
	 * @return true if the set command was sucessful.
	 */
	public abstract boolean changeToMapView(final Component newMapView);

	public abstract boolean changeToMapView(final String mapViewDisplayName);

	public abstract boolean changeToMode(final String modeName);

	/**
	 * Checks, whether or not a given url is already opened. Unlike
	 * tryToChangeToMapView, it does not consider the map+extension identifiers
	 * nor switches to the mapView.
	 *
	 * @return null, if not found, the map+extension identifier otherwise.
	 */
	public abstract String checkIfFileIsAlreadyOpened(final URL urlToCheck)
	        throws MalformedURLException;

	/**
	 * Close the currently active map, return false if closing canceled.
	 *
	 * @param force
	 *            forces the closing without any save actions.
	 */
	public abstract boolean close(final boolean force);

	/** @return an unmodifiable set of all display names of current opened maps. */
	public abstract List getMapKeys();

	/**
	 * @return a map of String to MapView elements.
	 * @deprecated use getMapViewVector instead (and get the displayname as
	 *             MapView.getDisplayName().
	 */
	@Deprecated
	public abstract Map<String, MapModel> getMaps();

	public abstract List getMapViewVector();

	public abstract int getViewNumber();

	public abstract void newMapView(final MapModel map, final ModeController modeController);

	public abstract void nextMapView();

	public abstract void previousMapView();

	public abstract void removeIMapViewChangeListener(final IMapChangeListener pListener);

	/**
	 * This is the question whether the map is already opened. If this is the
	 * case, the map is automatically opened + returns true. Otherwise does
	 * nothing + returns false.
	 */
	public abstract boolean tryToChangeToMapView(final String mapView);

	public abstract void updateMapViewName();

	public abstract IMapSelection getMapSelection();

	public abstract float getZoom();

	public abstract MapModel getModel();

	public abstract void setZoom(float zoom);

	public abstract Component getSelectedComponent();

	public abstract Color getTextColor(NodeModel node);

	public abstract Color getBackgroundColor(NodeModel node);

	public abstract Font getFont(NodeModel node);

	public abstract void scrollNodeToVisible(NodeModel node);

	public abstract Component getComponent(NodeModel node);

	public abstract void updateMapView();

	public abstract Component getMapViewComponent();
}
