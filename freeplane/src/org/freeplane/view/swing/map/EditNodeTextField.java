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
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.freeplane.core.frame.ViewController;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.features.common.addins.mapstyle.MapStyleModel;
import org.freeplane.features.mindmapmode.ortho.SpellCheckerController;
import org.freeplane.features.mindmapmode.text.AbstractEditNodeTextField;

/**
 * @author foltin
 */
class EditNodeTextField extends AbstractEditNodeTextField {
	private int extraWidth;

	private final class MyDocumentListener implements DocumentListener {
		public void changedUpdate(final DocumentEvent e) {
			onUpdate();
		}

		private void onUpdate() {
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					layout();
				}
			});
		}

		public void insertUpdate(final DocumentEvent e) {
			onUpdate();
		}

		public void removeUpdate(final DocumentEvent e) {
			onUpdate();
		}
	}

	private void layout() {
		if (textfield == null) {
			return;
		}
		final int lastWidth = textfield.getWidth();
		final int lastHeight = textfield.getHeight();
		final boolean lineWrap = lastWidth == maxWidth;
		final Dimension preferredSize;
		if (!lineWrap) {
			preferredSize = textfield.getPreferredSize();
			preferredSize.width += 1;
			if (preferredSize.width > maxWidth) {
				textfield.setSize(maxWidth, Integer.MAX_VALUE);
				textfield.setLineWrap(true);
				preferredSize.width = maxWidth;
				preferredSize.height = Math.max(lastHeight, textfield.getPreferredSize().height);
			}
			else {
				if (preferredSize.width < lastWidth) {
					preferredSize.width = lastWidth;
				}
				else {
					preferredSize.width = Math.min(preferredSize.width + extraWidth, maxWidth);
					if (preferredSize.width == maxWidth) {
						textfield.setLineWrap(true);
					}
				}
				preferredSize.height = Math.max(preferredSize.height, lastHeight);
			}
		}
		else {
			preferredSize = new Dimension(maxWidth, Math.max(lastHeight,
			    textfield.getPreferredScrollableViewportSize().height));
		}
		if (preferredSize.width == lastWidth && preferredSize.height == lastHeight) {
			textfield.repaint();
			return;
		}
		textfield.setSize(preferredSize);
		final JComponent mainView = (JComponent) textfield.getParent();
		mainView.setPreferredSize(new Dimension(preferredSize.width + horizontalSpace + iconWidth, preferredSize.height
		        + verticalSpace));
		textfield.revalidate();
		final NodeView nodeView = (NodeView) SwingUtilities.getAncestorOfClass(NodeView.class, mainView);
		final MapView mapView = (MapView) SwingUtilities.getAncestorOfClass(MapView.class, nodeView);
		mapView.scrollNodeToVisible(nodeView);
	}

	class TextFieldListener implements KeyListener, FocusListener, MouseListener {
		final int CANCEL = 2;
		final int EDIT = 1;
		// TODO rladstaetter 18.02.2009 eventSource should be an enum
		Integer eventSource = EDIT;
		private boolean popupShown;

		public TextFieldListener() {
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
			if (textfield == null || !textfield.isVisible() || eventSource == CANCEL || popupShown) {
				return;
			}
			if (e == null) {
				getEditControl().ok(textfield.getText());
				hideMe();
				eventSource = CANCEL;
				return;
			}
			if (e.isTemporary() && e.getOppositeComponent() == null) {
				return;
			}
			getEditControl().ok(textfield.getText());
			hideMe();
		}

		public void keyPressed(final KeyEvent e) {
			if (e.isControlDown() || e.isMetaDown() || eventSource == CANCEL) {
				return;
			}
			switch (e.getKeyCode()) {
				case KeyEvent.VK_ESCAPE:
					eventSource = CANCEL;
					hideMe();
					getEditControl().cancel();
					nodeView.requestFocus();
					e.consume();
					break;
				case KeyEvent.VK_ENTER: {
					final boolean enterConfirms = ResourceController.getResourceController().getBooleanProperty(
					    "il__enter_confirms_by_default");
					if (enterConfirms == e.isAltDown() || e.isShiftDown()) {
						e.consume();
						final Component component = e.getComponent();
						final KeyEvent keyEvent = new KeyEvent(component, e.getID(), e.getWhen(), 0, e.getKeyCode(), e
						    .getKeyChar(), e.getKeyLocation());
						SwingUtilities.processKeyBindings(keyEvent);
						break;
					}
				}
					final String output;
					output = textfield.getText();
					e.consume();
					eventSource = CANCEL;
					hideMe();
					getEditControl().ok(output);
					nodeView.requestFocus();
					break;
				case KeyEvent.VK_TAB:
					textfield.insert("    ", textfield.getCaretPosition());
				case KeyEvent.VK_SPACE:
					e.consume();
					break;
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

	final private KeyEvent firstEvent;
	private JTextArea textfield;
	private final DocumentListener documentListener;
	private int maxWidth;

	public EditNodeTextField(final NodeModel node, final String text, final KeyEvent firstEvent,
	                         final ModeController controller, final IEditControl editControl) {
		super(node, text, controller, editControl);
		this.firstEvent = firstEvent;
		documentListener = new MyDocumentListener();
	}

	private void hideMe() {
		if (textfield == null) {
			return;
		}
		textfield.getDocument().removeDocumentListener(documentListener);
		final MainView mainView = (MainView) textfield.getParent();
		textfield = null;
		mainView.setPreferredSize(null);
		mainView.updateText(getNode().getText());
		mainView.setHorizontalAlignment(JLabel.CENTER);
		mainView.remove(0);
		mainView.revalidate();
		mainView.repaint();
	}

	private NodeView nodeView;
	private Font font;
	private float zoom;
	private int iconWidth;
	private int horizontalSpace;
	private int verticalSpace;

	/* (non-Javadoc)
	 * @see org.freeplane.view.swing.map.INodeTextField#show()
	 */
	@Override
	public void show() {
		textfield = new JTextArea(getText());
		final ModeController modeController = getModeController();
		final ViewController viewController = modeController.getController().getViewController();
		final Component component = viewController.getComponent(getNode());
		nodeView = (NodeView) SwingUtilities.getAncestorOfClass(NodeView.class, component);
		final MapView mapView = (MapView) viewController.getMapView();
		maxWidth = MapStyleModel.getExtension(mapView.getModel()).getMaxNodeWidth();
		maxWidth = mapView.getZoomed(maxWidth) + 1;
		extraWidth = ResourceController.getResourceController().getIntProperty("editor_extra_width", 80);
		extraWidth = mapView.getZoomed(extraWidth);
		font = nodeView.getTextFont();
		zoom = viewController.getZoom();
		if (zoom != 1F) {
			final float fontSize = (int) (Math.rint(font.getSize() * zoom));
			font = font.deriveFont(fontSize);
		}
		textfield.setFont(font);
		final Color nodeTextColor = nodeView.getTextColor();
		textfield.setForeground(nodeTextColor);
		final Color nodeTextBackground = nodeView.getTextBackground();
		textfield.setBackground(nodeTextBackground);
		textfield.setCaretColor(nodeTextColor);
		final TextFieldListener textFieldListener = new TextFieldListener();
		this.textFieldListener = textFieldListener;
		textfield.addFocusListener(textFieldListener);
		textfield.addKeyListener(textFieldListener);
		textfield.addMouseListener(textFieldListener);
		textfield.setWrapStyleWord(true);
		SpellCheckerController.getController(modeController).enableAutoSpell(textfield, true);
		mapView.scrollNodeToVisible(nodeView);
		final MainView mainView = nodeView.getMainView();
		final int nodeWidth = mainView.getWidth();
		final int nodeHeight = mainView.getHeight();
		final Dimension textFieldSize;
		textfield.setBorder(new MatteBorder(2, 2, 2, 2, nodeView.getSelectedColor()));
		textFieldSize = textfield.getPreferredSize();
		textFieldSize.width += 1;
		if (textFieldSize.width > maxWidth) {
			textFieldSize.width = maxWidth;
			textfield.setSize(textFieldSize.width, Integer.MAX_VALUE);
			textfield.setLineWrap(true);
			textFieldSize.height = textfield.getPreferredSize().height;
			horizontalSpace = nodeWidth - textFieldSize.width;
			verticalSpace = nodeHeight - textFieldSize.height;
		}
		else {
			horizontalSpace = nodeWidth - textFieldSize.width;
			verticalSpace = nodeHeight - textFieldSize.height;
		}
		if (horizontalSpace < 0) {
			horizontalSpace = 0;
		}
		if (verticalSpace < 0) {
			verticalSpace = 0;
		}
		textfield.setSize(textFieldSize.width, textFieldSize.height);
		mainView.setPreferredSize(new Dimension(textFieldSize.width + horizontalSpace, textFieldSize.height
		        + verticalSpace));
		iconWidth = mainView.getIconWidth();
		if (iconWidth != 0) {
			iconWidth += mapView.getZoomed(mainView.getIconTextGap());
			horizontalSpace -= iconWidth;
		}
		final int x = (horizontalSpace + 1) / 2;
		final int y = (verticalSpace + 1) / 2;
		if (nodeView.isLeft() && !nodeView.isRoot()) {
			textfield.setBounds(x, y, textFieldSize.width, textFieldSize.height);
			mainView.setText("");
			mainView.setHorizontalAlignment(JLabel.RIGHT);
		}
		else {
			textfield.setBounds(x + iconWidth, y, textFieldSize.width, textFieldSize.height);
			mainView.setText("");
			mainView.setHorizontalAlignment(JLabel.LEFT);
		}
		mainView.add(textfield, 0);
		if (firstEvent != null) {
			redispatchKeyEvents(textfield, firstEvent);
		}
		else {
			textfield.setCaretPosition(getText().length());
		}
		textfield.getDocument().addDocumentListener(documentListener);
		textfield.repaint();
		textfield.requestFocus();
	}
}
