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

import org.freeplane.core.resources.ResourceController;

/**
 * @author Volker Boerchers
 */
class ScriptingPermissions {
	public static final String RESOURCES_EXECUTE_SCRIPTS_WITHOUT_ASKING = "execute_scripts_without_asking";
	public static final String RESOURCES_EXECUTE_SCRIPTS_WITHOUT_READ_RESTRICTION = "execute_scripts_without_file_restriction";
	public static final String RESOURCES_EXECUTE_SCRIPTS_WITHOUT_WRITE_RESTRICTION = "execute_scripts_without_write_restriction";
	public static final String RESOURCES_EXECUTE_SCRIPTS_WITHOUT_EXEC_RESTRICTION = "execute_scripts_without_exec_restriction";
	public static final String RESOURCES_EXECUTE_SCRIPTS_WITHOUT_NETWORK_RESTRICTION = "execute_scripts_without_network_restriction";
	public static final String RESOURCES_SCRIPT_USER_KEY_NAME_FOR_SIGNING = "script_user_key_name_for_signing";
	public static final String RESOURCES_SIGNED_SCRIPT_ARE_TRUSTED = "signed_script_are_trusted";
	private String executeWithoutAsking = "false";
	private String executeWithoutReadRestriction = "false";
	private String executeWithoutWriteRestriction = "false";
	private String executeWithoutNetworkRestriction = "false";
	private String executeWithoutExecRestriction = "false";
	private String signedScriptsWithoutRestriction = "false";

	void initFromPreferences() {
		executeWithoutAsking = ResourceController.getResourceController().getProperty(
		    ScriptingPermissions.RESOURCES_EXECUTE_SCRIPTS_WITHOUT_ASKING);
		executeWithoutReadRestriction = ResourceController.getResourceController().getProperty(
		    ScriptingPermissions.RESOURCES_EXECUTE_SCRIPTS_WITHOUT_READ_RESTRICTION);
		executeWithoutWriteRestriction = ResourceController.getResourceController().getProperty(
		    ScriptingPermissions.RESOURCES_EXECUTE_SCRIPTS_WITHOUT_WRITE_RESTRICTION);
		executeWithoutNetworkRestriction = ResourceController.getResourceController().getProperty(
		    ScriptingPermissions.RESOURCES_EXECUTE_SCRIPTS_WITHOUT_NETWORK_RESTRICTION);
		executeWithoutExecRestriction = ResourceController.getResourceController().getProperty(
		    ScriptingPermissions.RESOURCES_EXECUTE_SCRIPTS_WITHOUT_EXEC_RESTRICTION);
		signedScriptsWithoutRestriction = ResourceController.getResourceController().getProperty(
		    ScriptingPermissions.RESOURCES_SIGNED_SCRIPT_ARE_TRUSTED);
	}

	void restorePermissions() {
		ResourceController.getResourceController().setProperty(
		    ScriptingPermissions.RESOURCES_EXECUTE_SCRIPTS_WITHOUT_ASKING, executeWithoutAsking);
		ResourceController.getResourceController().setProperty(
		    ScriptingPermissions.RESOURCES_EXECUTE_SCRIPTS_WITHOUT_READ_RESTRICTION, executeWithoutReadRestriction);
		ResourceController.getResourceController().setProperty(
		    ScriptingPermissions.RESOURCES_EXECUTE_SCRIPTS_WITHOUT_NETWORK_RESTRICTION,
		    executeWithoutNetworkRestriction);
		ResourceController.getResourceController().setProperty(
		    ScriptingPermissions.RESOURCES_EXECUTE_SCRIPTS_WITHOUT_EXEC_RESTRICTION, executeWithoutExecRestriction);
		ResourceController.getResourceController().setProperty(
		    ScriptingPermissions.RESOURCES_SIGNED_SCRIPT_ARE_TRUSTED, signedScriptsWithoutRestriction);
	}

	ScriptingSecurityManager getScriptingSecurityManager() {
		boolean readPerm = Boolean.parseBoolean(executeWithoutReadRestriction);
		boolean writePerm = Boolean.parseBoolean(executeWithoutWriteRestriction);
		boolean networkPerm = Boolean.parseBoolean(executeWithoutNetworkRestriction);
		boolean execPerm = Boolean.parseBoolean(executeWithoutExecRestriction);
		return new ScriptingSecurityManager(readPerm, writePerm, networkPerm, execPerm);
	}

	ScriptingSecurityManager getRestrictedScriptingSecurityManager() {
		return new ScriptingSecurityManager(false, false, false, false);
	}

	ScriptingSecurityManager getPermissiveScriptingSecurityManager() {
		return new ScriptingSecurityManager(true, true, true, true);
	}

	boolean isExecuteSignedScriptsWithoutRestriction() {
		return Boolean.parseBoolean(signedScriptsWithoutRestriction);
	}
}
