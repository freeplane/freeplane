/*
 *  Freeplane - Maxd map editor
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
import org.freeplane.core.resources.components.QuantityProperty;
import org.freeplane.core.ui.LengthUnits;
import org.freeplane.core.util.Quantity;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.nodelocation.LocationController;
import org.freeplane.features.nodelocation.LocationModel;
import org.freeplane.features.nodelocation.mindmapmode.MLocationController;

import com.jgoodies.forms.builder.DefaultFormBuilder;


/**
 * @author Joe Berry
 * Nov 27, 2016
 */
class ChildDistanceControlGroup implements ControlGroup {
	private static final String VERTICAL_CHILD_GAP = "vertical_child_gap";

	private BooleanProperty mSetChildDistance;
	private QuantityProperty<LengthUnits> mChildDistance;

	private ChildDistanceChangeListener propertyChangeListener;

	private class ChildDistanceChangeListener extends ControlGroupChangeListener {
		public ChildDistanceChangeListener(final BooleanProperty mSet, final IPropertyControl... mProperty) {
			super(mSet, mProperty);
		}

		@Override
		void applyValue(final boolean enabled, final NodeModel node, final PropertyChangeEvent evt) {
			final MLocationController locationController = (MLocationController) Controller.getCurrentModeController().getExtension(LocationController.class);
			locationController.setMinimalDistanceBetweenChildren(node, enabled ? mChildDistance.getQuantifiedValue(): LocationModel.DEFAULT_VGAP);
		}

		@Override
		void setStyleOnExternalChange(NodeModel node) {
			final ModeController modeController = Controller.getCurrentModeController();
			final LocationModel locationModel = LocationModel.getModel(node);
			final LocationController locationController = modeController.getExtension(LocationController.class);
			final Quantity<LengthUnits> gap = locationModel.getVGap();
			final Quantity<LengthUnits> viewGap = locationController.getMinimalDistanceBetweenChildren(node);
			mSetChildDistance.setValue(gap != LocationModel.DEFAULT_VGAP);
			mChildDistance.setQuantifiedValue(viewGap);
		}
	}
	
	public void addControlGroup(DefaultFormBuilder formBuilder) {
		mSetChildDistance = new BooleanProperty(ControlGroup.SET_RESOURCE);
		mChildDistance = new  QuantityProperty<LengthUnits>(VERTICAL_CHILD_GAP, 0, 1000, 0.1, LengthUnits.px);
		propertyChangeListener = new ChildDistanceChangeListener(mSetChildDistance, mChildDistance);
		mSetChildDistance.addPropertyChangeListener(propertyChangeListener);
		mChildDistance.addPropertyChangeListener(propertyChangeListener);
		mSetChildDistance.layout(formBuilder);
		mChildDistance.layout(formBuilder);
	}
	
	public void setStyle(NodeModel node) {
		propertyChangeListener.setStyle(node);
	}
	
}