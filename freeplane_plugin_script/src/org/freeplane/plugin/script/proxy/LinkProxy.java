/**
 * 
 */
package org.freeplane.plugin.script.proxy;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import org.freeplane.core.util.LogUtils;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.link.NodeLinks;
import org.freeplane.features.link.mindmapmode.MLinkController;
import org.freeplane.features.map.NodeModel;
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
	    URI link = getUri();
	    try {
	        if (link == null)
	            return null;
	        if (!link.isAbsolute() && isFileUri(link)) {
	            final File mapFile = getDelegate().getMap().getFile();
	            return mapFile == null ? null : new File(mapFile.getParent(), link.getPath());
	        }
	    	return new File(link);
	    }
	    catch (Exception e) {
			LogUtils.warn("link is not a file uri: " + e);
			return null;
	    }
    }

    private boolean isFileUri(URI link) {
        return link.getScheme() == null || link.getScheme().equals("file");
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
				getLinkController().setLink(getDelegate(), new URI(target), LinkController.LINK_ABSOLUTE);
			}
		}
		catch (final URISyntaxException e) {
			throw new IllegalArgumentException(e);
		}
	}
	
	// Link R/W
	public void setUri(URI target) {
		if (!removeLinkIfNull(target)) {
			getLinkController().setLink(getDelegate(), target, LinkController.LINK_ABSOLUTE);
		}
	}
	
	// Link R/W
	public void setFile(File file) {
		if (!removeLinkIfNull(file)) {
			getLinkController().setLink(getDelegate(), file.toURI(), LinkController.LINK_ABSOLUTE);
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
			getLinkController().setLink(getDelegate(), (URI) null, LinkController.LINK_ABSOLUTE);
			return true;
		}
	    return false;
    }

    /** make <code>if (node.link) println "has link"</code> work. */
    public boolean asBoolean() {
        return getUri() != null;
    }

    @Override
    public boolean remove() {
        return removeLinkIfNull(null);
    }
}
