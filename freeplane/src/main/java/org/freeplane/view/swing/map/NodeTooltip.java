package org.freeplane.view.swing.map;

import java.awt.Component;
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
import java.net.URI;
import java.net.URL;
import java.security.AccessControlException;

import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.JToolTip;
import javax.swing.SwingUtilities;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.components.JRestrictedSizeScrollPane;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.ui.components.html.SynchronousScaledEditorKit;
import org.freeplane.core.util.Compat;
import org.freeplane.core.util.HtmlUtils;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.mode.Controller;

@SuppressWarnings("serial")
public class NodeTooltip extends JToolTip {
	class LinkMouseListener extends MouseAdapter implements MouseMotionListener{
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
	    				NodeView nodeView = (NodeView) SwingUtilities.getAncestorOfClass(NodeView.class, getComponent());
	    				LinkController.getController().loadURI(nodeView.getModel(), new URI(linkURL));
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

	public NodeTooltip(GraphicsConfiguration graphicsConfiguration){
		tip  = new JEditorPane();
		tip.setContentType("text/html");
		tip.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, false);
		final HTMLEditorKit kit = SynchronousScaledEditorKit.create();
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
		final Rectangle screenBounds = graphicsConfiguration.getBounds();
		final int screenHeigth = screenBounds.height - 80;
		final int screenWidth = screenBounds.width - 80;
		final int maximumHeight = Math.min(screenHeigth, getIntProperty("toolTipManager.max_tooltip_height"));
		final int scrollBarWidth = scrollPane.getVerticalScrollBar().getPreferredSize().width;
		scrollPane.setMaximumSize(new Dimension(screenWidth, maximumHeight));
		maximumWidth = Math.min(screenWidth, getIntProperty("toolTipManager.max_tooltip_width")) - scrollBarWidth;
		UITools.setScrollbarIncrement(scrollPane);
		add(scrollPane);
		tip.setOpaque(true);
		addComponentListener(new ComponentAdapter() {

			@Override
            public void componentResized(ComponentEvent e) {
				final NodeTooltip component = (NodeTooltip) e.getComponent();
				component.scrollUp();
				component.removeComponentListener(this);
            }

		});
	}

	private int getIntProperty(String propertyName) {
		return ResourceController.getResourceController().getIntProperty(propertyName, Integer.MAX_VALUE);
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
		if (preferredSize.width > maximumWidth) {
			final HTMLDocument document = (HTMLDocument) tip.getDocument();
			document.getStyleSheet().addRule("body { width: " + maximumWidth  + "}");
			// bad hack: call "setEditable" only to update view
			tip.setEditable(true);
			tip.setEditable(false);
			preferredSize = tip.getPreferredSize();
		}
		tip.setSize(preferredSize);
		preferredSize = tip.getPreferredSize();
		tip.setPreferredSize(preferredSize);

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
