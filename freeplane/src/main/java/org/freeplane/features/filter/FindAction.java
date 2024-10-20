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
import java.awt.GridBagConstraints;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;

import javax.swing.AbstractButton;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.KeystrokeDescriber;
import org.freeplane.core.ui.components.FreeplaneToolBar;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.ui.textchanger.TranslatedElementFactory;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.filter.FilterConditionEditor.Variant;
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
	private NodeModel searchStart;
	private NodeModel subtreeRoot;
    private boolean searchesInSubtree;

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
		KeyStroke keyStrokePrevious = KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.CTRL_DOWN_MASK);
		KeyStroke keyStrokeNext = KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.CTRL_DOWN_MASK);
		final NodeModel start = selection.getSelected();
		if (editor == null) {
			editor = new FilterConditionEditor(FilterController.getCurrentFilterController(), 5, Variant.SEARCH_DIALOG, new JPanel());
			editor.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(), BorderFactory.createEmptyBorder(5, 0, 5, 0)));
			JComponent editorPanel = editor.getPanel();
			final AbstractButton applyFindPreviousBtn = FreeplaneToolBar.createButton(findPreviousAction);
			applyFindPreviousBtn.setToolTipText(KeystrokeDescriber.createKeystrokeDescription(keyStrokePrevious));
			final AbstractButton applyFindNextBtn = FreeplaneToolBar.createButton(findNextAction);
			editor.setEnterKeyActionListener(findNextAction);
			applyFindNextBtn.setToolTipText(KeystrokeDescriber.createKeystrokeDescription(keyStrokeNext));
			GridBagConstraints constraints = new GridBagConstraints();
			constraints.anchor = GridBagConstraints.NORTHWEST;
			constraints.gridwidth = 1;
			constraints.gridheight = 1;
			constraints.gridy = 0;
			editorPanel.add(applyFindPreviousBtn, constraints);
			editorPanel.add(applyFindNextBtn, constraints);
			constraints.anchor = GridBagConstraints.EAST;
			constraints.gridy = 1;
			constraints.gridwidth = 2;
			JCheckBox searchInSubtreeCheckBox = TranslatedElementFactory.createCheckBox("searchInSubtree");
			searchesInSubtree = ResourceController.getResourceController().getBooleanProperty("searchInSubtree");
            searchInSubtreeCheckBox.setSelected(searchesInSubtree);
			searchInSubtreeCheckBox.addActionListener(x ->
			    ResourceController.getResourceController().setProperty("searchInSubtree", searchesInSubtree = searchInSubtreeCheckBox.isSelected()));
            editorPanel.add(searchInSubtreeCheckBox, constraints);
		}
		else {
			editor.filterChanged(selection.getFilter());
		}
		JComponent editorPanel = editor.getPanel();
		InputMap inputMap = editorPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		ActionMap actionMap = editorPanel.getActionMap();
		inputMap.put(keyStrokeNext, findNextAction);
		actionMap.put(findNextAction, findNextAction);
		inputMap.put(keyStrokePrevious, findPreviousAction);
		actionMap.put(findPreviousAction, findPreviousAction);
		editorPanel.addAncestorListener(new AncestorListener() {
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
				editorPanel.removeAncestorListener(this);
			}

			@Override
			public void ancestorMoved(final AncestorEvent event) {
			}

			@Override
			public void ancestorRemoved(final AncestorEvent event) {
			}
		});
		searchStart = selection.getSelected();
		subtreeRoot = selection.getSelectionRoot();
		UITools.showConfirmDialog(start, editorPanel, TextUtils.getText("FindAction.text"),
		    JOptionPane.DEFAULT_OPTION,JOptionPane.PLAIN_MESSAGE);
		searchStart = subtreeRoot = null;
		final Container parent = editorPanel.getParent();
		if (parent != null) {
			parent.remove(editorPanel);
		}
	}

	void findNext(Direction direction) {
	    if (editor == null) {
	        return;
	    }
		Controller controller = Controller.getCurrentController();
		final FilterController filterController = FilterController.getCurrentFilterController();
		final NodeModel start = controller.getSelection().getSelected();
		Filter filter = controller.getSelection().getFilter();
		ASelectableCondition condition = editor.getCondition();
		if (condition == null) {
			return;
		}
		final NodeModel next = filterController.findNextInSubtree(start, searchesInSubtree ? searchStart : subtreeRoot, direction, condition, filter);
		if (next == null || next == start) {
		    if(searchStart != null)
		        displayNotFoundMessage(searchStart, condition);
			return;
		}
	     final MapModel map = controller.getMap();
	     final FoundNodes info = FoundNodes.get(map);
		info.displayFoundNode(next);
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
