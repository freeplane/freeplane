package org.docear.plugin.core.actions;

import java.awt.event.ActionEvent;
import java.net.MalformedURLException;
import java.net.URL;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.mode.Controller;

public class DocearOpenUrlAction extends AFreeplaneAction {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String url;
	
	public DocearOpenUrlAction(final String key, final String url) {
		super(key);
		this.url = url;
	}

	public void actionPerformed(ActionEvent e) {
		try {
			Controller.getCurrentController().getViewController().openDocument(new URL(url));
		}
		catch (final MalformedURLException ex) {
			UITools.errorMessage(TextUtils.getText("url_error") + "\n" + ex);
			LogUtils.warn(ex);
		}
		catch (final Exception ex) {
			UITools.errorMessage(ex);
			LogUtils.warn(ex);
		}

	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

}
