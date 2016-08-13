package org.freeplane.plugin.script.proxy;

import groovy.lang.Closure;

import java.awt.Color;
import java.io.File;
import java.util.Map.Entry;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.ColorUtils;
import org.freeplane.features.filter.Filter;
import org.freeplane.features.filter.FilterController;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.styles.MapStyle;
import org.freeplane.features.styles.MapStyleModel;
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

    // MapRO: R
    public Color getBackgroundColor() {
        // see MapBackgroundColorAction
        final MapStyle mapStyle = (MapStyle) Controller.getCurrentModeController().getExtension(MapStyle.class);
        final MapStyleModel model = (MapStyleModel) mapStyle.getMapHook(getDelegate());
        if (model != null) {
            return model.getBackgroundColor();
        }
        else {
            final String colorPropertyString = ResourceController.getResourceController().getProperty(
                MapStyle.RESOURCES_BACKGROUND_COLOR);
            final Color defaultBgColor = ColorUtils.stringToColor(colorPropertyString);
            return defaultBgColor;
        }
    }

    // MapRO: R
    public String getBackgroundColorCode() {
        return ColorUtils.colorToString(getBackgroundColor());
    }

	// Map: R/W
	public boolean close(boolean force, boolean allowInteraction) {
		if (!getDelegate().isSaved() && !force && !allowInteraction)
			throw new RuntimeException("will not close an unsaved map without being told so");
		final IMapViewManager mapViewManager = getMapViewManager();
		changeToThisMap(mapViewManager);
		if(force) {
			mapViewManager.closeWithoutSaving();
			return true;
		}
		else
			return mapViewManager.close();
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
    public void setBackgroundColor(Color color) {
        final MapStyle mapStyle = (MapStyle) Controller.getCurrentModeController().getExtension(MapStyle.class);
        final MapStyleModel model = (MapStyleModel) mapStyle.getMapHook(getDelegate());
        mapStyle.setBackgroundColor(model, color);
    }

    // Map: R/W
    public void setBackgroundColorCode(String rgbString) {
        setBackgroundColor(ColorUtils.stringToColor(rgbString));
    }

	// Map: R/W
	public void setFilter(final Closure<Boolean> closure) {
		final FilterController filterController = FilterController.getCurrentFilterController();
		if (closure == null) {
			filterController.applyNoFiltering();
		}
		else {
			final Filter filter = new Filter(ProxyUtils.createCondition(closure, getScriptContext()), false, false,
			    true);
			filterController.applyFilter(filter, getDelegate(), true);
		}
	}

	// Map: R/W
	public void filter(final Closure<Boolean> closure) {
		setFilter(closure);
	}

	// Map: R/W
	public void setFilter(final boolean showAncestors, final boolean showDescendants, final Closure<Boolean> closure) {
		final FilterController filterController = FilterController.getCurrentFilterController();
		if (closure == null) {
			filterController.applyNoFiltering();
		}
		else {
			final Filter filter = new Filter(ProxyUtils.createCondition(closure, getScriptContext()), showAncestors,
			    showDescendants, true);
			filterController.applyFilter(filter, getDelegate(), true);
		}
	}
	
	// Map: R/W
	public void filter(final boolean showAncestors, final boolean showDescendants, final Closure<Boolean> closure) {
		setFilter(showAncestors, showDescendants, closure);
	}

	// Map: R/W
	public void redoFilter() {
		FilterController.getCurrentFilterController().redo();
    }

	// Map: R/W
	public void undoFilter() {
		FilterController.getCurrentFilterController().undo();
    }

    // Map: RO
    public Proxy.Properties getStorage() {
        return new PropertiesProxy(getDelegate(), getScriptContext());
    }
}
