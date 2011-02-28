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

import java.awt.Color;
import java.awt.event.KeyEvent;

import javax.swing.JComponent;

import org.freeplane.features.common.map.NodeModel;
import org.freeplane.features.mindmapmode.text.EditNodeBase;
import org.freeplane.features.mindmapmode.text.INodeTextFieldCreator;
import org.freeplane.features.mindmapmode.text.EditNodeBase.IEditControl;
import org.freeplane.view.swing.map.MainView;
import org.freeplane.view.swing.map.MapViewController;
import org.freeplane.view.swing.map.NodeView;
import org.freeplane.view.swing.map.ZoomableLabel;

/**
 * @author Dimitry Polivaev
 * Jan 31, 2009
 */
public class MMapViewController extends MapViewController implements INodeTextFieldCreator {
	public EditNodeBase createNodeTextField(final NodeModel node, final EditedComponent parent, final String text,
	                                                     final KeyEvent firstEvent,
	                                                     final IEditControl editControl) {
		final ZoomableLabel parentComponent;
		final MainView mainView = (MainView) getComponent(node);
        final NodeView nodeView = mainView.getNodeView();
		if(EditedComponent.TEXT.equals(parent))
			parentComponent = mainView;
		else if(EditedComponent.DETAIL.equals(parent)) {
			final JComponent component = nodeView.getContent(NodeView.DETAIL_VIEWER_POSITION);
	        if(component instanceof ZoomableLabel)
	        	parentComponent = (ZoomableLabel) component;
	        else
	        	parentComponent = null;
        }
        else
			parentComponent = null;
		if(parentComponent == null || ! parentComponent.isVisible()){
			return null;
		}
		final EditNodeTextField textField = new EditNodeTextField(node, (ZoomableLabel) parentComponent, text, firstEvent,editControl);
		if(EditedComponent.TEXT.equals(parent))
			textField.setBackground (nodeView.getTextBackground());
		else if(EditedComponent.DETAIL.equals(parent))
			textField.setBackground (nodeView.getBackgroundColor());
		return textField;
	}

	public MMapViewController() {
		new EditNodeTextField(null, null, null, null, null);
    }
	
}
