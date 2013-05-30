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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.components.EnterPasswordDialog;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.encrypt.DesEncrypter;
import org.freeplane.features.mode.Controller;

/**
 * @author foltin
 */
class SignedScriptHandler {
	public static class ScriptContents {
		private static Pattern sSignWithKeyPattern = null;
		public String mKeyName;
		public String mScript;
		public String mSignature;

		public ScriptContents() {
			if (ScriptContents.sSignWithKeyPattern == null) {
				ScriptContents.sSignWithKeyPattern = Pattern.compile(SignedScriptHandler.SIGN_PREFIX_REGEXP);
			}
		}

		public ScriptContents(final String pScript) {
			this();
			final int indexOfSignaturePrefix = pScript.lastIndexOf(SignedScriptHandler.SIGN_PREFIX);
			final int indexOfSignature = indexOfSignaturePrefix + SignedScriptHandler.SIGN_PREFIX.length();
			if (indexOfSignaturePrefix > 0 && pScript.length() > indexOfSignature) {
				mSignature = pScript.substring(indexOfSignature);
				mScript = pScript.substring(0, indexOfSignaturePrefix);
				mKeyName = null;
			}
			else {
				final Matcher matcher = ScriptContents.sSignWithKeyPattern.matcher(pScript);
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
	private static final String SIGN_PREFIX_REGEXP = "//SIGN\\((.*?)\\):(.*)";

	public SignedScriptHandler() {
	}

	private void initializeKeystore(final char[] pPassword) {
		if (SignedScriptHandler.mKeyStore != null) {
			return;
		}
		java.io.FileInputStream fis = null;
		try {
			SignedScriptHandler.mKeyStore = KeyStore.getInstance(KeyStore.getDefaultType());
			fis = new java.io.FileInputStream(System.getProperty("user.home") + File.separator + ".keystore");
			SignedScriptHandler.mKeyStore.load(fis, pPassword);
		}
		catch (final FileNotFoundException e) {
			LogUtils.warn(e);
		}
		catch (final Exception e) {
			LogUtils.severe(e);
		}
		finally {
			if (fis != null) {
				try {
					fis.close();
				}
				catch (final IOException e) {
					LogUtils.severe(e);
				}
			}
		}
	}

	public boolean isScriptSigned(final String pScript, final OutputStream pOutStream) {
		final ScriptContents content = new ScriptContents(pScript);
		if (content.mSignature != null) {
			try {
				final Signature instanceVerify = Signature.getInstance("SHA1withDSA");
				if (content.mKeyName == null) {
					/**
					 * This is the Freeplane public key. keytool -v -rfc
					 * -exportcert -alias freeplanescriptkey
					 */
					final String cer = "-----BEGIN CERTIFICATE-----\n"
					        + "MIIDKDCCAuWgAwIBAgIESAY2ADALBgcqhkjOOAQDBQAwdzELMAkGA1UEBhMCREUxCzAJBgNVBAgT"
					        + "AkRFMRMwEQYDVQQHEwpPcGVuU291cmNlMRgwFgYDVQQKEw9zb3VyY2Vmb3JnZS5uZXQxETAPBgNV"
					        + "BAsTCEZyZWVNaW5kMRkwFwYDVQQDExBDaHJpc3RpYW4gRm9sdGluMB4XDTA4MDQxNjE3MjMxMloX"
					        + "DTA4MDcxNTE3MjMxMlowdzELMAkGA1UEBhMCREUxCzAJBgNVBAgTAkRFMRMwEQYDVQQHEwpPcGVu"
					        + "U291cmNlMRgwFgYDVQQKEw9zb3VyY2Vmb3JnZS5uZXQxETAPBgNVBAsTCEZyZWVNaW5kMRkwFwYD"
					        + "VQQDExBDaHJpc3RpYW4gRm9sdGluMIIBtzCCASwGByqGSM44BAEwggEfAoGBAP1/U4EddRIpUt9K"
					        + "nC7s5Of2EbdSPO9EAMMeP4C2USZpRV1AIlH7WT2NWPq/xfW6MPbLm1Vs14E7gB00b/JmYLdrmVCl"
					        + "pJ+f6AR7ECLCT7up1/63xhv4O1fnxqimFQ8E+4P208UewwI1VBNaFpEy9nXzrith1yrv8iIDGZ3R"
					        + "SAHHAhUAl2BQjxUjC8yykrmCouuEC/BYHPUCgYEA9+GghdabPd7LvKtcNrhXuXmUr7v6OuqC+VdM"
					        + "Cz0HgmdRWVeOutRZT+ZxBxCBgLRJFnEj6EwoFhO3zwkyjMim4TwWeotUfI0o4KOuHiuzpnWRbqN/"
					        + "C/ohNWLx+2J6ASQ7zKTxvqhRkImog9/hWuWfBpKLZl6Ae1UlZAFMO/7PSSoDgYQAAoGAZm5z5EZX"
					        + "Vhtye5jY3X9w24DJ3yNJbNl2tfkOBIc0KfgyxONTSJKtUpmLI3btUxy3pQf/T8BShlY3PAC0fp3M"
					        + "eDG8WRq1wM3luLd1V9SS8EG6tPJBZ3mciCUymTT7n9CZNzATIpqNIXHSD/wljRABedUi8PMg4KbV"
					        + "Pnhu6Y6b1uAwCwYHKoZIzjgEAwUAAzAAMC0CFQCFHGwe+HHOvY0MmKYHbiq7fRxMGwIUC0voAGYU"
					        + "u6vgVFqdLI5F96JLTqk=" + "\n-----END CERTIFICATE-----\n";
					final CertificateFactory cf = CertificateFactory.getInstance("X.509");
					final Collection<? extends Certificate> c = cf.generateCertificates(new ByteArrayInputStream(cer
					    .getBytes()));
					if (c.isEmpty())
						throw new IllegalArgumentException("Internal certificate wrong.");
					for (Certificate cert : c) {
						instanceVerify.initVerify(cert);
					}
				}
				else {
					initializeKeystore(null);
					instanceVerify.initVerify(SignedScriptHandler.mKeyStore.getCertificate(content.mKeyName));
				}
				instanceVerify.update(content.mScript.getBytes());
				final boolean verify = instanceVerify.verify(DesEncrypter.fromBase64(content.mSignature));
				return verify;
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
		final EnterPasswordDialog pwdDialog = new EnterPasswordDialog(Controller.getCurrentController().getViewController().getJFrame(), false);
		pwdDialog.setModal(true);
		pwdDialog.setVisible(true);
		if (pwdDialog.getResult() == EnterPasswordDialog.CANCEL) {
			return content.mScript;
		}
		final char[] password = pwdDialog.getPassword().toString().toCharArray();
		initializeKeystore(password);
		try {
			final Signature instance = Signature.getInstance("SHA1withDSA");
			String keyName = SignedScriptHandler.FREEPLANE_SCRIPT_KEY_NAME;
			final String propertyKeyName = ResourceController.getResourceController().getProperty(
			    ScriptingPermissions.RESOURCES_SCRIPT_USER_KEY_NAME_FOR_SIGNING);
			if (content.mKeyName != null) {
				keyName = content.mKeyName;
			}
			else if (propertyKeyName != null && propertyKeyName.length() > 0) {
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
