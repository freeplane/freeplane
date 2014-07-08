package org.freeplane.plugin.openmaps;

import org.openstreetmap.gui.jmapviewer.Coordinate;

/**
 * @author Blair Archibald 
 */
public interface LocationChoosenListener {
	public void locationChoosenAction(Coordinate locationChoosen, int zoom);
}
