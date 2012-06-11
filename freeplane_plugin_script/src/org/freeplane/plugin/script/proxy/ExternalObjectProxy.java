/**
 * 
 */
package org.freeplane.plugin.script.proxy;

import java.net.URI;
import java.net.URISyntaxException;

import org.freeplane.core.util.LogUtils;
import org.freeplane.features.map.NodeModel;
import org.freeplane.plugin.script.ScriptContext;
import org.freeplane.view.swing.features.filepreview.ExternalResource;
import org.freeplane.view.swing.features.filepreview.ViewerController;

class ExternalObjectProxy extends AbstractProxy<NodeModel> implements Proxy.ExternalObject {
	ExternalObjectProxy(final NodeModel delegate, final ScriptContext scriptContext) {
		super(delegate, scriptContext);
	}

	private ExternalResource getExternalObjectModel() {
		return (ExternalResource) getDelegate().getExtension(ExternalResource.class);
	}
	
	public String getUri() {
		final ExternalResource externalObject = getExternalObjectModel();
		final URI uri = externalObject == null ? null : externalObject.getUri();
		return uri == null ? null : uri.toString();
	}
	
	@Deprecated
	public String getURI() {
		return getUri();
	}

	private ViewerController getViewerController() {
		return (ViewerController) getModeController().getExtension(ViewerController.class);
	}

	public float getZoom() {
		final ExternalResource externalObject = getExternalObjectModel();
		return externalObject == null ? 1f : externalObject.getZoom();
	}

	public void setUri(final String uri) {
		ExternalResource externalObject = getExternalObjectModel();
		try {
			if (externalObject != null) {
				if (uri == null) {
					// remove object
					getViewerController().undoableToggleHook(getDelegate(), null);
					return;
				}
				getViewerController().undoableToggleHook(getDelegate(), externalObject);
				externalObject = new ExternalResource(new URI(uri));
				getViewerController().undoableToggleHook(getDelegate(), externalObject);
			}
		}
		catch (final URISyntaxException e) {
			LogUtils.warn(e);
		}
	}
	
	@Deprecated
	public void setURI(final String uri) {
		setUri(uri);
	}

	public void setZoom(final float zoom) {
		final ExternalResource externalObject = getExternalObjectModel();
		if (externalObject != null)
			getViewerController().setZoom(getModeController(), getDelegate().getMap(), externalObject, zoom);
	}

    /** make <code>if (node.externalObject) println "has an externalObject"</code> work. */
    public boolean asBoolean() {
        return getExternalObjectModel() != null;
    }
}
