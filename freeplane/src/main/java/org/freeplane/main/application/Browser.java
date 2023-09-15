package org.freeplane.main.application;

import java.awt.Desktop;
import java.net.URI;

import org.freeplane.core.util.Hyperlink;
import org.freeplane.core.util.LogUtils;

public class Browser {
	public void openDocument(final Hyperlink link) {
		Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
		if (desktop == null || !desktop.isSupported(Desktop.Action.BROWSE)) {
			LogUtils.warn("Opening URI is not supported by the OS");
			return;
		}
		String uriString = link.toString();
		final String UNC_PREFIX = "file:////";
		try {
			URI uri;
			if (uriString.startsWith(UNC_PREFIX)) {
				uriString = "file://" + uriString.substring(UNC_PREFIX.length());
				uri = new URI(uriString);
			}
			else
				uri = link.getUri();
			desktop.browse(uri);
		} catch (Exception e) {
			LogUtils.severe("Can not open URI" + link, e);
		}
	}
}
