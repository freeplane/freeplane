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
package org.freeplane.features.mindmapmode.text;

import java.awt.event.ActionEvent;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.icon.IconController;
import org.freeplane.core.icon.MindIcon;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.HtmlTools;
import org.freeplane.features.common.text.TextController;
import org.freeplane.features.mindmapmode.MMapController;
import org.freeplane.features.mindmapmode.icon.MIconController;

class JoinNodesAction extends AFreeplaneAction {
	final static Pattern BODY_END = Pattern.compile("</body>", Pattern.CASE_INSENSITIVE);
	final static Pattern BODY_START = Pattern.compile("<body>", Pattern.CASE_INSENSITIVE);
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public JoinNodesAction(final Controller controller) {
		super("JoinNodesAction", controller);
	}

	public void actionPerformed(final ActionEvent e) {
		final NodeModel selectedNode = getController().getSelection().getSelected();
		final List selectedNodes = getController().getSelection().getSortedSelection(true);
		joinNodes(selectedNode, selectedNodes);
	}

	private String addContent(String joinedContent, final boolean isHtml, String nodeContent, final boolean isHtmlNode) {
		if (isHtml) {
			final String joinedContentParts[] = JoinNodesAction.BODY_END.split(joinedContent, -2);
			joinedContent = joinedContentParts[0];
			if (!isHtmlNode) {
				final String end[] = JoinNodesAction.BODY_START.split(joinedContent, 2);
				if (end.length == 1) {
					end[0] = "<html>";
				}
				nodeContent = end[0] + "<body><p>" + nodeContent + "</p>";
			}
		}
		if (isHtmlNode & !joinedContent.equals("")) {
			final String nodeContentParts[] = JoinNodesAction.BODY_START.split(nodeContent, 2);
			// if no <body> tag is found
			if (nodeContentParts.length == 1) {
				nodeContent = nodeContent.substring(6);
				nodeContentParts[0] = "<html>";
			}
			else {
				nodeContent = nodeContentParts[1];
			}
			if (!isHtml) {
				joinedContent = nodeContentParts[0] + "<body><p>" + joinedContent + "</p>";
			}
		}
		if (joinedContent.equals("")) {
			return nodeContent;
		}
		if (isHtml || isHtmlNode) {
			joinedContent += '\n';
		}
		else {
			joinedContent += ' ';
		}
		joinedContent += nodeContent;
		return joinedContent;
	}

	public void joinNodes(final NodeModel selectedNode, final List selectedNodes) {
		String joinedContent = "";
		final Controller controller = getController();
		for (final Iterator it = selectedNodes.iterator(); it.hasNext();) {
			final NodeModel node = (NodeModel) it.next();
			if (getModeController().getMapController().hasChildren(node)) {
				UITools.informationMessage(controller.getViewController().getFrame(), ResourceBundles
				    .getText("cannot_join_nodes_with_children"), "Freeplane", JOptionPane.WARNING_MESSAGE);
				return;
			}
		}
		boolean isHtml = false;
		final LinkedHashSet<MindIcon> icons = new LinkedHashSet<MindIcon>();
		for (final Iterator it = selectedNodes.iterator(); it.hasNext();) {
			final NodeModel node = (NodeModel) it.next();
			final String nodeContent = node.toString();
			icons.addAll(node.getIcons());
			final boolean isHtmlNode = HtmlTools.isHtmlNode(nodeContent);
			joinedContent = addContent(joinedContent, isHtml, nodeContent, isHtmlNode);
			if (node != selectedNode) {
				((MMapController) getModeController().getMapController()).deleteNode(node);
			}
			isHtml = isHtml || isHtmlNode;
		}
		controller.getSelection().selectAsTheOnlyOneSelected(selectedNode);
		((MTextController) TextController.getController(getModeController())).setNodeText(selectedNode, joinedContent);
		final MIconController iconController = (MIconController) IconController.getController(getModeController());
		iconController.removeAllIcons(selectedNode);
		for (final MindIcon icon : icons) {
			iconController.addIcon(selectedNode, icon);
		}
	}
}
