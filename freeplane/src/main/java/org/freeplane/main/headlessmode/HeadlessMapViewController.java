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

import javax.swing.JComboBox;
import javax.swing.JScrollPane;

import org.freeplane.features.map.IMapSelection;
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

	public void addMapSelectionListener(IMapSelectionListener pListener) {
		mapSelectionListeners.add(pListener);
	}

	public void addMapViewChangeListener(IMapViewChangeListener pListener) {
		
	}

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

	public boolean changeToMode(String modeName) {
		throw new RuntimeException("Method not implemented");
	}

	public String checkIfFileIsAlreadyOpened(URL urlToCheck) throws MalformedURLException {
		final String key = urlToCheck.toString();
		if(maps.containsKey(key))
			return key;
		else
			return null;
	}

	public boolean close() {
		closeWithoutSaving();
		return true;
	}
	
	public void closeWithoutSaving() {
		if(currentMap != null) {
			maps.remove(currentKey);
			currentKey = null;
			currentMap = null;
		}
	}

	public String createHtmlMap() {
		throw new RuntimeException("Method not implemented");
	}

	public RenderedImage createImage(int dpi) {
		throw new RuntimeException("Method not implemented");
	}

	public Color getBackgroundColor(NodeModel node) {
		throw new RuntimeException("Method not implemented");
	}

	public Component getComponent(NodeModel node) {
		throw new RuntimeException("Method not implemented");
	}

	public Font getFont(NodeModel node) {
		throw new RuntimeException("Method not implemented");
	}

	public List<String> getMapKeys() {
		throw new RuntimeException("Method not implemented");
	}

	public Map<String, MapModel> getMaps() {
		return maps;
	}

	public IMapSelection getMapSelection() {
		throw new RuntimeException("Method not implemented");
	}

	public Component getMapViewComponent() {
		throw new RuntimeException("Method not implemented");
	}

	public List<? extends Component> getMapViewVector() {
		return Collections.emptyList();
	}

	public ModeController getModeController(Component newMap) {
		throw new RuntimeException("Method not implemented");
	}

	public MapModel getModel() {
		return currentMap;
	}

	public MapModel getModel(Component mapView) {
		throw new RuntimeException("Method not implemented");
	}

	public Component getSelectedComponent() {
		throw new RuntimeException("Method not implemented");
	}

	public Color getTextColor(NodeModel node) {
		throw new RuntimeException("Method not implemented");
	}

	public float getZoom() {
		throw new RuntimeException("Method not implemented");
	}

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

	public void removeMapSelectionListener(IMapSelectionListener pListener) {
		throw new RuntimeException("Method not implemented");
	}

	public void removeMapViewChangeListener(IMapViewChangeListener pListener) {
		throw new RuntimeException("Method not implemented");
	}

	public void scrollNodeToVisible(NodeModel node) {
		throw new RuntimeException("Method not implemented");
	}

	public void setZoom(float zoom) {
		throw new RuntimeException("Method not implemented");
	}

	public boolean tryToChangeToMapView(String mapView) {
		return changeToMapView(mapView);
	}

	public boolean tryToChangeToMapView(URL url) throws MalformedURLException {
		if(url == null)
			return false;
		return tryToChangeToMapView(url.toString());
	}

	public void updateMapViewName() {
		throw new RuntimeException("Method not implemented");
	}

	public boolean isLeftTreeSupported(Component mapViewComponent) {
		throw new RuntimeException("Method not implemented");
	}

	public Map<String, MapModel> getMaps(String modename) {
		return maps;
	}

	public List<Component> getViews(MapModel map) {
		return Collections.emptyList();
	}

	public JScrollPane getScrollPane() {
		throw new RuntimeException("Method not implemented");
	}

	public Container getViewport() {
		throw new RuntimeException("Method not implemented");
	}

	public void obtainFocusForSelected() {
		throw new RuntimeException("Method not implemented");
	}

	public void setMapTitles() {
		throw new RuntimeException("Method not implemented");
	}

	public Object setEdgesRenderingHint(Graphics2D g) {
		throw new RuntimeException("Method not implemented");
	}

	public void setTextRenderingHint(Graphics2D g) {
		throw new RuntimeException("Method not implemented");
	}

	public boolean closeAllMaps() {
		maps.clear();
		currentKey = null;
		currentMap = null;
		return true;
	}

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

}
