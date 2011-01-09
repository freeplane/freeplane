/**
 * 
 */
package org.freeplane.plugin.script.proxy;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import org.freeplane.core.util.LogUtils;
import org.freeplane.features.common.link.LinkController;
import org.freeplane.features.common.link.NodeLinks;
import org.freeplane.features.common.map.NodeModel;
import org.freeplane.features.mindmapmode.link.MLinkController;
import org.freeplane.plugin.script.ScriptContext;
import org.freeplane.plugin.script.proxy.Proxy.Node;

class LinkProxy extends AbstractProxy<NodeModel> implements Proxy.Link {
	LinkProxy(final NodeModel delegate, final ScriptContext scriptContext) {
		super(delegate, scriptContext);
	}
	
	// LinkRO
	public String getText() {
		final URI link = getUri();
		return link == null ? null : link.toString();
	}
	
	// LinkRO
	public URI getUri() {
		return NodeLinks.getLink(getDelegate());
	}

	// LinkRO
	public File getFile() {
	    final URI link = getUri();
	    try {
	    	return link == null ? null : new File(link);
	    }
	    catch (Exception e) {
			LogUtils.warn("link is not a file uri: " + e);
			return null;
	    }
    }

	// LinkRO
	public Node getNode() {
		final URI uri = getUri();
		if (uri == null)
			return null;
		final String link = uri.toString();
		if (!link.startsWith("#")) {
			LogUtils.warn(link + " is no node id link");
			return null;
		}
		final NodeModel targetNode = getDelegate().getMap().getNodeForID(link.substring(1));
		if (targetNode == null) {
			LogUtils.warn(link + ": node does not exist (anymore?)");
			return null;
		}
		return new NodeProxy(targetNode, getScriptContext());
    }
	
	// LinkRO
	@Deprecated
	public String get() {
		// uses getValidLink() instead of getLink() as in getText()
		final URI link = NodeLinks.getValidLink(getDelegate());
        return link == null ? null : link.toString();
	}

	private MLinkController getLinkController() {
		return (MLinkController) LinkController.getController();
	}
	
	// Link R/W
	public void setText(String target) {
		try {
			if (!removeLinkIfNull(target)) {
				getLinkController().setLink(getDelegate(), new URI(target), false);
			}
		}
		catch (final URISyntaxException e) {
			throw new IllegalArgumentException(e);
		}
	}
	
	// Link R/W
	public void setUri(URI target) {
		if (!removeLinkIfNull(target)) {
			getLinkController().setLink(getDelegate(), target, false);
		}
	}
	
	// Link R/W
	public void setFile(File file) {
		if (!removeLinkIfNull(file)) {
			getLinkController().setLink(getDelegate(), file.toURI(), false);
		}
	}
	
	// Link R/W
	public void setNode(Node node) {
		if (!removeLinkIfNull(node)) {
			if (getModeController().getMapController().getNodeFromID(node.getId()) == null) {
				throw new IllegalArgumentException("target node " + node.toString() + " belongs to a different map");
			}
			setText("#" + node.getId());
		}
	}

	// Link R/W
	@Deprecated
	public boolean set(final String target) {
		try {
			setText(target);
			return true;
        }
        catch (RuntimeException e) {
			LogUtils.warn(e);
			return false;
        }
	}

	private boolean removeLinkIfNull(Object target) {
		if (target == null){
			getLinkController().setLink(getDelegate(), (URI) null, false);
			return true;
		}
	    return false;
    }
}
