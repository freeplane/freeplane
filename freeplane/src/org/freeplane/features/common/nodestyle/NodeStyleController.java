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

import org.freeplane.core.controller.CombinedPropertyChain;
import org.freeplane.core.controller.Controller;
import org.freeplane.core.controller.ExclusivePropertyChain;
import org.freeplane.core.controller.IPropertyHandler;
import org.freeplane.core.extension.IExtension;
import org.freeplane.core.io.ReadManager;
import org.freeplane.core.io.WriteManager;
import org.freeplane.core.model.MapChangeEvent;
import org.freeplane.core.model.MapController;
import org.freeplane.core.model.MapModel;
import org.freeplane.core.model.ModeController;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.resources.IFreeplanePropertyListener;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.ColorUtils;
import org.freeplane.features.common.addins.styles.LogicalStyleModel;
import org.freeplane.features.common.addins.styles.MapStyleModel;

/**
 * @author Dimitry Polivaev
 */
public class NodeStyleController implements IExtension {
	public static final String RESOURCES_NODE_SHAPE = "standardnodeshape";
	public static final String RESOURCES_NODE_TEXT_COLOR = "standardnodetextcolor";
	public static final String RESOURCES_ROOT_NODE_SHAPE = "standardrootnodeshape";
	public static Color standardNodeTextColor;

	public static NodeStyleController getController(final ModeController modeController) {
		return (NodeStyleController) modeController.getExtension(NodeStyleController.class);
	}

	public static void install(final ModeController modeController, final NodeStyleController styleController) {
		modeController.addExtension(NodeStyleController.class, styleController);
	}

	final private ExclusivePropertyChain<Color, NodeModel> backgroundColorHandlers;
	final private Controller controller;
	final private CombinedPropertyChain<Font, NodeModel> fontHandlers;
	final private ModeController modeController;
	final private ExclusivePropertyChain<String, NodeModel> shapeHandlers;
	final private ExclusivePropertyChain<Color, NodeModel> textColorHandlers;

	public NodeStyleController(final ModeController modeController) {
		this.modeController = modeController;
		controller = modeController.getController();
		fontHandlers = new CombinedPropertyChain<Font, NodeModel>();
		textColorHandlers = new ExclusivePropertyChain<Color, NodeModel>();
		backgroundColorHandlers = new ExclusivePropertyChain<Color, NodeModel>();
		shapeHandlers = new ExclusivePropertyChain<String, NodeModel>();
		addFontGetter(IPropertyHandler.NODE, new IPropertyHandler<Font, NodeModel>() {
			public Font getProperty(final NodeModel node, final Font font) {
				final NodeStyleModel nodeStyleModel = NodeStyleModel.getModel(node);
				if (nodeStyleModel == null) {
					return font;
				}
				String family = nodeStyleModel.getFontFamilyName();
				Integer size = nodeStyleModel.getFontSize();
				Boolean bold = nodeStyleModel.isBold();
				Boolean italic = nodeStyleModel.isItalic();
				return createFont(font, family, size, bold, italic);
			}

		});
		addFontGetter(IPropertyHandler.DEFAULT, new IPropertyHandler<Font, NodeModel>() {
			public Font getProperty(final NodeModel node, final Font currentValue) {
				final Font defaultFont = getDefaultFont();
				return defaultFont;
			}
		});
		addFontGetter(IPropertyHandler.DEFAULT_STYLE, new IPropertyHandler<Font, NodeModel>() {
			public Font getProperty(final NodeModel node, final Font currentValue) {
				final Font defaultFont = getStyleFont(currentValue, node.getMap(), MapStyleModel.DEFAULT_STYLE);
				return defaultFont;
			}
		});
		addFontGetter(IPropertyHandler.STYLE, new IPropertyHandler<Font, NodeModel>() {
			public Font getProperty(final NodeModel node, final Font currentValue) {
				final Font defaultFont = getStyleFont(currentValue, node.getMap(), LogicalStyleModel.getStyle(node));
				return defaultFont;
			}
		});
		addColorGetter(IPropertyHandler.NODE, new IPropertyHandler<Color, NodeModel>() {
			public Color getProperty(final NodeModel node, final Color currentValue) {
				return NodeStyleModel.getColor(node);
			}
		});
		addColorGetter(IPropertyHandler.DEFAULT, new IPropertyHandler<Color, NodeModel>() {
			public Color getProperty(final NodeModel node, final Color currentValue) {
				return standardNodeTextColor;
			}
		});
		addColorGetter(IPropertyHandler.DEFAULT_STYLE, new IPropertyHandler<Color, NodeModel>() {
			public Color getProperty(final NodeModel node, final Color currentValue) {
				return getStyleTextColor(node.getMap(), MapStyleModel.DEFAULT_STYLE);
			}
		});
		addColorGetter(IPropertyHandler.STYLE, new IPropertyHandler<Color, NodeModel>() {
			public Color getProperty(final NodeModel node, final Color currentValue) {
				return getStyleTextColor(node.getMap(), LogicalStyleModel.getStyle(node));
			}
		});
		addBackgroundColorGetter(IPropertyHandler.NODE, new IPropertyHandler<Color, NodeModel>() {
			public Color getProperty(final NodeModel node, final Color currentValue) {
				return NodeStyleModel.getBackgroundColor(node);
			}
		});
		addBackgroundColorGetter(IPropertyHandler.DEFAULT_STYLE, new IPropertyHandler<Color, NodeModel>() {
			public Color getProperty(final NodeModel node, final Color currentValue) {
				return getStyleBackgroundColor(node.getMap(), MapStyleModel.DEFAULT_STYLE);
			}
		});
		addBackgroundColorGetter(IPropertyHandler.STYLE, new IPropertyHandler<Color, NodeModel>() {
			public Color getProperty(final NodeModel node, final Color currentValue) {
				return getStyleBackgroundColor(node.getMap(), MapStyleModel.DEFAULT_STYLE);
			}
		});
		addBackgroundColorGetter(IPropertyHandler.STYLE, new IPropertyHandler<Color, NodeModel>() {
			public Color getProperty(final NodeModel node, final Color currentValue) {
				return getStyleBackgroundColor(node.getMap(), LogicalStyleModel.getStyle(node));
			}
		});
		addShapeGetter(IPropertyHandler.NODE, new IPropertyHandler<String, NodeModel>() {
			public String getProperty(final NodeModel node, final String currentValue) {
				final String returnedString = NodeStyleModel.getShape(node);
				if (NodeStyleModel.SHAPE_AS_PARENT.equals(returnedString)) {
					return null;
				}
				return returnedString;
			}
		});
		addShapeGetter(IPropertyHandler.STYLE, new IPropertyHandler<String, NodeModel>() {
			public String getProperty(final NodeModel node, final String currentValue) {
				String returnedString = getStyleShape(node.getMap(), LogicalStyleModel.getStyle(node));
                return returnedString;
			}
		});
		addShapeGetter(IPropertyHandler.DEFAULT_STYLE, new IPropertyHandler<String, NodeModel>() {
			public String getProperty(final NodeModel node, final String currentValue) {
				String returnedString = getStyleShape(node.getMap(), MapStyleModel.DEFAULT_STYLE);
                return returnedString;
			}
		});
		addShapeGetter(IPropertyHandler.DEFAULT, new IPropertyHandler<String, NodeModel>() {
			public String getProperty(final NodeModel node, final String currentValue) {
				return getShape(node);
			}

			private String getShape(final NodeModel node) {
				final String returnedString;
				NodeModel parentNode = node.getParentNode();
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
					getModeController().getMapController().fireMapChanged(event);
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

	private Color getStyleBackgroundColor(final MapModel map, Object styleKey) {
		final MapStyleModel model = MapStyleModel.getExtension(map);
		final NodeModel styleNode = model.getStyleNode(styleKey);
		if(styleNode == null){
			return null;
		}
		final NodeStyleModel styleModel = NodeStyleModel.getModel(styleNode);
		final Color styleColor = styleModel == null ? null : styleModel.getBackgroundColor();
		return styleColor;
	}

	public static Font getDefaultFont() {
		final int fontSize = getDefaultFontSize();
		final int fontStyle = getDefaultFontStyle();
		final String fontFamily = getDefaultFontFamilyName();
		return new Font(fontFamily, fontStyle, fontSize);
	}
 	/**
	 */
	private static  String getDefaultFontFamilyName() {
		return ResourceController.getResourceController().getProperty("defaultfont");
	}

	private static   int getDefaultFontStyle() {
	    return ResourceController.getResourceController().getIntProperty("defaultfontstyle", 0);
    }

	private static   int getDefaultFontSize() {
		return ResourceController.getResourceController().getIntProperty("defaultfontsize", 12);
    }

	public Font getDefaultFont(final MapModel map) {
        return getStyleFont(getDefaultFont(), map, MapStyleModel.DEFAULT_STYLE);
    }


    private Font getStyleFont(final Font baseFont, final MapModel map, Object styleKey) {
    	final MapStyleModel model = MapStyleModel.getExtension(map);
    	final NodeModel styleNode = model.getStyleNode(styleKey);
		if(styleNode == null){
			return baseFont;
		}
    	final NodeStyleModel styleModel = NodeStyleModel.getModel(styleNode);
    	if(styleModel == null){
    		return baseFont;
    	}
    	Boolean bold = styleModel.isBold();
		Boolean italic = styleModel.isItalic();
		String fontFamilyName = styleModel.getFontFamilyName();
		Integer fontSize = styleModel.getFontSize();
		return createFont(baseFont, fontFamilyName, fontSize, bold, italic);
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
	private String getStyleShape(final MapModel map, Object styleKey) {
		final MapStyleModel model = MapStyleModel.getExtension(map);
		final NodeModel styleNode = model.getStyleNode(styleKey);
		if(styleNode == null){
			return null;
		}
		final NodeStyleModel styleModel = NodeStyleModel.getModel(styleNode);
		final String shape = styleModel == null ? null : styleModel.getShape();
		return shape;
	}

	private Color getStyleTextColor(final MapModel map, Object styleKey) {
		final MapStyleModel model = MapStyleModel.getExtension(map);
		final NodeModel styleNode = model.getStyleNode(styleKey);
		if(styleNode == null){
			return null;
		}
		final NodeStyleModel styleModel = NodeStyleModel.getModel(styleNode);
		final Color styleColor = styleModel == null ? null : styleModel.getColor();
		return styleColor;
	}

	public Font getFont(final NodeModel node) {
		final Font font = fontHandlers.getProperty(node);
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

	public ModeController getModeController() {
		return modeController;
	}

	public String getShape(final NodeModel node) {
		final String returnedString = shapeHandlers.getProperty(node);
		if (returnedString.equals(NodeStyleModel.SHAPE_COMBINED)) {
			if (getModeController().getMapController().isFolded(node)) {
				return NodeStyleModel.STYLE_BUBBLE;
			}
			else {
				return NodeStyleModel.STYLE_FORK;
			}
		}
		return returnedString;
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
