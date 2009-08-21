package org.freeplane.view.swing.addins.filepreview;

import java.awt.Dimension;
import java.net.URI;


import javax.swing.JComponent;

public interface IViewerFactory {
	boolean accept(URI uri);
	JComponent createViewer(URI uri);
	Dimension getOriginalSize(JComponent viewer);
	void setViewerSize(JComponent viewer, Dimension size);
	String getDescription();
}
