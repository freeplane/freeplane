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
import org.freeplane.features.edge.EdgeController;
import org.freeplane.features.edge.EdgeModel;
import org.freeplane.features.edge.EdgeStyle;
import org.freeplane.features.edge.mindmapmode.MEdgeController;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;

import com.jgoodies.forms.builder.DefaultFormBuilder;

/**
 * @author Joe Berry
 * Dec 1, 2016
 */
public class EdgeStyleControlGroup implements ControlGroup {
	private static final String EDGE_STYLE = "edgestyle";
	private static final String[] EDGE_STYLES = EnumToStringMapper.getStringValuesOf(EdgeStyle.class, EdgeStyle.values().length - 1);

	private BooleanProperty mSetEdgeStyle;
	private ComboProperty mEdgeStyle;
	private EdgeStyleChangeListener propertyChangeListener;
	
	private class EdgeStyleChangeListener extends ControlGroupChangeListener {
		public EdgeStyleChangeListener(final BooleanProperty mSet, final IPropertyControl mProperty) {
			super(mSet, mProperty);
		}

		@Override
		void applyValue(final boolean enabled, final NodeModel node,
				final PropertyChangeEvent evt) {
			final MEdgeController styleController = (MEdgeController) Controller
					.getCurrentModeController().getExtension(
							EdgeController.class);
			styleController.setStyle(node, enabled ? EdgeStyle.getStyle(mEdgeStyle.getValue()) : null);
		}
		
		@Override
		void setStyleOnExternalChange(NodeModel node) {
			final EdgeController edgeController = EdgeController.getController();
			final EdgeModel edgeModel = EdgeModel.getModel(node);
			{
				final EdgeStyle style = edgeModel != null ? edgeModel.getStyle() : null;
				final EdgeStyle viewStyle = edgeController.getStyle(node);
				mSetEdgeStyle.setValue(style != null);
				mEdgeStyle.setValue(viewStyle.toString());
			}
		}
	}
	
	@Override
	public void addControlGroup(List<IPropertyControl> controls, DefaultFormBuilder formBuilder) {
		mSetEdgeStyle = new BooleanProperty(ControlGroup.SET_RESOURCE);
		controls.add(mSetEdgeStyle);
		mEdgeStyle = new ComboProperty(EDGE_STYLE, EDGE_STYLES);
		controls.add(mEdgeStyle);
		propertyChangeListener = new EdgeStyleChangeListener(mSetEdgeStyle, mEdgeStyle);
		mSetEdgeStyle.addPropertyChangeListener(propertyChangeListener);
		mEdgeStyle.addPropertyChangeListener(propertyChangeListener);
	}

	@Override
	public void setStyle(NodeModel node) {
		propertyChangeListener.setStyle(node);
	}
	
}
