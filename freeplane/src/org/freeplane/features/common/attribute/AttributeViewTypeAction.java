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
        final String attributeViewType = getAttributeViewType();
    	if (attributeViewType !=  null && attributeViewType != type) {
    		final MapModel map = getController().getMap();
    		final AttributeRegistry attributes = AttributeRegistry.getRegistry(map);
    		attributes.setAttributeViewType(type);
    	}
    }

	protected String getAttributeViewType() {
    	final MapModel map = getController().getMap();
    	if (map == null) {
    		return null;
    	}
    	final AttributeRegistry attributes = AttributeRegistry.getRegistry(map);
    	if (attributes == null) {
    		return null;
    	}
    	final String attributeViewType = attributes.getAttributeViewType();
    	return attributeViewType;
    }
}
