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

import org.freeplane.api.ChildNodesAlignment;
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
class ChildNodeAlignmentControlGroup implements ControlGroup {
	static final String CHILD_NODES_PLACEMENT = "child_nodes_placement";
	
	private static final ChildNodesAlignment[] ALIGNMENTS = {ChildNodesAlignment.BY_FIRST_NODE, 
			ChildNodesAlignment.BY_CENTER, 
			ChildNodesAlignment.BY_LAST_NODE, 
			ChildNodesAlignment.AS_PARENT};
	private RevertingProperty mSetChildNodesAlignment;
	private ComboProperty mChildNodesAlignment;

	private ChildNodesAlignmentChangeListener propertyChangeListener;

	private class ChildNodesAlignmentChangeListener extends ControlGroupChangeListener {
		public ChildNodesAlignmentChangeListener(final RevertingProperty mSet,final IPropertyControl... mProperty) {
			super(mSet, mProperty);
		}

		@Override
		void applyValue(final boolean enabled, final NodeModel node, final PropertyChangeEvent evt) {
			final MLocationController styleController = (MLocationController) Controller
					.getCurrentModeController().getExtension(LocationController.class);
			styleController.setChildNodesAlignment(node, enabled ? ChildNodesAlignment.valueOf(mChildNodesAlignment.getValue()) : null);
}

		@Override
		void setStyleOnExternalChange(NodeModel node) {
			LocationModel model = LocationModel.getModel(node);
			final ChildNodesAlignment alignment = model != null ? model.getChildNodesAlignment() : LocationModel.DEFAULT_CHILD_NODES_ALIGNMENT;
			ChildNodesAlignment displayedValue = displayedValue(node, alignment);
			mChildNodesAlignment.setValue(displayedValue.name());
		}

		private ChildNodesAlignment displayedValue(NodeModel node, final ChildNodesAlignment alignment) {
			final LocationController styleController = LocationController.getController();
			final ChildNodesAlignment viewAlignment = styleController.getChildNodesAlignment(node);
			mSetChildNodesAlignment.setValue(alignment != LocationModel.DEFAULT_CHILD_NODES_ALIGNMENT);
			ChildNodesAlignment displayedValue;
			if(viewAlignment == ChildNodesAlignment.AS_PARENT
					&& node.isRoot())
				return ChildNodesAlignment.BY_CENTER;
			else if (viewAlignment != LocationModel.DEFAULT_CHILD_NODES_ALIGNMENT)
				displayedValue = viewAlignment;
			else
				displayedValue = ChildNodesAlignment.BY_CENTER;
			return displayedValue;
		}

        @Override
        void adjustForStyle(NodeModel node) {
            StylePropertyAdjuster.adjustPropertyControl(node, mSetChildNodesAlignment);
            StylePropertyAdjuster.adjustPropertyControl(node, mChildNodesAlignment);
        }
	}
	
	public void addControlGroup(DefaultFormBuilder formBuilder) {
		mSetChildNodesAlignment = new RevertingProperty();
		final Vector<String> translations = new Vector<String>(ALIGNMENTS.length);
		for (int i = 0; i < ALIGNMENTS.length; i++) {
			translations.add(TextUtils.getText(ALIGNMENTS[i].name()));
		}
		Collection<String> alignmentNames = Stream.of(ALIGNMENTS).map(Enum::name).collect(Collectors.toList());
		mChildNodesAlignment = new ComboProperty(CHILD_NODES_PLACEMENT, alignmentNames, translations);
		propertyChangeListener = new ChildNodesAlignmentChangeListener(mSetChildNodesAlignment, mChildNodesAlignment);
		mSetChildNodesAlignment.addPropertyChangeListener(propertyChangeListener);
		mChildNodesAlignment.addPropertyChangeListener(propertyChangeListener);
		mChildNodesAlignment.appendToForm(formBuilder);
		mSetChildNodesAlignment.appendToForm(formBuilder);
	}
	
	public void setStyle(NodeModel node, boolean canEdit) {
		propertyChangeListener.setStyle(node);
	}

	
}