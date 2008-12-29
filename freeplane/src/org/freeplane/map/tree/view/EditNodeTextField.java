/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is modified by Dimitry Polivaev in 2008.
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
package org.freeplane.map.tree.view;

import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.freeplane.Tools;
import org.freeplane.map.tree.NodeModel;
import org.freeplane.modes.ModeController;
import org.freeplane.ui.components.UITools;

/**
 * @author foltin
 */
public class EditNodeTextField extends EditNodeBase {
	final private KeyEvent firstEvent;
	private JTextField textfield;

	public EditNodeTextField(final NodeView node, final String text, final KeyEvent firstEvent,
	                         final ModeController controller, final IEditControl editControl) {
		super(node, text, controller, editControl);
		this.firstEvent = firstEvent;
	}

	private void hideMe() {
		final JComponent parent = (JComponent) textfield.getParent();
		final Rectangle bounds = textfield.getBounds();
		textfield.removeFocusListener(textFieldListener);
		textfield.removeKeyListener((KeyListener) textFieldListener);
		textfield.removeMouseListener((MouseListener) textFieldListener);
		getNode().removeComponentListener((ComponentListener) textFieldListener);
		parent.remove(0);
		parent.revalidate();
		parent.repaint(bounds);
		textFieldListener = null;
	}

	public void show() {
		textfield = (getText().length() < 8) ? new JTextField(getText(), 8) : new JTextField(
		    getText());
		final int cursorWidth = 1;
		int xOffset = 0;
		final int yOffset = -1;
		final int widthAddition = 2 * 0 + cursorWidth + 2;
		final int heightAddition = 2;
		final int MINIMAL_LEAF_WIDTH = 150;
		final int MINIMAL_WIDTH = 50;
		final NodeView nodeView = getNode();
		final NodeModel model = nodeView.getModel();
		int xSize = nodeView.getMainView().getTextWidth() + widthAddition;
		xOffset += nodeView.getMainView().getTextX();
		int xExtraWidth = 0;
		if (MINIMAL_LEAF_WIDTH > xSize
		        && (model.getModeController().getMapController().isFolded(model) || !model
		            .getModeController().getMapController().hasChildren(model))) {
			xExtraWidth = MINIMAL_LEAF_WIDTH - xSize;
			xSize = MINIMAL_LEAF_WIDTH;
			if (nodeView.isLeft()) {
				xExtraWidth = -xExtraWidth;
				textfield.setHorizontalAlignment(SwingConstants.RIGHT);
			}
		}
		else if (MINIMAL_WIDTH > xSize) {
			xExtraWidth = MINIMAL_WIDTH - xSize;
			xSize = MINIMAL_WIDTH;
			if (nodeView.isLeft()) {
				xExtraWidth = -xExtraWidth;
				textfield.setHorizontalAlignment(SwingConstants.RIGHT);
			}
		}
		textfield.setSize(xSize, nodeView.getMainView().getHeight() + heightAddition);
		Font font = nodeView.getTextFont();
		final MapView mapView = nodeView.getMap();
		final float zoom = mapView.getZoom();
		if (zoom != 1F) {
			font = font.deriveFont(font.getSize() * zoom * MainView.ZOOM_CORRECTION_FACTOR);
		}
		textfield.setFont(font);
		final Color nodeTextColor = nodeView.getTextColor();
		textfield.setForeground(nodeTextColor);
		final Color nodeTextBackground = nodeView.getTextBackground();
		textfield.setBackground(nodeTextBackground);
		textfield.setCaretColor(nodeTextColor);
		final int EDIT = 1;
		final int CANCEL = 2;
		final Tools.IntHolder eventSource = new Tools.IntHolder();
		eventSource.setValue(EDIT);
		class TextFieldListener implements KeyListener, FocusListener, MouseListener,
		        ComponentListener {
			public void componentHidden(final ComponentEvent e) {
				focusLost(null);
			}

			public void componentMoved(final ComponentEvent e) {
				focusLost(null);
			}

			public void componentResized(final ComponentEvent e) {
				focusLost(null);
			}

			public void componentShown(final ComponentEvent e) {
				focusLost(null);
			}

			private void conditionallyShowPopup(final MouseEvent e) {
				if (e.isPopupTrigger()) {
					final JPopupMenu popupMenu = new EditPopupMenu(textfield);
					popupMenu.show(e.getComponent(), e.getX(), e.getY());
					e.consume();
				}
			}

			public void focusGained(final FocusEvent e) {
			}

			public void focusLost(final FocusEvent e) {
				if (!textfield.isVisible() || eventSource.getValue() == CANCEL) {
					return;
				}
				if (e == null) {
					getEditControl().ok(textfield.getText());
					hideMe();
					eventSource.setValue(CANCEL);
				}
				else {
					getEditControl().ok(textfield.getText());
					hideMe();
				}
			}

			public void keyPressed(final KeyEvent e) {
				if (e.isAltDown() || e.isControlDown() || e.isMetaDown()
				        || eventSource.getValue() == CANCEL) {
					return;
				}
				boolean commit = true;
				switch (e.getKeyCode()) {
					case KeyEvent.VK_ESCAPE:
						commit = false;
					case KeyEvent.VK_ENTER:
						e.consume();
						eventSource.setValue(CANCEL);
						if (commit) {
							getEditControl().ok(textfield.getText());
						}
						else {
							getEditControl().cancel();
						}
						hideMe();
						nodeView.requestFocus();
						break;
					case KeyEvent.VK_SPACE:
						e.consume();
				}
			}

			public void keyReleased(final KeyEvent e) {
			}

			public void keyTyped(final KeyEvent e) {
			}

			public void mouseClicked(final MouseEvent e) {
			}

			public void mouseEntered(final MouseEvent e) {
			}

			public void mouseExited(final MouseEvent e) {
			}

			public void mousePressed(final MouseEvent e) {
				conditionallyShowPopup(e);
			}

			public void mouseReleased(final MouseEvent e) {
				conditionallyShowPopup(e);
			}
		}
		final TextFieldListener textFieldListener = new TextFieldListener();
		this.textFieldListener = textFieldListener;
		textfield.addFocusListener(textFieldListener);
		textfield.addKeyListener(textFieldListener);
		textfield.addMouseListener(textFieldListener);
		getView().scrollNodeToVisible(nodeView, xExtraWidth);
		final Point textFieldLocation = new Point();
		UITools.convertPointToAncestor(nodeView.getMainView(), textFieldLocation, mapView);
		if (xExtraWidth < 0) {
			textFieldLocation.x += xExtraWidth;
		}
		textFieldLocation.x += xOffset;
		textFieldLocation.y += yOffset;
		textfield.setLocation(textFieldLocation);
		mapView.add(textfield, 0);
		textfield.repaint();
		redispatchKeyEvents(textfield, firstEvent);
		getNode().addComponentListener(textFieldListener);
		textfield.requestFocus();
	}
}
