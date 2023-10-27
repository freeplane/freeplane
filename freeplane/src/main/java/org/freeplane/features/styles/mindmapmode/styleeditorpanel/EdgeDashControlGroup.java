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

import org.freeplane.api.Dash;
import org.freeplane.core.resources.components.ComboProperty;
import org.freeplane.core.resources.components.IPropertyControl;
import org.freeplane.features.DashRenderedContent;
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
public class EdgeDashControlGroup implements ControlGroup {
    private static final String REVERT_EDGE = "revert-edge";
	private static final String EDGE_DASH = "edgedash";

	private RevertingProperty mSetEdgeDash;
	private ComboProperty mEdgeDash;
	private EdgeDashChangeListener propertyChangeListener;

	private class EdgeDashChangeListener extends ControlGroupChangeListener {
		public EdgeDashChangeListener(final RevertingProperty mSet,final IPropertyControl mProperty) {
			super(mSet, mProperty);
		}

		@Override
		void applyValue(final boolean enabled, final NodeModel node, final PropertyChangeEvent evt) {
			final MEdgeController styleController = (MEdgeController) Controller
			.getCurrentModeController().getExtension(
					EdgeController.class);
			styleController.setDash(node, enabled ? Dash.valueOf(mEdgeDash.getValue()): null);
		}

		@Override
		void setStyleOnExternalChange(NodeModel node) {
			final EdgeController edgeController = EdgeController.getController();
			final EdgeModel edgeModel = EdgeModel.getModel(node);
			{
				final Dash dash = edgeModel != null ? edgeModel.getDash() : null;
				final Dash viewDash = edgeController.getDash(node, StyleOption.FOR_UNSELECTED_NODE);
				mSetEdgeDash.setValue(dash != null);
				mEdgeDash.setValue(viewDash.name());
			}
		}

        @Override
        void adjustForStyle(NodeModel node) {
            StylePropertyAdjuster.adjustPropertyControl(node, mSetEdgeDash);
            StylePropertyAdjuster.adjustPropertyControl(node, mEdgeDash);
        }
	}

	@Override
	public void setStyle(NodeModel node, boolean canEdit) {
		propertyChangeListener.setStyle(node);
	}

	@Override
	public void addControlGroup(DefaultFormBuilder formBuilder) {
		mSetEdgeDash = new RevertingProperty(REVERT_EDGE);
		mEdgeDash = ComboProperty.of(EDGE_DASH, Dash.class, DashRenderedContent::of);
		propertyChangeListener = new EdgeDashChangeListener(mSetEdgeDash, mEdgeDash);
		mSetEdgeDash.addPropertyChangeListener(propertyChangeListener);
		mEdgeDash.addPropertyChangeListener(propertyChangeListener);
		mEdgeDash.appendToForm(formBuilder);
		mSetEdgeDash.appendToForm(formBuilder);
	}
}
