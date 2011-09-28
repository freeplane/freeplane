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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.freeplane.core.resources.ResourceController;

/**
 * @author Volker Boerchers
 */
public class ScriptingPermissions {
	final Map<String, Boolean> permissions = new LinkedHashMap<String, Boolean>();
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
	final static ScriptingPermissions restrictedPermissions = new ScriptingPermissions();
	private static ScriptingPermissions permissiveScriptingPermissions;

	public ScriptingPermissions() {
		// by default nothing is allowed
		for (String permissionName : PERMISSION_NAMES) {
			set(permissionName, false);
		}
	}
	
	public ScriptingPermissions(Properties properties) {
		// by default nothing is allowed
		this();
		for (String permissionName : PERMISSION_NAMES) {
			final Object value = properties.get(permissionName);
			if (value != null) {
				set(permissionName, Boolean.parseBoolean(value.toString()));
			}
		}
	}

	public boolean get(String permissionName) {
		// there must never be nulls in the map
		return permissions.get(permissionName).booleanValue();
	}
	
	private void set(String permissionName, boolean value) {
		permissions.put(permissionName, value);
	}

	void restorePermissions() {
		for (String permissionName : PERMISSION_NAMES) {
			restore(permissionName);
		}
	}

	private void restore(final String permissionName) {
		ResourceController.getResourceController().setProperty(permissionName, permissions.get(permissionName));
	}

	ScriptingSecurityManager getScriptingSecurityManager() {
		boolean readPerm = get(RESOURCES_EXECUTE_SCRIPTS_WITHOUT_READ_RESTRICTION);
		boolean writePerm = get(RESOURCES_EXECUTE_SCRIPTS_WITHOUT_WRITE_RESTRICTION);
		boolean networkPerm = get(RESOURCES_EXECUTE_SCRIPTS_WITHOUT_NETWORK_RESTRICTION);
		boolean execPerm = get(RESOURCES_EXECUTE_SCRIPTS_WITHOUT_EXEC_RESTRICTION);
		return new ScriptingSecurityManager(readPerm, writePerm, networkPerm, execPerm);
	}

	ScriptingSecurityManager getRestrictedScriptingSecurityManager() {
		return getRestrictedPermissions().getScriptingSecurityManager();
	}
	
	static ScriptingPermissions getRestrictedPermissions() {
		return restrictedPermissions;
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
	
	public boolean executeScriptsWithoutAsking() {
		return get(RESOURCES_EXECUTE_SCRIPTS_WITHOUT_ASKING);
	}

	public static List<String> getPermissionNames() {
		return Arrays.asList(PERMISSION_NAMES);
    }
}
