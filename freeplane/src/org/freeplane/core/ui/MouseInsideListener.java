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
package org.freeplane.core.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

/**
 * @author Dimitry Polivaev
 * 01.03.2013
 */
public class MouseInsideListener extends MouseAdapter implements MouseMotionListener{
	private boolean mouseInside = false;
	private final Component c;
	public boolean isMouseInside() {
    	return mouseInside;
    }
	public MouseInsideListener(Component c){
		this.c = c;
		connect();
	}
	protected void connect() {
	    recursivelyAddMouseInsideListener(c);
    }
	@Override
    public void mouseEntered(MouseEvent e) {
		mouseInside = true;
    }

	@Override
    public void mouseExited(MouseEvent e) {
		mouseInside = false;        }

	@Override
    public void mouseMoved(MouseEvent e) {
		mouseInside = true;        
	}

	private void recursivelyAddMouseInsideListener(Component c) {
        c.addMouseListener(this);
        c.addMouseMotionListener((MouseMotionListener) this);
        if(c instanceof Container){
        	Container container = (Container) c;
        	for(Component childComponent : container.getComponents())
        		recursivelyAddMouseInsideListener(childComponent);
        }
    }
	private void recursivelyRemoveMouseInsideListener(Component c) {
        c.removeMouseListener(this);
        c.removeMouseMotionListener((MouseMotionListener) this);
        if(c instanceof Container){
        	Container container = (Container) c;
        	for(Component childComponent : container.getComponents())
        		recursivelyRemoveMouseInsideListener(childComponent);
        }
    }
	public void disconnect() {
		recursivelyRemoveMouseInsideListener(c);
    }
}