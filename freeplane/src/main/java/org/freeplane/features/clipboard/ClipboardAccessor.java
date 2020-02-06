package org.freeplane.features.clipboard;

import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.security.AccessControlException;

import org.freeplane.core.util.LogUtils;

public class ClipboardAccessor{

	private static final ClipboardAccessor INSTANCE = new ClipboardAccessor();
    private Clipboard clipboard;
	private Clipboard selection;
	
	@Deprecated
    public static ClipboardAccessor getController() {
        return getInstance();
    }
	
    public static ClipboardAccessor getInstance() {
        return INSTANCE;
    }
    
	private ClipboardAccessor() {
		super();
		try {
			final Toolkit toolkit = Toolkit.getDefaultToolkit();

			if (!GraphicsEnvironment.isHeadless()) {
				selection = toolkit.getSystemSelection();
				clipboard = toolkit.getSystemClipboard();
			} else {
				selection = null;
				clipboard = null;
			}
		}
		catch (final AccessControlException e) {
			LogUtils.warn("can not access system clipboard, clipboard controller disabled");
		}
	}

	/**
	 */
	public Transferable getClipboardContents() {
	    if (clipboard != null) {
	        try {
	            return clipboard.getContents(this);
	        }
	        catch (IllegalStateException | NullPointerException e) {
	            LogUtils.warn("can not access clipboard contents");
	        }
	    }
	    return null;
	}

	/**
	 */
	public void setClipboardContents(final Transferable t) {
		if (clipboard != null) {
			clipboard.setContents(t, null);
		}
		if (selection != null) {
			selection.setContents(t, null);
		}
	}

    /** copies a string to the system clipboard. */
    public void setClipboardContents(final String string) {
        setClipboardContents(new StringSelection(string));
    }

    /** copies a string to the system clipboard with text/html mimetype. */
    public void setClipboardContentsToHtml(final String html) {
    	setClipboardContents(new HtmlSelection(html));
    }
}
