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
package org.freeplane.features.mindmapmode.addins;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.AMultipleNodeAction;
import org.freeplane.core.ui.ActionLocationDescriptor;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.features.mindmapnode.pattern.MPatternController;
import org.freeplane.features.mindmapnode.pattern.Pattern;
import org.freeplane.features.mindmapnode.pattern.StylePatternFactory;

/**
 * @author foltin
 */
@ActionLocationDescriptor(locations = { "/menu_bar/edit/paste" }, //
accelerator = "alt shift C")
class FormatCopy extends AFreeplaneAction {
	private static Pattern pattern = null;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static Pattern getPattern() {
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
		FormatCopy.pattern = StylePatternFactory.createPatternFromNode(node);
	}
}

/**
 * @author foltin
 */
@ActionLocationDescriptor(locations = { "/menu_bar/edit/paste" }, //
accelerator = "alt shift V")
public class FormatPaste extends AMultipleNodeAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public FormatPaste(final Controller controller, final MenuBuilder menuBuilder) {
		super("FormatPaste", controller);
		menuBuilder.addAnnotatedAction(new FormatCopy(controller));
		menuBuilder.addAnnotatedAction(this);
	}

	@Override
	protected void actionPerformed(final ActionEvent e, final NodeModel node) {
		pasteFormat(node);
	}

	/**
	 */
	private void pasteFormat(final NodeModel node) {
		final Pattern pattern = FormatCopy.getPattern();
		if (pattern == null) {
			JOptionPane.showMessageDialog(getController().getViewController().getContentPane(), ResourceBundles
			    .getText("no_format_copy_before_format_paste"), "" /*=Title*/, JOptionPane.ERROR_MESSAGE);
			return;
		}
		MPatternController.getController((getModeController())).applyPattern(node, pattern);
	}
}
