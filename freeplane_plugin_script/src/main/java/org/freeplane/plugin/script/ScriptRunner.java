/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2012 Dimitry
 *
 *  This file author is Dimitry
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
package org.freeplane.plugin.script;

import java.io.PrintStream;

import org.freeplane.features.map.NodeModel;

/**
 * @author Dimitry Polivaev
 * 17.12.2012
 */
public class ScriptRunner {
	private final IScript script;
	private IFreeplaneScriptErrorHandler errorHandler = ScriptResources.IGNORING_SCRIPT_ERROR_HANDLER;
	private PrintStream outStream = System.out;
	private ScriptContext scriptContext;

	public ScriptRunner(IScript script) {
		this.script = script;

	}
    public ScriptRunner setErrorHandler(IFreeplaneScriptErrorHandler errorHandler) {
    	this.errorHandler = errorHandler;
		return this;
    }

	public ScriptRunner setOutStream(PrintStream outStream) {
    	this.outStream = outStream;
		return this;
    }

	public ScriptRunner setScriptContext(ScriptContext scriptContext) {
    	this.scriptContext = scriptContext;
		return this;
    }

	public Object execute(final NodeModel node) {
		return script.execute(node, outStream, errorHandler, scriptContext);
	}
}
