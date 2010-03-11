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
package org.freeplane.features.controller.help;

import java.awt.event.ActionEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.modecontroller.MapController;
import org.freeplane.core.model.MapModel;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.core.ui.components.FreeplaneMenuBar;
import org.freeplane.core.util.HtmlTools;
import org.freeplane.core.util.LogTool;
import org.freeplane.core.util.MenuTools;
import org.freeplane.core.util.MenuTools.MenuEntry;
import org.freeplane.features.browsemode.BModeController;
import org.freeplane.main.mindmapmode.MModeControllerFactory;

class DocumentationAction extends AFreeplaneAction {
	private static final long serialVersionUID = 1L;
	private String document;

	DocumentationAction(final Controller controller, final String actionName, final String document) {
		super(actionName, controller);
		this.document = document;
	}

	public void actionPerformed(final ActionEvent e) {
		final ResourceController resourceController = ResourceController.getResourceController();
		final File baseDir = new File(resourceController.getResourceBaseDir()).getAbsoluteFile().getParentFile();
		final File file;
		final int extPosition = document.lastIndexOf('.');
		if (extPosition != -1) {
			final String languageCode = ((ResourceBundles) resourceController.getResources()).getLanguageCode();
			String map = document.substring(0, extPosition) + "_" + languageCode + document.substring(extPosition);
			File localFile = new File(baseDir, map);
			if (localFile.canRead()) {
				file = localFile;
			}
			else {
				file = new File(baseDir, document);
			}
		}
		else {
			file = new File(baseDir, document);
		}
		try {
			final URL endUrl = file.toURL();
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					try {
						if (endUrl.getFile().endsWith(".mm") && getController().selectMode(BModeController.MODENAME)) {
							if (getModeController().getMapController().newMap(endUrl))
								appendAcceleratableMenuEntries();
						}
						else {
							getController().getViewController().openDocument(endUrl);
						}
					}
					catch (final Exception e1) {
						LogTool.severe(e1);
					}
				}
			});
		}
		catch (MalformedURLException e1) {
			LogTool.warn(e1);
		}
	}

	@Override
	public void afterMapChange(final Object newMap) {
	}

	private void appendAcceleratableMenuEntries() {
		// use the MModeController for the mindmap mode menu - the browse doesn't contain much entries!
		final MenuBuilder menuBuilder = MModeControllerFactory.getModeController().getUserInputListenerFactory()
		    .getMenuBuilder();
		final DefaultMutableTreeNode menuEntryTree = MenuTools.createAcceleratebleMenuEntryTree(
		    FreeplaneMenuBar.MENU_BAR_PREFIX, menuBuilder);
		final MapController mapController = getModeController().getMapController();
		final NodeModel rootNode = mapController.getRootNode();
		final NodeModel newNode = mapController.newNode(ResourceBundles.getText("hot_keys"), rootNode.getMap());
		newNode.setFolded(true);
		newNode.setLeft(true);
		// TODO: search for proper insert point?
		final int HOT_KEYS_INDEX = 2;
		mapController.insertNodeIntoWithoutUndo(newNode, rootNode, HOT_KEYS_INDEX);
		insertAcceleratorHtmlTable(newNode, menuEntryTree);
		MenuTools.insertAsNodeModelRecursively(newNode, menuEntryTree.children(), mapController);
	}

	@SuppressWarnings("unchecked")
	private void insertAcceleratorHtmlTable(NodeModel newNode, DefaultMutableTreeNode menuEntryTree) {
		final MapController mapController = getModeController().getMapController();
		final String title = ResourceBundles.getText("hot_keys_table");
		final MapModel map = mapController.getRootNode().getMap();
		final NodeModel titleNode = mapController.newNode(title, map);
		titleNode.setFolded(true);
		newNode.insert(titleNode);
		final NodeModel tableNode = mapController.newNode(formatAsHtml(menuEntryTree.children(), title), map);
		titleNode.insert(tableNode);
	}

	// ==========================================================================
	//                 format accelerator map as html text
	// ==========================================================================
	private static String formatAsHtml(Enumeration<DefaultMutableTreeNode> children, String title) {
		StringBuilder builder = new StringBuilder();
		builder.append("<html><head/><body>");
		appendAsHtml(builder, children, title, 2);
		builder.append("</body></html>");
		return builder.toString();
	}

	private static void appendAsHtml(StringBuilder builder, Enumeration<DefaultMutableTreeNode> children, String title,
	                                 int level) {
		builder.append("<h").append(level).append('>').append(title).append("</h1>");
		appendChildrenAsHtml(builder, children, title, level);
	}

	@SuppressWarnings("unchecked")
	private static void appendChildrenAsHtml(StringBuilder builder, Enumeration<DefaultMutableTreeNode> children,
	                                         String title, int level) {
		ArrayList<MenuEntry> menuEntries = new ArrayList<MenuEntry>();
		ArrayList<DefaultMutableTreeNode> submenus = new ArrayList<DefaultMutableTreeNode>();
		// sort and divide
		while (children.hasMoreElements()) {
			DefaultMutableTreeNode node = children.nextElement();
			if (node.isLeaf()) {
				menuEntries.add((MenuEntry) node.getUserObject());
			}
			else {
				submenus.add(node);
			}
		}
		// actions
		if (!menuEntries.isEmpty()) {
			builder.append("<table>");
			for (MenuEntry entry : menuEntries) {
				final String keystroke = entry.getKeyStroke() == null ? "" //
				        : MenuTools.formatKeyStroke(entry.getKeyStroke());
				builder.append(el("tr", el("td", entry.getLabel()) + el("td", keystroke)
				        + el("td", entry.getToolTipText())));
			}
			builder.append("</table>");
		}
		// submenus
		for (DefaultMutableTreeNode node : submenus) {
			final String subtitle = (level > 2 ? title + "&#8594;" : "") + String.valueOf(node.getUserObject());
			appendAsHtml(builder, node.children(), subtitle, level + 1);
		}
	}

	private static String el(String name, String content) {
		return HtmlTools.element(name, content);
	}
}
