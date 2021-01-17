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
import org.freeplane.core.resources.components.ComboProperty;
import org.freeplane.core.resources.components.IPropertyControl;
import org.freeplane.features.DashVariant;
import org.freeplane.features.edge.EdgeController;
import org.freeplane.features.edge.EdgeModel;
import org.freeplane.features.edge.mindmapmode.MEdgeController;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;

import com.jgoodies.forms.builder.DefaultFormBuilder;

/**
 * @author Joe Berry
 * Dec 1, 2016
 */
public class EdgeDashControlGroup implements ControlGroup {
	private static final String EDGE_DASH = "edgedash";

	private BooleanProperty mSetEdgeDash;
	private ComboProperty mEdgeDash;
	private EdgeDashChangeListener propertyChangeListener;
	
	private class EdgeDashChangeListener extends ControlGroupChangeListener {
		public EdgeDashChangeListener(final BooleanProperty mSet, final IPropertyControl mProperty) {
			super(mSet, mProperty);
		}

		@Override
		void applyValue(final boolean enabled, final NodeModel node, final PropertyChangeEvent evt) {
			final MEdgeController styleController = (MEdgeController) Controller
			.getCurrentModeController().getExtension(
					EdgeController.class);
			styleController.setDash(node, enabled ? DashVariant.valueOf(mEdgeDash.getValue()): null);
		}

		@Override
		void setStyleOnExternalChange(NodeModel node) {
			final EdgeController edgeController = EdgeController.getController();
			final EdgeModel edgeModel = EdgeModel.getModel(node);
			{
				final DashVariant dash = edgeModel != null ? edgeModel.getDash() : null;
				final DashVariant viewDash = edgeController.getDash(node);
				mSetEdgeDash.setValue(dash != null);
				mEdgeDash.setValue(viewDash.name());
			}
		}
	}

	@Override
	public void setStyle(NodeModel node, boolean canEdit) {
		propertyChangeListener.setStyle(node);
	}

	@Override
	public void addControlGroup(DefaultFormBuilder formBuilder) {
		mSetEdgeDash = new BooleanProperty(ControlGroup.SET_RESOURCE);
		mEdgeDash = ComboProperty.of(EDGE_DASH, DashVariant.class);
		propertyChangeListener = new EdgeDashChangeListener(mSetEdgeDash, mEdgeDash);
		mSetEdgeDash.addPropertyChangeListener(propertyChangeListener);
		mEdgeDash.addPropertyChangeListener(propertyChangeListener);
		mSetEdgeDash.appendToForm(formBuilder);
		mEdgeDash.appendToForm(formBuilder);
	}
}
