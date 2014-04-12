package org.freeplane.features.attribute;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.mode.Controller;

public abstract class AttributeViewTypeAction extends AFreeplaneAction {
	private static final long serialVersionUID = 1L;

	public AttributeViewTypeAction(final String key) {
		super(key);
	}

	protected void setAttributeViewType(final String type) {
		final MapModel map = Controller.getCurrentController().getMap();
		ModelessAttributeController.getController().setAttributeViewType(map, type);
	}

	protected String getAttributeViewType() {
		final MapModel map = Controller.getCurrentController().getMap();
		return ModelessAttributeController.getController().getAttributeViewType(map);
	}
}
