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
package org.freeplane.features.mindmapmode.nodestyle;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.AMultipleNodeAction;
import org.freeplane.core.ui.ActionLocationDescriptor;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.common.addins.styles.LogicalStyleKeys;
import org.freeplane.features.common.map.NodeModel;

/**
 * @author foltin
 */
class FormatCopy extends AFreeplaneAction {
	private static NodeModel pattern = null;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static NodeModel getPattern() {
		return pattern;
	}

	public FormatCopy(final Controller controller) {
		super("FormatCopy", controller);
	}

	public void actionPerformed(final ActionEvent e) {
		copyFormat(getModeController().getMapController().getSelectedNode());
	}

	/**
	 */
	private void copyFormat(final NodeModel node) {
		FormatCopy.pattern = new NodeModel(null);
		getModeController().undoableCopyExtensions(LogicalStyleKeys.NODE_STYLE, node, pattern);
		getModeController().undoableCopyExtensions(LogicalStyleKeys.LOGICAL_STYLE, node, pattern);
	}
}

/**
 * @author foltin
 */
@ActionLocationDescriptor(locations = { "/menu_bar/edit/paste" }, //
accelerator = "alt shift V")
class FormatPaste extends AMultipleNodeAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public FormatPaste(final Controller controller) {
		super("FormatPaste", controller);
	}

	@Override
	protected void actionPerformed(final ActionEvent e, final NodeModel node) {
		pasteFormat(node);
	}

	/**
	 */
	private void pasteFormat(final NodeModel node) {
		final NodeModel pattern = FormatCopy.getPattern();
		if (pattern == null) {
			JOptionPane.showMessageDialog(getController().getViewController().getContentPane(), TextUtils
			    .getText("no_format_copy_before_format_paste"), "" /*=Title*/, JOptionPane.ERROR_MESSAGE);
			return;
		}
		getModeController().undoableCopyExtensions(LogicalStyleKeys.NODE_STYLE, pattern, node);
		getModeController().undoableCopyExtensions(LogicalStyleKeys.LOGICAL_STYLE, pattern, node);
	}
}
