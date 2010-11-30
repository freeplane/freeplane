/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2009 Dimitry Polivaev
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
package org.freeplane.view.swing.map.mindmapmode;

import java.awt.event.KeyEvent;

import org.freeplane.features.common.map.NodeModel;
import org.freeplane.features.mindmapmode.text.AbstractEditNodeTextField;
import org.freeplane.features.mindmapmode.text.INodeTextFieldCreator;
import org.freeplane.features.mindmapmode.text.EditNodeBase.IEditControl;
import org.freeplane.view.swing.map.MapViewController;

/**
 * @author Dimitry Polivaev
 * Jan 31, 2009
 */
public class MMapViewController extends MapViewController implements INodeTextFieldCreator {
	public AbstractEditNodeTextField createNodeTextField(final NodeModel node, final String text,
	                                                     final KeyEvent firstEvent,
	                                                     final IEditControl editControl) {
		return new EditNodeTextField(node, text, firstEvent,editControl);
	}

	public MMapViewController() {
		createNodeTextField(null, null, null, null);
    }
	
}
