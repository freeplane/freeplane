package org.freeplane.plugin.svg;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;

import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.swing.gvt.GVTTreeRendererAdapter;
import org.apache.batik.swing.gvt.GVTTreeRendererEvent;
import org.apache.batik.util.SVGConstants;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.TextUtils;
import org.freeplane.view.swing.features.filepreview.ExternalResource;
import org.freeplane.view.swing.features.filepreview.IViewerFactory;
import org.freeplane.view.swing.features.filepreview.ScalableComponent;
import org.freeplane.view.swing.features.filepreview.ViewerLayoutManager;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGLength;
import org.w3c.dom.svg.SVGSVGElement;

public class SvgViewerFactory implements IViewerFactory {
	private static final String HOURGLASS = "\u29D6";
	private static final Font HOURGLASS_FONT = UITools.scaleUI(new Font(Font.DIALOG, Font.PLAIN, 36));


	private final class ViewerComponent extends JSVGCanvas implements ScalableComponent {
		private static final long serialVersionUID = 1L;
		private Dimension originalSize = null;
		private Dimension maximumSize = null;
		private boolean showHourGlass;

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

		public ViewerComponent(final URI uri) {
			this(uri, new Dimension(1, 1));
		}

		public ViewerComponent(final URI uri, Dimension size) {
			super(null, false, false);
			setDocumentState(ALWAYS_STATIC);
			setSize(size);
			final Timer timer = new Timer(500, new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					showHourGlass = image == null && getWidth() > 1;
					if(showHourGlass)
						repaint();
					((Timer)e.getSource()).stop();
				}
			});
			timer.start();
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

		@Override
		public void setMaximumComponentSize(Dimension size) {
			this.maximumSize = size;
		}

		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			if(showHourGlass && image == null) {
				g.setFont(HOURGLASS_FONT);
				g.setColor(Color.GRAY);
				g.drawString(HOURGLASS, getWidth() / 2 - HOURGLASS_FONT.getSize() * 1 / 3, getHeight() / 2);
			}
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
	};

	@Override
	public ScalableComponent createViewer(final ExternalResource resource, final URI uri, final int maximumWidth) {
		final ViewerComponent canvas = new ViewerComponent(uri);
		canvas.addGVTTreeRendererListener(new GVTTreeRendererAdapter() {
			@Override
			public void gvtRenderingCompleted(final GVTTreeRendererEvent e) {
				final Dimension originalSize = canvas.getOriginalSize();
				float r = resource.getZoom();
				final int originalWidth = originalSize.width;
				final ViewerLayoutManager viewerLayoutManager = new ViewerLayoutManager(1f, resource, originalSize);
				canvas.setLayout(viewerLayoutManager);
				if(r == -1){
					r = resource.setZoom(originalWidth, maximumWidth);
				}
				canvas.resetRenderingTransform();
				canvas.setFinalViewerSize(originalSize);
				canvas.setPreferredSize(viewerLayoutManager.calculatePreferredSize());
				canvas.revalidate();
				canvas.removeGVTTreeRendererListener(this);
			}
		});
		return canvas;
	}

	@Override
	public ScalableComponent createViewer(final URI uri, final Dimension preferredSize) {
		final ViewerComponent canvas = new ViewerComponent(uri);
		canvas.setSize(preferredSize);
		canvas.addGVTTreeRendererListener(new GVTTreeRendererAdapter() {
			@Override
			public void gvtRenderingCompleted(final GVTTreeRendererEvent e) {
				canvas.resetRenderingTransform();
				canvas.setFinalViewerSize(canvas.getOriginalSize());
				canvas.setPreferredSize(preferredSize);
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						canvas.setSize(preferredSize);
					}
				});
				canvas.revalidate();
				canvas.repaint();
				canvas.removeGVTTreeRendererListener(this);
			}
		});
		return canvas;
	}

	@Override
	public ScalableComponent createViewer(URI uri, final float zoom) throws MalformedURLException, IOException {
		final ViewerComponent canvas = new ViewerComponent(uri);
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
