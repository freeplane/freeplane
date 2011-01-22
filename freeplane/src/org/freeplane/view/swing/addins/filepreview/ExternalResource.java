package org.freeplane.view.swing.addins.filepreview;

import java.awt.Dimension;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.extension.IExtension;
import org.freeplane.features.common.map.MapModel;
import org.freeplane.features.common.map.NodeModel;
import org.freeplane.features.common.url.UrlManager;
import org.freeplane.view.swing.map.MapView;
import org.freeplane.view.swing.map.NodeView;

public class ExternalResource implements IExtension {
	final private Set<NodeView> viewers;

	public ExternalResource() {
		viewers = new HashSet<NodeView>();
	}

	void removeViewers() {
		for (final NodeView nodeView : viewers) {
			nodeView.removeContent(ViewerController.VIEWER_POSITION);
		}
		viewers.clear();
	}

	public Set<NodeView> getViewers() {
		return viewers;
	}

	public URI getUri() {
		return uri;
	}

	public URI getAbsoluteUri(final MapModel map) {
		try {
			final UrlManager urlManager = (UrlManager) Controller.getCurrentModeController().getExtension(UrlManager.class);
			final URI absoluteUri = urlManager.getAbsoluteUri(map, uri);
			return absoluteUri;
		}
		catch (final MalformedURLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void setUri(final URI url) {
		uri = url;
	}

	private URI uri;
	private float zoom = -1f;

	public float getZoom() {
		return zoom;
	}

	public void setZoom(final float r) {
		zoom = r;
		for (final NodeView nodeView : viewers) {
			final JComponent viewer = nodeView.getContent(ViewerController.VIEWER_POSITION);
			final IViewerFactory factory = (IViewerFactory) viewer.getClientProperty(IViewerFactory.class);
			final MapView mapView = (MapView) SwingUtilities.getAncestorOfClass(MapView.class, viewer);
			final Dimension preferredSize = factory.getOriginalSize(viewer);
			preferredSize.width = (int) (preferredSize.width * r);
			preferredSize.height = (int) (preferredSize.height * r);
			preferredSize.width = mapView.getZoomed(preferredSize.width);
			preferredSize.height = mapView.getZoomed(preferredSize.height);
			factory.setViewerSize(viewer, preferredSize);
			viewer.revalidate();
		}
	}
	
	public float setZoom(final int originalWidth, final int maximumWidth) {
        float zoom;
        final float zoomedWidth;
        if(originalWidth <= maximumWidth){
        	zoomedWidth = originalWidth;
        	zoom = 1;
        }
        else{
        	zoomedWidth = maximumWidth;
        	zoom = zoomedWidth /originalWidth;
        }
        setZoom(zoom);
        return zoom;
    }


	static ExternalResource getPreviewUrl(final NodeModel model) {
		return (ExternalResource) model.getExtension(ExternalResource.class);
	}

	static ExternalResource setPreviewUrl(final NodeModel model, final URI uri, final IViewerFactory factory) {
		ExternalResource extension = (ExternalResource) model.getExtension(ExternalResource.class);
		if (extension == null) {
			extension = new ExternalResource();
			model.addExtension(extension);
		}
		extension.setUri(uri);
		return extension;
	}
}
