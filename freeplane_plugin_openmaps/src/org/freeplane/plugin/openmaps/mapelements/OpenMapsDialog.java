package org.freeplane.plugin.openmaps.mapelements;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;

import org.openstreetmap.gui.jmapviewer.Coordinate;

/**
 * @author Blair Archibald
 */
public class OpenMapsDialog extends JDialog implements ActionListener {
	private static final long serialVersionUID = 1L;
	
	private final OpenMapsViewer mapArea;
	private final OpenMapsController mapController;
	private final JButton done;
	
	private static final String TITLE = "OpenMaps";
	
	public OpenMapsDialog() {
		mapArea = new OpenMapsViewer();
		mapController = new OpenMapsController(mapArea);
		
		done = new JButton("Done");
		done.addActionListener(this);
		
		configureDialog();
		addComponents();
		
		this.pack();
		this.setVisible(true);
	}

	private void addComponents() {
		this.add(mapArea, BorderLayout.NORTH);
		this.add(done, BorderLayout.SOUTH);
	}

	private void configureDialog() {
		this.setTitle(TITLE);
		this.setLayout(new BorderLayout());
		this.setResizable(false);
	}
	
	public OpenMapsController getController() {
		return mapController;
	}

	public void actionPerformed(ActionEvent e) {
		this.dispose();
	}

	public void showZoomToLocation(Coordinate location, int zoom) {
		mapController.zoomToLocation(location, zoom);
	}
	
	
}
