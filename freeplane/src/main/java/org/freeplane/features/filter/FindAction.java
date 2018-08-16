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
package org.freeplane.features.filter;

import java.awt.Component;
import java.awt.Container;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;

import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.filter.condition.ASelectableCondition;
import org.freeplane.features.filter.condition.ICondition;
import org.freeplane.features.map.IMapSelection;
import org.freeplane.features.map.MapController.Direction;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;

class FindAction extends AFreeplaneAction {
	static final String KEY = "FindAction";
	private static final long serialVersionUID = 1L;
	private FilterConditionEditor editor;
	final private FindNextAction findNextAction;
	final private AFreeplaneAction findPreviousAction;

	public FindAction() {
		super(KEY);
		findNextAction = new FindNextAction();
		findPreviousAction = new FindPreviousAction();
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		final IMapSelection selection = Controller.getCurrentController().getSelection();
		if (selection == null) {
			return;
		}
		final NodeModel start = selection.getSelected();
		if (editor == null) {
			editor = new FilterConditionEditor(FilterController.getCurrentFilterController());
			editor.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(), BorderFactory.createEmptyBorder(5, 0, 5, 0)));

		}
		else {
			editor.mapChanged(start.getMap());
		}
		editor.addAncestorListener(new AncestorListener() {
			@Override
			public void ancestorAdded(final AncestorEvent event) {
				final Component component = event.getComponent();
				final Window windowAncestor = SwingUtilities.getWindowAncestor(component);
				if(windowAncestor.isFocused())
					editor.focusInputField(true);
				else{
					windowAncestor.addWindowFocusListener(new WindowFocusListener() {
						@Override
						public void windowLostFocus(WindowEvent e) {
						}

						@Override
						public void windowGainedFocus(WindowEvent e) {
							windowAncestor.removeWindowFocusListener(this);
							editor.focusInputField(true);
						}
					});
					windowAncestor.toFront();
				}
				editor.removeAncestorListener(this);
			}

			@Override
			public void ancestorMoved(final AncestorEvent event) {
			}

			@Override
			public void ancestorRemoved(final AncestorEvent event) {
			}
		});
		final int run = UITools.showConfirmDialog(start, editor, TextUtils.getText("FindAction.text"),
		    JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE);
		final Container parent = editor.getParent();
		if (parent != null) {
			parent.remove(editor);
		}
		if (run != JOptionPane.OK_OPTION) {
			return;
		}
		final ASelectableCondition condition = editor.getCondition();
		findFirst(condition);
	}

	void findFirst(final ASelectableCondition condition) {
	    final FoundNodes info = FoundNodes.get(Controller.getCurrentController().getMap());
		info.condition = condition;
		if (info.condition == null) {
			return;
		}
		info.rootID = Controller.getCurrentController().getSelection().getSelected().createID();
		findNext(Direction.FORWARD);
    }

	void findNext(Direction direction) {
		final MapModel map = Controller.getCurrentController().getMap();
		final FoundNodes info = FoundNodes.get(map);
		if (info.condition == null) {
			displayNoPreviousFindMessage();
			return;
		}
		final FilterController filterController = FilterController.getCurrentFilterController();
		final NodeModel start = Controller.getCurrentController().getSelection().getSelected();
		final NodeModel root = map.getNodeForID_(info.rootID);
		if (root == null) {
			info.condition = null;
			displayNoPreviousFindMessage();
			return;
		}
		final NodeModel next = filterController.findNext(start, null, direction, info.condition);
		if (next == null) {
			displayNotFoundMessage(root, info.condition);
			return;
		}
		info.displayFoundNode(next);
	}

	private void displayNoPreviousFindMessage() {
		UITools.informationMessage(Controller.getCurrentController().getViewController().getCurrentRootComponent(), TextUtils
		    .getText("no_previous_find"));
	}

	private void displayNotFoundMessage(final NodeModel start, final ICondition condition) {
		final String message = TextUtils.format("no_more_found_from", condition.toString(), getFindFromText(start));
		UITools.informationMessage(Controller.getCurrentController().getViewController().getCurrentRootComponent(), message);
	}

	public String getFindFromText(final NodeModel node) {
		final String plainNodeText = node.toString().replaceAll("\n", " ");
		return plainNodeText.length() <= 30 ? plainNodeText : plainNodeText.substring(0, 30) + "...";
	}

	public AFreeplaneAction getFindNextAction() {
		return findNextAction;
	}

	public AFreeplaneAction getFindPreviousAction() {
		return findPreviousAction;
	}

	@SuppressWarnings("serial")
	private class FindNextAction extends AFreeplaneAction{
		FindNextAction() {
			super("FindNextAction");
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			findNext(Direction.FORWARD);
		}
	}
	@SuppressWarnings("serial")
	private class FindPreviousAction extends AFreeplaneAction{
		FindPreviousAction() {
			super("FindPreviousAction");
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			findNext(Direction.BACK);
		}
	}
}
