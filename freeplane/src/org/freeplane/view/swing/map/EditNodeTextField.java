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
import java.awt.Graphics;
import java.awt.Insets;
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

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.DefaultCaret;
import javax.swing.text.JTextComponent;

import org.freeplane.core.frame.ViewController;
import org.freeplane.core.modecontroller.INodeChangeListener;
import org.freeplane.core.modecontroller.MapController;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.modecontroller.NodeChangeEvent;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.features.mindmapmode.ortho.SpellCheckerController;
import org.freeplane.features.mindmapmode.text.AbstractEditNodeTextField;

/**
 * @author foltin
 */
class EditNodeTextField extends AbstractEditNodeTextField {
	private final class MyDocumentListener implements DocumentListener {
	    public void changedUpdate(DocumentEvent e) {
	        onUpdate(e);
	        
	    }

	    private void onUpdate(DocumentEvent e) {
	    	EventQueue.invokeLater(new Runnable(){

				public void run() {
			    	layout();
                }});
	    }

		private void layout() {
	        final int lastWidth = textfield.getWidth() + 0;
	    	final int lastHeight = textfield.getHeight();
			final MapView mapView = (MapView)textfield.getParent();
			final int height;
			final int width ;
			final boolean lineWrap = lastWidth == maxWidth;
			if(! lineWrap){
				final Insets insets = textfield.getInsets();
				final Font font = textfield.getFont();
				final FontMetrics fontMetrics = textfield.getFontMetrics(font);
				final int preferredWidth = fontMetrics.stringWidth(textfield.getText() + insets.left + insets.right);
				if(preferredWidth <= maxWidth){
					if(preferredWidth < lastWidth){
						width = lastWidth;
					}
					else{
						width = Math.min(preferredWidth + mapView.getZoomed(80), maxWidth);
					}
					height = lastHeight;
				}
				else{
					width = maxWidth;
					height =textfield.getPreferredScrollableViewportSize().height; 
				}
			}
			else{
				width = maxWidth;
				height =textfield.getPreferredScrollableViewportSize().height; 
			}
			if(width == lastWidth && height == lastHeight){
				textfield.repaint();
				return;
			}
			final NodeView nodeView = mapView.getNodeView(getNode());
			mapView.anchorToSelected(nodeView, 0f, 0f);
			final MainView mainView = nodeView.getMainView();
			mainView.setPreferredSize(new Dimension(width, height));
			nodeView.revalidate();
        }

	    public void insertUpdate(DocumentEvent e) {
	        onUpdate(e);
	        
	    }

	    public void removeUpdate(DocumentEvent e) {
	        onUpdate(e);
	        
	    }
    }

	class TextFieldListener implements KeyListener, FocusListener, MouseListener, ComponentListener, INodeChangeListener {
		final int CANCEL = 2;
		final int EDIT = 1;
		// TODO rladstaetter 18.02.2009 eventSource should be an enum
		Integer eventSource = EDIT;
		private final NodeView nodeView;
		private boolean popupShown;

		public TextFieldListener(final NodeView nodeView) {
			this.nodeView = nodeView;
		}

		public void componentHidden(final ComponentEvent e) {
			focusLost(null);
		}

		public void componentMoved(final ComponentEvent e) {
					Point location = new Point();
					Component component = e.getComponent();
					if(component instanceof NodeView){
						component = ((NodeView) component).getMainView();
					}
					UITools.convertPointToAncestor(component, location, MapView.class);
					textfield.setLocation(location);
		}

		public void componentResized(final ComponentEvent e) {
					final Component component = e.getComponent();
					if(component instanceof NodeView){
						return;
					}
					textfield.setSize(component.getSize());
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
					hideMe();
					if (commit) {
						getEditControl().ok(textfield.getText());
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
			EventQueue.invokeLater(new Runnable(){
				public void run() {
					focusLost(null);
				}});
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
		final MapView mapView = (MapView) textfield.getParent();
		final Rectangle bounds = textfield.getBounds();
		textfield.removeFocusListener(textFieldListener);
		textfield.removeKeyListener((KeyListener) textFieldListener);
		textfield.removeMouseListener((MouseListener) textFieldListener);
		textfield.getDocument().removeDocumentListener(documentListener);
		final ModeController modeController = getModeController();
		modeController.getMapController().removeNodeChangeListener((INodeChangeListener)textFieldListener);
		final Component component = modeController.getController().getViewController().getComponent(getNode());
		if (component != null) {
			component.removeComponentListener((ComponentListener) textFieldListener);
			SwingUtilities.getAncestorOfClass(NodeView.class, component).removeComponentListener((ComponentListener) textFieldListener);
		}
		if (mapView != null) {
			final MainView mainView = mapView.getNodeView(getNode()).getMainView();
			mainView.setPreferredSize(null);
			mainView.updateText(getNode().getText(), mapView);
			mainView.getParent().invalidate();
			mapView.remove(0);
			mapView.revalidate();
			mapView.repaint(bounds);
		}
		textFieldListener = null;
	}

	/* (non-Javadoc)
	 * @see org.freeplane.view.swing.map.INodeTextField#show()
	 */
	@Override
	public void show() {
		textfield = new JTextArea(getText());
		final ModeController modeController = getModeController();
		final ViewController viewController = modeController.getController().getViewController();
		final Component component = viewController.getComponent(getNode());
		final NodeView nodeView = (NodeView) SwingUtilities.getAncestorOfClass(NodeView.class, component);
		final MapView mapView = (MapView) viewController.getMapView();
		maxWidth = ResourceController.getResourceController().getIntProperty("max_node_width", 0);
		maxWidth = mapView.getZoomed(maxWidth) + 1;
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
		SpellCheckerController.getController(modeController).enableAutoSpell(textfield);
		mapView.scrollNodeToVisible(nodeView);
		
		final Point textFieldLocation = new Point();
		final MainView mainView = nodeView.getMainView();
		UITools.convertPointToAncestor(mainView, textFieldLocation, mapView);
		final Dimension textFieldSize = textfield.getPreferredSize();
		textFieldSize.width += 1;
		textfield.setLineWrap(true);
		textfield.setWrapStyleWord(true);
		if(textFieldSize.width > maxWidth){
			textFieldSize.width = maxWidth;
			textfield.setSize(maxWidth, 1);
			textFieldSize.height =textfield.getPreferredSize().height; 
		}

		int nodeWidth = mainView.getWidth();
		final int nodeHeight = mainView.getHeight();
		maxWidth += nodeWidth - textFieldSize.width;
		textfield.setSize(nodeWidth, nodeHeight);
		textfield.setLocation(textFieldLocation);
		int iconWidth = mainView.getIconWidth();
		if(iconWidth != 0){
			iconWidth += mapView.getZoomed(mainView.getIconTextGap());
			nodeWidth -= iconWidth;
		}
		final int topBorder = (nodeHeight - textFieldSize.height)/2;
		final int leftBorder = (nodeWidth - textFieldSize.width)/2;
		final Color selectedColor = nodeView.getSelectedColor();
		if(nodeView.isLeft() && ! nodeView.isRoot()){
			textfield.setBorder(BorderFactory.createMatteBorder(
				topBorder, leftBorder,topBorder,leftBorder + iconWidth, 
				selectedColor));
		}
		else{
			textfield.setBorder(BorderFactory.createMatteBorder(
				topBorder, leftBorder + iconWidth, topBorder, leftBorder, 
				selectedColor));
		}
		final JViewport viewPort = (JViewport)mapView.getParent();
		mapView.add(textfield, 0);		
		redispatchKeyEvents(textfield, firstEvent);
		textfield.revalidate();
		textfield.repaint();
		component.addComponentListener(textFieldListener);
		nodeView.addComponentListener(textFieldListener);
		textfield.getDocument().addDocumentListener(documentListener);
		modeController.getMapController().addNodeChangeListener(textFieldListener);
		textfield.requestFocus();
	}
}
