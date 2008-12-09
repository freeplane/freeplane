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
package accessories.plugins;

import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.Vector;

import javax.swing.Action;

import org.freeplane.controller.Controller;
import org.freeplane.main.Tools;
import org.freeplane.map.icon.mindmapnode.MIconController;
import org.freeplane.map.tree.NodeModel;
import org.freeplane.map.tree.view.MapView;
import org.freeplane.map.tree.view.NodeView;
import org.freeplane.modes.mindmapmode.MModeController;
import org.freeplane.ui.dialogs.IconSelectionPopupDialog;

import deprecated.freemind.modes.mindmapmode.hooks.MindMapNodeHookAdapter;

/**
 * @author adapted to the plugin mechanism by ganzer
 */
public class IconSelectionPlugin extends MindMapNodeHookAdapter {
	/**
	 */
	public IconSelectionPlugin() {
		super();
	}

	@Override
	public void invoke(final NodeModel rootNode) {
		final NodeView focussed = getController().getSelectedView();
		final Vector actions = new Vector();
		final MModeController controller = getMindMapController();
		final Collection<Action> iconActions = ((MIconController) controller
		    .getIconController()).getIconActions();
		actions.addAll(iconActions);
		actions.add(Controller.getController()
		    .getAction("removeLastIconAction"));
		actions.add(Controller.getController()
		    .getAction("removeAllIconsAction"));
		final IconSelectionPopupDialog selectionDialog = new IconSelectionPopupDialog(
		    Controller.getController().getViewController().getJFrame(), actions);
		final MapView mapView = controller.getMapView();
		mapView.scrollNodeToVisible(focussed, 0);
		selectionDialog.pack();
		Tools.setDialogLocationRelativeTo(selectionDialog, focussed);
		selectionDialog.setModal(true);
		selectionDialog.show();
		final int result = selectionDialog.getResult();
		if (result >= 0) {
			final Action action = (Action) actions.get(result);
			action.actionPerformed(new ActionEvent(action, 0, "icon",
			    selectionDialog.getModifiers()));
		}
	}
}
