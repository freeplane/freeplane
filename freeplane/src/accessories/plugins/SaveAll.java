/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
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
package accessories.plugins;

import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import javax.swing.JOptionPane;

import org.freeplane.controller.Controller;
import org.freeplane.controller.Freeplane;
import org.freeplane.map.tree.view.MapView;
import org.freeplane.modes.mindmapmode.MModeController;

import deprecated.freemind.extensions.ModeControllerHookAdapter;

/**
 * @author foltin
 */
public class SaveAll extends ModeControllerHookAdapter {
	/**
	 *
	 */
	public SaveAll() {
		super();
	}

	/**
	 */
	private Map getMapViews() {
		return Freeplane.getController().getMapViewManager().getMapViews();
	}

	@Override
	public void startup() {
		super.startup();
		final Controller mainController = Freeplane.getController();
		final MapView initialMapView = mainController.getMapView();
		final Map mapViews = getMapViews();
		final Vector v = new Vector();
		v.addAll(mapViews.values());
		for (final Iterator iter = v.iterator(); iter.hasNext();) {
			final MapView mapView = (MapView) iter.next();
			mainController.getMapViewManager().changeToMapView(
			    mapView.getName());
			if (!((MModeController) mapView.getModeController()).save()) {
				JOptionPane
				    .showMessageDialog(
				        Freeplane.getController().getViewController()
				            .getContentPane(),
				        "FreeMind",
				        getResourceString("accessories/plugins/SaveAll.properties_save_all_cancelled"),
				        JOptionPane.ERROR_MESSAGE);
				return;
			}
		}
		mainController.getMapViewManager().changeToMapView(
		    initialMapView.toString());
	}
}
