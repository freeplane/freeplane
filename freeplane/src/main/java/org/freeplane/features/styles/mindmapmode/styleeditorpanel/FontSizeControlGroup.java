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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.freeplane.core.resources.components.BooleanProperty;
import org.freeplane.core.resources.components.ComboProperty;
import org.freeplane.core.resources.components.IPropertyControl;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.nodestyle.NodeStyleController;
import org.freeplane.features.nodestyle.NodeStyleModel;
import org.freeplane.features.nodestyle.mindmapmode.MNodeStyleController;
import org.freeplane.features.styles.mindmapmode.MUIFactory;

import com.jgoodies.forms.builder.DefaultFormBuilder;

/**
 * @author Joe Berry
 * Dec 1, 2016
 */
public class FontSizeControlGroup implements ControlGroup {
	private static final String NODE_FONT_SIZE = "nodefontsize";

	private BooleanProperty mSetNodeFontSize;
	private ComboProperty mNodeFontSize;

	private FontSizeChangeListener propertyChangeListener;
	
	private class FontSizeChangeListener extends ControlGroupChangeListener {
		public FontSizeChangeListener(final BooleanProperty mSet, final IPropertyControl mProperty) {
			super(mSet, mProperty);
		}

		@Override
		void applyValue(final boolean enabled, final NodeModel node, final PropertyChangeEvent evt) {
			final MNodeStyleController styleController = (MNodeStyleController) Controller
			.getCurrentModeController().getExtension(
					NodeStyleController.class);
			try {
	            styleController.setFontSize(node, enabled ? Integer.valueOf(mNodeFontSize.getValue()) : null);
            }
            catch (NumberFormatException e) {
            }
		}

		@Override
		void setStyleOnExternalChange(NodeModel node) {
			final NodeStyleController styleController = NodeStyleController.getController();
			final Integer fontSize = NodeStyleModel.getFontSize(node);
			final Integer viewfontSize = styleController.getFontSize(node);
			mSetNodeFontSize.setValue(fontSize != null);
			mNodeFontSize.setValue(viewfontSize.toString());
		}
	}

	@Override
	public void setStyle(NodeModel node, boolean canEdit) {
		propertyChangeListener.setStyle(node);
	}

	@Override
	public void addControlGroup(DefaultFormBuilder formBuilder) {
		mSetNodeFontSize = new BooleanProperty(ControlGroup.SET_RESOURCE);
		final List<String> sizesVector = new ArrayList<String>(Arrays.asList(MUIFactory.FONT_SIZES));
		mNodeFontSize = new ComboProperty(NODE_FONT_SIZE, sizesVector, sizesVector);
		mNodeFontSize.setEditable(true);
		propertyChangeListener = new FontSizeChangeListener(mSetNodeFontSize, mNodeFontSize);
		mSetNodeFontSize.addPropertyChangeListener(propertyChangeListener);
		mNodeFontSize.addPropertyChangeListener(propertyChangeListener);
		mSetNodeFontSize.layout(formBuilder);
		mNodeFontSize.layout(formBuilder);
	}
}
