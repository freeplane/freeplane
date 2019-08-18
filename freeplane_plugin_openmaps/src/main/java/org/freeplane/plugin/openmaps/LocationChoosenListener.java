package org.freeplane.plugin.openmaps;

import org.openstreetmap.gui.jmapviewer.interfaces.ICoordinate;

/**
 * @author Blair Archibald 
 */
public interface LocationChoosenListener {
	public void locationChoosenAction(ICoordinate locationChoosen, int zoom);
}
