package org.freeplane.plugin.svg;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.geom.AffineTransform;
import java.awt.geom.Dimension2D;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.border.MatteBorder;
import javax.swing.filechooser.FileFilter;

import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.swing.gvt.GVTTreeRendererEvent;
import org.apache.batik.swing.gvt.GVTTreeRendererListener;
import org.apache.batik.swing.svg.GVTTreeBuilderAdapter;
import org.apache.batik.swing.svg.GVTTreeBuilderEvent;
import org.apache.batik.swing.svg.JSVGComponent;
import org.apache.batik.util.XMLResourceDescriptor;
import org.freeplane.view.swing.addins.filepreview.IPreviewComponentFactory;
import org.freeplane.view.swing.map.MapView;
import org.w3c.dom.Document;

public class SvgViewerFactory implements IPreviewComponentFactory {

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

	public JComponent createPreviewComponent(File file) {
		final JSVGCanvas canvas = new JSVGCanvas();
		canvas.setMySize(new Dimension(100, 100));
		canvas.setURI (file.toURI().toString());
		return canvas;
	}

	public FileFilter getFileFilter() {
		return fileFilter;
	}

}
