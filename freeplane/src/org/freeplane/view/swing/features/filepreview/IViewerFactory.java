package org.freeplane.view.swing.features.filepreview;

import java.awt.Dimension;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;

import javax.swing.JComponent;

public interface IViewerFactory {
	boolean accept(URI uri);

	JComponent createViewer(ExternalResource resource, URI absoluteUri, int maximumWidth) throws MalformedURLException, IOException;

	Dimension getOriginalSize(JComponent viewer);

	void setFinalViewerSize(JComponent viewer, Dimension size);
	void setDraftViewerSize(JComponent viewer, Dimension size);

	String getDescription();

	JComponent createViewer(URI uri, final Dimension preferredSize) throws MalformedURLException, IOException;
}
