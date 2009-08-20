package org.freeplane.plugin.svg;

import java.awt.Dimension;
import java.io.File;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.swing.gvt.GVTTreeRendererAdapter;
import org.apache.batik.swing.gvt.GVTTreeRendererEvent;
import org.freeplane.view.swing.addins.filepreview.IViewerFactory;
import org.freeplane.view.swing.map.MapView;

public class SvgViewerFactory implements IViewerFactory {

	final private FileFilter fileFilter;
	
	SvgViewerFactory(){
		fileFilter = new FileFilter(){
			@Override
            public boolean accept(File f) {
	            return f.getName().endsWith(".svg");
            }

			@Override
            public String getDescription() {
	            return "SVG file";
            }};
	}

	public JComponent createViewer(File file) {
		final JSVGCanvas canvas = new JSVGCanvas(){

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public Dimension getPreferredSize() {
				Dimension preferredSize = super.getPreferredSize();
				MapView mapView = (MapView) SwingUtilities.getAncestorOfClass(MapView.class, this);
				preferredSize.width = mapView.getZoomed(preferredSize.width);
				preferredSize.height = mapView.getZoomed(preferredSize.height);
				return preferredSize;
			}
		};
		canvas.addGVTTreeRendererListener(new GVTTreeRendererAdapter() {
            public void gvtRenderingCompleted(GVTTreeRendererEvent e) {
                canvas.revalidate();
                canvas.removeGVTTreeRendererListener(this);
            }
        });


		canvas.setURI (file.toURI().toString());
		return canvas;
	}

	public FileFilter getFileFilter() {
		return fileFilter;
	}

}
