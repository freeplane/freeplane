package org.freeplane.plugin.script.proxy;

import groovy.lang.Closure;

import java.io.File;
import java.util.Map.Entry;

import org.freeplane.features.filter.Filter;
import org.freeplane.features.filter.FilterController;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.ui.IMapViewManager;
import org.freeplane.plugin.script.ScriptContext;
import org.freeplane.plugin.script.proxy.Proxy.Map;
import org.freeplane.plugin.script.proxy.Proxy.Node;

public class MapProxy extends AbstractProxy<MapModel> implements Map {
	public MapProxy(final MapModel map, final ScriptContext scriptContext) {
		super(map, scriptContext);
	}

	// MapRO: R
	public Node node(final String id) {
		final NodeModel node = getDelegate().getNodeForID(id);
		return node != null ? new NodeProxy(node, getScriptContext()) : null;
	}

	// MapRO: R
	public Node getRoot() {
		final NodeModel rootNode = getDelegate().getRootNode();
		return new NodeProxy(rootNode, getScriptContext());
	}

	@Deprecated
	public Node getRootNode() {
		return getRoot();
	}

	// MapRO: R
	public File getFile() {
		return getDelegate().getFile();
	}

	// MapRO: R
	public String getName() {
		final IMapViewManager mapViewManager = getMapViewManager();
		for (Entry<String, MapModel> map : mapViewManager.getMaps().entrySet()) {
			if (map.getValue().equals(getDelegate()))
				return map.getKey();
		}
		return null;
	}

	// MapRO: R
	public boolean isSaved() {
		return getDelegate().isSaved();
	}

	// Map: R/W
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

	// Map: R/W
	public boolean save(boolean allowInteraction) {
		if (!getDelegate().isSaved() && getDelegate().getURL() == null && !allowInteraction)
			throw new RuntimeException("no url set for map " + getDelegate());
		changeToThisMap(getMapViewManager());
		return getModeController().save();
	}

	// Map: R/W
	public void setName(final String title) {
		changeToThisMap(getMapViewManager());
		Controller.getCurrentController().getMapViewManager().getMapViewComponent().setName(title);
	}

	// Map: R/W
	public void setSaved(final boolean isSaved) {
		Controller.getCurrentModeController().getMapController().setSaved(getDelegate(), isSaved);
	}

	// Map: R/W
	public void setFilter(final Closure<Boolean> closure) {
		final FilterController filterController = FilterController.getCurrentFilterController();
		if (closure == null) {
			filterController.applyNoFiltering();
		}
		else {
			final Filter filter = new Filter(ProxyUtils.createCondition(closure, getScriptContext()), false, false,
			    true, true);
			filterController.applyFilter(filter, getDelegate(), true);
		}
	}

	// Map: R/W
	public void filter(final Closure<Boolean> closure) {
		setFilter(closure);
	}

	// Map: R/W
	public void setFilter(final boolean showAnchestors, final boolean showDescendants, final Closure<Boolean> closure) {
		final FilterController filterController = FilterController.getCurrentFilterController();
		if (closure == null) {
			filterController.applyNoFiltering();
		}
		else {
			final Filter filter = new Filter(ProxyUtils.createCondition(closure, getScriptContext()), showAnchestors,
			    showDescendants, true, true);
			filterController.applyFilter(filter, getDelegate(), true);
		}
	}
	
	// Map: R/W
	public void filter(final boolean showAnchestors, final boolean showDescendants, final Closure<Boolean> closure) {
		setFilter(showAnchestors, showDescendants, closure);
	}

	// Map: R/W
	public void redoFilter() {
		FilterController.getCurrentFilterController().redo();
    }

	// Map: R/W
	public void undoFilter() {
		FilterController.getCurrentFilterController().undo();
    }
}
