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

import org.freeplane.api.LengthUnit;
import org.freeplane.api.Quantity;
import org.freeplane.core.resources.components.IPropertyControl;
import org.freeplane.core.resources.components.QuantityProperty;
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
class ChildHorizontalGapControlGroup implements ControlGroup {
	private static final String HORIZONTAL_CHILD_GAP = "horizontal_child_gap";

	private RevertingProperty mSetChildDistance;
	private QuantityProperty<LengthUnit> mChildDistance;

	private ChildDistanceChangeListener propertyChangeListener;

	private class ChildDistanceChangeListener extends ControlGroupChangeListener {
		public ChildDistanceChangeListener(final RevertingProperty mSet,final IPropertyControl... mProperty) {
			super(mSet, mProperty);
		}

		@Override
		void applyValue(final boolean enabled, final NodeModel node, final PropertyChangeEvent evt) {
			final MLocationController locationController = (MLocationController) Controller.getCurrentModeController().getExtension(LocationController.class);
			locationController.setBaseHGapToChildren(node, enabled ? mChildDistance.getQuantifiedValue() : LocationModel.DEFAULT_HGAP);
		}

		@Override
		void setStyleOnExternalChange(NodeModel node) {
			final ModeController modeController = Controller.getCurrentModeController();
			final LocationModel locationModel = LocationModel.getModel(node);
			final LocationController locationController = modeController.getExtension(LocationController.class);
			final Quantity<LengthUnit> gap = locationModel.getBaseHGap();
			final Quantity<LengthUnit> viewGap = locationController.getBaseHGapToChildren(node);
			mSetChildDistance.setValue(gap != LocationModel.DEFAULT_HGAP);
			mChildDistance.setQuantifiedValue(viewGap);
		}

        @Override
        void adjustForStyle(NodeModel node) {
            StylePropertyAdjuster.adjustPropertyControl(node, mSetChildDistance);
            StylePropertyAdjuster.adjustPropertyControl(node, mChildDistance);
        }
	}

	public void addControlGroup(DefaultFormBuilder formBuilder) {
		mSetChildDistance = new RevertingProperty();
		mChildDistance = new  QuantityProperty<LengthUnit>(HORIZONTAL_CHILD_GAP, -1000, 1000, 0.1, LengthUnit.px);
		propertyChangeListener = new ChildDistanceChangeListener(mSetChildDistance, mChildDistance);
		mSetChildDistance.addPropertyChangeListener(propertyChangeListener);
		mChildDistance.addPropertyChangeListener(propertyChangeListener);
		mChildDistance.appendToForm(formBuilder);
		mSetChildDistance.appendToForm(formBuilder);
	}

	public void setStyle(NodeModel node, boolean canEdit) {
		propertyChangeListener.setStyle(node);
	}

}