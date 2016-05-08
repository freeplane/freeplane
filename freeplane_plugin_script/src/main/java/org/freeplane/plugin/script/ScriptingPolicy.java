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
import java.security.Permission;
import java.security.Permissions;
import java.security.Policy;
import java.security.ProtectionDomain;
import java.util.PropertyPermission;

import org.osgi.framework.AdminPermission;

/**
 * @author Dimitry Polivaev
 * Apr 9, 2016
 */
class ScriptingPolicy extends Policy {
	private static final boolean DISABLE_CHECKS = Boolean
	    .getBoolean("org.freeplane.main.application.FreeplaneSecurityManager.disable");
	final private Policy defaultPolicy;
	final private Permissions permissions;
	final private Permissions permissionBlackList;

	public ScriptingPolicy(Policy policy) {
		this.defaultPolicy = policy;
		permissions = new Permissions();
		permissionBlackList = new Permissions();
		permissionBlackList.add(new PropertyPermission("org.freeplane.basedirectory", "write"));
		permissions.add(new RuntimePermission("accessDeclaredMembers"));
		permissions.add(new RuntimePermission("accessClassInPackage.*"));
		permissions.add(new RuntimePermission("getProtectionDomain"));
		permissions.add(new RuntimePermission("modifyThreadGroup"));
		permissions.add(new RuntimePermission("queuePrintJob"));
		permissions.add(new RuntimePermission("setIO"));
		permissions.add(new RuntimePermission("exitVM.0"));
		permissions.add(new PropertyPermission("*", "read,write"));
		permissions.add(new AdminPermission("*", "resolve,resource"));
		permissions.add(new AWTPermission("showWindowWithoutWarningBanner"));
		permissions.add(new AWTPermission("accessClipboard"));
		permissions.add(new AWTPermission("accessEventQueue"));
	}

	@Override
	public boolean implies(ProtectionDomain domain, Permission permission) {
		if (DISABLE_CHECKS || defaultPolicy.implies(domain, permission)) {
			return true;
		}
		if (permissions.implies(permission)) {
			return !permissionBlackList.implies(permission);
		}
		for (ClassLoader classLoader = domain.getClassLoader(); classLoader != null; //
		classLoader = classLoader.getParent()) {
			if (classLoader instanceof ScriptClassLoader) {
				return ((ScriptClassLoader) classLoader).implies(permission) && !permissionBlackList.implies(permission);
			}
		}
		return false;
	}

	static public void installRestrictingPolicy() {
		ScriptClassLoader.class.getClassLoader();
		Policy.setPolicy(new ScriptingPolicy(Policy.getPolicy()));
	}
}