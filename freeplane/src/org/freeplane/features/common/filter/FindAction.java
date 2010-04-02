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
package org.freeplane.features.common.filter;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.regex.Matcher;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.controller.IMapSelection;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.HtmlUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.common.filter.condition.ISelectableCondition;
import org.freeplane.features.common.map.NodeModel;
import org.freeplane.features.common.text.TextController;
import org.freeplane.features.common.text.TextController.Direction;

class FindAction extends AFreeplaneAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ISelectableCondition condition;
	private FilterConditionEditor editor;
	private WeakReference<NodeModel> findRoot;

	public FindAction(final Controller controller) {
		super("FindAction", controller);
	}

	public void actionPerformed(final ActionEvent e) {
		final IMapSelection selection = getController().getSelection();
		if (selection == null) {
			return;
		}
		NodeModel start = selection.getSelected();
		findRoot = new WeakReference<NodeModel>(start);
		if (editor == null) {
			editor = new FilterConditionEditor(FilterController.getController(getController()));
		}
		else {
			editor.mapChanged(start.getMap());
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
		final int run = UITools.showConfirmDialog(getController(), start, editor, TextUtils
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
		findNext();
	}

	void findNext() {
		if (condition == null) {
			UITools.informationMessage(getController().getViewController().getFrame(), TextUtils.getText("no_previous_find"));
			return;
		}
		FilterController filterController = FilterController.getController(getController());
		NodeModel start = getController().getSelection().getSelected();
		NodeModel root = findRoot.get();
		if(root == null){
			root = start;
			findRoot = new WeakReference<NodeModel>(root);
		}
		else {
			for(NodeModel node = start; node != root; node = node.getParentNode()){
				if(node == null){
					root = start;
					findRoot = new WeakReference<NodeModel>(root);
					break;
				}
			}
		}
		NodeModel next = filterController.findNext(start, Direction.FORWARD, null);
		if (next == null) {
			displayNotFoundMessage(start);
			return;
		}
		for(NodeModel node = next.getParentNode(); node != root; node = node.getParentNode()){
			if(node == null){
				displayNotFoundMessage(root);
				return;
			}
		}
		getModeController().getMapController().select(next);
	}

	private void displayNotFoundMessage(NodeModel start) {
		final String messageText = TextUtils.getText("no_more_found_from");
		UITools.informationMessage(getController().getViewController().getFrame(), messageText.replaceAll("\\$1",
		    Matcher.quoteReplacement(condition.toString())).replaceAll("\\$2", Matcher.quoteReplacement(getFindFromText(start))));
	}
	public String getFindFromText(NodeModel node) {
		final String plainNodeText = HtmlUtils.htmlToPlain(node.toString()).replaceAll("\n", " ");
		return plainNodeText.length() <= 30 ? plainNodeText : plainNodeText.substring(0, 30) + "...";
	}

}
