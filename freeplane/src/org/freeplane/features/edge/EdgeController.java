/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is created by Dimitry Polivaev in 2008.
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
package org.freeplane.features.edge;

import java.awt.Color;
import java.util.Collection;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.io.ReadManager;
import org.freeplane.core.io.WriteManager;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ExclusivePropertyChain;
import org.freeplane.features.mode.IPropertyHandler;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.styles.AutomaticLayout;
import org.freeplane.features.styles.IStyle;
import org.freeplane.features.styles.LogicalStyleController;
import org.freeplane.features.styles.MapStyleModel;

/**
 * @author Dimitry Polivaev
 */
public class EdgeController implements IExtension {
	public static final EdgeStyle STANDARD_EDGE_STYLE = EdgeStyle.EDGESTYLE_BEZIER;
	public static final Color STANDARD_EDGE_COLOR = Color.GRAY;
	public static final Color ID_BY_PARENT = new Color(0);
	public static final Color ID_BY_GRID = new Color(0);

	public static EdgeController getController() {
		return getController(Controller.getCurrentModeController());
	}
	
	public static EdgeController getController(ModeController modeController) {
		return (EdgeController) modeController.getExtension(EdgeController.class);
	}
	public static void install( final EdgeController edgeController) {
		Controller.getCurrentModeController().addExtension(EdgeController.class, edgeController);
	}

	final private ExclusivePropertyChain<Color, NodeModel> colorHandlers;
// 	private final ModeController modeController;
	final private ExclusivePropertyChain<EdgeStyle, NodeModel> styleHandlers;
	final private ExclusivePropertyChain<Integer, NodeModel> widthHandlers;

	public EdgeController(final ModeController modeController) {
//		this.modeController = modeController;
		colorHandlers = new ExclusivePropertyChain<Color, NodeModel>();
		styleHandlers = new ExclusivePropertyChain<EdgeStyle, NodeModel>();
		widthHandlers = new ExclusivePropertyChain<Integer, NodeModel>();
		
		addColorGetter(IPropertyHandler.NODE, new IPropertyHandler<Color, NodeModel>() {
			public Color getProperty(final NodeModel node, final Color currentValue) {
				return getStyleEdgeColor(node.getMap(), LogicalStyleController.getController(modeController).getStyles(node));
			}
		});
		
		addColorGetter(IPropertyHandler.AUTO, new IPropertyHandler<Color, NodeModel>() {
			public Color getProperty(NodeModel model, Color currentValue) {
				AutomaticLayout layout = model.getMap().getRootNode().getExtension(AutomaticLayout.class);
				if(layout == AutomaticLayout.COLUMNS)
					return EdgeController.ID_BY_GRID;
				else
					return null;
			}
		});
		addColorGetter(IPropertyHandler.DEFAULT, new IPropertyHandler<Color, NodeModel>() {
			public Color getProperty(NodeModel node, final Color currentValue) {
				return ID_BY_PARENT;
			}
		});
		addStyleGetter(IPropertyHandler.STYLE, new IPropertyHandler<EdgeStyle, NodeModel>() {
			public EdgeStyle getProperty(final NodeModel node, final EdgeStyle currentValu) {
				return getStyleStyle(node.getMap(), LogicalStyleController.getController(modeController).getStyles(node));
			}
		});
		addStyleGetter(IPropertyHandler.DEFAULT, new IPropertyHandler<EdgeStyle, NodeModel>() {
			public EdgeStyle getProperty(NodeModel node, final EdgeStyle currentValue) {
				if(node.getParentNode() != null){
					return null;
				}
				return STANDARD_EDGE_STYLE;
			}
		});
		addWidthGetter(IPropertyHandler.STYLE, new IPropertyHandler<Integer, NodeModel>() {
			public Integer getProperty(final NodeModel node, final Integer currentValue) {
				return getStyleWidth(node.getMap(), LogicalStyleController.getController(modeController).getStyles(node));
			}
		});
		
		addWidthGetter(IPropertyHandler.DEFAULT, new IPropertyHandler<Integer, NodeModel>() {
			public Integer getProperty(NodeModel node, final Integer currentValue) {
				if(node.getParentNode() != null){
					return null;
				}
				return new Integer(EdgeModel.WIDTH_THIN);
			}
		});
		final MapController mapController = Controller.getCurrentModeController().getMapController();
		final ReadManager readManager = mapController.getReadManager();
		final WriteManager writeManager = mapController.getWriteManager();
		final EdgeBuilder edgeBuilder = new EdgeBuilder(this);
		edgeBuilder.registerBy(readManager, writeManager);
	}

	public IPropertyHandler<Color, NodeModel> addColorGetter(final Integer key,
	                                                         final IPropertyHandler<Color, NodeModel> getter) {
		return colorHandlers.addGetter(key, getter);
	}

	public IPropertyHandler<EdgeStyle, NodeModel> addStyleGetter(final Integer key,
	                                                             final IPropertyHandler<EdgeStyle, NodeModel> getter) {
		return styleHandlers.addGetter(key, getter);
	}

	public IPropertyHandler<Integer, NodeModel> addWidthGetter(final Integer key,
	                                                           final IPropertyHandler<Integer, NodeModel> getter) {
		return widthHandlers.addGetter(key, getter);
	}

	public Color getColor(final NodeModel node) {
		return getColor(node, true);
	}

	public Color getColor(final NodeModel node, final boolean resolveParent) {
	    final Color color = colorHandlers.getProperty(node);
		if(color == null && resolveParent)
			return getColor(node.getParentNode());
		return color;
    }

	public EdgeStyle getStyle(final NodeModel node) {
		return getStyle(node, true);
	}

	public EdgeStyle getStyle(final NodeModel node, final boolean resolveParent) {
	    final EdgeStyle style = styleHandlers.getProperty(node);
		if(style == null && resolveParent)
			return getStyle(node.getParentNode());
		return style;
    }

	public int getWidth(final NodeModel node) {
		return getWidth(node, true);
	}

	public Integer getWidth(final NodeModel node, final boolean resolveParent) {
	    final Integer width = widthHandlers.getProperty(node);
		if(width == null && resolveParent)
			return getWidth(node.getParentNode());
		return width;
    }

	public IPropertyHandler<Color, NodeModel> removeColorGetter(final Integer key) {
		return colorHandlers.removeGetter(key);
	}

	public IPropertyHandler<EdgeStyle, NodeModel> removeStyleGetter(final Integer key) {
		return styleHandlers.removeGetter(key);
	}

	public IPropertyHandler<Integer, NodeModel> removeWidthGetter(final Integer key) {
		return widthHandlers.removeGetter(key);
	}

	private Color getStyleEdgeColor(final MapModel map, final Collection<IStyle> collection) {
		final MapStyleModel model = MapStyleModel.getExtension(map);
		for(IStyle styleKey : collection){
			final NodeModel styleNode = model.getStyleNode(styleKey);
			if (styleNode == null) {
				continue;
			}
			final EdgeModel styleModel = EdgeModel.getModel(styleNode);
			if (styleModel == null) {
				continue;
			}
			final Color styleColor = styleModel.getColor();
			if (styleColor == null) {
				continue;
			}
			return styleColor;
		}
		return null;
	}

	private Integer getStyleWidth(final MapModel map, final Collection<IStyle> collection) {
		final MapStyleModel model = MapStyleModel.getExtension(map);
		for(IStyle styleKey : collection){
			final NodeModel styleNode = model.getStyleNode(styleKey);
			if (styleNode == null) {
				continue;
			}
			final EdgeModel styleModel = EdgeModel.getModel(styleNode);
			if (styleModel == null) {
				continue;
			}
			final int width = styleModel.getWidth();
			if (width == EdgeModel.DEFAULT_WIDTH ) {
				continue;
			}
			return width;
		}
		return null;
	}

	private EdgeStyle getStyleStyle(final MapModel map, final Collection<IStyle> collection) {
		final MapStyleModel model = MapStyleModel.getExtension(map);
		for(IStyle styleKey : collection){
			final NodeModel styleNode = model.getStyleNode(styleKey);
			if (styleNode == null) {
				continue;
			}
			final EdgeModel styleModel = EdgeModel.getModel(styleNode);
			if (styleModel == null) {
				continue;
			}
			final EdgeStyle style = styleModel.getStyle();
			if (style == null) {
				continue;
			}
			return style;
		}
		return null;
	}

}
