/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2011 dimitry
 *
 *  This file author is dimitry
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
package org.freeplane.view.swing.ui;

import java.awt.event.MouseEvent;

import javax.swing.SwingUtilities;

import org.freeplane.features.map.NodeModel;
import org.freeplane.features.text.DetailTextModel;
import org.freeplane.features.text.TextController;
import org.freeplane.features.text.mindmapmode.MTextController;
import org.freeplane.view.swing.map.MapView;
import org.freeplane.view.swing.map.NodeView;
import org.freeplane.view.swing.map.ZoomableLabel;

/**
 * @author Dimitry Polivaev
 * Oct 1, 2011
 */
public class DetailsViewMouseListener extends LinkNavigatorMouseListener {
    @Override
    public void mouseClicked(MouseEvent e) {
    	final NodeView nodeView = (NodeView)SwingUtilities.getAncestorOfClass(NodeView.class, e.getComponent());
    	if(nodeView == null)
    		return;
    	MapView mapView = nodeView.getMap();
    	mapView.select();
    	final NodeModel model = nodeView.getModel();
    	TextController controller = TextController.getController();
    	final ZoomableLabel component = (ZoomableLabel) e.getComponent();
    	if(e.getX() < component.getIconWidth())
    		controller.setDetailsHidden(model, ! DetailTextModel.getDetailText(model).isHidden());
    	else if(canEdit(controller) && isEditingStartEventt(e)){
    		((MTextController) controller).editDetails(model, e, e.isAltDown());
    	}
    	else super.mouseClicked(e);
    }

	private boolean canEdit(TextController controller) {
		try {
			return controller instanceof MTextController;
		} catch (Throwable e) {
			return false;
		}
	}

	private boolean isEditingStartEventt(MouseEvent e) {
		return e.getClickCount() == 2;
	}
}