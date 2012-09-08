package org.freeplane.plugin.openmaps.mapelements;

import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.MemoryTileCache;

/** Wrapper class around JMapViewer - stops adding default controller in constructor */

public class OpenMapsViewer extends JMapViewer {
	
	public OpenMapsViewer () {
		 super(new MemoryTileCache(), 4);
	}

}
