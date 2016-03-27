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

import java.io.File;
import java.io.FilePermission;
import java.net.SocketPermission;
import java.security.Permission;
import java.security.Permissions;
import java.util.concurrent.Callable;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.main.application.SecureRunner;

import sun.security.util.SecurityConstants;

class ScriptingSecurityManager {

    final private Permissions blackList;

    final private Permissions whiteList;

    final private SecureRunner secureRunner;

    public ScriptingSecurityManager(boolean pWithoutFileRestriction,
            boolean pWithoutWriteRestriction,
            boolean pWithoutNetworkRestriction, boolean pWithoutExecRestriction) {
        blackList = new Permissions();
        whiteList = new Permissions();
        if (!pWithoutNetworkRestriction) {
            blackList(new SocketPermission("*", SecurityConstants.SOCKET_ACCEPT_ACTION));
            blackList(new SocketPermission("*", SecurityConstants.SOCKET_CONNECT_ACTION));
            blackList(new SocketPermission("*", SecurityConstants.SOCKET_LISTEN_ACTION));
            blackList(new SocketPermission("*",
                    SecurityConstants.SOCKET_CONNECT_ACCEPT_ACTION));
            blackList(new RuntimePermission("setFactory"));
        }

        if (!pWithoutExecRestriction) {
            blackList(new FilePermission("<<ALL FILES>>",
                    SecurityConstants.FILE_EXECUTE_ACTION));
            blackList(new RuntimePermission("loadLibrary.*"));
        }

        if (!pWithoutFileRestriction) {
            whiteList(new FilePermission(ResourceController.getResourceController()
                    .getInstallationBaseDir() + File.separatorChar + "*",
                    SecurityConstants.FILE_READ_ACTION));
            whiteList(new FilePermission(System.getProperty("java.home") + "*",
                    SecurityConstants.FILE_READ_ACTION));
            blackList(new FilePermission("*", SecurityConstants.FILE_READ_ACTION));
            blackList(new RuntimePermission("readFileDescriptor"));
        }
        if (!pWithoutWriteRestriction) {
            blackList(new RuntimePermission("writeFileDescriptor"));
            blackList(new FilePermission("*", SecurityConstants.FILE_WRITE_ACTION));
            blackList(new FilePermission("*",
                    SecurityConstants.FILE_DELETE_ACTION));
        }
        blackList.setReadOnly();
        whiteList.setReadOnly();
        secureRunner = new SecureRunner();
    }

    private void blackList(Permission permission) {
        blackList.add(permission);
    }

    private void whiteList(Permission permission) {
        whiteList.add(permission);
    }

    public Object call(Callable<?> callable) throws Exception {
        return secureRunner.call(whiteList, blackList, callable);
    }

}
