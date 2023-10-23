package org.freeplane.view.swing.map;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.net.URL;
import java.security.AccessControlException;

import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import org.freeplane.core.ui.components.JRestrictedSizeScrollPane;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.ui.components.html.SynchronousScaledEditorKit;
import org.freeplane.core.util.Compat;
import org.freeplane.core.util.HtmlUtils;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.mode.Controller;

class TextualTooltipRendererFactory {
	private class LinkMouseListener extends MouseAdapter implements MouseMotionListener{
	    @Override
		public void mouseMoved(final MouseEvent ev) {
	    	final String link = HtmlUtils.getURLOfExistingLink((HTMLDocument) tip.getDocument(), tip.viewToModel(ev.getPoint()));
	    	boolean followLink = link != null;
	    	Controller currentController = Controller.getCurrentController();
	        final int requiredCursor;
	        if(followLink){
	    		currentController.getViewController().out(link);
	    		requiredCursor = Cursor.HAND_CURSOR;
	        }
	        else{
	        	requiredCursor = Cursor.DEFAULT_CURSOR;
	        }
	        if (tip.getCursor().getType() != requiredCursor) {
	        	tip.setCursor(requiredCursor != Cursor.DEFAULT_CURSOR ? new Cursor(requiredCursor) : null);
	        }
	    }

	    @Override
		public void mouseClicked(final MouseEvent ev) {
	    	if (Compat.isPlainEvent(ev)) {
	    		final String linkURL = HtmlUtils.getURLOfExistingLink((HTMLDocument) tip.getDocument(), tip.viewToModel(ev.getPoint()));
	    		if (linkURL != null) {
	    			try {
	    				NodeView nodeView = (NodeView) SwingUtilities.getAncestorOfClass(NodeView.class, component);
	    				LinkController.getController().loadURI(nodeView.getNode(), LinkController.createHyperlink(linkURL));
	    			} catch (Exception e) {
	    				LogUtils.warn(e);
	    			}
	    		}
	    	}
	    }

		@Override
		public void mouseDragged(MouseEvent e) {
        }
    }

	final private JEditorPane tip;
	private int maximumWidth;
	private final String contentType;
	private final JRestrictedSizeScrollPane scrollPane;
	private JComponent component;
	private URL baseUrl;
	TextualTooltipRendererFactory(GraphicsConfiguration graphicsConfiguration, String contentType, URL baseUrl, String tipText, JComponent component, Dimension tooltipSize){
		this.contentType = contentType;
		this.baseUrl = baseUrl;
		this.component = component;
		tip  = new JEditorPane();
		tip.setContentType(contentType);
		tip.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, false);
		if(contentType.equals(FreeplaneTooltip.TEXT_HTML)) {
			final HTMLEditorKit kit = SynchronousScaledEditorKit.create();
			tip.setEditorKit(kit);
			final HTMLDocument document = (HTMLDocument) tip.getDocument();
			document.setPreservesUnknownTags(false);
			final StyleSheet styleSheet = document.getStyleSheet();
			styleSheet.addRule("p {margin-top:0;}");
			styleSheet.addRule("table {border: 0; border-spacing: 0;}");
			styleSheet.addRule("th, td {border: 1px solid;}");
		}
		tip.setEditable(false);
		tip.setMargin(new Insets(0, 0, 0, 0));
		final LinkMouseListener linkMouseListener = new LinkMouseListener();
		tip.addMouseListener(linkMouseListener);
		tip.addMouseMotionListener(linkMouseListener);

		scrollPane = new JRestrictedSizeScrollPane(tip);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		final int scrollBarWidth = scrollPane.getVerticalScrollBar().getPreferredSize().width;
		tooltipSize.width -= scrollBarWidth;
		scrollPane.setMaximumSize(tooltipSize);
		maximumWidth = tooltipSize.width;
		UITools.setScrollbarIncrement(scrollPane);
		tip.setOpaque(true);
		scrollPane.addComponentListener(new ComponentAdapter() {

			@Override
            public void componentResized(ComponentEvent e) {
				scrollUp();
				scrollPane.removeComponentListener(this);
            }

		});
		setTipText(tipText);

	}

    private void setTipText(String tipText) {
		try{
        	setTipTextUnsafe(tipText);
        }
        catch (Exception e1) {
        	if(e1 instanceof AccessControlException)
        		LogUtils.warn(e1.getMessage());
        	else
        		LogUtils.severe(e1);
            final String localizedMessage = e1.getLocalizedMessage();
        	final String htmlEscapedText = HtmlUtils.plainToHTML(localizedMessage + '\n' + tipText);
        	try{
        		setTipTextUnsafe(htmlEscapedText);
        	}
        	catch (Exception e2){
        	}
        }
    }

	private void setTipTextUnsafe(String tipText) throws Exception{
		tip.setSize(0, 0);
		tip.setPreferredSize(null);
		tip.setText(tipText);
		((HTMLDocument)tip.getDocument()).setBase(baseUrl);
		Dimension preferredSize = tip.getPreferredSize();
		if (preferredSize.width > maximumWidth && contentType.equals(FreeplaneTooltip.TEXT_HTML)) {
			final HTMLDocument document = (HTMLDocument) tip.getDocument();
			document.getStyleSheet().addRule("body { width: " + maximumWidth  + "}");
			// bad hack: call "setEditable" only to update view
			tip.setEditable(true);
			tip.setEditable(false);
			preferredSize = tip.getPreferredSize();
			if (preferredSize.width > maximumWidth) {

			}
		}
		tip.setSize(preferredSize);
		preferredSize = tip.getPreferredSize();
		tip.setPreferredSize(preferredSize);

	}

	JComponent getTooltipRenderer() {
		return scrollPane;
	}
	private void scrollUp() {
		tip.scrollRectToVisible(new Rectangle(1, 1));
    }

}
