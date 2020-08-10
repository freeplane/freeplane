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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.Signature;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.components.EnterPasswordDialog;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.encrypt.DesEncrypter;


class SignedScriptHandler {
	static class ScriptContents {
		final private static Pattern SIGNATURE_WITH_KEY = Pattern.compile(SignedScriptHandler.SIGN_PREFIX_REGEXP);
		String mKeyName;
		String mScript;
		String mSignature;

		public ScriptContents(final String pScript) {
			final int indexOfSignaturePrefix = pScript.lastIndexOf(SignedScriptHandler.SIGN_PREFIX);
			final int indexOfSignature = indexOfSignaturePrefix + SignedScriptHandler.SIGN_PREFIX.length();
			if (indexOfSignaturePrefix > 0 && pScript.length() > indexOfSignature) {
				mSignature = pScript.substring(indexOfSignature);
				mScript = pScript.substring(0, indexOfSignaturePrefix);
				mKeyName = null;
			}
			else {
				final Matcher matcher = ScriptContents.SIGNATURE_WITH_KEY.matcher(pScript);
				if (matcher.find()) {
					mScript = pScript.substring(0, matcher.start());
					mKeyName = matcher.group(1);
					mSignature = matcher.group(2);
				}
				else {
					mSignature = null;
					mScript = pScript;
					mKeyName = null;
				}
			}
		}

		@Override
		public String toString() {
			String prefix;
			if (mKeyName != null) {
				prefix = "//SIGN(" + mKeyName + "):";
			}
			else {
				prefix = SignedScriptHandler.SIGN_PREFIX;
			}
			return mScript + prefix + mSignature + "\n";
		}
	}

	public static final String FREEPLANE_SCRIPT_KEY_NAME = "FreeplaneScriptKey";
	private static KeyStore mKeyStore = null;
	private static final String SIGN_PREFIX = "//SIGN:";
	/** This is for / /SIGN(keyname):signature */
	private static final String SIGN_PREFIX_REGEXP = "//SIGN\\((.+?)\\):(.*)";

	public SignedScriptHandler() {
	}

	private void initializeKeystore(final char[] pPassword) {
		if (SignedScriptHandler.mKeyStore != null) {
			return;
		}
		try {
			SignedScriptHandler.mKeyStore = KeyStore.getInstance(KeyStore.getDefaultType());
			try (FileInputStream fis = new FileInputStream(System.getProperty("user.home") + File.separator + ".keystore")) {
			    SignedScriptHandler.mKeyStore.load(fis, pPassword);
			}
		}
		catch (final FileNotFoundException e) {
			LogUtils.warn(e);
		}
		catch (final Exception e) {
			LogUtils.severe(e);
		}
	}

	public boolean isScriptSigned(final String pScript, final OutputStream pOutStream) {
		final ScriptContents content = new ScriptContents(pScript);
		if (content.mSignature != null) {
			try {
				final Signature instanceVerify = Signature.getInstance("SHA1withDSA");
				if (content.mKeyName == null) {
					return false;
				}
				else {
					initializeKeystore(null);
					instanceVerify.initVerify(SignedScriptHandler.mKeyStore.getCertificate(content.mKeyName));
					instanceVerify.update(content.mScript.getBytes());
					final boolean verify = instanceVerify.verify(DesEncrypter.fromBase64(content.mSignature));
					return verify;
				}
			}
			catch (final Exception e) {
				LogUtils.severe(e);
				try {
					pOutStream.write(e.toString().getBytes());
					pOutStream.write("\n".getBytes());
				}
				catch (final Exception e1) {
					LogUtils.severe(e1);
				}
			}
		}
		return false;
	}

	public String signScript(final String pScript) {
		final ScriptContents content = new ScriptContents(pScript);
		final EnterPasswordDialog pwdDialog = new EnterPasswordDialog(UITools.getCurrentFrame(), false);
		pwdDialog.setModal(true);
		pwdDialog.setVisible(true);
		if (pwdDialog.getResult() == EnterPasswordDialog.CANCEL) {
			return content.mScript;
		}
		final char[] password = pwdDialog.getPassword().toString().toCharArray();
		initializeKeystore(password);
		try {
			final Signature instance = Signature.getInstance("SHA1withDSA");
			String keyName = FREEPLANE_SCRIPT_KEY_NAME;
			final ResourceController resourceController = ResourceController.getResourceController();
			String propertyKeyName = resourceController.getProperty(
			    ScriptingPermissions.RESOURCES_SCRIPT_USER_KEY_NAME_FOR_SIGNING);
			if (propertyKeyName == null || propertyKeyName.trim().length() == 0){
				resourceController.setProperty(ScriptingPermissions.RESOURCES_SCRIPT_USER_KEY_NAME_FOR_SIGNING, 
						FREEPLANE_SCRIPT_KEY_NAME);
				propertyKeyName = keyName;
			}
			if (content.mKeyName != null) {
				keyName = content.mKeyName;
			}
			else {
				content.mKeyName = propertyKeyName;
				keyName = content.mKeyName;
			}
			instance.initSign((PrivateKey) SignedScriptHandler.mKeyStore.getKey(keyName, password));
			instance.update(content.mScript.getBytes());
			final byte[] signature = instance.sign();
			content.mSignature = DesEncrypter.toBase64(signature);
			return content.toString();
		}
		catch (final Exception e) {
			if(! (e instanceof KeyStoreException))
				LogUtils.severe(e);
			UITools.errorMessage(e.getLocalizedMessage());
		}
		return content.mScript;
	}
}
