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
	private SecurityManager mFinalSecurityManager = null;

	public FreeplaneSecurityManager() {
	}

	@Override
	public void checkAccept(final String pHost, final int pPort) {
		if (mFinalSecurityManager == null) {
			return;
		}
		mFinalSecurityManager.checkAccept(pHost, pPort);
	}

	@Override
	public void checkAccess(final Thread pT) {
		if (mFinalSecurityManager == null) {
			return;
		}
		mFinalSecurityManager.checkAccess(pT);
	}

	@Override
	public void checkAccess(final ThreadGroup pG) {
		if (mFinalSecurityManager == null) {
			return;
		}
		mFinalSecurityManager.checkAccess(pG);
	}

	@Override
	public void checkAwtEventQueueAccess() {
		if (mFinalSecurityManager == null) {
			return;
		}
		mFinalSecurityManager.checkAwtEventQueueAccess();
	}

	@Override
	public void checkConnect(final String pHost, final int pPort) {
		if (mFinalSecurityManager == null) {
			return;
		}
		mFinalSecurityManager.checkConnect(pHost, pPort);
	}

	@Override
	public void checkConnect(final String pHost, final int pPort, final Object pContext) {
		if (mFinalSecurityManager == null) {
			return;
		}
		mFinalSecurityManager.checkConnect(pHost, pPort, pContext);
	}

	@Override
	public void checkCreateClassLoader() {
		if (mFinalSecurityManager == null) {
			return;
		}
		mFinalSecurityManager.checkCreateClassLoader();
	}

	@Override
	public void checkDelete(final String pFile) {
		if (mFinalSecurityManager == null) {
			return;
		}
		mFinalSecurityManager.checkDelete(pFile);
	}

	@Override
	public void checkExec(final String pCmd) {
		if (mFinalSecurityManager == null) {
			return;
		}
		mFinalSecurityManager.checkExec(pCmd);
	}

	@Override
	public void checkExit(final int pStatus) {
		if (mFinalSecurityManager == null) {
			return;
		}
		mFinalSecurityManager.checkExit(pStatus);
	}

	@Override
	public void checkLink(final String pLib) {
		if (mFinalSecurityManager == null) {
			return;
		}
		mFinalSecurityManager.checkLink(pLib);
	}

	@Override
	public void checkListen(final int pPort) {
		if (mFinalSecurityManager == null) {
			return;
		}
		mFinalSecurityManager.checkListen(pPort);
	}

	@Override
	public void checkMemberAccess(final Class<?> clazz, final int which) {
		if (mFinalSecurityManager == null) {
			return;
		}
		mFinalSecurityManager.checkMemberAccess(clazz, which);
	}

	@Override
	public void checkMulticast(final InetAddress pMaddr) {
		if (mFinalSecurityManager == null) {
			return;
		}
		mFinalSecurityManager.checkMulticast(pMaddr);
	}

	@SuppressWarnings("deprecation")// we have to override it in case it's used by anyone
    @Override
	public void checkMulticast(final InetAddress pMaddr, final byte pTtl) {
		if (mFinalSecurityManager == null) {
			return;
		}
		mFinalSecurityManager.checkMulticast(pMaddr, pTtl);
	}

	@Override
	public void checkPackageAccess(final String pPkg) {
		if (mFinalSecurityManager == null) {
			return;
		}
		mFinalSecurityManager.checkPackageAccess(pPkg);
	}

	@Override
	public void checkPackageDefinition(final String pPkg) {
		if (mFinalSecurityManager == null) {
			return;
		}
		mFinalSecurityManager.checkPackageDefinition(pPkg);
	}

	@Override
	public void checkPermission(final Permission pPerm) {
		if (mFinalSecurityManager == null) {
			return;
		}
		mFinalSecurityManager.checkPermission(pPerm);
	}

	@Override
	public void checkPermission(final Permission pPerm, final Object pContext) {
		if (mFinalSecurityManager == null) {
			return;
		}
		mFinalSecurityManager.checkPermission(pPerm, pContext);
	}

	@Override
	public void checkPrintJobAccess() {
		if (mFinalSecurityManager == null) {
			return;
		}
		mFinalSecurityManager.checkPrintJobAccess();
	}

	@Override
	public void checkPropertiesAccess() {
		if (mFinalSecurityManager == null) {
			return;
		}
		mFinalSecurityManager.checkPropertiesAccess();
	}

	@Override
	public void checkPropertyAccess(final String pKey) {
		if (mFinalSecurityManager == null) {
			return;
		}
		mFinalSecurityManager.checkPropertyAccess(pKey);
	}

	@Override
	public void checkRead(final FileDescriptor pFd) {
		if (mFinalSecurityManager == null) {
			return;
		}
		mFinalSecurityManager.checkRead(pFd);
	}

	@Override
	public void checkRead(final String pFile) {
		if (mFinalSecurityManager == null) {
			return;
		}
		mFinalSecurityManager.checkRead(pFile);
	}

	@Override
	public void checkRead(final String pFile, final Object pContext) {
		if (mFinalSecurityManager == null) {
			return;
		}
		mFinalSecurityManager.checkRead(pFile, pContext);
	}

	@Override
	public void checkSecurityAccess(final String pTarget) {
		if (mFinalSecurityManager == null) {
			return;
		}
		mFinalSecurityManager.checkSecurityAccess(pTarget);
	}

	@Override
	public void checkSetFactory() {
		if (mFinalSecurityManager == null) {
			return;
		}
		mFinalSecurityManager.checkSetFactory();
	}

	@Override
	public void checkSystemClipboardAccess() {
		if (mFinalSecurityManager == null) {
			return;
		}
		mFinalSecurityManager.checkSystemClipboardAccess();
	}

	@Override
	public boolean checkTopLevelWindow(final Object pWindow) {
		if (mFinalSecurityManager == null) {
			return true;
		}
		return mFinalSecurityManager.checkTopLevelWindow(pWindow);
	}

	@Override
	public void checkWrite(final FileDescriptor pFd) {
		if (mFinalSecurityManager == null) {
			return;
		}
		mFinalSecurityManager.checkWrite(pFd);
	}

	@Override
	public void checkWrite(final String pFile) {
		if (mFinalSecurityManager == null) {
			return;
		}
		mFinalSecurityManager.checkWrite(pFile);
	}

	@Override
	public Object getSecurityContext() {
		if (mFinalSecurityManager == null) {
			return super.getSecurityContext();
		}
		return mFinalSecurityManager.getSecurityContext();
	}

	public void setFinalSecurityManager(final SecurityManager finalSecurityManager) {
		if (mFinalSecurityManager != null) {
			throw new SecurityException("There is a SecurityManager installed already.");
		}
		mFinalSecurityManager = finalSecurityManager;
	}
	
	public void removeFinalSecurityManager(final SecurityManager finalSecurityManager) {
		if (finalSecurityManager == mFinalSecurityManager) {
			mFinalSecurityManager = null;
			return;
		}
		else {
			throw new SecurityException("Wrong SecurityManager to remove.");
		}
	}

	/** needed since scripts may be invoked recursively and the security manager may only be set replaced. */
	public boolean needsFinalSecurityManager() {
	    return mFinalSecurityManager == null 
	    && ! Boolean.valueOf(System.getProperty("org.freeplane.main.application.FreeplaneSecurityManager.disable", "false"));
    }
}
