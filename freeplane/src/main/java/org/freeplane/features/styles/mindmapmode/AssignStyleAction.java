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

import org.freeplane.core.ui.AMultipleNodeAction;
import org.freeplane.core.ui.SelectableAction;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.IMapSelection;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.styles.IStyle;
import org.freeplane.features.styles.LogicalStyleController;
import org.freeplane.features.styles.LogicalStyleModel;
import org.freeplane.features.styles.StyleTranslatedObject;

/**
 * @author Dimitry Polivaev
 * 28.09.2009
 */
@SelectableAction(checkOnPopup = true)
public class AssignStyleAction extends AMultipleNodeAction {
	final private IStyle style;

	public AssignStyleAction(final IStyle style) {
		super(actionName(style), actionText(style), null);
		this.style = style;
	}

	private static String actionText(final IStyle style) {
		if(style != null)
			return style.toString();
		return TextUtils.getRawText("ResetStyleAction.text");
    }

	private static String actionName(final IStyle style) {
		if(style != null)
			return "AssignStyleAction." + StyleTranslatedObject.toKeyString(style);
		return "ResetStyleAction";
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
		IMapSelection selection = Controller.getCurrentController().getSelection();
		if(selection != null){
			NodeModel node= selection.getSelected();
			final IStyle style = LogicalStyleModel.getStyle(node);
			setSelected(this.style == style || this.style != null && this.style.equals(style));
		}
		else
			setSelected(false);
	}
}
