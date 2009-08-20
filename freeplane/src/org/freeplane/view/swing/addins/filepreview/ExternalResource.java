package org.freeplane.view.swing.addins.filepreview;

import java.io.File;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JComponent;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.model.NodeModel;

class ExternalResource implements IExtension {
	final private Set<JComponent> viewers;
	
	ExternalResource(){
		viewers = new HashSet<JComponent>();
	}

	void removeViewers() {
		for (final JComponent comp : viewers){
			comp.getParent().remove(comp);
		}
		viewers.clear();
	}

	public Set<JComponent> getViewers() {
		return viewers;
	}

	public URI getUri() {
		return uri;
	}

	public void setUri(URI url, IViewerFactory factory) {
		this.uri = url;
	}

	private URI uri; 
	
	static ExternalResource getPreviewUrl(NodeModel model){
		return (ExternalResource) model.getExtension(ExternalResource.class);
	}

	static ExternalResource setPreviewUrl(NodeModel model, URI uri, IViewerFactory factory){
		ExternalResource extension = (ExternalResource) model.getExtension(ExternalResource.class);
		if(extension == null){
			extension = new ExternalResource();
			model.addExtension(extension);
		}
		extension.setUri(uri, factory);
		return extension;
	}
}
