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

import org.freeplane.api.ChildNodesAlignment;
import org.freeplane.api.LayoutOrientation;
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
class LayoutOrientationControlGroup implements ControlGroup {
	static final String LAYOUT_ORIÉNTATION = "layout_orientation";
	
	private static final LayoutOrientation[] ORIENTATIONS = {
	        LayoutOrientation.AS_PARENT, 
			LayoutOrientation.LEFT_TO_RIGHT, 
			LayoutOrientation.TOP_DOWN};
	private RevertingProperty mSetLayoutOrientation;
	private ComboProperty mLayoutOrientation;

	private LayoutOrientationChangeListener propertyChangeListener;

	private class LayoutOrientationChangeListener extends ControlGroupChangeListener {
		public LayoutOrientationChangeListener(final RevertingProperty mSet,final IPropertyControl... mProperty) {
			super(mSet, mProperty);
		}

		@Override
		void applyValue(final boolean enabled, final NodeModel node, final PropertyChangeEvent evt) {
			final MLayoutController styleController = (MLayoutController) Controller
					.getCurrentModeController().getExtension(LayoutController.class);
			styleController.setLayoutOrientation(node, enabled ? LayoutOrientation.valueOf(mLayoutOrientation.getValue()) : null);
}

		@Override
		void setStyleOnExternalChange(NodeModel node) {
			LayoutModel model = LayoutModel.getModel(node);
			final LayoutOrientation orientation = model != null ? model.getLayoutOrientation() : LayoutOrientation.AS_PARENT;
			LayoutOrientation displayedValue = displayedValue(node, orientation);
			mLayoutOrientation.setValue(displayedValue.name());
		}

		private LayoutOrientation displayedValue(NodeModel node, final LayoutOrientation orientation) {
			final LayoutController styleController = LayoutController.getController();
			final LayoutOrientation viewOrientation = styleController.getLayoutOrientation(node);
			mSetLayoutOrientation.setValue(orientation != LayoutOrientation.NOT_SET);
            if(viewOrientation == LayoutOrientation.AS_PARENT
                    && node.isRoot())
                return LayoutOrientation.TOP_DOWN;
            else
                return viewOrientation;
		}

        @Override
        void adjustForStyle(NodeModel node) {
            StylePropertyAdjuster.adjustPropertyControl(node, mSetLayoutOrientation);
            StylePropertyAdjuster.adjustPropertyControl(node, mLayoutOrientation);
        }
	}
	
	@Override
    public void addControlGroup(DefaultFormBuilder formBuilder) {
		mSetLayoutOrientation = new RevertingProperty();
		final Vector<String> translations = new Vector<String>(ORIENTATIONS.length);
		for (int i = 0; i < ORIENTATIONS.length; i++) {
			translations.add(TextUtils.getText(ORIENTATIONS[i].name()));
		}
		Collection<String> orientationNames = Stream.of(ORIENTATIONS).map(Enum::name).collect(Collectors.toList());
		mLayoutOrientation = new ComboProperty(LAYOUT_ORIÉNTATION, orientationNames, translations);
		propertyChangeListener = new LayoutOrientationChangeListener(mSetLayoutOrientation, mLayoutOrientation);
		mSetLayoutOrientation.addPropertyChangeListener(propertyChangeListener);
		mLayoutOrientation.addPropertyChangeListener(propertyChangeListener);
		mLayoutOrientation.appendToForm(formBuilder);
		mSetLayoutOrientation.appendToForm(formBuilder);
	}
	
	@Override
    public void setStyle(NodeModel node, boolean canEdit) {
		propertyChangeListener.setStyle(node);
	}

	
}