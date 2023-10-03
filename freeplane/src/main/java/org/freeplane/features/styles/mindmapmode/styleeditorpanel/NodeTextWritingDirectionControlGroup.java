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

import org.freeplane.api.TextWritingDirection;
import org.freeplane.core.resources.components.ComboProperty;
import org.freeplane.core.resources.components.IPropertyControl;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.nodestyle.NodeStyleController;
import org.freeplane.features.nodestyle.NodeStyleModel;
import org.freeplane.features.nodestyle.mindmapmode.MNodeStyleController;
import org.freeplane.features.styles.LogicalStyleController.StyleOption;

import com.jgoodies.forms.builder.DefaultFormBuilder;

/**
 * @author Joe Berry
 * Nov 27, 2016
 */
class NodeTextWritingDirectionControlGroup implements ControlGroup {
	static final String TEXT_DIRECTION = "textdirection";

	private static final String[] TEXT_DIRECTIONS = EnumToStringMapper.getStringValuesOf(TextWritingDirection.class);
	private RevertingProperty mSetNodeTextDirection;
	private ComboProperty mNodeTextDirection;

	private NodeTextDirectionChangeListener propertyChangeListener;

	private class NodeTextDirectionChangeListener extends ControlGroupChangeListener {
		public NodeTextDirectionChangeListener(final RevertingProperty mSet,final IPropertyControl... mProperty) {
			super(mSet, mProperty);
		}

		@Override
		void applyValue(final boolean enabled, final NodeModel node, final PropertyChangeEvent evt) {
			final MNodeStyleController styleController = (MNodeStyleController) Controller
					.getCurrentModeController().getExtension(NodeStyleController.class);
			styleController.setTextWritingDirection(node, enabled ? TextWritingDirection.valueOf(mNodeTextDirection.getValue()) : null);
}

		@Override
		void setStyleOnExternalChange(NodeModel node) {
			final NodeStyleController styleController = NodeStyleController.getController();
			final TextWritingDirection style = NodeStyleModel.getTextWritingDirection(node);
			final TextWritingDirection viewStyle = styleController.getTextWritingDirection(node, StyleOption.FOR_UNSELECTED_NODE);
			mSetNodeTextDirection.setValue(style != null);
			mNodeTextDirection.setValue(viewStyle.name());
		}

        @Override
        void adjustForStyle(NodeModel node) {
            StylePropertyAdjuster.adjustPropertyControl(node, mSetNodeTextDirection);
            StylePropertyAdjuster.adjustPropertyControl(node, mNodeTextDirection);
        }
	}

	public void addControlGroup(DefaultFormBuilder formBuilder) {
		mSetNodeTextDirection = new RevertingProperty();
		final Vector<String> possibleTranslations = new Vector<String>(TEXT_DIRECTIONS.length);
		for (int i = 0; i < TEXT_DIRECTIONS.length; i++) {
			possibleTranslations.add(TextUtils.getText("TextWritingDirectionAction." + TEXT_DIRECTIONS[i] + ".text"));
		}
		Vector<String> translations = possibleTranslations;
		mNodeTextDirection = new ComboProperty(TEXT_DIRECTION, Arrays.asList(TEXT_DIRECTIONS), translations);
		propertyChangeListener = new NodeTextDirectionChangeListener(mSetNodeTextDirection, mNodeTextDirection);
		mSetNodeTextDirection.addPropertyChangeListener(propertyChangeListener);
		mNodeTextDirection.addPropertyChangeListener(propertyChangeListener);
		mNodeTextDirection.appendToForm(formBuilder);
		mSetNodeTextDirection.appendToForm(formBuilder);
	}

	public void setStyle(NodeModel node, boolean canEdit) {
		propertyChangeListener.setStyle(node);
	}


}