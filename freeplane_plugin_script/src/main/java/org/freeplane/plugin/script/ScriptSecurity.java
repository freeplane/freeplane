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

import org.freeplane.core.resources.ResourceController;

/**
 * @author Dimitry Polivaev 19.12.2012
 */
public class ScriptSecurity {
    final private Object script;

    final private ScriptingPermissions specificPermissions;

    final private PrintStream outStream;

    public ScriptSecurity(Object script, ScriptingPermissions specificPermissions,
            PrintStream outStream) {
        super();
        this.script = script;
        this.specificPermissions = specificPermissions;
        this.outStream = outStream;
    }

    ScriptingSecurityManager getScriptingSecurityManager() {
        final ScriptingSecurityManager scriptingSecurityManager;
        // get preferences (and store them again after the script execution,
        // such that the scripts are not able to change them).
        final ScriptingPermissions permissions = permissions();
        permissions.assertScriptExecutionAllowed();
        final boolean executeSignedScripts = permissions
                .isExecuteSignedScriptsWithoutRestriction();
        if (executeSignedScripts && isSignedScript()) {
            scriptingSecurityManager = permissions.getPermissiveScriptingSecurityManager();
        } else {
            scriptingSecurityManager = permissions.getScriptingSecurityManager();
        }
        return scriptingSecurityManager;
    }

    private boolean isSignedScript() {
        return script instanceof String && new SignedScriptHandler().isScriptSigned((String) script,
                outStream);
    }

    private ScriptingPermissions permissions() {
        if (specificPermissions != null) {
            return specificPermissions;
        } else {
            return new ScriptingPermissions(ResourceController.getResourceController()
                    .getProperties());
        }
    }

}
