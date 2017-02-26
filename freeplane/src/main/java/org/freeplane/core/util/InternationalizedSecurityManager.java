/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2016 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
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
package org.freeplane.core.util;

import java.io.FileDescriptor;
import java.lang.reflect.ReflectPermission;
import java.net.InetAddress;
import java.security.AccessControlException;
import java.security.Permission;

import org.freeplane.core.util.TextUtils;

public class InternationalizedSecurityManager extends SecurityManager {
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

	public InternationalizedSecurityManager() {
	}

	@Override
	public void checkAccept(final String pHost, final int pPort) {
		try{
			super.checkAccept(pHost, pPort);
		}
		catch(AccessControlException e){
			throw getException(e, InternationalizedSecurityManager.PERM_GROUP_NETWORK, InternationalizedSecurityManager.PERM_Accept);
		}
	}

	@Override
	public void checkConnect(final String pHost, final int pPort) {
		try{
			super.checkConnect(pHost, pPort);
		}
		catch(AccessControlException e){
			throw getException(e, InternationalizedSecurityManager.PERM_GROUP_NETWORK, InternationalizedSecurityManager.PERM_Connect);
		}
	}

	@Override
	public void checkConnect(final String pHost, final int pPort, final Object pContext) {
		try{
			checkConnectHandleNullContext(pHost, pPort, pContext);
		}
		catch(AccessControlException e){
			throw getException(e, InternationalizedSecurityManager.PERM_GROUP_NETWORK, InternationalizedSecurityManager.PERM_Connect);
		}
	}

	private void checkConnectHandleNullContext(final String pHost, final int pPort, final Object pContext) {
		if(pContext != null)
			super.checkConnect(pHost, pPort, pContext);
		else
			super.checkConnect(pHost, pPort);
	}

	@Override
	public void checkDelete(final String pFile) {
		try{
			super.checkDelete(pFile);
		}
		catch(AccessControlException e){
			throw getException(e, InternationalizedSecurityManager.PERM_GROUP_FILE, InternationalizedSecurityManager.PERM_Delete);
		}
	}

	@Override
	public void checkExec(final String pCmd) {
		try{
			super.checkExec(pCmd);
		}
		catch(AccessControlException e){
			throw getException(e, InternationalizedSecurityManager.PERM_GROUP_EXEC, InternationalizedSecurityManager.PERM_Exec);
		}
	}


	@Override
	public void checkLink(final String pLib) {
		try{
			super.checkLink(pLib);
		}
		catch(AccessControlException e){
			throw getException(e, InternationalizedSecurityManager.PERM_GROUP_EXEC, InternationalizedSecurityManager.PERM_Link);
		}
	}

	@Override
	public void checkListen(final int pPort) {
		try{
			super.checkListen(pPort);
		}
		catch(AccessControlException e){
			throw getException(e, InternationalizedSecurityManager.PERM_GROUP_NETWORK, InternationalizedSecurityManager.PERM_Listen);
		}
	}

	@Override
	public void checkMulticast(final InetAddress pMaddr) {
		try{
			super.checkMulticast(pMaddr);
		}
		catch(AccessControlException e){
			throw getException(e, InternationalizedSecurityManager.PERM_GROUP_NETWORK, InternationalizedSecurityManager.PERM_Multicast);
		}
	}

	@Override
	public void checkMulticast(final InetAddress pMaddr, final byte pTtl) {
		try{
			super.checkMulticast(pMaddr, pTtl);
		}
		catch(AccessControlException e){
			throw getException(e, InternationalizedSecurityManager.PERM_GROUP_NETWORK, InternationalizedSecurityManager.PERM_Multicast);
		}
	}

	@Override
	public void checkRead(final FileDescriptor pFd) {
		try{
			super.checkRead(pFd);
		}
		catch(AccessControlException e){
			throw getException(e, InternationalizedSecurityManager.PERM_GROUP_FILE, InternationalizedSecurityManager.PERM_Read);
		}
	}

	@Override
	public void checkRead(final String pFile) {
		try{
			super.checkRead(pFile);
		}
		catch(AccessControlException e){
			throw getException(e, InternationalizedSecurityManager.PERM_GROUP_FILE, InternationalizedSecurityManager.PERM_Read, pFile);
		}
	}

	@Override
	public void checkRead(final String pFile, final Object pContext) {
		try{
			super.checkRead(pFile, pContext);
		}
		catch(AccessControlException e){
			throw getException(e, InternationalizedSecurityManager.PERM_GROUP_FILE, InternationalizedSecurityManager.PERM_Read, pFile);
		}
	}

	@Override
	public void checkSetFactory() {
		try{
			super.checkSetFactory();
		}
		catch(AccessControlException e){
			throw getException(e, InternationalizedSecurityManager.PERM_GROUP_NETWORK, InternationalizedSecurityManager.PERM_SetFactory);
		}
	}

	@Override
	public void checkWrite(final FileDescriptor pFd) {
		try{
			super.checkWrite(pFd);
		}
		catch(AccessControlException e){
			throw getException(e, InternationalizedSecurityManager.PERM_GROUP_FILE, InternationalizedSecurityManager.PERM_Write);
		}
	}

	@Override
	public void checkWrite(final String pFile) {
		try{
			super.checkWrite(pFile);
		}
		catch(AccessControlException e){
			throw getException(e, InternationalizedSecurityManager.PERM_GROUP_FILE, InternationalizedSecurityManager.PERM_Write);
		}
	}

	private SecurityException getException(final AccessControlException e, final int pPermissionGroup, final int pPermission, final String pFile) {
		final String message = TextUtils.format("plugins/ScriptEditor.FORBIDDEN_ACTION", new Integer(
			    pPermissionGroup), new Integer(pPermission), pFile);
		return new SecurityException(message, e);
    }

	private SecurityException getException(AccessControlException e, final int pPermissionGroup, final int pPermission) {
		return getException(e, pPermissionGroup, pPermission, "");
	}

	@Override
	public void checkPermission(Permission perm) {
		disallowSupressingAccessChecks(perm);
		super.checkPermission(perm);
	}

	private void disallowSupressingAccessChecks(Permission perm) {
	}

	@Override
	public void checkPermission(Permission perm, Object context) {
		disallowSupressingAccessChecks(perm);
		super.checkPermission(perm, context);	
	}
	
	
}
