/**
 * 
 */
package org.freeplane.plugin.script.proxy;

import java.net.URI;
import java.net.URISyntaxException;

import org.freeplane.core.model.NodeModel;
import org.freeplane.core.util.LogTool;
import org.freeplane.features.mindmapmode.MModeController;
import org.freeplane.view.swing.addins.filepreview.ExternalResource;
import org.freeplane.view.swing.addins.filepreview.ViewerController;

class ExternalObjectProxy extends AbstractProxy<NodeModel> implements Proxy.ExternalObject {
	ExternalObjectProxy(final NodeModel delegate,
			final MModeController modeController) {
		super(delegate, modeController);
	}

	private ExternalResource getExternalObjectModel() {
		return (ExternalResource) getDelegate()
				.getExtension(ExternalResource.class);
	}

	public String getURI() {
		final ExternalResource externalObject = getExternalObjectModel();
		return externalObject.getUri().toString();
	}

	private ViewerController getViewerController() {
		return (ViewerController) getModeController().getExtension(
				ViewerController.class);
	}

	public float getZoom() {
		final ExternalResource externalObject = getExternalObjectModel();
		return externalObject.getZoom();
	}

	public void setURI(final String uri) {
		try {
			ExternalResource externalObject = getExternalObjectModel();
			if (externalObject != null) {
				getViewerController().undoableToggleHook(getDelegate(), externalObject);
			}
			externalObject = new ExternalResource();
			externalObject.setUri(new URI(uri));
			getViewerController().undoableToggleHook(getDelegate(), externalObject);
		} catch (final URISyntaxException e) {
			LogTool.warn(e);
		}

	}

	public void setZoom(final float zoom) {
		final ExternalResource externalObject = getExternalObjectModel();
		getViewerController().setZoom(getModeController(), getDelegate().getMap(),
				externalObject, zoom);

	}
}