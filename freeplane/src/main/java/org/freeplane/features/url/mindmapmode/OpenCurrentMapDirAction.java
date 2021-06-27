/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is modified by Dimitry Polivaev in 2008.
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
package org.freeplane.features.url.mindmapmode;

import java.awt.Component;
import java.awt.event.ActionEvent;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.util.Hyperlink;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.map.IMapChangeListener;
import org.freeplane.features.map.MapChangeEvent;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.ui.IMapViewChangeListener;
import org.freeplane.view.swing.map.MapView;

class OpenCurrentMapDirAction extends AFreeplaneAction implements IMapViewChangeListener, IMapChangeListener {
    private static final long serialVersionUID = 1L;

    public OpenCurrentMapDirAction() {
        super("OpenCurrentMapDirAction");
        Controller.getCurrentController().getMapViewManager().addMapViewChangeListener(this);
        Controller.getCurrentModeController().getMapController().addMapChangeListener(this);
    }

    public void actionPerformed(final ActionEvent e) {
        final Controller controller = Controller.getCurrentController();
        LinkController.getController().loadHyperlink(new Hyperlink(controller.getMap().getFile().getParentFile().toURI()));
    }

    @Override
    public void mapChanged(MapChangeEvent event) {
        final MapModel map = event.getMap();
        setEnabled(map != null && map.getFile() != null);
    }

    public void afterViewChange(Component oldView, Component newView) {
        final MapView mapView = (MapView) newView;
        setEnabled(mapView != null && mapView.getModel().getFile() != null);
    }
}
