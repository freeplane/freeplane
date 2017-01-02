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
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.nodestyle.NodeStyleController;
import org.freeplane.features.nodestyle.NodeStyleModel;
import org.freeplane.features.nodestyle.NodeStyleModel.TextAlign;
import org.freeplane.features.nodestyle.mindmapmode.MNodeStyleController;

import com.jgoodies.forms.builder.DefaultFormBuilder;

/**
 * @author Joe Berry
 * Nov 27, 2016
 */
class NodeTextAlignmentControlGroup implements ControlGroup {
	private static final String TEXT_ALIGNMENT = "textalignment";
	
	private static final String[] TEXT_ALIGNMENTS = EnumToStringMapper.getStringValuesOf(TextAlign.class);
	private BooleanProperty mSetNodeTextAlignment;
	private ComboProperty mNodeTextAlignment;

	private NodeTextAlignmentChangeListener propertyChangeListener;

	private class NodeTextAlignmentChangeListener extends ControlGroupChangeListener {
		public NodeTextAlignmentChangeListener(final BooleanProperty mSet, final IPropertyControl... mProperty) {
			super(mSet, mProperty);
		}

		@Override
		void applyValue(final boolean enabled, final NodeModel node, final PropertyChangeEvent evt) {
			final MNodeStyleController styleController = (MNodeStyleController) Controller
					.getCurrentModeController().getExtension(NodeStyleController.class);
			styleController.setTextAlign(node, enabled ? TextAlign.valueOf(mNodeTextAlignment.getValue()) : null);
}

		@Override
		void setStyleOnExternalChange(NodeModel node) {
			final NodeStyleController styleController = NodeStyleController.getController();
			final TextAlign style = NodeStyleModel.getTextAlign(node);
			final TextAlign viewStyle = styleController.getTextAlign(node);
			mSetNodeTextAlignment.setValue(style != null);
			mNodeTextAlignment.setValue(viewStyle.toString());
		}
	}
	
	public void addControlGroup(final List<IPropertyControl> controls, DefaultFormBuilder formBuilder) {
		mSetNodeTextAlignment = new BooleanProperty(ControlGroup.SET_RESOURCE);
		controls.add(mSetNodeTextAlignment);
		final Vector<String> possibleTranslations = new Vector<String>(TEXT_ALIGNMENTS.length);
		for (int i = 0; i < TEXT_ALIGNMENTS.length; i++) {
			possibleTranslations.add(TextUtils.getText("TextAlignAction." + TEXT_ALIGNMENTS[i] + ".text"));
		}
		Vector<String> translations = possibleTranslations;
		mNodeTextAlignment = new ComboProperty(TEXT_ALIGNMENT, Arrays.asList(TEXT_ALIGNMENTS), translations);
		controls.add(mNodeTextAlignment);
		propertyChangeListener = new NodeTextAlignmentChangeListener(mSetNodeTextAlignment, mNodeTextAlignment);
		mSetNodeTextAlignment.addPropertyChangeListener(propertyChangeListener);
		mNodeTextAlignment.addPropertyChangeListener(propertyChangeListener);
	}
	
	public void setStyle(NodeModel node) {
		propertyChangeListener.setStyle(node);
	}

	
}