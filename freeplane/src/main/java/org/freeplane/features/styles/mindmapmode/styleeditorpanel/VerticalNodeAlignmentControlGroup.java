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
import java.util.Collection;
import java.util.Enumeration;
import java.util.Vector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.freeplane.api.VerticalNodeAlignment;
import org.freeplane.core.resources.components.ComboProperty;
import org.freeplane.core.resources.components.IPropertyControl;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.nodelocation.LocationController;
import org.freeplane.features.nodelocation.LocationModel;
import org.freeplane.features.nodelocation.mindmapmode.MLocationController;

import com.jgoodies.forms.builder.DefaultFormBuilder;

/**
 * @author Joe Berry
 * Nov 27, 2016
 */
class VerticalNodeAlignmentControlGroup implements ControlGroup {
	static final String CHILD_VERTICAL_PLACEMENT = "child_vertical_placement";
	
	private static final VerticalNodeAlignment[] ALIGNMENTS = {VerticalNodeAlignment.TOP, 
			VerticalNodeAlignment.CENTER, 
			VerticalNodeAlignment.BOTTOM, 
			VerticalNodeAlignment.AS_PARENT};
	private RevertingProperty mSetVerticalNodeAlignment;
	private ComboProperty mVerticalNodeAlignment;

	private VerticalNodeAlignmentChangeListener propertyChangeListener;

	private class VerticalNodeAlignmentChangeListener extends ControlGroupChangeListener {
		public VerticalNodeAlignmentChangeListener(final RevertingProperty mSet,final IPropertyControl... mProperty) {
			super(mSet, mProperty);
		}

		@Override
		void applyValue(final boolean enabled, final NodeModel node, final PropertyChangeEvent evt) {
			final MLocationController styleController = (MLocationController) Controller
					.getCurrentModeController().getExtension(LocationController.class);
			styleController.setVerticalAlignment(node, enabled ? VerticalNodeAlignment.valueOf(mVerticalNodeAlignment.getValue()) : null);
}

		@Override
		void setStyleOnExternalChange(NodeModel node) {
			LocationModel model = LocationModel.getModel(node);
			final VerticalNodeAlignment alignment = model != null ? model.getVerticalAlignment() : LocationModel.DEFAULT_VERTICAL_ALIGNMENT;
			VerticalNodeAlignment displayedValue = displayedValue(node, alignment);
			mVerticalNodeAlignment.setValue(displayedValue.name());
		}

		private VerticalNodeAlignment displayedValue(NodeModel node, final VerticalNodeAlignment alignment) {
			final LocationController styleController = LocationController.getController();
			final VerticalNodeAlignment viewAlignment = styleController.getVerticalAlignment(node);
			mSetVerticalNodeAlignment.setValue(alignment != LocationModel.DEFAULT_VERTICAL_ALIGNMENT);
			VerticalNodeAlignment displayedValue;
			if(viewAlignment == VerticalNodeAlignment.AS_PARENT
					&& node.isRoot())
				return VerticalNodeAlignment.CENTER;
			else if (viewAlignment != LocationModel.DEFAULT_VERTICAL_ALIGNMENT)
				displayedValue = viewAlignment;
			else
				displayedValue = VerticalNodeAlignment.CENTER;
			return displayedValue;
		}

        @Override
        void adjustForStyle(NodeModel node) {
            StylePropertyAdjuster.adjustPropertyControl(node, mSetVerticalNodeAlignment);
            StylePropertyAdjuster.adjustPropertyControl(node, mVerticalNodeAlignment);
        }
	}
	
	public void addControlGroup(DefaultFormBuilder formBuilder) {
		mSetVerticalNodeAlignment = new RevertingProperty();
		final Vector<String> translations = new Vector<String>(ALIGNMENTS.length);
		for (int i = 0; i < ALIGNMENTS.length; i++) {
			translations.add(TextUtils.getText(ALIGNMENTS[i].name()));
		}
		Collection<String> alignmentNames = Stream.of(ALIGNMENTS).map(Enum::name).collect(Collectors.toList());
		mVerticalNodeAlignment = new ComboProperty(CHILD_VERTICAL_PLACEMENT, alignmentNames, translations);
		propertyChangeListener = new VerticalNodeAlignmentChangeListener(mSetVerticalNodeAlignment, mVerticalNodeAlignment);
		mSetVerticalNodeAlignment.addPropertyChangeListener(propertyChangeListener);
		mVerticalNodeAlignment.addPropertyChangeListener(propertyChangeListener);
		mVerticalNodeAlignment.appendToForm(formBuilder);
		mSetVerticalNodeAlignment.appendToForm(formBuilder);
	}
	
	public void setStyle(NodeModel node, boolean canEdit) {
		propertyChangeListener.setStyle(node);
	}

	
}