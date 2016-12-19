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
import org.freeplane.core.resources.components.ComboProperty;
import org.freeplane.core.resources.components.IPropertyControl;
import org.freeplane.core.resources.components.NextColumnProperty;
import org.freeplane.core.resources.components.QuantityProperty;
import org.freeplane.core.ui.LengthUnits;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.nodestyle.NodeStyleController;
import org.freeplane.features.nodestyle.NodeStyleModel;
import org.freeplane.features.nodestyle.ShapeConfigurationModel;
import org.freeplane.features.nodestyle.NodeStyleModel.Shape;
import org.freeplane.features.nodestyle.mindmapmode.MNodeStyleController;

/**
 * @author Joe Berry
 * Nov 27, 2016
 */
class NodeShapeControlGroup implements ControlGroup {
	private static final String NODE_SHAPE = "nodeshape";
	private static final String SHAPE_HORIZONTAL_MARGIN = "shape_horizontal_margin";
	private static final String SHAPE_VERTICAL_MARGIN = "shape_vertical_margin";
	private static final String UNIFORM_SHAPE = "uniform_shape";

	private BooleanProperty mSetNodeShape;
	private ComboProperty mNodeShape;
	
	private QuantityProperty<LengthUnits> mShapeHorizontalMargin;
	private QuantityProperty<LengthUnits> mShapeVerticalMargin;
	private BooleanProperty mUniformShape;

	private NodeShapeChangeListener propertyChangeListener;

	private class NodeShapeChangeListener extends ControlGroupChangeListener {
		public NodeShapeChangeListener(final BooleanProperty mSet, final IPropertyControl... mProperty) {
			super(mSet, mProperty);
		}

		@Override
		void applyValue(final boolean enabled, final NodeModel node, final PropertyChangeEvent evt) {
			final MNodeStyleController styleController = (MNodeStyleController) Controller
			.getCurrentModeController().getExtension(
					NodeStyleController.class);
			if(enabled){
				styleController.setShapeConfiguration(node, ShapeConfigurationModel.NULL_SHAPE
						.withShape(NodeStyleModel.Shape.valueOf(mNodeShape.getValue()))
						.withHorizontalMargin(mShapeHorizontalMargin.getQuantifiedValue())
						.withVerticalMargin(mShapeVerticalMargin.getQuantifiedValue())
						.withUniform(mUniformShape.getBooleanValue())
						);
			}
			else {
				styleController.setShapeConfiguration(node, ShapeConfigurationModel.NULL_SHAPE);
			}
			final Shape shape = styleController.getShape(node);
			enableShapeConfigurationProperties(enabled, shape);
		}

		@Override
		void setStyleOnExternalChange(NodeModel node) {
			final NodeStyleController styleController = NodeStyleController.getController();
			final NodeStyleModel.Shape shape = NodeStyleModel.getShape(node);
			ShapeConfigurationModel viewShape = styleController.getShapeConfiguration(node);
			final boolean enabled = shape != null;
			mSetNodeShape.setValue(enabled);
			mNodeShape.setValue(viewShape.getShape().toString());
			enableShapeConfigurationProperties(enabled, shape);
			mShapeHorizontalMargin.setQuantifiedValue(viewShape.getHorizontalMargin());
			mShapeVerticalMargin.setQuantifiedValue(viewShape.getVerticalMargin());
			mUniformShape.setValue(viewShape.isUniform());
		}
	}
	
	public void addControlGroup(final List<IPropertyControl> controls) {
		mSetNodeShape = new BooleanProperty(ControlGroup.SET_RESOURCE);
		controls.add(mSetNodeShape);
		mNodeShape = new ComboProperty(NODE_SHAPE, EnumToStringMapper.getStringValuesOf(NodeStyleModel.Shape.class));
		controls.add(mNodeShape);
		controls.add(new NextColumnProperty(2));
		mShapeHorizontalMargin = new QuantityProperty<LengthUnits>(SHAPE_HORIZONTAL_MARGIN, 0, 1000, 0.1, LengthUnits.pt);
		controls.add(mShapeHorizontalMargin);
		controls.add(new NextColumnProperty(2));
		mShapeVerticalMargin = new QuantityProperty<LengthUnits>(SHAPE_VERTICAL_MARGIN, 0, 1000, 0.1, LengthUnits.pt);
		controls.add(mShapeVerticalMargin);
		controls.add(new NextColumnProperty(2));
		mUniformShape = new BooleanProperty(UNIFORM_SHAPE);
		controls.add(mUniformShape);
		propertyChangeListener = new NodeShapeChangeListener(mSetNodeShape, mNodeShape, mShapeHorizontalMargin, mShapeVerticalMargin, mUniformShape);
		mSetNodeShape.addPropertyChangeListener(propertyChangeListener);
		mNodeShape.addPropertyChangeListener(propertyChangeListener);
		mShapeHorizontalMargin.addPropertyChangeListener(propertyChangeListener);
		mShapeVerticalMargin.addPropertyChangeListener(propertyChangeListener);
		mUniformShape.addPropertyChangeListener(propertyChangeListener);
	}
	
	public void setStyle(NodeModel node) {
		propertyChangeListener.setStyle(node);
	}
	
	private void enableShapeConfigurationProperties(final boolean enabled, final Shape shape) {
		final boolean enableConfigurationProperties = enabled && shape.hasConfiguration;
		mShapeHorizontalMargin.setEnabled(enableConfigurationProperties);
		mShapeVerticalMargin.setEnabled(enableConfigurationProperties);
		mUniformShape.setEnabled(enableConfigurationProperties);
	}

	
}