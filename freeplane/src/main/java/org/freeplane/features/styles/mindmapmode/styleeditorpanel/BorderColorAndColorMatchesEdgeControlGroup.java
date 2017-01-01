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

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import org.freeplane.core.resources.components.BooleanProperty;
import org.freeplane.core.resources.components.ColorProperty;
import org.freeplane.core.resources.components.IPropertyControl;
import org.freeplane.core.util.ColorUtils;
import org.freeplane.features.edge.EdgeController;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.nodestyle.NodeBorderModel;
import org.freeplane.features.nodestyle.NodeStyleController;
import org.freeplane.features.nodestyle.mindmapmode.MNodeStyleController;

/**
 * @author Joe Berry
 * Dec 17, 2016
 */
public class BorderColorAndColorMatchesEdgeControlGroup implements ControlGroup {
	private static final String BORDER_COLOR_MATCHES_EDGE_COLOR = "border_color_matches_edge_color";
	private static final String BORDER_COLOR = "border_color";
	
	private BooleanProperty mSetBorderColor;
	private ColorProperty mBorderColor;

	private BooleanProperty mSetBorderColorMatchesEdgeColor;
	private BooleanProperty mBorderColorMatchesEdgeColor;
	
	private BorderColorListener borderColorListener;
	private BorderColorMatchesEdgeColorListener borderColorMatchesEdgeColorChangeListener;
	
	private class BorderColorListener extends ControlGroupChangeListener {
		public BorderColorListener(final BooleanProperty mSet, final IPropertyControl mProperty) {
			super(mSet, mProperty);
		}

		@Override
		void applyValue(final boolean enabled, final NodeModel node, final PropertyChangeEvent evt) {
			final MNodeStyleController styleController = (MNodeStyleController) Controller
			.getCurrentModeController().getExtension(NodeStyleController.class);
			styleController.setBorderColor(node, enabled ? mBorderColor.getColorValue(): null);
		}

		@Override
		void setStyleOnExternalChange(NodeModel node) {
			final NodeStyleController styleController = NodeStyleController.getController();
			final NodeBorderModel nodeBorderModel = NodeBorderModel.getModel(node);
			final Color color = nodeBorderModel != null ? nodeBorderModel.getBorderColor() : null;
			final Color viewColor = styleController.getBorderColor(node);
			mSetBorderColor.setValue(color != null);
			mBorderColor.setColorValue(viewColor);
			enableOrDisableBorderColorControls();
		}
	}
	
	private class BorderColorMatchesEdgeColorListener extends ControlGroupChangeListener {
		public BorderColorMatchesEdgeColorListener(final BooleanProperty mSet, final IPropertyControl mProperty) {
			super(mSet, mProperty);
		}

		@Override
		void applyValue(final boolean enabled, final NodeModel node, final PropertyChangeEvent evt) {
			final MNodeStyleController styleController = (MNodeStyleController) Controller
			.getCurrentModeController().getExtension(NodeStyleController.class);
			styleController.setBorderColorMatchesEdgeColor(node, enabled ? mBorderColorMatchesEdgeColor.getBooleanValue(): null);
		}

		@Override
		void setStyleOnExternalChange(NodeModel node) {
			final NodeStyleController styleController = NodeStyleController.getController();
			final NodeBorderModel nodeBorderModel = NodeBorderModel.getModel(node);
			final Boolean match = nodeBorderModel != null ? nodeBorderModel.getBorderColorMatchesEdgeColor() : null;
			final Boolean viewMatch = styleController.getBorderColorMatchesEdgeColor(node);
			mSetBorderColorMatchesEdgeColor.setValue(match != null);
			mBorderColorMatchesEdgeColor.setValue(viewMatch);
		}
	}
	
	@Override
	public void addControlGroup(List<IPropertyControl> controls) {
		addBorderColorMatchesEdgeColorControl(controls);
		addBorderColorControl(controls);
	}
	
	private void addBorderColorControl(final List<IPropertyControl> controls) {
		mSetBorderColor = new BooleanProperty(ControlGroup.SET_RESOURCE);
		controls.add(mSetBorderColor);
		mBorderColor = new ColorProperty(BORDER_COLOR, ColorUtils.colorToString(EdgeController.STANDARD_EDGE_COLOR));
		controls.add(mBorderColor);
		borderColorListener = new BorderColorListener(mSetBorderColor, mBorderColor);
		mSetBorderColor.addPropertyChangeListener(borderColorListener);
		mBorderColor.addPropertyChangeListener(borderColorListener);
	}
	
	public void addBorderColorMatchesEdgeColorControl(List<IPropertyControl> controls) {
		mSetBorderColorMatchesEdgeColor = new BooleanProperty(ControlGroup.SET_RESOURCE);
		controls.add(mSetBorderColorMatchesEdgeColor);
		mBorderColorMatchesEdgeColor = new BooleanProperty(BORDER_COLOR_MATCHES_EDGE_COLOR);
		controls.add(mBorderColorMatchesEdgeColor);
		borderColorMatchesEdgeColorChangeListener = new BorderColorMatchesEdgeColorListener(mSetBorderColorMatchesEdgeColor, mBorderColorMatchesEdgeColor);
		mSetBorderColorMatchesEdgeColor.addPropertyChangeListener(borderColorMatchesEdgeColorChangeListener);
		mBorderColorMatchesEdgeColor.addPropertyChangeListener(borderColorMatchesEdgeColorChangeListener);
		mBorderColorMatchesEdgeColor.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				enableOrDisableBorderColorControls();
			}
		});
	}

	@Override
	public void setStyle(NodeModel node) {
		borderColorListener.setStyle(node);
		borderColorMatchesEdgeColorChangeListener.setStyle(node);
	}

	private void enableOrDisableBorderColorControls() {
		final boolean borderColorCanBeSet = ! mBorderColorMatchesEdgeColor.getBooleanValue();
		mSetBorderColor.setEnabled(borderColorCanBeSet);
		mBorderColor.setEnabled(borderColorCanBeSet);
	}
}
