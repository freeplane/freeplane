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
import org.freeplane.features.DashVariant;
import org.freeplane.features.link.ConnectorModel;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.link.mindmapmode.MLinkController;

import com.jgoodies.forms.builder.DefaultFormBuilder;

public class ConnectorDashControlGroup implements ControlGroup {

	private BooleanProperty mSetConnectorDash;
	private ComboProperty mConnectorDash;
	private ConnectorDashChangeListener propertyChangeListener;
	private ConnectorModel connector;
	
	private class ConnectorDashChangeListener extends ControlGroupChangeListener {
		public ConnectorDashChangeListener(final BooleanProperty mSet, final IPropertyControl mProperty) {
			super(mSet, mProperty);
		}

		@Override
		void applyValue(final boolean enabled, final PropertyChangeEvent evt) {
		    final MLinkController linkController 
		    = (MLinkController) LinkController.getController();
		    linkController.setConnectorDashArray(connector, enabled ? 
		            Optional.of(DashVariant.valueOf(mConnectorDash.getValue()).variant) : Optional.empty());
		}

		@Override
		void updateValue() {
		    final LinkController linkController = LinkController.getController();
		    final Optional<int[]> ownDash = connector.getDash();
		    final Optional<DashVariant> viewDash = DashVariant.of(linkController.getDashArray(connector));
		    mSetConnectorDash.setValue(ownDash.isPresent());
		    viewDash.map(DashVariant::name).ifPresent(mConnectorDash::setValue);
		}
	}

	@Override
	public void updateValue(ConnectorModel connector) {
	    this.connector = connector;
	        propertyChangeListener.update();
	    }

	@Override
	public void addControlGroup(DefaultFormBuilder formBuilder) {
		mSetConnectorDash = new BooleanProperty(ControlGroup.SET_RESOURCE);
		mConnectorDash = ComboProperty.of("connector_dash", DashVariant.class);
		propertyChangeListener = new ConnectorDashChangeListener(mSetConnectorDash, mConnectorDash);
		mSetConnectorDash.addPropertyChangeListener(propertyChangeListener);
		mConnectorDash.addPropertyChangeListener(propertyChangeListener);
		mSetConnectorDash.appendToForm(formBuilder);
		mConnectorDash.appendToForm(formBuilder);
	}
}
