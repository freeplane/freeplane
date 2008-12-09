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
package org.freeplane.map.cloud;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;

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
public class CloudController {
	protected static class CloudAdapterListener implements
	        IFreemindPropertyListener {
		public void propertyChanged(final String propertyName,
		                            final String newValue, final String oldValue) {
			if (propertyName.equals(ResourceController.RESOURCES_CLOUD_COLOR)) {
				standardColor = Tools.xmlToColor(newValue);
			}
		}
	}

	static final Stroke DEF_STROKE = new BasicStroke(3);
	public static final int DEFAULT_WIDTH = -1;
	private static CloudAdapterListener listener = null;
	public static final int NORMAL_WIDTH = 3;
	private static Color standardColor = null;
	final private PropertyChain<Color, NodeModel> colorHandlers;

	public CloudController(final ModeController modeController) {
		colorHandlers = new PropertyChain<Color, NodeModel>();
		if (listener == null) {
			listener = new CloudAdapterListener();
			Controller.getResourceController().addPropertyChangeListener(
			    listener);
		}
		updateStandards(modeController);
		addColorGetter(PropertyChain.NODE,
		    new IPropertyGetter<Color, NodeModel>() {
			    public Color getProperty(final NodeModel node) {
				    final CloudModel cloud = node.getCloud();
				    return cloud != null ? cloud.getColor() : null;
			    }
		    });
		addColorGetter(PropertyChain.DEFAULT,
		    new IPropertyGetter<Color, NodeModel>() {
			    public Color getProperty(final NodeModel node) {
				    return standardColor;
			    }
		    });
		final MapController mapController = modeController.getMapController();
		final ReadManager readManager = mapController.getReadManager();
		final WriteManager writeManager = mapController.getWriteManager();
		final CloudBuilder cloudBuilder = new CloudBuilder();
		cloudBuilder.registerBy(readManager, writeManager);
	}

	public IPropertyGetter<Color, NodeModel> addColorGetter(
	                                                        final Integer key,
	                                                        final IPropertyGetter<Color, NodeModel> getter) {
		return colorHandlers.addGetter(key, getter);
	}

	public Color getColor(final NodeModel node) {
		return colorHandlers.getProperty(node);
	}

	public Color getExteriorColor(final NodeModel node) {
		return getColor(node).darker();
	}

	public int getWidth(final NodeModel node) {
		return NORMAL_WIDTH;
	}

	public IPropertyGetter<Color, NodeModel> removeColorGetter(final Integer key) {
		return colorHandlers.removeGetter(key);
	}

	private void updateStandards(final ModeController controller) {
		if (standardColor == null) {
			final String stdColor = Controller.getResourceController()
			    .getProperty(ResourceController.RESOURCES_CLOUD_COLOR);
			standardColor = Tools.xmlToColor(stdColor);
		}
	}
}
