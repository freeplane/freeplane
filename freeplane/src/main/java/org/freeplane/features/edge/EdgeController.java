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
import org.freeplane.core.util.ConstantObject;
import org.freeplane.core.util.ObjectRule;
import org.freeplane.core.util.RuleReference;
import org.freeplane.features.DashVariant;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ExclusivePropertyChain;
import org.freeplane.features.mode.IPropertyHandler;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.styles.AutomaticLayoutController;
import org.freeplane.features.styles.IStyle;
import org.freeplane.features.styles.LogicalStyleController;
import org.freeplane.features.styles.MapStyleModel;

/**
 * @author Dimitry Polivaev
 */
public class EdgeController implements IExtension {
	public static final EdgeStyle STANDARD_EDGE_STYLE = EdgeStyle.EDGESTYLE_BEZIER;
	public static final Color STANDARD_EDGE_COLOR = new Color(Color.GRAY.getRGB());
	public static enum Rules {BY_PARENT, BY_COLUMN, BY_LEVEL, BY_BRANCH};

	public static EdgeController getController() {
		return getController(Controller.getCurrentModeController());
	}
	
	public static EdgeController getController(ModeController modeController) {
		return (EdgeController) modeController.getExtension(EdgeController.class);
	}
	public static void install( final EdgeController edgeController) {
		Controller.getCurrentModeController().addExtension(EdgeController.class, edgeController);
	}

	final private ExclusivePropertyChain<ObjectRule<Color, Rules>, NodeModel> colorHandlers;
// 	private final ModeController modeController;
	final private ExclusivePropertyChain<EdgeStyle, NodeModel> styleHandlers;
	final private ExclusivePropertyChain<Integer, NodeModel> widthHandlers;
	final private ExclusivePropertyChain<DashVariant, NodeModel> dashHandlers;
	private ModeController modeController;
	final private EdgeColorsConfigurationFactory edgeColorsConfigurationFactory;

	public EdgeController(final ModeController modeController) {
		this.modeController = modeController;
		edgeColorsConfigurationFactory = new EdgeColorsConfigurationFactory(modeController);
		colorHandlers = new ExclusivePropertyChain<ObjectRule<Color, Rules>, NodeModel>();
		styleHandlers = new ExclusivePropertyChain<EdgeStyle, NodeModel>();
		widthHandlers = new ExclusivePropertyChain<Integer, NodeModel>();
		dashHandlers = new ExclusivePropertyChain<DashVariant, NodeModel>();
		
		addColorGetter(IPropertyHandler.NODE, new IPropertyHandler<ObjectRule<Color, Rules>, NodeModel>() {
			public ObjectRule<Color, Rules> getProperty(final NodeModel node, final ObjectRule<Color, Rules> currentValue) {
				return getStyleEdgeColorRule(node);
			}
		});
		
		addColorGetter(IPropertyHandler.AUTO, new IPropertyHandler<ObjectRule<Color, Rules>, NodeModel>() {
			@Override
			public ObjectRule<Color, Rules> getProperty(NodeModel model, ObjectRule<Color, Rules> currentValue) {
				MapModel map = model.getMap();
				AutomaticEdgeColor layout = map.getRootNode().getExtension(AutomaticEdgeColor.class);
				if(layout != null){
					switch(layout.rule) {
					case FOR_COLUMNS:
						return new RuleReference<Color, EdgeController.Rules>(Rules.BY_COLUMN);
					case FOR_LEVELS:
						return new RuleReference<Color, EdgeController.Rules>(Rules.BY_LEVEL);
					case FOR_BRANCHES:{
						NodeModel parentNode = model.getParentNode();
						if (parentNode!= null && parentNode.isRoot()){
							return new RuleReference<Color, EdgeController.Rules>(Rules.BY_BRANCH);
						}
						break;
					}
					default:
						break;
					}
				}
				return null;
			}
		});
		addColorGetter(IPropertyHandler.DEFAULT, new IPropertyHandler<ObjectRule<Color, Rules>, NodeModel>() {
			public ObjectRule<Color, Rules> getProperty(NodeModel node, final ObjectRule<Color, Rules> currentValue) {
				return new RuleReference<Color, EdgeController.Rules>(Rules.BY_PARENT);
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
		
		addDashGetter(IPropertyHandler.STYLE, new IPropertyHandler<DashVariant, NodeModel>() {
			public DashVariant getProperty(final NodeModel node, final DashVariant currentValue) {
				return getStyleDash(node.getMap(), LogicalStyleController.getController(modeController).getStyles(node));
			}
		});
		
		addDashGetter(IPropertyHandler.DEFAULT, new IPropertyHandler<DashVariant, NodeModel>() {
			public DashVariant getProperty(NodeModel node, final DashVariant currentValue) {
				if(node.getParentNode() != null){
					return null;
				}
				return DashVariant.DEFAULT;
			}
		});
		
		final MapController mapController = Controller.getCurrentModeController().getMapController();
		final ReadManager readManager = mapController.getReadManager();
		final WriteManager writeManager = mapController.getWriteManager();
		final EdgeBuilder edgeBuilder = new EdgeBuilder(this);
		edgeBuilder.registerBy(readManager, writeManager);
	}

	public IPropertyHandler<ObjectRule<Color, Rules>, NodeModel> addColorGetter(final Integer key,
	                                                         final IPropertyHandler<ObjectRule<Color, Rules>, NodeModel> getter) {
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

	public IPropertyHandler<DashVariant, NodeModel> addDashGetter(final Integer key,
			final IPropertyHandler<DashVariant, NodeModel> getter) {
		return dashHandlers.addGetter(key, getter);
	}

	public Color getColor(final NodeModel node) {
		return getColor(node, true);
	}

	public Color getColor(final NodeModel node, final boolean resolveColor) {
	    final ObjectRule<Color, Rules> colorRule = getColorRule(node);
		if(colorRule.hasValue())
			return colorRule.getValue();
		if(Rules.BY_PARENT == colorRule.getRule()) {
			final NodeModel parentNode = node.getParentNode();
			if(parentNode != null)
				return getColor(parentNode);
		}
		return STANDARD_EDGE_COLOR;
    }

	public ObjectRule<Color, Rules> getColorRule(final NodeModel node) {
		final ObjectRule<Color, Rules> color = colorHandlers.getProperty(node);
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
	
	public DashVariant getDash(NodeModel node) {
		return getDash(node, true);
	}

	public DashVariant getDash(NodeModel node, final boolean resolveParent) {
	    final DashVariant dash = dashHandlers.getProperty(node);
		if(dash == null && resolveParent)
			return getDash(node.getParentNode());
		return dash;
	}
	

	private ObjectRule<Color, Rules> getStyleEdgeColorRule(NodeModel node) {
		MapModel map = node.getMap(); 
		Collection<IStyle> collection = LogicalStyleController.getController(modeController).getStyles(node);
		final MapStyleModel styles = MapStyleModel.getExtension(map);
		for(IStyle styleKey : collection){
			final NodeModel styleNode = styles.getStyleNode(styleKey);
			if (styleNode == null) {
				continue;
			}
			if (node != styleNode && map.getRootNode().containsExtension(AutomaticEdgeColor.class)) {
				AutomaticLayoutController automaticLayoutController = modeController.getExtension(AutomaticLayoutController.class);
				if (automaticLayoutController != null && automaticLayoutController.isAutomaticLevelStyle(styleNode)) {
					continue;
				}
			}
			ObjectRule<Color, Rules> nodeColor = getNodeColorRule(styleNode);
			if(nodeColor != null)
				return nodeColor;
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
	
	private DashVariant getStyleDash(final MapModel map, final Collection<IStyle> collection) {
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
			final DashVariant dash = styleModel.getDash();
			if (dash == null ) {
				continue;
			}
			return dash;
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

	private ObjectRule<Color, Rules> getNodeColorRule(NodeModel styleNode) {
		final EdgeModel styleModel = EdgeModel.getModel(styleNode);
		if (styleModel == null) {
			return null;
		}
		final Color styleColor = styleModel.getColor();
		if (styleColor == null) {
			return null;
		}
		return new ConstantObject<Color, Rules>(styleColor);
	}


	public boolean areEdgeColorsAvailable(MapModel map) {
		final EdgeColorConfiguration configuration = edgeColorsConfigurationFactory.create(map);
		return configuration.areEdgeColorsAvailable();
	}

	public Color getEdgeColor(MapModel map, int colorCounter) {
		final EdgeColorConfiguration configuration = edgeColorsConfigurationFactory.create(map);
		return configuration.getEdgeColor(colorCounter);
	}
}
