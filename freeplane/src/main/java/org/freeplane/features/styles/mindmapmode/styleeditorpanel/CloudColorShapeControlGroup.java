/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2016 jberry
 *
 *  This file author is jberry
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
package org.freeplane.features.styles.mindmapmode.styleeditorpanel;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.util.List;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.resources.components.BooleanProperty;
import org.freeplane.core.resources.components.ColorProperty;
import org.freeplane.core.resources.components.ComboProperty;
import org.freeplane.core.resources.components.IPropertyControl;
import org.freeplane.core.resources.components.NextColumnProperty;
import org.freeplane.core.resources.components.NextLineProperty;
import org.freeplane.features.cloud.CloudController;
import org.freeplane.features.cloud.CloudModel;
import org.freeplane.features.cloud.mindmapmode.MCloudController;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;

import com.jgoodies.forms.builder.DefaultFormBuilder;


/**
 * @author Joe Berry
 * Dec 17, 2016
 */
public class CloudColorShapeControlGroup implements ControlGroup {
	private static final String CLOUD_COLOR = "cloudcolor";
	private static final String CLOUD_SHAPE = "cloudshape";
	private static final String[] CLOUD_SHAPES = EnumToStringMapper.getStringValuesOf(CloudModel.Shape.class);

	private BooleanProperty mSetCloud;
	private ColorProperty mCloudColor;
	private ComboProperty mCloudShape;

	private CloudColorChangeListener cloudColorChangeListener;
	private CloudShapeChangeListener cloudShapeChangeListener;
	
	private class CloudColorChangeListener extends ControlGroupChangeListener {
		public CloudColorChangeListener(final BooleanProperty mSet, final IPropertyControl mProperty) {
			super(mSet, mProperty);
		}

		@Override
		void applyValue(final boolean enabled, final NodeModel node, final PropertyChangeEvent evt) {
			final MCloudController styleController = (MCloudController) Controller
					.getCurrentModeController().getExtension(
							CloudController.class);
			if (enabled) {
				styleController.setColor(node, mCloudColor.getColorValue());
			}
			else {
				styleController.setCloud(node, false);
			}
		}

		@Override
		void setStyleOnExternalChange(NodeModel node) {
			final CloudController cloudController = CloudController.getController();
			final CloudModel cloudModel = CloudModel.getModel(node);
			final Color viewCloudColor = cloudController.getColor(node);
			mSetCloud.setValue(cloudModel != null);
			mCloudColor.setColorValue(viewCloudColor);

		}
	}
	
	private class CloudShapeChangeListener extends ControlGroupChangeListener {
		public CloudShapeChangeListener(final BooleanProperty mSet, final IPropertyControl mProperty) {
			super(mSet, mProperty);
		}
	
		@Override
		void applyValue(final boolean enabled, final NodeModel node, final PropertyChangeEvent evt) {
			final MCloudController styleController = (MCloudController) Controller
					.getCurrentModeController().getExtension(
						CloudController.class);
			if (enabled) {
				styleController.setShape(node, CloudModel.Shape.valueOf(mCloudShape.getValue()));
			}
			else {
				styleController.setCloud(node, false);
			}
		}

		@Override
		void setStyleOnExternalChange(NodeModel node) {
			final CloudController cloudController = CloudController.getController();
			final CloudModel.Shape viewCloudShape = cloudController.getShape(node);
			mCloudShape.setValue(viewCloudShape != null ? viewCloudShape.toString() : CloudModel.Shape.ARC.toString());
		}
	}
	
	@Override
	public void addControlGroup(DefaultFormBuilder formBuilder) {
		addCloudColorControl(formBuilder);
		new NextLineProperty().layout(formBuilder);
		new NextColumnProperty(2).layout(formBuilder);
		addCloudShapeControl(formBuilder);
	}
	
	private void addCloudColorControl(DefaultFormBuilder formBuilder) {
		mSetCloud = new BooleanProperty(ControlGroup.SET_RESOURCE);
		mCloudColor = new ColorProperty(CLOUD_COLOR, ResourceController.getResourceController()
		    .getDefaultProperty(CloudController.RESOURCES_CLOUD_COLOR));
		cloudColorChangeListener = new CloudColorChangeListener(mSetCloud, mCloudColor);
		mSetCloud.addPropertyChangeListener(cloudColorChangeListener);
		mCloudColor.addPropertyChangeListener(cloudColorChangeListener);
		mSetCloud.layout(formBuilder);
		mCloudColor.layout(formBuilder);
	}

	private void addCloudShapeControl(DefaultFormBuilder formBuilder) {
		mCloudShape = new ComboProperty(CLOUD_SHAPE, CLOUD_SHAPES);
		cloudShapeChangeListener = new CloudShapeChangeListener(mSetCloud, mCloudShape);
		mSetCloud.addPropertyChangeListener(cloudShapeChangeListener);
		mCloudShape.addPropertyChangeListener(cloudShapeChangeListener);
		mCloudShape.layout(formBuilder);
	}

	@Override
	public void setStyle(NodeModel node) {
		cloudColorChangeListener.setStyle(node);
		cloudShapeChangeListener.setStyle(node);
	}
}
