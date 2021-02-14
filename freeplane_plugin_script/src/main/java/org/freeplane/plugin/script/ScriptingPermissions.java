/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file author is Christian Foltin
 *  It is modified by Volker Boerchers in 2011.
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.JOptionPane;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.components.OptionalDontShowMeAgainDialog;
import org.freeplane.core.ui.components.OptionalDontShowMeAgainDialog.MessageType;
import org.freeplane.core.util.TextUtils;

/**
 * @author Volker Boerchers
 */
public class ScriptingPermissions {
	final Map<String, Boolean> permissions;
	public static final String RESOURCES_EXECUTE_SCRIPTS_WITHOUT_ASKING = "execute_scripts_without_asking";
	public static final String RESOURCES_EXECUTE_SCRIPTS_WITHOUT_READ_RESTRICTION = "execute_scripts_without_file_restriction";
	public static final String RESOURCES_EXECUTE_SCRIPTS_WITHOUT_WRITE_RESTRICTION = "execute_scripts_without_write_restriction";
	public static final String RESOURCES_EXECUTE_SCRIPTS_WITHOUT_EXEC_RESTRICTION = "execute_scripts_without_exec_restriction";
	public static final String RESOURCES_EXECUTE_SCRIPTS_WITHOUT_NETWORK_RESTRICTION = "execute_scripts_without_network_restriction";
	public static final String RESOURCES_SIGNED_SCRIPT_ARE_TRUSTED = "signed_script_are_trusted";
	public static final String RESOURCES_SCRIPT_USER_KEY_NAME_FOR_SIGNING = "script_user_key_name_for_signing";
	public static final String[] PERMISSION_NAMES = { //
		RESOURCES_EXECUTE_SCRIPTS_WITHOUT_ASKING //
        , RESOURCES_EXECUTE_SCRIPTS_WITHOUT_READ_RESTRICTION //
        , RESOURCES_EXECUTE_SCRIPTS_WITHOUT_WRITE_RESTRICTION //
        , RESOURCES_EXECUTE_SCRIPTS_WITHOUT_EXEC_RESTRICTION //
        , RESOURCES_EXECUTE_SCRIPTS_WITHOUT_NETWORK_RESTRICTION //
        , RESOURCES_SIGNED_SCRIPT_ARE_TRUSTED //
	};
	private static ScriptingPermissions formulaPermissions;
	private static ScriptingPermissions permissiveScriptingPermissions;

	public ScriptingPermissions() {
		 permissions = new HashMap<String, Boolean>();
		// by default nothing is allowed
		for (String permissionName : PERMISSION_NAMES) {
			set(permissionName, false);
		}
	}

	public ScriptingPermissions(Properties properties) {
		this();
		for (String permissionName : PERMISSION_NAMES) {
			final Object value = properties.get(permissionName);
			if (value != null) {
				final String valueString = value.toString();
				if(! "".equals(valueString))
					set(permissionName, Boolean.parseBoolean(valueString));
			}
		}
	}



	public ScriptingPermissions(Map<String, Boolean> permissions) {
		this();
		this.permissions.putAll(permissions);
	}

	public boolean get(String permissionName) {
		final Boolean savedValue = permissions.get(permissionName);
		return savedValue != null && savedValue.booleanValue();
	}

	private void set(String permissionName, boolean value) {
		permissions.put(permissionName, value);
	}

//	void restorePermissions() {
//		for (String permissionName : PERMISSION_NAMES) {
//			restore(permissionName);
//		}
//	}
//
//	private void restore(final String permissionName) {
//		final Boolean savedValue = permissions.get(permissionName);
//		if (savedValue != null)
//			ResourceController.getResourceController().setProperty(permissionName, savedValue);
//		else
//			ResourceController.getResourceController().setProperty(permissionName, "");
//	}
//
	ScriptingSecurityManager getScriptingSecurityManager() {
		boolean readPerm = get(RESOURCES_EXECUTE_SCRIPTS_WITHOUT_READ_RESTRICTION);
		boolean writePerm = get(RESOURCES_EXECUTE_SCRIPTS_WITHOUT_WRITE_RESTRICTION);
		boolean networkPerm = get(RESOURCES_EXECUTE_SCRIPTS_WITHOUT_NETWORK_RESTRICTION);
		boolean execPerm = get(RESOURCES_EXECUTE_SCRIPTS_WITHOUT_EXEC_RESTRICTION);
		return new ScriptingSecurityManager(readPerm, writePerm, networkPerm, execPerm);
	}

	/** this method is called only if the formula plugin is active and so formula evaluation is allowed. */
	public static ScriptingPermissions getFormulaPermissions() {
		if (formulaPermissions == null) {
			formulaPermissions = new ScriptingPermissions();
			formulaPermissions.set(RESOURCES_EXECUTE_SCRIPTS_WITHOUT_ASKING, true);
			// the classpath is set by the user - this forces us to loose the permissions a bit (if the user permits it)
			if (ScriptResources.getClasspath() != null) {
				formulaPermissions.set(RESOURCES_EXECUTE_SCRIPTS_WITHOUT_READ_RESTRICTION, ResourceController
				    .getResourceController().getBooleanProperty(RESOURCES_EXECUTE_SCRIPTS_WITHOUT_READ_RESTRICTION));
			}
		}
		return formulaPermissions;
	}

	ScriptingSecurityManager getPermissiveScriptingSecurityManager() {
		return new ScriptingSecurityManager(true, true, true, true);
	}

	public static ScriptingPermissions getPermissiveScriptingPermissions() {
		if (permissiveScriptingPermissions == null) {
			permissiveScriptingPermissions = new ScriptingPermissions();
			permissiveScriptingPermissions.set(RESOURCES_EXECUTE_SCRIPTS_WITHOUT_ASKING, true);
			permissiveScriptingPermissions.set(RESOURCES_EXECUTE_SCRIPTS_WITHOUT_READ_RESTRICTION, true);
			permissiveScriptingPermissions.set(RESOURCES_EXECUTE_SCRIPTS_WITHOUT_WRITE_RESTRICTION, true);
			permissiveScriptingPermissions.set(RESOURCES_EXECUTE_SCRIPTS_WITHOUT_NETWORK_RESTRICTION, true);
			permissiveScriptingPermissions.set(RESOURCES_EXECUTE_SCRIPTS_WITHOUT_EXEC_RESTRICTION, true);
		}
		return permissiveScriptingPermissions;
	}

	boolean isExecuteSignedScriptsWithoutRestriction() {
		return get(RESOURCES_SIGNED_SCRIPT_ARE_TRUSTED);
	}

	private boolean executeScriptsWithoutAsking() {
		return get(RESOURCES_EXECUTE_SCRIPTS_WITHOUT_ASKING);
	}

	public static List<String> getPermissionNames() {
		return Arrays.asList(PERMISSION_NAMES);
    }

	public void assertScriptExecutionAllowed() {
		if (! executeScriptsWithoutAsking()) {
    		final int showResult = OptionalDontShowMeAgainDialog.show("really_execute_script", "confirmation",
    		    ScriptingPermissions.RESOURCES_EXECUTE_SCRIPTS_WITHOUT_ASKING,
    		    MessageType.BOTH_OK_AND_CANCEL_OPTIONS_ARE_STORED);
    		if (showResult != JOptionPane.OK_OPTION) {
    			throw new ExecuteScriptException(new SecurityException(TextUtils.getText("script_execution_disabled")));
    		}
    	}
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((permissions == null) ? 0 : permissions.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ScriptingPermissions other = (ScriptingPermissions) obj;
        if (permissions == null) {
            if (other.permissions != null)
                return false;
        }
        else if (!permissions.equals(other.permissions))
            return false;
        return true;
    }
}
