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
package org.freeplane.features.help;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.HtmlUtils;
import org.freeplane.core.util.MenuUtils;
import org.freeplane.core.util.MenuUtils.MenuEntry;
import org.freeplane.core.util.TextUtils;

/**
 * @author Dimitry Polivaev
 * Sep 27, 2011
 */

public class HotKeyInfoAction extends AFreeplaneAction{

	public HotKeyInfoAction() {
	    super("HotKeyInfoAction");
    }

	/**
     * 
     */
    private static final long serialVersionUID = 1L;
	// ==========================================================================
	//                 format accelerator map as html text
	// ==========================================================================
	private String formatAsHtml(final Enumeration<TreeNode> children) {
		final StringBuilder builder = new StringBuilder();
		builder.append("<html><head><style type=\"text/css\">" //
		        //doesn't work: + "  table { margin: 1px 0px; border-spacing: 0px; }"//
		        + "  h1 { background-color: #B5C8DB; margin-bottom: 0px; margin-top: 1ex; }"//
		        + "  h2 { background-color: #B5C8DB; margin-bottom: 0px; margin-top: 1ex; }"//
		        + "  h3 { background-color: #B5C8DB; margin-bottom: 0px; margin-top: 1ex; }"//
		        + "</head><body width=\"600\">");
		appendAsHtml(builder, children, "", 2);
		builder.append("</body></html>");
		return builder.toString();
	}

	private void appendAsHtml(final StringBuilder builder, final Enumeration<TreeNode> children,
	                                 final String title, final int level) {
		if(! title.equals(""))
			builder.append("<h").append(level).append('>').append(title).append("</h").append(level).append('>');
		appendChildrenAsHtml(builder, children, title, level);
	}

	@SuppressWarnings("unchecked")
	private void appendChildrenAsHtml(final StringBuilder builder,
	                                         final Enumeration<TreeNode> children, final String title,
	                                         final int level) {
		final ArrayList<MenuEntry> menuEntries = new ArrayList<MenuEntry>();
		final ArrayList<DefaultMutableTreeNode> submenus = new ArrayList<DefaultMutableTreeNode>();
		// sort and divide
		while (children.hasMoreElements()) {
			final DefaultMutableTreeNode node = (DefaultMutableTreeNode) children.nextElement();
			if (node.isLeaf()) {
				menuEntries.add((MenuEntry) node.getUserObject());
			}
			else {
				submenus.add(node);
			}
		}
		// actions
		if (!menuEntries.isEmpty()) {
			builder.append("<table cellspacing=\"0\" cellpadding=\"0\">");
			for (final MenuEntry entry : menuEntries) {
				final String keystroke = entry.getKeyStroke() == null ? "" //
				        : MenuUtils.formatKeyStroke(entry.getKeyStroke());
				builder.append(el("tr", el("td", entry.getLabel() + "&#xa0;")
				        + el("td", keystroke)
				        + el("td", entry.getToolTipText())));
			}
			builder.append("</table>");
		}
		// submenus
		for (final DefaultMutableTreeNode node : submenus) {
			final String subtitle = (level > 2 ? title + "&#8594;" : "") + String.valueOf(node.getUserObject());
			appendAsHtml(builder, node.children(), subtitle, level + 1);
		}
	}

	private String el(final String name, final String content) {
		return HtmlUtils.element(name, content);
	}

	@SuppressWarnings("unchecked")
	public void actionPerformed(ActionEvent e) {
		final DefaultMutableTreeNode menuEntryTree = MenuUtils.createAcceleratebleMenuEntryTree("main_menu");
		final String title = TextUtils.getText("hot_keys_table");
		final String html = formatAsHtml(menuEntryTree.children());
		JEditorPane refPane = new JEditorPane("text/html", html);
		refPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, false);
		refPane.setCaretPosition(0);
		refPane.setEditable(false);
		final Dimension preferredSize = refPane.getPreferredSize();
		JScrollPane scrollPane = new JScrollPane(refPane, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setPreferredSize(new Dimension(preferredSize.width, 600));
		JOptionPane pane = new JOptionPane(scrollPane, JOptionPane.INFORMATION_MESSAGE);
		JDialog dialog = pane.createDialog(UITools.getMenuComponent(), title);
		 // the line below is added to the example from the docs
		 dialog.setModal(false); // this says not to block background components
		dialog.setResizable(true);
		dialog.setVisible(true);
    }
}
