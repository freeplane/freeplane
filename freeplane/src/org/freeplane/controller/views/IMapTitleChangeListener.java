package org.freeplane.controller.views;

import org.freeplane.core.map.MapModel;
import org.freeplane.view.swing.map.MapView;

/**
 * You can register yourself to this listener at the main controller.
 */
public interface IMapTitleChangeListener {
	void setMapTitle(String pNewMapTitle, MapView pMapView, MapModel pModel);
}
