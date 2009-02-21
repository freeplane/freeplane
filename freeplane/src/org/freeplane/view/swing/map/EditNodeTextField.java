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
package org.freeplane.view.swing.map;

import java.awt.Color;
import java.awt.Component;
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
import javax.swing.SwingUtilities;

import org.freeplane.core.frame.ViewController;
import org.freeplane.core.modecontroller.MapController;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.features.mindmapmode.ortho.SpellCheckerController;
import org.freeplane.features.mindmapmode.text.AbstractEditNodeTextField;

/**
 * @author foltin
 */
class EditNodeTextField extends AbstractEditNodeTextField {
	final private KeyEvent firstEvent;
	private JTextField textfield;

	public EditNodeTextField(final NodeModel node, final String text, final KeyEvent firstEvent,
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
		final Component component = getModeController().getController().getViewController().getComponent(getNode());
		component.removeComponentListener((ComponentListener) textFieldListener);
		parent.remove(0);
		parent.revalidate();
		parent.repaint(bounds);
		textFieldListener = null;
	}

	/* (non-Javadoc)
	 * @see org.freeplane.view.swing.map.INodeTextField#show()
	 */
	@Override
	public void show() {
		textfield = (getText().length() < 8) ? new JTextField(getText(), 8) : new JTextField(getText());
		final int cursorWidth = 1;
		int xOffset = 0;
		final int yOffset = -1;
		final int widthAddition = 2 * 0 + cursorWidth + 2;
		final int heightAddition = 2;
		final int MINIMAL_LEAF_WIDTH = 150;
		final int MINIMAL_WIDTH = 50;
		final ViewController viewController = getModeController().getController().getViewController();
		final Component component = viewController.getComponent(getNode());
		final NodeView nodeView = (NodeView) SwingUtilities.getAncestorOfClass(NodeView.class, component);
		final NodeModel model = nodeView.getModel();
		final MapView mapView = (MapView) viewController.getMapView();
		int xSize = nodeView.getMainView().getTextWidth() + widthAddition;
		xOffset += nodeView.getMainView().getTextX();
		int xExtraWidth = 0;
		final MapController mapController = mapView.getModeController().getMapController();
		if (MINIMAL_LEAF_WIDTH > xSize
		        && (mapController.isFolded(model) || !mapController.hasChildren(model))) {
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
		final float zoom = viewController.getZoom();
		if (zoom != 1F) {
			font = font.deriveFont(font.getSize() * zoom * MainView.ZOOM_CORRECTION_FACTOR);
		}
		textfield.setFont(font);
		final Color nodeTextColor = nodeView.getTextColor();
		textfield.setForeground(nodeTextColor);
		final Color nodeTextBackground = nodeView.getTextBackground();
		textfield.setBackground(nodeTextBackground);
		textfield.setCaretColor(nodeTextColor);
		final TextFieldListener textFieldListener = new TextFieldListener(nodeView);
		this.textFieldListener = textFieldListener;
		textfield.addFocusListener(textFieldListener);
		textfield.addKeyListener(textFieldListener);
		textfield.addMouseListener(textFieldListener);
		SpellCheckerController.getController(getModeController()).enableAutoSpell(textfield);
		mapView.scrollNodeToVisible(nodeView, xExtraWidth);
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
		component.addComponentListener(textFieldListener);
		textfield.requestFocus();
	}

	class TextFieldListener implements KeyListener, FocusListener, MouseListener, ComponentListener {
		private boolean popupShown;
		final int EDIT = 1;
		final int CANCEL = 2;
		// TODO rladstaetter 18.02.2009 eventSource should be an enum
		Integer eventSource = EDIT;
		private NodeView nodeView;

		public TextFieldListener(NodeView nodeView) {
			this.nodeView = nodeView; 
		}

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
				popupShown = true;
				popupMenu.show(e.getComponent(), e.getX(), e.getY());
				e.consume();
			}
		}

		public void focusGained(final FocusEvent e) {
			popupShown = false;
		}

		public void focusLost(final FocusEvent e) {
			if (!textfield.isVisible() || eventSource == CANCEL || popupShown) {
				return;
			}
			if (e == null) {
				getEditControl().ok(textfield.getText());
				hideMe();
				eventSource = CANCEL;
			}
			else {
				getEditControl().ok(textfield.getText());
				hideMe();
			}
		}

		public void keyPressed(final KeyEvent e) {
			if (e.isAltDown() || e.isControlDown() || e.isMetaDown() || eventSource == CANCEL) {
				return;
			}
			boolean commit = true;
			switch (e.getKeyCode()) {
				case KeyEvent.VK_ESCAPE:
					commit = false;
				case KeyEvent.VK_ENTER:
					e.consume();
					eventSource = CANCEL;
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

}
