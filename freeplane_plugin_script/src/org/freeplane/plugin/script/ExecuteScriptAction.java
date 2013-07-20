/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2009 Volker Boerchers
 *
 *  This file author is Volker Boerchers
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
 *  along with this program.  If not, see &lt;http://www.gnu.org/licenses/&gt;.
 */
package org.freeplane.plugin.script;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.mindmapmode.MModeController;

/**
 * Action that executes a script defined by filename.
 * 
 * @author vboerchers
 */
public class ExecuteScriptAction extends AFreeplaneAction {
	private static final long serialVersionUID = 1L;

	/** controls how often a script is executed in case of a multi selection. */
	public enum ExecutionMode {
		/** once with <code>node</code> set to one selected (random) node. */
		ON_SINGLE_NODE,
		/** n times for n selected nodes, once for each node. */
		ON_SELECTED_NODE,
		/** script on every selected node and recursively on all of its children. */
		ON_SELECTED_NODE_RECURSIVELY
	}

	private final File script;
	private final ExecutionMode mode;
	private ScriptingPermissions permissions;
	private boolean allowedDuringEditing = false;

	public ExecuteScriptAction(final String scriptName, final String menuItemName, final String script,
	                           final ExecutionMode mode, final boolean cacheContent, ScriptingPermissions permissions) {
		super(ExecuteScriptAction.makeMenuItemKey(scriptName, mode), menuItemName, null);
		this.script = new File(script);
		this.mode = mode;
		this.permissions = permissions;
	}

	public static String makeMenuItemKey(final String scriptName, final ExecutionMode mode) {
		return scriptName + "_" + mode.toString().toLowerCase();
	}

	public void actionPerformed(final ActionEvent e) {
		Controller.getCurrentController().getViewController().setWaitingCursor(true);
		try {
			final List<NodeModel> nodes = new ArrayList<NodeModel>();
			if (mode == ExecutionMode.ON_SINGLE_NODE) {
				nodes.add(Controller.getCurrentController().getSelection().getSelected());
			}
			else {
				nodes.addAll(Controller.getCurrentController().getSelection().getSelection());
			}
			final MModeController modeController = (MModeController) Controller.getCurrentModeController();
			modeController.startTransaction();
			for (final NodeModel node : nodes) {
				try {
					if (mode == ExecutionMode.ON_SELECTED_NODE_RECURSIVELY) {
						// TODO: ensure that a script is invoked only once on every node?
						// (might be a problem with recursive actions if parent and child
						// are selected.)
						ScriptingEngine.executeScriptRecursive(node, script, permissions);
					}
					else {
						ScriptingEngine.executeScript(node, script, permissions);
					}
                }
				catch (ExecuteScriptException ex) {
				    LogUtils.warn("error executing script " + script + " - giving up", ex);
				    modeController.delayedRollback();
					ScriptingEngine.showScriptExceptionErrorMessage(ex);
                	return;
                }
			}
			modeController.delayedCommit();
		}
		finally {
			Controller.getCurrentController().getViewController().setWaitingCursor(false);
		}
	}

	public void setAllowedDuringEditing(boolean allowedDuringEditing) {
    	this.allowedDuringEditing = allowedDuringEditing;
    }
	
	
}
