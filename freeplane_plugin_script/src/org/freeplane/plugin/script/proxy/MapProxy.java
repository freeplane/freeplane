package org.freeplane.plugin.script.proxy;

import java.io.File;
import java.util.Map.Entry;

import org.freeplane.core.frame.IMapViewManager;
import org.freeplane.features.common.map.MapModel;
import org.freeplane.features.common.map.NodeModel;
import org.freeplane.plugin.script.proxy.Proxy.Map;
import org.freeplane.plugin.script.proxy.Proxy.Node;

public class MapProxy extends AbstractProxy<MapModel> implements Map {
	public MapProxy(final MapModel map) {
		super(map);
	}

	public Node node(final String id) {
		final NodeModel node = getDelegate().getNodeForID(id);
		return node != null ? new NodeProxy(node) : null;
	}

	public Node getRoot() {
		final NodeModel rootNode = getDelegate().getRootNode();
		return new NodeProxy(rootNode);
	}
	
	@Deprecated
	public Node getRootNode() {
		return getRoot();
	}

	public File getFile() {
		return getDelegate().getFile();
	}

	public String getName() {
		final IMapViewManager mapViewManager = getMapViewManager();
		for (Entry<String, MapModel> map : mapViewManager.getMaps().entrySet()) {
			if (map.getValue().equals(getDelegate()))
				return map.getKey();
        }
		return null;
    }

	public boolean close(boolean force, boolean allowInteraction) {
		if (!getDelegate().isSaved() && !force && !allowInteraction)
			throw new RuntimeException("will not close an unsaved map without being told so");
		final IMapViewManager mapViewManager = getMapViewManager();
		changeToThisMap(mapViewManager);
		return mapViewManager.close(force);
	}

	private void changeToThisMap(final IMapViewManager mapViewManager) {
		String mapKey = findMapViewKey(mapViewManager);
		if (mapKey == null)
			throw new RuntimeException("map " + getDelegate() + " does not seem to be opened");
		mapViewManager.changeToMapView(mapKey);
	}

	private IMapViewManager getMapViewManager() {
		return getModeController().getController().getMapViewManager();
	}

	private String findMapViewKey(final IMapViewManager mapViewManager) {
		for (Entry<String, MapModel> entry : mapViewManager.getMaps().entrySet()) {
			if (entry.getValue().equals(getDelegate())) {
				return entry.getKey();
			}
		}
		return null;
	}

	public boolean save(boolean allowInteraction) {
		if (!getDelegate().isSaved() && getDelegate().getURL() == null && !allowInteraction)
			throw new RuntimeException("no url set for map " + getDelegate());
		changeToThisMap(getMapViewManager());
		return getModeController().save();
	}
}
