package org.freeplane.core.frame;

import org.freeplane.core.map.MapModel;
import org.freeplane.core.view.IMapView;

/**
 * You can register yourself to this listener at the main controller.
 */
public interface IMapTitleChangeListener {
	void setMapTitle(String pNewMapTitle, IMapView pMapView, MapModel pModel);
}
