package org.freeplane.core.frame;

import org.freeplane.core.model.MapModel;


/**
 * You can register yourself to this listener at the main controller.
 */
public interface IMapTitleChangeListener {
	void setMapTitle(MapModel pModel, String pNewMapTitle);
}
