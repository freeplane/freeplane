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
import java.util.Collection;
import java.util.Vector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.freeplane.api.ChildrenSides;
import org.freeplane.core.resources.components.ComboProperty;
import org.freeplane.core.resources.components.IPropertyControl;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.layout.LayoutController;
import org.freeplane.features.layout.LayoutModel;
import org.freeplane.features.layout.mindmapmode.MLayoutController;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;

import com.jgoodies.forms.builder.DefaultFormBuilder;

/**
 * @author Joe Berry
 * Nov 27, 2016
 */
class ChildrenSidesControlGroup implements ControlGroup {
	static final String CHILDREN_SIDES = "children_sides";
	
	private static final ChildrenSides[] SIDES = {
	        ChildrenSides.AUTO, 
			ChildrenSides.BOTH_SIDES, 
			ChildrenSides.BOTTOM_OR_RIGHT, 
			ChildrenSides.TOP_OR_LEFT
	};
	private RevertingProperty mSetChildrenSides;
	private ComboProperty mChildrenSides;

	private ChildrenSidesChangeListener propertyChangeListener;

	private class ChildrenSidesChangeListener extends ControlGroupChangeListener {
		public ChildrenSidesChangeListener(final RevertingProperty mSet,final IPropertyControl... mProperty) {
			super(mSet, mProperty);
		}

		@Override
		void applyValue(final boolean enabled, final NodeModel node, final PropertyChangeEvent evt) {
			final MLayoutController styleController = (MLayoutController) Controller
					.getCurrentModeController().getExtension(LayoutController.class);
			styleController.setChildrenSides(node, enabled ? ChildrenSides.valueOf(mChildrenSides.getValue()) : null);
}

		@Override
		void setStyleOnExternalChange(NodeModel node) {
			LayoutModel model = LayoutModel.getModel(node);
			final ChildrenSides alignment = model != null ? model.getChildrenSides() : ChildrenSides.NOT_SET;
			ChildrenSides displayedValue = displayedValue(node, alignment);
			mChildrenSides.setValue(displayedValue.name());
		}

		private ChildrenSides displayedValue(NodeModel node, final ChildrenSides alignment) {
			final LayoutController styleController = LayoutController.getController();
			final ChildrenSides displayedValue = styleController.getChildrenSides(node);
			mSetChildrenSides.setValue(alignment != ChildrenSides.NOT_SET);
			return displayedValue;
		}

        @Override
        void adjustForStyle(NodeModel node) {
            StylePropertyAdjuster.adjustPropertyControl(node, mSetChildrenSides);
            StylePropertyAdjuster.adjustPropertyControl(node, mChildrenSides);
        }
	}
	
	public void addControlGroup(DefaultFormBuilder formBuilder) {
		mSetChildrenSides = new RevertingProperty();
		final Vector<String> translations = new Vector<String>(SIDES.length);
		for (int i = 0; i < SIDES.length; i++) {
			translations.add(TextUtils.getText(SIDES[i].name()));
		}
		Collection<String> alignmentNames = Stream.of(SIDES).map(Enum::name).collect(Collectors.toList());
		mChildrenSides = new ComboProperty(CHILDREN_SIDES, alignmentNames, translations);
		propertyChangeListener = new ChildrenSidesChangeListener(mSetChildrenSides, mChildrenSides);
		mSetChildrenSides.addPropertyChangeListener(propertyChangeListener);
		mChildrenSides.addPropertyChangeListener(propertyChangeListener);
		mChildrenSides.appendToForm(formBuilder);
		mSetChildrenSides.appendToForm(formBuilder);
	}
	
	public void setStyle(NodeModel node, boolean canEdit) {
		propertyChangeListener.setStyle(node);
	}

	
}