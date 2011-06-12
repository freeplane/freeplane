/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2009 Dimitry
 *
 *  This file author is Dimitry
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
package org.freeplane.features.styles.mindmapmode;

import java.awt.event.ActionEvent;

import javax.swing.ImageIcon;

import org.freeplane.core.ui.AMultipleNodeAction;
import org.freeplane.core.ui.SelectableAction;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.styles.IStyle;
import org.freeplane.features.styles.LogicalStyleController;
import org.freeplane.features.styles.StyleNamedObject;

/**
 * @author Dimitry Polivaev
 * 28.09.2009
 */
@SelectableAction(checkOnNodeChange = true)
public class AssignStyleAction extends AMultipleNodeAction {
	final private IStyle style;

	public AssignStyleAction(final IStyle style, final String title, final ImageIcon icon) {
		super("AssignStyleAction." + StyleNamedObject.toKeyString(style), title, icon);
		this.style = style;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void actionPerformed(final ActionEvent e, final NodeModel node) {
		final MLogicalStyleController controller = (MLogicalStyleController) Controller.getCurrentModeController().getExtension(
		    LogicalStyleController.class);
		controller.setStyle(node, style);
	}

	@Override
	public void setSelected() {
		NodeModel node= Controller.getCurrentController().getSelection().getSelected();
		final IStyle style = LogicalStyleController.getController().getFirstStyle(node);
		setSelected(this.style.equals(style));
	}
}
