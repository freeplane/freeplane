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
package org.freeplane.view.swing.map.mindmapmode;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.io.Writer;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.DefaultEditorKit.PasteAction;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.NavigationFilter;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.Position.Bias;
import javax.swing.text.StyledEditorKit.BoldAction;
import javax.swing.text.StyledEditorKit.ForegroundAction;
import javax.swing.text.StyledEditorKit.ItalicAction;
import javax.swing.text.StyledEditorKit.StyledTextAction;
import javax.swing.text.StyledEditorKit.UnderlineAction;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.HTMLWriter;
import javax.swing.text.html.StyleSheet;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.ColorUtils;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.mode.mindmapmode.ortho.SpellCheckerController;
import org.freeplane.features.styles.MapStyleModel;
import org.freeplane.features.text.TextController;
import org.freeplane.features.text.mindmapmode.EditNodeBase;
import org.freeplane.features.text.mindmapmode.MTextController;
import org.freeplane.features.ui.ViewController;
import org.freeplane.view.swing.map.MainView;
import org.freeplane.view.swing.map.MapView;
import org.freeplane.view.swing.map.NodeView;
import org.freeplane.view.swing.map.ZoomableLabel;

import com.lightdev.app.shtm.SHTMLWriter;


/**
 * @author foltin
 */
class EditNodeTextField extends EditNodeBase {
    private class MyNavigationFilter extends NavigationFilter {
        /* (non-Javadoc)
         * @see javax.swing.text.NavigationFilter#moveDot(javax.swing.text.NavigationFilter.FilterBypass, int, javax.swing.text.Position.Bias)
         */
        public void moveDot(final FilterBypass fb, int dot, final Bias bias) {
            dot = getValidPosition(dot);
            super.moveDot(fb, dot, bias);
        }

        /* (non-Javadoc)
         * @see javax.swing.text.NavigationFilter#setDot(javax.swing.text.NavigationFilter.FilterBypass, int, javax.swing.text.Position.Bias)
         */
        public void setDot(final FilterBypass fb, int dot, final Bias bias) {
            dot = getValidPosition(dot);
            super.setDot(fb, dot, bias);
        }
    }

    private int getValidPosition(int position) {
        final HTMLDocument doc = (HTMLDocument) textfield.getDocument();
        if (doc.getDefaultRootElement().getElementCount() > 1) {
            final int startPos = doc.getDefaultRootElement().getElement(1).getStartOffset();
            final int validPosition = Math.max(position, startPos);
            return validPosition;
        }
        return position;
    }
    
	private int extraWidth;
	final private boolean layoutMapOnTextChange;

	private final class MyDocumentListener implements DocumentListener {
		private boolean updateRunning = false;
		public void changedUpdate(final DocumentEvent e) {
			onUpdate();
		}

		private void onUpdate() {
			if(updateRunning){
				return;
			}
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					updateRunning = true;
					layout();
					updateRunning = false;
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
		Dimension preferredSize = textfield.getPreferredSize();
		if (!lineWrap) {
			preferredSize.width ++;
			if (preferredSize.width > maxWidth) {
				setLineWrap();
				preferredSize = textfield.getPreferredSize();
			}
			else {
				if (preferredSize.width < lastWidth) {
					preferredSize.width = lastWidth;
				}
				else {
					preferredSize.width = Math.min(preferredSize.width + extraWidth, maxWidth);
					if (preferredSize.width == maxWidth) {
						setLineWrap();
					}
				}
			}
		}
		else {
			preferredSize.width = Math.max(maxWidth, preferredSize.width); 
		}
		if(preferredSize.width != lastWidth){
			preferredSize.height = lastHeight;
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					layout();
				}
			});
		}
		else{
			preferredSize.height = Math.max(preferredSize.height, lastHeight);
		}
		if (preferredSize.width == lastWidth && preferredSize.height == lastHeight) {
			textfield.repaint();
			return;
		}
		textfield.setSize(preferredSize);
		if(layoutMapOnTextChange)
			parent.setPreferredSize(new Dimension(preferredSize.width + horizontalSpace + iconWidth, preferredSize.height
				+ verticalSpace));
		textfield.revalidate();
		final NodeView nodeView = (NodeView) SwingUtilities.getAncestorOfClass(NodeView.class, parent);
		final MapView mapView = (MapView) SwingUtilities.getAncestorOfClass(MapView.class, nodeView);
		if(layoutMapOnTextChange)
			mapView.scrollNodeToVisible(nodeView);
		else
			mapView.scrollRectToVisible(textfield.getBounds());
	}

	private void setLineWrap() {
		if(null != textfield.getClientProperty("EditNodeTextField.linewrap")){
			return;
		}
	    final HTMLDocument document = (HTMLDocument) textfield.getDocument();
	    document.getStyleSheet().addRule("body { width: " + (maxWidth - 1) + "}");
	    // bad hack: call "setEditable" only to update view
	    textfield.setEditable(false);
	    textfield.setEditable(true);
	    textfield.putClientProperty("EditNodeTextField.linewrap", true);
    }

	class TextFieldListener implements KeyListener, FocusListener, MouseListener {
		final int CANCEL = 2;
		final int EDIT = 1;
		Integer eventSource = EDIT;
		private boolean popupShown;

		public TextFieldListener() {
		}

		private void conditionallyShowPopup(final MouseEvent e) {
			if (e.isPopupTrigger()) {
				final Component component = e.getComponent();
				final JPopupMenu popupMenu = createPopupMenu(component);
				popupShown = true;
				popupMenu.show(component, e.getX(), e.getY());
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
				submitText();
				hideMe();
				eventSource = CANCEL;
				return;
			}
			if (e.isTemporary() && e.getOppositeComponent() == null) {
				return;
			}
			if(textfield.isShowing()){
				submitText();
			}
			else{
				getEditControl().cancel();
			}
			hideMe();
		}

		private void submitText() {
	        submitText(getNewText());
        }

		private void submitText(final String output) {
			getEditControl().ok(output);
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
					nodeView.requestFocusInWindow();
					e.consume();
					break;
				case KeyEvent.VK_ENTER: {
					final boolean enterConfirms = ResourceController.getResourceController().getBooleanProperty("el__enter_confirms_by_default");
					if (enterConfirms == e.isAltDown() || e.isShiftDown()) {
						e.consume();
						final Component component = e.getComponent();
						final KeyEvent keyEvent = new KeyEvent(component, e.getID(), e.getWhen(), 0, e.getKeyCode(), e
						    .getKeyChar(), e.getKeyLocation());
						SwingUtilities.processKeyBindings(keyEvent);
						break;
					}
				}
				final String output = getNewText();
				e.consume();
				eventSource = CANCEL;
				hideMe();
				submitText(output);
				nodeView.requestFocusInWindow();
				break;
				case KeyEvent.VK_TAB:
					textfield.replaceSelection("    ");
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

	final private InputEvent firstEvent;
	private JEditorPane textfield;
	private final DocumentListener documentListener;
	private int maxWidth;

	@SuppressWarnings("serial")
    public EditNodeTextField(final NodeModel node, final ZoomableLabel parent, final String text, final InputEvent firstEvent,
	                         final IEditControl editControl) {
		super(node, text, editControl);
		this.firstEvent = firstEvent;
		this.parent = parent;
		this.layoutMapOnTextChange = ResourceController.getResourceController().getBooleanProperty("layout_map_on_text_change");
		documentListener = new MyDocumentListener();

		pasteAction = new DefaultEditorKit.PasteAction(){

			@Override
			public void actionPerformed(ActionEvent e) {
				JTextComponent target = getTextComponent(e);
				if (target == null) {
					return;
				}
				final Transferable contents = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(this);
				if(contents.isDataFlavorSupported(DataFlavor.stringFlavor)){
					try {
						String text = (String) contents.getTransferData(DataFlavor.stringFlavor);
						target.replaceSelection(text);
					}
					catch (Exception ex) {
					}
				}
			}
		};
		
		boldAction = new StyledEditorKit.BoldAction();
		boldAction.putValue(Action.NAME, TextUtils.getText("BoldAction.text"));
		boldAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control B"));
	
		italicAction = new StyledEditorKit.ItalicAction();
		italicAction.putValue(Action.NAME, TextUtils.getText("ItalicAction.text"));
		italicAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control I"));
		
		underlineAction = new StyledEditorKit.UnderlineAction();
		underlineAction.putValue(Action.NAME, TextUtils.getText("UnderlineAction.text"));
		underlineAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control U"));
		
		redAction = new ForegroundAction(TextUtils.getText("red"), Color.RED);
		redAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control R"));
		
		greenAction = new ForegroundAction(TextUtils.getText("green"), Color.GREEN);
		greenAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control G"));
		
		blueAction = new ForegroundAction(TextUtils.getText("blue"), Color.BLUE);
		blueAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control E"));
		
		blackAction = new ForegroundAction(TextUtils.getText("black"), Color.BLACK);
		blackAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control K"));
		
		defaultColorAction = new ExtendedEditorKit.RemoveStyleAttributeAction(StyleConstants.Foreground, TextUtils.getText("DefaultColorAction.text"));
		defaultColorAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control D"));
		
		removeFormattingAction = new ExtendedEditorKit.RemoveStyleAttributeAction(null, TextUtils.getText("simplyhtml.clearFormatLabel"));
		removeFormattingAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control T"));
		
		if(editControl != null ){
			final ModeController modeController = Controller.getCurrentModeController();
			final MTextController textController = (MTextController) TextController.getController(modeController);
			textfield = textController.createEditorPane(MTextController.NODE_TEXT);
			textfield.setNavigationFilter(new MyNavigationFilter());
		}
	}

	public String getNewText() {
		final SHTMLWriter shtmlWriter = new SHTMLWriter((HTMLDocument) textfield.getDocument());
		try {
	        shtmlWriter.write();
        }
        catch (Exception e) {
	        LogUtils.severe(e);
        }
		return shtmlWriter.toString();
    }

	private void hideMe() {
		if (textfield == null) {
			return;
		}
		textfield.getDocument().removeDocumentListener(documentListener);
		parent.setPreferredSize(null);
		nodeView.update();
		if(nodeView.isRoot() && parent instanceof MainView)
		    parent.setHorizontalAlignment(JLabel.CENTER);
		if(layoutMapOnTextChange)
			parent.remove(0);
		else
			nodeView.getMap().remove(0);
		parent.revalidate();
		parent.repaint();
		textfield = null;
	}

	private final ZoomableLabel parent;
	private NodeView nodeView;
	private Font font;
	private float zoom;
	private int iconWidth;
	private int horizontalSpace;
	private int verticalSpace;
	private final PasteAction pasteAction;
	private final BoldAction boldAction;
	private final ItalicAction italicAction;
	private final UnderlineAction underlineAction;
	private final ForegroundAction redAction;
	private final ForegroundAction greenAction;
	private final ForegroundAction blueAction;
	private final ForegroundAction blackAction;
	private StyledTextAction defaultColorAction;
	private StyledTextAction removeFormattingAction;

	@Override
    protected JPopupMenu createPopupMenu(Component component) {
		JPopupMenu menu = super.createPopupMenu(component);
	    JMenu formatMenu = new JMenu(TextUtils.getText("simplyhtml.formatLabel")); 
	    menu.add(formatMenu);
		if (textfield.getSelectionStart() == textfield.getSelectionEnd()){
			formatMenu.setEnabled(false);
			return menu;
		}
	    formatMenu.add(boldAction);
	    formatMenu.add(italicAction);
	    formatMenu.add(underlineAction);
	    formatMenu.add(redAction);
	    formatMenu.add(greenAction);
	    formatMenu.add(blueAction);
	    formatMenu.add(blackAction);
	    formatMenu.add(defaultColorAction);
	    formatMenu.add(removeFormattingAction);
		return menu;
    }

	/* (non-Javadoc)
	 * @see org.freeplane.view.swing.map.INodeTextField#show()
	 */
	@SuppressWarnings("serial")
    @Override
	public void show(final JFrame frame) {
		final ModeController modeController = Controller.getCurrentModeController();
		final ViewController viewController = modeController.getController().getViewController();
		final MTextController textController = (MTextController) TextController.getController(modeController);
		nodeView = (NodeView) SwingUtilities.getAncestorOfClass(NodeView.class, parent);
		font = parent.getFont();
		zoom = viewController.getZoom();
		if (zoom != 1F) {
			final float fontSize = (int) (Math.rint(font.getSize() * zoom));
			font = font.deriveFont(fontSize);
		}
		textfield.setEditorKit(new HTMLEditorKit(){

			@Override
            public void write(Writer out, Document doc, int pos, int len) throws IOException, BadLocationException {
	            if (doc instanceof HTMLDocument) {
                    HTMLWriter w = new SHTMLWriter(out, (HTMLDocument)doc, pos, len);
                    w.write();
                } else {
                    super.write(out, doc, pos, len);
                }
            }
			
		});

		final InputMap inputMap = textfield.getInputMap();
		final ActionMap actionMap = textfield.getActionMap();
		actionMap.put(DefaultEditorKit.pasteAction, pasteAction);
		
		inputMap.put((KeyStroke) boldAction.getValue(Action.ACCELERATOR_KEY), "boldAction");
		actionMap.put("boldAction",boldAction);
		
		inputMap.put((KeyStroke) italicAction.getValue(Action.ACCELERATOR_KEY), "italicAction");
		actionMap.put("italicAction", italicAction);
		
		inputMap.put((KeyStroke) underlineAction.getValue(Action.ACCELERATOR_KEY), "underlineAction");
		actionMap.put("underlineAction", underlineAction);
		
		inputMap.put((KeyStroke) redAction.getValue(Action.ACCELERATOR_KEY), "redAction");
		actionMap.put("redAction", redAction);
		
		inputMap.put((KeyStroke) greenAction.getValue(Action.ACCELERATOR_KEY), "greenAction");
		actionMap.put("greenAction", greenAction);
		
		inputMap.put((KeyStroke) blueAction.getValue(Action.ACCELERATOR_KEY), "blueAction");
		actionMap.put("blueAction", blueAction);
		
		inputMap.put((KeyStroke) blackAction.getValue(Action.ACCELERATOR_KEY), "blackAction");
		actionMap.put("blackAction", blackAction);
		
		inputMap.put((KeyStroke) defaultColorAction.getValue(Action.ACCELERATOR_KEY), "defaultColorAction");
		actionMap.put("defaultColorAction", defaultColorAction);
		
		inputMap.put((KeyStroke) removeFormattingAction.getValue(Action.ACCELERATOR_KEY), "removeFormattingAction");
		actionMap.put("removeFormattingAction", removeFormattingAction);
		
		final Color nodeTextColor = parent.getForeground();
		textfield.setCaretColor(nodeTextColor);
	    textfield.setBackground(getBackground());
		final StringBuilder ruleBuilder = new StringBuilder(100);
		ruleBuilder.append("body {");
		ruleBuilder.append("font-family: ").append(font.getFamily()).append(";");
		ruleBuilder.append("font-size: ").append(font.getSize()).append("pt;");
		if (font.isItalic()) {
			ruleBuilder.append("font-style: italic; ");
		}
		if (font.isBold()) {
			ruleBuilder.append("font-weight: bold; ");
		}
		ruleBuilder.append("color: ").append(ColorUtils.colorToString(nodeTextColor)).append(";");
		ruleBuilder.append("}\n");
		ruleBuilder.append("p {margin-top:0;}\n");
		final HTMLDocument document = (HTMLDocument) textfield.getDocument();
		final StyleSheet styleSheet = document.getStyleSheet();
		styleSheet.removeStyle("p");
		styleSheet.removeStyle("body");
		styleSheet.addRule(ruleBuilder.toString());
		textfield.setText(text);
		final MapView mapView = (MapView) viewController.getMapView();
		maxWidth = MapStyleModel.getExtension(mapView.getModel()).getMaxNodeWidth();
		maxWidth = mapView.getZoomed(maxWidth) + 1;
		extraWidth = ResourceController.getResourceController().getIntProperty("editor_extra_width", 80);
		extraWidth = mapView.getZoomed(extraWidth);
		final TextFieldListener textFieldListener = new TextFieldListener();
		this.textFieldListener = textFieldListener;
		textfield.addFocusListener(textFieldListener);
		textfield.addKeyListener(textFieldListener);
		textfield.addMouseListener(textFieldListener);
		SpellCheckerController.getController().enableAutoSpell(textfield, true);
		mapView.scrollNodeToVisible(nodeView);
		final int nodeWidth = parent.getWidth();
		final int nodeHeight = parent.getHeight();
		final Dimension textFieldSize;
		textfield.setBorder(new MatteBorder(2, 2, 2, 2, nodeView.getSelectedColor()));
		textFieldSize = textfield.getPreferredSize();
		textFieldSize.width += 1;
        if(textFieldSize.width < extraWidth)
            textFieldSize.width = extraWidth;
        if(textFieldSize.width < 10)
            textFieldSize.width = 10;
		if (textFieldSize.width > maxWidth) {
			textFieldSize.width = maxWidth;
			setLineWrap();
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
		parent.setPreferredSize(new Dimension(textFieldSize.width + horizontalSpace, textFieldSize.height
		        + verticalSpace));
		iconWidth = parent.getIconWidth();
		if (iconWidth != 0) {
			iconWidth += mapView.getZoomed(parent.getIconTextGap());
			horizontalSpace -= iconWidth;
		}

		final int x;
		if(nodeView.isRoot() && parent instanceof MainView) 
		    x= (horizontalSpace + 1) / 2;
		else{
		    final Insets insets = parent.getInsets();
		    x = mapView.getZoomed(insets.left);
		}
		final int y = (verticalSpace + 1) / 2;
		final Point location = new Point(x + iconWidth, y);
		if(! layoutMapOnTextChange)
			UITools.convertPointToAncestor(parent, location, mapView);
		textfield.setBounds(location.x, location.y, textFieldSize.width, textFieldSize.height);
		parent.setText("");
        if(nodeView.isRoot() && parent instanceof MainView)
            parent.setHorizontalAlignment(JLabel.LEFT);
		if(layoutMapOnTextChange)
			parent.add(textfield, 0);
		else
			mapView.add(textfield, 0);
		if (firstEvent instanceof KeyEvent) {
			redispatchKeyEvents(textfield, (KeyEvent) firstEvent);
		}
		else if(firstEvent instanceof MouseEvent){
			final Point point = ((MouseEvent) firstEvent).getPoint();
			point.x -= x + iconWidth;
			point.y -= y;
			textfield.setCaretPosition(textfield.viewToModel(point));;
		}
		else{
			textfield.setCaretPosition(document.getLength());
		}
		document.addDocumentListener(documentListener);
		if(textController.getIsShortened(node)){
			layout();
		}
		textfield.repaint();
		textfield.requestFocusInWindow();
	}
}
