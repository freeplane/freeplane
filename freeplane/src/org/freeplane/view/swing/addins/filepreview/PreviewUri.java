package org.freeplane.view.swing.addins.filepreview;

import java.io.File;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JComponent;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.model.NodeModel;

class PreviewUri implements IExtension {
	final private Set<JComponent> viewers;
	
	PreviewUri(){
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

	public void setUri(URI url, IPreviewComponentFactory factory) {
		this.uri = url;
	}

	private URI uri; 
	
	static PreviewUri getPreviewUrl(NodeModel model){
		return (PreviewUri) model.getExtension(PreviewUri.class);
	}

	static PreviewUri setPreviewUrl(NodeModel model, URI uri, IPreviewComponentFactory factory){
		PreviewUri extension = (PreviewUri) model.getExtension(PreviewUri.class);
		if(extension == null){
			extension = new PreviewUri();
			model.addExtension(extension);
		}
		extension.setUri(uri, factory);
		return extension;
	}
}
