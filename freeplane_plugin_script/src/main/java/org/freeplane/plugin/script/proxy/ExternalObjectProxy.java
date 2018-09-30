/**
 * 
 */
package org.freeplane.plugin.script.proxy;

import java.io.File;
import java.net.URI;
import java.net.URL;

import org.freeplane.core.util.LogUtils;
import org.freeplane.features.map.NodeModel;
import org.freeplane.plugin.script.ScriptExecution;
import org.freeplane.view.swing.features.filepreview.ExternalResource;
import org.freeplane.view.swing.features.filepreview.ViewerController;

class ExternalObjectProxy extends AbstractProxy<NodeModel> implements Proxy.ExternalObject {
    ExternalObjectProxy(final NodeModel delegate, final ScriptExecution scriptExecution) {
        super(delegate, scriptExecution);
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
        return getModeController().getExtension(ViewerController.class);
    }

    public float getZoom() {
        final ExternalResource externalObject = getExternalObjectModel();
        return externalObject == null ? 1f : externalObject.getZoom();
    }

    public void setUri(final String target) {
        if (!removeIfTargetIsNull(target)) {
            setUriImpl(convertToUri(target));
        }
    }

    public void setUri(final Object target) {
        if (!removeIfTargetIsNull(target)) {
            setUriImpl(convertToUri(target));
        }
    }
    
    public void setFile(final File target) {
        if (!removeIfTargetIsNull(target)) {
            setUriImpl(convertToUri(target));
        }
    }

    private URI convertToUri(Object target) {
        try {
            if (target instanceof URI) {
                return (URI) target;
            }
            else if (target instanceof String) {
                // file names are not usable for displaying images
                return new URL((String) target).toURI();
            }
            else if (target instanceof File) {
                return ((File) target).toURI();
            }
            else if (target instanceof URL) {
                return ((URL) target).toURI();
            }
            else {
                LogUtils.warn("cannot convert to an uri: " + target);
                return null;
            }
        }
        catch (Exception e) {
            LogUtils.warn("cannot convert to an uri: " + target, e);
            return null;
        }
    }

    private boolean removeIfTargetIsNull(Object target) {
        if (target == null && getExternalObjectModel() != null) {
            getViewerController().undoableToggleHook(getDelegate(), null);
            return true;
        }
        return false;
    }

    private void setUriImpl(final URI uri) {
        if (uri != null)
            getViewerController().paste(uri, getDelegate());
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
