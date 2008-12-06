/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
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
package deprecated.freemind.common;

import org.freeplane.controller.Freeplane;

/**
 * @author foltin
 */
public class RemindValueProperty extends ThreeCheckBoxProperty {
	public RemindValueProperty(final String pDescription, final String pLabel) {
		super(pDescription, pLabel);
		mDontTouchValue = "";
	}

	/**
	 *
	 */
	@Override
	protected void setState(final int newState) {
		state = newState;
		String[] strings;
		strings = new String[3];
		strings[ThreeCheckBoxProperty.TRUE_VALUE_INT] = Freeplane
		    .getController().getResourceController().getResourceString(
		        "OptionalDontShowMeAgainDialog.ok").replaceFirst("&", "");
		strings[ThreeCheckBoxProperty.FALSE_VALUE_INT] = Freeplane
		    .getController().getResourceController().getResourceString(
		        "OptionalDontShowMeAgainDialog.cancel").replaceFirst("&", "");
		strings[ThreeCheckBoxProperty.DON_T_TOUCH_VALUE_INT] = Freeplane
		    .getController().getResourceController().getResourceString(
		        "OptionPanel.ask").replaceFirst("&", "");
		mButton.setText(strings[state]);
	}
}
