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
package org.freeplane.core.model;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.logging.Logger;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.icon.IconStore;
import org.freeplane.core.icon.UIIcon;
import org.freeplane.core.icon.factory.IconStoreFactory;
import org.freeplane.core.io.MapWriter;
import org.freeplane.core.io.MapWriter.Mode;
import org.freeplane.core.modecontroller.IEncrypter;
import org.freeplane.core.modecontroller.MapController;
import org.freeplane.core.util.LogTool;
import org.freeplane.features.common.clipboard.ClipboardController;

public class EncryptionModel implements IExtension {
	private static UIIcon decryptedIcon;
	private static UIIcon encryptedIcon;
	private static Logger logger;

	private static final IconStore STORE = IconStoreFactory.create();
	
	public static EncryptionModel getModel(final NodeModel node) {
		return (EncryptionModel) node.getExtension(EncryptionModel.class);
	}

	private String encryptedContent;
	private boolean isAccessible = true;
	/**
	 * is only set to false by the load mechanism. If the node is generated or
	 * it is decrypted once, this is always true.
	 */
	private boolean isDecrypted = true;
	private IEncrypter mEncrypter;
	/**
	 * password have to be stored in a StringBuilder as Strings cannot be deleted
	 * or overwritten.
	 */
	final private NodeModel node;

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
	public boolean checkPassword(final IEncrypter encrypter) {
		final String decryptedNode = decryptXml(encryptedContent, encrypter);
		if (decryptedNode == null || !decryptedNode.equals("") && !decryptedNode.startsWith("<node ")) {
			EncryptionModel.logger.warning("Wrong password supplied (stored!=given).");
			return false;
		}
		setEncrypter(encrypter);
		return true;
	}

	/**
	 * @param mapController 
	 * @return true, if the password was correct.
	 */
	public boolean decrypt(final MapController mapController, final IEncrypter encrypter) {
		if (!checkPassword(encrypter)) {
			return false;
		}
		setAccessible(true);
		if (!isDecrypted) {
			try {
				final String childXml = decryptXml(encryptedContent, encrypter);
				final String[] childs = childXml.split(ClipboardController.NODESEPARATOR);
				for (int i = 0; i < childs.length ; i++) {
					final String string = childs[i];
					if (string.length() == 0) {
						continue;
					}
					pasteXML(string, node, mapController);
				}
				isDecrypted = true;
			}
			catch (final Exception e) {
				LogTool.severe(e);
				setAccessible(false);
				return true;
			}
		}
		return true;
	}

	/**
	 * @return null if the password is wrong.
	 */
	private String decryptXml(final String encryptedString, final IEncrypter encrypter) {
		final String decrypted = encrypter.decrypt(encryptedString);
		return decrypted;
	}

	/**
	 */
	private String encryptXml(final StringBuffer childXml) {
		try {
			final String encrypted = mEncrypter.encrypt(childXml.toString());
			return encrypted;
		}
		catch (final Exception e) {
			throw new IllegalArgumentException("Can't encrypt the node.", e);
		}
	}

	/**
	 * @param mapController 
	 * @param mode 
	 * @throws IOException
	 */
	private void generateEncryptedContent(final MapController mapController) throws IOException {
		final StringWriter sWriter = new StringWriter();
		for (final Iterator<NodeModel> i = node.getChildren().listIterator(); i.hasNext();) {
			final NodeModel child = i.next();
			mapController.getMapWriter().writeNodeAsXml(sWriter, child, MapWriter.Mode.FILE, true, true);
			if (i.hasNext()) {
				sWriter.write(ClipboardController.NODESEPARATOR);
			}
		}
		final StringBuffer childXml = sWriter.getBuffer();
		encryptedContent = encryptXml(childXml);
	}

	public String getEncryptedContent(final MapController mapController) {
		if (isDecrypted) {
			try {
				generateEncryptedContent(mapController);
			}
			catch (final Exception e) {
				LogTool.severe(e);
			}
		}
		return encryptedContent;
	}

	private void init(final NodeModel node) {
		if (EncryptionModel.logger == null) {
			EncryptionModel.logger = Logger.global;
		}
		if (EncryptionModel.encryptedIcon == null) {
			EncryptionModel.encryptedIcon = STORE.getUIIcon("lock.png");
		}
		if (EncryptionModel.decryptedIcon == null) {
			EncryptionModel.decryptedIcon = STORE.getUIIcon("unlock.png");
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

	private void pasteXML(final String pasted, final NodeModel target, final MapController mapController) {
		try {
			final NodeModel node = mapController.getMapReader().createNodeTreeFromXml(target.getMap(),
			    new StringReader(pasted), Mode.FILE);
			mapController.insertNodeIntoWithoutUndo(node, target, target.getChildCount());
		}
		catch (final Exception ee) {
			LogTool.severe(ee);
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

	public void setEncrypter(final IEncrypter encrypter) {
		mEncrypter = encrypter;
	}

	/*
	 * (non-Javadoc)
	 * @see freeplane.modes.MindMapNode#getIcons()
	 */
	public void updateIcon() {
		if (isAccessible()) {
			node.removeStateIcons("encrypted");
			node.setStateIcon("decrypted", EncryptionModel.decryptedIcon, true);
		}
		else {
			node.removeStateIcons("decrypted");
			node.setStateIcon("encrypted", EncryptionModel.encryptedIcon, true);
		}
	}
}
