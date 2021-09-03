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

import org.freeplane.core.resources.components.BooleanProperty;
import org.freeplane.core.resources.components.IPropertyControl;
import org.freeplane.core.resources.components.NumberProperty;
import org.freeplane.features.edge.EdgeController;
import org.freeplane.features.edge.EdgeModel;
import org.freeplane.features.edge.mindmapmode.MEdgeController;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.styles.LogicalStyleController.StyleOption;

import com.jgoodies.forms.builder.DefaultFormBuilder;

/**
 * @author Joe Berry
 * Dec 1, 2016
 */
public class EdgeWidthControlGroup implements ControlGroup {
	private static final String EDGE_WIDTH = "edgewidth";

	private RevertingProperty mSetEdgeWidth;
	private NumberProperty mEdgeWidth;
	private EdgeWidthChangeListener propertyChangeListener;
	
	private class EdgeWidthChangeListener extends ControlGroupChangeListener {
		public EdgeWidthChangeListener(final RevertingProperty mSet,final IPropertyControl mProperty) {
			super(mSet, mProperty);
		}

		@Override
		void applyValue(final boolean enabled, final NodeModel node, final PropertyChangeEvent evt) {
			final MEdgeController styleController = (MEdgeController) Controller
			.getCurrentModeController().getExtension(
					EdgeController.class);
			styleController.setWidth(node, enabled ? Integer.parseInt(mEdgeWidth.getValue()): EdgeModel.AUTO_WIDTH);
		}

		@Override
		void setStyleOnExternalChange(NodeModel node) {
			final EdgeController edgeController = EdgeController.getController();
			final EdgeModel edgeModel = EdgeModel.getModel(node);
			{
				final int width = edgeModel != null ? edgeModel.getWidth() : EdgeModel.AUTO_WIDTH;
				final int viewWidth = edgeController.getWidth(node, StyleOption.FOR_UNSELECTED_NODE);
				mSetEdgeWidth.setValue(width != EdgeModel.AUTO_WIDTH);
				mEdgeWidth.setValue(Integer.toString(viewWidth));
			}
		}
	       
        @Override
        void adjustForStyle(NodeModel node) {
            StylePropertyAdjuster.adjustPropertyControl(node, mSetEdgeWidth);
            StylePropertyAdjuster.adjustPropertyControl(node, mEdgeWidth);
        }
	}

	@Override
	public void addControlGroup(DefaultFormBuilder formBuilder) {
		mSetEdgeWidth = new RevertingProperty();
		mEdgeWidth = new NumberProperty(EDGE_WIDTH, 0, 100, 1);
		propertyChangeListener = new EdgeWidthChangeListener(mSetEdgeWidth, mEdgeWidth);
		mSetEdgeWidth.addPropertyChangeListener(propertyChangeListener);
		mEdgeWidth.addPropertyChangeListener(propertyChangeListener);
		mEdgeWidth.appendToForm(formBuilder);
		mSetEdgeWidth.appendToForm(formBuilder);
	}

	@Override
	public void setStyle(NodeModel node, boolean canEdit) {
		propertyChangeListener.setStyle(node);
	}
}
