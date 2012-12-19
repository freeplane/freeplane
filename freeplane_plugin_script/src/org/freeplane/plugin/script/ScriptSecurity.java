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

import javax.swing.JOptionPane;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.components.OptionalDontShowMeAgainDialog;
import org.freeplane.core.util.TextUtils;
import org.freeplane.main.application.FreeplaneSecurityManager;

/**
 * @author Dimitry Polivaev
 * 19.12.2012
 */
public class ScriptSecurity {
	final private Object script; 
	final private ScriptingPermissions specificPermissions;
	final private PrintStream outStream;
	
	public ScriptSecurity(Object script, ScriptingPermissions specificPermissions, PrintStream outStream) {
	    super();
	    this.script = script;
	    this.specificPermissions = specificPermissions;
	    this.outStream = outStream;
    }

	ScriptingSecurityManager getScriptingSecurityManager() {
		final FreeplaneSecurityManager securityManager = (FreeplaneSecurityManager) System.getSecurityManager();
	    final ScriptingSecurityManager scriptingSecurityManager;
	    final boolean needsSecurityManager = securityManager.needsFinalSecurityManager();
	    // get preferences (and store them again after the script execution,
	    // such that the scripts are not able to change them).
	    if (needsSecurityManager) {
	    	final ScriptingPermissions permissions = permissions();
			if (!permissions.executeScriptsWithoutAsking()) {
	    		final int showResult = OptionalDontShowMeAgainDialog.show("really_execute_script", "confirmation",
	    		    ScriptingPermissions.RESOURCES_EXECUTE_SCRIPTS_WITHOUT_ASKING,
	    		    OptionalDontShowMeAgainDialog.BOTH_OK_AND_CANCEL_OPTIONS_ARE_STORED);
	    		if (showResult != JOptionPane.OK_OPTION) {
	    			throw new ExecuteScriptException(new SecurityException(TextUtils.getText("script_execution_disabled")));
	    		}
	    	}
	    	final boolean executeSignedScripts = permissions.isExecuteSignedScriptsWithoutRestriction();
	    	final String scriptContent;
	    	if(script instanceof String)
	    		scriptContent = (String) script;
	    	else
	    		scriptContent = null;
	    	if (executeSignedScripts && scriptContent != null && new SignedScriptHandler().isScriptSigned(scriptContent, outStream)) {
	            scriptingSecurityManager = permissions.getPermissiveScriptingSecurityManager();
	        }
	        else
	    		scriptingSecurityManager = permissions.getScriptingSecurityManager();
	    }
	    else {
	    	// will not be used
	    	scriptingSecurityManager = null;
	    }
	    return scriptingSecurityManager;
    }
	
	void checkScriptExecutionEnabled() {
		final FreeplaneSecurityManager securityManager = (FreeplaneSecurityManager) System.getSecurityManager();
		final boolean needsSecurityManager = securityManager.needsFinalSecurityManager();
		// get preferences (and store them again after the script execution,
		// such that the scripts are not able to change them).
		if (needsSecurityManager) {
			if (!permissions().executeScriptsWithoutAsking()) {
				final int showResult = OptionalDontShowMeAgainDialog.show("really_execute_script", "confirmation",
				    ScriptingPermissions.RESOURCES_EXECUTE_SCRIPTS_WITHOUT_ASKING,
				    OptionalDontShowMeAgainDialog.BOTH_OK_AND_CANCEL_OPTIONS_ARE_STORED);
				if (showResult != JOptionPane.OK_OPTION) {
					throw new ExecuteScriptException(new SecurityException(TextUtils.getText("script_execution_disabled")));
				}
			}
		}
    }
	
	private ScriptingPermissions permissions() {
		if(specificPermissions != null)
	        return specificPermissions;
        else
	        return new ScriptingPermissions(ResourceController.getResourceController().getProperties());
    }


}
