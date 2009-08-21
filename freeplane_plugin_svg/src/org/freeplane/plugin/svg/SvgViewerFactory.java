package org.freeplane.plugin.svg;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.geom.Dimension2D;
import java.io.File;
import java.net.URI;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.swing.gvt.GVTTreeRendererAdapter;
import org.apache.batik.swing.gvt.GVTTreeRendererEvent;
import org.freeplane.view.swing.addins.filepreview.IViewerFactory;
import org.freeplane.view.swing.map.MapView;

public class SvgViewerFactory implements IViewerFactory {

	public boolean accept(URI uri) {
		return uri.getRawPath().endsWith(".svg");
	}

	public String getDescription() {
		return "SVG";
	};

	public JComponent createViewer(URI uri) {
		final JSVGCanvas canvas = new JSVGCanvas(){

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;
			private float zoom = 1f;
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
		};
		canvas.addGVTTreeRendererListener(new GVTTreeRendererAdapter() {
            public void gvtRenderingCompleted(GVTTreeRendererEvent e) {
                canvas.revalidate();
                canvas.removeGVTTreeRendererListener(this);
            }
        });


		canvas.setURI (uri.toString());
		return canvas;
	}

	public Dimension getOriginalSize(JComponent viewer) {
		final JSVGCanvas canvas = (JSVGCanvas) viewer;
		Dimension2D documentSize = canvas.getSVGDocumentSize();
		documentSize.setSize(Math.ceil(documentSize.getWidth()), documentSize.getHeight());
		return new Dimension((int)documentSize.getWidth(), (int)documentSize.getHeight());
	}

	public void setViewerSize(JComponent viewer, Dimension size) {
		final JSVGCanvas canvas = (JSVGCanvas) viewer;
		canvas.setMySize(size);
	}

}
