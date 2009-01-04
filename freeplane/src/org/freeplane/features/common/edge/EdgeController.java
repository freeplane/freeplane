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
package org.freeplane.features.common.edge;

import java.awt.Color;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.extension.IExtension;
import org.freeplane.core.io.ReadManager;
import org.freeplane.core.io.WriteManager;
import org.freeplane.core.io.xml.TreeXmlReader;
import org.freeplane.core.modecontroller.ExclusivePropertyChain;
import org.freeplane.core.modecontroller.IPropertyGetter;
import org.freeplane.core.modecontroller.MapController;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.resources.ui.IFreemindPropertyListener;

/**
 * @author Dimitry Polivaev
 */
public class EdgeController implements IExtension {
	protected static class EdgePropertyListener implements IFreemindPropertyListener {
		public void propertyChanged(final String propertyName, final String newValue,
		                            final String oldValue) {
			if (propertyName.equals(ResourceController.RESOURCES_EDGE_COLOR)) {
				standardColor = TreeXmlReader.xmlToColor(newValue);
			}
			if (propertyName.equals(ResourceController.RESOURCES_EDGE_STYLE)) {
				standardStyle = newValue;
			}
		}
	}

	static final int DEFAULT_WIDTH = EdgeModel.WIDTH_PARENT;
	private static EdgePropertyListener listener = null;
	private static Color standardColor = null;
	private static String standardStyle = null;

	public static EdgeController getController(final ModeController modeController) {
		return (EdgeController) modeController.getExtension(EdgeController.class);
	}

	public static void install(final ModeController modeController,
	                           final EdgeController edgeController) {
		modeController.addExtension(EdgeController.class, edgeController);
	}

	final private ExclusivePropertyChain<Color, NodeModel> colorHandlers;
	final private ExclusivePropertyChain<String, NodeModel> styleHandlers;
	final private ExclusivePropertyChain<Integer, NodeModel> widthHandlers;

	public EdgeController(final ModeController modeController) {
		colorHandlers = new ExclusivePropertyChain<Color, NodeModel>();
		styleHandlers = new ExclusivePropertyChain<String, NodeModel>();
		widthHandlers = new ExclusivePropertyChain<Integer, NodeModel>();
		updateStandards(modeController);
		if (listener == null) {
			listener = new EdgePropertyListener();
			Controller.getResourceController().addPropertyChangeListener(listener);
		}
		addColorGetter(ExclusivePropertyChain.NODE, new IPropertyGetter<Color, NodeModel>() {
			public Color getProperty(final NodeModel node, final Color currentValue) {
				final EdgeModel edge = EdgeModel.getModel(node);
				return edge == null ? null : edge.getColor();
			}
		});
		addColorGetter(ExclusivePropertyChain.DEFAULT, new IPropertyGetter<Color, NodeModel>() {
			public Color getProperty(final NodeModel node, final Color currentValue) {
				if (node.isRoot()) {
					return standardColor;
				}
				return getColor(node.getParentNode());
			}
		});
		addStyleGetter(ExclusivePropertyChain.NODE, new IPropertyGetter<String, NodeModel>() {
			public String getProperty(final NodeModel node, final String currentValue) {
				final EdgeModel edge = EdgeModel.getModel(node);
				return edge == null ? null : edge.getStyle();
			}
		});
		addStyleGetter(ExclusivePropertyChain.DEFAULT, new IPropertyGetter<String, NodeModel>() {
			public String getProperty(final NodeModel node, final String currentValue) {
				if (node.isRoot()) {
					return standardStyle;
				}
				return getStyle(node.getParentNode());
			}
		});
		addWidthGetter(ExclusivePropertyChain.NODE, new IPropertyGetter<Integer, NodeModel>() {
			public Integer getProperty(final NodeModel node, final Integer currentValue) {
				final EdgeModel edge = EdgeModel.getModel(node);
				int width = edge == null ? DEFAULT_WIDTH : edge.getWidth();
				if (width == EdgeModel.WIDTH_PARENT) {
					if (node.isRoot()) {
						width = EdgeModel.WIDTH_THIN;
						return new Integer(width);
					}
					return getWidth(node.getParentNode());
				}
				return width != DEFAULT_WIDTH ? new Integer(width) : null;
			}
		});
		addWidthGetter(ExclusivePropertyChain.DEFAULT, new IPropertyGetter<Integer, NodeModel>() {
			public Integer getProperty(final NodeModel node, final Integer currentValue) {
				return new Integer(EdgeModel.WIDTH_THIN);
			}
		});
		final MapController mapController = modeController.getMapController();
		final ReadManager readManager = mapController.getReadManager();
		final WriteManager writeManager = mapController.getWriteManager();
		final EdgeBuilder edgeBuilder = new EdgeBuilder();
		edgeBuilder.registerBy(readManager, writeManager);
	}

	public IPropertyGetter<Color, NodeModel> addColorGetter(
	                                                        final Integer key,
	                                                        final IPropertyGetter<Color, NodeModel> getter) {
		return colorHandlers.addGetter(key, getter);
	}

	public IPropertyGetter<String, NodeModel> addStyleGetter(
	                                                         final Integer key,
	                                                         final IPropertyGetter<String, NodeModel> getter) {
		return styleHandlers.addGetter(key, getter);
	}

	public IPropertyGetter<Integer, NodeModel> addWidthGetter(
	                                                          final Integer key,
	                                                          final IPropertyGetter<Integer, NodeModel> getter) {
		return widthHandlers.addGetter(key, getter);
	}

	public Color getColor(final NodeModel node) {
		return colorHandlers.getProperty(node);
	}

	public String getStyle(final NodeModel node) {
		return styleHandlers.getProperty(node);
	}

	public int getWidth(final NodeModel node) {
		return widthHandlers.getProperty(node).intValue();
	}

	public IPropertyGetter<Color, NodeModel> removeColorGetter(final Integer key) {
		return colorHandlers.removeGetter(key);
	}

	public IPropertyGetter<String, NodeModel> removeStyleGetter(final Integer key) {
		return styleHandlers.removeGetter(key);
	}

	public IPropertyGetter<Integer, NodeModel> removeWidthGetter(final Integer key) {
		return widthHandlers.removeGetter(key);
	}

	private void updateStandards(final ModeController controller) {
		if (standardColor == null) {
			final String stdColor = Controller.getResourceController().getProperty(
			    ResourceController.RESOURCES_EDGE_COLOR);
			if (stdColor != null && stdColor.length() == 7) {
				standardColor = TreeXmlReader.xmlToColor(stdColor);
			}
			else {
				standardColor = Color.RED;
			}
		}
		if (standardStyle == null) {
			final String stdStyle = Controller.getResourceController().getProperty(
			    ResourceController.RESOURCES_EDGE_STYLE);
			if (stdStyle != null) {
				standardStyle = stdStyle;
			}
		}
	}
}
