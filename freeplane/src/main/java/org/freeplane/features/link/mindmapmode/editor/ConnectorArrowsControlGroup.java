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

import java.beans.PropertyChangeEvent;
import java.util.Optional;

import org.freeplane.core.resources.components.BooleanProperty;
import org.freeplane.core.resources.components.ComboProperty;
import org.freeplane.core.resources.components.IPropertyControl;
import org.freeplane.features.link.ArrowType;
import org.freeplane.features.link.ConnectorArrows;
import org.freeplane.features.link.ConnectorModel;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.link.mindmapmode.MLinkController;

import com.jgoodies.forms.builder.DefaultFormBuilder;

public class ConnectorArrowsControlGroup implements ControlGroup {
	private BooleanProperty mSetConnectorArrows;
	private ComboProperty mConnectorArrows;
	private ConnectorArrowsChangeListener propertyChangeListener;
	
	private ConnectorModel connector;
	
	private class ConnectorArrowsChangeListener extends ControlGroupChangeListener {
		public ConnectorArrowsChangeListener(final BooleanProperty mSet, final IPropertyControl mProperty) {
			super(mSet, mProperty);
		}

		@Override
		void applyValue(final boolean enabled, 
				final PropertyChangeEvent evt) {
            final MLinkController linkController 
            = (MLinkController) LinkController.getController();
            ConnectorArrows arrows = ConnectorArrows.valueOf(mConnectorArrows.getValue());
            linkController.changeArrowsOfArrowLink(connector, 
                    enabled ? Optional.of(arrows.start) : Optional.empty(),
                    enabled ? Optional.of(arrows.end) : Optional.empty());

		}

		@Override
		void updateValue() {
		    final LinkController linkController = LinkController.getController();
		    final Optional<ArrowType> ownStart = connector.getStartArrow();
		    final Optional<ArrowType> ownEnd = connector.getEndArrow();
		    final Optional<ConnectorArrows> viewArrows = ConnectorArrows.of(
		            linkController.getStartArrow(connector),
		            linkController.getEndArrow(connector));
		    mSetConnectorArrows.setValue(ownStart.isPresent() || ownEnd.isPresent());
		    viewArrows.map(ConnectorArrows::name).ifPresent(mConnectorArrows::setValue);
		}
	}

	@Override
	public void updateValue(ConnectorModel connector) {
        this.connector = connector;
        propertyChangeListener.update();
    }
    
	@Override
	public void addControlGroup(DefaultFormBuilder formBuilder) {
		mSetConnectorArrows = new BooleanProperty(ControlGroup.SET_RESOURCE);
		mConnectorArrows = ComboProperty.of("connector_arrows", ConnectorArrows.class);
		propertyChangeListener = new ConnectorArrowsChangeListener(mSetConnectorArrows, mConnectorArrows);
		mSetConnectorArrows.addPropertyChangeListener(propertyChangeListener);
		mConnectorArrows.addPropertyChangeListener(propertyChangeListener);
		mSetConnectorArrows.appendToForm(formBuilder);
		mConnectorArrows.appendToForm(formBuilder);
	}
}
