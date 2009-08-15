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
import java.awt.FontMetrics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.JPopupMenu;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;

import org.freeplane.core.frame.ViewController;
import org.freeplane.core.modecontroller.INodeChangeListener;
import org.freeplane.core.modecontroller.MapController;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.modecontroller.NodeChangeEvent;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.features.mindmapmode.ortho.SpellCheckerController;
import org.freeplane.features.mindmapmode.text.AbstractEditNodeTextField;

/**
 * @author foltin
 */
class EditNodeTextField extends AbstractEditNodeTextField {
	private int extraWidth;

	private final class MyDocumentListener implements DocumentListener {
		public void changedUpdate(DocumentEvent e) {
			onUpdate(false);
		}

		private void onUpdate(final boolean forceLineWrap) {
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					layout(forceLineWrap);
				}
			});
		}

		public void insertUpdate(DocumentEvent e) {
			if (!textfield.getLineWrap()) {
				final int offset = e.getOffset();
				final int length = e.getLength();
				try {
					if (e.getDocument().getText(offset, length).contains("\n") && !textfield.getLineWrap()) {
						onUpdate(true);
						return;
					}
				}
				catch (BadLocationException e1) {
				}
			}
			onUpdate(false);
		}

		public void removeUpdate(DocumentEvent e) {
			onUpdate(false);
		}
	}

	private void layout(boolean forceLineWrap) {
		if (textfield == null) {
			return;
		}
		final int lastWidth = textfield.getWidth();
		final int lastHeight = textfield.getHeight();
		final int height;
		final int width;
		final boolean lineWrap = lastWidth == maxWidth;
		if (!lineWrap) {
			int preferredWidth;
			if(forceLineWrap || (preferredWidth= getTextPreferredWidth()) > maxWidth){
    			textfield.setSize(maxWidth, Integer.MAX_VALUE);
    			textfield.setLineWrap(true);
				width = maxWidth;
				height = Math.max(lastHeight, textfield.getPreferredScrollableViewportSize().height);
			}
			else{
				preferredWidth= getTextPreferredWidth();
				if (preferredWidth < lastWidth) {
					width = lastWidth;
				}
				else {
					width = Math.min(preferredWidth + extraWidth, maxWidth);
					if (width == maxWidth) {
						textfield.setLineWrap(true);
					}
				}
				height = lastHeight;
			}
		}
		else {
			width = maxWidth;
			height = Math.max(lastHeight, textfield.getPreferredScrollableViewportSize().height);
		}
		if (width == lastWidth && height == lastHeight) {
			textfield.repaint();
			return;
		}
		textfield.setSize(width, height);
		final Component mainView = textfield.getParent();
		mainView.setPreferredSize(new Dimension(width, height));
		textfield.revalidate();
	}

	class TextFieldListener implements KeyListener, FocusListener, MouseListener, INodeChangeListener {
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
					final String output;
					if (commit) {
						output = textfield.getText();
					}
					else {
						output = null;
					}
					e.consume();
					eventSource = CANCEL;
					hideMe();
					if (commit) {
						getEditControl().ok(output);
					}
					else {
						getEditControl().cancel();
					}
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

		public void nodeChanged(NodeChangeEvent event) {
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					focusLost(null);
				}
			});
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
		final ModeController modeController = getModeController();
		modeController.getMapController().removeNodeChangeListener((INodeChangeListener) textFieldListener);
		final MainView mainView = (MainView) textfield.getParent();
		final Rectangle bounds = textfield.getBounds();
		textfield.getDocument().removeDocumentListener(documentListener);
		textfield = null;
		mainView.setPreferredSize(null);
		mainView.updateText(getNode().getText());
		mainView.getParent().invalidate();
		mainView.remove(0);
		mainView.revalidate();
		mainView.repaint();
	}

	private NodeView nodeView;
	private Font font;
	private float zoom;

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
		maxWidth = ResourceController.getResourceController().getIntProperty("max_node_width", 0);
		maxWidth = mapView.getZoomed(maxWidth) + 1;
		extraWidth = ResourceController.getResourceController().getIntProperty("editor_extra_width", 80);
		extraWidth = mapView.getZoomed(extraWidth);
		font = nodeView.getTextFont();
		zoom = viewController.getZoom();
		if (zoom != 1F) {
			font = font.deriveFont(font.getSize() * zoom * 0.97f);
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
		SpellCheckerController.getController(modeController).enableAutoSpell(textfield);
		mapView.scrollNodeToVisible(nodeView);
		final MainView mainView = nodeView.getMainView();
		final Dimension textFieldSize = textfield.getPreferredSize();
		textFieldSize.width += 1;
		textfield.setWrapStyleWord(true);
		if (textFieldSize.width > maxWidth) {
			textFieldSize.width = maxWidth;
			textfield.setSize(maxWidth, Integer.MAX_VALUE);
			textfield.setLineWrap(true);
			textFieldSize.height = textfield.getPreferredSize().height;
		}
		int nodeWidth = mainView.getWidth();
		final int nodeHeight = mainView.getHeight();
		maxWidth += nodeWidth - textFieldSize.width;
		textfield.setSize(nodeWidth, nodeHeight);
		int iconWidth = mainView.getIconWidth();
		if (iconWidth != 0) {
			iconWidth += mapView.getZoomed(mainView.getIconTextGap());
			nodeWidth -= iconWidth;
		}
		int topBorder = (nodeHeight + 1 - textFieldSize.height) / 2;
		final int leftBorder = (nodeWidth + 1 - textFieldSize.width) / 2;
		final Color borderColor;
		//		if(MapView.standardDrawRectangleForSelection){
		//			borderColor= nodeTextBackground;
		//		}
		//		else{
		borderColor = nodeView.getSelectedColor();
		//		}
		if (nodeView.isLeft() && !nodeView.isRoot()) {
			textfield.setBorder(BorderFactory.createMatteBorder(topBorder, leftBorder, topBorder, leftBorder
			        + iconWidth, borderColor));
		}
		else {
			textfield.setBorder(BorderFactory.createMatteBorder(topBorder, leftBorder + iconWidth, topBorder,
			    leftBorder, borderColor));
		}
		mainView.add(textfield, 0);
		if (firstEvent != null) {
			redispatchKeyEvents(textfield, firstEvent);
		}
		else {
			textfield.setCaretPosition(getText().length());
		}
		textfield.getDocument().addDocumentListener(documentListener);
		final MapController mapController = modeController.getMapController();
		mapController.addNodeChangeListener(textFieldListener);
		layout(false);
		textfield.repaint();
		textfield.requestFocus();
	}

	private int getTextPreferredWidth() {
		final Insets insets = textfield.getInsets();
		final FontMetrics fontMetrics = textfield.getFontMetrics(font);
		int preferredWidth = fontMetrics.stringWidth(textfield.getText()) + insets.left + insets.right + 1;
		if (zoom != 1f) {
			preferredWidth += font.getSize() / 2;
		}
		return preferredWidth;
	}
}
