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

import org.freeplane.core.resources.components.BooleanProperty;
import org.freeplane.core.resources.components.IPropertyControl;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.nodestyle.NodeStyleController;
import org.freeplane.features.nodestyle.NodeStyleModel;
import org.freeplane.features.nodestyle.mindmapmode.MNodeStyleController;

import com.jgoodies.forms.builder.DefaultFormBuilder;


public class FontStrikeThroughControlGroup implements ControlGroup {
	private static final String NODE_FONT_STRIKE_THROUGH = "nodefontstrikethrough";

	private BooleanProperty mSetNodeFontStrikeThrough;
	private BooleanProperty mNodeFontStrikeThrough;
	private FontStrikeThroughChangeListener propertyChangeListener;
	
	private class FontStrikeThroughChangeListener extends ControlGroupChangeListener {
		public FontStrikeThroughChangeListener(final BooleanProperty mSet, final IPropertyControl mProperty) {
			super(mSet, mProperty);
		}

		@Override
		void applyValue(final boolean enabled, final NodeModel node, final PropertyChangeEvent evt) {
			final MNodeStyleController styleController = (MNodeStyleController) Controller
			.getCurrentModeController().getExtension(
					NodeStyleController.class);
			styleController.setStrikedThrough(node, enabled ? mNodeFontStrikeThrough.getBooleanValue() : null);
		}

		@Override
		void setStyleOnExternalChange(NodeModel node) {
			final NodeStyleController styleController = NodeStyleController.getController();
			final Boolean strikeThrough = NodeStyleModel.isStrikedThrough(node);
			final Boolean viewstrikeThrough = styleController.isStrikedThrough(node);
			mSetNodeFontStrikeThrough.setValue(strikeThrough != null);
			mNodeFontStrikeThrough.setValue(viewstrikeThrough);
		}
	}

	@Override
	public void setStyle(NodeModel node) {
		propertyChangeListener.setStyle(node);
	}

	@Override
	public void addControlGroup(DefaultFormBuilder formBuilder) {
		mSetNodeFontStrikeThrough = new BooleanProperty(ControlGroup.SET_RESOURCE);
		mNodeFontStrikeThrough = new BooleanProperty(NODE_FONT_STRIKE_THROUGH);
		propertyChangeListener = new FontStrikeThroughChangeListener(mSetNodeFontStrikeThrough, mNodeFontStrikeThrough);
		mSetNodeFontStrikeThrough.addPropertyChangeListener(propertyChangeListener);
		mNodeFontStrikeThrough.addPropertyChangeListener(propertyChangeListener);
		mSetNodeFontStrikeThrough.layout(formBuilder);
		mNodeFontStrikeThrough.layout(formBuilder);
	}
}
