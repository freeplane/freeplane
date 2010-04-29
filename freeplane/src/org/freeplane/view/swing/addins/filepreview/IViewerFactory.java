package org.freeplane.view.swing.addins.filepreview;

import java.awt.Dimension;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;

import javax.swing.JComponent;

public interface IViewerFactory {
	boolean accept(URI uri);

	JComponent createViewer(ExternalResource resource, URI absoluteUri) throws MalformedURLException, IOException;

	Dimension getOriginalSize(JComponent viewer);

	void setViewerSize(JComponent viewer, Dimension size);

	String getDescription();

	JComponent createViewer(URI uri, final Dimension preferredSize) throws MalformedURLException, IOException;
}
