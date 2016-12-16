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
import org.freeplane.core.resources.components.QuantityProperty;
import org.freeplane.core.ui.LengthUnits;
import org.freeplane.core.util.Quantity;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.nodestyle.NodeSizeModel;
import org.freeplane.features.nodestyle.NodeStyleController;
import org.freeplane.features.nodestyle.mindmapmode.MNodeStyleController;

/**
 * @author Joe Berry
 * Nov 27, 2016
 */
class MinNodeWidthControlGroup implements ControlGroup {
	static final String MIN_NODE_WIDTH = "min_node_width";

	private BooleanProperty mSetMinNodeWidth;
	private QuantityProperty<LengthUnits> mMinNodeWidth;
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
			final Quantity<LengthUnits> width = nodeSizeModel != null ? nodeSizeModel.getMinNodeWidth() : null;
			final Quantity<LengthUnits> viewWidth = styleController.getMinWidth(node);
			mSetMinNodeWidth.setValue(width != null);
			mMinNodeWidth.setQuantifiedValue(viewWidth);
		}
	}
	
	public void addControlGroup(final List<IPropertyControl> controls) {
		mSetMinNodeWidth = new BooleanProperty(ControlGroup.SET_RESOURCE);
		controls.add(mSetMinNodeWidth);
		mMinNodeWidth = new QuantityProperty<LengthUnits>(MIN_NODE_WIDTH, 0, 100000, 0.1, LengthUnits.px);
		controls.add(mMinNodeWidth);
		propertyChangeListener = new MinNodeWidthChangeListener(mSetMinNodeWidth, mMinNodeWidth);
		mSetMinNodeWidth.addPropertyChangeListener(propertyChangeListener);
		mMinNodeWidth.addPropertyChangeListener(propertyChangeListener);
	}
	
	public void setStyle(NodeModel node) {
		propertyChangeListener.setStyle(node);
	}
	
}