/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is to be deleted.
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
package deprecated.freemind.modes.mindmapmode.actions.instance;

import java.util.ArrayList;

public class TransferableContent {
	private String transferable;
	private String transferableAsDrop;
	private String transferableAsHtml;
	private String transferableAsPlainText;
	private String transferableAsRTF;
	final private ArrayList transferableFileList = new ArrayList();

	public void addAtTransferableFile(final int position,
	                                  final TransferableFile transferableFile) {
		transferableFileList.add(position, transferableFile);
	}

	public void addTransferableFile(final TransferableFile transferableFile) {
		transferableFileList.add(transferableFile);
	}

	public void clearTransferableFileList() {
		transferableFileList.clear();
	}

	public java.util.List getListTransferableFileList() {
		return java.util.Collections.unmodifiableList(transferableFileList);
	}

	public String getTransferable() {
		return transferable;
	}

	public String getTransferableAsDrop() {
		return transferableAsDrop;
	}

	public String getTransferableAsHtml() {
		return transferableAsHtml;
	}

	public String getTransferableAsPlainText() {
		return transferableAsPlainText;
	}

	public String getTransferableAsRTF() {
		return transferableAsRTF;
	}

	public TransferableFile getTransferableFile(final int index) {
		return (TransferableFile) transferableFileList.get(index);
	}

	public void setTransferable(final String transferable) {
		this.transferable = transferable;
	}

	public void setTransferableAsDrop(final String transferableAsDrop) {
		this.transferableAsDrop = transferableAsDrop;
	}

	public void setTransferableAsHtml(final String transferableAsHtml) {
		this.transferableAsHtml = transferableAsHtml;
	}

	public void setTransferableAsPlainText(final String transferableAsPlainText) {
		this.transferableAsPlainText = transferableAsPlainText;
	}

	public void setTransferableAsRTF(final String transferableAsRTF) {
		this.transferableAsRTF = transferableAsRTF;
	}

	public int sizeTransferableFileList() {
		return transferableFileList.size();
	}
}
