package org.freeplane.controller.views;

import org.freeplane.map.tree.MapModel;
import org.freeplane.map.tree.view.MapView;

/**
 * You can register yourself to this listener at the main controller.
 */
public interface IMapTitleChangeListener {
	void setMapTitle(String pNewMapTitle, MapView pMapView, MapModel pModel);
}
