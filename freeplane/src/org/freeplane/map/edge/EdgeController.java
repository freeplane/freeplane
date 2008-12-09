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
package org.freeplane.map.edge;

import java.awt.Color;

import org.freeplane.controller.Controller;
import org.freeplane.controller.resources.ResourceController;
import org.freeplane.io.ReadManager;
import org.freeplane.io.WriteManager;
import org.freeplane.main.Tools;
import org.freeplane.map.IPropertyGetter;
import org.freeplane.map.PropertyChain;
import org.freeplane.map.tree.MapController;
import org.freeplane.map.tree.NodeModel;
import org.freeplane.modes.ModeController;

import deprecated.freemind.preferences.IFreemindPropertyListener;

/**
 * @author Dimitry Polivaev
 */
public class EdgeController {
	protected static class EdgePropertyListener implements
	        IFreemindPropertyListener {
		public void propertyChanged(final String propertyName,
		                            final String newValue, final String oldValue) {
			if (propertyName.equals(ResourceController.RESOURCES_EDGE_COLOR)) {
				standardColor = Tools.xmlToColor(newValue);
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
	final private PropertyChain<Color, NodeModel> colorHandlers;
	final private PropertyChain<String, NodeModel> styleHandlers;
	final private PropertyChain<Integer, NodeModel> widthHandlers;

	public EdgeController(final ModeController modeController) {
		colorHandlers = new PropertyChain<Color, NodeModel>();
		styleHandlers = new PropertyChain<String, NodeModel>();
		widthHandlers = new PropertyChain<Integer, NodeModel>();
		updateStandards(modeController);
		if (listener == null) {
			listener = new EdgePropertyListener();
			Controller.getResourceController().addPropertyChangeListener(
			    listener);
		}
		addColorGetter(PropertyChain.NODE,
		    new IPropertyGetter<Color, NodeModel>() {
			    public Color getProperty(final NodeModel node) {
				    final EdgeModel edge = node.getEdge();
				    return edge == null ? null : edge.getColor();
			    }
		    });
		addColorGetter(PropertyChain.DEFAULT,
		    new IPropertyGetter<Color, NodeModel>() {
			    public Color getProperty(final NodeModel node) {
				    if (node.isRoot()) {
					    return standardColor;
				    }
				    return getProperty(node.getParentNode());
			    }
		    });
		addStyleGetter(PropertyChain.NODE,
		    new IPropertyGetter<String, NodeModel>() {
			    public String getProperty(final NodeModel node) {
				    final EdgeModel edge = node.getEdge();
				    return edge == null ? null : edge.getStyle();
			    }
		    });
		addStyleGetter(PropertyChain.DEFAULT,
		    new IPropertyGetter<String, NodeModel>() {
			    public String getProperty(final NodeModel node) {
				    if (node.isRoot()) {
					    return standardStyle;
				    }
				    return getProperty(node.getParentNode());
			    }
		    });
		addWidthGetter(PropertyChain.NODE,
		    new IPropertyGetter<Integer, NodeModel>() {
			    public Integer getProperty(final NodeModel node) {
				    final EdgeModel edge = node.getEdge();
				    int width = edge == null ? DEFAULT_WIDTH : edge.getWidth();
				    if (width == EdgeModel.WIDTH_PARENT) {
					    if (node.isRoot()) {
						    width = EdgeModel.WIDTH_THIN;
						    return new Integer(width);
					    }
					    return getProperty(node.getParentNode());
				    }
				    return width != DEFAULT_WIDTH ? new Integer(width) : null;
			    }
		    });
		addWidthGetter(PropertyChain.DEFAULT,
		    new IPropertyGetter<Integer, NodeModel>() {
			    public Integer getProperty(final NodeModel node) {
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

	public IPropertyGetter<String, NodeModel> removeStyleGetter(
	                                                            final Integer key) {
		return styleHandlers.removeGetter(key);
	}

	public IPropertyGetter<Integer, NodeModel> removeWidthGetter(
	                                                             final Integer key) {
		return widthHandlers.removeGetter(key);
	}

	private void updateStandards(final ModeController controller) {
		if (standardColor == null) {
			final String stdColor = Controller.getResourceController()
			    .getProperty(ResourceController.RESOURCES_EDGE_COLOR);
			if (stdColor != null && stdColor.length() == 7) {
				standardColor = Tools.xmlToColor(stdColor);
			}
			else {
				standardColor = Color.RED;
			}
		}
		if (standardStyle == null) {
			final String stdStyle = Controller.getResourceController()
			    .getProperty(ResourceController.RESOURCES_EDGE_STYLE);
			if (stdStyle != null) {
				standardStyle = stdStyle;
			}
		}
	}
}
