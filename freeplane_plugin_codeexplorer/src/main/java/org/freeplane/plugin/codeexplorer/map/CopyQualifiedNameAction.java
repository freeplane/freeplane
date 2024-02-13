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
package org.freeplane.plugin.codeexplorer.map;

import java.awt.event.ActionEvent;
import java.util.stream.Collectors;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.features.clipboard.ClipboardAccessor;
import org.freeplane.features.mode.Controller;

class CopyQualifiedNameAction extends AFreeplaneAction {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public CopyQualifiedNameAction() {
		super("code.CopyQualifiedName");
	}

	@Override
    public void actionPerformed(final ActionEvent e) {
		 String names = Controller.getCurrentController().getSelection()
		 .getOrderedSelection()
		 .stream()
		 .map(CodeNode.class::cast)
		 .map(CodeNode::getCodeElementName)
		 .collect(Collectors.joining(", "));
		 ClipboardAccessor.getInstance().setClipboardContents(names);
	}
}
