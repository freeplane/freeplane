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

import java.beans.PropertyChangeEvent;
import java.util.List;

import org.freeplane.core.resources.components.BooleanProperty;
import org.freeplane.core.resources.components.IPropertyControl;
import org.freeplane.core.resources.components.NumberProperty;
import org.freeplane.features.edge.EdgeController;
import org.freeplane.features.edge.EdgeModel;
import org.freeplane.features.edge.mindmapmode.MEdgeController;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;

/**
 * @author Joe Berry
 * Dec 1, 2016
 */
public class EdgeWidthControlGroup implements ControlGroup {
	static final String EDGE_WIDTH = "edgewidth";

	private BooleanProperty mSetEdgeWidth;
	private NumberProperty mEdgeWidth;
	private EdgeWidthChangeListener propertyChangeListener;
	
	private class EdgeWidthChangeListener extends ControlGroupChangeListener {
		public EdgeWidthChangeListener(final BooleanProperty mSet, final IPropertyControl mProperty) {
			super(mSet, mProperty);
		}

		@Override
		void applyValue(final boolean enabled, final NodeModel node, final PropertyChangeEvent evt) {
			final MEdgeController styleController = (MEdgeController) Controller
			.getCurrentModeController().getExtension(
					EdgeController.class);
			styleController.setWidth(node, enabled ? Integer.parseInt(mEdgeWidth.getValue()): EdgeModel.DEFAULT_WIDTH);
		}

		@Override
		void setStyleOnExternalChange(NodeModel node) {
			final EdgeController edgeController = EdgeController.getController();
			final EdgeModel edgeModel = EdgeModel.getModel(node);
			{
				final int width = edgeModel != null ? edgeModel.getWidth() : EdgeModel.DEFAULT_WIDTH;
				final int viewWidth = edgeController.getWidth(node);
				mSetEdgeWidth.setValue(width != EdgeModel.DEFAULT_WIDTH);
				mEdgeWidth.setValue(Integer.toString(viewWidth));
			}
		}
	}

	@Override
	public void addControlGroup(final List<IPropertyControl> controls) {
		mSetEdgeWidth = new BooleanProperty(ControlGroup.SET_RESOURCE);
		controls.add(mSetEdgeWidth);
		mEdgeWidth = new NumberProperty(EDGE_WIDTH, 0, 100, 1);
		controls.add(mEdgeWidth);
		propertyChangeListener = new EdgeWidthChangeListener(mSetEdgeWidth, mEdgeWidth);
		mSetEdgeWidth.addPropertyChangeListener(propertyChangeListener);
		mEdgeWidth.addPropertyChangeListener(propertyChangeListener);
	}

	@Override
	public void setStyle(NodeModel node) {
		propertyChangeListener.setStyle(node);
	}
}
