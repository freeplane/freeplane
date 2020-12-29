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
import org.freeplane.features.link.ConnectorModel;
import org.freeplane.features.link.ConnectorModel.Shape;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.link.mindmapmode.MLinkController;
import org.freeplane.features.styles.mindmapmode.styleeditorpanel.EnumToStringMapper;

import com.jgoodies.forms.builder.DefaultFormBuilder;

public class ConnectorShapeControlGroup implements ControlGroup {
	private BooleanProperty mSetConnectorShape;
	private ComboProperty mConnectorShape;
	private ConnectorShapeChangeListener propertyChangeListener;
	
	private ConnectorModel connector;
	
	private class ConnectorShapeChangeListener extends ControlGroupChangeListener {
		public ConnectorShapeChangeListener(final BooleanProperty mSet, final IPropertyControl mProperty) {
			super(mSet, mProperty);
		}

		@Override
		void applyValue(final boolean enabled, 
				final PropertyChangeEvent evt) {
            final MLinkController linkController 
            = (MLinkController) LinkController.getController();
            linkController.setShape(connector, enabled ? 
                    Optional.of(Shape.valueOf(mConnectorShape.getValue())) : Optional.empty());

		}
		
		@Override
		void updateValue() {
            final LinkController linkController = LinkController.getController();
				final Optional<Shape> ownShape = connector.getShape();
				final Shape viewShape = linkController.getShape(connector);
				mSetConnectorShape.setValue(ownShape.isPresent());
				mConnectorShape.setValue(viewShape.name());
		}
	}
	
    @Override
    public void updateValue(ConnectorModel connector) {
        this.connector = connector;
        propertyChangeListener.update();
    }
    
	@Override
	public void addControlGroup(DefaultFormBuilder formBuilder) {
		mSetConnectorShape = new BooleanProperty(ControlGroup.SET_RESOURCE);
		mConnectorShape = ComboProperty.of("connector_shapes", Shape.class);
		mConnectorShape.setNameAsLabelAndToolTip();
		propertyChangeListener = new ConnectorShapeChangeListener(mSetConnectorShape, mConnectorShape);
		mSetConnectorShape.addPropertyChangeListener(propertyChangeListener);
		mConnectorShape.addPropertyChangeListener(propertyChangeListener);
		mSetConnectorShape.appendToForm(formBuilder);
		mConnectorShape.appendToForm(formBuilder);
	}
}
