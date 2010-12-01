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
package org.freeplane.features.common.styles;

import java.awt.EventQueue;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.NoSuchElementException;

import org.freeplane.core.controller.CombinedPropertyChain;
import org.freeplane.core.controller.Controller;
import org.freeplane.core.controller.IPropertyHandler;
import org.freeplane.core.extension.IExtension;
import org.freeplane.core.io.IAttributeHandler;
import org.freeplane.core.io.IAttributeWriter;
import org.freeplane.core.io.ITreeWriter;
import org.freeplane.core.io.ReadManager;
import org.freeplane.core.io.WriteManager;
import org.freeplane.core.resources.NamedObject;
import org.freeplane.core.undo.IActor;
import org.freeplane.features.common.filter.condition.ASelectableCondition;
import org.freeplane.features.common.map.IMapChangeListener;
import org.freeplane.features.common.map.INodeChangeListener;
import org.freeplane.features.common.map.MapChangeEvent;
import org.freeplane.features.common.map.MapController;
import org.freeplane.features.common.map.MapModel;
import org.freeplane.features.common.map.ModeController;
import org.freeplane.features.common.map.NodeBuilder;
import org.freeplane.features.common.map.NodeChangeEvent;
import org.freeplane.features.common.map.NodeModel;
import org.freeplane.features.common.styles.ConditionalStyleModel.Item;

/**
 * @author Dimitry Polivaev
 * 28.09.2009
 */
public class LogicalStyleController implements IExtension {
// 	final private ModeController modeController;
	
	private WeakReference<NodeModel> cachedNode;
	private Collection<IStyle>  cachedStyle;
	final private CombinedPropertyChain<Collection<IStyle>, NodeModel> styleHandlers;

	public LogicalStyleController() {
//	    this.modeController = modeController;
		styleHandlers = new CombinedPropertyChain<Collection<IStyle>, NodeModel>(false);		
		createBuilder();
		registerChangeListener();
		addStyleGetter(IPropertyHandler.NODE, new IPropertyHandler<Collection<IStyle>, NodeModel>() {
			public Collection<IStyle> getProperty(NodeModel node, Collection<IStyle> currentValue) {
				currentValue.add(new StyleNode(node));
				IStyle style = LogicalStyleModel.getStyle(node);
				if(style != null){
					currentValue.add(style);
				}
				return currentValue;
			}
		});
		addStyleGetter(IPropertyHandler.STYLE, new IPropertyHandler<Collection<IStyle>, NodeModel>() {
			public Collection<IStyle> getProperty(NodeModel node, Collection<IStyle> currentValue) {
				final MapStyleModel styleModel = MapStyleModel.getExtension(node.getMap());
				Collection<IStyle> condStyles = styleModel.getConditionalStyleModel().getStyles(node);
				currentValue.addAll(condStyles);
				return currentValue;
			}
		});
		addStyleGetter(IPropertyHandler.DEFAULT, new IPropertyHandler<Collection<IStyle>, NodeModel>() {
			public Collection<IStyle> getProperty(NodeModel model, Collection<IStyle> currentValue) {
				currentValue.add(MapStyleModel.DEFAULT_STYLE);
				return currentValue;
			}
		});
	}

	private void registerChangeListener() {
		ModeController modeController = Controller.getCurrentModeController();
		final MapController mapController = modeController.getMapController();
		mapController.addMapChangeListener(new IMapChangeListener() {
			public void onPreNodeMoved(NodeModel oldParent, int oldIndex, NodeModel newParent, NodeModel child, int newIndex) {
				clearCache();
			}
			
			public void onPreNodeDelete(NodeModel oldParent, NodeModel selectedNode, int index) {
				clearCache();
			}
			
			public void onNodeMoved(NodeModel oldParent, int oldIndex, NodeModel newParent, NodeModel child, int newIndex) {
				clearCache();
			}
			
			public void onNodeInserted(NodeModel parent, NodeModel child, int newIndex) {
				clearCache();
			}
			
			public void onNodeDeleted(NodeModel parent, NodeModel child, int index) {
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
				extension.setStyle(StyleFactory.create(NamedObject.formatText(value)));
			}
		});
		final WriteManager writeManager = mapController.getWriteManager();
		writeManager.addAttributeWriter(NodeBuilder.XML_NODE, new IAttributeWriter() {
			public void writeAttributes(final ITreeWriter writer, final Object node, final String tag) {
				final LogicalStyleModel extension = LogicalStyleModel.getExtension((NodeModel) node);
				if (extension == null) {
					return;
				}
				final IStyle style = extension.getStyle();
				if (style == null) {
					return;
				}
				final String value = StyleNamedObject.toKeyString(style);
				if (style instanceof StyleNamedObject) {
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
		return (LogicalStyleController) modeController.getExtension(LogicalStyleController.class);
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
				Controller.getCurrentModeController().getMapController().fireMapChanged(
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
		return null;
	}
	public Collection<IStyle>  getStyles(final NodeModel node) {
		if(cachedNode != null && node.equals(cachedNode.get())){
			return cachedStyle;
		}
		cachedStyle = styleHandlers.getProperty(node, new LinkedHashSet<IStyle>());
		cachedNode = new WeakReference<NodeModel>(node);
		return cachedStyle;
	}
	
	public void moveConditionalStyleDown(final MapModel map, int index){
		final MapStyleModel styleModel = MapStyleModel.getExtension(map);
		styleModel.getConditionalStyleModel().moveDown(index);
	}
	public void moveConditionalStyleUp(final MapModel map, int index){
		final MapStyleModel styleModel = MapStyleModel.getExtension(map);
		styleModel.getConditionalStyleModel().moveUp(index);
	}
	public void addConditionalStyle(final MapModel map, boolean isActive, ASelectableCondition condition, IStyle style, boolean isLast){
		final MapStyleModel styleModel = MapStyleModel.getExtension(map);
		styleModel.getConditionalStyleModel().addCondition(isActive, condition, style, isLast);
	}
	public void insertConditionalStyle(final MapModel map, int index, boolean isActive, ASelectableCondition condition, IStyle style, boolean isLast){
		final MapStyleModel styleModel = MapStyleModel.getExtension(map);
		styleModel.getConditionalStyleModel().insertCondition(index, isActive, condition, style, isLast);
	}
	public Item removeConditionalStyle(final MapModel map, int index){
		final MapStyleModel styleModel = MapStyleModel.getExtension(map);
		return styleModel.getConditionalStyleModel().removeCondition(index);
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

	   
	public NodeModel getUserStyleParentNode(final MapModel styleMap) {
		final NodeModel rootNode = styleMap.getRootNode();
		final int childCount = rootNode.getChildCount();
		for(int i = 0; i < childCount; i++){
			final NodeModel childNode = (NodeModel) rootNode.getChildAt(i);
			final StyleNamedObject userObject = (StyleNamedObject) childNode.getUserObject();
			if(userObject.getObject().equals("styles.user-defined")){
				return childNode;
			}
		}
		throw new NoSuchElementException();
	}
}
