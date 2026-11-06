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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.WeakHashMap;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.map.MapWriter.Mode;
import org.freeplane.features.map.clipboard.MapClipboardController;
import org.freeplane.features.map.clipboard.MapClipboardController.CopiedNodeSet;

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

    /**
     * @return true if decrypted child content passes the start/end checks (or is empty),
     * and uncached children can be read. Legacy DES does not authenticate the password.
     */
    public boolean decrypt(final MapController mapController, final IEncrypter encrypter) {
        if (encryptedContent == null)
            throw new IllegalStateException("No encrypted content");
        final String[] childXml = decryptXml(encryptedContent, encrypter);
        if (childXml == null) {
            return false;
        }
        if (!hiddenChildren.containsKey(node)) {
            final List<NodeModel> children = new ArrayList<>();
            try {
                for (final String xml : childXml) {
                    children.add(mapController.getMapReader().createNodeTreeFromXml(node.getMap(),
                            new StringReader(xml), Mode.FILE));
                }
            }
            catch (final Exception e) {
                LogUtils.severe(e);
                return false;
            }
            for (final NodeModel child : children) {
                mapController.insertNodeIntoWithoutUndo(child, node, node.getChildCount());
            }
            hiddenChildren.put(node, node.getChildrenInternal());
        }
        mEncrypter = encrypter;
        return true;
    }

    private String[] decryptXml(final String encryptedString, final IEncrypter encrypter) {
        final String decrypted = encrypter.decrypt(encryptedString);
        if (decrypted == null) {
            return null;
        }
        if (decrypted.isEmpty()) {
            return new String[0];
        }
        final String[] childXml = decrypted.split(MapClipboardController.NODESEPARATOR, -1);
        for (final String xml : childXml) {
            // Reject DES garbage before importing or reusing cached children.
            if (!hasNodeXmlStartAndEnd(xml)) {
                return null;
            }
        }
        return childXml;
    }

    private boolean hasNodeXmlStartAndEnd(final String xml) {
        int start = 0;
        int end = xml.length();
        while (start < end && isXmlWhitespace(xml.charAt(start))) {
            start++;
        }
        while (end > start && isXmlWhitespace(xml.charAt(end - 1))) {
            end--;
        }
        final String nodeXml = xml.substring(start, end);
        final String nodeStart = "<" + NodeBuilder.XML_NODE;
        if (nodeXml.length() < nodeStart.length() + 2 || !nodeXml.startsWith(nodeStart)) {
            return false;
        }
        final char afterNodeName = nodeXml.charAt(nodeStart.length());
        final boolean hasNodeStart = isXmlWhitespace(afterNodeName) || afterNodeName == '>'
                || nodeXml.startsWith("/>", nodeStart.length());
        return hasNodeStart && (nodeXml.endsWith("</" + NodeBuilder.XML_NODE + ">") || nodeXml.endsWith("/>"));
    }

    private boolean isXmlWhitespace(final char character) {
        return character == ' ' || character == '\t' || character == '\r' || character == '\n';
    }

	private String encryptXml(final StringBuffer childXml) {
		if (mEncrypter == null) {
			throw new IllegalStateException("Cannot encrypt: encrypter is null");
		}
		try {
			return mEncrypter.encrypt(childXml.toString());
		}
		catch (final Exception e) {
			throw new IllegalArgumentException("Can't encrypt the node", e);
		}
	}

	private String encrypt(final MapWriter mapWriter, List<NodeModel> childNodes) {
		try {
			final StringWriter sWriter = new StringWriter();
			for (final Iterator<NodeModel> i = childNodes.listIterator(); i.hasNext();) {
				final NodeModel child = i.next();
				mapWriter.writeNodeAsXml(sWriter, child, MapWriter.Mode.FILE, CopiedNodeSet.ALL_NODES, true, false);
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
	
	/**
	 * Get the encrypted content string. Used for algorithm detection.
	 * @return the encrypted content, or null if not encrypted
	 */
	public String getEncryptedContent() {
		return encryptedContent;
	}

	public void setEncrypter(IEncrypter encrypter) {
		if (mEncrypter != null) {
			mEncrypter.destroy();
		}
		mEncrypter = encrypter;
	}

	synchronized public void unlock() {
		node.setChildrenInternal(hiddenChildren.remove(node));
		encryptedContent = null;
	}

	synchronized public void lock(MapWriter mapWriter) {
		List<NodeModel> childNodes = node.getChildrenInternal();
		String encryptedContent = encrypt(mapWriter, childNodes);
		if(encryptedContent != null) {
			this.encryptedContent = encryptedContent;
			List<NodeModel> oldContent = hiddenChildren.put(node, childNodes);
			node.setChildrenInternal(Collections.emptyList());
			if(oldContent != null) {
				LogUtils.severe("Hidden children replaced");
			}
		}
	}
	
	public void destroy() {
		if (mEncrypter != null) {
			mEncrypter.destroy();
			mEncrypter = null;
		}
	}
}
