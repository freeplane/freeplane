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

import org.freeplane.api.LengthUnit;
import org.freeplane.api.Quantity;
import org.freeplane.core.resources.components.BooleanProperty;
import org.freeplane.core.resources.components.IPropertyControl;
import org.freeplane.core.resources.components.QuantityProperty;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.nodestyle.NodeSizeModel;
import org.freeplane.features.nodestyle.NodeStyleController;
import org.freeplane.features.nodestyle.mindmapmode.MNodeStyleController;

import com.jgoodies.forms.builder.DefaultFormBuilder;

/**
 * @author Joe Berry
 * Nov 27, 2016
 */
class MinNodeWidthControlGroup implements ControlGroup {
	private static final String MIN_NODE_WIDTH = "min_node_width";

	private BooleanProperty mSetMinNodeWidth;
	private QuantityProperty<LengthUnit> mMinNodeWidth;
	private MinNodeWidthChangeListener propertyChangeListener;

	private class MinNodeWidthChangeListener extends ControlGroupChangeListener {
		public MinNodeWidthChangeListener(final BooleanProperty mSet, final IPropertyControl... mProperty) {
			super(mSet, mProperty);
		}

		@Override
		void applyValue(final boolean enabled, final NodeModel node, final PropertyChangeEvent evt) {
			final MNodeStyleController styleController = (MNodeStyleController) Controller
			.getCurrentModeController().getExtension(NodeStyleController.class);
			styleController.setMinNodeWidth(node, enabled ? mMinNodeWidth.getQuantifiedValue(): null);
		}

		@Override
		void setStyleOnExternalChange(NodeModel node) {
			final NodeSizeModel nodeSizeModel = NodeSizeModel.getModel(node);
			final NodeStyleController styleController = NodeStyleController.getController();
			final Quantity<LengthUnit> width = nodeSizeModel != null ? nodeSizeModel.getMinNodeWidth() : null;
			final Quantity<LengthUnit> viewWidth = styleController.getMinWidth(node);
			mSetMinNodeWidth.setValue(width != null);
			mMinNodeWidth.setQuantifiedValue(viewWidth);
		}
	}
	
	public void addControlGroup(DefaultFormBuilder formBuilder) {
		mSetMinNodeWidth = new BooleanProperty(ControlGroup.SET_RESOURCE);
		mMinNodeWidth = new QuantityProperty<LengthUnit>(MIN_NODE_WIDTH, 0, 100000, 0.1, LengthUnit.px);
		propertyChangeListener = new MinNodeWidthChangeListener(mSetMinNodeWidth, mMinNodeWidth);
		mSetMinNodeWidth.addPropertyChangeListener(propertyChangeListener);
		mMinNodeWidth.addPropertyChangeListener(propertyChangeListener);
		mSetMinNodeWidth.appendToForm(formBuilder);
		mMinNodeWidth.appendToForm(formBuilder);
	}
	
	public void setStyle(NodeModel node, boolean canEdit) {
		propertyChangeListener.setStyle(node);
	}
	
}