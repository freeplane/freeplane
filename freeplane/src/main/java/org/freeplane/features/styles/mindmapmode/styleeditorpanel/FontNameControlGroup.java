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
import org.freeplane.core.resources.components.FontProperty;
import org.freeplane.core.resources.components.IPropertyControl;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.nodestyle.NodeStyleController;
import org.freeplane.features.nodestyle.NodeStyleModel;
import org.freeplane.features.nodestyle.mindmapmode.MNodeStyleController;

import com.jgoodies.forms.builder.DefaultFormBuilder;

/**
 * @author Joe Berry
 * Dec 1, 2016
 */
public class FontNameControlGroup implements ControlGroup {
	private static final String NODE_FONT_NAME = "nodefontname";

	private BooleanProperty mSetNodeFontName;
	private FontProperty mNodeFontName;

	private FontNameChangeListener propertyChangeListener;
	
	private class FontNameChangeListener extends ControlGroupChangeListener {
		public FontNameChangeListener(final BooleanProperty mSet, final IPropertyControl mProperty) {
			super(mSet, mProperty);
		}

		@Override
		void applyValue(final boolean enabled, final NodeModel node, final PropertyChangeEvent evt) {
			final MNodeStyleController styleController = (MNodeStyleController) Controller
			.getCurrentModeController().getExtension(
					NodeStyleController.class);
			styleController.setFontFamily(node, enabled ? mNodeFontName.getValue() : null);
		}

		@Override
		void setStyleOnExternalChange(NodeModel node) {
			final NodeStyleController styleController = NodeStyleController.getController();
			final String fontFamilyName = NodeStyleModel.getFontFamilyName(node);
			final String viewFontFamilyName = styleController.getFontFamilyName(node);
			mSetNodeFontName.setValue(fontFamilyName != null);
			mNodeFontName.setValue(viewFontFamilyName);
		}
	}

	@Override
	public void setStyle(NodeModel node) {
		propertyChangeListener.setStyle(node);
	}

	@Override
	public void addControlGroup(List<IPropertyControl> controls, DefaultFormBuilder formBuilder) {
		mSetNodeFontName = new BooleanProperty(ControlGroup.SET_RESOURCE);
		controls.add(mSetNodeFontName);
		mNodeFontName = new FontProperty(NODE_FONT_NAME);
		controls.add(mNodeFontName);
		propertyChangeListener = new FontNameChangeListener(mSetNodeFontName, mNodeFontName);
		mSetNodeFontName.addPropertyChangeListener(propertyChangeListener);
		mNodeFontName.addPropertyChangeListener(propertyChangeListener);
	}
}
