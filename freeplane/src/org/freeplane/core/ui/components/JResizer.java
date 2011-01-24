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
package org.freeplane.core.ui.components;

import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

/**
 * @author Dimitry Polivaev
 * Jan 24, 2011
 */
@SuppressWarnings("serial")
public class JResizer extends JComponent{
	public enum Direction {RIGHT, LEFT, UP, DOWN}
	
	public JResizer(final Direction d) {
		setOpaque(true);
		final int w;
		final int h;
		if(d.equals(Direction.RIGHT)){
			w = 2;
			h = 0;
			setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
		}
		else if(d.equals(Direction.LEFT)){
			h = 2;
			w = 0;
			setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
		}
		else if(d.equals(Direction.UP)){
			h = 0;
			w = 2;
			setCursor(Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR));
		}
		else /*Direction.DOWN*/ {
			h = 0;
			w = 2;
			setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
		}
		
		setPreferredSize(new Dimension(w, h));
		addMouseMotionListener(new MouseAdapter() {
			private Point point;
			private int index;
			@Override
            public void mousePressed(MouseEvent e) {
				point = e.getPoint();
				SwingUtilities.convertPointToScreen(point, e.getComponent());
				index = getIndex();
            }

			private int getIndex() {
				final Container parent = getParent();
				for(int i = 0; i < parent.getComponentCount(); i++ ){
					if(JResizer.this.equals(parent.getComponent(i))){
						if(d.equals(Direction.RIGHT)){
							return i + 1;
						}
						else if(d.equals(Direction.LEFT)){
							return i - 1;
						}
						else if(d.equals(Direction.UP)){
							return i - 1;
						}
						else if(d.equals(Direction.DOWN)){
							return i + 1;
						}
					}
				}
				return -1;
            }

			@Override
            public void mouseReleased(MouseEvent e) {
				point = null;
            }

			@Override
            public void mouseExited(MouseEvent e) {
				point = null;
            }

			@Override
            public void mouseDragged(MouseEvent e) {
				final Point point2 = e.getPoint();
				SwingUtilities.convertPointToScreen(point2, e.getComponent());
				if(point != null){
					final JComponent parent = (JComponent) getParent();
					final Component resizedComponent = parent.getComponent(index);
					final Dimension size = new Dimension(resizedComponent.getPreferredSize());
					if(d.equals(Direction.RIGHT)){
						size.width -= (point2.x - point.x);
					}
					else if(d.equals(Direction.LEFT)){
						size.width += (point2.x - point.x);
					}
					else if(d.equals(Direction.UP)){
						size.height += (point2.x - point.x);
					}
					else if(d.equals(Direction.DOWN)){
						size.height -= (point2.x - point.x);
					}
					resizedComponent.setPreferredSize(size);
					parent.revalidate();
					parent.repaint();
				}
				else{
					index = getIndex();
				}
				point = point2;
            }
			
		});
    }
	
}
