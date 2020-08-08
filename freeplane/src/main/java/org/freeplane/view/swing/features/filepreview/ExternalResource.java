package org.freeplane.view.swing.features.filepreview;

import java.awt.Dimension;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.swing.JComponent;

import org.freeplane.core.extension.IExtension;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.url.UrlManager;
import org.freeplane.view.swing.map.NodeView;

public class ExternalResource implements IExtension {
	final private Set<NodeView> viewers;

	public ExternalResource(URI uri) {
		if(uri == null)
			throw new NullPointerException();
		viewers = new LinkedHashSet<NodeView>();
		this.uri = uri;
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

	final private URI uri;
	private float zoom = -1f;

	public float getZoom() {
		return zoom;
	}

	public void setZoom(final float r) {
		zoom = r;
		for (final NodeView nodeView : viewers) {
			final JComponent viewer = nodeView.getContent(ViewerController.VIEWER_POSITION);
			final ViewerLayoutManager layoutManager = (ViewerLayoutManager)viewer.getLayout();
			if(layoutManager != null) {
				final Dimension preferredSize = layoutManager.calculatePreferredSize();
				((ScalableComponent) viewer).setFinalViewerSize(preferredSize);
				viewer.revalidate();
				viewer.repaint();
			}
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
}
