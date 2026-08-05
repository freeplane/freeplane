/*
 * Created on 16 May 2025
 *
 * author dimitry
 */
package org.freeplane.view.swing.map;

import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.function.Supplier;
import java.util.function.Predicate;

import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.html.HTMLDocument;

import org.freeplane.core.util.Compat;
import org.freeplane.core.util.HtmlUtils;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;

public class LinkOpener extends MouseAdapter implements MouseMotionListener{
	private final Supplier<NodeModel> nodeSupplier;
	private final Predicate<String> customLinkOpener;

    public LinkOpener(Supplier<NodeModel> nodeSupplier) {
		this(nodeSupplier, link -> false);
	}

    public LinkOpener(Supplier<NodeModel> nodeSupplier, Predicate<String> customLinkOpener) {
		super();
		this.nodeSupplier = nodeSupplier;
		this.customLinkOpener = customLinkOpener;
	}

	@Override
	public void mouseMoved(final MouseEvent ev) {
    	JTextComponent textComponent = (JTextComponent) ev.getComponent();
    	final Document document = textComponent.getDocument();
    	if(!(document instanceof HTMLDocument) || nodeSupplier.get() == null)
    		return;
		final String link = HtmlUtils.getURLOfExistingLink((HTMLDocument) document, textComponent.viewToModel(ev.getPoint()));
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
        if (textComponent.getCursor().getType() != requiredCursor) {
        	textComponent.setCursor(requiredCursor != Cursor.DEFAULT_CURSOR ? new Cursor(requiredCursor) : null);
        }
    }

    @Override
	public void mouseClicked(final MouseEvent ev) {
    	if (Compat.isPlainEvent(ev)) {
    		JTextComponent textComponent = (JTextComponent) ev.getComponent();
    		final Document document = textComponent.getDocument();
        	if(!(document instanceof HTMLDocument) || nodeSupplier.get() == null)
        		return;
			final String linkURL = HtmlUtils.getURLOfExistingLink((HTMLDocument) document, textComponent.viewToModel(ev.getPoint()));
			if (linkURL != null) {
				try {
					if (customLinkOpener.test(linkURL)) return;
					final NodeModel node = nodeSupplier.get();
					LinkController.getController().loadURI(node, LinkController.createHyperlink(linkURL));
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
