/*
 * Freeplane - A Program for creating and viewing MindmapsCopyright (C) 2000-2006
 * Joerg Mueller, Daniel Polansky, Christian Foltin and others.See COPYING for
 * DetailsThis program is free software; you can redistribute it and/ormodify it
 * under the terms of the GNU General Public Licenseas published by the Free
 * Software Foundation; either version 2of the License, or (at your option) any
 * later version.This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty ofMERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See theGNU General Public License for
 * more details.You should have received a copy of the GNU General Public
 * Licensealong with this program; if not, write to the Free SoftwareFoundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA. Created on
 * 02.09.2006
 */
/*
 * $Id: ScriptingEngine.java,v 1.1.2.20 2008/04/18 21:18:26 christianfoltin Exp
 * $
 */
package org.freeplane.plugin.script;

import java.io.File;
import java.io.PrintStream;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.regex.Matcher;

import org.apache.commons.lang.WordUtils;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.attribute.NodeAttributeTableModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;

/**
 * @author foltin
 */
public class ScriptingEngine {
	public static final String SCRIPT_PREFIX = "script";
	// need a File for caching! Scripts from String have to be cached elsewhere
    private static Map<File, IScript> scriptCache = new WeakHashMap<File, IScript>();
	/**
	 * @param permissions if null use default scripting permissions.
	 * @return the result of the script, or null, if the user has cancelled.
	 * @throws ExecuteScriptException on errors
	 */
    public static Object executeScript(final NodeModel node, final String script, final IFreeplaneScriptErrorHandler pErrorHandler,
                                final PrintStream pOutStream, final ScriptExecution scriptExecution,
                                ScriptingPermissions permissions) {
    	return new ScriptRunner(new GroovyScript(script, permissions))
    		.setErrorHandler(pErrorHandler)
    		.setOutStream(pOutStream)
    		.setScriptExecution(scriptExecution)
    		.execute(node);

    }

    public static int findLineNumberInString(final String resultString, int lineNumber) {
		final java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(".*@ line ([0-9]+).*",
		    java.util.regex.Pattern.DOTALL);
		final Matcher matcher = pattern.matcher(resultString);
		if (matcher.matches()) {
			lineNumber = Integer.parseInt(matcher.group(1));
		}
		return lineNumber;
	}

	public static Object executeScript(final NodeModel node, final String script) {
    	return new ScriptRunner(new GroovyScript(script)).execute(node);
	}

	public synchronized static IScript createScriptForFile(File scriptFile, ScriptingPermissions permissions) {
		final boolean isGroovy = scriptFile.getName().endsWith(".groovy");
	    IScript script = scriptCache.get(scriptFile);
	    if (script == null || ! script.hasPermissions(permissions)) {
	        script = isGroovy ? new GroovyScript(scriptFile, permissions) : new GenericScript(scriptFile, permissions);
	        scriptCache.put(scriptFile, script);
	    }
	    return script;
    }

	public static Object executeScript(NodeModel node, String script, ScriptingPermissions permissions) {
        return new ScriptRunner(new GroovyScript(script, permissions)) //
            .execute(node);
	}

    public static Object executeScript(NodeModel node, String script, PrintStream printStream) {
        return new ScriptRunner(new GroovyScript(script)) //
            .setOutStream(printStream) //
            .execute(node);
    }

    public static Object executeScript(final NodeModel node, final String script, final ScriptExecution scriptExecution,
                                       final ScriptingPermissions permissions) {
        return new ScriptRunner(new GroovyScript(script, permissions)) //
            .setScriptExecution(scriptExecution) //
            .execute(node);
    }

	static void performScriptOperationRecursive(final NodeModel node) {
		ModeController modeController = Controller.getCurrentModeController();
		for (final NodeModel child : modeController.getMapController().childrenUnfolded(node)) {
			performScriptOperationRecursive(child);
		}
		performScriptOperation(node);
	}

	static void performScriptOperation(final NodeModel node) {
		final NodeAttributeTableModel attributes = NodeAttributeTableModel.getModel(node);
		if (attributes == null) {
			return;
		}
		for (int row = 0; row < attributes.getRowCount(); ++row) {
			final String attrKey = (String) attributes.getName(row);
			final Object value = attributes.getValue(row);
			if(value instanceof String){
				final String script = (String) value;
				if (attrKey.startsWith(ScriptingEngine.SCRIPT_PREFIX)) {
					executeScript(node, script);
				}
			}
		}
		return;
	}

	/** @deprecated use ScriptResources.getUserScriptDir() instead. */
    @Deprecated
    public static File getUserScriptDir() {
        return ScriptResources.getUserScriptDir();
    }

    static void showScriptExceptionErrorMessage(ExecuteScriptException ex) {
        if (ex.getCause() instanceof SecurityException) {
        	final String message = WordUtils.wrap(ex.getCause().getMessage(), 80, "\n    ", false);
        	UITools.errorMessage(TextUtils.format("ExecuteScriptSecurityError.text", message));
        }
        else {
        	final String message = WordUtils.wrap(ex.getMessage(), 80, "\n    ", false);
        	UITools.errorMessage(TextUtils.format("ExecuteScriptError.text", message));
        }
    }
}
