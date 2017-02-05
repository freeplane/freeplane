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
package org.freeplane.view.swing.ui;

/**
 * @author Dimitry Polivaev
 * Oct 1, 2011
 */
import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.net.URI;

import javax.swing.SwingUtilities;

import org.freeplane.core.ui.AMouseListener;
import org.freeplane.core.util.Compat;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.mode.Controller;
import org.freeplane.view.swing.map.NodeView;
import org.freeplane.view.swing.map.ZoomableLabel;

public class LinkNavigatorMouseListener extends AMouseListener {
	public LinkNavigatorMouseListener() {
		super();
	}

	public void mouseMoved(final MouseEvent e) {
    	final ZoomableLabel node = ((ZoomableLabel) e.getComponent());
    	String link = node.getLink(e.getPoint());
    	boolean followLink = link != null;
    	Controller currentController = Controller.getCurrentController();
        final int requiredCursor;
        if(followLink){
    		currentController.getViewController().out(link);
    		requiredCursor = Cursor.HAND_CURSOR;
        }
        else{
        	requiredCursor = Cursor.DEFAULT_CURSOR;
        }
        if (node.getCursor().getType() != requiredCursor) {
        	node.setCursor(requiredCursor != Cursor.DEFAULT_CURSOR ? new Cursor(requiredCursor) : null);
        }
    }

	public void mouseClicked(final MouseEvent e) {
    	final ZoomableLabel component = (ZoomableLabel) e.getComponent();
    	if(e.getClickCount() == 1 && e.getButton() == 1)
    		if(Compat.isPlainEvent(e)){
    			final String link = component.getLink(e.getPoint());
    			if(link != null){
    				if (link != null) {
    					try {
    						NodeView nodeView = (NodeView) SwingUtilities.getAncestorOfClass(NodeView.class, component);
    						LinkController.getController().loadURI(nodeView.getModel(), new URI(link));
    					} catch (Exception ex) {
    						LogUtils.warn(ex);
    					}
    				}
    				return;
    			}
    		}
    }
}
