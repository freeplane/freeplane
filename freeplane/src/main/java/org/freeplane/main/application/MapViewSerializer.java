/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2013 Dimitry
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
package org.freeplane.main.application;

import java.awt.Component;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JPanel;

import net.infonode.docking.View;
import net.infonode.docking.ViewSerializer;

import org.freeplane.features.map.MapController;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.mindmapmode.DocuMapAttribute;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.view.swing.map.MapView;
import org.freeplane.view.swing.map.MapViewScrollPane;

/**
 * @author Dimitry Polivaev
 * 29.04.2013
 */
class MapViewSerializer implements ViewSerializer {
    private Collection<View> viewsToBeRemoved = new ArrayList<View>();

	public void writeView(View view, ObjectOutputStream out) throws IOException {
    	if(view.isDisplayable()) {
    		Component component = MapViewDockingWindows.getContainedMapView(view);
    		if (component instanceof MapView) {
    			MapView mapView = (MapView) component;
    			if(mapView.getModeController().getModeName().equals(MModeController.MODENAME) 
    					&& ! mapView.getModel().containsExtension(DocuMapAttribute.class)){
    				out.writeBoolean(true);
    				out.writeUTF(mapView.getModeController().getModeName());
    				out.writeObject(mapView.getModel().getURL());
    				return;
    			}
            }
    	}
    	out.writeBoolean(false);
    }

    public View readView(ObjectInputStream in) throws IOException {
    	try {
    		if (in.readBoolean()){
    			String modeName = in.readUTF();
    			URL mapUrl = (URL) in.readObject();
    			if(mapUrl == null)
    				return newViewToBeRemoved();
    			Controller controller = Controller.getCurrentController();
    			controller.selectMode(modeName);
    			ModeController modeController = Controller.getCurrentModeController();
    			MapController mapController = modeController.getMapController();
    			mapController.openMap(mapUrl);
    			Component mapViewComponent = controller.getMapViewManager().getMapViewComponent();
    			if(mapViewComponent.getParent() == null) {
                    final Component pNewMap = mapViewComponent;
					return newDockedView(pNewMap, pNewMap.getName());
                }
                else
    				return newViewToBeRemoved();
    		}
            return newViewToBeRemoved();
        }
        catch (Exception e) {
        	return newViewToBeRemoved();
        }
    }
    
	private View newViewToBeRemoved() {
	    View view = new View("", null, new JPanel());
	    viewsToBeRemoved.add(view);
		return view;
    }

	protected View newDockedView(final Component pNewMap, final String title) {
		if(pNewMap.getParent() != null)
			return null;
		MapViewScrollPane mapViewScrollPane = new MapViewScrollPane();
		mapViewScrollPane.getViewport().setView(pNewMap);
		@SuppressWarnings("serial")
        final View viewFrame = new ConnectedToMenuView(title, null, mapViewScrollPane);
	    return viewFrame;
	}

	public void removeDummyViews() {
	    for(View view : viewsToBeRemoved)
	    	view.close();
	    viewsToBeRemoved.clear();
    }
	
	

    
}