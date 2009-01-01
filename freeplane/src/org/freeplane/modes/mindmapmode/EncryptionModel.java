/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is modified by Dimitry Polivaev in 2008.
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
package org.freeplane.modes.mindmapmode;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.logging.Logger;

import javax.swing.ImageIcon;

import org.freeplane.addins.encrypt.SingleDesEncrypter;
import org.freeplane.core.extension.IExtension;
import org.freeplane.core.map.MapController;
import org.freeplane.core.map.ModeController;
import org.freeplane.core.map.NodeModel;
import org.freeplane.map.icon.MindIcon;

public class EncryptionModel implements IExtension {
	private static ImageIcon decryptedIcon;
	private static ImageIcon encryptedIcon;
	private static Logger logger;
	private String encryptedContent;
	private boolean isAccessible = true;
	/**
	 * is only set to false by the load mechanism. If the node is generated or
	 * it is decrypted once, this is always true.
	 */
	private boolean isDecrypted = true;
	/**
	 * password have to be stored in a StringBuffer as Strings cannot be deleted
	 * or overwritten.
	 */
	final private NodeModel node;
	private StringBuffer password = null;

	public EncryptionModel(final NodeModel node) {
		this.node = node;
		encryptedContent = null;
		setAccessible(true);
		isDecrypted = true;
		init(node);
	}

	/**
	 * @param encryptedContent
	 */
	public EncryptionModel(final NodeModel node, final String encryptedContent) {
		this.node = node;
		this.encryptedContent = encryptedContent;
		setAccessible(false);
		isDecrypted = false;
		init(node);
	}

	/**
	 */
	public boolean checkPassword(final StringBuffer givenPassword) {
		if (password != null) {
			if (!equals(givenPassword, password)) {
				EncryptionModel.logger.warning("Wrong password supplied (cached!=given).");
				return false;
			}
			return true;
		}
		final String decryptedNode = decryptXml(encryptedContent, givenPassword);
		if (decryptedNode == null || decryptedNode.equals("")
		        || !decryptedNode.startsWith("<node ")) {
			EncryptionModel.logger.warning("Wrong password supplied (stored!=given).");
			return false;
		}
		password = givenPassword;
		return true;
	}

	/**
	 * @return true, if the password was correct.
	 */
	public boolean decrypt(final StringBuffer givenPassword) {
		if (!checkPassword(givenPassword)) {
			return false;
		}
		setAccessible(true);
		if (!isDecrypted) {
			try {
				final String childXml = decryptXml(encryptedContent, password);
				final String[] childs = childXml.split(ModeController.NODESEPARATOR);
				for (int i = childs.length - 1; i >= 0; i--) {
					final String string = childs[i];
					if (string.length() == 0) {
						continue;
					}
					pasteXML(string, node);
				}
				isDecrypted = true;
			}
			catch (final Exception e) {
				org.freeplane.core.util.Tools.logException(e);
				setAccessible(false);
				return true;
			}
		}
		return true;
	}

	/**
	 * @return null if the password is wrong.
	 */
	private String decryptXml(final String encryptedString, final StringBuffer pwd) {
		final SingleDesEncrypter encrypter = new SingleDesEncrypter(pwd);
		final String decrypted = encrypter.decrypt(encryptedString);
		return decrypted;
	}

	/**
	 */
	private String encryptXml(final StringBuffer childXml) {
		try {
			final SingleDesEncrypter encrypter = new SingleDesEncrypter(password);
			final String encrypted = encrypter.encrypt(childXml.toString());
			return encrypted;
		}
		catch (final Exception e) {
			org.freeplane.core.util.Tools.logException(e);
		}
		throw new IllegalArgumentException("Can't encrypt the node.");
	}

	/**
	 */
	private boolean equals(final StringBuffer givenPassword, final StringBuffer password2) {
		if (givenPassword.length() != password.length()) {
			return false;
		}
		for (int i = 0; i < password2.length(); i++) {
			final char c1 = password2.charAt(i);
			final char c2 = givenPassword.charAt(i);
			if (c1 != c2) {
				return false;
			}
		}
		return true;
	}

	/**
	 * @throws IOException
	 */
	private void generateEncryptedContent() throws IOException {
		final StringWriter sWriter = new StringWriter();
		for (final Iterator i = node.getModeController().getMapController().childrenUnfolded(node); i
		    .hasNext();) {
			final NodeModel child = (NodeModel) i.next();
			child.getModeController().getMapController().writeNodeAsXml(sWriter, child, true, true);
			if (i.hasNext()) {
				sWriter.write(ModeController.NODESEPARATOR);
			}
		}
		final StringBuffer childXml = sWriter.getBuffer();
		encryptedContent = encryptXml(childXml);
	}

	public String getEncryptedContent() {
		if (isDecrypted) {
			try {
				generateEncryptedContent();
			}
			catch (final Exception e) {
				org.freeplane.core.util.Tools.logException(e);
			}
		}
		return encryptedContent;
	}

	private void init(final NodeModel node) {
		if (EncryptionModel.logger == null) {
			EncryptionModel.logger = Logger.global;
		}
		if (EncryptionModel.encryptedIcon == null) {
			EncryptionModel.encryptedIcon = MindIcon.factory("encrypted").getIcon();
		}
		if (EncryptionModel.decryptedIcon == null) {
			EncryptionModel.decryptedIcon = MindIcon.factory("decrypted").getIcon();
		}
		updateIcon();
	}

	/**
	 * @return Returns the isAccessible (ie. if the node is decrypted
	 *         (isAccessible==true) or not).
	 */
	public boolean isAccessible() {
		return isAccessible;
	}

	/**
	 *
	 */
	public boolean isFolded() {
		if (isAccessible()) {
			return node.getModeController().getMapController().isFolded(node);
		}
		return true;
	}

	private void pasteXML(final String pasted, final NodeModel target) {
		try {
			final MapController mapController = target.getModeController().getMapController();
			final NodeModel node = mapController.createNodeTreeFromXml(target.getMap(),
			    new StringReader(pasted));
			mapController.insertNodeIntoWithoutUndo(node, target, target.getChildCount());
		}
		catch (final Exception ee) {
			org.freeplane.core.util.Tools.logException(ee);
		}
	}

	/**
	 * @param isAccessible
	 *            The isAccessible to set.
	 */
	public void setAccessible(final boolean isAccessible) {
		this.isAccessible = isAccessible;
		updateIcon();
	}

	public void setPassword(final StringBuffer password) {
		this.password = password;
	}

	/*
	 * (non-Javadoc)
	 * @see freemind.modes.MindMapNode#getIcons()
	 */
	public void updateIcon() {
		if (isAccessible()) {
			node.setStateIcon("encrypted", null);
			node.setStateIcon("decrypted", EncryptionModel.decryptedIcon);
		}
		else {
			node.setStateIcon("decrypted", null);
			node.setStateIcon("encrypted", EncryptionModel.encryptedIcon);
		}
	}
}
