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
package org.freeplane.features.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.RenderedImage;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.swing.JScrollPane;

import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.features.map.IMapSelection;
import org.freeplane.features.map.IMapSelectionListener;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.ModeController;
import org.freeplane.view.swing.map.MapView;

/**
 * @author Dimitry Polivaev
 * 12.01.2009
 */
public interface IMapViewManager {
	public void addMapSelectionListener(final IMapSelectionListener pListener);

	public void addMapViewChangeListener(final IMapViewChangeListener pListener);

	/**
	 * is null if the old mode should be closed.
	 *
	 * @return true if the set command was sucessful.
	 */
	public boolean changeToMapView(final Component newMapView);

	public boolean changeToMapView(final String mapViewDisplayName);

	public boolean changeToMode(final String modeName);

	/**
	 * Checks, whether or not a given url is already opened. Unlike
	 * tryToChangeToMapView, it does not consider the map+extension identifiers
	 * nor switches to the mapView.
	 *
	 * @return null, if not found, the map+extension identifier otherwise.
	 */
	public String checkIfFileIsAlreadyOpened(final URL urlToCheck) throws MalformedURLException;

	/**
	 * Close the currently active map, return false if closing canceled.
	 *
	 * @param withoutSave
	 *            forces the closing without any save actions.
	 */
	public boolean close(final boolean withoutSave);
	public boolean close(final Component view, final boolean force);
	
	public String createHtmlMap();

	public RenderedImage createImage(int dpi);

	public Color getBackgroundColor(NodeModel node);

	public Component getComponent(NodeModel node);

	public Font getFont(NodeModel node);

	/** @return an unmodifiable set of all display names of current opened maps. */
	public List<String> getMapKeys();

	public Map<String, MapModel> getMaps();

	public IMapSelection getMapSelection();

	public Component getMapViewComponent();

	public List<? extends Component> getMapViewVector();

	public ModeController getModeController(Component newMap);

	public MapModel getModel();

	public MapModel getModel(Component mapView);

	public Component getSelectedComponent();

	public Color getTextColor(NodeModel node);

	public float getZoom();

	public void newMapView(final MapModel map, ModeController modeController);

	public void removeMapSelectionListener(final IMapSelectionListener pListener);

	public void removeMapViewChangeListener(final IMapViewChangeListener pListener);

	public void scrollNodeToVisible(NodeModel node);

	public void setZoom(float zoom);

	/**
	 * This is the question whether the map is already opened. If this is the
	 * case, the map is automatically opened + returns true. Otherwise does
	 * nothing + returns false.
	 */
	public boolean tryToChangeToMapView(final String mapView);
	public boolean tryToChangeToMapView(final URL url) throws MalformedURLException;

	public void updateMapViewName();

	public boolean isLeftTreeSupported(Component mapViewComponent);

	public Map<String, MapModel> getMaps(String modename);

	public List<Component> getViews(MapModel map);
	public void updateMenus(final MenuBuilder menuBuilder);
	public void obtainFocusForSelected();
	public void setTitle();
	public Object setEdgesRenderingHint(final Graphics2D g);
	public void setTextRenderingHint(final Graphics2D g);
	public boolean closeAllMaps();
}
