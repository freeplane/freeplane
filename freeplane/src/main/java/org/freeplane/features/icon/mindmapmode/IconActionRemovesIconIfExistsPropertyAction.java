package org.freeplane.features.icon.mindmapmode;

import org.freeplane.core.resources.SetBooleanPropertyAction;
import org.freeplane.core.ui.SelectableAction;

@SelectableAction
class IconActionRemovesIconIfExistsPropertyAction extends SetBooleanPropertyAction {
	private static final long serialVersionUID = 1L;

	IconActionRemovesIconIfExistsPropertyAction() {
		super(IconAction.ICON_ACTION_REMOVES_ICON_IF_EXISTS_PROPERTY);
	}
}
