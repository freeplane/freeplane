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

import java.io.FileDescriptor;
import java.net.InetAddress;
import java.security.Permission;
import java.util.HashSet;

import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.filter.condition.ICondition;
import org.freeplane.plugin.script.proxy.Proxy;

/**
 * @author foltin
 */
class ScriptingSecurityManager extends SecurityManager {
	private static final String INTERNAL_API_PACKAGE_BASE = "org.freeplane";
	private static final HashSet<String> whiteList = createWhiteList();
	private static final int PERM_Accept = 0;
	private static final int PERM_Connect = 1;
	private static final int PERM_Delete = 7;
	private static final int PERM_Exec = 5;
	private static final int PERM_GROUP_EXEC = 2;
	private static final int PERM_GROUP_FILE = 0;
	private static final int PERM_GROUP_NETWORK = 1;
	private static final int PERM_Link = 6;
	private static final int PERM_Listen = 2;
	private static final int PERM_Multicast = 3;
	private static final int PERM_Read = 8;
	private static final int PERM_SetFactory = 4;
	private static final int PERM_Write = 9;
	final private boolean mWithoutReadRestriction;
	final private boolean mWithoutWriteRestriction;
	final private boolean mWithoutExecRestriction;
	final private boolean mWithoutNetworkRestriction;

	public ScriptingSecurityManager(final boolean pWithoutFileRestriction, boolean pWithoutWriteRestriction,
	                                final boolean pWithoutNetworkRestriction, final boolean pWithoutExecRestriction) {
		mWithoutReadRestriction = pWithoutFileRestriction;
		mWithoutWriteRestriction = pWithoutWriteRestriction;
		mWithoutNetworkRestriction = pWithoutNetworkRestriction;
		mWithoutExecRestriction = pWithoutExecRestriction;
	}

	private static HashSet<String> createWhiteList() {
	    final HashSet<String> result = new HashSet<String>();
	    result.add(Proxy.class.getPackage().getName());
	    result.add(TextUtils.class.getPackage().getName());
	    // this one is under debate since UITools should be moved to the utils package
	    result.add(UITools.class.getPackage().getName());
	    // this one is necessary due to deprecated API methods: find(ICondition)
	    result.add(ICondition.class.getPackage().getName());
	    // the following are considered wrong
//	    result.add(NodeModel.class.getPackage().getName());
//	    result.add(NoteModel.class.getPackage().getName());
//	    result.add(LinkController.class.getPackage().getName());
//	    result.add(MLinkController.class.getPackage().getName());
//	    result.add(MindIcon.class.getPackage().getName());
//	    result.add(MindIconFactory.class.getPackage().getName());
//	    result.add(MNoteController.class.getPackage().getName());
		return result;
    }

	@Override
	public void checkAccept(final String pHost, final int pPort) {
		if (mWithoutNetworkRestriction) {
			return;
		}
		throw getException(ScriptingSecurityManager.PERM_GROUP_NETWORK, ScriptingSecurityManager.PERM_Accept);
	}

	@Override
	public void checkAccess(final Thread pT) {
	}

	@Override
	public void checkAccess(final ThreadGroup pG) {
	}

	@Override
	public void checkAwtEventQueueAccess() {
	}

	@Override
	public void checkConnect(final String pHost, final int pPort) {
		if (mWithoutNetworkRestriction) {
			return;
		}
		throw getException(ScriptingSecurityManager.PERM_GROUP_NETWORK, ScriptingSecurityManager.PERM_Connect);
	}

	@Override
	public void checkConnect(final String pHost, final int pPort, final Object pContext) {
		if (mWithoutNetworkRestriction) {
			return;
		}
		throw getException(ScriptingSecurityManager.PERM_GROUP_NETWORK, ScriptingSecurityManager.PERM_Connect);
	}

	@Override
	public void checkCreateClassLoader() {
	}

	@Override
	public void checkDelete(final String pFile) {
		if (mWithoutReadRestriction) {
			return;
		}
		throw getException(ScriptingSecurityManager.PERM_GROUP_FILE, ScriptingSecurityManager.PERM_Delete);
	}

	@Override
	public void checkExec(final String pCmd) {
		if (mWithoutExecRestriction) {
			return;
		}
		throw getException(ScriptingSecurityManager.PERM_GROUP_EXEC, ScriptingSecurityManager.PERM_Exec);
	}

	@Override
	public void checkExit(final int pStatus) {
	}

	@Override
	public void checkLink(final String pLib) {
		/*
		 * This should permit system libraries to be loaded.
		 */
		final HashSet<String> set = new HashSet<String>();
		set.add("awt");
		set.add("net");
		set.add("jpeg");
		set.add("fontmanager");
		if (mWithoutExecRestriction || set.contains(pLib)) {
			return;
		}
		throw getException(ScriptingSecurityManager.PERM_GROUP_EXEC, ScriptingSecurityManager.PERM_Link);
	}

	@Override
	public void checkListen(final int pPort) {
		if (mWithoutNetworkRestriction) {
			return;
		}
		throw getException(ScriptingSecurityManager.PERM_GROUP_NETWORK, ScriptingSecurityManager.PERM_Listen);
	}

	@Override
	public void checkMemberAccess(final Class<?> arg0, final int arg1) {
	}

	@Override
	public void checkMulticast(final InetAddress pMaddr) {
		if (mWithoutNetworkRestriction) {
			return;
		}
		throw getException(ScriptingSecurityManager.PERM_GROUP_NETWORK, ScriptingSecurityManager.PERM_Multicast);
	}

	@Override
	public void checkMulticast(final InetAddress pMaddr, final byte pTtl) {
		if (mWithoutNetworkRestriction) {
			return;
		}
		throw getException(ScriptingSecurityManager.PERM_GROUP_NETWORK, ScriptingSecurityManager.PERM_Multicast);
	}

	@Override
	public void checkPackageAccess(final String pkg) {
		if (pkg.startsWith(INTERNAL_API_PACKAGE_BASE) && !whiteList.contains(pkg)) {
			// temporaribly disabled:
			// throw new SecurityException(TextUtils.format("plugins/ScriptingEngine.illegalAccessToInternalAPI", pkg));
//			LogUtils.warn("access to internal package " + pkg);
		}
	}

	@Override
	public void checkPackageDefinition(final String pPkg) {
	}

	@Override
	public void checkPermission(final Permission pPerm) {
	}

	@Override
	public void checkPermission(final Permission pPerm, final Object pContext) {
	}

	@Override
	public void checkPrintJobAccess() {
	}

	@Override
	public void checkPropertiesAccess() {
	}

	@Override
	public void checkPropertyAccess(final String pKey) {
	}

	@Override
	public void checkRead(final FileDescriptor pFd) {
		if (mWithoutReadRestriction) {
			return;
		}
		throw getException(ScriptingSecurityManager.PERM_GROUP_FILE, ScriptingSecurityManager.PERM_Read);
	}

	@Override
	public void checkRead(final String pFile) {
		if (mWithoutReadRestriction) {
			return;
		}
		throw getException(ScriptingSecurityManager.PERM_GROUP_FILE, ScriptingSecurityManager.PERM_Read, pFile);
	}

	@Override
	public void checkRead(final String pFile, final Object pContext) {
		if (mWithoutReadRestriction) {
			return;
		}
		throw getException(ScriptingSecurityManager.PERM_GROUP_FILE, ScriptingSecurityManager.PERM_Read);
	}

	@Override
	public void checkSecurityAccess(final String pTarget) {
	}

	@Override
	public void checkSetFactory() {
		if (mWithoutNetworkRestriction) {
			return;
		}
		throw getException(ScriptingSecurityManager.PERM_GROUP_NETWORK, ScriptingSecurityManager.PERM_SetFactory);
	}

	@Override
	public void checkSystemClipboardAccess() {
	}

	@Override
	public boolean checkTopLevelWindow(final Object pWindow) {
		return true;
	}

	@Override
	public void checkWrite(final FileDescriptor pFd) {
		if (mWithoutWriteRestriction) {
			return;
		}
		throw getException(ScriptingSecurityManager.PERM_GROUP_FILE, ScriptingSecurityManager.PERM_Write);
	}

	@Override
	public void checkWrite(final String pFile) {
		if (mWithoutWriteRestriction) {
			return;
		}
		throw getException(ScriptingSecurityManager.PERM_GROUP_FILE, ScriptingSecurityManager.PERM_Write);
	}

	private SecurityException getException(final int pPermissionGroup, final int pPermission, String pFile) {
		return new SecurityException(TextUtils.format("plugins/ScriptEditor.FORBIDDEN_ACTION", new Integer(
		    pPermissionGroup), new Integer(pPermission), pFile));
    }

	private SecurityException getException(final int pPermissionGroup, final int pPermission) {
		return getException(pPermissionGroup, pPermission, "");
	}
}
