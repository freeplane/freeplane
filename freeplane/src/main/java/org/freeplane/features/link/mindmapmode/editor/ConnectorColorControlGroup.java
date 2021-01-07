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
package org.freeplane.features.link.mindmapmode.editor;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.util.Optional;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.resources.components.BooleanProperty;
import org.freeplane.core.resources.components.ColorProperty;
import org.freeplane.core.resources.components.IPropertyControl;
import org.freeplane.core.util.ColorUtils;
import org.freeplane.features.edge.EdgeController;
import org.freeplane.features.link.ConnectorModel;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.link.mindmapmode.MLinkController;

import com.jgoodies.forms.builder.DefaultFormBuilder;

class ConnectorColorControlGroup implements ControlGroup {
	private static final String CONNECTOR_COLOR = "connector_color";

	private BooleanProperty setColor;
	private ColorProperty color;
	private ConnectorColorChangeListener propertyChangeListener;
	private ConnectorModel connector;

	private class ConnectorColorChangeListener extends ControlGroupChangeListener {
		public ConnectorColorChangeListener(final BooleanProperty mSet, final IPropertyControl mProperty) {
			super(mSet, mProperty);
		}

		@Override
		void applyValue(final boolean enabled, final PropertyChangeEvent evt) {
            final MLinkController linkController 
            = (MLinkController)LinkController.getController();
        linkController.setConnectorColor(connector, enabled ? 
                Optional.of(color.getColorValue()) : Optional.empty());
		}

		@Override
		void updateValue() {
		    final LinkController linkController = LinkController.getController();
		    final Optional<Color> ownColor = connector.getColor();
		    final Color viewColor = linkController.getColor(connector);
		    setColor.setValue(ownColor.isPresent());
		    color.setColorValue(viewColor);
		}
	}
    @Override
    public void updateValue(ConnectorModel connector) {
        this.connector = connector;
        propertyChangeListener.update();
    }

    public void addControlGroup(DefaultFormBuilder formBuilder) {
		setColor = new BooleanProperty(ControlGroup.SET_RESOURCE);
		color = new ColorProperty(ConnectorColorControlGroup.CONNECTOR_COLOR, 
		        ResourceController.getResourceController().getProperty(LinkController.RESOURCES_LINK_COLOR));
		propertyChangeListener = new ConnectorColorChangeListener(setColor, color);
		setColor.addPropertyChangeListener(propertyChangeListener);
		color.addPropertyChangeListener(propertyChangeListener);
		setColor.appendToForm(formBuilder);;
		color.appendToForm(formBuilder);;
	}
	
}