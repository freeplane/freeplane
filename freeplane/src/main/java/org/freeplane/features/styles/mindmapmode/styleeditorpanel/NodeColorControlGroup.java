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
import org.freeplane.core.resources.components.IPropertyControl;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.nodestyle.NodeStyleController;
import org.freeplane.features.nodestyle.NodeStyleModel;
import org.freeplane.features.nodestyle.mindmapmode.MNodeStyleController;

import com.jgoodies.forms.builder.DefaultFormBuilder;

/**
 * @author Joe Berry
 * Nov 27, 2016
 */
class NodeColorControlGroup implements ControlGroup {
	private static final String NODE_COLOR = "nodecolor";
	private static final String NODE_TEXT_COLOR = "standardnodetextcolor";

	private BooleanProperty mSetNodeColor;
	private ColorProperty mNodeColor;
	private NodeColorChangeListener propertyChangeListener;

	private class NodeColorChangeListener extends ControlGroupChangeListener {
		public NodeColorChangeListener(final BooleanProperty mSet, final IPropertyControl mProperty) {
			super(mSet, mProperty);
		}

		@Override
		void applyValue(final boolean enabled, final NodeModel node, final PropertyChangeEvent evt) {
			final MNodeStyleController styleController = (MNodeStyleController) Controller
			.getCurrentModeController().getExtension(
					NodeStyleController.class);
			styleController.setColor(node, enabled ? mNodeColor.getColorValue() : null);
		}

		@Override
		void setStyleOnExternalChange(NodeModel node) {
			final NodeStyleController styleController = NodeStyleController.getController();
			final Color nodeColor = NodeStyleModel.getColor(node);
			final Color viewNodeColor = styleController.getColor(node);
			mSetNodeColor.setValue(nodeColor != null);
			mNodeColor.setColorValue(viewNodeColor);
		}
	}
	
	public void addControlGroup(final List<IPropertyControl> controls, DefaultFormBuilder formBuilder) {
		mSetNodeColor = new BooleanProperty(ControlGroup.SET_RESOURCE);
		controls.add(mSetNodeColor);
		mNodeColor = new ColorProperty(NODE_COLOR, ResourceController.getResourceController()
		    .getDefaultProperty(NODE_TEXT_COLOR));
		controls.add(mNodeColor);
		propertyChangeListener = new NodeColorChangeListener(mSetNodeColor, mNodeColor);
		mSetNodeColor.addPropertyChangeListener(propertyChangeListener);
		mNodeColor.addPropertyChangeListener(propertyChangeListener);
	}
	
	public void setStyle(NodeModel node) {
		propertyChangeListener.setStyle(node);
	}
}