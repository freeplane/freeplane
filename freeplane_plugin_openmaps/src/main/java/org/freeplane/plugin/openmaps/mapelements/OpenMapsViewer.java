package org.freeplane.plugin.openmaps.mapelements;

import java.awt.Dimension;

import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.MemoryTileCache;

/**
 * @author Blair Archibald
 */
public class OpenMapsViewer extends JMapViewer {
	private static final long serialVersionUID = 1L;
	private static final int HEIGHT = 500;
	private static final int WIDTH = 800;

	public OpenMapsViewer () {
		 super(new MemoryTileCache());
		 this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
	}

}
