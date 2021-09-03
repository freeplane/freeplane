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
import java.beans.PropertyChangeListener;

import org.freeplane.core.resources.components.BooleanProperty;
import org.freeplane.core.resources.components.ComboProperty;
import org.freeplane.core.resources.components.IPropertyControl;
import org.freeplane.features.DashVariant;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.nodestyle.NodeBorderModel;
import org.freeplane.features.nodestyle.NodeStyleController;
import org.freeplane.features.nodestyle.mindmapmode.MNodeStyleController;
import org.freeplane.features.styles.MapStyleModel;
import org.freeplane.features.styles.LogicalStyleController.StyleOption;

import com.jgoodies.forms.builder.DefaultFormBuilder;

/**
 * @author Joe Berry
 * Dec 17, 2016
 */
public class BorderDashAndDashMatchesEdgeControlGroup implements ControlGroup {
	private static final String BORDER_DASH_MATCHES_EDGE_DASH = "border_dash_matches_edge_dash";
	private static final String BORDER_DASH = "border_dash";

	private RevertingProperty mSetBorderDash;
	private ComboProperty mBorderDash;

	private RevertingProperty mSetBorderDashMatchesEdgeDash;
	private BooleanProperty mBorderDashMatchesEdgeDash;
	
	private BorderDashListener borderDashListener;
	private BorderDashMatchesEdgeDashListener borderDashMatchesEdgeDashChangeListener;
	private boolean canEdit;
	
	private class BorderDashListener extends ControlGroupChangeListener {
		public BorderDashListener(final RevertingProperty mSet,final IPropertyControl mProperty) {
			super(mSet, mProperty);
		}

		@Override
		void applyValue(final boolean enabled, final NodeModel node, final PropertyChangeEvent evt) {
			final MNodeStyleController styleController = (MNodeStyleController) Controller
			.getCurrentModeController().getExtension(NodeStyleController.class);
			styleController.setBorderDash(node, enabled ? DashVariant.valueOf(mBorderDash.getValue()): null);
		}

		@Override
		void setStyleOnExternalChange(NodeModel node) {
			final NodeBorderModel nodeBorderModel = NodeBorderModel.getModel(node);
			final NodeStyleController styleController = NodeStyleController.getController();
			final DashVariant dash = nodeBorderModel != null ? nodeBorderModel.getBorderDash() : null;
			final DashVariant viewDash = styleController.getBorderDash(node, StyleOption.FOR_UNSELECTED_NODE);
			mSetBorderDash.setValue(dash != null);
			mBorderDash.setValue(viewDash.name());
		}
		
        @Override
        void adjustForStyle(NodeModel node) {
            StylePropertyAdjuster.adjustPropertyControl(node, mSetBorderDash);
            StylePropertyAdjuster.adjustPropertyControl(node, mBorderDash);
            if(!MapStyleModel.isStyleNode(node) || mBorderDash.isEnabled())
                enableOrDisableBorderDashControls();
        }
	}
	
	private class BorderDashMatchesEdgeDashListener extends ControlGroupChangeListener {
		public BorderDashMatchesEdgeDashListener(final RevertingProperty mSet,final IPropertyControl mProperty) {
			super(mSet, mProperty);
		}

		@Override
		void applyValue(final boolean enabled, final NodeModel node, final PropertyChangeEvent evt) {
			final MNodeStyleController styleController = (MNodeStyleController) Controller
			.getCurrentModeController().getExtension(NodeStyleController.class);
			styleController.setBorderDashMatchesEdgeDash(node, enabled ? mBorderDashMatchesEdgeDash.getBooleanValue(): null);
		}

		@Override
		void setStyleOnExternalChange(NodeModel node) {
			final NodeBorderModel nodeBorderModel = NodeBorderModel.getModel(node);
			final NodeStyleController styleController = NodeStyleController.getController();
			final Boolean match = nodeBorderModel != null ? nodeBorderModel.getBorderDashMatchesEdgeDash() : null;
			final Boolean viewMatch = styleController.getBorderDashMatchesEdgeDash(node, StyleOption.FOR_UNSELECTED_NODE);
			mSetBorderDashMatchesEdgeDash.setValue(match != null);
			mBorderDashMatchesEdgeDash.setValue(viewMatch);
		}
        
        @Override
        void adjustForStyle(NodeModel node) {
            StylePropertyAdjuster.adjustPropertyControl(node, mSetBorderDashMatchesEdgeDash);
            StylePropertyAdjuster.adjustPropertyControl(node, mBorderDashMatchesEdgeDash);
        }
	}
	
	@Override
	public void addControlGroup(DefaultFormBuilder formBuilder) {
		addBorderDashMatchesEdgeDashControl(formBuilder);
		addBorderDashControl(formBuilder);
	}
	
	private void addBorderDashControl(DefaultFormBuilder formBuilder) {
		mSetBorderDash = new RevertingProperty();
		mBorderDash = ComboProperty.of(BORDER_DASH, DashVariant.class);
		borderDashListener = new BorderDashListener(mSetBorderDash, mBorderDash);
		mSetBorderDash.addPropertyChangeListener(borderDashListener);
		mBorderDash.addPropertyChangeListener(borderDashListener);
		mBorderDash.appendToForm(formBuilder);
		mSetBorderDash.appendToForm(formBuilder);
	}
	
	public void addBorderDashMatchesEdgeDashControl(DefaultFormBuilder formBuilder) {
		mSetBorderDashMatchesEdgeDash = new RevertingProperty();
		mBorderDashMatchesEdgeDash = new BooleanProperty(BORDER_DASH_MATCHES_EDGE_DASH);
		borderDashMatchesEdgeDashChangeListener = new BorderDashMatchesEdgeDashListener(mSetBorderDashMatchesEdgeDash, mBorderDashMatchesEdgeDash);
		mSetBorderDashMatchesEdgeDash.addPropertyChangeListener(borderDashMatchesEdgeDashChangeListener);
		mBorderDashMatchesEdgeDash.addPropertyChangeListener(borderDashMatchesEdgeDashChangeListener);
		mBorderDashMatchesEdgeDash.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				enableOrDisableBorderDashControls();
			}
		});
		mBorderDashMatchesEdgeDash.appendToForm(formBuilder);
		mSetBorderDashMatchesEdgeDash.appendToForm(formBuilder);
	}

	@Override
	public void setStyle(NodeModel node, boolean canEdit) {
		this.canEdit = canEdit;
		borderDashListener.setStyle(node);
		borderDashMatchesEdgeDashChangeListener.setStyle(node);
	}

	private void enableOrDisableBorderDashControls() {
		final boolean borderDashCanBeSet = ! mBorderDashMatchesEdgeDash.getBooleanValue();
		mSetBorderDash.setEnabled(borderDashCanBeSet && canEdit);
		mBorderDash.setEnabled(borderDashCanBeSet && canEdit);
	}

}
