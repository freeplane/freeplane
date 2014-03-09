package org.freeplane.plugin.svg;

import java.awt.Dimension;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;

import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.swing.gvt.GVTTreeRendererAdapter;
import org.apache.batik.swing.gvt.GVTTreeRendererEvent;
import org.apache.batik.util.SVGConstants;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.TextUtils;
import org.freeplane.view.swing.features.filepreview.ExternalResource;
import org.freeplane.view.swing.features.filepreview.IViewerFactory;
import org.freeplane.view.swing.features.filepreview.ImageLoadingListener;
import org.freeplane.view.swing.features.filepreview.ScalableComponent;
import org.freeplane.view.swing.features.filepreview.ViewerLayoutManager;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGLength;
import org.w3c.dom.svg.SVGSVGElement;

public class SvgViewerFactory implements IViewerFactory {

	private ViewerComponent canvas;

	private final class ViewerComponent extends JSVGCanvas implements ScalableComponent {
		private static final long serialVersionUID = 1L;
		private Dimension originalSize = null;
		private Dimension maximumSize = null;

		public Dimension getOriginalSize() {
			return new Dimension(originalSize);
		}

		public void setFinalViewerSize(final Dimension size) {
			Dimension sizeWithScaleCorrection = fitToMaximumSize(size);
			setRenderingTransform(initialTransform);
			setPreferredSize(sizeWithScaleCorrection);
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

		public void setDraftViewerSize(final Dimension size) {
			setFinalViewerSize(size);
		}

		public void setFinalViewerSize(final float zoom) {
			if (originalSize != null) {
				int scaledWidth = (int) (originalSize.width * zoom);
				int scaledHeight = (int) (originalSize.height * zoom);
				setFinalViewerSize(new Dimension(scaledWidth, scaledHeight));
			}
		}

		public ViewerComponent(final URI uri) {
			super(null, false, false);
			setDocumentState(ALWAYS_STATIC);
			setSize(1, 1);
			addGVTTreeRendererListener(new GVTTreeRendererAdapter() {
				@Override
				public void gvtRenderingStarted(final GVTTreeRendererEvent e) {
					super.gvtRenderingStarted(e);
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
					removeGVTTreeRendererListener(this);
				}
			});
			setURI(uri.toString());
		}

		@Override
		public Dimension getPreferredSize() {
			if (originalSize == null) {
				return new Dimension(1, 1);
			}
			return super.getPreferredSize();
		}

		public void setMaximumComponentSize(Dimension size) {
			this.maximumSize = size;
		}

		public void setCenter(boolean center) {
		}

		public void setImageLoadingListener(final ImageLoadingListener listener) {
			addGVTTreeRendererListener(new GVTTreeRendererAdapter(){
				@Override
                public void gvtRenderingCompleted(GVTTreeRendererEvent e) {
					listener.imageLoaded();
               }
			});
        }
	}

	public boolean accept(final URI uri) {
		return uri.getRawPath().endsWith(".svg");
	}

	public String getDescription() {
		return TextUtils.getText("svg");
	};

	public ScalableComponent createViewer(final ExternalResource resource, final URI uri, final int maximumWidth) {
		canvas = new ViewerComponent(uri);
		canvas.addGVTTreeRendererListener(new GVTTreeRendererAdapter() {
			@Override
			public void gvtRenderingCompleted(final GVTTreeRendererEvent e) {
				final Dimension preferredSize = canvas.getOriginalSize();
				float r = resource.getZoom();
				final int originalWidth = preferredSize.width;
				if (r == -1) {
					r = resource.setZoom(originalWidth, maximumWidth);
				}
				preferredSize.width = (int) (Math.rint(originalWidth * r));
				preferredSize.height = (int) (Math.rint(preferredSize.height * r));
				canvas.setPreferredSize(preferredSize);
				canvas.setLayout(new ViewerLayoutManager(1f));
				canvas.revalidate();
				canvas.removeGVTTreeRendererListener(this);
			}
		});
		return canvas;
	}

	public ScalableComponent createViewer(final URI uri, final Dimension preferredSize) {
		canvas = new ViewerComponent(uri);
		canvas.setFinalViewerSize(preferredSize);
		canvas.addGVTTreeRendererListener(new GVTTreeRendererAdapter() {
			@Override
			public void gvtRenderingCompleted(final GVTTreeRendererEvent e) {
				canvas.setFinalViewerSize(canvas.getSize());
				canvas.revalidate();
				canvas.removeGVTTreeRendererListener(this);
			}
		});
		return canvas;
	}

	public ScalableComponent getComponent() {
		return canvas;
	}

	public ScalableComponent createViewer(URI uri, final float zoom) throws MalformedURLException, IOException {
		canvas = new ViewerComponent(uri);
		canvas.addGVTTreeRendererListener(new GVTTreeRendererAdapter() {
			@Override
			public void gvtRenderingCompleted(final GVTTreeRendererEvent e) {
				canvas.setFinalViewerSize(zoom);
				canvas.revalidate();
				canvas.removeGVTTreeRendererListener(this);
			}
		});
		return canvas;
	}
}
