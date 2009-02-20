/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is created by Dimitry Polivaev in 2008.
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
package org.freeplane.features.common.icon;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.extension.ExtensionContainer;
import org.freeplane.core.extension.IExtension;
import org.freeplane.core.filter.FilterController;
import org.freeplane.core.io.ReadManager;
import org.freeplane.core.modecontroller.MapController;
import org.freeplane.core.modecontroller.ModeController;

/**
 * @author Dimitry Polivaev
 */
public class IconController implements IExtension {
	private static boolean firstRun = true;

	public static <T extends ExtensionContainer> IconController getController(final T modeController) {
		return (IconController) modeController.getExtension(IconController.class);
	}

	public static void install(final ModeController modeController, final IconController iconController) {
		modeController.putExtension(IconController.class, iconController);
		if (firstRun) {
			final Controller controller = modeController.getController();
			FilterController.getConditionFactory().addConditionController(1, new IconConditionController(controller));
			firstRun = false;
		}
	}

	final private ModeController modeController;

	public IconController(final ModeController modeController) {
		super();
		this.modeController = modeController;
		final MapController mapController = modeController.getMapController();
		final ReadManager readManager = mapController.getReadManager();
		final IconBuilder textBuilder = new IconBuilder();
		textBuilder.registerBy(readManager);
	}

	public ModeController getModeController() {
		return modeController;
	}
}
