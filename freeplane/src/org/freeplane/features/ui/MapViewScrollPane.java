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
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.Timer;

import org.freeplane.core.resources.ResourceController;
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
			if(timer != null) {
				timer.stop();
				timer = null;
			}
			final Point viewPosition = getViewPosition();
	        int dx = p.x - viewPosition.x;
	        int dy = p.y - viewPosition.y;
	        int slowDx = calcScrollIncrement(dx);
	        int slowDy = calcScrollIncrement(dy);
	        viewPosition.translate(slowDx, slowDy);
	        super.setViewPosition(viewPosition);
	        if(slowDx == dx && slowDy == dy)
	            return;
	        timer = new Timer(20, new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					timer = null;
					MapViewPort.this.slowSetViewPosition(p);
				}
			});
	        timer.setRepeats(false);
	        timer.start();
        }

		private int calcScrollIncrement(int dx) {
			int v = ResourceController.getResourceController().getIntProperty("scrolling_speed");
			final int slowDX = (int) (v  / 5.0 *  Math.sqrt(Math.abs(dx)));
			if (Math.abs(dx) > 2 && slowDX < Math.abs(dx)) {
	            dx = slowDX * Integer.signum(dx);
            }
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
