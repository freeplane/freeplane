package org.freeplane.plugin.svg;

import java.awt.Dimension;
import java.net.URI;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.swing.gvt.GVTTreeRendererAdapter;
import org.apache.batik.swing.gvt.GVTTreeRendererEvent;
import org.apache.batik.util.SVGConstants;
import org.freeplane.view.swing.addins.filepreview.ExternalResource;
import org.freeplane.view.swing.addins.filepreview.IViewerFactory;
import org.freeplane.view.swing.map.MapView;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGLength;
import org.w3c.dom.svg.SVGSVGElement;

public class SvgViewerFactory implements IViewerFactory {

	private final class ViewerComponent extends JSVGCanvas {
	    /**
	     * 
	     */
	    private static final long serialVersionUID = 1L;
	    private float zoom = 1f;
	    private Dimension originalSize = null;

	    protected Dimension getOriginalSize() {
        	return new Dimension(originalSize);
        }

		public ViewerComponent(final ExternalResource resource, final URI uri) {
	        super(null, false, false);
			addGVTTreeRendererListener(new GVTTreeRendererAdapter() {
	            @Override
                public void gvtRenderingStarted(GVTTreeRendererEvent e) {
	                super.gvtRenderingStarted(e);
					final SVGDocument document = getSVGDocument();
					final SVGSVGElement rootElement = document.getRootElement();
					final SVGLength width = rootElement.getWidth().getBaseVal();
					final SVGLength height = rootElement.getHeight().getBaseVal();
					float defaultWidth = width.getValue();	
					float defaultHeigth = height.getValue();
					originalSize = new Dimension((int)defaultWidth, (int)defaultHeigth );
					zoom = 1f;
					if("".equals(rootElement.getAttributeNS(null, SVGConstants.SVG_VIEW_BOX_ATTRIBUTE))){
						rootElement.setAttributeNS(null, SVGConstants.SVG_VIEW_BOX_ATTRIBUTE, "0 0 "+ defaultWidth + " " + defaultHeigth);
					}
					setSize(originalSize);
	                removeGVTTreeRendererListener(this);
                }
	        });
			addGVTTreeRendererListener(new GVTTreeRendererAdapter() {

				public void gvtRenderingCompleted(GVTTreeRendererEvent e) {
	            	final float r = resource.getZoom();
	            	zoom = 1f;
	            	Dimension preferredSize = getOriginalSize();
	            	preferredSize.width = (int)(Math.rint(preferredSize.width * r));
	            	preferredSize.height = (int)(Math.rint(preferredSize.height * r));
	            	setPreferredSize(preferredSize);
	                revalidate();
	                removeGVTTreeRendererListener(this);
	            }
	        });
			setURI (uri.toString());
        }

		@Override
	    public Dimension getPreferredSize() {
	    	Dimension preferredSize = super.getPreferredSize();
	    	MapView mapView = (MapView) SwingUtilities.getAncestorOfClass(MapView.class, this);
	    	float newZoom = mapView.getZoom();
	    	if(zoom != newZoom){
	    		float ratio = newZoom/ zoom;
	    		preferredSize.width = (int)(Math.rint(preferredSize.width * ratio));
	    		preferredSize.height = (int)(Math.rint(preferredSize.height * ratio));
	    		setPreferredSize(preferredSize);
	    		zoom = newZoom;
	    	}
	    	return preferredSize;
	    }
    }

	public boolean accept(URI uri) {
		return uri.getRawPath().endsWith(".svg");
	}

	public String getDescription() {
		return "SVG";
	};

	public JComponent createViewer(final ExternalResource resource, final URI uri) {
		final JSVGCanvas canvas = new ViewerComponent(resource, uri);
		return canvas;
	}

	public Dimension getOriginalSize(JComponent viewer) {
		final ViewerComponent canvas = (ViewerComponent) viewer;
		return canvas.getOriginalSize();
	}

	public void setViewerSize(JComponent viewer, Dimension size) {
		final JSVGCanvas canvas = (JSVGCanvas) viewer;
		canvas.setMySize(size);
	}
}
