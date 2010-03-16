/**
 * 
 */
package org.freeplane.plugin.script.proxy;

import java.net.URI;
import java.net.URISyntaxException;

import org.freeplane.core.model.NodeModel;
import org.freeplane.core.util.LogTool;
import org.freeplane.features.common.link.LinkController;
import org.freeplane.features.common.link.NodeLinks;
import org.freeplane.features.mindmapmode.MModeController;
import org.freeplane.features.mindmapmode.link.MLinkController;

class LinkProxy extends AbstractProxy<NodeModel> implements Proxy.Link {
	LinkProxy(final NodeModel delegate, final MModeController modeController) {
		super(delegate, modeController);
	}

	public String get() {
		final URI link = NodeLinks.getValidLink(getDelegate());
		return link == null ? null : link.toString();
	}

	private MLinkController getLinkController() {
		return (MLinkController) LinkController
				.getController(getModeController());
	}

	public boolean set(final String target) {
		try {
			getLinkController().setLink(getDelegate(), new URI(target), false);
			return true;
		} catch (final URISyntaxException e) {
			LogTool.warn(e);
			return false;
		}
	}
}