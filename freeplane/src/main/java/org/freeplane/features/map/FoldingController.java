/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
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
package org.freeplane.features.map;

import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JPopupMenu;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.AMultipleNodeAction;
import org.freeplane.core.ui.IMouseWheelEventHandler;
import org.freeplane.core.ui.IUserInputListenerFactory;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;

/**
 * @author foltin
 */
public class FoldingController implements IMouseWheelEventHandler, IExtension {

	@SuppressWarnings("serial")
	private class FoldAllAction extends AMultipleNodeAction {


		public FoldAllAction() {
			super("FoldAllAction");
		}

		@Override
		public void actionPerformed(final ActionEvent e, final NodeModel node) {
			foldAll(node);
		}
	}

	@SuppressWarnings("serial")
	private class FoldOneLevelAction extends AMultipleNodeAction {


		public FoldOneLevelAction() {
			super("FoldOneLevelAction");
		}

		@Override
		public void actionPerformed(final ActionEvent e, final NodeModel node) {
			foldOneStage(node);
		}
	}

	@SuppressWarnings("serial")
	private class UnfoldAllAction extends AMultipleNodeAction {


		public UnfoldAllAction() {
			super("UnfoldAllAction");
		}

		@Override
		public void actionPerformed(final ActionEvent e, final NodeModel node) {
			unfoldAll(node);
		}
	}

	@SuppressWarnings("serial")
	private class UnfoldOneLevelAction extends AMultipleNodeAction {

		public UnfoldOneLevelAction() {
			super("UnfoldOneLevelAction");
		}

		@Override
		public void actionPerformed(final ActionEvent e, final NodeModel node) {
			unfoldOneStage(node);
		}
	}

	protected static Insets nullInsets = new Insets(0, 0, 0, 0);

	@SuppressWarnings("serial")
	private class FoldingPopupMenu extends JPopupMenu{
		final private NodeModel node;
		FoldingPopupMenu(NodeModel node){
			this.node = node;
			addAction(new UnfoldOneLevelPopupAction());
			addAction(new FoldOneLevelPopupAction());
			addAction(new UnfoldAllPopupAction());
			addAction(new FoldAllPopupAction());
		}

        private JButton addAction(Action a) {
	        final JButton menuItem = new JButton(a);
	        menuItem.setToolTipText(menuItem.getText());
	        menuItem.setText(null);
	        add(menuItem);
	        menuItem.setMargin(nullInsets);
			return menuItem;
        }

		@SuppressWarnings("serial")
		private class FoldAllPopupAction extends FoldAllAction{
			@Override
			public void actionPerformed(final ActionEvent e){
				actionPerformed(e, node);
			}
		}

		@SuppressWarnings("serial")
		private class FoldOneLevelPopupAction extends FoldOneLevelAction{
			@Override
			public void actionPerformed(final ActionEvent e){
				actionPerformed(e, node);
			}
		}

		@SuppressWarnings("serial")
		private class UnfoldAllPopupAction extends UnfoldAllAction{
			@Override
			public void actionPerformed(final ActionEvent e){
				actionPerformed(e, node);
			}
		}

		@SuppressWarnings("serial")
		private class UnfoldOneLevelPopupAction extends UnfoldOneLevelAction{
			@Override
			public void actionPerformed(final ActionEvent e){
				actionPerformed(e, node);
		}
	}
	}



// // 	final private Controller controller;

	public static void install( final FoldingController foldingController) {
		Controller.getCurrentModeController().addExtension(FoldingController.class, foldingController);
	}
	public FoldingController() {
		super();
		if(!GraphicsEnvironment.isHeadless()){
			final ModeController modeController = Controller.getCurrentModeController();
			final IUserInputListenerFactory userInputListenerFactory = modeController.getUserInputListenerFactory();
			userInputListenerFactory.addMouseWheelEventHandler(this);
			for (final AFreeplaneAction annotatedAction : getAnnotatedActions()) {
				modeController.addAction(annotatedAction);
			}
		}
	}

	private List<AMultipleNodeAction> getAnnotatedActions() {
		final ArrayList<AMultipleNodeAction> result = new ArrayList<AMultipleNodeAction>();
		result.add(new UnfoldAllAction());
		result.add(new FoldAllAction());
		result.add(new UnfoldOneLevelAction());
		result.add(new FoldOneLevelAction());
		return result;
	}

	protected void foldAll(final NodeModel node) {
		setFolded(node, true);
		for (NodeModel child : node.getChildren()) {
			foldAll(child);
		}
	}

	/**
	 * Unfolds every node that has only children which themselves have children.
	 * As this function is a bit difficult to describe and perhaps not so
	 * useful, it is currently not introduced into the menus.
	 *
	 * @param node
	 *            node to start from.
	 */
	public void foldLastBranches(final NodeModel node) {
		boolean nodeHasChildWhichIsLeave = false;
		for (final NodeModel child : node.getChildren()) {
			if (child.getChildCount() == 0) {
				nodeHasChildWhichIsLeave = true;
			}
		}
		setFolded(node, nodeHasChildWhichIsLeave);
		for (final NodeModel child : node.getChildren()) {
			foldLastBranches(child);
		}
	}

	protected void foldOneStage(final NodeModel node) {
		foldStageN(node, getMaxDepth(node) - 1);
	}

	public void foldStageN(final NodeModel node, final int stage) {
		final int k = depth(node);
		if (k < stage) {
			setFolded(node, false);
			for (final NodeModel child : node.getChildren()) {
				foldStageN(child, stage);
			}
		}
		else {
			foldAll(node);
		}
	}

	protected int getMaxDepth(final NodeModel node) {
		final MapController mapController = Controller.getCurrentModeController().getMapController();
		if (mapController.isFolded(node) || !node.hasChildren()) {
			return depth(node);
		}
		int k = 0;
		for (final NodeModel child : node.getChildren()) {
			final int l = getMaxDepth(child);
			if (l > k) {
				k = l;
			}
		}
		return k;
	}

	private int getMinDepth(final NodeModel node) {
		final EncryptionModel encryptionModel = EncryptionModel.getModel(node);
		if (encryptionModel != null && !encryptionModel.isAccessible() ) {
			return Integer.MAX_VALUE;
		}
		final MapController mapController = Controller.getCurrentModeController().getMapController();
		if (mapController.isFolded(node)) {
			return depth(node);
		}
		if (!node.hasChildren()||AlwaysUnfoldedNode.isAlwaysUnfolded(node)) {
			return Integer.MAX_VALUE;
		}
		int k = Integer.MAX_VALUE;
		for (final NodeModel child : node.getChildren()) {
			final int l = getMinDepth(child);
			if (l < k) {
				k = l;
			}
		}
		return k;
	}

	public boolean handleMouseWheelEvent(final MouseWheelEvent e) {
		if ((e.getModifiers() & InputEvent.ALT_MASK) != 0) {
			Controller controller = Controller.getCurrentController();
			final IMapSelection selection = controller.getSelection();
			final NodeModel node = selection.getSelected();
			if (e.getWheelRotation() > 0) {
				unfoldOneStage(node);
			}
			else {
				foldOneStage(node);
			}
			return true;
		}
		return false;
	}

	private void setFolded(final NodeModel node, final boolean state) {
		if (! node.isRoot()) {
			final MapController mapController = Controller.getCurrentModeController().getMapController();
			mapController.setFolded(node, state);
		}
	}

	public void unfoldAll(final NodeModel node) {
		setFolded(node, false);
		for (final NodeModel child : node.getChildren()) {
			unfoldAll(child);
		}
	}

	protected void unfoldOneStage(final NodeModel node) {
		int minDepth = getMinDepth(node);
		if (minDepth < Integer.MAX_VALUE) {
			minDepth++;
		}
		unfoldStageN(node, minDepth);
	}

	public void unfoldStageN(final NodeModel node, final int stage) {
		final int k = depth(node);
		if (k < stage) {
			setFolded(node, false);
			for (final NodeModel child : node.getChildren()) {
				unfoldStageN(child, stage);
			}
		}
		else {
			foldAll(node);
		}
	}

	private int depth(NodeModel node) {
		if (node.isRoot())
			return 0;
		final int parentDepth = depth(node.getParentNode());
		if (! node.hasVisibleContent() || AlwaysUnfoldedNode.isAlwaysUnfolded(node)) {
			return parentDepth;
		}
		else
			return parentDepth + 1;
	}

	public JPopupMenu createFoldingPopupMenu(NodeModel node){
		return new FoldingPopupMenu(node);
	}
}
