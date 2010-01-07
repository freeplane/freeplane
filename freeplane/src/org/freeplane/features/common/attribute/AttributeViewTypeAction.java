package org.freeplane.features.common.attribute;

import javax.swing.ImageIcon;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.model.MapModel;
import org.freeplane.core.ui.AFreeplaneAction;

public abstract class AttributeViewTypeAction extends AFreeplaneAction {
	public AttributeViewTypeAction(String key, Controller controller) {
		super(key, controller);
	}

	public AttributeViewTypeAction(String key, Controller controller, String title, ImageIcon icon) {
		super(key, controller, title, icon);
	}

	protected void setAttributeViewType(final String type) {
    	final MapModel map = getController().getMap();
    	ModelessAttributeController.getController(getController()).setAttributeViewType(map, type);
    }

	protected String getAttributeViewType() {
    	final MapModel map = getController().getMap();
    	return ModelessAttributeController.getController(getController()).getAttributeViewType(map);
    }
}
