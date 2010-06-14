package org.freeplane.features.common.attribute;

import javax.swing.ImageIcon;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.features.common.map.MapModel;

public abstract class AttributeViewTypeAction extends AFreeplaneAction {
	private static final long serialVersionUID = 1L;

	public AttributeViewTypeAction(final String key, final Controller controller) {
		super(key, controller);
	}

	public AttributeViewTypeAction(final String key, final Controller controller, final String title,
	                               final ImageIcon icon) {
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
