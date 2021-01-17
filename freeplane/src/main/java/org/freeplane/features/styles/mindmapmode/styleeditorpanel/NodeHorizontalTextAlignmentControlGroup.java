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
import java.util.Vector;

import org.freeplane.core.resources.components.BooleanProperty;
import org.freeplane.core.resources.components.ComboProperty;
import org.freeplane.core.resources.components.IPropertyControl;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.nodestyle.NodeStyleController;
import org.freeplane.features.nodestyle.NodeStyleModel;
import org.freeplane.features.nodestyle.NodeStyleModel.HorizontalTextAlignment;
import org.freeplane.features.nodestyle.mindmapmode.MNodeStyleController;

import com.jgoodies.forms.builder.DefaultFormBuilder;

/**
 * @author Joe Berry
 * Nov 27, 2016
 */
class NodeHorizontalTextAlignmentControlGroup implements ControlGroup {
	private static final String TEXT_ALIGNMENT = "textalignment";
	
	private static final String[] TEXT_ALIGNMENTS = EnumToStringMapper.getStringValuesOf(HorizontalTextAlignment.class);
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
			styleController.setHorizontalTextAlignment(node, enabled ? HorizontalTextAlignment.valueOf(mNodeTextAlignment.getValue()) : null);
}

		@Override
		void setStyleOnExternalChange(NodeModel node) {
			final NodeStyleController styleController = NodeStyleController.getController();
			final HorizontalTextAlignment style = NodeStyleModel.getHorizontalTextAlignment(node);
			final HorizontalTextAlignment viewStyle = styleController.getHorizontalTextAlignment(node);
			mSetNodeTextAlignment.setValue(style != null);
			mNodeTextAlignment.setValue(viewStyle.toString());
		}
	}
	
	public void addControlGroup(DefaultFormBuilder formBuilder) {
		mSetNodeTextAlignment = new BooleanProperty(ControlGroup.SET_RESOURCE);
		final Vector<String> possibleTranslations = new Vector<String>(TEXT_ALIGNMENTS.length);
		for (int i = 0; i < TEXT_ALIGNMENTS.length; i++) {
			possibleTranslations.add(TextUtils.getText("TextAlignAction." + TEXT_ALIGNMENTS[i] + ".text"));
		}
		Vector<String> translations = possibleTranslations;
		mNodeTextAlignment = new ComboProperty(TEXT_ALIGNMENT, Arrays.asList(TEXT_ALIGNMENTS), translations);
		propertyChangeListener = new NodeTextAlignmentChangeListener(mSetNodeTextAlignment, mNodeTextAlignment);
		mSetNodeTextAlignment.addPropertyChangeListener(propertyChangeListener);
		mNodeTextAlignment.addPropertyChangeListener(propertyChangeListener);
		mSetNodeTextAlignment.appendToForm(formBuilder);
		mNodeTextAlignment.appendToForm(formBuilder);
	}
	
	public void setStyle(NodeModel node, boolean canEdit) {
		propertyChangeListener.setStyle(node);
	}

	
}