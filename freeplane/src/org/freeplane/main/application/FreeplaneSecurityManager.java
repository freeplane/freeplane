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
package org.freeplane.main.application;

import java.io.FileDescriptor;
import java.net.InetAddress;
import java.security.Permission;

/**
 * By default, everything is allowed. But you can install a different security
 * controller once, until you install it again. Thus, the code executed in
 * between is securely controlled by that different security manager. Moreover,
 * only by double registering the manager is removed. So, no malicious code can
 * remove the active security manager.
 *
 * @author foltin
 */
public final class FreeplaneSecurityManager extends SecurityManager {
    private SecurityManager finalSecurityManager = null;

    public FreeplaneSecurityManager() {
    }

    @Override
    public void checkAccept(final String pHost, final int pPort) {
        if (finalSecurityManager != null)
            finalSecurityManager.checkAccept(pHost, pPort);
    }

    @Override
    public void checkAccess(final Thread pT) {
        if (finalSecurityManager != null)
            finalSecurityManager.checkAccess(pT);
    }

    @Override
    public void checkAccess(final ThreadGroup pG) {
        if (finalSecurityManager != null)
            finalSecurityManager.checkAccess(pG);
    }

    @Override
    public void checkAwtEventQueueAccess() {
        if (finalSecurityManager != null)
            finalSecurityManager.checkAwtEventQueueAccess();
    }

    @Override
    public void checkConnect(final String pHost, final int pPort) {
        if (finalSecurityManager != null)
            finalSecurityManager.checkConnect(pHost, pPort);
    }

    @Override
    public void checkConnect(final String pHost, final int pPort, final Object pContext) {
        if (finalSecurityManager != null)
            finalSecurityManager.checkConnect(pHost, pPort, pContext);
    }

    @Override
    public void checkCreateClassLoader() {
        if (finalSecurityManager != null)
            finalSecurityManager.checkCreateClassLoader();
    }

    @Override
    public void checkDelete(final String pFile) {
        if (finalSecurityManager != null)
            finalSecurityManager.checkDelete(pFile);
    }

    @Override
    public void checkExec(final String pCmd) {
        if (finalSecurityManager != null)
            finalSecurityManager.checkExec(pCmd);
    }

    @Override
    public void checkExit(final int pStatus) {
        if (finalSecurityManager != null)
            finalSecurityManager.checkExit(pStatus);
    }

    @Override
    public void checkLink(final String pLib) {
        if (finalSecurityManager != null)
            finalSecurityManager.checkLink(pLib);
    }

    @Override
    public void checkListen(final int pPort) {
        if (finalSecurityManager != null)
            finalSecurityManager.checkListen(pPort);
    }

    @Override
    public void checkMemberAccess(final Class<?> clazz, final int which) {
        if (finalSecurityManager != null)
            finalSecurityManager.checkMemberAccess(clazz, which);
    }

    @Override
    public void checkMulticast(final InetAddress pMaddr) {
        if (finalSecurityManager != null)
            finalSecurityManager.checkMulticast(pMaddr);
    }

    @SuppressWarnings("deprecation")
    // we have to override it in case it's used by anyone
    @Override
    public void checkMulticast(final InetAddress pMaddr, final byte pTtl) {
        if (finalSecurityManager != null)
            finalSecurityManager.checkMulticast(pMaddr, pTtl);
    }

    @Override
    public void checkPackageAccess(final String pPkg) {
        if (finalSecurityManager != null)
            finalSecurityManager.checkPackageAccess(pPkg);
    }

    @Override
    public void checkPackageDefinition(final String pPkg) {
        if (finalSecurityManager != null)
            finalSecurityManager.checkPackageDefinition(pPkg);
    }

    @Override
    public void checkPermission(final Permission pPerm) {
        if (finalSecurityManager != null)
            finalSecurityManager.checkPermission(pPerm);
    }

    @Override
    public void checkPermission(final Permission pPerm, final Object pContext) {
        if (finalSecurityManager != null)
            finalSecurityManager.checkPermission(pPerm, pContext);
    }

    @Override
    public void checkPrintJobAccess() {
        if (finalSecurityManager != null)
            finalSecurityManager.checkPrintJobAccess();
    }

    @Override
    public void checkPropertiesAccess() {
        if (finalSecurityManager != null)
            finalSecurityManager.checkPropertiesAccess();
    }

    @Override
    public void checkPropertyAccess(final String pKey) {
        if (finalSecurityManager != null)
            finalSecurityManager.checkPropertyAccess(pKey);
    }

    @Override
    public void checkRead(final FileDescriptor pFd) {
        if (finalSecurityManager != null)
            finalSecurityManager.checkRead(pFd);
    }

    @Override
    public void checkRead(final String pFile) {
        if (finalSecurityManager != null)
            finalSecurityManager.checkRead(pFile);
    }

    @Override
    public void checkRead(final String pFile, final Object pContext) {
        if (finalSecurityManager != null)
            finalSecurityManager.checkRead(pFile, pContext);
    }

    @Override
    public void checkSecurityAccess(final String pTarget) {
        if (finalSecurityManager != null)
            finalSecurityManager.checkSecurityAccess(pTarget);
    }

    @Override
    public void checkSetFactory() {
        if (finalSecurityManager != null)
            finalSecurityManager.checkSetFactory();
    }

    @Override
    public void checkSystemClipboardAccess() {
        if (finalSecurityManager != null)
            finalSecurityManager.checkSystemClipboardAccess();
    }

    @Override
    public boolean checkTopLevelWindow(final Object pWindow) {
        if (finalSecurityManager == null) {
            return true;
        }
        return finalSecurityManager.checkTopLevelWindow(pWindow);
    }

    @Override
    public void checkWrite(final FileDescriptor pFd) {
        if (finalSecurityManager != null)
            finalSecurityManager.checkWrite(pFd);
    }

    @Override
    public void checkWrite(final String pFile) {
        if (finalSecurityManager != null)
            finalSecurityManager.checkWrite(pFile);
    }

    @Override
    public Object getSecurityContext() {
        if (finalSecurityManager == null) {
            return super.getSecurityContext();
        }
        return finalSecurityManager.getSecurityContext();
    }

    public void setFinalSecurityManager(final SecurityManager finalSecurityManager) {
        if (hasFinalSecurityManager()) {
            throw new SecurityException("There is a SecurityManager installed already.");
        }
        this.finalSecurityManager = finalSecurityManager;
    }

    public boolean hasFinalSecurityManager() {
        return finalSecurityManager != null;
    }

    public void removeFinalSecurityManager(final SecurityManager finalSecurityManager) {
        if (this.finalSecurityManager == finalSecurityManager) {
            this.finalSecurityManager = null;
        }
        else {
            throw new SecurityException("Wrong SecurityManager to remove.");
        }
    }

    /** needed since scripts may be invoked recursively and the security manager may only be set replaced. */
    public boolean needToSetFinalSecurityManager() {
        return !hasFinalSecurityManager() && isEnabled();
    }

    public boolean isEnabled() {
        return !Boolean.valueOf(System.getProperty("org.freeplane.main.application.FreeplaneSecurityManager.disable",
            "false"));
    }
}
