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
package org.freeplane.features.cloud;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;
import java.util.Collection;
import org.freeplane.core.extension.IExtension;
import org.freeplane.core.io.ReadManager;
import org.freeplane.core.io.WriteManager;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.ColorUtils;
import org.freeplane.features.cloud.CloudModel.Shape;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ExclusivePropertyChain;
import org.freeplane.features.mode.IPropertyHandler;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.styles.IStyle;
import org.freeplane.features.styles.LogicalStyleController;
import org.freeplane.features.styles.MapStyleModel;

/**
 * @author Dimitry Polivaev
 */
public class CloudController implements IExtension {
	static final Stroke DEF_STROKE = new BasicStroke(3);
	public static final int DEFAULT_WIDTH = -1;
	public static final int NORMAL_WIDTH = 3;
	public static final String RESOURCES_CLOUD_COLOR = "standardcloudcolor";
	public static final String RESOURCES_CLOUD_SHAPE = "standardcloudshape";

	public static Color getStandardColor() {
		final ResourceController resourceController = ResourceController.getResourceController();
		final String colorCode = resourceController.getProperty(CloudController.RESOURCES_CLOUD_COLOR);
		return ColorUtils.stringToColor(colorCode);
	}

	public static Shape getStandardShape() {
		final ResourceController resourceController = ResourceController.getResourceController();
		return resourceController.getEnumProperty(CloudController.RESOURCES_CLOUD_SHAPE, Shape.ARC);
	}

	public static CloudController getController() {
		return getController(Controller.getCurrentModeController());
	}

	public static CloudController getController(ModeController modeController) {
		return (CloudController) modeController.getExtension(CloudController.class);
	}
	public static void install( final CloudController cloudController) {
		Controller.getCurrentModeController().addExtension(CloudController.class, cloudController);
	}

	final private ExclusivePropertyChain<CloudModel, NodeModel> cloudHandlers;
// 	private final ModeController modeController;

	public CloudController(final ModeController modeController) {
//		this.modeController = modeController;
		cloudHandlers = new ExclusivePropertyChain<CloudModel, NodeModel>();
		addCloudGetter(IPropertyHandler.STYLE, new IPropertyHandler<CloudModel, NodeModel>() {
			public CloudModel getProperty(final NodeModel node, final CloudModel currentValue) {
				return getStyleCloud(node.getMap(), LogicalStyleController.getController(modeController).getStyles(node));
			}
		});
		final MapController mapController = Controller.getCurrentModeController().getMapController();
		final ReadManager readManager = mapController.getReadManager();
		final WriteManager writeManager = mapController.getWriteManager();
		final CloudBuilder cloudBuilder = new CloudBuilder(mapController, this);
		cloudBuilder.registerBy(readManager, writeManager);
	}

	protected CloudModel getStyleCloud(final MapModel map, final Collection<IStyle> collection) {
		final MapStyleModel model = MapStyleModel.getExtension(map);
		for(IStyle styleKey : collection){
			final NodeModel styleNode = model.getStyleNode(styleKey);
			if (styleNode == null) {
				continue;
			}
			final CloudModel styleModel = CloudModel.getModel(styleNode);
			if (styleModel != null) {
				return styleModel;
			}
		}
		return null;
	}

	public IPropertyHandler<CloudModel, NodeModel> addCloudGetter(final Integer key,
	                                                              final IPropertyHandler<CloudModel, NodeModel> getter) {
		return cloudHandlers.addGetter(key, getter);
	}

	public Color getColor(final NodeModel node) {
		final CloudModel cloud = getCloud(node);
		return cloud != null ? cloud.getColor() : null;
	}

	public int getWidth(final NodeModel node) {
		return NORMAL_WIDTH;
	}

	public IPropertyHandler<CloudModel, NodeModel> removeCloudGetter(final Integer key) {
		return cloudHandlers.removeGetter(key);
	}

	public CloudModel getCloud(final NodeModel model) {
		return cloudHandlers.getProperty(model);
	}

	public Shape getShape(NodeModel node) {
		final CloudModel cloud = getCloud(node);
		return cloud != null ? cloud.getShape() : getStandardShape();
    }
}
