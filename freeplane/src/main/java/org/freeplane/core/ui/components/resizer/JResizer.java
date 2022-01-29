/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2011 dimitry
 *
 *  This file author is dimitry
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version CONTROL_SIZE of the License, or
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
package org.freeplane.core.ui.components.resizer;

import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;

/**
 * @author Dimitry Polivaev
 * Jan 24, 2011
 */
public class JResizer extends JComponent {
	private static final long serialVersionUID = 1L;
	private static final int CONTROL_SIZE = 5;
	protected boolean sliderLock = false;
	private Point point;
	private int index;
	public enum Direction {RIGHT, LEFT, UP, DOWN;
		Box createBox() {
			switch (this) {
				case RIGHT:
				case LEFT:
					return Box.createHorizontalBox();
				default:
					return Box.createVerticalBox();
			}
		}

		int getPreferredSize(final Component component) {
			final Dimension preferredSize = component.getPreferredSize();
			switch (this) {
				case RIGHT:
				case LEFT:
					return preferredSize.width;
				default:
					return preferredSize.height;
			}
		}

		void setPreferredSize(Component component, int size) {
			switch (this) {
				case RIGHT:
				case LEFT:
					component.setPreferredSize(new Dimension(size, 1));
					return;
				default:
					component.setPreferredSize(new Dimension(1, size));
			}
        }

		public Box createBox(Component resizedComponent) {
			final Box box = createBox();
			final JResizer resizer = createResizer();
			switch (this) {
				case RIGHT:
				case DOWN:
					box.add(resizer);
					box.add(resizedComponent);
					break;
				default:
					box.add(resizedComponent);
					box.add(resizer);
			}
			return box;

		}

		protected JResizer createResizer() {
			return new JResizer(this);
		}
	}

	JResizer(final Direction direction) {
		setOpaque(true);
		final int w;
		final int h;
		if(direction.equals(Direction.RIGHT)){
			w = CONTROL_SIZE;
			h = 0;
			setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
		}
		else if(direction.equals(Direction.LEFT)){
			h = CONTROL_SIZE;
			w = 0;
			setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
		}
		else if(direction.equals(Direction.UP)){
			h = 0;
			w = CONTROL_SIZE;
			setCursor(Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR));
		}
		else /*Direction.DOWN*/ {
			h = 0;
			w = CONTROL_SIZE;
			setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
		}

		setPreferredSize(new Dimension(w, h));
		addMouseListener(new MouseAdapter() {



			@Override
            public void mousePressed(MouseEvent e) {
				point = null;
            }

			@Override
            public void mouseReleased(MouseEvent e) {
				point = null;
            }

		});
		addMouseMotionListener(new MouseMotionAdapter() {

			private int getIndex() {
				final Container parent = getParent();
				for(int i = 0; i < parent.getComponentCount(); i++ ){
					if(JResizer.this.equals(parent.getComponent(i))){
						if(direction.equals(Direction.RIGHT)){
							return i + 1;
						}
						else if(direction.equals(Direction.LEFT)){
							return i - 1;
						}
						else if(direction.equals(Direction.UP)){
							return i - 1;
						}
						else if(direction.equals(Direction.DOWN)){
							return i + 1;
						}
					}
				}
				return -1;
            }


            @Override
            public void mouseDragged(MouseEvent e) {
            	if(sliderLock) {
            		return;
            	}
				final Point point2 = e.getPoint();
				SwingUtilities.convertPointToScreen(point2, e.getComponent());
				if(point != null){
					final JComponent parent = (JComponent) getParent();
					JRootPane rootPane = getRootPane();
					final Component resizedComponent = parent.getComponent(index);
					final Dimension size = new Dimension(resizedComponent.getPreferredSize());
					if(direction.equals(Direction.RIGHT)){
						size.width -= (point2.x - point.x);
					}
					else if(direction.equals(Direction.LEFT)){
						size.width += (point2.x - point.x);
					}
					else if(direction.equals(Direction.UP)){
						size.height += (point2.y - point.y);
					}
					else if(direction.equals(Direction.DOWN)){
						size.height -= (point2.y - point.y);
					}
                    size.width = Math.min(size.width, rootPane.getWidth() * 9 / 10 - CONTROL_SIZE);
                    size.height = Math.min(size.height, rootPane.getHeight() * 9 / 10 - CONTROL_SIZE);
					resizedComponent.setPreferredSize(new Dimension(Math.max(size.width, 0), Math.max(size.height, 0)));
					parent.revalidate();
					parent.repaint();
					onSizeChanged(resizedComponent);
				}
				else{
					index = getIndex();
				}
				point = point2;
            }
		});
    }

	public void setSliderLocked(boolean enabled) {
		this.sliderLock = enabled;
	}

	public boolean isSliderLocked() {
		return this.sliderLock;
	}

	void onSizeChanged(Component resizedComponent) {/**/}

}
