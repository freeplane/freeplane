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
package org.freeplane.features.common.nodestyle;

import java.awt.Color;
import java.awt.Font;
import java.util.Collection;
import org.freeplane.core.controller.CombinedPropertyChain;
import org.freeplane.core.controller.Controller;
import org.freeplane.core.controller.ExclusivePropertyChain;
import org.freeplane.core.controller.IPropertyHandler;
import org.freeplane.core.extension.IExtension;
import org.freeplane.core.io.ReadManager;
import org.freeplane.core.io.WriteManager;
import org.freeplane.core.resources.IFreeplanePropertyListener;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.ColorUtils;
import org.freeplane.features.common.map.MapChangeEvent;
import org.freeplane.features.common.map.MapController;
import org.freeplane.features.common.map.MapModel;
import org.freeplane.features.common.map.ModeController;
import org.freeplane.features.common.map.NodeModel;
import org.freeplane.features.common.styles.IStyle;
import org.freeplane.features.common.styles.LogicalStyleController;
import org.freeplane.features.common.styles.MapStyleModel;

/**
 * @author Dimitry Polivaev
 */
public class NodeStyleController implements IExtension {
	public static final String RESOURCES_NODE_SHAPE = "standardnodeshape";
	public static final String RESOURCES_NODE_TEXT_COLOR = "standardnodetextcolor";
	public static final String RESOURCES_ROOT_NODE_SHAPE = "standardrootnodeshape";
	public static Color standardNodeTextColor;

	public static NodeStyleController getController() {
		final ModeController modeController = Controller.getCurrentModeController();
		return getController(modeController);
	}

	public static NodeStyleController getController(ModeController modeController) {
		return (NodeStyleController) modeController.getExtension(NodeStyleController.class);
	}
	public static void install( final NodeStyleController styleController) {
		final ModeController modeController = Controller.getCurrentModeController();
		modeController.addExtension(NodeStyleController.class, styleController);
	}

	final private ExclusivePropertyChain<Color, NodeModel> backgroundColorHandlers;
// // //	final private Controller controller;
	final private CombinedPropertyChain<Font, NodeModel> fontHandlers;
// 	final private ModeController modeController;
	final private ExclusivePropertyChain<String, NodeModel> shapeHandlers;
	final private ExclusivePropertyChain<Color, NodeModel> textColorHandlers;

	public NodeStyleController(final ModeController modeController) {
//		this.modeController = modeController;
//		controller = modeController.getController();
		fontHandlers = new CombinedPropertyChain<Font, NodeModel>(true);
		textColorHandlers = new ExclusivePropertyChain<Color, NodeModel>();
		backgroundColorHandlers = new ExclusivePropertyChain<Color, NodeModel>();
		shapeHandlers = new ExclusivePropertyChain<String, NodeModel>();
		addFontGetter(IPropertyHandler.DEFAULT, new IPropertyHandler<Font, NodeModel>() {
			public Font getProperty(final NodeModel node, final Font currentValue) {
				final Font defaultFont = NodeStyleController.getDefaultFont();
				return defaultFont;
			}
		});
		addFontGetter(IPropertyHandler.STYLE, new IPropertyHandler<Font, NodeModel>() {
			public Font getProperty(final NodeModel node, final Font currentValue) {
				final Font defaultFont = getStyleFont(currentValue, node.getMap(), LogicalStyleController.getController(modeController).getStyles(node));
				return defaultFont;
			}
		});
		addColorGetter(IPropertyHandler.DEFAULT, new IPropertyHandler<Color, NodeModel>() {
			public Color getProperty(final NodeModel node, final Color currentValue) {
				return standardNodeTextColor;
			}
		});
		addColorGetter(IPropertyHandler.STYLE, new IPropertyHandler<Color, NodeModel>() {
			public Color getProperty(final NodeModel node, final Color currentValue) {
				return getStyleTextColor(node.getMap(), LogicalStyleController.getController(modeController).getStyles(node));
			}
		});
		addBackgroundColorGetter(IPropertyHandler.STYLE, new IPropertyHandler<Color, NodeModel>() {
			public Color getProperty(final NodeModel node, final Color currentValue) {
				return getStyleBackgroundColor(node.getMap(), LogicalStyleController.getController(modeController).getStyles(node));
			}
		});
		addShapeGetter(IPropertyHandler.STYLE, new IPropertyHandler<String, NodeModel>() {
			public String getProperty(final NodeModel node, final String currentValue) {
				final MapModel map = node.getMap();
				final LogicalStyleController styleController = LogicalStyleController.getController(modeController);
				final Collection<IStyle> style = styleController.getStyles(node);
				final String returnedString = getStyleShape(map, style);
				return returnedString;
			}
		});
		addShapeGetter(IPropertyHandler.DEFAULT, new IPropertyHandler<String, NodeModel>() {
			public String getProperty(final NodeModel node, final String currentValue) {
				return getShape(node);
			}

			private String getShape(final NodeModel node) {
				final String returnedString;
				final NodeModel parentNode = node.getParentNode();
				if (parentNode == null) {
					returnedString = ResourceController.getResourceController().getProperty(
					    NodeStyleController.RESOURCES_ROOT_NODE_SHAPE);
				}
				else {
					final String stdstyle = ResourceController.getResourceController().getProperty(
					    NodeStyleController.RESOURCES_NODE_SHAPE);
					if (stdstyle.equals(NodeStyleModel.SHAPE_AS_PARENT)) {
						returnedString = getShape(node.getParentNode());
					}
					else {
						returnedString = stdstyle;
					}
				}
				return returnedString;
			}
		});
		final MapController mapController = modeController.getMapController();
		final ReadManager readManager = mapController.getReadManager();
		final WriteManager writeManager = mapController.getWriteManager();
		final NodeStyleBuilder styleBuilder = new NodeStyleBuilder(this);
		styleBuilder.registerBy(readManager, writeManager);
		if (standardNodeTextColor == null) {
			final String stdcolor = ResourceController.getResourceController().getProperty(
			    NodeStyleController.RESOURCES_NODE_TEXT_COLOR);
			standardNodeTextColor = ColorUtils.stringToColor(stdcolor);
			createPropertyChangeListener();
		}
	}

	public IPropertyHandler<Color, NodeModel> addBackgroundColorGetter(final Integer key,
	                                                                   final IPropertyHandler<Color, NodeModel> getter) {
		return backgroundColorHandlers.addGetter(key, getter);
	}

	public IPropertyHandler<Color, NodeModel> addColorGetter(final Integer key,
	                                                         final IPropertyHandler<Color, NodeModel> getter) {
		return textColorHandlers.addGetter(key, getter);
	}

	public IPropertyHandler<Font, NodeModel> addFontGetter(final Integer key,
	                                                       final IPropertyHandler<Font, NodeModel> getter) {
		return fontHandlers.addGetter(key, getter);
	}

	public IPropertyHandler<String, NodeModel> addShapeGetter(final Integer key,
	                                                          final IPropertyHandler<String, NodeModel> getter) {
		return shapeHandlers.addGetter(key, getter);
	}

	private void createPropertyChangeListener() {
		final IFreeplanePropertyListener propertyChangeListener = new IFreeplanePropertyListener() {
			public void propertyChanged(final String propertyName, final String newValue, final String oldValue) {
				if (propertyName.equals(NodeStyleController.RESOURCES_NODE_TEXT_COLOR)) {
					standardNodeTextColor = ColorUtils.stringToColor(newValue);
					final MapChangeEvent event = new MapChangeEvent(NodeStyleController.this,
					    NodeStyleController.RESOURCES_NODE_TEXT_COLOR, ColorUtils.stringToColor(oldValue),
					    standardNodeTextColor);
					Controller.getCurrentModeController().getMapController().fireMapChanged(event);
				}
			}
		};
		ResourceController.getResourceController().addPropertyChangeListener(propertyChangeListener);
	}

	public Color getBackgroundColor(final NodeModel node) {
		return backgroundColorHandlers.getProperty(node);
	}

	public Color getColor(final NodeModel node) {
		return textColorHandlers.getProperty(node);
	}

	private Color getStyleBackgroundColor(final MapModel map, final Collection<IStyle> styleKeys) {
		final MapStyleModel model = MapStyleModel.getExtension(map);
		for(IStyle styleKey : styleKeys){
			final NodeModel styleNode = model.getStyleNode(styleKey);
			if (styleNode == null) {
				continue;
			}
			final NodeStyleModel styleModel = NodeStyleModel.getModel(styleNode);
			if (styleModel == null) {
				continue;
			}
			final Color styleColor =styleModel.getBackgroundColor();
			return styleColor;
		}
		return null;
	}

	public static Font getDefaultFont() {
		final int fontSize = NodeStyleController.getDefaultFontSize();
		final int fontStyle = NodeStyleController.getDefaultFontStyle();
		final String fontFamily = NodeStyleController.getDefaultFontFamilyName();
		return new Font(fontFamily, fontStyle, fontSize);
	}

	/**
	*/
	private static String getDefaultFontFamilyName() {
		return ResourceController.getResourceController().getProperty("defaultfont");
	}

	private static int getDefaultFontStyle() {
		return ResourceController.getResourceController().getIntProperty("defaultfontstyle", 0);
	}

	private static int getDefaultFontSize() {
		return ResourceController.getResourceController().getIntProperty("defaultfontsize", 12);
	}

	public Font getDefaultFont(final MapModel map) {
		final MapStyleModel model = MapStyleModel.getExtension(map);
		final NodeModel styleNode = model.getStyleNode(MapStyleModel.DEFAULT_STYLE);
		Font baseFont = NodeStyleController.getDefaultFont();
		if (styleNode == null) {
			return baseFont;
		}
		final NodeStyleModel styleModel = NodeStyleModel.getModel(styleNode);
		if (styleModel == null) {
			return baseFont;
		}
		return createFont(baseFont, styleModel);
	}

	private Font createFont(Font baseFont, final NodeStyleModel styleModel) {
	    final Boolean bold = styleModel.isBold();
		final Boolean italic = styleModel.isItalic();
		final String fontFamilyName = styleModel.getFontFamilyName();
		final Integer fontSize = styleModel.getFontSize();
		return createFont(baseFont, fontFamilyName, fontSize, bold, italic);
    }

	private Font getStyleFont(final Font baseFont, final MapModel map, final Collection<IStyle> collection) {
		final MapStyleModel model = MapStyleModel.getExtension(map);
		for(IStyle styleKey : collection){
			final NodeModel styleNode = model.getStyleNode(styleKey);
			if (styleNode == null) {
				continue;
			}
			final NodeStyleModel styleModel = NodeStyleModel.getModel(styleNode);
			if (styleModel == null) {
				continue;
			}
			return createFont(baseFont, styleModel);
		}
		return baseFont;
	}

	private Font createFont(final Font baseFont, String family, Integer size, Boolean bold, Boolean italic) {
		if (family == null && size == null && bold == null && italic == null) {
			return baseFont;
		}
		if (family == null) {
			family = baseFont.getFamily();
		}
		if (size == null) {
			size = baseFont.getSize();
		}
		if (bold == null) {
			bold = baseFont.isBold();
		}
		if (italic == null) {
			italic = baseFont.isItalic();
		}
		int style = 0;
		if (bold) {
			style += Font.BOLD;
		}
		if (italic) {
			style += Font.ITALIC;
		}
		return new Font(family, style, size);
	}

	private String getStyleShape(final MapModel map, final Collection<IStyle> style) {
		final MapStyleModel model = MapStyleModel.getExtension(map);
		for(IStyle styleKey : style){
			final NodeModel styleNode = model.getStyleNode(styleKey);
			if (styleNode == null) {
				continue;
			}
			final NodeStyleModel styleModel = NodeStyleModel.getModel(styleNode);
			if (styleModel == null) {
				continue;
			}
			final String shape = styleModel.getShape();
			return shape;
		}
		return null;
	}

	private Color getStyleTextColor(final MapModel map, final Collection<IStyle> collection) {
		final MapStyleModel model = MapStyleModel.getExtension(map);
		for(IStyle styleKey : collection){
			final NodeModel styleNode = model.getStyleNode(styleKey);
			if (styleNode == null) {
				continue;
			}
			final NodeStyleModel styleModel = NodeStyleModel.getModel(styleNode);
			if (styleModel == null) {
				continue;
			}
			final Color styleColor = styleModel == null ? null : styleModel.getColor();
			return styleColor;
		}
		return null;
	}

	public Font getFont(final NodeModel node) {
		final Font font = fontHandlers.getProperty(node, null);
		return font;
	}

	public String getFontFamilyName(final NodeModel node) {
		final Font font = getFont(node);
		return font.getFamily();
	}

	public int getFontSize(final NodeModel node) {
		final Font font = getFont(node);
		return font.getSize();
	}

	public String getShape(final NodeModel node) {
		final String returnedString = getShapeEx(node);
		if (returnedString.equals(NodeStyleModel.SHAPE_COMBINED)) {
			if (Controller.getCurrentModeController().getMapController().isFolded(node)) {
				return NodeStyleModel.STYLE_BUBBLE;
			}
			else {
				return NodeStyleModel.STYLE_FORK;
			}
		}
		return returnedString;
	}

	public String getShapeEx(final NodeModel node) {
	    return shapeHandlers.getProperty(node);
    }

	public boolean isBold(final NodeModel node) {
		return getFont(node).isBold();
	}

	public boolean isItalic(final NodeModel node) {
		return getFont(node).isItalic();
	}

	public IPropertyHandler<Color, NodeModel> removeBackgroundColorGetter(final Integer key) {
		return backgroundColorHandlers.removeGetter(key);
	}

	public IPropertyHandler<Color, NodeModel> removeColorGetter(final Integer key) {
		return textColorHandlers.removeGetter(key);
	}

	public IPropertyHandler<Font, NodeModel> removeFontGetter(final Integer key) {
		return fontHandlers.removeGetter(key);
	}

	public IPropertyHandler<String, NodeModel> removeShapeGetter(final Integer key) {
		return shapeHandlers.removeGetter(key);
	}

}
