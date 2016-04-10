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

import java.security.Permission;
import java.security.Permissions;
import java.security.Policy;
import java.security.ProtectionDomain;
import java.util.PropertyPermission;

/**
 * @author Dimitry Polivaev
 * Apr 9, 2016
 */
class ScriptingPolicy extends Policy {
	private static final boolean DISABLE_CHECKS = Boolean
	    .getBoolean("org.freeplane.main.application.FreeplaneSecurityManager.disable");
	final private Policy defaultPolicy;
	private Permissions permissions;

	public ScriptingPolicy(Policy policy) {
		this.defaultPolicy = policy;
		permissions = new Permissions();
		permissions.add(new RuntimePermission("accessDeclaredMembers"));
		permissions.add(new RuntimePermission("accessClassInPackage.*"));
		permissions.add(new PropertyPermission("*", "read"));
	}

	@Override
	public boolean implies(ProtectionDomain domain, Permission permission) {
		if (DISABLE_CHECKS || defaultPolicy.implies(domain, permission) || permissions.implies(permission)) {
			return true;
		}
		for (ClassLoader classLoader = domain.getClassLoader(); classLoader != null; //
		classLoader = classLoader.getParent()) {
			if (classLoader instanceof ScriptClassLoader) {
				return ((ScriptClassLoader) classLoader).implies(permission);
			}
		}
		return false;
	}

	static public void installRestrictingPolicy() {
		ScriptClassLoader.class.getClassLoader();
		Policy.setPolicy(new ScriptingPolicy(Policy.getPolicy()));
	}
}