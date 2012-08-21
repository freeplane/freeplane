package org.freeplane.plugin.openmaps;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JDialog;
import javax.swing.JPanel;

import org.openstreetmap.gui.jmapviewer.JMapViewer;

/**
 * @author Blair Archibald
 */
public class MapViewer extends JDialog {
	private static final long serialVersionUID = 1L;
	private final JMapViewer mapArea;
	private final MapController mapController;
	
	private static final int WIDTH = 800; 
	private static final int HEIGHT = 600;
	private static final String title = "OpenMaps";
	
	public MapViewer() {
		mapArea = new JMapViewer();
		mapController = new MapController(mapArea);
		
		configureDialog();
		this.add(mapArea);
		this.setVisible(true);
	}

	private void configureDialog() {
		this.setSize(new Dimension(WIDTH,HEIGHT));
		this.setTitle(title);
		this.setLayout(new BorderLayout());
	}
	
	public MapController getController() {
		return mapController;
	}
	
}
