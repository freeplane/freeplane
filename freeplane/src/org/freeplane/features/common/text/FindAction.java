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
package org.freeplane.features.common.text;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.regex.Matcher;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.filter.FilterConditionEditor;
import org.freeplane.core.filter.FilterController;
import org.freeplane.core.filter.condition.ISelectableCondition;
import org.freeplane.core.modecontroller.IMapSelection;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.HtmlTools;

class FindAction extends AFreeplaneAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ISelectableCondition condition;
	private FilterConditionEditor editor;
	private NodeModel findFromNode;
	private LinkedList findNodeQueue;
	private ArrayList findNodesUnfoldedByLastFind;
	private String searchTerm;

	public FindAction(final Controller controller) {
		super("FindAction", controller);
	}

	public void actionPerformed(final ActionEvent e) {
		final IMapSelection selection = getController().getSelection();
		if (selection == null) {
			return;
		}
		final NodeModel selected = selection.getSelected();
		if (editor == null) {
			editor = new FilterConditionEditor(FilterController.getController(getController()));
		}
		else {
			editor.mapChanged(selected.getMap());
		}
		editor.addAncestorListener(new AncestorListener() {
			public void ancestorAdded(final AncestorEvent event) {
				final Component component = event.getComponent();
				((FilterConditionEditor) component).focusInputField();
				((JComponent) component).removeAncestorListener(this);
			}

			public void ancestorMoved(final AncestorEvent event) {
			}

			public void ancestorRemoved(final AncestorEvent event) {
			}
		});
		final int run = UITools.showConfirmDialog(getController(), selected, editor, ResourceBundles
		    .getText("FindAction.text"), JOptionPane.OK_CANCEL_OPTION);
		final Container parent = editor.getParent();
		if (parent != null) {
			parent.remove(editor);
		}
		if (run != JOptionPane.OK_OPTION) {
			return;
		}
		condition = editor.getCondition();
		if (condition == null) {
			return;
		}
		final boolean found = find(getModeController().getMapController().getSelectedNode());
		searchTerm = condition.toString();
		if (!found) {
			final String messageText = ResourceBundles.getText("no_found_from");
			UITools.informationMessage(getController().getViewController().getFrame(), messageText.replaceAll("\\$1",
			    Matcher.quoteReplacement(searchTerm)).replaceAll("\\$2", Matcher.quoteReplacement(getFindFromText())));
			searchTerm = null;
		}
	}

	/**
	 * Display a node in the display (used by find and the goto action by arrow
	 * link actions).
	 */
	public void displayNode(final NodeModel node, final ArrayList nodesUnfoldedByDisplay) {
		final NodeModel[] path = node.getPathToRoot();
		for (int i = 0; i < path.length - 1; i++) {
			final NodeModel nodeOnPath = path[i];
			if (getModeController().getMapController().isFolded(nodeOnPath)) {
				if (nodesUnfoldedByDisplay != null) {
					nodesUnfoldedByDisplay.add(nodeOnPath);
				}
				getModeController().getMapController().setFolded(nodeOnPath, false);
			}
		}
	}

	private boolean find(final LinkedList /* queue of MindMapNode */nodes) {
		if (!findNodesUnfoldedByLastFind.isEmpty()) {
			final ListIterator i = findNodesUnfoldedByLastFind.listIterator(findNodesUnfoldedByLastFind.size());
			while (i.hasPrevious()) {
				final NodeModel node = (NodeModel) i.previous();
				try {
					getModeController().getMapController().setFolded(node, true);
				}
				catch (final Exception e) {
				}
			}
			findNodesUnfoldedByLastFind = new ArrayList();
		}
		while (!nodes.isEmpty()) {
			final NodeModel node = (NodeModel) nodes.removeFirst();
			for (final ListIterator i = getModeController().getMapController().childrenUnfolded(node); i.hasNext();) {
				nodes.addLast(i.next());
			}
			if (!node.isVisible()) {
				continue;
			}
			findNodeQueue = nodes;
			final boolean found = condition.checkNode(node);
			if (found) {
				displayNode(node, findNodesUnfoldedByLastFind);
				getModeController().getMapController().select(node);
				return true;
			}
		}
		getModeController().getMapController().select(findFromNode);
		return false;
	}

	public boolean find(final NodeModel node) {
		findNodesUnfoldedByLastFind = new ArrayList();
		final LinkedList nodes = new LinkedList();
		nodes.addFirst(node);
		findFromNode = node;
		return find(nodes);
	}

	public boolean findNext() {
		if (condition != null) {
			if (findNodeQueue.isEmpty()) {
				return find(getModeController().getMapController().getSelectedNode());
			}
			return find(findNodeQueue);
		}
		return false;
	}

	public String getFindFromText() {
		final String plainNodeText = HtmlTools.htmlToPlain(findFromNode.toString()).replaceAll("\n", " ");
		return plainNodeText.length() <= 30 ? plainNodeText : plainNodeText.substring(0, 30) + "...";
	}

	public String getSearchTerm() {
		return searchTerm;
	}
}
