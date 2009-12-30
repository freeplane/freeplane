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
import java.io.FileReader;
import java.io.IOException;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.LogTool;
import org.freeplane.features.mindmapmode.MModeController;

/**
 * Action that executes a script defined by filename.
 * 
 * @author vboerchers
 */
//not needed: @ActionLocationDescriptor(locations = { "/menu_bar/extras/first/scripting" })
public class ExecuteScriptAction extends AFreeplaneAction {
	private static final long serialVersionUID = 1L;

	public enum ExecutionMode {
		ON_SELECTED_NODE,
		ON_SELECTED_NODE_RECURSIVELY
	}

	final private ScriptingEngine engine;
	private final String script;
	private ExecutionMode mode;

	public ExecuteScriptAction(final Controller controller, ScriptingEngine engine, String name, String menuItemName, String script, ExecutionMode mode) {
	    super(name, controller, menuItemName, null);
	    this.engine = engine;
	    this.script = script;
	    this.mode = mode;
    }

    public void actionPerformed(ActionEvent e) {
		getController().getViewController().setWaitingCursor(true);
		boolean result = true;
		try {
			String scriptContent = readScript();
			// TODO: ensure that a script is invoked only once on every node?
			// (might be a problem with recursive actions if parent and child
			// are selected.)
			for (NodeModel node : getController().getSelection().getSelection()) {
				if (mode == ExecutionMode.ON_SELECTED_NODE) {
					result = engine.executeScript(
							(MModeController) getModeController(), node,
							scriptContent);
				} else {
					result = engine.executeScriptRecursive(
							(MModeController) getModeController(), node,
							scriptContent);
				}
				if (!result) {
					LogTool.warn("error executing script " + script + " - giving up");
					UITools.errorMessage(ResourceBundles.getText("ExecuteScriptError.text"));
					break;
				}
			}
		}
		catch (IOException ex) {
			LogTool.warn("error reading " + script, ex);
			UITools.errorMessage(ResourceBundles.getText("ReadScriptError.text"));
		}
		finally{
			getController().getViewController().setWaitingCursor(false);
		}
    }


	private String readScript() throws IOException {
		FileReader in = new FileReader(script);
		StringBuilder builder = new StringBuilder();
		final char[] buf = new char[1024];
		int len;
		while ((len = in.read(buf)) > 0) {
			builder.append(buf, 0, len);
		}
		return builder.toString();
	}
}
