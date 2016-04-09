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
import java.security.Permission;
import java.security.Permissions;

class ScriptingSecurityManager {

    final private Permissions permissions;

    public ScriptingSecurityManager(boolean pWithoutFileRestriction,
            boolean pWithoutWriteRestriction,
            boolean pWithoutNetworkRestriction, boolean pWithoutExecRestriction) {
        permissions = new Permissions();
		if (pWithoutNetworkRestriction) {
			whiteList(new SocketPermission("*", "connect,accept,listen,resolve"));
			whiteList(new RuntimePermission("setFactory"));
        }

		if (pWithoutExecRestriction) {
			whiteList(new FilePermission("<<ALL FILES>>", "execute"));
			whiteList(new RuntimePermission("loadLibrary.*"));
        }

		if (pWithoutFileRestriction) {
			whiteList(new FilePermission("*", "read"));
			whiteList(new RuntimePermission("readFileDescriptor"));
        }
		if (pWithoutWriteRestriction) {
			whiteList(new RuntimePermission("writeFileDescriptor"));
			whiteList(new FilePermission("*", "write,delete"));
        }
        permissions.setReadOnly();
    }

    private void whiteList(Permission permission) {
        permissions.add(permission);
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
}
