/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2016 dimitry
 *
 *  This file author is dimitry
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

import java.awt.AWTPermission;
import java.io.File;
import java.io.FilePermission;
import java.net.URL;
import java.security.AllPermission;
import java.security.CodeSource;
import java.security.Permission;
import java.security.Permissions;
import java.security.Policy;
import java.security.ProtectionDomain;
import java.security.cert.Certificate;
import java.util.PropertyPermission;
import java.util.logging.LoggingPermission;

import org.freeplane.core.util.Compat;
import org.freeplane.main.application.ApplicationResourceController;
import org.osgi.framework.AdminPermission;

/**
 * @author Dimitry Polivaev
 * Apr 9, 2016
 */
class ScriptingPolicy extends Policy {
	private static final AllPermission ALL_PERMISSION = new AllPermission();
	final private Policy defaultPolicy;
	final private Permissions permissions;
	final private Permissions permissionBlackList;
	final private CodeSource userLibCodeSource;

	public ScriptingPolicy(Policy policy) {
		this.defaultPolicy = policy;
		permissions = new Permissions();
		permissionBlackList = new Permissions();
		permissionBlackList.add(new PropertyPermission(ApplicationResourceController.FREEPLANE_BASEDIRECTORY_PROPERTY, "write"));
		permissionBlackList.add(new PropertyPermission(Compat.FREEPLANE_USERDIR_PROPERTY, "write"));
		CodeSource userLibCodeSource;
		try {
			final String userLibDirectory = Compat.getApplicationUserDirectory() + "/lib/-";
			final URL userLibUrl = Compat.fileToUrl(new File (userLibDirectory));
			userLibCodeSource = new CodeSource(userLibUrl, (Certificate[])null);
			permissionBlackList.add(new FilePermission(userLibDirectory, "write,delete"));
			final String applicationDirectory = new File(System.getProperty(ApplicationResourceController.FREEPLANE_BASEDIRECTORY_PROPERTY)).getCanonicalPath();
			permissionBlackList.add(new FilePermission(applicationDirectory + "/-", "write,delete"));
		} catch (Exception e) {
			userLibCodeSource = new CodeSource(null, (Certificate[])null);
		}
		this.userLibCodeSource = userLibCodeSource;
		permissions.add(new RuntimePermission("accessDeclaredMembers"));
		permissions.add(new RuntimePermission("accessClassInPackage.*"));
		permissions.add(new RuntimePermission("getProtectionDomain"));
		permissions.add(new RuntimePermission("modifyThreadGroup"));
		permissions.add(new RuntimePermission("queuePrintJob"));
		permissions.add(new RuntimePermission("setIO"));
		permissions.add(new RuntimePermission("exitVM.0"));
		permissions.add(new RuntimePermission("setContextClassLoader"));
		permissions.add(new PropertyPermission("*", "read,write"));
		permissions.add(new AdminPermission("*", "resolve,resource"));
		permissions.add(new AWTPermission("showWindowWithoutWarningBanner"));
		permissions.add(new AWTPermission("accessClipboard"));
		permissions.add(new AWTPermission("accessEventQueue"));
		permissions.add(new AWTPermission("setWindowAlwaysOnTop"));
		permissions.add(new FilePermission(Compat.getApplicationUserDirectory() + "/resources/-", "read"));
		permissions.add(new FilePermission(Compat.getApplicationUserDirectory() + "/icons/-", "read"));
		permissions.add(new LoggingPermission("control", ""));
	}


	@Override
	public boolean implies(ProtectionDomain domain, Permission permission) {
		if (defaultPolicy.implies(domain, permission) || //
				userLibCodeSource.implies(domain.getCodeSource())) {
			return true;
		}
		final Permission requiredPermission = permissionBlackList.implies(permission) ? ALL_PERMISSION : permission;

		if (permissions.implies(requiredPermission)) {
			return true;
		}

		for (ClassLoader classLoader = domain.getClassLoader(); classLoader != null; //
		classLoader = classLoader.getParent()) {
			if (classLoader instanceof ScriptClassLoader) {
				return ((ScriptClassLoader) classLoader).implies(requiredPermission);
			}
		}
		return false;
	}

	static public void installRestrictingPolicy() {
		ScriptClassLoader.class.getClassLoader();
		Policy.setPolicy(new ScriptingPolicy(Policy.getPolicy()));
	}
}