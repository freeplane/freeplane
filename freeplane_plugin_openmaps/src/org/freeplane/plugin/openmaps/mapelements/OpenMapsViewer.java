package org.freeplane.plugin.openmaps.mapelements;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JDialog;
import javax.swing.JPanel;

import org.openstreetmap.gui.jmapviewer.JMapViewer;

/**
 * @author Blair Archibald
 */
public class OpenMapsViewer extends JDialog {
	private static final long serialVersionUID = 1L;
	private final JMapViewer mapArea;
	private final OpenMapsController mapController;
	
	private static final int WIDTH = 800; 
	private static final int HEIGHT = 600;
	private static final String title = "OpenMaps";
	
	public OpenMapsViewer() {
		mapArea = new JMapViewer();
		mapController = new OpenMapsController(mapArea);
		
		configureDialog();
		this.add(mapArea);
		this.setVisible(true);
	}

	private void configureDialog() {
		this.setSize(new Dimension(WIDTH,HEIGHT));
		this.setTitle(title);
		this.setLayout(new BorderLayout());
	}
	
	public OpenMapsController getController() {
		return mapController;
	}
	
}
