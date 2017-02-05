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
import java.io.IOException;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.io.IAttributeHandler;
import org.freeplane.core.io.IAttributeWriter;
import org.freeplane.core.io.IElementDOMHandler;
import org.freeplane.core.io.IElementWriter;
import org.freeplane.core.io.IExtensionAttributeWriter;
import org.freeplane.core.io.IExtensionElementWriter;
import org.freeplane.core.io.ITreeWriter;
import org.freeplane.core.io.BackwardCompatibleQuantityWriter;
import org.freeplane.core.io.ReadManager;
import org.freeplane.core.io.WriteManager;
import org.freeplane.core.ui.LengthUnits;
import org.freeplane.core.util.ColorUtils;
import org.freeplane.core.util.FreeplaneVersion;
import org.freeplane.core.util.Quantity;
import org.freeplane.features.DashVariant;
import org.freeplane.features.map.MapWriter;
import org.freeplane.features.map.NodeBuilder;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.NodeWriter;
import org.freeplane.features.nodestyle.NodeStyleModel.Shape;
import org.freeplane.features.nodestyle.NodeStyleModel.TextAlign;
import org.freeplane.n3.nanoxml.XMLElement;

class NodeStyleBuilder implements IElementDOMHandler, IExtensionElementWriter, IExtensionAttributeWriter,
        IAttributeWriter, IElementWriter {
	static class FontProperties {
		String fontName;
		Integer fontSize;
		Boolean isBold;
		Boolean isItalic;
	}

	private final NodeStyleController nsc;

	public NodeStyleBuilder(final NodeStyleController nsc) {
		this.nsc = nsc;
	}

	public Object createElement(final Object parent, final String tag, final XMLElement attributes) {
		if (tag.equals("font")) {
			return new FontProperties();
		}
		return null;
	}

	public void endElement(final Object parent, final String tag, final Object userObject, final XMLElement dom) {
		if (parent instanceof NodeModel) {
			final NodeModel node = (NodeModel) parent;
			if (tag.equals("font")) {
				final FontProperties fp = (FontProperties) userObject;
				NodeStyleModel nodeStyleModel = NodeStyleModel.getModel(node);
				if (nodeStyleModel == null) {
					nodeStyleModel = new NodeStyleModel();
					node.addExtension(nodeStyleModel);
				}
				nodeStyleModel.setFontFamilyName(fp.fontName);
				nodeStyleModel.setFontSize(fp.fontSize);
				nodeStyleModel.setItalic(fp.isItalic);
				nodeStyleModel.setBold(fp.isBold);
				return;
			}
			return;
		}
	}

	private void registerAttributeHandlers(final ReadManager reader) {
		final IAttributeHandler colorHandler = new IAttributeHandler() {
			public void setAttribute(final Object userObject, final String value) {
				final NodeModel node = (NodeModel) userObject;
				NodeStyleModel.setColor(node, ColorUtils.stringToColor(value, NodeStyleModel.getColor(node)));
			}
		};
		final IAttributeHandler alphaHandler = new IAttributeHandler() {
			public void setAttribute(final Object userObject, final String value) {
				final NodeModel node = (NodeModel) userObject;
				NodeStyleModel.setColor(node, ColorUtils.alphaToColor(value, NodeStyleModel.getColor(node)));
			}
		};
		reader.addAttributeHandler(NodeBuilder.XML_NODE, "COLOR", colorHandler);
		reader.addAttributeHandler(NodeBuilder.XML_NODE, "ALPHA", alphaHandler);
		reader.addAttributeHandler(NodeBuilder.XML_STYLENODE, "COLOR", colorHandler);
		reader.addAttributeHandler(NodeBuilder.XML_STYLENODE, "ALPHA", alphaHandler);
		final IAttributeHandler bgHandler = new IAttributeHandler() {
			public void setAttribute(final Object userObject, final String value) {
				final NodeModel node = (NodeModel) userObject;
				NodeStyleModel.setBackgroundColor(node, ColorUtils.stringToColor(value, NodeStyleModel.getBackgroundColor(node)));
			}
		};
		final IAttributeHandler bgAlphaHandler = new IAttributeHandler() {
			public void setAttribute(final Object userObject, final String value) {
				final NodeModel node = (NodeModel) userObject;
				NodeStyleModel.setBackgroundColor(node, ColorUtils.alphaToColor(value, NodeStyleModel.getBackgroundColor(node)));
			}
		};
		reader.addAttributeHandler(NodeBuilder.XML_NODE, "BACKGROUND_COLOR", bgHandler);
		reader.addAttributeHandler(NodeBuilder.XML_NODE, "BACKGROUND_ALPHA", bgAlphaHandler);
		reader.addAttributeHandler(NodeBuilder.XML_STYLENODE, "BACKGROUND_COLOR", bgHandler);
		reader.addAttributeHandler(NodeBuilder.XML_STYLENODE, "BACKGROUND_ALPHA", bgAlphaHandler);
		
		final IAttributeHandler shapeHandler = new IAttributeHandler() {
			public void setAttribute(final Object userObject, final String value) {
				final NodeModel node = (NodeModel) userObject;
				NodeStyleModel.setShape(node, value);
			}
		};
		reader.addAttributeHandler(NodeBuilder.XML_NODE, "STYLE", shapeHandler);
		reader.addAttributeHandler(NodeBuilder.XML_STYLENODE, "STYLE", shapeHandler);
		
		final IAttributeHandler shapeHorizontalMarginHandler = new IAttributeHandler() {
			public void setAttribute(final Object userObject, final String value) {
				final NodeModel node = (NodeModel) userObject;
				NodeStyleModel.setShapeHorizontalMargin(node, Quantity.fromString(value, LengthUnits.px));
			}
		};
		reader.addAttributeHandler(NodeBuilder.XML_NODE, "SHAPE_HORIZONTAL_MARGIN", shapeHorizontalMarginHandler);
		reader.addAttributeHandler(NodeBuilder.XML_STYLENODE, "SHAPE_HORIZONTAL_MARGIN", shapeHorizontalMarginHandler);
		
		final IAttributeHandler shapeVerticalMarginHandler = new IAttributeHandler() {
			public void setAttribute(final Object userObject, final String value) {
				final NodeModel node = (NodeModel) userObject;
				NodeStyleModel.setShapeVerticalMargin(node, Quantity.fromString(value, LengthUnits.px));
			}
		};
		reader.addAttributeHandler(NodeBuilder.XML_NODE, "SHAPE_VERTICAL_MARGIN", shapeVerticalMarginHandler);
		reader.addAttributeHandler(NodeBuilder.XML_STYLENODE, "SHAPE_VERTICAL_MARGIN", shapeVerticalMarginHandler);

		final IAttributeHandler uniformShapeHandler = new IAttributeHandler() {
			public void setAttribute(final Object userObject, final String value) {
				final NodeModel node = (NodeModel) userObject;
				NodeStyleModel.setShapeUniform(node, Boolean.valueOf(value));
			}
		};
		reader.addAttributeHandler(NodeBuilder.XML_NODE, "UNIFORM_SHAPE", uniformShapeHandler);
		reader.addAttributeHandler(NodeBuilder.XML_STYLENODE, "UNIFORM_SHAPE", uniformShapeHandler);

		reader.addAttributeHandler("font", "SIZE", new IAttributeHandler() {
			public void setAttribute(final Object userObject, final String value) {
				final FontProperties fp = (FontProperties) userObject;
				fp.fontSize = Integer.parseInt(value.toString());
			}
		});
		reader.addAttributeHandler("font", "NAME", new IAttributeHandler() {
			public void setAttribute(final Object userObject, final String value) {
				final FontProperties fp = (FontProperties) userObject;
				fp.fontName = value;
			}
		});
		reader.addAttributeHandler("font", "BOLD", new IAttributeHandler() {
			public void setAttribute(final Object userObject, final String value) {
				final FontProperties fp = (FontProperties) userObject;
				fp.isBold = value.equals("true");
			}
		});
		reader.addAttributeHandler("font", "ITALIC", new IAttributeHandler() {
			public void setAttribute(final Object userObject, final String value) {
				final FontProperties fp = (FontProperties) userObject;
				fp.isItalic = value.equals("true");
			}
		});
		final IAttributeHandler nodenumberingHandler = new IAttributeHandler() {
			public void setAttribute(final Object userObject, final String value) {
				final NodeModel node = (NodeModel) userObject;
				NodeStyleModel.setNodeNumbering(node, value.equals("true"));
			}
		};
		reader.addAttributeHandler(NodeBuilder.XML_NODE, "NUMBERED", nodenumberingHandler);
		reader.addAttributeHandler(NodeBuilder.XML_STYLENODE, "NUMBERED", nodenumberingHandler);

		final IAttributeHandler formatHandler = new IAttributeHandler() {
			public void setAttribute(final Object userObject, final String value) {
				final NodeModel node = (NodeModel) userObject;
				NodeStyleModel.setNodeFormat(node, value);
			}
		};
		reader.addAttributeHandler(NodeBuilder.XML_NODE, "FORMAT", formatHandler);
		reader.addAttributeHandler(NodeBuilder.XML_STYLENODE, "FORMAT", formatHandler);
		if (FreeplaneVersion.getVersion().isOlderThan(new FreeplaneVersion(1, 3, 0))) {
			// compatibility for a view 1.2.X preview versions - remove after release 1.3
			reader.addAttributeHandler(NodeBuilder.XML_NODE, "TEMPLATE", formatHandler);
			reader.addAttributeHandler(NodeBuilder.XML_STYLENODE, "TEMPLATE", formatHandler);
		}

		// save to 1.1: MAX_WIDTH="200" MAX_WIDTH_QUANTITY="200.0 px"
		// save: MAX_WIDTH="200.0 px"
		final IAttributeHandler nodeMaxNodeWidthQuantityHandler = new IAttributeHandler() {
			public void setAttribute(final Object userObject, final String value) {
				final NodeModel node = (NodeModel) userObject;
				Quantity<LengthUnits> width = Quantity.fromString(value, LengthUnits.px);
				NodeSizeModel.setMaxNodeWidth(node, width);
			}
		};
		reader.addAttributeHandler(NodeBuilder.XML_NODE, "MAX_WIDTH_QUANTITY", nodeMaxNodeWidthQuantityHandler);
		reader.addAttributeHandler(NodeBuilder.XML_STYLENODE, "MAX_WIDTH_QUANTITY", nodeMaxNodeWidthQuantityHandler);

		final IAttributeHandler nodeMaxNodeWidthHandler = new IAttributeHandler() {
			public void setAttribute(final Object userObject, final String value) {
				final NodeModel node = (NodeModel) userObject;
				if (NodeSizeModel.getMaxNodeWidth(node) == null) {
					nodeMaxNodeWidthQuantityHandler.setAttribute(node, value);
				}
			}
		};
		reader.addAttributeHandler(NodeBuilder.XML_NODE, "MAX_WIDTH", nodeMaxNodeWidthHandler);
		reader.addAttributeHandler(NodeBuilder.XML_STYLENODE, "MAX_WIDTH", nodeMaxNodeWidthHandler);

		final IAttributeHandler nodeIconSizeHandler = new IAttributeHandler() {
			public void setAttribute(final Object userObject, final String value) {
				final NodeModel node = (NodeModel) userObject;
				Quantity<LengthUnits> iconSize = Quantity.fromString(value, LengthUnits.px);
				node.getSharedData().getIcons().setIconSize(iconSize);
			}
		};
		reader.addAttributeHandler(NodeBuilder.XML_NODE, "ICON_SIZE", nodeIconSizeHandler);
		reader.addAttributeHandler(NodeBuilder.XML_STYLENODE, "ICON_SIZE", nodeIconSizeHandler);

		final IAttributeHandler nodeMinNodeWidthQuantityHandler = new IAttributeHandler() {
			public void setAttribute(final Object userObject, final String value) {
				final NodeModel node = (NodeModel) userObject;
				Quantity<LengthUnits> width = Quantity.fromString(value, LengthUnits.px);
				NodeSizeModel.setNodeMinWidth(node, width);
			}
		};
		reader.addAttributeHandler(NodeBuilder.XML_NODE, "MIN_WIDTH_QUANTITY", nodeMinNodeWidthQuantityHandler);
		reader.addAttributeHandler(NodeBuilder.XML_STYLENODE, "MIN_WIDTH_QUANTITY", nodeMinNodeWidthQuantityHandler);

		final IAttributeHandler nodeMinWidthHandler = new IAttributeHandler() {
			public void setAttribute(final Object userObject, final String value) {
				final NodeModel node = (NodeModel) userObject;
				if (NodeSizeModel.getMinNodeWidth(node) == null) {
					nodeMinNodeWidthQuantityHandler.setAttribute(node, value);
				}
			}
		};
		reader.addAttributeHandler(NodeBuilder.XML_NODE, "MIN_WIDTH", nodeMinWidthHandler);
		reader.addAttributeHandler(NodeBuilder.XML_STYLENODE, "MIN_WIDTH", nodeMinWidthHandler);

		final IAttributeHandler textAlignHandler = new IAttributeHandler() {
			public void setAttribute(final Object userObject, final String value) {
				final NodeModel node = (NodeModel) userObject;
				NodeStyleModel.setTextAlign(node, TextAlign.valueOf(value));
			}
		};
		reader.addAttributeHandler(NodeBuilder.XML_NODE, "TEXT_ALIGN", textAlignHandler);
		reader.addAttributeHandler(NodeBuilder.XML_STYLENODE, "TEXT_ALIGN", textAlignHandler);

		final IAttributeHandler borderWidthMatchesEdgeWidthHandler = new IAttributeHandler() {
			public void setAttribute(final Object userObject, final String value) {
				final NodeModel node = (NodeModel) userObject;
				NodeBorderModel.setBorderWidthMatchesEdgeWidth(node, Boolean.valueOf(value));
			}
		};
		reader.addAttributeHandler(NodeBuilder.XML_NODE, "BORDER_WIDTH_LIKE_EDGE", borderWidthMatchesEdgeWidthHandler);
		reader.addAttributeHandler(NodeBuilder.XML_STYLENODE, "BORDER_WIDTH_LIKE_EDGE", borderWidthMatchesEdgeWidthHandler);

		final IAttributeHandler borderWidthHandler = new IAttributeHandler() {
			public void setAttribute(final Object userObject, final String value) {
				final NodeModel node = (NodeModel) userObject;
				NodeBorderModel.setBorderWidth(node, Quantity.fromString(value, LengthUnits.px));
			}
		};
		reader.addAttributeHandler(NodeBuilder.XML_NODE, "BORDER_WIDTH", borderWidthHandler);
		reader.addAttributeHandler(NodeBuilder.XML_STYLENODE, "BORDER_WIDTH", borderWidthHandler);

		final IAttributeHandler borderDashMatchesEdgeDashHandler = new IAttributeHandler() {
			public void setAttribute(final Object userObject, final String value) {
				final NodeModel node = (NodeModel) userObject;
				NodeBorderModel.setBorderDashMatchesEdgeDash(node, Boolean.valueOf(value));
			}
		};
		reader.addAttributeHandler(NodeBuilder.XML_NODE, "BORDER_DASH_LIKE_EDGE", borderDashMatchesEdgeDashHandler);
		reader.addAttributeHandler(NodeBuilder.XML_STYLENODE, "BORDER_DASH_LIKE_EDGE", borderDashMatchesEdgeDashHandler);

		final IAttributeHandler borderDashHandler = new IAttributeHandler() {
			public void setAttribute(final Object userObject, final String value) {
				final NodeModel node = (NodeModel) userObject;
				NodeBorderModel.setBorderDash(node, DashVariant.valueOf(value));
			}
		};
		reader.addAttributeHandler(NodeBuilder.XML_NODE, "BORDER_DASH", borderDashHandler);
		reader.addAttributeHandler(NodeBuilder.XML_STYLENODE, "BORDER_DASH", borderDashHandler);

		final IAttributeHandler borderColorMatchesEdgeColorHandler = new IAttributeHandler() {
			public void setAttribute(final Object userObject, final String value) {
				final NodeModel node = (NodeModel) userObject;
				NodeBorderModel.setBorderColorMatchesEdgeColor(node, Boolean.valueOf(value));
			}
		};
		reader.addAttributeHandler(NodeBuilder.XML_NODE, "BORDER_COLOR_LIKE_EDGE", borderColorMatchesEdgeColorHandler);
		reader.addAttributeHandler(NodeBuilder.XML_STYLENODE, "BORDER_COLOR_LIKE_EDGE", borderColorMatchesEdgeColorHandler);

		final IAttributeHandler borderColorHandler = new IAttributeHandler() {
			public void setAttribute(final Object userObject, final String value) {
				final NodeModel node = (NodeModel) userObject;
				NodeBorderModel.setBorderColor(node, ColorUtils.stringToColor(value, NodeBorderModel.getBorderColor(node)));
			}
		};
		final IAttributeHandler borderColorAlphaHandler = new IAttributeHandler() {
			public void setAttribute(final Object userObject, final String value) {
				final NodeModel node = (NodeModel) userObject;
				NodeBorderModel.setBorderColor(node, ColorUtils.alphaToColor(value, NodeBorderModel.getBorderColor(node)));
			}
		};

		
		reader.addAttributeHandler(NodeBuilder.XML_NODE, "BORDER_COLOR", borderColorHandler);
		reader.addAttributeHandler(NodeBuilder.XML_NODE, "BORDER_COLOR_ALPHA", borderColorAlphaHandler);
		reader.addAttributeHandler(NodeBuilder.XML_STYLENODE, "BORDER_COLOR", borderColorHandler);
		reader.addAttributeHandler(NodeBuilder.XML_STYLENODE, "BORDER_COLOR_ALPHA", borderColorAlphaHandler);
	}

	/**
	 */
	public void registerBy(final ReadManager reader, final WriteManager writer) {
		reader.addElementHandler("font", this);
		registerAttributeHandlers(reader);
		writer.addAttributeWriter(NodeBuilder.XML_NODE, this);
		writer.addAttributeWriter(NodeBuilder.XML_STYLENODE, this);
		writer.addElementWriter(NodeBuilder.XML_NODE, this);
		writer.addElementWriter(NodeBuilder.XML_STYLENODE, this);
		writer.addExtensionElementWriter(NodeStyleModel.class, this);
		writer.addExtensionAttributeWriter(NodeStyleModel.class, this);
		writer.addExtensionAttributeWriter(NodeSizeModel.class, this);
		writer.addExtensionAttributeWriter(NodeBorderModel.class, this);
	}

	public void setAttributes(final String tag, final Object node, final XMLElement attributes) {
	}

	public void writeAttributes(final ITreeWriter writer, final Object userObject, final String tag) {
		final boolean forceFormatting = Boolean.TRUE.equals(writer.getHint(MapWriter.WriterHint.FORCE_FORMATTING));
		if (!forceFormatting) {
			return;
		}
		final NodeModel node = (NodeModel) userObject;
		writeAttributes(writer, node, (NodeStyleModel)null, true);
		writeAttributes(writer, node, (NodeSizeModel)null, true);
		writeAttributes(writer, node, (NodeBorderModel)null, true);
	}

	public void writeAttributes(final ITreeWriter writer, final Object userObject, final IExtension extension) {
		final boolean forceFormatting = Boolean.TRUE.equals(writer.getHint(MapWriter.WriterHint.FORCE_FORMATTING));
		if (forceFormatting) {
			return;
		}
		final NodeModel node = (NodeModel)userObject;
		if(extension instanceof NodeStyleModel){
			final NodeStyleModel style = (NodeStyleModel) extension;
			writeAttributes(writer, node, style, false);
			return;
		}
		if(extension instanceof NodeSizeModel){
			final NodeSizeModel size = (NodeSizeModel) extension;
			writeAttributes(writer, node, size, false);
			return;
		}
		if(extension instanceof NodeBorderModel){
			final NodeBorderModel border = (NodeBorderModel) extension;
			writeAttributes(writer, null, border, false);
			return;
		}

	}

	private void writeAttributes(final ITreeWriter writer, final NodeModel node, final NodeStyleModel style,
	                             final boolean forceFormatting) {
		final Color color = forceFormatting ? nsc.getColor(node) : style.getColor();
		if (color != null) {
			ColorUtils.addColorAttributes(writer, "COLOR", "ALPHA", color);
		}
		final Color backgroundColor = forceFormatting ? nsc.getBackgroundColor(node) : style.getBackgroundColor();
		if (backgroundColor != null) {
			ColorUtils.addColorAttributes(writer, "BACKGROUND_COLOR", "BACKGROUND_ALPHA", backgroundColor);
		}
		final ShapeConfigurationModel shapeConfiguration = forceFormatting ? nsc.getShapeConfiguration(node) : style.getShapeConfiguration();
		final Shape shape = shapeConfiguration.getShape();
		if (shape != null) {
			writer.addAttribute("STYLE", shape.toString());
		}
		final Quantity<LengthUnits> shapeHorizontalMargin = shapeConfiguration.getHorizontalMargin();
		if (! shapeHorizontalMargin.equals(ShapeConfigurationModel.DEFAULT_MARGIN)) {
			BackwardCompatibleQuantityWriter.forWriter(writer).writeQuantity("SHAPE_HORIZONTAL_MARGIN", shapeHorizontalMargin);
		}
		final Quantity<LengthUnits> shapeVerticalMargin = shapeConfiguration.getVerticalMargin();
		if (! shapeVerticalMargin.equals(ShapeConfigurationModel.DEFAULT_MARGIN)) {
			BackwardCompatibleQuantityWriter.forWriter(writer).writeQuantity("SHAPE_VERTICAL_MARGIN", shapeVerticalMargin);
		}
		final boolean uniformShape = shapeConfiguration.isUniform();
		if (uniformShape) {
			writer.addAttribute("UNIFORM_SHAPE", "true");
		}
		final Boolean numbered = forceFormatting ? nsc.getNodeNumbering(node) : style.getNodeNumbering();
		if (numbered != null && numbered) {
			writer.addAttribute("NUMBERED", numbered.toString());
		}
		final String format = forceFormatting ? nsc.getNodeFormat(node) : style.getNodeFormat();
		if (format != null) {
			writer.addAttribute("FORMAT", format);
		}
		final TextAlign textAlign = forceFormatting ? nsc.getTextAlign(node) : style.getTextAlign();
		if (textAlign != null) {
			writer.addAttribute("TEXT_ALIGN", textAlign.toString());
		}

	}

	private void writeAttributes(final ITreeWriter writer, final NodeModel node, final NodeSizeModel size,
	                             final boolean forceFormatting) {
		final Quantity<LengthUnits> maxTextWidth = forceFormatting ? nsc.getMaxWidth(node) : size.getMaxNodeWidth();
		if (maxTextWidth != null) {
			BackwardCompatibleQuantityWriter.forWriter(writer).writeQuantity("MAX_WIDTH", maxTextWidth);
		}

		final Quantity<LengthUnits> minTextWidth = forceFormatting ? nsc.getMinWidth(node) : size.getMinNodeWidth();
		if (minTextWidth != null) {
			BackwardCompatibleQuantityWriter.forWriter(writer).writeQuantity("MIN_WIDTH", minTextWidth);
		}
	}
	
	private void writeAttributes(final ITreeWriter writer, final NodeModel node, final NodeBorderModel border,
			final boolean forceFormatting) {
		final Boolean borderWidthMatchesEdgeWidth = forceFormatting ? nsc.getBorderWidthMatchesEdgeWidth(node) : border.getBorderWidthMatchesEdgeWidth();
		if (borderWidthMatchesEdgeWidth != null) {
			writer.addAttribute("BORDER_WIDTH_LIKE_EDGE", borderWidthMatchesEdgeWidth.toString());
		}
		final Quantity<LengthUnits> borderWidth = forceFormatting ? nsc.getBorderWidth(node) : border.getBorderWidth();
		if (borderWidth != null) {
			writer.addAttribute("BORDER_WIDTH", borderWidth.toString());
		}
		final Boolean borderColorMatchesEdgeColor = forceFormatting ? nsc.getBorderColorMatchesEdgeColor(node) : border.getBorderColorMatchesEdgeColor();
		if (borderColorMatchesEdgeColor != null) {
			writer.addAttribute("BORDER_COLOR_LIKE_EDGE", borderColorMatchesEdgeColor.toString());
		}
		final Color borderColor = forceFormatting ? nsc.getBorderColor(node) : border.getBorderColor();
		if (borderColor != null) {
			ColorUtils.addColorAttributes(writer, "BORDER_COLOR", "BORDER_COLOR_ALPHA", borderColor);
		}
		final Boolean borderDashMatchesEdgeDash = forceFormatting ? nsc.getBorderDashMatchesEdgeDash(node) : border.getBorderDashMatchesEdgeDash();
		if (borderDashMatchesEdgeDash != null) {
			writer.addAttribute("BORDER_DASH_LIKE_EDGE", borderDashMatchesEdgeDash.toString());
		}
		DashVariant borderDash = forceFormatting ? nsc.getBorderDash(node) : border.getBorderDash();
		if (borderDash != null) {
			writer.addAttribute("BORDER_DASH", borderDash.name());
		}
	}
	
	public void writeContent(final ITreeWriter writer, final Object userObject, final String tag) throws IOException {
		final boolean forceFormatting = Boolean.TRUE.equals(writer.getHint(MapWriter.WriterHint.FORCE_FORMATTING));
		if (!forceFormatting) {
			return;
		}
		final NodeModel node = (NodeModel) userObject;
		writeContent(writer, node, null, true);
	}

	public void writeContent(final ITreeWriter writer, final Object userObject, final IExtension extension)
	        throws IOException {
		final boolean forceFormatting = Boolean.TRUE.equals(writer.getHint(MapWriter.WriterHint.FORCE_FORMATTING));
		if (forceFormatting) {
			return;
		}
		final NodeStyleModel style = (NodeStyleModel) extension;
		writeContent(writer, null, style, false);
	}

	private void writeContent(final ITreeWriter writer, final NodeModel node, final NodeStyleModel style,
	                          final boolean forceFormatting) throws IOException {
		if(! NodeWriter.shouldWriteSharedContent(writer))
			return;
		if (forceFormatting || style != null) {
			final XMLElement fontElement = new XMLElement();
			fontElement.setName("font");
			boolean isRelevant = forceFormatting;
			final String fontFamilyName = forceFormatting ? nsc.getFontFamilyName(node) : style.getFontFamilyName();
			if (fontFamilyName != null) {
				fontElement.setAttribute("NAME", fontFamilyName);
				isRelevant = true;
			}
			final Integer fontSize = forceFormatting ? Integer.valueOf(nsc.getFontSize(node)) : style.getFontSize();
			if (fontSize != null) {
				fontElement.setAttribute("SIZE", Integer.toString(fontSize));
				isRelevant = true;
			}
			final Boolean bold = forceFormatting ? Boolean.valueOf(nsc.isBold(node)) : style.isBold();
			if (bold != null) {
				fontElement.setAttribute("BOLD", bold ? "true" : "false");
				isRelevant = true;
			}
			final Boolean italic = forceFormatting ? Boolean.valueOf(nsc.isItalic(node)) : style.isItalic();
			if (italic != null) {
				fontElement.setAttribute("ITALIC", italic ? "true" : "false");
				isRelevant = true;
			}
			if (isRelevant) {
				writer.addElement(style, fontElement);
			}
		}
	}
}
