/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2009 Dimitry
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
package org.freeplane.features.styles;

import java.awt.Component;
import java.awt.EventQueue;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.io.IAttributeHandler;
import org.freeplane.core.io.IAttributeWriter;
import org.freeplane.core.io.ITreeWriter;
import org.freeplane.core.io.ReadManager;
import org.freeplane.core.io.WriteManager;
import org.freeplane.core.resources.TranslatedObject;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.undo.IActor;
import org.freeplane.core.util.HtmlUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.filter.condition.ASelectableCondition;
import org.freeplane.features.map.IMapChangeListener;
import org.freeplane.features.map.INodeChangeListener;
import org.freeplane.features.map.ITooltipProvider;
import org.freeplane.features.map.MapChangeEvent;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeBuilder;
import org.freeplane.features.map.NodeChangeEvent;
import org.freeplane.features.map.NodeDeletionEvent;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.NodeMoveEvent;
import org.freeplane.features.map.NodeWriter;
import org.freeplane.features.mode.CombinedPropertyChain;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.IPropertyHandler;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.styles.ConditionalStyleModel.Item;

/**
 * @author Dimitry Polivaev
 * 28.09.2009
 */
public class LogicalStyleController implements IExtension {
// 	final private ModeController modeController;

	private static final int STYLE_TOOLTIP = 0;
	private WeakReference<NodeModel> cachedNode;
	private Collection<IStyle>  cachedStyle;
	final private CombinedPropertyChain<Collection<IStyle>, NodeModel> styleHandlers;

	public LogicalStyleController(ModeController modeController) {
//	    this.modeController = modeController;
		styleHandlers = new CombinedPropertyChain<Collection<IStyle>, NodeModel>(false);
		createBuilder();
		registerChangeListener();
		addStyleGetter(IPropertyHandler.NODE, new IPropertyHandler<Collection<IStyle>, NodeModel>() {
			public Collection<IStyle> getProperty(NodeModel node, Collection<IStyle> currentValue) {
				final MapStyleModel styleModel = MapStyleModel.getExtension(node.getMap());
				add(node, styleModel, currentValue, new StyleNode(node));
				return currentValue;
			}
		});
		addStyleGetter(IPropertyHandler.STYLE, new IPropertyHandler<Collection<IStyle>, NodeModel>() {
			public Collection<IStyle> getProperty(NodeModel node, Collection<IStyle> currentValue) {
				final MapStyleModel styleModel = MapStyleModel.getExtension(node.getMap());
				Collection<IStyle> condStyles = styleModel.getConditionalStyleModel().getStyles(node);
				addAll(node, styleModel, currentValue, condStyles);
				return currentValue;
			}
		});
		addStyleGetter(IPropertyHandler.DEFAULT, new IPropertyHandler<Collection<IStyle>, NodeModel>() {
			public Collection<IStyle> getProperty(NodeModel node, Collection<IStyle> currentValue) {
				add(node, currentValue, MapStyleModel.DEFAULT_STYLE);
				return currentValue;
			}
		});
		modeController.addToolTipProvider(STYLE_TOOLTIP, new ITooltipProvider() {
			public String getTooltip(ModeController modeController, NodeModel node, Component view) {
				if(!ResourceController.getResourceController().getBooleanProperty("show_styles_in_tooltip"))
					return null;
				final Collection<IStyle> styles = getStyles(node);
				if(styles.size() > 0)
					styles.remove(styles.iterator().next());
				final String label = TextUtils.getText("node_styles");
				return HtmlUtils.plainToHTML(label + ": " + getStyleNames(styles, ", "));
			}
		});
	}

	protected Collection<IStyle> getResursively(NodeModel node, Collection<IStyle> collection) {
		final MapStyleModel styleModel = MapStyleModel.getExtension(node.getMap());
		Collection<IStyle> set = new LinkedHashSet<IStyle>();
		addAll(node, styleModel, set, collection);
		return set;
	}

	protected void addAll(NodeModel node, MapStyleModel styleModel, Collection<IStyle> currentValue, Collection<IStyle> collection) {
		for(IStyle styleKey : collection){
			add(node, styleModel, currentValue, styleKey);
		}
    }

	public void add(NodeModel node, Collection<IStyle> currentValue, IStyle style) {
		final MapStyleModel styleModel = MapStyleModel.getExtension(node.getMap());
		add(node, styleModel, currentValue, style);
    }

	protected void add(NodeModel node, MapStyleModel styleModel, Collection<IStyle> currentValue, IStyle styleKey) {
			if(!currentValue.add(styleKey)){
				return;
			}
			final NodeModel styleNode = styleModel.getStyleNode(styleKey);
			if (styleNode == null) {
				return;
			}
			if(styleKey instanceof StyleNode){
				IStyle style = LogicalStyleModel.getStyle(styleNode);
				if(style != null){
					add(node, styleModel, currentValue, style);
				}
			}
			final ConditionalStyleModel conditionalStyleModel = styleNode.getExtension(ConditionalStyleModel.class);
			if(conditionalStyleModel == null)
				return;
			Collection<IStyle> styles = conditionalStyleModel.getStyles(node);
			cachedNode = null;
			addAll(node, styleModel, currentValue, styles);
    }

	private void registerChangeListener() {
		ModeController modeController = Controller.getCurrentModeController();
		final MapController mapController = modeController.getMapController();
		mapController.addMapChangeListener(new IMapChangeListener() {
			public void onPreNodeMoved(NodeMoveEvent nodeMoveEvent) {
				clearCache();
			}

			public void onPreNodeDelete(NodeDeletionEvent nodeDeletionEvent) {
				clearCache();
			}

			public void onNodeMoved(NodeMoveEvent nodeMoveEvent) {
				clearCache();
			}

			public void onNodeInserted(NodeModel parent, NodeModel child, int newIndex) {
				clearCache();
			}

			public void onNodeDeleted(NodeDeletionEvent nodeDeletionEvent) {
				clearCache();
			}

			public void mapChanged(MapChangeEvent event) {
				clearCache();
			}
		});
		mapController.addNodeChangeListener(new INodeChangeListener() {
			public void nodeChanged(NodeChangeEvent event) {
				clearCache();
			}
		});

    }

	private void createBuilder() {
		ModeController modeController = Controller.getCurrentModeController();
		final MapController mapController = modeController.getMapController();
		final ReadManager readManager = mapController.getReadManager();
		readManager.addAttributeHandler(NodeBuilder.XML_NODE, "STYLE_REF", new IAttributeHandler() {
			public void setAttribute(final Object node, final String value) {
				final LogicalStyleModel extension = LogicalStyleModel.createExtension((NodeModel) node);
				extension.setStyle(StyleFactory.create(value));
			}
		});
		readManager.addAttributeHandler(NodeBuilder.XML_NODE, "LOCALIZED_STYLE_REF", new IAttributeHandler() {
			public void setAttribute(final Object node, final String value) {
				final LogicalStyleModel extension = LogicalStyleModel.createExtension((NodeModel) node);
				extension.setStyle(StyleFactory.create(TranslatedObject.format(value)));
			}
		});
		final WriteManager writeManager = mapController.getWriteManager();
		writeManager.addAttributeWriter(NodeBuilder.XML_NODE, new IAttributeWriter() {
			public void writeAttributes(final ITreeWriter writer, final Object node, final String tag) {
				if(! NodeWriter.shouldWriteSharedContent(writer))
					return;
				final LogicalStyleModel extension = LogicalStyleModel.getExtension((NodeModel) node);
				if (extension == null) {
					return;
				}
				final IStyle style = extension.getStyle();
				if (style == null) {
					return;
				}
				final String value = StyleTranslatedObject.toKeyString(style);
				if (style instanceof StyleTranslatedObject) {
					writer.addAttribute("LOCALIZED_STYLE_REF", value);
				}
				else {
					writer.addAttribute("STYLE_REF", value);
				}
			}
		});
    }

	public static void install( final LogicalStyleController logicalStyleController) {
		final ModeController modeController = Controller.getCurrentModeController();
		modeController.addExtension(LogicalStyleController.class, logicalStyleController);
	}

	public static LogicalStyleController getController() {
		final ModeController modeController = Controller.getCurrentModeController();
		return getController(modeController);
	}

	public static LogicalStyleController getController(ModeController modeController) {
		return modeController.getExtension(LogicalStyleController.class);
    }
	public void refreshMap(final MapModel map) {
		final IActor actor = new IActor() {
			public void undo() {
				refreshMapLater(map);
			}

			public String getDescription() {
				return "refreshMap";
			}

			public void act() {
				refreshMapLater(map);
			}
		};
		Controller.getCurrentModeController().execute(actor, map);
	}

	private static Map<MapModel, Integer> mapsToRefresh = new HashMap<MapModel, Integer>();

	private void refreshMapLater(final MapModel map) {
		final Integer count = mapsToRefresh.get(map);
		if (count == null) {
			mapsToRefresh.put(map, 0);
		}
		else {
			mapsToRefresh.put(map, count + 1);
		}
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				final Integer count = mapsToRefresh.get(map);
				if (count > 0) {
					mapsToRefresh.put(map, count - 1);
					return;
				}
				mapsToRefresh.remove(map);
			    final MapStyleModel extension = MapStyleModel.getExtension(map);
			    extension.refreshStyles();
				final MapController mapController = Controller.getCurrentModeController().getMapController();
				mapController.fireMapChanged(
				    new MapChangeEvent(this, map, MapStyle.MAP_STYLES, null, null));
			}
		});
	}

	public IStyle getFirstStyle(final NodeModel node){
		final Collection<IStyle> styles = getStyles(node);
		boolean found = false;
		for(IStyle style:styles){
			if(found){
				return style;
			}
			if((style instanceof StyleNode)){
				found = true;
			}
		}
		return MapStyleModel.DEFAULT_STYLE;
	}
	public Collection<IStyle>  getStyles(final NodeModel node) {
		if(cachedNode != null && node.equals(cachedNode.get())){
			return cachedStyle;
		}
		cachedStyle = null;
		cachedNode = null;
		cachedStyle = styleHandlers.getProperty(node, new LinkedHashSet<IStyle>());
		cachedNode = new WeakReference<NodeModel>(node);
		return cachedStyle;
	}

	public void moveConditionalStyleDown(final ConditionalStyleModel conditionalStyleModel, int index) {
	    conditionalStyleModel.moveDown(index);
    }

	public void moveConditionalStyleUp(final ConditionalStyleModel conditionalStyleModel, int index) {
	    conditionalStyleModel.moveUp(index);
    }

	public void addConditionalStyle(final ConditionalStyleModel conditionalStyleModel, boolean isActive,
                                    ASelectableCondition condition, IStyle style, boolean isLast) {
	    conditionalStyleModel.addCondition(isActive, condition, style, isLast);
    }

	public void insertConditionalStyle(final ConditionalStyleModel conditionalStyleModel, int index, boolean isActive,
                                       ASelectableCondition condition, IStyle style, boolean isLast) {
	    conditionalStyleModel.insertCondition(index, isActive, condition, style, isLast);
    }

	public Item removeConditionalStyle(final ConditionalStyleModel conditionalStyleModel, int index) {
	    return conditionalStyleModel.removeCondition(index);
    }

	private void clearCache() {
	    cachedStyle = null;
	    cachedNode = null;
    }

	public IPropertyHandler<Collection<IStyle>, NodeModel> addStyleGetter(
		final Integer key,
		final IPropertyHandler<Collection<IStyle>, NodeModel> getter) {
		return styleHandlers.addGetter(key, getter);
	}

	public IPropertyHandler<Collection<IStyle>, NodeModel> removeStyleGetter(
		final Integer key,
		final IPropertyHandler<Collection<IStyle>, NodeModel> getter) {
		return styleHandlers.addGetter(key, getter);
	}

	public String getStyleNames(final Collection<IStyle> styles, String separator) {
	    StringBuilder sb = new StringBuilder();
	    int i = 0;
	    for(IStyle style :styles){
	    	if(i > 0)
	    		sb.append(separator);
	    	sb.append(style.toString());
	    	i++;
	    }
	    return sb.toString();
    }

	public Collection<IStyle>  getConditionalMapStyles(final NodeModel node) {
		final MapStyleModel styleModel = MapStyleModel.getExtension(node.getMap());
		Collection<IStyle> condStyles = styleModel.getConditionalStyleModel().getStyles(node);
		clearCache();
		return getResursively(node, condStyles);
	}

	public Collection<IStyle>  getConditionalNodeStyles(final NodeModel node) {
		final Collection<IStyle> condStyles = new LinkedHashSet<IStyle>();
		IStyle style = LogicalStyleModel.getStyle(node);
		if(style != null){
			condStyles.add(style);
		}

		final ConditionalStyleModel conditionalStyleModel = node.getExtension(ConditionalStyleModel.class);
		if(conditionalStyleModel != null) {
			Collection<IStyle> styles = conditionalStyleModel.getStyles(node);
			clearCache();
			condStyles.addAll(styles);
		}
		final Collection<IStyle> all = getResursively(node, condStyles);
		if(style != null){
			all.remove(style);
		}
		return all;
	}

	public String getNodeStyleNames(NodeModel node, String separator) {
		return getStyleNames(getConditionalNodeStyles(node), separator);
    }

	public String getMapStyleNames(NodeModel node, String separator) {
		return getStyleNames(getConditionalMapStyles(node), separator);
    }

	public ConditionalStyleChecker conditionalStylesOf(NodeModel node) {
		final MapStyleModel styleModel = MapStyleModel.getExtension(node.getMap());
		return new ConditionalStyleChecker(styleModel.getConditionalStyleModel(), node.getExtension(ConditionalStyleModel.class));
	}
}
