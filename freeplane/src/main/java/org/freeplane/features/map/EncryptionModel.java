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
package org.freeplane.features.map;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.WeakHashMap;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.map.MapWriter.Mode;
import org.freeplane.features.map.clipboard.MapClipboardController;

public class EncryptionModel implements IExtension {
	private final static WeakHashMap<NodeModel, List<NodeModel>> hiddenChildren = new WeakHashMap<>();
	public static EncryptionModel getModel(final NodeModel node) {
		return node.getExtension(EncryptionModel.class);
	}

	private String encryptedContent;
	private IEncrypter mEncrypter;
	/**
	 * password have to be stored in a StringBuilder as Strings cannot be deleted
	 * or overwritten.
	 */
	final private NodeModel node;

	public EncryptionModel(final NodeModel node, IEncrypter encrypter) {
		this.node = node;
		this.mEncrypter = encrypter;
		encryptedContent = null;
	}

	/**
	 * @param encryptedContent
	 */
	public EncryptionModel(final NodeModel node, final String encryptedContent) {
		this.node = node;
		this.encryptedContent = encryptedContent;
		this.mEncrypter = null;
	}

	private boolean checkAndSetEncrypter(final IEncrypter encrypter) {
		final String decryptedNode = decryptXml(encryptedContent, encrypter);
		if (decryptedNode == null || !decryptedNode.equals("") && !decryptedNode.startsWith("<node ")) {
			LogUtils.warn("Wrong password supplied (stored!=given).");
			return false;
		}
		mEncrypter = encrypter;
		return true;
	}

	/**
	 * @param mapController
	 * @return true, if the password was correct.
	 */
	public boolean decrypt(final MapController mapController, final IEncrypter encrypter) {
		if(encryptedContent == null)
			throw new IllegalStateException("No encrypted content");
		if (!checkAndSetEncrypter(encrypter)) {
			return false;
		}
		if (! hiddenChildren.containsKey(node)) {
			try {
				final String childXml = decryptXml(encryptedContent, encrypter);
				final String[] childs = childXml.split(MapClipboardController.NODESEPARATOR);
				for (int i = 0; i < childs.length; i++) {
					final String string = childs[i];
					if (string.length() == 0) {
						continue;
					}
					pasteXML(string, node, mapController);
					hiddenChildren.put(node, node.getChildrenInternal());
				}
			}
			catch (final Exception e) {
				LogUtils.severe(e);
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

	private String encrypt(final MapWriter mapWriter, List<NodeModel> childNodes) {
		try {
			final StringWriter sWriter = new StringWriter();
			for (final Iterator<NodeModel> i = childNodes.listIterator(); i.hasNext();) {
				final NodeModel child = i.next();
				mapWriter.writeNodeAsXml(sWriter, child, MapWriter.Mode.FILE, true, true, false);
				if (i.hasNext()) {
					sWriter.write(MapClipboardController.NODESEPARATOR);
				}
			}
			final StringBuffer childXml = sWriter.getBuffer();
			String encryptedContent = encryptXml(childXml);
			return encryptedContent;
		}
		catch (IOException e) {
			throw new RuntimeException("Unexpected", e);
		}
	}

	public String calculateEncryptedContent(final MapWriter mapWriter) {
		if (encryptedContent == null) {
			try {
				return encrypt(mapWriter, node.getChildrenInternal());
			}
			catch (final Exception e) {
				LogUtils.severe(e);
			}
		}
		return encryptedContent;
	}

	public boolean isAccessible() {
		return encryptedContent == null;
	}

	public boolean isLocked() {
		return encryptedContent != null;
	}

	private void pasteXML(final String pasted, final NodeModel target, final MapController mapController) {
		try {
			final NodeModel node = mapController.getMapReader().createNodeTreeFromXml(target.getMap(),
			    new StringReader(pasted), Mode.FILE);
			mapController.insertNodeIntoWithoutUndo(node, target, target.getChildCount());
		}
		catch (final Exception ee) {
			LogUtils.severe(ee);
		}
	}

	synchronized public void unlock() {
		node.setChildrenInternal(hiddenChildren.remove(node));
		encryptedContent = null;
	}

	synchronized public void lock(MapWriter mapWriter) {
		List<NodeModel> childNodes = node.getChildrenInternal();
		encryptedContent = encrypt(mapWriter, childNodes);
		List<NodeModel> oldContent = hiddenChildren.put(node, childNodes);
		node.setChildrenInternal(Collections.emptyList());
		if(oldContent != null) {
			LogUtils.severe("Hidden children replaced");
		}
	}
}
