package org.freeplane.plugin.svg;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JComponent;
import javax.swing.border.MatteBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.swing.gvt.GVTTreeRendererEvent;
import org.apache.batik.swing.gvt.GVTTreeRendererListener;
import org.apache.batik.swing.svg.SVGDocumentLoaderAdapter;
import org.apache.batik.swing.svg.SVGDocumentLoaderEvent;
import org.apache.batik.util.XMLResourceDescriptor;
import org.freeplane.view.swing.addins.filepreview.IPreviewComponentFactory;
import org.w3c.dom.Document;

public class SvgViewerFactory implements IPreviewComponentFactory {

	final private FileFilter fileFilter;
	
	SvgViewerFactory(){
		fileFilter = new FileNameExtensionFilter("SVG file", "svg");
	}

	public JComponent createPreviewComponent(File file) {
		try {
			final JSVGCanvas canvas = new JSVGCanvas();
			canvas.setBorder(new MatteBorder(1, 1, 1, 1, Color.BLACK));
			String parser = XMLResourceDescriptor.getXMLParserClassName();
			SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parser);
			URL url = file.toURL();
			Document doc = f.createDocument(url.toString());
			canvas.setDocument(doc);
			canvas.setSize(1, 0);
			// Set the JSVGCanvas listeners.
			canvas.addGVTTreeRendererListener(new GVTTreeRendererListener() {
				public void gvtRenderingCancelled(GVTTreeRendererEvent arg0) {
				}

				public void gvtRenderingCompleted(GVTTreeRendererEvent arg0) {
	    			canvas.setSize(canvas.getPreferredSize());
	    			((JComponent) canvas.getParent()).revalidate();
	    			canvas.removeGVTTreeRendererListener(this);
				}

				public void gvtRenderingFailed(GVTTreeRendererEvent arg0) {
				}

				public void gvtRenderingPrepare(GVTTreeRendererEvent arg0) {
				}

				public void gvtRenderingStarted(GVTTreeRendererEvent arg0) {
				}
	        });

			return canvas;
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public FileFilter getFileFilter() {
		return fileFilter;
	}

}
