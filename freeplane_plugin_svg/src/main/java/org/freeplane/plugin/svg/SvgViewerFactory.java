package org.freeplane.plugin.svg;

import java.awt.Dimension;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.function.Consumer;

import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.swing.svg.GVTTreeBuilderAdapter;
import org.apache.batik.swing.svg.GVTTreeBuilderEvent;
import org.apache.batik.util.SVGConstants;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.TextUtils;
import org.freeplane.view.swing.features.filepreview.ExternalResource;
import org.freeplane.view.swing.features.filepreview.IViewerFactory;
import org.freeplane.view.swing.features.filepreview.ScalableComponent;
import org.freeplane.view.swing.features.filepreview.ViewerLayoutManager;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGLength;
import org.w3c.dom.svg.SVGSVGElement;

public class SvgViewerFactory implements IViewerFactory {

	private static class ViewerComponent extends JSVGCanvas implements ScalableComponent {
		private static final long serialVersionUID = 1L;
		private Dimension originalSize = null;
		private Dimension maximumSize = null;

		public ViewerComponent(final URI uri, Consumer<ViewerComponent> initializer) {
		    super(null, false, false);
		    setPreferredSize(new Dimension(1, 1));
		    setRecenterOnResize(false);
		    setDocumentState(ALWAYS_STATIC);
		    setURI(uri.toString());
		    addGVTTreeBuilderListener(new GVTTreeBuilderAdapter() {
		        @Override
		        public void gvtBuildCompleted(GVTTreeBuilderEvent e) {
		            removeGVTTreeBuilderListener(this);
                    final SVGDocument document = getSVGDocument();
                    final SVGSVGElement rootElement = document.getRootElement();
                    final SVGLength width = rootElement.getWidth().getBaseVal();
                    final SVGLength height = rootElement.getHeight().getBaseVal();
                    float defaultWidth = (float) Math.ceil(width.getValue());
                    float defaultHeigth = (float) Math.ceil(height.getValue());
                    if (defaultWidth == 1f && defaultHeigth == 1f) {
                        defaultWidth = ResourceController.getResourceController().getIntProperty(
                                "default_external_component_width", 200);
                        defaultHeigth = ResourceController.getResourceController().getIntProperty(
                                "default_external_component_height", 200);
                    }
                    originalSize = new Dimension((int) defaultWidth, (int) defaultHeigth);
                    if ("".equals(rootElement.getAttributeNS(null, SVGConstants.SVG_VIEW_BOX_ATTRIBUTE))) {
                        rootElement.setAttributeNS(null, SVGConstants.SVG_VIEW_BOX_ATTRIBUTE, "0 0 " + defaultWidth
                                + " " + defaultHeigth);
                    }
                    if ("".equals(rootElement.getAttributeNS(null, SVGConstants.SVG_PRESERVE_ASPECT_RATIO_ATTRIBUTE))) {
                        rootElement.setAttributeNS(null, SVGConstants.SVG_PRESERVE_ASPECT_RATIO_ATTRIBUTE, "none");
                    }
                    initializer.accept(ViewerComponent.this);
                    revalidate();
                    repaint();
                }
		    });

		}
		
        @Override
		public Dimension getOriginalSize() {
			return new Dimension(originalSize);
		}

		@Override
		public void setFinalViewerSize(final Dimension size) {
			Dimension sizeWithScaleCorrection = fitToMaximumSize(size);
			setMySize(sizeWithScaleCorrection);
			setSize(sizeWithScaleCorrection);
		}

		private Dimension fitToMaximumSize(final Dimension size) {
			if (maximumSize == null || isUnderMaximumSize(size)) {
				return size;
			}
			else {
				return maximumSize;
			}
		}

		private boolean isUnderMaximumSize(final Dimension size) {
			return maximumSize.getWidth() >= size.width || maximumSize.getHeight() >= size.height;
		}

		@Override
		public void setDraftViewerSize(final Dimension size) {
			setFinalViewerSize(size);
		}

		@Override
		public void setFinalViewerSize(final float zoom) {
			if (originalSize != null) {
				int scaledWidth = (int) (originalSize.width * zoom);
				int scaledHeight = (int) (originalSize.height * zoom);
				setFinalViewerSize(new Dimension(scaledWidth, scaledHeight));
			}
		}

		@Override
		public void setMaximumComponentSize(Dimension size) {
			this.maximumSize = size;
		}
}

	@Override
	public boolean accept(final URI uri) {
		String path = uri.isOpaque() ? uri.getSchemeSpecificPart() : uri.getRawPath();
		return path.toLowerCase().endsWith(".svg");
	}

	@Override
	public String getDescription() {
		return TextUtils.getText("svg");
	}

	@Override
	public ScalableComponent createViewer(final ExternalResource resource, final URI uri, final int maximumWidth, float zoom) {
		return new ViewerComponent(uri, canvas -> {
            final Dimension originalSize = canvas.getOriginalSize();
            float r = resource.getZoom();
            final int originalWidth = originalSize.width;
            final ViewerLayoutManager viewerLayoutManager = new ViewerLayoutManager(zoom, resource, originalSize);
            canvas.setLayout(viewerLayoutManager);
            if(r == -1){
                r = resource.setZoom(originalWidth, maximumWidth);
            }
            float scaledZoom = r * zoom;
            if(scaledZoom != 1f) {
                canvas.setFinalViewerSize(scaledZoom);
            }
		});
	}

	@Override
	public ScalableComponent createViewer(final URI uri, final Dimension size) {
        return new ViewerComponent(uri, canvas -> {
            canvas.setFinalViewerSize(size);
        });
	}

	@Override
	public ScalableComponent createViewer(URI uri, final float zoom) throws MalformedURLException, IOException {
        return new ViewerComponent(uri, canvas -> {
            if(zoom != 1f) {
                canvas.setFinalViewerSize(zoom);
            }
        });
	}
}
