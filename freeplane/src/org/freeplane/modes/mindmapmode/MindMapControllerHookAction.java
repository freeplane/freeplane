/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is modified by Dimitry Polivaev in 2008.
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
package org.freeplane.modes.mindmapmode;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import deprecated.freemind.extensions.IHookFactory;
import deprecated.freemind.extensions.IModeControllerHook;
import deprecated.freemind.modes.mindmapmode.actions.undo.IHookAction;

class MindMapControllerHookAction extends AbstractAction implements IHookAction {
	String mHookName;
	MModeController mindMapController;

	public MindMapControllerHookAction(final String hookName,
	                                   final MModeController mindMapController) {
		super(hookName);
		mHookName = hookName;
		this.mindMapController = mindMapController;
	}

	public void actionPerformed(final ActionEvent arg0) {
		final IHookFactory hookFactory = mindMapController.getHookFactory();
		final IModeControllerHook hook = hookFactory
		    .createIModeControllerHook(mHookName);
		hook.setController(mindMapController);
		mindMapController.invokeHook(hook);
	}

	public String getHookName() {
		return mHookName;
	}
}
