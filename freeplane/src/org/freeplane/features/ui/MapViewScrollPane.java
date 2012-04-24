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
package org.freeplane.features.ui;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.Timer;

import org.freeplane.core.ui.components.UITools;

/**
 * @author Dimitry Polivaev
 * 10.01.2009
 */
class MapViewScrollPane extends JScrollPane {
	@SuppressWarnings("serial")
    static class MapViewPort extends JViewport{

		private Timer timer;

		@Override
        public void setViewPosition(Point p) {
			boolean scrollingToVisible = Boolean.TRUE.equals(getClientProperty(ViewController.SLOW_SCROLLING)) ;
			if(scrollingToVisible){
				putClientProperty(ViewController.SLOW_SCROLLING, null);
				slowSetViewPosition(p);
			}
			else
				super.setViewPosition(p);
        }

		private void slowSetViewPosition(final Point p) {
			final Point viewPosition = getViewPosition();
	        int dx = p.x - viewPosition.x;
	        int dy = p.y - viewPosition.y;
	        if(Math.abs(dx) <= 1 && Math.abs(dy) <= 1){
	        	super.setViewPosition(p);
	        	return;
	        }
	        dx = calcScrollIncrement(dx);
	        dy = calcScrollIncrement(dy);
	        viewPosition.translate(dx, dy);
	        super.setViewPosition(viewPosition);
	        if(timer != null)
	        	timer.stop();
	        timer = new Timer(1, new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					timer = null;
					MapViewPort.this.slowSetViewPosition(p);
				}
			});
	        timer.setRepeats(false);
	        timer.start();
        }

		private int calcScrollIncrement(int dx) {
			int i = 5;
			if (Math.abs(dx) > i)
				dx /= i;
			else
				dx = Integer.signum(dx);
			return dx;
        }
		
		
		
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public MapViewScrollPane() {
		super();
		setViewport(new MapViewPort());
		UITools.setScrollbarIncrement(this);
		UITools.addScrollbarIncrementPropertyListener(this);
	}

	@Override
	protected void validateTree() {
		final Component view = getViewport().getView();
		if (view != null) {
			view.validate();
		}
		super.validateTree();
	}
}
