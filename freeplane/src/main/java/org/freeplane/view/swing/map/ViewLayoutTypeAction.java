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

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.SelectableAction;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.styles.MapStyle;
import org.freeplane.features.styles.MapViewLayout;

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
		final MapView map = (MapView) Controller.getCurrentController().getMapViewManager().getMapViewComponent();
		if (isSelected()) {
			map.setLayoutType(MapViewLayout.MAP);
			setSelected(false);
		}
		else {
			map.setLayoutType(layoutType);
			setSelected(true);
		}
		final MapStyle mapStyle = (MapStyle) map.getModeController().getExtension(MapStyle.class);
		mapStyle.setMapViewLayout(map.getModel(), map.getLayoutType());
		map.getMapSelection().preserveNodeLocationOnScreen(map.getSelected().getModel(), 0.5f, 0.5f);
		final NodeView root = map.getRoot();
		invalidate(root);
		root.revalidate();
	}

	private void invalidate(final Component c) {
		c.invalidate();
		if(! (c instanceof Container))
			return;
		Container c2 = (Container) c;
		for(int i = 0; i < c2.getComponentCount(); i++)
			invalidate(c2.getComponent(i));
    }

	@Override
	public void setSelected() {
		final MapView map = (MapView)  Controller.getCurrentController().getMapViewManager().getMapViewComponent();
		setSelected(map != null && map.getLayoutType() == layoutType);
	}
}
