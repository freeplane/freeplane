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
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import org.freeplane.core.resources.components.BooleanProperty;
import org.freeplane.core.resources.components.ComboProperty;
import org.freeplane.core.resources.components.IPropertyControl;
import org.freeplane.core.resources.components.NextColumnProperty;
import org.freeplane.core.resources.components.QuantityProperty;
import org.freeplane.core.ui.LengthUnits;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.link.NodeLinks;
import org.freeplane.features.link.mindmapmode.MLinkController;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.nodestyle.NodeStyleController;
import org.freeplane.features.nodestyle.NodeStyleModel;
import org.freeplane.features.nodestyle.ShapeConfigurationModel;
import org.freeplane.features.nodestyle.NodeStyleModel.Shape;
import org.freeplane.features.nodestyle.NodeStyleModel.TextAlign;
import org.freeplane.features.nodestyle.mindmapmode.MNodeStyleController;

/**
 * @author Joe Berry
 * Dec 18, 2016
 */
class NodeFontHyperLinkControlGroup implements ControlGroup {
	static final String NODE_FONT_HYPERLINK = "nodefonthyperlink";

	private BooleanProperty mSetNodeFontHyperlink;
	private BooleanProperty mNodeFontHyperlink;

	private FontHyperlinkChangeListener propertyChangeListener;

	private class FontHyperlinkChangeListener extends ControlGroupChangeListener {
		public FontHyperlinkChangeListener(final BooleanProperty mSet, final IPropertyControl... mProperty) {
			super(mSet, mProperty);
		}

		@Override
    		void applyValue(final boolean enabled, final NodeModel node, final PropertyChangeEvent evt) {
			final MLinkController styleController = (MLinkController) Controller
			.getCurrentModeController().getExtension(
				LinkController.class);
			styleController.setFormatNodeAsHyperlink(node, enabled ? mNodeFontHyperlink.getBooleanValue() : null);
		}

		@Override
		void setStyleOnExternalChange(NodeModel node) {
			final Boolean hyperlink = NodeLinks.formatNodeAsHyperlink(node);
			final Boolean viewhyperlink = LinkController.getController().formatNodeAsHyperlink(node);
			mSetNodeFontHyperlink.setValue(hyperlink != null);
			mNodeFontHyperlink.setValue(viewhyperlink);
		}
	}
	
	public void addControlGroup(final List<IPropertyControl> controls) {
		mSetNodeFontHyperlink = new BooleanProperty(ControlGroup.SET_RESOURCE);
		controls.add(mSetNodeFontHyperlink);
		mNodeFontHyperlink = new BooleanProperty(NODE_FONT_HYPERLINK);
		controls.add(mNodeFontHyperlink);
		propertyChangeListener = new FontHyperlinkChangeListener(mSetNodeFontHyperlink, mNodeFontHyperlink);
		mSetNodeFontHyperlink.addPropertyChangeListener(propertyChangeListener);
		mNodeFontHyperlink.addPropertyChangeListener(propertyChangeListener);
	}
	
	public void setStyle(NodeModel node) {
		propertyChangeListener.setStyle(node);
	}

	
}