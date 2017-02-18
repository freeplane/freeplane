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
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputMethodEvent;
import java.awt.event.InputMethodListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.text.AttributedCharacterIterator;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.Icon;
import javax.swing.InputMap;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.RootPaneContainer;
import javax.swing.SwingConstants;
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
import javax.swing.text.Position.Bias;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledEditorKit;
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
import org.freeplane.core.ui.components.html.CssRuleBuilder;
import org.freeplane.core.ui.components.html.ScaledEditorKit;
import org.freeplane.core.util.Compat;
import org.freeplane.core.util.HtmlUtils;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.nodestyle.NodeStyleController;
import org.freeplane.features.spellchecker.mindmapmode.SpellCheckerController;
import org.freeplane.features.text.TextController;
import org.freeplane.features.text.mindmapmode.EditNodeBase;
import org.freeplane.features.text.mindmapmode.EventBuffer;
import org.freeplane.features.text.mindmapmode.MTextController;
import org.freeplane.features.ui.IMapViewChangeListener;
import org.freeplane.features.ui.IMapViewManager;
import org.freeplane.view.swing.map.MapView;
import org.freeplane.view.swing.map.NodeView;
import org.freeplane.view.swing.map.ZoomableLabel;
import org.freeplane.view.swing.map.ZoomableLabelUI;
import org.freeplane.view.swing.map.ZoomableLabelUI.LayoutData;

import com.lightdev.app.shtm.SHTMLWriter;


/**
 * @author foltin
 */
public class EditNodeTextField extends EditNodeBase {
    private class MyNavigationFilter extends NavigationFilter {
    	private final JEditorPane textfield;
        public MyNavigationFilter(JEditorPane textfield) {
	        this.textfield = textfield;
        }

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

        private int getValidPosition(int position) {
        	final HTMLDocument doc = (HTMLDocument) textfield.getDocument();
        	if (doc.getDefaultRootElement().getElementCount() > 1) {
        		final int startPos = doc.getDefaultRootElement().getElement(1).getStartOffset();
        		final int validPosition = Math.max(position, startPos);
        		return validPosition;
        	}
        	return position;
        }
    }
    
	private static class InputMethodInUseListener implements InputMethodListener {
		private boolean imeInUse = false;

		public void inputMethodTextChanged(InputMethodEvent event) {
			updateImeInUseState(event);
		}

		public void caretPositionChanged(InputMethodEvent event) {
			updateImeInUseState(event);
		}

		public boolean isIMEInUse(){
			return imeInUse;
		}
		
		private void updateImeInUseState(InputMethodEvent event) {
	        AttributedCharacterIterator aci = event.getText();
			if(aci != null) {
				int inputLen = aci.getEndIndex() - aci.getBeginIndex();
				int committedLen = event.getCommittedCharacterCount();
				imeInUse = inputLen > 0 && inputLen != committedLen;
			}
            else
	            imeInUse = false;
        }

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
			parent.setPreferredSize(new Dimension(preferredSize.width + horizontalSpace , preferredSize.height + verticalSpace));
		textfield.revalidate();
		final NodeView nodeView = (NodeView) SwingUtilities.getAncestorOfClass(NodeView.class, parent);
		final MapView mapView = (MapView) SwingUtilities.getAncestorOfClass(MapView.class, nodeView);
		if(mapView == null)
			return;
		if(layoutMapOnTextChange)
			mapView.scrollNodeToVisible(nodeView);
		else
			mapView.scrollRectToVisible(textfield.getBounds());
	}

	private void setLineWrap() {
		if(null != textfield.getClientProperty("EditNodeTextField.linewrap") || inputMethodInUseListener.isIMEInUse()){
			return;
		}
		
	    final HTMLDocument document = (HTMLDocument) textfield.getDocument();
	    document.getStyleSheet().addRule("body { width: " + (maxWidth - 1) + "}");
	    // bad hack: call "setEditable" only to update view
	    textfield.setEditable(false);
	    textfield.setEditable(true);
	    textfield.putClientProperty("EditNodeTextField.linewrap", true);
    }

	private static final int SPLIT_KEY_CODE;
	static {
		String rawLabel = TextUtils.getRawText("split");
		final int mnemoSignIndex = rawLabel.indexOf('&');
		if (mnemoSignIndex >= 0 && mnemoSignIndex + 1 < rawLabel.length()) {
			final char charAfterMnemoSign = rawLabel.charAt(mnemoSignIndex + 1);
			if (charAfterMnemoSign != ' ') {
				SPLIT_KEY_CODE = charAfterMnemoSign;
			}
			else SPLIT_KEY_CODE = -1;
		}
		else SPLIT_KEY_CODE = -1;
	}
	private class TextFieldListener implements KeyListener, FocusListener, MouseListener {
		private static final int KEYSTROKE_MODIFIERS = KeyEvent.ALT_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK | KeyEvent.CTRL_DOWN_MASK | KeyEvent.META_DOWN_MASK;
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
			submitText();
			hideMe();
		}

		private void submitText() {
	        submitText(getNewText());
        }

		private void submitText(final String output) {
			getEditControl().ok(output);
        }

		public void keyPressed(final KeyEvent e) {
			if (eventSource == CANCEL||textfield==null) {
				return;
			}
			final int keyCode = e.getKeyCode();
			switch (keyCode) {
				case KeyEvent.VK_ESCAPE:
					if (e.isControlDown() || e.isMetaDown())
						break;
					eventSource = CANCEL;
					hideMe();
					getEditControl().cancel();
					nodeView.requestFocusInWindow();
					e.consume();
					break;
				case KeyEvent.VK_ENTER: {
					if (e.isControlDown() || e.isMetaDown())
						break;
					final boolean enterConfirms = ResourceController.getResourceController().getBooleanProperty("el__enter_confirms_by_default");
					if (enterConfirms == e.isAltDown() || e.isShiftDown()) {
						e.consume();
						final Component component = e.getComponent();
						final KeyEvent keyEvent = new KeyEvent(component, e.getID(), e.getWhen(), 0, keyCode, e
						    .getKeyChar(), e.getKeyLocation());
						SwingUtilities.processKeyBindings(keyEvent);
						break;
					}
					final String output = getNewText();
					e.consume();
					eventSource = CANCEL;
					hideMe();
					submitText(output);
					nodeView.requestFocusInWindow();
				}
				break;
				case KeyEvent.VK_TAB:
					if (e.isControlDown() || e.isMetaDown())
						break;
					textfield.replaceSelection("    ");
					e.consume();
					break;
				case KeyEvent.VK_SPACE:
					if (e.isControlDown() || e.isMetaDown())
						break;
					e.consume();
					break;
				default:
					if(isSplitActionTriggered(e) && getEditControl().canSplit()){
						eventSource = CANCEL;
						final String output = getNewText();
						final int caretPosition = textfield.getCaretPosition();
						hideMe();
						getEditControl().split(output, caretPosition);
						nodeView.requestFocusInWindow();
						e.consume();
					}
					break;
			}
		}

		protected boolean isSplitActionTriggered(final KeyEvent e) {
			final int keyCode = e.getKeyCode();
			if (keyCode == SPLIT_KEY_CODE && keyCode != -1 && e.isAltDown()&& !e.isAltGraphDown()&& !e.isControlDown() && ! Compat.isMacOsX())
				return true;
			final KeyStroke splitNodeHotKey = ResourceController.getResourceController().getAcceleratorManager().getAccelerator("SplitNode");
			return splitNodeHotKey != null && splitNodeHotKey.getKeyCode() == keyCode  &&  
					(e.getModifiersEx() & KEYSTROKE_MODIFIERS)  == (splitNodeHotKey.getModifiers() & KEYSTROKE_MODIFIERS);
		}

		public void keyReleased(final KeyEvent e) {
		}

		public void keyTyped(final KeyEvent e) {
		}

		public void mouseClicked(final MouseEvent ev) {
			if (textfield != null && (ev.getModifiers() & MouseEvent.CTRL_MASK) != 0) {
				final String linkURL = HtmlUtils.getURLOfExistingLink((HTMLDocument) textfield.getDocument(), textfield.viewToModel(ev.getPoint()));
				if (linkURL != null) {
					try {
						LinkController.getController().loadURI(nodeView.getModel(), new URI(linkURL));
					} catch (Exception e) {
						LogUtils.warn(e);
					}
				}
			}
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
	
	private class MapViewChangeListener implements IMapViewChangeListener{
		public void afterViewChange(Component oldView, Component newView) {
        }

		public void afterViewClose(Component oldView) {
        }

		public void afterViewCreated(Component mapView) {
        }

		public void beforeViewChange(Component oldView, Component newView) {
			final String output = getNewText();
			hideMe();
			getEditControl().ok(output);
        }
	}
	
	private JEditorPane textfield;
	final private InputMethodInUseListener inputMethodInUseListener;
	private final DocumentListener documentListener;
	private int maxWidth;

	@SuppressWarnings("serial")
    public EditNodeTextField(final NodeModel node, final ZoomableLabel parent, final String text, final IEditControl editControl) {
		super(node, text, editControl);
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
		
		greenAction = new ForegroundAction(TextUtils.getText("green"), new Color(0, 0x80, 0));
		greenAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control G"));
		
		blueAction = new ForegroundAction(TextUtils.getText("blue"), new Color(0, 0, 0xc0));
		blueAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control L"));
		
		blackAction = new ForegroundAction(TextUtils.getText("black"), Color.BLACK);
		blackAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control K"));
		
		defaultColorAction = new ExtendedEditorKit.RemoveStyleAttributeAction(StyleConstants.Foreground, TextUtils.getText("DefaultColorAction.text"));
		defaultColorAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control D"));
		
		removeFormattingAction = new ExtendedEditorKit.RemoveStyleAttributeAction(null, TextUtils.getText("simplyhtml.clearFormatLabel"));
		removeFormattingAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control T"));
		
		inputMethodInUseListener = new InputMethodInUseListener();
		if(editControl != null ){
			final ModeController modeController = Controller.getCurrentModeController();
			final MTextController textController = (MTextController) TextController.getController(modeController);
			textfield = textController.createEditorPane(MTextController.NODE_TEXT);
			textfield.setNavigationFilter(new MyNavigationFilter(textfield));
			textfield.addInputMethodListener(inputMethodInUseListener);
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
		final JEditorPane textfield = this.textfield;
		this.textfield = null;
		textfield.getDocument().removeDocumentListener(documentListener);
		final IMapViewManager mapViewManager = Controller.getCurrentController().getMapViewManager();
		mapViewManager.removeMapViewChangeListener(mapViewChangeListener);
		mapViewChangeListener = null;
		parent.setPreferredSize(null);
		if(SwingUtilities.getAncestorOfClass(MapView.class, nodeView) != null) {
			nodeView.update();
			keepNodePosition();
		}
		final Dimension textFieldSize = textfield.getSize();
		final Point textFieldCoordinate = new Point();
		final MapView mapView = nodeView.getMap();
		UITools.convertPointToAncestor(textfield, textFieldCoordinate, mapView);
		textfield.getParent().remove(textfield);
		parent.revalidate();
		parent.repaint();
		mapView.repaint(textFieldCoordinate.x, textFieldCoordinate.y, textFieldSize.width, textFieldSize.height);
	}

	private final ZoomableLabel parent;
	private NodeView nodeView;
	private Font font;
	private float zoom;
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
	private int verticalSpace;
	private int horizontalSpace;
	private MapViewChangeListener mapViewChangeListener;

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
	public void show(final RootPaneContainer frame) {
		final ModeController modeController = Controller.getCurrentModeController();
		final IMapViewManager viewController = modeController.getController().getMapViewManager();
		final MTextController textController = (MTextController) TextController.getController(modeController);
		nodeView = (NodeView) SwingUtilities.getAncestorOfClass(NodeView.class, parent);
		font = parent.getFont();
		zoom = viewController.getZoom();
		if (zoom != 1F) {
			final float fontSize = (int) (Math.rint(font.getSize() * zoom));
			font = font.deriveFont(fontSize);
		}
		final HTMLEditorKit kit = new ScaledEditorKit(){
			@Override
			public void write(Writer out, Document doc, int pos, int len) throws IOException, BadLocationException {
				if (doc instanceof HTMLDocument) {
					HTMLWriter w = new SHTMLWriter(out, (HTMLDocument) doc, pos, len);
					w.write();
				}
				else {
					super.write(out, doc, pos, len);
				}
			}
		};
		textfield.setEditorKit(kit);

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
		final StringBuilder ruleBuilder = new StringBuilder(100);
		ruleBuilder.append("body {");
		final int labelHorizontalAlignment = parent.getHorizontalAlignment();
		ruleBuilder.append(new CssRuleBuilder()
				.withCSSFont(font, UITools.FONT_SCALE_FACTOR)
				.withColor(nodeTextColor)
				.withBackground(getBackground())
				.withAlignment(labelHorizontalAlignment));
		ruleBuilder.append("}\n");
		final HTMLDocument document = (HTMLDocument) textfield.getDocument();
		final StyleSheet styleSheet = document.getStyleSheet();
		styleSheet.addRule(ruleBuilder.toString());
		textfield.setText(text);
		final MapView mapView = nodeView.getMap();
		if(! mapView.isValid())
			mapView.validate();
		final NodeStyleController nsc = NodeStyleController.getController(modeController);
		maxWidth = Math.max(mapView.getZoomed(nsc.getMaxWidth(node).toBaseUnitsRounded()), parent.getWidth());
		final Icon icon = parent.getIcon();
		if(icon != null){
			maxWidth -= mapView.getZoomed(icon.getIconWidth());
			maxWidth -= mapView.getZoomed(parent.getIconTextGap());
		}
		Insets parentInsets = parent.getZoomedInsets();
		maxWidth -= parentInsets.left + parentInsets.right;
		maxWidth = mapView.getZoomed(maxWidth);
		extraWidth = ResourceController.getResourceController().getIntProperty("editor_extra_width", 80);
		extraWidth = mapView.getZoomed(extraWidth);
		final TextFieldListener textFieldListener = new TextFieldListener();
		this.textFieldListener = textFieldListener;
		textfield.addFocusListener(textFieldListener);
		textfield.addKeyListener(textFieldListener);
		textfield.addMouseListener(textFieldListener);
		mapViewChangeListener = new MapViewChangeListener();
		Controller.getCurrentController().getMapViewManager().addMapViewChangeListener(mapViewChangeListener);
		SpellCheckerController.getController().enableAutoSpell(textfield, true);
		mapView.scrollNodeToVisible(nodeView);
		assert( parent.isValid());
		final int nodeWidth = parent.getWidth();
		final int textFieldBorderWidth = 2;
		textfield.setBorder(new MatteBorder(textFieldBorderWidth, textFieldBorderWidth, textFieldBorderWidth, textFieldBorderWidth, nodeView.getSelectedColor()));
		final Dimension textFieldMinimumSize = textfield.getPreferredSize();
		textFieldMinimumSize.width += 1;
        if(textFieldMinimumSize.width < extraWidth)
            textFieldMinimumSize.width = extraWidth;
        if(textFieldMinimumSize.width < 10)
            textFieldMinimumSize.width = 10;
		if (textFieldMinimumSize.width > maxWidth) {
			textFieldMinimumSize.width = maxWidth;
			setLineWrap();
			textFieldMinimumSize.height = textfield.getPreferredSize().height;
		}
		final ZoomableLabelUI parentUI = (ZoomableLabelUI)parent.getUI();
		final LayoutData layoutData = parentUI.getLayoutData(parent);
		Rectangle iconR = layoutData.iconR;
		final Rectangle textR = layoutData.textR;
		int textFieldX = parentInsets.left - textFieldBorderWidth + (iconR.width > 0 ? textR.x - iconR.x : 0);
		
		
		final EventBuffer eventQueue = MTextController.getController().getEventQueue();
		KeyEvent firstEvent = eventQueue.getFirstEvent();
		
		Point mouseEventPoint = null;
		if (firstEvent == null) {
			MouseEvent currentEvent = eventQueue.getMouseEvent();
			if(currentEvent != null){
				MouseEvent mouseEvent = (MouseEvent) currentEvent;
				if(mouseEvent.getComponent().equals(parent)){
					mouseEventPoint = mouseEvent.getPoint();
					mouseEventPoint.x -= textR.x;
					mouseEventPoint.y -= textR.y;
				}
			}
		}

		
		textFieldMinimumSize.width = Math.max(textFieldMinimumSize.width, nodeWidth - textFieldX - (parentInsets.right - textFieldBorderWidth));
		textFieldMinimumSize.height = Math.max(textFieldMinimumSize.height, textR.height);
		textfield.setSize(textFieldMinimumSize.width, textFieldMinimumSize.height);
		final int textY = Math.max(textR.y - (textFieldMinimumSize.height - textR.height) / 2, 0);
		final Dimension newParentSize = new Dimension(textFieldX + textFieldMinimumSize.width + parentInsets.right,  2 * textY + textFieldMinimumSize.height);
		horizontalSpace = newParentSize.width - textFieldMinimumSize.width;
		verticalSpace = 2 * textY;
		final int widthAddedToParent = newParentSize.width - parent.getWidth();
		final Point location = new Point(textR.x - textFieldBorderWidth, textY);
		
		final int widthAddedToTextField = textFieldMinimumSize.width - (textR.width + 2 * textFieldBorderWidth);
		if(widthAddedToTextField > 0){
			switch(labelHorizontalAlignment){
			case SwingConstants.CENTER:
				location.x -= (widthAddedToTextField - widthAddedToParent) / 2;
				if(mouseEventPoint != null)
					mouseEventPoint.x += widthAddedToTextField / 2;
				break;
			case SwingConstants.RIGHT:
				location.x -= widthAddedToTextField - widthAddedToParent;
				if(mouseEventPoint != null)
					mouseEventPoint.x += widthAddedToTextField;
				break;
			}
		}
		
        keepNodePosition();        
		parent.setPreferredSize(newParentSize);
		parent.setText("");
        parent.setHorizontalAlignment(JLabel.LEFT);

		if(! layoutMapOnTextChange) {
			mapView.doLayout();
			UITools.convertPointToAncestor(parent, location, mapView);
		}
		
		textfield.setBounds(location.x, location.y, textFieldMinimumSize.width, textFieldMinimumSize.height);
		if(layoutMapOnTextChange)
			parent.add(textfield, 0);
		else
			mapView.add(textfield, 0);
		
		redispatchKeyEvents(textfield, firstEvent);
		if (firstEvent == null) {
			int pos = document.getLength();
			if(mouseEventPoint != null)
				pos = textfield.viewToModel(mouseEventPoint);
			textfield.setCaretPosition(pos);
		}
		document.addDocumentListener(documentListener);
		if(textController.isMinimized(node)){
			layout();
		}
		textfield.repaint();
		textfield.requestFocusInWindow();
	}

	private void keepNodePosition() {
		nodeView.getMap().keepNodePosition(nodeView, 0 , 0);
	}
}
