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

import javax.swing.JComponent;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.SelectableAction;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.styles.MapViewLayout;
import org.freeplane.features.ui.IMapViewManager;

/**
 * @author Dimitry Polivaev
 * 29.08.2009
 */
@SelectableAction(checkOnPopup = true)
public class ViewLayoutTypeAction extends AFreeplaneAction {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	/**
	 *
	 */
	private final MapViewLayout layoutType;

	public ViewLayoutTypeAction(final MapViewLayout layoutType) {
		super("ViewLayoutTypeAction." + layoutType.toString());
		this.layoutType = layoutType;
	}

	public void actionPerformed(final ActionEvent e) {
	    boolean wasSelected = isSelected();
	    MapViewLayout newLayoutType = wasSelected ? MapViewLayout.MAP : layoutType;
		IMapViewManager mapViewManager = Controller.getCurrentController().getMapViewManager();
        final JComponent map = mapViewManager.getMapViewComponent();
        mapViewManager.setLayout(map, newLayoutType);
		setSelected(! wasSelected);
	}

	@Override
	public void setSelected() {
		final MapView map = (MapView)  Controller.getCurrentController().getMapViewManager().getMapViewComponent();
		setSelected(map != null && map.getLayoutType() == layoutType);
	}
}
