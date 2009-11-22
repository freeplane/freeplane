package org.freeplane.view.swing.addins.filepreview;

import java.awt.Dimension;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.model.MapModel;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.url.UrlManager;
import org.freeplane.view.swing.map.MapView;

public class ExternalResource implements IExtension {
	final private Set<JComponent> viewers;
	
	public ExternalResource(){
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
	
	public URI getAbsoluteUri(MapModel map, ModeController modeController) {
        try {
    		UrlManager urlManager = (UrlManager) modeController.getExtension(UrlManager.class);
        	URI absoluteUri = urlManager.getAbsoluteUri(map, uri);
			return absoluteUri;
        }
        catch (MalformedURLException e) {
	        e.printStackTrace();
        }
        return null;
	}

	public void setUri(URI url) {
		this.uri = url;
	}

	private URI uri;
	private float zoom = 1f;
	
	public float getZoom() {
		return zoom;
	}

	public void setZoom(float r) {
		this.zoom = r;
		for(JComponent viewer:viewers){
			IViewerFactory factory = (IViewerFactory) viewer.getClientProperty(IViewerFactory.class);
			MapView mapView = (MapView) SwingUtilities.getAncestorOfClass(MapView.class, viewer);
			Dimension preferredSize = factory.getOriginalSize(viewer);
			preferredSize.width = (int)(preferredSize.width*r);
			preferredSize.height = (int)(preferredSize.height*r);
			preferredSize.width = mapView.getZoomed(preferredSize.width);
			preferredSize.height = mapView.getZoomed(preferredSize.height);
			factory.setViewerSize(viewer, preferredSize);
			viewer.revalidate();
		}
	}

	static ExternalResource getPreviewUrl(NodeModel model){
		return (ExternalResource) model.getExtension(ExternalResource.class);
	}

	static ExternalResource setPreviewUrl(NodeModel model, URI uri, IViewerFactory factory){
		ExternalResource extension = (ExternalResource) model.getExtension(ExternalResource.class);
		if(extension == null){
			extension = new ExternalResource();
			model.addExtension(extension);
		}
		extension.setUri(uri);
		return extension;
	}
}
