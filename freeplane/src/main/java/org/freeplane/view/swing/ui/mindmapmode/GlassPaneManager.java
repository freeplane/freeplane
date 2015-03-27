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
package org.freeplane.view.swing.ui.mindmapmode;

import java.awt.Component;

import javax.swing.JRootPane;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import org.freeplane.core.ui.DelayedMouseListener;
import org.freeplane.core.ui.IMouseListener;


/**
 * @author Dimitry Polivaev
 * Mar 3, 2011
 */
public class GlassPaneManager implements AncestorListener {
    private final IMouseListener mouseListener;
    private final JRootPane rootPane;
    public GlassPaneManager(JRootPane jframe, INodeSelector nodeSelector) {
	    this.mouseListener = new DelayedMouseListener(new GlassPaneNodeSelector(nodeSelector), 2, 1);
	    this.rootPane = jframe;
    }

    public void ancestorRemoved(AncestorEvent event) {
    	final Component glassPane = rootPane.getRootPane().getGlassPane();
    	glassPane.removeMouseListener(mouseListener);
    	glassPane.removeMouseMotionListener(mouseListener);
    	glassPane.setVisible(false);
    	SwingUtilities.getWindowAncestor(rootPane).setFocusableWindowState(true);
    }

    public void ancestorMoved(AncestorEvent event) {
    }

    public void ancestorAdded(AncestorEvent event) {
    	final Component glassPane = rootPane.getRootPane().getGlassPane();
    	glassPane.addMouseListener(mouseListener);
    	glassPane.addMouseMotionListener(mouseListener);
    	glassPane.setVisible(true);
    	SwingUtilities.getWindowAncestor(rootPane).setFocusableWindowState(false);
    }
}