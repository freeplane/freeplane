/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Dimitry Polivaev
 *
 *  This file author is Dimitry Polivaev
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
package org.freeplane.view.swing.ui;

import java.awt.event.MouseEvent;

import javax.swing.JPopupMenu;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.modecontroller.IMapSelection;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.ui.ControllerPopupMenuListener;
import org.freeplane.core.ui.IMapMouseReceiver;
import org.freeplane.core.ui.IMouseListener;
import org.freeplane.features.common.link.LinkController;
import org.freeplane.view.swing.map.MapView;

/**
 * @author Dimitry Polivaev
 * 06.01.2009
 */
/**
 * The MouseListener which belongs to MapView
 */
public class DefaultMapMouseListener implements IMouseListener {
	final private Controller controller;
	private final IMapMouseReceiver mReceiver;

	public DefaultMapMouseListener(final Controller controller, final IMapMouseReceiver mReceiver) {
		this.controller = controller;
		this.mReceiver = mReceiver;
	}

	private void handlePopup(final MouseEvent e) {
		if (e.isPopupTrigger()) {
			JPopupMenu popup = null;
			final MapView mapView = (MapView) controller.getViewController().getMapView();
			final java.lang.Object obj = mapView.detectCollision(e.getPoint());
			final ModeController modeController = controller.getModeController();
			final JPopupMenu popupForModel = LinkController.getController(modeController).getPopupForModel(obj);
			if (popupForModel != null) {
				final ControllerPopupMenuListener popupListener = new ControllerPopupMenuListener(modeController);
				popupForModel.addPopupMenuListener(popupListener);
				popup = popupForModel;
			}
			else {
				popup = modeController.getUserInputListenerFactory().getMapPopup();
			}
			popup.show(e.getComponent(), e.getX(), e.getY());
			popup.setVisible(true);
		}
	}

	public void mouseClicked(final MouseEvent e) {
		final IMapSelection selection = controller.getSelection();
		selection.selectAsTheOnlyOneSelected(selection.getSelected());
	}

	public void mouseDragged(final MouseEvent e) {
		if (mReceiver != null) {
			mReceiver.mouseDragged(e);
		}
	}

	public void mouseEntered(final MouseEvent e) {
	}

	public void mouseExited(final MouseEvent e) {
	}

	public void mouseMoved(final MouseEvent e) {
	}

	public void mousePressed(final MouseEvent e) {
		if (e.isPopupTrigger()) {
			handlePopup(e);
		}
		else if (mReceiver != null) {
			mReceiver.mousePressed(e);
		}
		e.consume();
	}

	public void mouseReleased(final MouseEvent e) {
		if (mReceiver != null) {
			mReceiver.mouseReleased(e);
		}
		handlePopup(e);
		e.consume();
		((MapView) controller.getViewController().getMapView()).setMoveCursor(false);
	}
}
