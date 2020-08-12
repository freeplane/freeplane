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

import javax.swing.JPopupMenu;

import org.freeplane.core.util.Compat;
import org.freeplane.features.map.IMapSelection;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.text.DetailTextModel;
import org.freeplane.features.text.TextController;
import org.freeplane.features.text.mindmapmode.MTextController;
import org.freeplane.view.swing.map.NodeView;
import org.freeplane.view.swing.map.ZoomableLabel;

/**
 * @author Dimitry Polivaev
 * Oct 1, 2011
 */
public class DetailsViewMouseListener extends LinkNavigatorMouseListener {
	protected final NodeSelector nodeSelector;

	public DetailsViewMouseListener() {
		nodeSelector = new NodeSelector();
	}

	@Override
    public void mouseClicked(MouseEvent e) {
		final ModeController mc = Controller.getCurrentController().getModeController();
		if (Compat.isMacOsX()) {
			final JPopupMenu popupmenu = mc.getUserInputListenerFactory().getNodePopupMenu();
			if (popupmenu.isShowing()) {
				return;
			}
		}
		final NodeView nodeView = nodeSelector.getRelatedNodeView(e);
		if (nodeView == null)
			return;
		final NodeModel model = nodeView.getModel();
    	TextController controller = TextController.getController();
		if (eventFromHideDisplayArea(e)){
			final IMapSelection selection = Controller.getCurrentController().getSelection();
			selection.preserveNodeLocationOnScreen(model);
    		controller.setDetailsHidden(model, ! DetailTextModel.getDetailText(model).isHidden());
		}
		else {
			nodeSelector.extendSelection(e);
			if (canEdit(controller) && isEditingStartEvent(e)) {
				final boolean editLong = e.isAltDown();
				if(! editLong)
					((MTextController)controller).getEventQueue().activate(e);
				((MTextController) controller).editDetails(model, e, editLong);
			}
			else
				super.mouseClicked(e);
		}
    }

	protected boolean eventFromHideDisplayArea(MouseEvent e) {
		final ZoomableLabel component = (ZoomableLabel) e.getComponent();
	    return e.getX() < component.getIconWidth();
    }

	private boolean canEdit(TextController controller) {
		return controller.canEdit();
	}

	private boolean isEditingStartEvent(MouseEvent e) {
		return e.getClickCount() == 2;
	}

	@Override
	public void mousePressed(MouseEvent e) {
		new NodePopupMenuDisplayer().showNodePopupMenu(e);
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		new NodePopupMenuDisplayer().showNodePopupMenu(e);
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		super.mouseMoved(e);
		if (!eventFromHideDisplayArea(e) && nodeSelector.isRelevant(e))
			nodeSelector.createTimer(e);
		else
			nodeSelector.stopTimerForDelayedSelection();
	}

	/**
	 * Invoked when a mouse button is pressed on a component and then
	 * dragged.
	 */
	@Override
	public void mouseDragged(final MouseEvent e) {
		nodeSelector.stopTimerForDelayedSelection();
		nodeSelector.selectSingleNode(e);
	}

	@Override
	public void mouseEntered(final MouseEvent e) {
		if (!eventFromHideDisplayArea(e) && nodeSelector.isRelevant(e)) {
			mouseMoved(e);
		}
		else
			nodeSelector.stopTimerForDelayedSelection();
	}

	@Override
	public void mouseExited(final MouseEvent e) {
		nodeSelector.stopTimerForDelayedSelection();
		nodeSelector.trackWindowForComponent(e.getComponent());
	}
}