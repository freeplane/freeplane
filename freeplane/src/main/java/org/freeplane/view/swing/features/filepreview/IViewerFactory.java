package org.freeplane.view.swing.features.filepreview;

import java.awt.Dimension;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;

public interface IViewerFactory {
	/**
	 * Returns true if factory can create viewer component for given URI
	 */
	boolean accept(URI uri);

	/**
	 * Creates viewer component for given URI with given preferred size
	 */
	ScalableComponent createViewer(URI uri, final Dimension preferredSize)
			throws MalformedURLException, IOException;

	/**
	 * Creates viewer component for given URI with given zoom
	 */
	ScalableComponent createViewer(URI uri, float zoom) throws MalformedURLException,
			IOException;

	/**
	 * Creates viewer component for given URI calculating its preferred size from the zoom of the resource
	 */
	ScalableComponent createViewer(ExternalResource resource, URI absoluteUri,
			int maximumWidth, float zoom) throws MalformedURLException, IOException;

	/**
	 * Returns description to be used in a user interface
	 */
	String getDescription();

}
