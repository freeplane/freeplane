package org.freeplane.view.swing.features.filepreview;

import java.awt.Dimension;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;

import javax.swing.JComponent;

public interface IViewerFactory {
	/**
	 * Returns true if factory can create viewer component for given URI
	 */
	boolean accept(URI uri);

	/**
	 * Creates viewer component for given URI with given preferred size
	 */
	JComponent createViewer(URI uri, final Dimension preferredSize) throws MalformedURLException, IOException;

	/**
	 * Creates viewer component for given URI calculating its preferred size from the zoom of the resource
	 */
	JComponent createViewer(ExternalResource resource, URI absoluteUri, int maximumWidth) throws MalformedURLException, IOException;

	/**
	 * Returns not scaled size of the displayed component
	 */
	Dimension getOriginalSize(JComponent viewer);

	/**
	 * Adjusts size of the given viewer component after the mouse button is released
	 */
	void setFinalViewerSize(JComponent viewer, Dimension size);
	/**
	 * Adjusts size of the given viewer component inside resize operation by mouse drag
	 */
	void setDraftViewerSize(JComponent viewer, Dimension size);

	/**
	 * Returns description to be used in a user interface
	 */
	String getDescription();

}
