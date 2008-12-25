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
package org.freeplane.map.nodestyle;

import java.awt.Color;
import java.awt.Font;

import org.freeplane.controller.Controller;
import org.freeplane.controller.resources.ResourceController;
import org.freeplane.io.ReadManager;
import org.freeplane.io.WriteManager;
import org.freeplane.map.CombinedPropertyChain;
import org.freeplane.map.ExclusivePropertyChain;
import org.freeplane.map.IPropertyGetter;
import org.freeplane.map.tree.MapController;
import org.freeplane.map.tree.NodeModel;
import org.freeplane.map.tree.view.MapView;
import org.freeplane.modes.ModeController;

/**
 * @author Dimitry Polivaev
 */
public class NodeStyleController {
	final private ExclusivePropertyChain<Color, NodeModel> backgroundColorHandlers;
	final private CombinedPropertyChain<Font, NodeModel> fontHandlers;
	final private ModeController modeController;
	final private ExclusivePropertyChain<String, NodeModel> shapeHandlers;
	final private ExclusivePropertyChain<Color, NodeModel> textColorHandlers;

	public NodeStyleController(final ModeController modeController) {
		this.modeController = modeController;
		fontHandlers = new CombinedPropertyChain<Font, NodeModel>();
		textColorHandlers = new ExclusivePropertyChain<Color, NodeModel>();
		backgroundColorHandlers = new ExclusivePropertyChain<Color, NodeModel>();
		shapeHandlers = new ExclusivePropertyChain<String, NodeModel>();
		addFontGetter(CombinedPropertyChain.NODE, new IPropertyGetter<Font, NodeModel>() {
			public Font getProperty(final NodeModel node, final Font font) {
				final NodeStyleModel nodeStyleModel = node.getNodeStyleModel();
				if (nodeStyleModel == null) {
					return font;
				}
				String family = nodeStyleModel.getFontFamilyName();
				Integer size = nodeStyleModel.getFontSize();
				Boolean bold = nodeStyleModel.isBold();
				Boolean italic = nodeStyleModel.isItalic();
				if (family == null && size == null && bold == null && italic == null) {
					return font;
				}
				if (family == null) {
					family = font.getFamily();
				}
				if (size == null) {
					size = font.getSize();
				}
				if (bold == null) {
					bold = font.isBold();
				}
				if (italic == null) {
					italic = font.isItalic();
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
		});
		addFontGetter(CombinedPropertyChain.DEFAULT, new IPropertyGetter<Font, NodeModel>() {
			public Font getProperty(final NodeModel node, final Font currentValue) {
				return Controller.getResourceController().getDefaultFont();
			}
		});
		addColorGetter(ExclusivePropertyChain.NODE, new IPropertyGetter<Color, NodeModel>() {
			public Color getProperty(final NodeModel node, final Color currentValue) {
				return node.getColor();
			}
		});
		addColorGetter(ExclusivePropertyChain.DEFAULT, new IPropertyGetter<Color, NodeModel>() {
			public Color getProperty(final NodeModel node, final Color currentValue) {
				return MapView.standardNodeTextColor;
			}
		});
		addBackgroundColorGetter(ExclusivePropertyChain.NODE,
		    new IPropertyGetter<Color, NodeModel>() {
			    public Color getProperty(final NodeModel node, final Color currentValue) {
				    return node.getBackgroundColor();
			    }
		    });
		addShapeGetter(ExclusivePropertyChain.NODE, new IPropertyGetter<String, NodeModel>() {
			public String getProperty(final NodeModel node, final String currentValue) {
				return getShape(node);
			}

			private String getShape(final NodeModel node) {
				String returnedString = node.getShape(); 
				if (node.getShape() == null) {
					if (node.isRoot()) {
						returnedString = Controller.getResourceController().getProperty(
						    ResourceController.RESOURCES_ROOT_NODE_SHAPE);
					}
					else {
						final String stdstyle = Controller.getResourceController().getProperty(
						    ResourceController.RESOURCES_NODE_SHAPE);
						if (stdstyle.equals(NodeStyleModel.SHAPE_AS_PARENT)) {
							returnedString = getShape(node.getParentNode());
						}
						else {
							returnedString = stdstyle;
						}
					}
				}
				else if (node.isRoot() && node.getShape().equals(NodeStyleModel.SHAPE_AS_PARENT)) {
					returnedString = Controller.getResourceController().getProperty(
					    ResourceController.RESOURCES_ROOT_NODE_SHAPE);
				}
				else if (node.getShape().equals(NodeStyleModel.SHAPE_AS_PARENT)) {
					returnedString = getShape(node.getParentNode());
				}
				if (returnedString.equals(NodeStyleModel.SHAPE_COMBINED)) {
					if (node.getModeController().getMapController().isFolded(node)) {
						return NodeStyleModel.STYLE_BUBBLE;
					}
					else {
						return NodeStyleModel.STYLE_FORK;
					}
				}
				return returnedString;
			}
		});
		final MapController mapController = modeController.getMapController();
		final ReadManager readManager = mapController.getReadManager();
		final WriteManager writeManager = mapController.getWriteManager();
		final NodeStyleBuilder styleBuilder = new NodeStyleBuilder();
		styleBuilder.registerBy(readManager, writeManager);
	}

	public IPropertyGetter<Color, NodeModel> addBackgroundColorGetter(
	                                                                  final Integer key,
	                                                                  final IPropertyGetter<Color, NodeModel> getter) {
		return backgroundColorHandlers.addGetter(key, getter);
	}

	public IPropertyGetter<Color, NodeModel> addColorGetter(
	                                                        final Integer key,
	                                                        final IPropertyGetter<Color, NodeModel> getter) {
		return textColorHandlers.addGetter(key, getter);
	}

	public IPropertyGetter<Font, NodeModel> addFontGetter(
	                                                      final Integer key,
	                                                      final IPropertyGetter<Font, NodeModel> getter) {
		return fontHandlers.addGetter(key, getter);
	}

	public IPropertyGetter<String, NodeModel> addShapeGetter(
	                                                         final Integer key,
	                                                         final IPropertyGetter<String, NodeModel> getter) {
		return shapeHandlers.addGetter(key, getter);
	}

	public Color getBackgroundColor(final NodeModel node) {
		return backgroundColorHandlers.getProperty(node);
	}

	public Color getColor(final NodeModel node) {
		return textColorHandlers.getProperty(node);
	}

	public Font getFont(final NodeModel node) {
		return fontHandlers.getProperty(node);
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
		return shapeHandlers.getProperty(node);
	}

	public boolean isBold(final NodeModel node) {
		return getFont(node).isBold();
	}

	public boolean isItalic(final NodeModel node) {
		return getFont(node).isItalic();
	}

	public IPropertyGetter<Color, NodeModel> removeBackgroundColorGetter(final Integer key) {
		return backgroundColorHandlers.removeGetter(key);
	}

	public IPropertyGetter<Color, NodeModel> removeColorGetter(final Integer key) {
		return textColorHandlers.removeGetter(key);
	}

	public IPropertyGetter<Font, NodeModel> removeFontGetter(final Integer key) {
		return fontHandlers.removeGetter(key);
	}

	public IPropertyGetter<String, NodeModel> removeShapeGetter(final Integer key) {
		return shapeHandlers.removeGetter(key);
	}
}
