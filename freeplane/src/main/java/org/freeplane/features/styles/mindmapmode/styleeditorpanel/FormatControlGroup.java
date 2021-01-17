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
import org.freeplane.features.format.FormatController;
import org.freeplane.features.format.IFormattedObject;
import org.freeplane.features.format.PatternFormat;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.nodestyle.NodeStyleController;
import org.freeplane.features.nodestyle.NodeStyleModel;
import org.freeplane.features.nodestyle.mindmapmode.MNodeStyleController;
import org.freeplane.features.styles.mindmapmode.EditablePatternComboProperty;
import org.freeplane.features.text.TextController;

import com.jgoodies.forms.builder.DefaultFormBuilder;

/**
 * @author Joe Berry
 * Dec 1, 2016
 */
public class FormatControlGroup implements ControlGroup {
	private static final String NODE_FORMAT = "nodeformat";

	private BooleanProperty mSetNodeFormat;
	private EditablePatternComboProperty mNodeFormat;
	
	private NodeFormatChangeListener propertyChangeListener;
	
	private class NodeFormatChangeListener extends ControlGroupChangeListener {
		public NodeFormatChangeListener(final BooleanProperty mSet, final IPropertyControl mProperty) {
			super(mSet, mProperty);
		}

		@Override
		void applyValue(final boolean enabled, final NodeModel node, final PropertyChangeEvent evt) {
			final MNodeStyleController styleController = (MNodeStyleController) Controller.getCurrentModeController()
				    .getExtension(NodeStyleController.class);
				styleController.setNodeFormat(node, enabled ? mNodeFormat.getSelectedPattern() : null);
		}

		@Override
		void setStyleOnExternalChange(NodeModel node) {
			String nodeFormat = NodeStyleModel.getNodeFormat(node);
			String viewNodeFormat = TextController.getController().getNodeFormat(node);
			mSetNodeFormat.setValue(nodeFormat != null);
			if (viewNodeFormat == null && node.getUserObject() instanceof IFormattedObject)
				viewNodeFormat = ((IFormattedObject)node.getUserObject()).getPattern();
			mNodeFormat.setValue(viewNodeFormat);
		}
	}

	@Override
	public void setStyle(NodeModel node, boolean canEdit) {
		propertyChangeListener.setStyle(node);
	}

	@Override
	public void addControlGroup(DefaultFormBuilder formBuilder) {
        mSetNodeFormat = new BooleanProperty(ControlGroup.SET_RESOURCE);
        mNodeFormat = new EditablePatternComboProperty(NODE_FORMAT,
            PatternFormat.getIdentityPatternFormat(), FormatController.getController().getAllFormats());
        propertyChangeListener = new NodeFormatChangeListener(mSetNodeFormat, mNodeFormat);
        mSetNodeFormat.addPropertyChangeListener(propertyChangeListener);
        mNodeFormat.addPropertyChangeListener(propertyChangeListener);
        mSetNodeFormat.appendToForm(formBuilder);
        mNodeFormat.appendToForm(formBuilder);
	}
}
