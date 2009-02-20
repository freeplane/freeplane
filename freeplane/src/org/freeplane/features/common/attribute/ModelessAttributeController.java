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
import org.freeplane.core.filter.FilterController;
import org.freeplane.core.model.MapModel;

/**
 * @author Dimitry Polivaev
 */
public class ModelessAttributeController implements IExtension {
	public static ModelessAttributeController getController(final Controller controller) {
		return (ModelessAttributeController) controller.getExtension(ModelessAttributeController.class);
	}

	public static void install(final Controller controller) {
		controller.putExtension(ModelessAttributeController.class, new ModelessAttributeController(controller));
		FilterController.getConditionFactory().addConditionController(2, new AttributeConditionController(controller));
	}

	final private HideAllAttributesAction hideAllAttributes;
	final private ShowAllAttributesAction showAllAttributes;
	final private ShowAttributeDialogAction showAttributeManagerAction;
	final private ShowSelectedAttributesAction showSelectedAttributes;

	public ModelessAttributeController(final Controller controller) {
		super();
		showAttributeManagerAction = new ShowAttributeDialogAction(controller);
		showAllAttributes = new ShowAllAttributesAction(controller);
		showSelectedAttributes = new ShowSelectedAttributesAction(controller);
		hideAllAttributes = new HideAllAttributesAction(controller);
		controller.putAction(showAttributeManagerAction);
		controller.putAction(showAllAttributes);
		controller.putAction(showSelectedAttributes);
		controller.putAction(hideAllAttributes);
	}

	public void setAttributeViewType(final MapModel map, final String value) {
		if (value.equals(AttributeTableLayoutModel.SHOW_SELECTED)) {
			((ShowSelectedAttributesAction) showSelectedAttributes).setAttributeViewType(map);
		}
		else if (value.equals(AttributeTableLayoutModel.HIDE_ALL)) {
			((HideAllAttributesAction) hideAllAttributes).setAttributeViewType(map);
		}
		else if (value.equals(AttributeTableLayoutModel.SHOW_ALL)) {
			((ShowAllAttributesAction) showAllAttributes).setAttributeViewType(map);
		}
	}
}
