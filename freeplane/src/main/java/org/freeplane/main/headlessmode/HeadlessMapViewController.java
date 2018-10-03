/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2012 Dimitry
 *
 *  This file author is Dimitry
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
package org.freeplane.main.headlessmode;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.RenderedImage;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.*;

import org.freeplane.core.extension.Configurable;
import org.freeplane.features.map.IMapSelection;
import org.freeplane.features.map.IMapSelection.NodePosition;
import org.freeplane.features.map.IMapSelectionListener;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.ui.IMapViewChangeListener;
import org.freeplane.features.ui.IMapViewManager;

/**
 * @author Dimitry Polivaev
 * 24.12.2012
 */
public class HeadlessMapViewController implements IMapViewManager {
	final private Map<String, MapModel> maps = new HashMap<String, MapModel>();
	Collection<IMapSelectionListener> mapSelectionListeners = new ArrayList<IMapSelectionListener>();
	private MapModel currentMap = null;
	private String currentKey = null;

	@Override
	public void addMapSelectionListener(IMapSelectionListener pListener) {
		mapSelectionListeners.add(pListener);
	}

	@Override
	public void addMapViewChangeListener(IMapViewChangeListener pListener) {

	}

	@Override
	public boolean changeToMapView(Component newMapView) {
		throw new RuntimeException("Method not implemented");
	}


	@Override
	public void changeToMap(MapModel map) {
		for(Map.Entry<String, MapModel> mapEntry : maps.entrySet())
			if(mapEntry.getValue().equals(map)) {
				changeToMap(map, mapEntry.getKey());
				return;
			}
	}

	@Override
	public boolean changeToMapView(String mapViewDisplayName) {
		if(mapViewDisplayName != null && maps.containsKey(mapViewDisplayName)) {
			final MapModel nextMap = maps.get(mapViewDisplayName);
			changeToMap(nextMap, mapViewDisplayName);
	        return true;
        }
        else
			return false;
	}

	private void changeToMap(final MapModel nextMap, String mapViewDisplayName) {
		MapModel oldMap = currentMap;
		for(IMapSelectionListener mapSelectionListener : mapSelectionListeners)
			mapSelectionListener.beforeMapChange(oldMap, nextMap);
		currentKey = mapViewDisplayName;
		currentMap = nextMap;
		for(IMapSelectionListener mapSelectionListener : mapSelectionListeners)
			mapSelectionListener.afterMapChange(oldMap, nextMap);
	}

	@Override
	public boolean changeToMode(String modeName) {
		throw new RuntimeException("Method not implemented");
	}

	@Override
	public String checkIfFileIsAlreadyOpened(URL urlToCheck) throws MalformedURLException {
		final String key = urlToCheck.toString();
		if(maps.containsKey(key))
			return key;
		else
			return null;
	}

	@Override
	public boolean close() {
		closeWithoutSaving();
		return true;
	}

	@Override
	public void closeWithoutSaving() {
		if(currentMap != null) {
			maps.remove(currentKey);
			currentKey = null;
			currentMap = null;
		}
	}

	@Override
	public String createHtmlMap() {
		throw new RuntimeException("Method not implemented");
	}

	@Override
	public RenderedImage createImage(int dpi) {
		throw new RuntimeException("Method not implemented");
	}

	@Override
	public RenderedImage createImage(final Dimension slideSize, NodeModel placedNode, NodePosition placedNodePosition, int dpi) {
		throw new RuntimeException("Method not implemented");
	}

	@Override
	public Color getBackgroundColor(NodeModel node) {
		throw new RuntimeException("Method not implemented");
	}

	@Override
	public Component getComponent(NodeModel node) {
		throw new RuntimeException("Method not implemented");
	}

	@Override
	public Font getFont(NodeModel node) {
		throw new RuntimeException("Method not implemented");
	}

	@Override
	public List<String> getMapKeys() {
		throw new RuntimeException("Method not implemented");
	}

	@Override
	public Map<String, MapModel> getMaps() {
		return maps;
	}

	@Override
	public IMapSelection getMapSelection() {
		return null;
	}

	@Override
	public JComponent getMapViewComponent() {
		throw new RuntimeException("Method not implemented");
	}

	@Override
	public Configurable getMapViewConfiguration() {
		throw new RuntimeException("Method not implemented");
	}

	@Override
	public List<? extends Component> getMapViewVector() {
		return Collections.emptyList();
	}

	@Override
	public ModeController getModeController(Component newMap) {
		throw new RuntimeException("Method not implemented");
	}

	@Override
	public MapModel getModel() {
		return currentMap;
	}

	@Override
	public MapModel getModel(Component mapView) {
		throw new RuntimeException("Method not implemented");
	}

	@Override
	public Component getSelectedComponent() {
		throw new RuntimeException("Method not implemented");
	}

	@Override
	public Color getTextColor(NodeModel node) {
		throw new RuntimeException("Method not implemented");
	}

	@Override
	public float getZoom() {
		throw new RuntimeException("Method not implemented");
	}

	@Override
	public void newMapView(MapModel map, ModeController modeController) {
		final String key = map.getURL().toString();
		if(key.equals(currentKey))
			close();
		maps.put(key, map);
		changeToMapView(key);
	}

	public void nextMapView() {
		throw new RuntimeException("Method not implemented");
	}

	public void previousMapView() {
		throw new RuntimeException("Method not implemented");
	}

	@Override
	public void removeMapSelectionListener(IMapSelectionListener pListener) {
		throw new RuntimeException("Method not implemented");
	}

	@Override
	public void removeMapViewChangeListener(IMapViewChangeListener pListener) {
		throw new RuntimeException("Method not implemented");
	}

	@Override
	public void scrollNodeToVisible(NodeModel node) {
		throw new RuntimeException("Method not implemented");
	}

	@Override
	public void setZoom(float zoom) {
		throw new RuntimeException("Method not implemented");
	}

	@Override
	public boolean tryToChangeToMapView(String mapView) {
		return changeToMapView(mapView);
	}

	@Override
	public boolean tryToChangeToMapView(URL url) throws MalformedURLException {
		if(url == null)
			return false;
		return tryToChangeToMapView(url.toString());
	}

	@Override
	public void updateMapViewName() {
		throw new RuntimeException("Method not implemented");
	}

	@Override
	public boolean isLeftTreeSupported(Component mapViewComponent) {
		throw new RuntimeException("Method not implemented");
	}

	@Override
	public Map<String, MapModel> getMaps(String modename) {
		return maps;
	}

	@Override
	public List<Component> getViews(MapModel map) {
		return Collections.emptyList();
	}

	public JScrollPane getScrollPane() {
		throw new RuntimeException("Method not implemented");
	}

	public Container getViewport() {
		throw new RuntimeException("Method not implemented");
	}

	@Override
	public void obtainFocusForSelected() {
		throw new RuntimeException("Method not implemented");
	}

	@Override
	public void setMapTitles() {
	}

	@Override
	public Object setEdgesRenderingHint(Graphics2D g) {
		throw new RuntimeException("Method not implemented");
	}

	@Override
	public void setTextRenderingHint(Graphics2D g) {
		throw new RuntimeException("Method not implemented");
	}

	public boolean closeAllMaps() {
		maps.clear();
		currentKey = null;
		currentMap = null;
		return true;
	}

	@Override
	public boolean close(Component mapViewComponent) {
		throw new RuntimeException("Method not implemented");
    }

	@Override
	public JComboBox createZoomBox() {
		throw new RuntimeException("Method not implemented");
	}

	@Override
	public boolean isFoldedOnCurrentView(NodeModel node) {
		return node.isFolded();
	}

	@Override
	public void setFoldedOnCurrentView(NodeModel node, boolean folded) {
		throw new RuntimeException("Method not implemented");
	}

	@Override
	public void onQuitApplication() {
	}

	@Override
	public void moveFocusFromDescendantToSelection(Component ancestor) {
		throw new RuntimeException("Method not implemented");
	}

	@Override
	public void displayOnCurrentView(NodeModel node) {
		throw new RuntimeException("Method not implemented");
	}

	@Override
	public boolean isChildHidden(NodeModel nodeOnPath) {
		return false;
	}

	@Override
	public int getHiddenChildCount(NodeModel node) {
		return 0;
	}

	@Override
	public boolean hasHiddenChildren(NodeModel selected) {
		return false;
	}

	@Override
	public boolean unfoldHiddenChildren(NodeModel node) {
		return false;
	}

	@Override
	public void hideChildren(NodeModel node) {
	}

	@Override
	public boolean showHiddenNode(NodeModel child) {
		return false;
	}

	@Override
	public boolean isSpotlightEnabled() {
		return false;
	}

	@Override
	public boolean isHeadless() {
		return true;
	}

}
