/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
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
package org.freeplane.features.mindmapmode.addins.styles;

/*
 * Fc, 8.4.06: ArrayListTransferHandler.java was adapted from Sun Tutorial
 * Examples. License unknown. We take it on "As is" basis.
 */
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.TransferHandler;

import org.freeplane.core.util.LogTool;
import org.freeplane.features.mindmapmode.addins.styles.ManagePatternsPopupDialog.PatternListModel;

class ArrayListTransferHandler extends TransferHandler {
	class ArrayListTransferable implements Transferable {
		ArrayList data;

		public ArrayListTransferable(final ArrayList alist) {
			data = alist;
		}

		public Object getTransferData(final DataFlavor flavor) throws UnsupportedFlavorException {
			if (!isDataFlavorSupported(flavor)) {
				throw new UnsupportedFlavorException(flavor);
			}
			return data;
		}

		public DataFlavor[] getTransferDataFlavors() {
			return new DataFlavor[] { localArrayListFlavor };
		}

		public boolean isDataFlavorSupported(final DataFlavor flavor) {
			if (localArrayListFlavor.equals(flavor)) {
				return true;
			}
			return false;
		}
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	int addCount = 0;
	int addIndex = -1;
	int[] indices = null;
	DataFlavor localArrayListFlavor;
	String localArrayListType = DataFlavor.javaJVMLocalObjectMimeType + ";class=java.util.ArrayList";
	JList source = null;

	public ArrayListTransferHandler() {
		try {
			localArrayListFlavor = new DataFlavor(localArrayListType);
		}
		catch (final ClassNotFoundException e) {
			System.out.println("ArrayListTransferHandler: unable to create data flavor");
		}
	}

	@Override
	public boolean canImport(final JComponent c, final DataFlavor[] flavors) {
		if (hasLocalArrayListFlavor(flavors)) {
			return true;
		}
		return false;
	}

	@Override
	protected Transferable createTransferable(final JComponent c) {
		if (c instanceof JList) {
			source = (JList) c;
			indices = source.getSelectedIndices();
			final Object[] values = source.getSelectedValues();
			if (values == null || values.length == 0) {
				return null;
			}
			final ArrayList alist = new ArrayList(values.length);
			for (int i = 0; i < values.length; i++) {
				final Object o = values[i];
				String str = o.toString();
				if (str == null) {
					str = "";
				}
				alist.add(str);
			}
			return new ArrayListTransferable(alist);
		}
		return null;
	}

	@Override
	protected void exportDone(final JComponent c, final Transferable data, final int action) {
		if ((action == TransferHandler.MOVE) && (indices != null)) {
			final PatternListModel model = (PatternListModel) source.getModel();
			if (addCount > 0) {
				for (int i = 0; i < indices.length; i++) {
					if (indices[i] > addIndex) {
						indices[i] += addCount;
					}
				}
			}
			for (int i = indices.length - 1; i >= 0; i--) {
				model.remove(indices[i]);
			}
		}
		indices = null;
		addIndex = -1;
		addCount = 0;
	}

	@Override
	public int getSourceActions(final JComponent c) {
		return TransferHandler.COPY_OR_MOVE;
	}

	private boolean hasLocalArrayListFlavor(final DataFlavor[] flavors) {
		if (localArrayListFlavor == null) {
			return false;
		}
		for (int i = 0; i < flavors.length; i++) {
			if (flavors[i].equals(localArrayListFlavor)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean importData(final JComponent c, final Transferable t) {
		JList target = null;
		ArrayList alist = null;
		if (!canImport(c, t.getTransferDataFlavors())) {
			return false;
		}
		try {
			target = (JList) c;
			if (hasLocalArrayListFlavor(t.getTransferDataFlavors())) {
				alist = (ArrayList) t.getTransferData(localArrayListFlavor);
			}
			else {
				return false;
			}
		}
		catch (final UnsupportedFlavorException ufe) {
			System.out.println("importData: unsupported data flavor");
			return false;
		}
		catch (final IOException ioe) {
			LogTool.severe("importData: I/O exception", ioe);
			return false;
		}
		int index = target.getSelectedIndex();
		if (source.equals(target)) {
			if (indices != null && index >= indices[0] - 1 && index <= indices[indices.length - 1]) {
				indices = null;
				return true;
			}
		}
		final PatternListModel listModel = (PatternListModel) target.getModel();
		final int max = listModel.getSize();
		if (index < 0) {
			index = max;
		}
		else {
			index++;
			if (index > max) {
				index = max;
			}
		}
		addIndex = index;
		addCount = alist.size();
		for (int i = 0; i < alist.size(); i++) {
			listModel.add(index++, alist.get(i));
		}
		return true;
	}
}
