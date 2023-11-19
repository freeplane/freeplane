package org.freeplane.plugin.script.proxy;

import groovy.lang.Closure;
import org.freeplane.api.ConditionalStyles;
import org.freeplane.api.NodeChangeListener;
import org.freeplane.api.NodeCondition;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.ColorUtils;
import org.freeplane.features.filter.Filter;
import org.freeplane.features.filter.FilterController;
import org.freeplane.features.filter.condition.ICondition;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.styles.ConditionalStyleModel;
import org.freeplane.features.styles.ConditionalStyleModel.Item;
import org.freeplane.features.styles.IStyle;
import org.freeplane.features.styles.LogicalStyleController;
import org.freeplane.features.styles.MapStyle;
import org.freeplane.features.styles.MapStyleModel;
import org.freeplane.features.styles.mindmapmode.MLogicalStyleController;
import org.freeplane.features.ui.IMapViewManager;
import org.freeplane.features.url.mindmapmode.MFileManager;
import org.freeplane.plugin.script.FormulaUtils;
import org.freeplane.plugin.script.ScriptContext;
import org.freeplane.plugin.script.proxy.Proxy.Map;
import org.freeplane.plugin.script.proxy.Proxy.MindMap;
import org.freeplane.plugin.script.proxy.Proxy.Node;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

public class MapProxy extends AbstractProxy<MapModel> implements MindMap, Map {
	public MapProxy(final MapModel map, final ScriptContext scriptContext) {
		super(map, scriptContext);
	}

	// MapRO: R
	@Override
	public Node node(final String id) {
		final NodeModel node = getDelegate().getNodeForID(id);
		return node != null ? new NodeProxy(node, getScriptContext()) : null;
	}

	// MapRO: R
	@Override
	public Node getRoot() {
		final NodeModel rootNode = getDelegate().getRootNode();
		return new NodeProxy(rootNode, getScriptContext());
	}

	@Override
	@Deprecated
	public Node getRootNode() {
		return getRoot();
	}

	// MapRO: R
	@Override
	public File getFile() {
		return getDelegate().getFile();
	}

	// MapRO: R
	@Override
	public String getName() {
		return getDelegate().getTitle();
	}

	// MapRO: R
	@Override
	public boolean isSaved() {
		return getDelegate().isSaved();
	}

    // MapRO: R
    @Override
	public Color getBackgroundColor() {
        // see MapBackgroundColorAction
        final MapStyle mapStyle = Controller.getCurrentModeController().getExtension(MapStyle.class);
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
    @Override
	public String getBackgroundColorCode() {
        return ColorUtils.colorToString(getBackgroundColor());
    }

	@Override
	public ConditionalStyles getConditionalStyles() {
		return new MapConditionalStylesProxy(getDelegate(), getScriptContext());
	}

    // MapRO: R
	@Override
	public List<String> getUserDefinedStylesNames() {
		final MapStyleModel styleModel = MapStyleModel.getExtension(getDelegate());
		final MapModel styleMap = styleModel.getStyleMap();
		final NodeModel styleNodeGroup = styleModel.getStyleNodeGroup(styleMap, MapStyleModel.STYLES_USER_DEFINED);
		final List<NodeModel> nodes = styleNodeGroup.getChildren();
		int size = nodes.size();
		if (size == 0) {
			return Collections.emptyList();
		}
		final ArrayList<String> list = new ArrayList<String>(size);
		for (final NodeModel node : nodes) {
			list.add(node.getText());
		}
		return Collections.unmodifiableList(list);
	}

	// Map: R/W
	@Override
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
		if (! GraphicsEnvironment.isHeadless()) {
			String mapKey = findMapViewKey(mapViewManager);
			if (mapKey == null)
				throw new RuntimeException("map " + getDelegate() + " does not seem to be opened");
			mapViewManager.changeToMapView(mapKey);
		}
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
	@Override
	public boolean save(boolean allowInteraction) {
		if (!getDelegate().isSaved() && getDelegate().getURL() == null && !allowInteraction)
			throw new RuntimeException("no url set for map " + getDelegate());
		changeToThisMap(getMapViewManager());
		return getModeController().save();
	}

	// Map: R/W
	@Override
	public boolean saveAs(File file) {
		changeToThisMap(getMapViewManager());
		return MFileManager.getController(getModeController()).save(getDelegate(), file);
	}

	// Map: R/W
	@Override
	public void setName(final String title) {
		changeToThisMap(getMapViewManager());
		Controller.getCurrentController().getMapViewManager().getMapViewComponent().setName(title);
	}

	// Map: R/W
	@Override
	public void setSaved(final boolean isSaved) {
		Controller.getCurrentModeController().getMapController().setSaved(getDelegate(), isSaved);
	}

    // Map: R/W
    @Override
	public void setBackgroundColor(Color color) {
        final MapStyle mapStyle = Controller.getCurrentModeController().getExtension(MapStyle.class);
        final MapStyleModel model = (MapStyleModel) mapStyle.getMapHook(getDelegate());
        mapStyle.setBackgroundColor(model, color);
    }

    // Map: R/W
    @Override
	public void setBackgroundColorCode(String rgbString) {
        setBackgroundColor(ColorUtils.stringToColor(rgbString));
    }

	// Map: R/W
	public void setFilter(final Closure<Boolean> closure) {
		setFilter(false, false, closure);
	}

	// Map: R/W
	public void filter(final Closure<Boolean> closure) {
		setFilter(closure);
	}


	// Map: R/W
	@Override
	public void filter(NodeCondition condition) {
		setFilter(condition);
	}

	// Map: R/W
	@Override
	public void setFilter(NodeCondition condition) {
		setFilter(false, false, condition);
	}

	// Map: R/W
	@Override
	public void filter(boolean showAncestors, boolean showDescendants, NodeCondition condition) {
		setFilter(showAncestors, showDescendants, condition);
	}


	// Map: R/W
	@Override
	public void setFilter(final boolean showAncestors, final boolean showDescendants, final NodeCondition nc) {
		final ICondition condition = ProxyUtils.createCondition(nc, getScriptContext());
		setFilter(false, showAncestors, showDescendants, condition);
	}

	// Map: R/W
	public void setFilter(final boolean showAncestors, final boolean showDescendants, final Closure<Boolean> closure) {
		final ICondition condition = ProxyUtils.createCondition(closure, getScriptContext());
		setFilter(false, showAncestors, showDescendants, condition);
	}

    // Map: R/W
    @Override
    public void hide(final boolean showAncestors, final boolean showDescendants, final NodeCondition nc) {
        final ICondition condition = ProxyUtils.createCondition(nc, getScriptContext());
        setFilter(true, showAncestors, showDescendants, condition);
    }

    // Map: R/W
    public void hide(final boolean showAncestors, final boolean showDescendants, final Closure<Boolean> closure) {
        final ICondition condition = ProxyUtils.createCondition(closure, getScriptContext());
        setFilter(true, showAncestors, showDescendants, condition);
    }
    
	private void setFilter(boolean hideMatches, final boolean showAncestors, final boolean showDescendants, final ICondition condition) {
		final FilterController filterController = FilterController.getCurrentFilterController();
		if (condition == null) {
			filterController.applyNoFiltering(getDelegate());
		}
		else {
			final Filter filter = new Filter(condition, hideMatches, showAncestors,
			    showDescendants, false, null);
			filterController.applyFilter(getDelegate(), true, filter);
		}
	}

	// Map: R/W
	public void filter(final boolean showAncestors, final boolean showDescendants, final Closure<Boolean> closure) {
		setFilter(showAncestors, showDescendants, closure);
	}

	// Map: R/W
	@Override
	public void redoFilter() {
		FilterController.getCurrentFilterController().redo();
    }

	// Map: R/W
	@Override
	public void undoFilter() {
		FilterController.getCurrentFilterController().undo();
    }

    // Map: RO
    @Override
	public Proxy.Properties getStorage() {
        return new PropertiesProxy(getDelegate(), getScriptContext());
    }

	@Override
	public void evaluateAllFormulas() {
		FormulaUtils.evaluateAllFormulas(getDelegate());
	}


	@Override
	public void evaluateOutdatedFormulas() {
		FormulaUtils.evaluateOutdatedFormulas(getDelegate());
	}

	@Override
	public void addListener(NodeChangeListener listener) {
		NodeChangeListeners.of(Controller.getCurrentModeController(), getDelegate()).add(getScriptContext(), listener);
	}

	@Override
	public void removeListener(NodeChangeListener listener) {
		NodeChangeListeners.of(Controller.getCurrentModeController(), getDelegate()).remove(listener);
	}

	@Override
	public List<NodeChangeListener> getListeners() {
		return NodeChangeListeners.of(Controller.getCurrentModeController(), getDelegate())
				.getListeners();
	}

	@Override
	public void copyStyleFrom(org.freeplane.api.MindMap source, String styleName) {
	    IStyle style = NodeStyleProxy.styleByName(source, styleName);
	    if(style == null)
	        throw new IllegalArgumentException("Style " + styleName +" not found");
	    final MapStyleModel sourceStyleModel = MapStyleModel.getExtension(((MapProxy)source).getDelegate());
	    NodeModel sourceNode = sourceStyleModel.getStyleNode(style);
	    MapStyle styles = getModeController().getExtension(MapStyle.class);
	    styles.undoableCopyStyle(style, sourceNode, getDelegate());
	}

    @Override
    public void copyConditionalStylesFrom(org.freeplane.api.MindMap source, String styleName) {
        IStyle style = NodeStyleProxy.styleByName(source, styleName);
        if(style == null)
            throw new IllegalArgumentException("Style " + styleName +" not found");
        IStyle ownStyle = NodeStyleProxy.styleByName(getDelegate(), styleName);
        if(ownStyle == null)
            copyStyleFrom(source, styleName);
        final MapStyleModel ownStyleModel = MapStyleModel.getExtension(getDelegate());
        ConditionalStyleModel ownConditionalStyleModel = ownStyleModel.getConditionalStyleModel();
        MLogicalStyleController controller = (MLogicalStyleController) getModeController().getExtension(LogicalStyleController.class);
        List<Item> ownConditionalStyles = ownConditionalStyleModel.getStyles();
        for(int i = ownConditionalStyles.size() - 1; i >= 0; i-- ) {
            Item item = ownConditionalStyles.get(i);
            if (item.getStyle().equals(style)) {
                controller.removeConditionalStyle(getDelegate(), ownConditionalStyleModel, i);
            }
        }
        final MapStyleModel sourceStyleModel = MapStyleModel.getExtension(((MapProxy)source).getDelegate());
        ConditionalStyleModel sourceConditionalStyleModel = sourceStyleModel.getConditionalStyleModel();
        List<Item> sourceConditionalStyles = sourceConditionalStyleModel.getStyles();
        for(int i = 0; i < sourceConditionalStyles.size(); i++ ) {
            Item item = sourceConditionalStyles.get(i);
            if (item.getStyle().equals(style)) {
                controller.addConditionalStyle(getDelegate(), ownConditionalStyleModel, item.isActive(), item.getCondition(), style, item.isLast());
            }
        }
        controller.refreshMapLaterUndoable(getDelegate());
    }

    @Override
    public List<String> copyUserStylesFrom(org.freeplane.api.MindMap source, boolean includeConditionalRules, String... styleNameFilters) {
        List<String> styleNames = source.getUserDefinedStylesNames();
        ArrayList<String> styleNamesToImport = new ArrayList<>();
        for(String name: styleNames) {
            for (String filter : styleNameFilters){
                if(name.matches(filter)) {
                    styleNamesToImport.add(name);
                    copyStyleFrom(source, name);
                    if(includeConditionalRules){
                        copyConditionalStylesFrom(source, name);
                    }
                    break;
                }
            }
        }
        return styleNamesToImport;
    }

    @Override
    public List<String> copyUserStylesFrom(org.freeplane.api.MindMap source, boolean includeConditionalRules, ArrayList<String> styleNameFilters) {
        return copyUserStylesFrom( source, includeConditionalRules, styleNameFilters.toArray(new String[0]));
    }

    @Override
    public List<String> copyUserStylesFrom(org.freeplane.api.MindMap source) {
        return copyUserStylesFrom( source, true);
    }

    @Override
    public List<String> copyUserStylesFrom(org.freeplane.api.MindMap source, String... styleNameFilters) {
        return copyUserStylesFrom( source, true, styleNameFilters);
    }

    @Override
    public List<String> copyUserStylesFrom(org.freeplane.api.MindMap source, ArrayList<String> styleNameFilters) {
        return copyUserStylesFrom( source, styleNameFilters.toArray(new String[0]));
    }

    @Override
    public List<String> copyUserStylesFrom(org.freeplane.api.MindMap source, boolean includeConditionalRules) {
        return copyUserStylesFrom( source, includeConditionalRules, ".*");
    }

}
