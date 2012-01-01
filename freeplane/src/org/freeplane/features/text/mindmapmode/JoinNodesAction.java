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
package org.freeplane.features.text.mindmapmode;

import java.awt.event.ActionEvent;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.HtmlUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.icon.IconController;
import org.freeplane.features.icon.MindIcon;
import org.freeplane.features.icon.mindmapmode.MIconController;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.mindmapmode.MMapController;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.text.TextController;

class JoinNodesAction extends AFreeplaneAction {
	final static Pattern BODY_END = Pattern.compile("</body>", Pattern.CASE_INSENSITIVE);
	final static Pattern BODY_START = Pattern.compile("<body>", Pattern.CASE_INSENSITIVE);
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public JoinNodesAction() {
		super("JoinNodesAction");
	}

	public void actionPerformed(final ActionEvent e) {
		final List<NodeModel> selectedNodes = Controller.getCurrentController().getSelection().getSortedSelection(true);
		joinNodes(selectedNodes);
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

	public void joinNodes(final List<NodeModel> selectedNodes) {
		if(selectedNodes.isEmpty())
			return;
		String joinedContent = "";
		final Controller controller = Controller.getCurrentController();
		boolean isHtml = false;
		final LinkedHashSet<MindIcon> icons = new LinkedHashSet<MindIcon>();
		final NodeModel selectedNode = selectedNodes.get(0);
		for (final NodeModel node: selectedNodes) {
			final String nodeContent = node.getText();
			icons.addAll(node.getIcons());
			final boolean isHtmlNode = HtmlUtils.isHtmlNode(nodeContent);
			joinedContent = addContent(joinedContent, isHtml, nodeContent, isHtmlNode);
			if (node != selectedNode) {
				final MMapController mapController = (MMapController) Controller.getCurrentModeController().getMapController();
				for(final NodeModel child: node.getChildren().toArray(new NodeModel[]{})){
					mapController.moveNode(child, selectedNode, selectedNode.getChildCount());
				}
				mapController.deleteNode(node);
			}
			isHtml = isHtml || isHtmlNode;
		}
		controller.getSelection().selectAsTheOnlyOneSelected(selectedNode);
		((MTextController) TextController.getController()).setNodeText(selectedNode, joinedContent);
		final MIconController iconController = (MIconController) IconController.getController();
		iconController.removeAllIcons(selectedNode);
		for (final MindIcon icon : icons) {
			iconController.addIcon(selectedNode, icon);
		}
	}
}
