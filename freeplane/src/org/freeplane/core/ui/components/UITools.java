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
package org.freeplane.core.ui.components;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.frame.ViewController;
import org.freeplane.core.model.NodeModel;

/**
 * @author Dimitry Polivaev
 * 29.12.2008
 */
public class UITools {
	public static void addEscapeActionToDialog(final JDialog dialog) {
		class EscapeAction extends AbstractAction {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void actionPerformed(final ActionEvent e) {
				dialog.dispose();
			};
		}
		UITools.addEscapeActionToDialog(dialog, new EscapeAction());
	}

	public static void addEscapeActionToDialog(final JDialog dialog, final Action action) {
		UITools.addKeyActionToDialog(dialog, action, "ESCAPE", "end_dialog");
	}

	public static void addKeyActionToDialog(final JDialog dialog, final Action action, final String keyStroke,
	                                        final String actionId) {
		action.putValue(Action.NAME, actionId);
		dialog.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(keyStroke),
		    action.getValue(Action.NAME));
		dialog.getRootPane().getActionMap().put(action.getValue(Action.NAME), action);
	}

	public static void convertPointFromAncestor(final Component source, final Point p, Component c) {
		int x, y;
		while (c != source) {
			x = c.getX();
			y = c.getY();
			p.x -= x;
			p.y -= y;
			c = c.getParent();
		};
	}

	public static void convertPointToAncestor(final Component source, final Point point, final Class ancestorClass) {
		final Component destination = SwingUtilities.getAncestorOfClass(ancestorClass, source);
		UITools.convertPointToAncestor(source, point, destination);
	}

	public static void convertPointToAncestor(Component c, final Point p, final Component destination) {
		int x, y;
		while (c != destination) {
			x = c.getX();
			y = c.getY();
			p.x += x;
			p.y += y;
			c = c.getParent();
		};
	}

	public static KeyStroke getKeyStroke(final String keyStrokeDescription) {
		if (keyStrokeDescription == null) {
			return null;
		}
		final KeyStroke keyStroke = KeyStroke.getKeyStroke(keyStrokeDescription);
		if (keyStroke != null) {
			return keyStroke;
		}
		return KeyStroke.getKeyStroke("typed " + keyStrokeDescription);
	}

	static public void informationMessage(final Frame frame, final String message) {
		UITools.informationMessage(frame, message, "Freeplane");
	}

	static public void informationMessage(final Frame frame, final String message, final String title) {
		JOptionPane.showMessageDialog(frame, message, title, JOptionPane.INFORMATION_MESSAGE);
	}

	public static void informationMessage(final Frame frame, final String text, final String string, final int type) {
		JOptionPane.showMessageDialog(frame, text, string, type);
	}

	public static void setDialogLocationRelativeTo(final JDialog dialog, final Component c) {
		if (c == null) {
			return;
		}
		final Point compLocation = c.getLocationOnScreen();
		final int cw = c.getWidth();
		final int ch = c.getHeight();
		final Container parent = dialog.getParent();
		final Point parentLocation = parent.getLocationOnScreen();
		final int pw = parent.getWidth();
		final int ph = parent.getHeight();
		final int dw = dialog.getWidth();
		final int dh = dialog.getHeight();
		final Toolkit defaultToolkit = Toolkit.getDefaultToolkit();
		final Insets screenInsets = defaultToolkit.getScreenInsets(dialog.getGraphicsConfiguration());
		final Dimension screenSize = defaultToolkit.getScreenSize();
		final int minX = Math.max(parentLocation.x, screenInsets.left);
		final int minY = Math.max(parentLocation.y, screenInsets.top);
		final int maxX = Math.min(parentLocation.x + pw, screenSize.width - screenInsets.right);
		final int maxY = Math.min(parentLocation.y + ph, screenSize.height - screenInsets.bottom);
		int dx, dy;
		if (compLocation.x + cw < minX) {
			dx = minX;
		}
		else if (compLocation.x > maxX) {
			dx = maxX - dw;
		}
		else {
			final int leftSpace = compLocation.x - minX;
			final int rightSpace = maxX - (compLocation.x + cw);
			if (leftSpace > rightSpace) {
				if (leftSpace > dw) {
					dx = compLocation.x - dw;
				}
				else {
					dx = minX;
				}
			}
			else {
				if (rightSpace > dw) {
					dx = compLocation.x + cw;
				}
				else {
					dx = maxX - dw;
				}
			}
		}
		if (compLocation.y + ch < minY) {
			dy = minY;
		}
		else if (compLocation.y > maxY) {
			dy = maxY - dh;
		}
		else {
			final int topSpace = compLocation.y - minY;
			final int bottomSpace = maxY - (compLocation.y + ch);
			if (topSpace > bottomSpace) {
				if (topSpace > dh) {
					dy = compLocation.y - dh;
				}
				else {
					dy = minY;
				}
			}
			else {
				if (bottomSpace > dh) {
					dy = compLocation.y + ch;
				}
				else {
					dy = maxY - dh;
				}
			}
		}
		dialog.setLocation(dx, dy);
	}

	public static void setDialogLocationRelativeTo(final JDialog dialog, final Controller controller,
	                                               final NodeModel node) {
		if (node == null) {
			return;
		}
		final ViewController viewController = controller.getViewController();
		viewController.scrollNodeToVisible(node);
		final Component c = viewController.getComponent(node);
		UITools.setDialogLocationRelativeTo(dialog, c);
	}

	public static void setDialogLocationUnder(JDialog dialog, Controller controller, NodeModel node) {
		final ViewController viewController = controller.getViewController();
		final JComponent c = (JComponent) viewController.getComponent(node);
		final int x = 0;
		final int y = c.getHeight();
		Point location = new Point(x, y);
		SwingUtilities.convertPointToScreen(location, c);
		dialog.setLocation(location);
    }
	
	public static String showInputDialog(final Controller controller, final NodeModel node, final String text,
	                                     final String string) {
		if (node == null) {
			return null;
		}
		final ViewController viewController = controller.getViewController();
		viewController.scrollNodeToVisible(node);
		final Component parentComponent = viewController.getComponent(node);
		return JOptionPane.showInputDialog(parentComponent, text, string);
	}

	public static String showInputDialog(final Controller controller, final NodeModel node, final String text,
	                                     final String title, final int type) {
		if (node == null) {
			return null;
		}
		final ViewController viewController = controller.getViewController();
		viewController.scrollNodeToVisible(node);
		final Component parentComponent = viewController.getComponent(node);
		return JOptionPane.showInputDialog(parentComponent, text, title, type);
	}

	static public void setBounds(Component frame, int win_x, int win_y, int win_width, int win_height) {
        win_width = (win_width > 0) ? win_width : 640;
    	win_height = (win_height > 0) ? win_height : 440;
    	final Toolkit defaultToolkit = Toolkit.getDefaultToolkit();
    	final Insets screenInsets = defaultToolkit.getScreenInsets(frame.getGraphicsConfiguration());
    	final Dimension screenSize = defaultToolkit.getScreenSize();
    	final int screenWidth = screenSize.width - screenInsets.left - screenInsets.right;
    	win_width = Math.min(win_width, screenWidth);
    	final int screenHeight = screenSize.height - screenInsets.top - screenInsets.bottom;
    	win_height = Math.min(win_height, screenHeight);
    	win_x = Math.max(screenInsets.left, win_x);
    	win_x = Math.min(screenWidth + screenInsets.left - win_width, win_x);
    	win_y = Math.max(screenInsets.top, win_y);
    	win_y = Math.min(screenWidth + screenInsets.top - win_height, win_y);
    	frame.setBounds(win_x, win_y, win_width, win_height);
    }

}
