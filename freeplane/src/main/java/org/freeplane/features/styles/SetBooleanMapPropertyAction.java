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

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.EnabledAction;
import org.freeplane.core.ui.SelectableAction;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;

/**
 * @author Dimitry Polivaev
 * Mar 2, 2011
 */
@SuppressWarnings("serial")
@SelectableAction(checkOnPopup = true)
@EnabledAction(checkOnNodeChange = true)
public class SetBooleanMapPropertyAction extends AFreeplaneAction{

	private String propertyName;
	public SetBooleanMapPropertyAction(String propertyName) {
	    super("SetBooleanMapPropertyAction." + propertyName, 
	    	TextUtils.getRawText("OptionPanel." + propertyName),
	    	null);
	    this.propertyName = propertyName;
	    setTooltip(getTooltipKey());
    }

	public void actionPerformed(ActionEvent e) {
		final Controller controller = Controller.getCurrentController();
		final NodeModel node = controller.getSelection().getSelected();
		final ModeController modeController = controller.getModeController();
		final MapStyle mapStyleController = MapStyle.getController(modeController);
		final MapModel map = node.getMap();
		final String value = mapStyleController.getPropertySetDefault(map, propertyName);
		boolean set = ! Boolean.parseBoolean(value);
		mapStyleController.setProperty(map, propertyName, Boolean.toString(set));
		setSelected(set);
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
			final Controller controller = Controller.getCurrentController();
			final NodeModel node = controller.getSelection().getSelected();
			final ModeController modeController = controller.getModeController();
			final MapStyle mapStyleController = MapStyle.getController(modeController);
			final String value = mapStyleController.getPropertySetDefault(node.getMap(), propertyName);
			boolean isSet = Boolean.parseBoolean(value);
			setSelected(isSet);
		}
		catch (Exception e) {
			setSelected(false);
		}
	}
	
	public void setEnabled() {
 		final Controller controller = Controller.getCurrentController();
		setEnabled(controller.getSelection() != null);
	}

}
