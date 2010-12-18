package org.freeplane.features.common.attribute;

import javax.swing.ImageIcon;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.features.common.map.MapChangeEvent;
import org.freeplane.features.common.map.MapModel;

public abstract class AttributeViewTypeAction extends AFreeplaneAction {
	private static final long serialVersionUID = 1L;

	public AttributeViewTypeAction(final String key) {
		super(key);
	}

	public AttributeViewTypeAction(final String key, final String title,
	                               final ImageIcon icon) {
		super(key, title, icon);
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
