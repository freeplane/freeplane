package org.freeplane.view.swing.addins.filepreview;

import java.awt.Dimension;
import java.net.URI;


import javax.swing.JComponent;

public interface IViewerFactory {
	boolean accept(URI uri);
	JComponent createViewer(ExternalResource resource, URI absoluteUri);
	Dimension getOriginalSize(JComponent viewer);
	void setViewerSize(JComponent viewer, Dimension size);
	String getDescription();
	JComponent createViewer(URI uri, final Dimension preferredSize);
}
