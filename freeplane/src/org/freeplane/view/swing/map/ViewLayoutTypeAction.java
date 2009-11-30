/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2009 Dimitry
 *
 *  This file author is Dimitry
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
package org.freeplane.view.swing.map;

import java.awt.event.ActionEvent;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.extension.IExtension;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.model.MapModel;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.SelectableAction;
import org.freeplane.features.common.addins.mapstyle.MapStyle;
import org.freeplane.features.common.addins.mapstyle.MapViewLayout;


/**
 * @author Dimitry Polivaev
 * 29.08.2009
 */
@SelectableAction(checkOnPopup=true)
public class ViewLayoutTypeAction extends AFreeplaneAction{

	/**
     * 
     */
    private static final long serialVersionUID = 1L;
	/**
     * 
     */
	private MapViewLayout layoutType;

	public ViewLayoutTypeAction(Controller controller, MapViewLayout layoutType) {
	    super("ViewLayoutTypeAction." + layoutType.toString(), controller);
	    this.layoutType = layoutType;
    }

	public void actionPerformed(ActionEvent e) {
		MapView map = (MapView) getController().getViewController().getMapView();
		if(isSelected()){
			map.setLayoutType(MapViewLayout.MAP);
			setSelected(false);
		}
		else{
			map.setLayoutType(this.layoutType);
			setSelected(true);
		}
		MapStyle mapStyle = (MapStyle) map.getModeController().getExtension(MapStyle.class);
		mapStyle.setMapViewLayout(map.getModel(), map.getLayoutType());
		map.anchorToSelected(map.getSelected(), 0.5f, 0.5f);
		map.getRoot().updateAll();
    }

	@Override
    public void setSelected() {
		MapView map = (MapView) getController().getViewController().getMapView();
		setSelected(map != null && map.getLayoutType() == layoutType);
    }


}
