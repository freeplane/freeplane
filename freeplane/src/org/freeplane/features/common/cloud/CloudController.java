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
package org.freeplane.features.common.cloud;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.io.ReadManager;
import org.freeplane.core.io.WriteManager;
import org.freeplane.core.modecontroller.ExclusivePropertyChain;
import org.freeplane.core.modecontroller.IPropertyHandler;
import org.freeplane.core.modecontroller.MapController;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.resources.IFreeplanePropertyListener;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.ColorUtils;

/**
 * @author Dimitry Polivaev
 */
public class CloudController implements IExtension {
	protected static class CloudAdapterListener implements IFreeplanePropertyListener {
		public void propertyChanged(final String propertyName, final String newValue, final String oldValue) {
			if (propertyName.equals(CloudController.RESOURCES_CLOUD_COLOR)) {
				standardColor = ColorUtils.stringToColor(newValue);
			}
		}
	}

	static final Stroke DEF_STROKE = new BasicStroke(3);
	public static final int DEFAULT_WIDTH = -1;
	private static CloudAdapterListener listener = null;
	public static final int NORMAL_WIDTH = 3;
	public static final String RESOURCES_CLOUD_COLOR = "standardcloudcolor";
	private static Color standardColor = null;

	public static CloudController getController(final ModeController modeController) {
		return (CloudController) modeController.getExtension(CloudController.class);
	}

	public static void install(final ModeController modeController, final CloudController cloudController) {
		modeController.addExtension(CloudController.class, cloudController);
	}

	final private ExclusivePropertyChain<Color, NodeModel> colorHandlers;
	private final ModeController modeController;

	public CloudController(final ModeController modeController) {
		this.modeController = modeController;
		colorHandlers = new ExclusivePropertyChain<Color, NodeModel>();
		if (listener == null) {
			listener = new CloudAdapterListener();
			ResourceController.getResourceController().addPropertyChangeListener(listener);
		}
		updateStandards(modeController);
		addColorGetter(IPropertyHandler.NODE, new IPropertyHandler<Color, NodeModel>() {
			public Color getProperty(final NodeModel node, final Color currentValue) {
				final CloudModel cloud = CloudModel.getModel(node);
				return cloud != null ? cloud.getColor() : null;
			}
		});
		addColorGetter(IPropertyHandler.DEFAULT, new IPropertyHandler<Color, NodeModel>() {
			public Color getProperty(final NodeModel node, final Color currentValue) {
				return standardColor;
			}
		});
		final MapController mapController = modeController.getMapController();
		final ReadManager readManager = mapController.getReadManager();
		final WriteManager writeManager = mapController.getWriteManager();
		final CloudBuilder cloudBuilder = new CloudBuilder(mapController);
		cloudBuilder.registerBy(readManager, writeManager);
	}

	public IPropertyHandler<Color, NodeModel> addColorGetter(final Integer key,
	                                                         final IPropertyHandler<Color, NodeModel> getter) {
		return colorHandlers.addGetter(key, getter);
	}

	public Color getColor(final NodeModel node) {
		return colorHandlers.getProperty(node);
	}

	public Color getExteriorColor(final NodeModel node) {
		return getColor(node).darker();
	}

	protected ModeController getModeController() {
		return modeController;
	}

	public int getWidth(final NodeModel node) {
		return NORMAL_WIDTH;
	}

	public IPropertyHandler<Color, NodeModel> removeColorGetter(final Integer key) {
		return colorHandlers.removeGetter(key);
	}

	private void updateStandards(final ModeController controller) {
		if (standardColor == null) {
			final String stdColor = ResourceController.getResourceController().getProperty(
			    CloudController.RESOURCES_CLOUD_COLOR);
			standardColor = ColorUtils.stringToColor(stdColor);
		}
	}
}
