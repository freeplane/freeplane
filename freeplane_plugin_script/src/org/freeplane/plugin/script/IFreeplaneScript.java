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

import groovy.lang.Script;

import java.io.PrintStream;

import org.freeplane.features.map.NodeModel;

/**
 * @author Dimitry Polivaev
 * 17.12.2012
 */
public interface IFreeplaneScript {
	public static final String RESOURCES_SCRIPT_DIRECTORIES = "script_directories";
	public static final String RESOURCES_SCRIPT_CLASSPATH = "script_classpath";
	public static final IFreeplaneScriptErrorHandler IGNORING_SCRIPT_ERROR_HANDLER = new IFreeplaneScriptErrorHandler() {
		public void gotoLine(final int pLineNumber) {
		}
	};

	public IFreeplaneScriptErrorHandler getErrorHandler();

	public IFreeplaneScript setErrorHandler(IFreeplaneScriptErrorHandler pErrorHandler);

	public PrintStream getpOutStream();

	public IFreeplaneScript setOutStream(PrintStream outStream);

	public ScriptContext getScriptContext();

	public IFreeplaneScript setScriptContext(ScriptContext scriptContext);

	public Object getScript();

	public ScriptingPermissions getSpecificPermissions();

	public Object execute(final NodeModel node);
}
