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
package org.freeplane.features.nodestyle;

import java.awt.Color;
import java.awt.Font;
import java.awt.font.TextAttribute;
import java.util.Collection;

import org.freeplane.api.Dash;
import org.freeplane.api.HorizontalTextAlignment;
import org.freeplane.api.LengthUnit;
import org.freeplane.api.Quantity;
import org.freeplane.api.TextWritingDirection;
import org.freeplane.core.extension.IExtension;
import org.freeplane.core.io.ReadManager;
import org.freeplane.core.io.WriteManager;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.features.edge.EdgeController;
import org.freeplane.features.format.PatternFormat;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.SummaryNode;
import org.freeplane.features.mode.CombinedPropertyChain;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ExclusivePropertyChain;
import org.freeplane.features.mode.IPropertyHandler;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.styles.IStyle;
import org.freeplane.features.styles.LogicalStyleController;
import org.freeplane.features.styles.LogicalStyleController.StyleOption;
import org.freeplane.features.styles.MapStyleModel;

/**
 * @author Dimitry Polivaev
 */
public class NodeStyleController implements IExtension {
	public static Color standardNodeTextColor = Color.BLACK;

	public static NodeStyleController getController() {
		final ModeController modeController = Controller.getCurrentModeController();
		return getController(modeController);
	}

	public static NodeStyleController getController(ModeController modeController) {
		return modeController.getExtension(NodeStyleController.class);
	}
	public static void install( final NodeStyleController styleController) {
		final ModeController modeController = Controller.getCurrentModeController();
		modeController.addExtension(NodeStyleController.class, styleController);
	}

	final private ExclusivePropertyChain<Color, NodeModel> backgroundColorHandlers;
// // //	final private Controller controller;
	final private CombinedPropertyChain<Font, NodeModel> fontHandlers;
 	final private ModeController modeController;
	final public ExclusivePropertyChain<NodeGeometryModel, NodeModel> shapeHandlers;
	final private ExclusivePropertyChain<Color, NodeModel> textColorHandlers;
	final private ExclusivePropertyChain<HorizontalTextAlignment, NodeModel> horizontalTextAlignmentHandlers;
	final private ExclusivePropertyChain<TextWritingDirection, NodeModel> textWritingDirectionHandlers;
	public static final String NODE_NUMBERING = "NodeNumbering";

	private static final Quantity<LengthUnit> DEFAULT_MINIMUM_WIDTH = new Quantity<LengthUnit>(0, LengthUnit.cm);
	private static final Quantity<LengthUnit> DEFAULT_MAXIMUM_WIDTH = new Quantity<LengthUnit>(10, LengthUnit.cm);

	public NodeStyleController(final ModeController modeController) {
		this.modeController = modeController;
		new NodeCssHook();
//		controller = modeController.getController();
		fontHandlers = new CombinedPropertyChain<>(true);
		fontHandlers.addGetter(IPropertyHandler.DEFAULT, new IPropertyHandler<Font, NodeModel>() {
			public Font getProperty(final NodeModel node, LogicalStyleController.StyleOption option, final Font currentValue) {
				final Font defaultFont = NodeStyleController.getDefaultFont();
				return defaultFont;
			}
		});
		fontHandlers.addGetter(IPropertyHandler.STYLE, new IPropertyHandler<Font, NodeModel>() {
			public Font getProperty(final NodeModel node, LogicalStyleController.StyleOption option, final Font currentValue) {
				final Font defaultFont = getStyleFont(currentValue, node.getMap(), LogicalStyleController.getController(modeController).getStyles(node, option));
				return defaultFont;
			}
		});
		textColorHandlers = new ExclusivePropertyChain<>();
		textColorHandlers.addGetter(IPropertyHandler.DEFAULT, new IPropertyHandler<Color, NodeModel>() {
			public Color getProperty(final NodeModel node, LogicalStyleController.StyleOption option, final Color currentValue) {
				return standardNodeTextColor;
			}
		});
		textColorHandlers.addGetter(IPropertyHandler.STYLE, new IPropertyHandler<Color, NodeModel>() {
			public Color getProperty(final NodeModel node, LogicalStyleController.StyleOption option, final Color currentValue) {
				return getStyleTextColor(node.getMap(), LogicalStyleController.getController(modeController).getStyles(node, option));
			}
		});

		backgroundColorHandlers = new ExclusivePropertyChain<>();
		backgroundColorHandlers.addGetter(IPropertyHandler.STYLE, new IPropertyHandler<Color, NodeModel>() {
			public Color getProperty(final NodeModel node, LogicalStyleController.StyleOption option, final Color currentValue) {
				return getStyleBackgroundColor(node.getMap(), LogicalStyleController.getController(modeController).getStyles(node, option));
			}
		});

		shapeHandlers = new ExclusivePropertyChain<>();
		shapeHandlers.addGetter(IPropertyHandler.STYLE, new IPropertyHandler<NodeGeometryModel, NodeModel>() {
			public NodeGeometryModel getProperty(final NodeModel node, LogicalStyleController.StyleOption option, final NodeGeometryModel currentValue) {
				final MapModel map = node.getMap();
				final LogicalStyleController styleController = LogicalStyleController.getController(modeController);
				final Collection<IStyle> style = styleController.getStyles(node, option);
				final NodeGeometryModel returnedShape = getStyleShape(map, style);
				return returnedShape;
			}
		});
		shapeHandlers.addGetter(IPropertyHandler.DEFAULT, new IPropertyHandler<NodeGeometryModel, NodeModel>() {
			public NodeGeometryModel getProperty(final NodeModel node, LogicalStyleController.StyleOption option, final NodeGeometryModel currentValue) {
				return NodeGeometryModel.AS_PARENT;
			}
		});

		horizontalTextAlignmentHandlers = new ExclusivePropertyChain<>();
		horizontalTextAlignmentHandlers.addGetter(IPropertyHandler.DEFAULT, new IPropertyHandler<HorizontalTextAlignment, NodeModel>() {
			public HorizontalTextAlignment getProperty(final NodeModel node, LogicalStyleController.StyleOption option, final HorizontalTextAlignment currentValue) {
				return HorizontalTextAlignment.DEFAULT;
			}
		});

		horizontalTextAlignmentHandlers.addGetter(IPropertyHandler.STYLE, new IPropertyHandler<HorizontalTextAlignment, NodeModel>() {
			public HorizontalTextAlignment getProperty(final NodeModel node, LogicalStyleController.StyleOption option, final HorizontalTextAlignment currentValue) {
				return getHorizontalTextAlignment(node.getMap(), LogicalStyleController.getController(modeController).getStyles(node, option));
			}
		});

		textWritingDirectionHandlers = new ExclusivePropertyChain<>();
		textWritingDirectionHandlers.addGetter(IPropertyHandler.DEFAULT, new IPropertyHandler<TextWritingDirection, NodeModel>() {
			public TextWritingDirection getProperty(final NodeModel node, LogicalStyleController.StyleOption option, final TextWritingDirection currentValue) {
				return TextWritingDirection.DEFAULT;
			}
		});

		textWritingDirectionHandlers.addGetter(IPropertyHandler.STYLE, new IPropertyHandler<TextWritingDirection, NodeModel>() {
			public TextWritingDirection getProperty(final NodeModel node, LogicalStyleController.StyleOption option, final TextWritingDirection currentValue) {
				return getTextWritingDirection(node.getMap(), LogicalStyleController.getController(modeController).getStyles(node, option));
			}
		});

		final MapController mapController = modeController.getMapController();
		final ReadManager readManager = mapController.getReadManager();
		final WriteManager writeManager = mapController.getWriteManager();
		final NodeStyleBuilder styleBuilder = new NodeStyleBuilder(this);
		styleBuilder.registerBy(readManager, writeManager);
	}

	public NodeCss getStyleSheet(NodeModel node, LogicalStyleController.StyleOption option) {
		return getStyleSheet(node.getMap(), LogicalStyleController.getController(modeController).getStyles(node, option));
	}

	 private NodeCss getStyleSheet(final MapModel map, final Collection<IStyle> style) {
		final MapStyleModel model = MapStyleModel.getExtension(map);
		for(IStyle styleKey : style){
			final NodeModel styleNode = model.getStyleNode(styleKey);
			if (styleNode == null) {
				continue;
			}
			final NodeCss nodeCss = styleNode.getExtension(NodeCss.class);
			if (nodeCss == null) {
				continue;
			}
			return nodeCss;
		}
		return NodeCss.EMPTY;
	}


	public Color getBackgroundColor(final NodeModel node, StyleOption option) {
		return backgroundColorHandlers.getProperty(node, option);
	}

	public Color getColor(final NodeModel node, StyleOption option) {
		return textColorHandlers.getProperty(node, option);
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
			if (styleColor == null) {
				continue;
			}
			return styleColor;
		}
		return null;
	}

	private Quantity<LengthUnit> getMaxNodeWidth(final MapModel map, final Collection<IStyle> styleKeys) {
		final MapStyleModel model = MapStyleModel.getExtension(map);
		for(IStyle styleKey : styleKeys){
			final NodeModel styleNode = model.getStyleNode(styleKey);
			if (styleNode == null) {
				continue;
			}
			final NodeSizeModel sizeModel = NodeSizeModel.getModel(styleNode);
			if (sizeModel == null) {
				continue;
			}
			final Quantity<LengthUnit> maxTextWidth = sizeModel.getMaxNodeWidth();
			if (maxTextWidth == null) {
				continue;
			}
			return maxTextWidth;
		}
		return DEFAULT_MAXIMUM_WIDTH;
	}

	private Quantity<LengthUnit> getStyleMinWidth(final MapModel map, final Collection<IStyle> styleKeys) {
		final MapStyleModel model = MapStyleModel.getExtension(map);
		for(IStyle styleKey : styleKeys){
			final NodeModel styleNode = model.getStyleNode(styleKey);
			if (styleNode == null) {
				continue;
			}
			final NodeSizeModel sizeModel = NodeSizeModel.getModel(styleNode);
			if (sizeModel == null) {
				continue;
			}
			final Quantity<LengthUnit> minWidth = sizeModel.getMinNodeWidth();
			if (minWidth == null) {
				continue;
			}
			return minWidth;
		}
		return DEFAULT_MINIMUM_WIDTH;
	}

	private Boolean getBorderWidthMatchesEdgeWidth(final MapModel map, final Collection<IStyle> styleKeys) {
		final MapStyleModel model = MapStyleModel.getExtension(map);
		for(IStyle styleKey : styleKeys){
			final NodeModel styleNode = model.getStyleNode(styleKey);
			if (styleNode == null) {
				continue;
			}
			final NodeBorderModel borderModel = NodeBorderModel.getModel(styleNode);
			if (borderModel == null) {
				continue;
			}
			final Boolean borderWidthMatchesEdgeWidth = borderModel.getBorderWidthMatchesEdgeWidth();
			if (borderWidthMatchesEdgeWidth == null) {
				continue;
			}
			return borderWidthMatchesEdgeWidth;
		}
		return false;
	}

	private Quantity<LengthUnit> getBorderWidth(final MapModel map, final Collection<IStyle> styleKeys) {
		final MapStyleModel model = MapStyleModel.getExtension(map);
		for(IStyle styleKey : styleKeys){
			final NodeModel styleNode = model.getStyleNode(styleKey);
			if (styleNode == null) {
				continue;
			}
			final NodeBorderModel borderModel = NodeBorderModel.getModel(styleNode);
			if (borderModel == null) {
				continue;
			}
			final Quantity<LengthUnit> borderWidth = borderModel.getBorderWidth();
			if (borderWidth == null) {
				continue;
			}
			return borderWidth;
		}
		return null;
	}


	private Boolean getBorderDashMatchesEdgeDash(final MapModel map, final Collection<IStyle> styleKeys) {
		final MapStyleModel model = MapStyleModel.getExtension(map);
		for(IStyle styleKey : styleKeys){
			final NodeModel styleNode = model.getStyleNode(styleKey);
			if (styleNode == null) {
				continue;
			}
			final NodeBorderModel borderModel = NodeBorderModel.getModel(styleNode);
			if (borderModel == null) {
				continue;
			}
			final Boolean borderDashMatchesEdgeDash = borderModel.getBorderDashMatchesEdgeDash();
			if (borderDashMatchesEdgeDash == null) {
				continue;
			}
			return borderDashMatchesEdgeDash;
		}
		return false;
	}

	private Dash getBorderDash(final MapModel map, final Collection<IStyle> styleKeys) {
		final MapStyleModel model = MapStyleModel.getExtension(map);
		for(IStyle styleKey : styleKeys){
			final NodeModel styleNode = model.getStyleNode(styleKey);
			if (styleNode == null) {
				continue;
			}
			final NodeBorderModel borderModel = NodeBorderModel.getModel(styleNode);
			if (borderModel == null) {
				continue;
			}
			final Dash borderDash = borderModel.getBorderDash();
			if (borderDash == null) {
				continue;
			}
			return borderDash;
		}
		return Dash.DEFAULT;
	}


	private Boolean getBorderColorMatchesEdgeColor(final MapModel map, final Collection<IStyle> styleKeys) {
		final MapStyleModel model = MapStyleModel.getExtension(map);
		for(IStyle styleKey : styleKeys){
			final NodeModel styleNode = model.getStyleNode(styleKey);
			if (styleNode == null) {
				continue;
			}
			final NodeBorderModel borderModel = NodeBorderModel.getModel(styleNode);
			if (borderModel == null) {
				continue;
			}
			final Boolean borderColorMatchesEdgeColor = borderModel.getBorderColorMatchesEdgeColor();
			if (borderColorMatchesEdgeColor == null) {
				continue;
			}
			return borderColorMatchesEdgeColor;
		}
		return true;
	}

	private Color getBorderColor(final MapModel map, final Collection<IStyle> styleKeys) {
		final MapStyleModel model = MapStyleModel.getExtension(map);
		for(IStyle styleKey : styleKeys){
			final NodeModel styleNode = model.getStyleNode(styleKey);
			if (styleNode == null) {
				continue;
			}
			final NodeBorderModel borderModel = NodeBorderModel.getModel(styleNode);
			if (borderModel == null) {
				continue;
			}
			final Color borderColor = borderModel.getBorderColor();
			if (borderColor == null) {
				continue;
			}
			return borderColor;
		}
		return EdgeController.STANDARD_EDGE_COLOR;
	}

	private static Font getDefaultFont() {
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
		return ResourceController.getResourceController().getIntProperty("defaultfontsize", 10);
	}

	private Font getStyleFont(final Font baseFont, final MapModel map, final Collection<IStyle> collection) {
		final MapStyleModel model = MapStyleModel.getExtension(map);
		Boolean bold = null;
		Boolean strikedThrough = null;
        Boolean italic = null;
        String fontFamilyName = null;
        Integer fontSize = null;
		for(IStyle styleKey : collection){
			final NodeModel styleNode = model.getStyleNode(styleKey);
			if (styleNode == null) {
				continue;
			}
			final NodeStyleModel styleModel = NodeStyleModel.getModel(styleNode);
			if (styleModel == null) {
				continue;
			}
			if (bold == null) bold = styleModel.isBold();
			if (strikedThrough == null) strikedThrough = styleModel.isStrikedThrough();
			if (italic == null) italic = styleModel.isItalic();
			if (fontFamilyName == null) fontFamilyName = styleModel.getFontFamilyName();
			if (fontSize == null) fontSize = styleModel.getFontSize();
			if(bold != null && italic != null && fontFamilyName != null && fontSize != null && strikedThrough == null) break;
		}
		return createFont(baseFont, fontFamilyName, fontSize, bold, italic, strikedThrough);
	}

	public HorizontalTextAlignment getHorizontalTextAlignment(final NodeModel node, StyleOption option) {
		return horizontalTextAlignmentHandlers.getProperty(node, option);
	}

	public TextWritingDirection getTextWritingDirection(final NodeModel node, StyleOption option) {
		return textWritingDirectionHandlers.getProperty(node, option);
	}

	private Font createFont(final Font baseFont, String family, Integer size, Boolean bold, Boolean italic, Boolean strikedThrough) {
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
		final Font font = new Font(family, style, size);
		if(strikedThrough == TextAttribute.STRIKETHROUGH_ON) {
			return FontUtils.strikeThrough(font);
		}
		else
			return font;
	}

	private NodeGeometryModel getStyleShape(final MapModel map, final Collection<IStyle> style) {
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
			final NodeGeometryModel shapeConfiguration = styleModel.getShapeConfiguration();
			if (shapeConfiguration.getShape() == null) {
				continue;
			}
			return shapeConfiguration;
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
			if (styleColor == null) {
				continue;
			}
			return styleColor;
		}
		return null;
	}

	private HorizontalTextAlignment getHorizontalTextAlignment(final MapModel map, final Collection<IStyle> style) {
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
			final HorizontalTextAlignment textAlignment = styleModel.getHorizontalTextAlignment();
			if (textAlignment == null) {
				continue;
			}
			return textAlignment;
		}
		return null;
	}

	private TextWritingDirection getTextWritingDirection(final MapModel map, final Collection<IStyle> style) {
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
			final TextWritingDirection textAlignment = styleModel.getTextWritingDirection();
			if (textAlignment == null) {
				continue;
			}
			return textAlignment;
		}
		return null;
	}

	public Font getFont(final NodeModel node, StyleOption option) {
		final Font font = fontHandlers.getProperty(node, option, null);
		return font;
	}

	public String getFontFamilyName(final NodeModel node, StyleOption option) {
		final Font font = getFont(node, option);
		return font.getFamily();
	}

	public int getFontSize(final NodeModel node, StyleOption option) {
		final Font font = getFont(node,  option);
		return font.getSize();
	}

	public NodeStyleShape getShape(final NodeModel node, StyleOption option) {
		final NodeGeometryModel shapeConfiguration = shapeHandlers.getProperty(node, option);
		return shapeConfiguration.getShape();
	}

	public NodeGeometryModel getShapeConfiguration(NodeModel node, StyleOption option) {
		final NodeGeometryModel shapeConfiguration = shapeHandlers.getProperty(node, option);
		return shapeConfiguration;
	}


	public boolean isBold(final NodeModel node, StyleOption option) {
		return getFont(node, option).isBold();
	}

	public boolean isItalic(final NodeModel node, StyleOption option) {
		return getFont(node, option).isItalic();
	}

	public String getNodeFormat(NodeModel node) {
		Collection<IStyle> collection = LogicalStyleController.getController(modeController).getStyles(node, StyleOption.FOR_UNSELECTED_NODE);
		final MapStyleModel model = MapStyleModel.getExtension(node.getMap());
		for(IStyle styleKey : collection){
			final NodeModel styleNode = model.getStyleNode(styleKey);
			if (styleNode == null) {
				continue;
			}
			final String format = NodeStyleModel.getNodeFormat(styleNode);
			if (format != null) {
				return format;
			}
        }
		// do not return PatternFormat.IDENTITY_PATTERN if parse_data=false because that would
		// automatically disable all IContentTransformers!
		return PatternFormat.STANDARD_FORMAT_PATTERN;
    }

    public boolean getNodeNumbering(NodeModel node) {
    	if(SummaryNode.isFirstGroupNode(node) || SummaryNode.isSummaryNode(node))
    		return false;
		Collection<IStyle> collection = LogicalStyleController.getController(modeController).getStyles(node, StyleOption.FOR_UNSELECTED_NODE);
		final MapStyleModel model = MapStyleModel.getExtension(node.getMap());
		for(IStyle styleKey : collection){
			final NodeModel styleNode = model.getStyleNode(styleKey);
			if (styleNode == null) {
				continue;
			}
			final Boolean numbering = NodeStyleModel.getNodeNumbering(styleNode);
			if (numbering != null) {
				return numbering;
			}
		}
		return false;
    }

	public Quantity<LengthUnit> getMaxWidth(NodeModel node, StyleOption option) {
		final MapModel map = node.getMap();
		final LogicalStyleController styleController = LogicalStyleController.getController(modeController);
		final Collection<IStyle> style = styleController.getStyles(node, option);
		final Quantity<LengthUnit> maxTextWidth = getMaxNodeWidth(map, style);
		return maxTextWidth;
    }

	public Quantity<LengthUnit> getMinWidth(NodeModel node, StyleOption option) {
		final MapModel map = node.getMap();
		final LogicalStyleController styleController = LogicalStyleController.getController(modeController);
		final Collection<IStyle> styles = styleController.getStyles(node, option);
		final Quantity<LengthUnit> minWidth = getStyleMinWidth(map, styles);
		return minWidth;
    }

	public ModeController getModeController() {
	    return modeController;
    }

	public Boolean getBorderWidthMatchesEdgeWidth(NodeModel node, StyleOption option) {
		final MapModel map = node.getMap();
		final LogicalStyleController styleController = LogicalStyleController.getController(modeController);
		final Collection<IStyle> style = styleController.getStyles(node, option);
		final Boolean borderWidthMatchesEdgeWidth = getBorderWidthMatchesEdgeWidth(map, style);
		return borderWidthMatchesEdgeWidth;
	}

	public Boolean getBorderDashMatchesEdgeDash(NodeModel node, StyleOption option) {
		final MapModel map = node.getMap();
		final LogicalStyleController styleController = LogicalStyleController.getController(modeController);
		final Collection<IStyle> style = styleController.getStyles(node, option);
		final Boolean borderDashMatchesEdgeDash = getBorderDashMatchesEdgeDash(map, style);
		return borderDashMatchesEdgeDash;
	}

	public Quantity<LengthUnit> getBorderWidth(NodeModel node, StyleOption option) {
		final MapModel map = node.getMap();
		final LogicalStyleController styleController = LogicalStyleController.getController(modeController);
		final Collection<IStyle> style = styleController.getStyles(node, option);
		final Quantity<LengthUnit> borderWidth = getBorderWidth(map, style);
		return borderWidth != null ? borderWidth : new Quantity<>(1, LengthUnit.px);
	}

	public Dash getBorderDash(NodeModel node, StyleOption option) {
		final MapModel map = node.getMap();
		final LogicalStyleController styleController = LogicalStyleController.getController(modeController);
		final Collection<IStyle> style = styleController.getStyles(node, option);
		final Dash borderDash = getBorderDash(map, style);
		return borderDash;
	}

	public Boolean getBorderColorMatchesEdgeColor(NodeModel node, StyleOption option) {
		final MapModel map = node.getMap();
		final LogicalStyleController styleController = LogicalStyleController.getController(modeController);
		final Collection<IStyle> style = styleController.getStyles(node, option);
		final Boolean borderColorMatchesEdgeColor = getBorderColorMatchesEdgeColor(map, style);
		return borderColorMatchesEdgeColor;
	}

	public Color getBorderColor(NodeModel node, StyleOption option) {
		final MapModel map = node.getMap();
		final LogicalStyleController styleController = LogicalStyleController.getController(modeController);
		final Collection<IStyle> style = styleController.getStyles(node, option);
		final Color borderColor = getBorderColor(map, style);
		return borderColor;
	}

	public boolean isStrikedThrough(NodeModel node, StyleOption option) {
		return FontUtils.isStrikedThrough(getFont(node, option));
	}
}
