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
package org.freeplane.view.swing.ui;

import java.awt.Component;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.SwingUtilities;

import org.freeplane.core.ui.components.UITools;

/**
 * @author Dimitry Polivaev
 * 21.01.2013
 */
public class MovedMouseEventFilter extends WindowAdapter {

	private Window trackedWindow = null;
	private Point mousePositionAfterFocusGained = null;

	public boolean isRelevant(MouseEvent e) {
		if(mousePositionAfterFocusGained == null)
			return true;
		final Component component = e.getComponent();
		final Window windowToTrack = SwingUtilities.getWindowAncestor(component);
		if(! windowToTrack.equals(trackedWindow))
			return true;
		final Point eventPoint = e.getPoint();
		UITools.convertPointToAncestor(component, eventPoint, trackedWindow);
		final boolean hasMoved = !eventPoint.equals(mousePositionAfterFocusGained);
		if(hasMoved)
			mousePositionAfterFocusGained = null;
		return hasMoved;
    }

	@Override
    public void windowGainedFocus(WindowEvent e) {
		try {
	        mousePositionAfterFocusGained = trackedWindow.getMousePosition();
        }
		// Work around for mac os java bug
        catch (ClassCastException | NullPointerException ex) {
        }
    }

	public void trackWindowForComponent(Component c){
		final Window windowToTrack = SwingUtilities.getWindowAncestor(c);
		if(windowToTrack.equals(trackedWindow))
			return;
		if(trackedWindow != null)
			trackedWindow.removeWindowFocusListener(this);
		trackedWindow = windowToTrack;
		trackedWindow.addWindowFocusListener(this);
		mousePositionAfterFocusGained = null;
	}

}
