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
package org.freeplane.features.attribute;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.features.filter.FilterController;
import org.freeplane.features.map.MapChangeEvent;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.mode.Controller;

/**
 * @author Dimitry Polivaev
 */
public class ModelessAttributeController implements IExtension {
	public static final String ATTRIBUTE_VIEW_TYPE = "AttributeViewType";

	public static ModelessAttributeController getController() {
		Controller controller = Controller.getCurrentController();
		return (ModelessAttributeController) controller.getExtension(ModelessAttributeController.class);
	}

	public static void install() {
		Controller controller = Controller.getCurrentController();
		controller.addExtension(ModelessAttributeController.class, new ModelessAttributeController());
		installConditions();
	}

	public static void installConditions() {
		FilterController.getCurrentFilterController().getConditionFactory().addConditionController(1000, new AttributeConditionController());
	}

// // //	final private Controller controller;

	public ModelessAttributeController() {
		super();
//		this.controller = controller;
		Controller controller = Controller.getCurrentController();
		final AFreeplaneAction showAllAttributes = new ShowAllAttributesAction();
		final AFreeplaneAction showSelectedAttributes = new ShowSelectedAttributesAction();
		final AFreeplaneAction hideAllAttributes = new HideAllAttributesAction();
		controller.addAction(showAllAttributes);
		controller.addAction(showSelectedAttributes);
		controller.addAction(hideAllAttributes);
	}

	protected void setAttributeViewType(final MapModel map, final String type) {
		final String attributeViewType = getAttributeViewType(map);
		if (attributeViewType != null && attributeViewType != type) {
			final AttributeRegistry attributes = AttributeRegistry.getRegistry(map);
			attributes.setAttributeViewType(type);
			final MapChangeEvent mapChangeEvent = new MapChangeEvent(this, map, ATTRIBUTE_VIEW_TYPE, attributeViewType, type);
			Controller.getCurrentModeController().getMapController().fireMapChanged(mapChangeEvent);
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
