/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Dimitry Polivaev
 *
 *  This file author is Dimitry Polivaev
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
package org.freeplane.features.common.attribute;

import java.awt.Frame;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.resources.FreeplaneResourceBundle;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.IFreeplaneAction;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.features.mindmapmode.attribute.AttributeManagerDialog;

class ShowAttributeDialogAction extends AbstractAction implements IFreeplaneAction {
	private static final long serialVersionUID = -3301794915146529692L;
	private AttributeManagerDialog attributeDialog = null;
	final private Controller controller;
	private Frame frame;

	/**
	 *
	 */
	ShowAttributeDialogAction(final Controller controller) {
		super(null, new ImageIcon(ResourceController.getResourceController().getResource("/images/showAttributes.gif")));
		this.controller = controller;
		MenuBuilder.setLabelAndMnemonic(this, FreeplaneResourceBundle.getText("attributes_dialog"));
	}

	public void actionPerformed(final ActionEvent e) {
		if (frame == null) {
			frame = controller.getViewController().getFrame();
		}
		if (getAttributeDialog().isVisible() == false && controller.getMap() != null) {
			getAttributeDialog().pack();
			getAttributeDialog().show();
		}
	}

	private AttributeManagerDialog getAttributeDialog() {
		if (attributeDialog == null) {
			attributeDialog = new AttributeManagerDialog(controller, frame);
		}
		return attributeDialog;
	}

	public String getName() {
		return "showAttributeManagerAction";
	}
}
