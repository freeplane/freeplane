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
package org.freeplane.features.link.mindmapmode;

import java.awt.event.ActionEvent;
import java.util.Optional;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.features.link.ConnectorModel;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.link.ConnectorModel.Shape;

class ChangeConnectorShapeAction extends AFreeplaneAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	ConnectorModel arrowLink;
	final private Shape shape;

	public ChangeConnectorShapeAction(final MLinkController linkController, final ConnectorModel arrowLink, final Shape shape) {
		super("ChangeConnectorShapeAction." + shape.toString());
		this.arrowLink = arrowLink;
		this.shape = shape;
		final boolean selected = shape.equals(linkController.getShape(arrowLink));
		setSelected(selected);
	}

	public void actionPerformed(final ActionEvent e) {
		final MLinkController linkController = (MLinkController) LinkController.getController();
		linkController.setShape(arrowLink, Optional.of(shape));
	}
}
