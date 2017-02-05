/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2011 dimitry
 *
 *  This file author is dimitry
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
package org.freeplane.features.styles;

import java.awt.event.ActionEvent;

import javax.swing.JComponent;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.EnabledAction;
import org.freeplane.core.ui.SelectableAction;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.mode.Controller;

/**
 * @author Dimitry Polivaev
 * Mar 2, 2017
 */
@SuppressWarnings("serial")
@SelectableAction(checkOnPopup = true)
@EnabledAction(checkOnNodeChange = true)
public class SetBooleanMapViewPropertyAction extends AFreeplaneAction{

	private String propertyName;
	public SetBooleanMapViewPropertyAction(String propertyName) {
	    super("SetBooleanMapViewPropertyAction." + propertyName, 
	    	TextUtils.getRawText("OptionPanel." + propertyName),
	    	null);
	    this.propertyName = propertyName;
	    setTooltip(getTooltipKey());
    }

	public void actionPerformed(ActionEvent e) {
		final JComponent mapViewComponent = getMapViewComponent();
		if(mapViewComponent != null) {
			final Boolean value = Boolean.TRUE.equals(mapViewComponent.getClientProperty(propertyName));
			boolean newValue = ! value.booleanValue();
			mapViewComponent.putClientProperty(propertyName, newValue);
			setSelected(newValue);
		}
    }

	private JComponent getMapViewComponent() {
		final JComponent mapViewComponent = (JComponent) Controller.getCurrentController().getMapViewManager().getMapViewComponent();
		return mapViewComponent;
	}

	@Override
	public String getTextKey() {
		return "OptionPanel." + propertyName;
	}

	@Override
	public String getTooltipKey() {
		return getTextKey() + ".tooltip";
	}
	
	@Override
	public void setSelected() {
		try {
			final JComponent mapViewComponent = getMapViewComponent();
			if(mapViewComponent != null) {
				final Boolean value = Boolean.TRUE.equals(mapViewComponent.getClientProperty(propertyName));
				setSelected(value);
				return;
			}
		}
		catch (Exception e) {
		}
		setSelected(false);
	}
	
	public void setEnabled() {
		setEnabled(getMapViewComponent() != null);
	}

}
