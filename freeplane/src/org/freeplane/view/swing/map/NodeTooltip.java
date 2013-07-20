package org.freeplane.view.swing.map;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.net.URI;
import java.net.URL;
import java.security.AccessControlException;

import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.JToolTip;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import org.freeplane.core.ui.MouseInsideListener;
import org.freeplane.core.ui.components.JRestrictedSizeScrollPane;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.ui.components.html.ScaledEditorKit;
import org.freeplane.core.util.Compat;
import org.freeplane.core.util.HtmlUtils;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.url.UrlManager;

@SuppressWarnings("serial")
public class NodeTooltip extends JToolTip {
	class LinkMouseListener extends MouseAdapter implements MouseMotionListener{
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

	    public void mouseClicked(final MouseEvent ev) {
	    	if (Compat.isPlainEvent(ev)) {
	    		final String linkURL = HtmlUtils.getURLOfExistingLink((HTMLDocument) tip.getDocument(), tip.viewToModel(ev.getPoint()));
	    		if (linkURL != null) {
	    			try {
	    				UrlManager.getController().loadURL(new URI(linkURL));
	    			} catch (Exception e) {
	    				LogUtils.warn(e);
	    			}
	    		}
	    	}
	    }

		public void mouseDragged(MouseEvent e) {
        }
    }
	
	final private JEditorPane tip; 
	
	public NodeTooltip(){
		tip  = new JEditorPane();
		tip.setContentType("text/html");
		tip.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, false);
		final HTMLEditorKit kit = ScaledEditorKit.create();
		tip.setEditorKit(kit);
		tip.setEditable(false);
		tip.setMargin(new Insets(0, 0, 0, 0));
		final LinkMouseListener linkMouseListener = new LinkMouseListener();
		tip.addMouseListener(linkMouseListener);
		tip.addMouseMotionListener(linkMouseListener);
		final HTMLDocument document = (HTMLDocument) tip.getDocument();
		final StyleSheet styleSheet = document.getStyleSheet();
		styleSheet.removeStyle("p");
		styleSheet.removeStyle("body");
		styleSheet.addRule("p {margin-top:0;}\n");

		final JRestrictedSizeScrollPane scrollPane = new JRestrictedSizeScrollPane(tip);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, maximumWidth / 2));
		scrollPane.addComponentListener(new ComponentAdapter() {
			@Override
            public void componentResized(ComponentEvent e) {
	            revalidate();
            }
		});
		UITools.setScrollbarIncrement(scrollPane);
		add(scrollPane);
		tip.setOpaque(false);
//		scrollPane.setOpaque(false);
//		scrollPane.getViewport().setOpaque(false);
	}
	
	private static int maximumWidth = Integer.MAX_VALUE;
	/**
	 *  set maximum width
	 *  0 = no maximum width
	 */
	public static void setMaximumWidth(final int width) {
		maximumWidth = width;
	}

	@Override
    public void setTipText(String tipText) {
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
		Dimension preferredSize = tip.getPreferredSize();
		if (preferredSize.width < maximumWidth) {
			tip.setPreferredSize(preferredSize);
			return ;
		}
		final HTMLDocument document = (HTMLDocument) tip.getDocument();
		document.getStyleSheet().addRule("body { width: " + maximumWidth  + "}");
		// bad hack: call "setEditable" only to update view
		tip.setEditable(true);
		tip.setEditable(false);
		tip.setPreferredSize(tip.getPreferredSize());
	}

	@Override
    public Dimension getPreferredSize() {
	    final Component scrollPane = getComponent(0);
		return scrollPane.getPreferredSize();
    }

	@Override
    public void layout() {
		final Component scrollPane = getComponent(0);
		scrollPane.setSize(getPreferredSize());
	    super.layout();
    }

	void scrollUp() {
		tip.scrollRectToVisible(new Rectangle(1, 1));
    }
	
	public void setBase(URL url){
		((HTMLDocument)tip.getDocument()).setBase(url);
	}
}
