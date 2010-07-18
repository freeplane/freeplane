/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Dimitry Polivaev
 *
 *  This file author is Dimitry Polivaev
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.freeplane.features.common.attribute;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.extension.IExtension;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.features.common.filter.FilterController;
import org.freeplane.features.common.map.MapModel;

/**
 * @author Dimitry Polivaev
 */
public class ModelessAttributeController implements IExtension {
	public static ModelessAttributeController getController(final Controller controller) {
		return (ModelessAttributeController) controller.getExtension(ModelessAttributeController.class);
	}

	public static void install(final Controller controller) {
		controller.addExtension(ModelessAttributeController.class, new ModelessAttributeController(controller));
		FilterController.getController(controller).getConditionFactory().addConditionController(2,
		    new AttributeConditionController(controller));
	}

// // //	final private Controller controller;

	public ModelessAttributeController(final Controller controller) {
		super();
//		this.controller = controller;
		final AFreeplaneAction showAllAttributes = new ShowAllAttributesAction(controller);
		final AFreeplaneAction showSelectedAttributes = new ShowSelectedAttributesAction(controller);
		final AFreeplaneAction hideAllAttributes = new HideAllAttributesAction(controller);
		controller.addAction(showAllAttributes);
		controller.addAction(showSelectedAttributes);
		controller.addAction(hideAllAttributes);
	}

	protected void setAttributeViewType(final MapModel map, final String type) {
		final String attributeViewType = getAttributeViewType(map);
		if (attributeViewType != null && attributeViewType != type) {
			final AttributeRegistry attributes = AttributeRegistry.getRegistry(map);
			attributes.setAttributeViewType(type);
		}
	}

	protected String getAttributeViewType(final MapModel map) {
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
