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
package org.freeplane.features.mindmapmode;

import java.awt.dnd.DropTargetListener;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.util.Vector;

import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.resources.PropertyAction;
import org.freeplane.core.resources.ui.OptionPanelBuilder;
import org.freeplane.core.resources.ui.OptionString;
import org.freeplane.core.ui.IMouseListener;
import org.freeplane.core.ui.IndexedTree;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.core.ui.components.FreemindMenuBar;
import org.freeplane.core.undo.IUndoHandler;
import org.freeplane.core.undo.IUndoableActor;
import org.freeplane.core.url.UrlManager;
import org.freeplane.features.common.icon.IconController;
import org.freeplane.features.common.link.LinkController;
import org.freeplane.features.common.link.NodeLinks;
import org.freeplane.features.common.note.NoteController;
import org.freeplane.features.common.text.TextController;
import org.freeplane.features.mindmapmode.file.MFileManager;
import org.freeplane.features.mindmapmode.icon.MIconController;
import org.freeplane.features.mindmapmode.note.MNoteController;
import org.freeplane.features.mindmapmode.text.MTextController;
import org.freeplane.features.mindmapnode.pattern.MPatternController;
import org.freeplane.features.ui.UserInputListenerFactory;
import org.freeplane.view.swing.map.MainView;

public class MModeController extends ModeController {
	static public final String MODENAME = "MindMap";
	static private RedoAction redo;
	static private UndoAction undo;

	static public MModeController getMModeController() {
		return (MModeController) Controller.getModeController();
	}

	private OptionPanelBuilder optionPanelBuilder;

	MModeController() {
		super();
		createActions();
		createOptionPanelControls();
	}

	private void addUndoableActor(final IUndoableActor actor) {
		final MMapModel map = (MMapModel) Controller.getController().getMap();
		final IUndoHandler undoHandler = map.getUndoHandler();
		undoHandler.addActor(actor);
		undo.setEnabled(true);
		redo.setEnabled(false);
	}

	private void createActions() {
		undo = new UndoAction();
		redo = new RedoAction();
		undo.setRedo(redo);
		redo.setUndo(undo);
		addAction("undo", undo);
		addAction("redo", redo);
		addAction("selectBranchAction", new SelectBranchAction());
		addAction("selectAllAction", new SelectAllAction());
	}

	private void createOptionPanelControls() {
		optionPanelBuilder = new OptionPanelBuilder();
		optionPanelBuilder.load(Controller.getResourceController().getResource(
		    "org/freeplane/modes/mindmapmode/preferences.xml"));
		final LookAndFeelInfo[] lafInfo = UIManager.getInstalledLookAndFeels();
		final Vector<String> lafNames = new Vector(lafInfo.length + 5);
		final Vector<String> translatedLafNames = new Vector(lafInfo.length + 5);
		lafNames.add("default");
		translatedLafNames.add(OptionString.getText("OptionPanel.default"));
		lafNames.add("metal");
		translatedLafNames.add(OptionString.getText("OptionPanel.metal"));
		lafNames.add("windows");
		translatedLafNames.add(OptionString.getText("OptionPanel.windows"));
		lafNames.add("motif");
		translatedLafNames.add(OptionString.getText("OptionPanel.motif"));
		lafNames.add("gtk");
		translatedLafNames.add(OptionString.getText("OptionPanel.gtk"));
		lafNames.add("nothing");
		translatedLafNames.add(OptionString.getText("OptionPanel.nothing"));
		for (int i = 0; i < lafInfo.length; i++) {
			final LookAndFeelInfo info = lafInfo[i];
			final String className = info.getClassName();
			lafNames.add(className);
			translatedLafNames.add(info.getName());
		}
		optionPanelBuilder.addComboProperty("Appearance/look_and_feel/lookandfeel", "lookandfeel",
		    lafNames, translatedLafNames, IndexedTree.AS_CHILD);
		addAction("propertyAction", new PropertyAction(optionPanelBuilder.getRoot()));
	}

	@Override
	public void doubleClick(final MouseEvent e) {
		/* perform action only if one selected node. */
		if (getMapController().getSelectedNodes().size() != 1) {
			return;
		}
		final NodeModel node = ((MainView) e.getComponent()).getNodeView().getModel();
		if (!e.isAltDown() && !e.isControlDown() && !e.isShiftDown() && !e.isPopupTrigger()
		        && e.getButton() == MouseEvent.BUTTON1 && (NodeLinks.getLink(node) == null)) {
			((MTextController) TextController.getController(this)).edit(null, false, false);
		}
	}

	@Override
	public void execute(final IUndoableActor actor) {
		actor.act();
		addUndoableActor(actor);
	}

	@Override
	public String getModeName() {
		return MModeController.MODENAME;
	}

	public OptionPanelBuilder getOptionPanelBuilder() {
		return optionPanelBuilder;
	}

	public boolean isUndoAction() {
		return ((MMapModel) Controller.getController().getMapView().getModel()).getUndoHandler()
		    .isUndoActionRunning();
	}

	@Override
	public void plainClick(final MouseEvent e) {
		/* perform action only if one selected node. */
		if (getMapController().getSelectedNodes().size() != 1) {
			return;
		}
		final MainView component = (MainView) e.getComponent();
		if (component.isInFollowLinkRegion(e.getX())) {
			LinkController.getController(this).loadURL();
		}
		else {
			final NodeModel node = (component).getNodeView().getModel();
			if (!node.getModeController().getMapController().hasChildren(node)) {
				doubleClick(e);
				return;
			}
			(getMapController()).toggleFolded();
		}
	}

	/**
	 *
	 */
	public boolean save() {
		return ((MFileManager) UrlManager.getController(this)).save(Controller.getController()
		    .getMapView().getModel());
	}

	public void setMapMouseMotionListener(final IMouseListener mapMouseMotionListener) {
		((UserInputListenerFactory) getUserInputListenerFactory())
		    .setMapMouseListener(mapMouseMotionListener);
	}

	public void setNodeDropTargetListener(final DropTargetListener nodeDropTargetListener) {
		((UserInputListenerFactory) getUserInputListenerFactory())
		    .setNodeDropTargetListener(nodeDropTargetListener);
	}

	public void setNodeKeyListener(final KeyListener nodeKeyListener) {
		((UserInputListenerFactory) getUserInputListenerFactory())
		    .setNodeKeyListener(nodeKeyListener);
	}

	public void setNodeMotionListener(final IMouseListener nodeMotionListener) {
		((UserInputListenerFactory) getUserInputListenerFactory())
		    .setNodeMotionListener(nodeMotionListener);
	}

	@Override
	public void shutdown() {
		super.shutdown();
		((MNoteController) NoteController.getController(this)).shutdownController();
	}

	/**
	 * This method is called after and before a change of the map mapView. Use
	 * it to perform the actions that cannot be performed at creation time.
	 */
	@Override
	public void startup() {
		super.startup();
		((MNoteController) NoteController.getController(this)).startupController();
	}

	public void undo() {
		undo.actionPerformed(null);
		redo.reset();
	}

	protected void updateMenus(final String resource) {
		final UserInputListenerFactory userInputListenerFactory = (UserInputListenerFactory) getUserInputListenerFactory();
		userInputListenerFactory.setMenuStructure(resource);
		userInputListenerFactory.updateMenus(this);
		final MenuBuilder builder = getUserInputListenerFactory().getMenuBuilder();
		((MIconController) IconController.getController(this)).updateIconToolbar();
		((MIconController) IconController.getController(this)).updateMenus(builder);
		MPatternController.getController(this).createPatternSubMenu(builder,
		    UserInputListenerFactory.NODE_POPUP);
		final String formatMenuString = FreemindMenuBar.FORMAT_MENU;
		MPatternController.getController(this).createPatternSubMenu(builder, formatMenuString);
		super.updateMenus();
	}
}
