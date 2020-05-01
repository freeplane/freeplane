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

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.resources.components.BooleanProperty;
import org.freeplane.core.resources.components.ColorProperty;
import org.freeplane.core.resources.components.IPropertyControl;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.nodestyle.NodeStyleController;
import org.freeplane.features.nodestyle.NodeStyleModel;
import org.freeplane.features.nodestyle.mindmapmode.MNodeStyleController;

import com.jgoodies.forms.builder.DefaultFormBuilder;

/**
 * @author Joe Berry
 * Dec 1, 2016
 */
public class NodeBackgroundColorControlGroup implements ControlGroup {
	private static final String NODE_BACKGROUND_COLOR = "nodebackgroundcolor";

	private BooleanProperty mSetNodeBackgroundColor;
	private ColorProperty mNodeBackgroundColor;
	private BgColorChangeListener propertyChangeListener;
	
	private class BgColorChangeListener extends ControlGroupChangeListener {
		public BgColorChangeListener(final BooleanProperty mSet, final IPropertyControl mProperty) {
			super(mSet, mProperty);
		}

		@Override
		void applyValue(final boolean enabled, final NodeModel node, final PropertyChangeEvent evt) {
			final MNodeStyleController styleController = (MNodeStyleController) Controller
					.getCurrentModeController().getExtension(
							NodeStyleController.class);
			styleController.setBackgroundColor(node, enabled ? mNodeBackgroundColor.getColorValue() : null);
		}

		@Override
		void setStyleOnExternalChange(NodeModel node) {
			final NodeStyleController styleController = NodeStyleController.getController();
			final Color color = NodeStyleModel.getBackgroundColor(node);
			final Color viewColor = styleController.getBackgroundColor(node);
			mSetNodeBackgroundColor.setValue(color != null);
			mNodeBackgroundColor.setColorValue(viewColor != null ? viewColor : Controller.getCurrentController()
			    .getMapViewManager().getBackgroundColor(node));
		}
	}

	@Override
	public void setStyle(NodeModel node, boolean canEdit) {
		propertyChangeListener.setStyle(node);
	}

	@Override
	public void addControlGroup(DefaultFormBuilder formBuilder) {
		mSetNodeBackgroundColor = new BooleanProperty(ControlGroup.SET_RESOURCE);
		mNodeBackgroundColor = new ColorProperty(NODE_BACKGROUND_COLOR, ResourceController
		    .getResourceController().getDefaultProperty(NODE_BACKGROUND_COLOR));
		propertyChangeListener = new BgColorChangeListener(mSetNodeBackgroundColor, mNodeBackgroundColor);
		mSetNodeBackgroundColor.addPropertyChangeListener(propertyChangeListener);
		mNodeBackgroundColor.addPropertyChangeListener(propertyChangeListener);
		mSetNodeBackgroundColor.layout(formBuilder);
		mNodeBackgroundColor.layout(formBuilder);
	}

	
}
