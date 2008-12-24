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
import org.freeplane.map.IPropertyGetter;
import org.freeplane.map.PropertyChain;
import org.freeplane.map.tree.MapController;
import org.freeplane.map.tree.NodeModel;
import org.freeplane.map.tree.view.MapView;
import org.freeplane.modes.ModeController;

/**
 * @author Dimitry Polivaev
 */
public class NodeStyleController {
	final private PropertyChain<Color, NodeModel> backgroundColorHandlers;
	final private PropertyChain<String, NodeModel> fontFamilyHandlers;
	final private PropertyChain<Integer, NodeModel> fontSizeHandlers;
	final private PropertyChain<Boolean, NodeModel> fontItalicHandlers;
	final private PropertyChain<Boolean, NodeModel> fontBoldHandlers;
	final private ModeController modeController;
	final private PropertyChain<String, NodeModel> shapeHandlers;
	final private PropertyChain<Color, NodeModel> textColorHandlers;

	public NodeStyleController(final ModeController modeController) {
		this.modeController = modeController;
		fontFamilyHandlers = new PropertyChain<String, NodeModel>();
		fontSizeHandlers = new PropertyChain<Integer, NodeModel>();
		fontItalicHandlers = new PropertyChain<Boolean, NodeModel>();
		fontBoldHandlers = new PropertyChain<Boolean, NodeModel>();
		textColorHandlers = new PropertyChain<Color, NodeModel>();
		backgroundColorHandlers = new PropertyChain<Color, NodeModel>();
		shapeHandlers = new PropertyChain<String, NodeModel>();
		addFontFamilyGetter(PropertyChain.NODE, new IPropertyGetter<String, NodeModel>() {
			public String getProperty(final NodeModel node) {
				final NodeStyleModel nodeStyleModel = node.getNodeStyleModel();
				return nodeStyleModel == null ? null : nodeStyleModel.getFontFamilyName();
			}
		});
		addFontSizeGetter(PropertyChain.NODE, new IPropertyGetter<Integer, NodeModel>() {
			public Integer getProperty(final NodeModel node) {
				final NodeStyleModel nodeStyleModel = node.getNodeStyleModel();
				return nodeStyleModel == null ? null : nodeStyleModel.getFontSize();
			}
		});

		addFontBoldGetter(PropertyChain.NODE, new IPropertyGetter<Boolean, NodeModel>() {
			public Boolean getProperty(final NodeModel node) {
				final NodeStyleModel nodeStyleModel = node.getNodeStyleModel();
				return nodeStyleModel == null ? null : nodeStyleModel.isBold();
			}
		});

		addFontItalicGetter(PropertyChain.NODE, new IPropertyGetter<Boolean, NodeModel>() {
			public Boolean getProperty(final NodeModel node) {
				final NodeStyleModel nodeStyleModel = node.getNodeStyleModel();
				return nodeStyleModel == null ? null : nodeStyleModel.isItalic();
			}
		});

		addColorGetter(PropertyChain.NODE, new IPropertyGetter<Color, NodeModel>() {
			public Color getProperty(final NodeModel node) {
				return node.getColor();
			}
		});
		addColorGetter(PropertyChain.DEFAULT, new IPropertyGetter<Color, NodeModel>() {
			public Color getProperty(final NodeModel node) {
				return MapView.standardNodeTextColor;
			}
		});
		addBackgroundColorGetter(PropertyChain.NODE, new IPropertyGetter<Color, NodeModel>() {
			public Color getProperty(final NodeModel node) {
				return node.getBackgroundColor();
			}
		});
		addShapeGetter(PropertyChain.NODE, new IPropertyGetter<String, NodeModel>() {
			public String getProperty(final NodeModel node) {
				return getShape(node);
			}

			private String getShape(final NodeModel node) {
				String returnedString = node.getShape(); /*
													    			    						    						    						    			    						    														 * Style string
													    			    						    						    						    			    						    														 * returned
													    			    						    						    						    			    						    														 */
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

	public IPropertyGetter<String, NodeModel> addFontFamilyGetter(
		final Integer key,
		final IPropertyGetter<String, NodeModel> getter) {
		return fontFamilyHandlers.addGetter(key, getter);
	}

	public IPropertyGetter<Integer, NodeModel> addFontSizeGetter(
		final Integer key,
		final IPropertyGetter<Integer, NodeModel> getter) {
		return fontSizeHandlers.addGetter(key, getter);
	}

	public IPropertyGetter<Boolean, NodeModel> addFontItalicGetter(
		final Integer key,
		final IPropertyGetter<Boolean, NodeModel> getter) {
		return fontItalicHandlers.addGetter(key, getter);
	}

	public IPropertyGetter<Boolean, NodeModel> addFontBoldGetter(
		final Integer key,
		final IPropertyGetter<Boolean, NodeModel> getter) {
		return fontBoldHandlers.addGetter(key, getter);
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
		String family = fontFamilyHandlers.getProperty(node);
		Integer size = fontSizeHandlers.getProperty(node);
		Boolean bold = fontBoldHandlers.getProperty(node);
		Boolean italic = fontItalicHandlers.getProperty(node);
		Font font = Controller.getResourceController().getDefaultFont();
		if(family == null && size == null && bold == null && italic == null){
			return font;
		}
		if(family == null){
			family = font.getFamily();
		}
		if(size == null){
			size = font.getSize();
		}
		if(bold == null){
			bold = font.isBold();
		}
		if(italic == null){
			italic = font.isItalic();
		}
		int style = 0;
		if(bold){
			style += Font.BOLD;
		}
		if(italic){
			style += Font.ITALIC;
		}
		return new Font(family, style, size);
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

	public IPropertyGetter<String, NodeModel> removeFontFamilyGetter(
		final Integer key) {
		return fontFamilyHandlers.removeGetter(key);
	}

	public IPropertyGetter<Integer, NodeModel> removeFontSizeGetter(
		final Integer key) {
		return fontSizeHandlers.removeGetter(key);
	}

	public IPropertyGetter<Boolean, NodeModel> removeFontItalicGetter(
		final Integer key) {
		return fontItalicHandlers.removeGetter(key);
	}

	public IPropertyGetter<Boolean, NodeModel> removeFontBoldGetter(
		final Integer key) {
		return fontBoldHandlers.removeGetter(key);
	}


	public IPropertyGetter<String, NodeModel> removeShapeGetter(final Integer key) {
		return shapeHandlers.removeGetter(key);
	}
}
