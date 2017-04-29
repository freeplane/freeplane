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

    final private Permissions permissions;

    public ScriptingSecurityManager(boolean pWithoutFileRestriction,
            boolean pWithoutWriteRestriction,
            boolean pWithoutNetworkRestriction, boolean pWithoutExecRestriction) {
        permissions = new Permissions();
		if (pWithoutExecRestriction && pWithoutFileRestriction && pWithoutWriteRestriction
		        && pWithoutNetworkRestriction) {
			permissions.add(new AllPermission());
        }
		else {
			if (pWithoutNetworkRestriction) {
				permissions.add(new SocketPermission("*", "connect,accept,listen,resolve"));
				permissions.add(new RuntimePermission("setFactory"));
			}

			if (pWithoutExecRestriction) {
				permissions.add(new FilePermission("<<ALL FILES>>", "execute"));
				permissions.add(new RuntimePermission("loadLibrary.*"));
			}
			if (pWithoutFileRestriction) {
				permissions.add(new FilePermission("<<ALL FILES>>", "read"));
				permissions.add(new RuntimePermission("readFileDescriptor"));
			}

			if (pWithoutWriteRestriction) {
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
}
