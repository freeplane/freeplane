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
import org.freeplane.core.resources.components.BooleanProperty;
import org.freeplane.core.resources.components.ComboProperty;
import org.freeplane.core.resources.components.IPropertyControl;
import org.freeplane.core.resources.components.NextColumnProperty;
import org.freeplane.core.resources.components.QuantityProperty;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.nodestyle.NodeGeometryModel;
import org.freeplane.features.nodestyle.NodeStyleController;
import org.freeplane.features.nodestyle.NodeStyleModel;
import org.freeplane.features.nodestyle.NodeStyleShape;
import org.freeplane.features.nodestyle.mindmapmode.MNodeStyleController;
import org.freeplane.features.styles.MapStyleModel;
import org.freeplane.features.styles.LogicalStyleController.StyleOption;

import com.jgoodies.forms.builder.DefaultFormBuilder;

/**
 * @author Joe Berry
 * Nov 27, 2016
 */
class NodeShapeControlGroup implements ControlGroup {
	private static final String NODE_SHAPE = "nodeshape";
	private static final String SHAPE_HORIZONTAL_MARGIN = "shape_horizontal_margin";
	private static final String SHAPE_VERTICAL_MARGIN = "shape_vertical_margin";
	private static final String UNIFORM_SHAPE = "uniform_shape";

	private RevertingProperty mSetNodeShape;
	private ComboProperty mNodeShape;
	
	private QuantityProperty<LengthUnit> mShapeHorizontalMargin;
	private QuantityProperty<LengthUnit> mShapeVerticalMargin;
	private BooleanProperty mUniformShape;

	private NodeShapeChangeListener propertyChangeListener;
	private boolean canEdit;

	private class NodeShapeChangeListener extends ControlGroupChangeListener {
		public NodeShapeChangeListener(final RevertingProperty mSet,final IPropertyControl... mProperty) {
			super(mSet, mProperty);
		}

		@Override
		void applyValue(final boolean enabled, final NodeModel node, final PropertyChangeEvent evt) {
			final MNodeStyleController styleController = (MNodeStyleController) Controller
			.getCurrentModeController().getExtension(
					NodeStyleController.class);
			if(enabled){
				styleController.setShapeConfiguration(node, NodeGeometryModel.NULL_SHAPE
						.withShape(NodeStyleShape.valueOf(mNodeShape.getValue()))
						.withHorizontalMargin(mShapeHorizontalMargin.getQuantifiedValue())
						.withVerticalMargin(mShapeVerticalMargin.getQuantifiedValue())
						.withUniform(mUniformShape.getBooleanValue())
						);
			}
			else {
				styleController.setShapeConfiguration(node, NodeGeometryModel.NULL_SHAPE);
			}
			final NodeStyleShape shape = styleController.getShape(node, StyleOption.FOR_UNSELECTED_NODE);
			enableShapeConfigurationProperties(enabled, shape);
		}

		@Override
		void setStyleOnExternalChange(NodeModel node) {
			final NodeStyleController styleController = NodeStyleController.getController();
			final NodeStyleShape shape = NodeStyleModel.getShape(node);
			final boolean enabled = shape != null;
			mSetNodeShape.setValue(enabled);
			NodeGeometryModel viewShape = styleController.getShapeConfiguration(node, StyleOption.FOR_UNSELECTED_NODE);
			mNodeShape.setValue(viewShape.getShape().toString());
			mShapeHorizontalMargin.setQuantifiedValue(viewShape.getHorizontalMargin());
			mShapeVerticalMargin.setQuantifiedValue(viewShape.getVerticalMargin());
			mUniformShape.setValue(viewShape.isUniform());
		}

        @Override
        void adjustForStyle(NodeModel node) {
            StylePropertyAdjuster.adjustPropertyControl(node, mSetNodeShape);
            StylePropertyAdjuster.adjustPropertyControl(node, mNodeShape);
            StylePropertyAdjuster.adjustPropertyControl(node, mShapeHorizontalMargin);
            StylePropertyAdjuster.adjustPropertyControl(node, mShapeVerticalMargin);
            StylePropertyAdjuster.adjustPropertyControl(node, mUniformShape);
            if(!MapStyleModel.isStyleNode(node) || mNodeShape.isEnabled())
                enableShapeConfigurationProperties(NodeStyleModel.getShape(node) != null, NodeStyleModel.getShape(node));
        }
	}
	
	public void addControlGroup(DefaultFormBuilder formBuilder) {
		mSetNodeShape = new RevertingProperty();
		mNodeShape = ComboProperty.of(NODE_SHAPE, NodeStyleShape.class);
		mShapeHorizontalMargin = new QuantityProperty<LengthUnit>(SHAPE_HORIZONTAL_MARGIN, 0, 1000, 0.1, LengthUnit.pt);
		mShapeVerticalMargin = new QuantityProperty<LengthUnit>(SHAPE_VERTICAL_MARGIN, 0, 1000, 0.1, LengthUnit.pt);
		mUniformShape = new BooleanProperty(UNIFORM_SHAPE);
		propertyChangeListener = new NodeShapeChangeListener(mSetNodeShape, mNodeShape, mShapeHorizontalMargin, mShapeVerticalMargin, mUniformShape);
		mSetNodeShape.addPropertyChangeListener(propertyChangeListener);
		mNodeShape.addPropertyChangeListener(propertyChangeListener);
		mShapeHorizontalMargin.addPropertyChangeListener(propertyChangeListener);
		mShapeVerticalMargin.addPropertyChangeListener(propertyChangeListener);
		mUniformShape.addPropertyChangeListener(propertyChangeListener);
		
		mNodeShape.appendToForm(formBuilder);
		mSetNodeShape.appendToForm(formBuilder);
		mShapeHorizontalMargin.appendToForm(formBuilder);
		formBuilder.nextLine();
		mShapeVerticalMargin.appendToForm(formBuilder);
		formBuilder.nextLine();
		mUniformShape.appendToForm(formBuilder);
		formBuilder.nextLine();
	}
	
	public void setStyle(NodeModel node, boolean canEdit) {
		this.canEdit = canEdit;
		propertyChangeListener.setStyle(node);
	}
	
	private void enableShapeConfigurationProperties(final boolean enabled, final NodeStyleShape shape) {
		final boolean enableConfigurationProperties = enabled && shape.hasConfiguration && canEdit;
		mShapeHorizontalMargin.setEnabled(enableConfigurationProperties);
		mShapeVerticalMargin.setEnabled(enableConfigurationProperties);
		mUniformShape.setEnabled(enableConfigurationProperties);
	}

	
}