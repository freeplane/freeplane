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
package org.freeplane.map.text;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

import javax.swing.JOptionPane;

import org.freeplane.controller.Controller;
import org.freeplane.controller.FreeMindAction;
import org.freeplane.main.HtmlTools;
import org.freeplane.map.tree.NodeModel;

class FindAction extends FreeMindAction {
	private boolean findCaseSensitive;
	private NodeModel findFromNode;
	private LinkedList findNodeQueue;
	private ArrayList findNodesUnfoldedByLastFind;
	private String searchTerm;
	private Collection subterms;

	public FindAction() {
		super("find", "images/filefind.png");
	}

	public void actionPerformed(final ActionEvent e) {
		final String what = JOptionPane.showInputDialog(getModeController()
		    .getMapView().getSelected(), getModeController().getText(
		    "find_what"), getModeController().getText("find"),
		    JOptionPane.QUESTION_MESSAGE);
		if (what == null || what.equals("")) {
			return;
		}
		final Collection subterms = breakSearchTermIntoSubterms(what);
		searchTerm = what;
		final boolean found = find(getModeController().getSelectedNode(),
		    subterms, /*
		    		    		    		    	    		    				 * caseSensitive=
		    		    		    		    	    		    				 */
		    false);
		getModeController().getMapView().repaint();
		if (!found) {
			final String messageText = getModeController().getText(
			    "no_found_from");
			final String searchTerm = messageText.startsWith("<html>") ? HtmlTools
			    .toXMLEscapedText(getSearchTerm())
			        : getSearchTerm();
			Controller.getController().informationMessage(
			    messageText.replaceAll("\\$1", searchTerm).replaceAll("\\$2",
			        getFindFromText()),
			    getModeController().getMapView().getSelected());
		}
	}

	private Collection breakSearchTermIntoSubterms(final String searchTerm) {
		final ArrayList subterms = new ArrayList();
		final StringBuffer subterm = new StringBuffer();
		final int len = searchTerm.length();
		char myChar;
		boolean withinQuotes = false;
		for (int i = 0; i < len; ++i) {
			myChar = searchTerm.charAt(i);
			if (myChar == ' ' && withinQuotes) {
				subterm.append(myChar);
			}
			else if ((myChar == ' ' && !withinQuotes)) {
				subterms.add(subterm.toString());
				subterm.setLength(0);
			}
			else if (myChar == '"' && i > 0 && i < len - 1
			        && searchTerm.charAt(i - 1) != ' '
			        && searchTerm.charAt(i + 1) != ' ') {
				subterm.append(myChar);
			}
			else if (myChar == '"' && withinQuotes) {
				withinQuotes = false;
			}
			else if (myChar == '"' && !withinQuotes) {
				withinQuotes = true;
			}
			else {
				subterm.append(myChar);
			}
		}
		subterms.add(subterm.toString());
		return subterms;
	}

	/**
	 */
	private void centerNode(final NodeModel node) {
		getModeController().centerNode(node);
	}

	/**
	 * Display a node in the display (used by find and the goto action by arrow
	 * link actions).
	 */
	public void displayNode(final NodeModel node,
	                        final ArrayList nodesUnfoldedByDisplay) {
		final Object[] path = Controller.getController().getMap()
		    .getPathToRoot(node);
		for (int i = 0; i < path.length - 1; i++) {
			final NodeModel nodeOnPath = (NodeModel) path[i];
			if (nodeOnPath.getModeController().getMapController().isFolded(
			    nodeOnPath)) {
				if (nodesUnfoldedByDisplay != null) {
					nodesUnfoldedByDisplay.add(nodeOnPath);
				}
				getModeController().getMapController().setFolded(nodeOnPath,
				    false);
			}
		}
	}

	private boolean find(final LinkedList /* queue of MindMapNode */nodes,
	                     final Collection subterms, final boolean caseSensitive) {
		if (!findNodesUnfoldedByLastFind.isEmpty()) {
			final ListIterator i = findNodesUnfoldedByLastFind
			    .listIterator(findNodesUnfoldedByLastFind.size());
			while (i.hasPrevious()) {
				final NodeModel node = (NodeModel) i.previous();
				try {
					getModeController().getMapController()
					    .setFolded(node, true);
				}
				catch (final Exception e) {
				}
			}
			findNodesUnfoldedByLastFind = new ArrayList();
		}
		while (!nodes.isEmpty()) {
			final NodeModel node = (NodeModel) nodes.removeFirst();
			for (final ListIterator i = node.getModeController()
			    .getMapController().childrenUnfolded(node); i.hasNext();) {
				nodes.addLast(i.next());
			}
			if (!node.isVisible()) {
				continue;
			}
			final String nodeText = caseSensitive ? node.toString() : node
			    .toString().toLowerCase();
			this.subterms = subterms;
			findCaseSensitive = caseSensitive;
			findNodeQueue = nodes;
			boolean found = true;
			for (final Iterator i = subterms.iterator(); i.hasNext();) {
				if (nodeText.indexOf((String) i.next()) < 0) {
					found = false;
					break;
				}
			}
			if (found) {
				displayNode(node, findNodesUnfoldedByLastFind);
				centerNode(node);
				return true;
			}
		}
		centerNode(findFromNode);
		return false;
	}

	public boolean find(final NodeModel node, final Collection subterms,
	                    final boolean caseSensitive) {
		findNodesUnfoldedByLastFind = new ArrayList();
		final LinkedList nodes = new LinkedList();
		nodes.addFirst(node);
		findFromNode = node;
		Collection finalizedSubterms;
		if (!caseSensitive) {
			finalizedSubterms = new ArrayList();
			for (final Iterator i = subterms.iterator(); i.hasNext();) {
				finalizedSubterms.add(((String) i.next()).toLowerCase());
			}
		}
		else {
			finalizedSubterms = subterms;
		}
		return find(nodes, finalizedSubterms, caseSensitive);
	}

	public boolean findNext() {
		if (subterms != null) {
			if (findNodeQueue.isEmpty()) {
				return find(getModeController().getSelectedNode(), subterms,
				    findCaseSensitive);
			}
			return find(findNodeQueue, subterms, findCaseSensitive);
		}
		return false;
	}

	public String getFindFromText() {
		final String plainNodeText = HtmlTools.htmlToPlain(
		    findFromNode.toString()).replaceAll("\n", " ");
		return plainNodeText.length() <= 30 ? plainNodeText : plainNodeText
		    .substring(0, 30)
		        + "...";
	}

	public String getSearchTerm() {
		return searchTerm;
	}

	/**
	 * @return Returns the subterms.
	 */
	public Collection getSubterms() {
		return subterms;
	}
}
