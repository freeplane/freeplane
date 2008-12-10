/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is to be deleted.
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
package deprecated.freemind.modes.mindmapmode.actions.undo;

import java.awt.event.ActionEvent;
import java.util.ListIterator;

import javax.swing.Action;

import org.freeplane.controller.Controller;
import org.freeplane.main.Tools;
import org.freeplane.map.tree.NodeModel;
import org.freeplane.map.tree.mindmapmode.MindMapMapModel;
import org.freeplane.ui.MenuBuilder;

import deprecated.freemind.modes.mindmapmode.actions.instance.ActionInstance;
import deprecated.freemind.modes.mindmapmode.actions.instance.CompoundActionInstance;

public class NodeGeneralAction extends AbstractUndoableAction {
	private deprecated.freemind.modes.mindmapmode.actions.undo.INodeActor actor;
	ISingleNodeOperation singleNodeOperation;

	/**
	 * null if you cannot provide a title that is present in the resources. Use
	 * the setName method to set your not translateble title after that. give a
	 * resource name for the icon.
	 */
	protected NodeGeneralAction(final String textID, final String iconPath) {
		super(textID, iconPath);
		if (textID != null) {
			setName(Controller.getText(textID));
		}
		singleNodeOperation = null;
		actor = null;
	}

	public NodeGeneralAction(
	                         final String textID,
	                         final String iconPath,
	                         final deprecated.freemind.modes.mindmapmode.actions.undo.INodeActor actor) {
		this(textID, iconPath);
		addActor(actor);
	}

	public NodeGeneralAction(final String textID, final String iconPath,
	                         final ISingleNodeOperation singleNodeOperation) {
		this(textID, iconPath);
		this.singleNodeOperation = singleNodeOperation;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * freemind.controller.actions.FreeMindAction#act(freemind.controller.actions
	 * .generated.instance.XmlAction)
	 */
	public void act(final ActionInstance action) {
	}

	public void addActor(final INodeActor actor) {
		this.actor = actor;
		if (actor != null) {
			super.addActor(actor);
		}
	}

	protected void execute(final ActionPair pair) {
		getMModeController().getActionFactory().executeAction(pair);
	}

	/**
	 */
	protected NodeModel getNodeFromID(final String string) {
		return getMModeController().getMapController().getNodeFromID(string);
	}

	/**
	 */
	protected String getNodeID(final NodeModel selected) {
		return getMModeController().getMapController().getNodeID(selected);
	}

	protected void setName(final String name) {
		if (name != null) {
			MenuBuilder.setLabelAndMnemonic(this, name);
			putValue(Action.SHORT_DESCRIPTION, Tools.removeMnemonic(name));
		}
	}

	/**
	 * The singleNodeOperation to set.
	 */
	public void setSingleNodeOperation(
	                                   final ISingleNodeOperation singleNodeOperation) {
		this.singleNodeOperation = singleNodeOperation;
	}

	@Override
	public void undoableActionPerformed(final ActionEvent e) {
		if (singleNodeOperation != null) {
			for (final ListIterator it = getMModeController()
			    .getSelectedNodes().listIterator(); it.hasNext();) {
				final NodeModel selected = (NodeModel) it.next();
				singleNodeOperation.apply((MindMapMapModel) Controller
				    .getController().getMap(), selected);
			}
		}
		else {
			final CompoundActionInstance doAction = new CompoundActionInstance();
			final CompoundActionInstance undo = new CompoundActionInstance();
			for (final ListIterator it = getMModeController()
			    .getSelectedNodes().listIterator(); it.hasNext();) {
				final NodeModel selected = (NodeModel) it.next();
				final ActionPair pair = actor.getActionPair(selected);
				if (pair != null) {
					doAction.addChoice(pair.getDoAction());
					undo.addAtChoice(0, pair.getUndoAction());
				}
			}
			if (doAction.sizeChoiceList() == 0) {
				return;
			}
			getMModeController().getActionFactory().startTransaction(
			    (String) getValue(Action.NAME));
			getMModeController().getActionFactory().executeAction(
			    new ActionPair(doAction, undo));
			getMModeController().getActionFactory().endTransaction(
			    (String) getValue(Action.NAME));
		}
	}
}
