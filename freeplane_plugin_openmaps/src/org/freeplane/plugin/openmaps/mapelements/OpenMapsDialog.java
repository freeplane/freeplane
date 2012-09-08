package org.freeplane.plugin.openmaps.mapelements;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JDialog;

/**
 * @author Blair Archibald
 */
public class OpenMapsDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	private final OpenMapsViewer mapArea;
	private final OpenMapsController mapController;
	
	private static final int WIDTH = 800; 
	private static final int HEIGHT = 600;
	private static final String title = "OpenMaps";
	
	public OpenMapsDialog() {
		mapArea = new OpenMapsViewer();
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
