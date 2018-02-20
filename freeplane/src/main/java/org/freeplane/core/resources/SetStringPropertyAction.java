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
package org.freeplane.core.resources;

import java.awt.event.ActionEvent;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.SelectableAction;
import org.freeplane.core.util.TextUtils;

/**
 * @author Dimitry Polivaev
 * Mar 2, 2011
 */
@SuppressWarnings("serial")
@SelectableAction(checkOnPopup = true)
public class SetStringPropertyAction extends AFreeplaneAction{

	private String propertyName;
	private String propertyValue;
	private String property;
	public SetStringPropertyAction(String property) {
	    super("SetBooleanPropertyAction." + property, 
	    	TextUtils.getRawText("OptionPanel." + property),
	    	null);
		this.property = property;
	    int separator = property.indexOf('.');
	    this.propertyName = property.substring(0, separator);
	    this.propertyValue = property.substring(separator + 1);
	    setIcon(property + ".icon");
	    setTooltip(getTooltipKey());
    }

	public void actionPerformed(ActionEvent e) {
		ResourceController.getResourceController().setProperty(propertyName, propertyValue);
	    
    }
	
	@Override
	public String getTextKey() {
		return "OptionPanel." + property;
	}
	
	@Override
	public String getTooltipKey() {
		return getTextKey() + ".tooltip";
	}
	
	@Override
	public void setSelected() {
		setSelected(isPropertySelected());
	}

	public boolean isPropertySelected() {
	    return propertyValue.equals(ResourceController.getResourceController().getProperty(propertyName, null));
    }
}
