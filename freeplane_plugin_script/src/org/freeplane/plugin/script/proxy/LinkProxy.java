/**
 * 
 */
package org.freeplane.plugin.script.proxy;

import java.net.URI;
import java.net.URISyntaxException;

import org.freeplane.core.util.LogUtils;
import org.freeplane.features.common.link.LinkController;
import org.freeplane.features.common.link.NodeLinks;
import org.freeplane.features.common.map.NodeModel;
import org.freeplane.features.mindmapmode.link.MLinkController;
import org.freeplane.plugin.script.ScriptContext;

class LinkProxy extends AbstractProxy<NodeModel> implements Proxy.Link {
	LinkProxy(final NodeModel delegate, final ScriptContext scriptContext) {
		super(delegate, scriptContext);
	}

	public String get() {
		final URI link = NodeLinks.getValidLink(getDelegate());
		return link == null ? null : link.toString();
	}

	private MLinkController getLinkController() {
		return (MLinkController) LinkController.getController();
	}

	public boolean set(final String target) {
		try {
			getLinkController().setLink(getDelegate(), new URI(target), false);
			return true;
		}
		catch (final URISyntaxException e) {
			LogUtils.warn(e);
			return false;
		}
	}
}
