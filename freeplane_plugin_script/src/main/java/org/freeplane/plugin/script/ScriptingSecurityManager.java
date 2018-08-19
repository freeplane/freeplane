/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file author is Christian Foltin
 *  It is modified by Dimitry Polivaev in 2008.
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

import java.io.FilePermission;
import java.net.SocketPermission;
import java.security.AllPermission;
import java.security.Permission;
import java.security.Permissions;
import java.util.Enumeration;

class ScriptingSecurityManager {

    private final Permissions permissions;
	private final boolean withoutFileRestriction;
	private final boolean withoutWriteRestriction;
	private final boolean withoutNetworkRestriction;
	private final boolean withoutExecRestriction;

    public ScriptingSecurityManager(boolean withoutFileRestriction,
            boolean withoutWriteRestriction,
            boolean withoutNetworkRestriction, boolean withoutExecRestriction) {
        this.withoutFileRestriction = withoutFileRestriction;
				this.withoutWriteRestriction = withoutWriteRestriction;
				this.withoutNetworkRestriction = withoutNetworkRestriction;
				this.withoutExecRestriction = withoutExecRestriction;
		permissions = new Permissions();
		if (withoutExecRestriction && withoutFileRestriction && withoutWriteRestriction
		        && withoutNetworkRestriction) {
			permissions.add(new AllPermission());
        }
		else {
			if (withoutNetworkRestriction) {
				permissions.add(new SocketPermission("*", "connect,accept,listen,resolve"));
				permissions.add(new RuntimePermission("setFactory"));
			}

			if (withoutExecRestriction) {
				permissions.add(new FilePermission("<<ALL FILES>>", "execute"));
				permissions.add(new RuntimePermission("loadLibrary.*"));
			}
			if (withoutFileRestriction) {
				permissions.add(new FilePermission("<<ALL FILES>>", "read"));
				permissions.add(new RuntimePermission("readFileDescriptor"));
			}

			if (withoutWriteRestriction) {
				permissions.add(new RuntimePermission("writeFileDescriptor"));
				permissions.add(new FilePermission("<<ALL FILES>>", "write,delete"));
				permissions.add(new RuntimePermission("preferences"));
			}
        }
        permissions.setReadOnly();
        checkRequiredPermissions();
    }

    private static final Permission URL_PERMISSION = new SocketPermission("*", "connect");

	public boolean implies(Permission permission) {
		if (permission.getClass().getSimpleName().equals("URLPermission")) {
			return isAllowed(URL_PERMISSION);
		}
		else {
			return isAllowed(permission);
		}
	}

	private boolean isAllowed(Permission permission) {
		final boolean isAllowed = permissions.implies(permission);
		return isAllowed;
	}

	private void checkRequiredPermissions(){
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            final Enumeration<Permission> permissionElements = permissions.elements();
            while(permissionElements.hasMoreElements())
            	sm.checkPermission(permissionElements.nextElement());
        }
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (withoutExecRestriction ? 1231 : 1237);
		result = prime * result + (withoutFileRestriction ? 1231 : 1237);
		result = prime * result + (withoutNetworkRestriction ? 1231 : 1237);
		result = prime * result + (withoutWriteRestriction ? 1231 : 1237);
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
		ScriptingSecurityManager other = (ScriptingSecurityManager) obj;
		if (withoutExecRestriction != other.withoutExecRestriction)
			return false;
		if (withoutFileRestriction != other.withoutFileRestriction)
			return false;
		if (withoutNetworkRestriction != other.withoutNetworkRestriction)
			return false;
		if (withoutWriteRestriction != other.withoutWriteRestriction)
			return false;
		return true;
	}


}
