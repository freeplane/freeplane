package org.freeplane.plugin.svg;

import java.awt.Dimension;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.function.Consumer;

import org.apache.batik.bridge.ViewBox;
import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.swing.gvt.GVTTreeRendererAdapter;
import org.apache.batik.swing.gvt.GVTTreeRendererEvent;
import org.apache.batik.swing.svg.GVTTreeBuilderAdapter;
import org.apache.batik.swing.svg.GVTTreeBuilderEvent;
import org.apache.batik.swing.svg.JSVGComponent;
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

	private static class ViewerComponent extends JSVGComponent implements ScalableComponent {
		private static final long serialVersionUID = 1L;
		private Dimension originalSize = null;
		private Dimension maximumSize = null;

		public ViewerComponent(final URI uri, Consumer<ViewerComponent> initializer) {
		    super(null, false, false);
		    setPreferredSize(new Dimension(1, 1));
		    setRecenterOnResize(false);
		    setDocumentState(ALWAYS_STATIC);
		    addGVTTreeBuilderListener(new GVTTreeBuilderAdapter() {
		        @Override
		        public void gvtBuildCompleted(GVTTreeBuilderEvent e) {
		            removeGVTTreeBuilderListener(this);
                    final SVGDocument document = getSVGDocument();
                    final SVGSVGElement svgElt = document.getRootElement();
                    String svgViewboxAttribute = svgElt.getAttributeNS(null, SVGConstants.SVG_VIEW_BOX_ATTRIBUTE);
                    if (svgViewboxAttribute.isEmpty()) {
                        final SVGLength svgWidth = svgElt.getWidth().getBaseVal();
                        final SVGLength svgHeight = svgElt.getHeight().getBaseVal();
                        float width = (float) Math.ceil(svgWidth.getValue());
                        float heigth = (float) Math.ceil(svgHeight.getValue());
                        if (width <= 1f && heigth <= 1f) {
                            width = ResourceController.getResourceController().getIntProperty(
                                    "default_external_component_width", 200);
                            heigth = ResourceController.getResourceController().getIntProperty(
                                    "default_external_component_height", 200);
                        }
                        originalSize = new Dimension((int) width, (int) heigth);
                        svgElt.setAttributeNS(null, SVGConstants.SVG_VIEW_BOX_ATTRIBUTE, "0 0 " + width
                                + " " + heigth);
                    } else {
                        float[] vb= ViewBox.parseViewBoxAttribute(svgElt, svgViewboxAttribute, bridgeContext);
                        originalSize = new Dimension((int) vb[2], (int) vb[3]);
                    }
                    initializer.accept(ViewerComponent.this);
                    revalidate();
                    repaint();
                }
		    });
		    loadSVGDocument(uri.toString());

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
	public ViewerComponent createViewer(final ExternalResource resource, final URI uri, final int maximumWidth, float zoom) {
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
	public ViewerComponent createViewer(final URI uri, final Dimension size) {
        return new ViewerComponent(uri, canvas -> {
            canvas.setFinalViewerSize(size);
        });
	}

	@Override
	public ViewerComponent createViewer(URI uri, final float zoom) throws MalformedURLException, IOException {
        return new ViewerComponent(uri, canvas -> {
            canvas.setFinalViewerSize(zoom);
        });
	}

    @Override
    public ViewerComponent createViewer(URI uri, Dimension preferredSize,
            Consumer<ScalableComponent> callback) throws MalformedURLException, IOException {
        ViewerComponent viewer = createViewer(uri, preferredSize);
        viewer.addGVTTreeRendererListener(new GVTTreeRendererAdapter() {

            @Override
            public void gvtRenderingCompleted(GVTTreeRendererEvent e) {
                callback.accept(viewer);
            }
            
        });
        return viewer;
    }

    @Override
    public ViewerComponent createViewer(URI uri, float zoom, Consumer<ScalableComponent> callback)
            throws MalformedURLException, IOException {
        ViewerComponent viewer = createViewer(uri, zoom);
        viewer.addGVTTreeRendererListener(new GVTTreeRendererAdapter() {

            @Override
            public void gvtRenderingCompleted(GVTTreeRendererEvent e) {
                callback.accept(viewer);
            }
            
        });
        return viewer;

    }
}
