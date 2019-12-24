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

import org.freeplane.core.resources.components.BooleanProperty;
import org.freeplane.core.resources.components.ColorProperty;
import org.freeplane.core.resources.components.IPropertyControl;
import org.freeplane.core.util.ColorUtils;
import org.freeplane.features.edge.EdgeController;
import org.freeplane.features.edge.EdgeModel;
import org.freeplane.features.edge.mindmapmode.MEdgeController;
import org.freeplane.features.map.NodeModel;

import com.jgoodies.forms.builder.DefaultFormBuilder;

/**
 * @author Joe Berry
 * Nov 27, 2016
 */
class EdgeColorControlGroup implements ControlGroup {
	private static final String EDGE_COLOR = "edgecolor";

	private BooleanProperty mSetEdgeColor;
	private ColorProperty mEdgeColor;
	private EdgeColorChangeListener propertyChangeListener;

	private class EdgeColorChangeListener extends ControlGroupChangeListener {
		public EdgeColorChangeListener(final BooleanProperty mSet, final IPropertyControl mProperty) {
			super(mSet, mProperty);
		}

		@Override
		void applyValue(final boolean enabled, final NodeModel node, final PropertyChangeEvent evt) {
			final MEdgeController edgeController = (MEdgeController) MEdgeController.getController();
			edgeController.setColor(node, enabled ? mEdgeColor.getColorValue() : null);
		}

		@Override
		void setStyleOnExternalChange(NodeModel node) {
			final EdgeModel edgeModel = EdgeModel.getModel(node);
			final EdgeController edgeController = EdgeController.getController();
			{
				final Color edgeColor = edgeModel != null ? edgeModel.getColor() : null;
				final Color viewColor = edgeController.getColor(node);
				mSetEdgeColor.setValue(edgeColor != null);
				mEdgeColor.setColorValue(viewColor);
			}
		}
	}
	public void addControlGroup(DefaultFormBuilder formBuilder) {
		mSetEdgeColor = new BooleanProperty(ControlGroup.SET_RESOURCE);
		mEdgeColor = new ColorProperty(EdgeColorControlGroup.EDGE_COLOR, ColorUtils.colorToString(EdgeController.STANDARD_EDGE_COLOR));
		propertyChangeListener = new EdgeColorChangeListener(mSetEdgeColor, mEdgeColor);
		mSetEdgeColor.addPropertyChangeListener(propertyChangeListener);
		mEdgeColor.addPropertyChangeListener(propertyChangeListener);
		mSetEdgeColor.layout(formBuilder);;
		mEdgeColor.layout(formBuilder);;
	}
	
	public void setStyle(NodeModel node, boolean canEdit) {
		propertyChangeListener.setStyle(node);
	}
}