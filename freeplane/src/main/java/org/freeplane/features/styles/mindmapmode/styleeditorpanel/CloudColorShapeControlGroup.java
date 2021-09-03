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

import org.freeplane.core.resources.IFreeplanePropertyListener;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.resources.components.ColorProperty;
import org.freeplane.core.resources.components.ComboProperty;
import org.freeplane.core.resources.components.IPropertyControl;
import org.freeplane.core.resources.components.NextLineProperty;
import org.freeplane.features.cloud.CloudController;
import org.freeplane.features.cloud.CloudModel;
import org.freeplane.features.cloud.CloudShape;
import org.freeplane.features.cloud.mindmapmode.MCloudController;
import org.freeplane.features.map.IMapSelection;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.styles.LogicalStyleController.StyleOption;

import com.jgoodies.forms.builder.DefaultFormBuilder;


/**
 * @author Joe Berry
 * Dec 17, 2016
 */
public class CloudColorShapeControlGroup implements ControlGroup {
	static final String REVERT_CLOUD = "revert-cloud";
    static final String CLOUD_COLOR = "cloudcolor";
	static final String CLOUD_SHAPE = "cloudshape";

	final private RevertingProperty mSetCloud;
	final private ColorProperty mCloudColor;
	final private ComboProperty mCloudShape;
	final private CloudColorChangeListener mPropertyListener;
	
	public CloudColorShapeControlGroup() {
		mSetCloud = new RevertingProperty(REVERT_CLOUD);
		mCloudColor = new ColorProperty(CLOUD_COLOR, ResourceController.getResourceController()
		    .getDefaultProperty(CloudController.RESOURCES_CLOUD_COLOR));
		mCloudShape = ComboProperty.of(CLOUD_SHAPE, CloudShape.class);
		mPropertyListener = new CloudColorChangeListener(mSetCloud, mCloudColor, mCloudShape);
		mSetCloud.addPropertyChangeListener(mPropertyListener);
		mCloudColor.addPropertyChangeListener(mPropertyListener);
		ResourceController.getResourceController().addPropertyChangeListener(new IFreeplanePropertyListener() {
			
			@Override
			public void propertyChanged(String propertyName, String newValue, String oldValue) {
				if(propertyName.equals(CloudController.RESOURCES_CLOUD_COLOR) || propertyName.equals(CloudController.RESOURCES_CLOUD_SHAPE)) {
					final IMapSelection selection = Controller.getCurrentController().getSelection();
					if(selection != null) {
						final NodeModel selected = selection.getSelected();
						mPropertyListener.setStyle(selected);
					}
				}
			}
		});
	}

	private class CloudColorChangeListener extends ControlGroupChangeListener {
		public CloudColorChangeListener(final RevertingProperty mSet,final IPropertyControl mProperty1, final IPropertyControl mProperty2) {
			super(mSet, mProperty1, mProperty2);
		}

		@Override
		void applyValue(final boolean enabled, final NodeModel node, final PropertyChangeEvent evt) {
			final MCloudController styleController = (MCloudController) Controller
					.getCurrentModeController().getExtension(
							CloudController.class);
			if (enabled) {
				styleController.setColor(node, mCloudColor.getColorValue());
				styleController.setShape(node, CloudShape.valueOf(mCloudShape.getValue()));
			}
			else {
				styleController.setCloud(node, false);
			}
		}

		@Override
		void setStyleOnExternalChange(NodeModel node) {
			final CloudController cloudController = CloudController.getController();
			final CloudModel cloudModel = CloudModel.getModel(node);
			final Color viewCloudColor = cloudController.getColor(node, StyleOption.FOR_UNSELECTED_NODE);
			mSetCloud.setValue(cloudModel != null);
			mCloudColor.setColorValue(viewCloudColor);
			final CloudShape viewCloudShape = cloudController.getShape(node, StyleOption.FOR_UNSELECTED_NODE);
			mCloudShape.setValue(viewCloudShape.name());
		}
        
        @Override
        void adjustForStyle(NodeModel node) {
            StylePropertyAdjuster.adjustPropertyControl(node, mSetCloud);
            StylePropertyAdjuster.adjustPropertyControl(node, mCloudColor);
            StylePropertyAdjuster.adjustPropertyControl(node, mCloudShape);
        }
	}
	
	@Override
	public void addControlGroup(DefaultFormBuilder formBuilder) {
		addCloudColorControl(formBuilder);
		new NextLineProperty().appendToForm(formBuilder);
		addCloudShapeControl(formBuilder);
	}
	
	private void addCloudColorControl(DefaultFormBuilder formBuilder) {
		mCloudColor.appendToForm(formBuilder);
		mSetCloud.appendToForm(formBuilder);
	}

	private void addCloudShapeControl(DefaultFormBuilder formBuilder) {
		mCloudShape.addPropertyChangeListener(mPropertyListener);
		mCloudShape.appendToForm(formBuilder);
	}

	@Override
	public void setStyle(NodeModel node, boolean canEdit) {
		mPropertyListener.setStyle(node);
	}
}
