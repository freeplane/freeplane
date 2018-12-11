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
package org.freeplane.features.attribute.clipboard;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;

import org.freeplane.core.util.LogUtils;

public class AttributeTransferable implements Transferable, ClipboardOwner {
	public static DataFlavor attributesFlavor = null;
	static {
		try {
			AttributeTransferable.attributesFlavor = new DataFlavor("text/freeplane-attributes; class=java.lang.String");
		}
		catch (final Exception e) {
			LogUtils.severe(e);
		}
	}
	final private String attributesContent;
	final private String stringContent;

	public AttributeTransferable(final String attributesContent, final String stringContent) {
		this.attributesContent = attributesContent;
		this.stringContent = stringContent;
	}

	@Override
	public Object getTransferData(final DataFlavor flavor) throws UnsupportedFlavorException {
		if (flavor.equals(DataFlavor.stringFlavor)) {
			return stringContent;
		}
		if (flavor.equals(AttributeTransferable.attributesFlavor)) {
			return attributesContent;
		}
		throw new UnsupportedFlavorException(flavor);
	}

	@Override
	public DataFlavor[] getTransferDataFlavors() {
		return new DataFlavor[] { DataFlavor.stringFlavor, AttributeTransferable.attributesFlavor};
	}

	@Override
	public boolean isDataFlavorSupported(final DataFlavor flavor) {
		if (flavor.equals(DataFlavor.stringFlavor)) {
			return true;
		}
		else if (flavor.equals(AttributeTransferable.attributesFlavor)) {
			return true;
		}
		return false;
	}

	@Override
	public void lostOwnership(final Clipboard clipboard, final Transferable contents) {
	}
}
