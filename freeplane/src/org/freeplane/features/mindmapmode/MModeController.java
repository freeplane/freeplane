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

import java.util.Vector;

import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.model.MapModel;
import org.freeplane.core.resources.FpStringUtils;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.resources.ui.OptionPanelBuilder;
import org.freeplane.core.resources.ui.PropertyAction;
import org.freeplane.core.ui.IndexedTree;
import org.freeplane.core.undo.IActor;
import org.freeplane.core.undo.IUndoHandler;
import org.freeplane.core.url.UrlManager;
import org.freeplane.features.common.note.NoteController;
import org.freeplane.features.mindmapmode.file.MFileManager;
import org.freeplane.features.mindmapmode.note.MNoteController;

public class MModeController extends ModeController {
	static public final String MODENAME = "MindMap";
	static private RedoAction redo;
	public static final String RESOURCES_DELETE_NODES_WITHOUT_QUESTION = "delete_nodes_without_question";
	static private UndoAction undo;

	static public MModeController getMModeController(final Controller controller) {
		return (MModeController) controller.getModeController(MODENAME);
	}

	private OptionPanelBuilder optionPanelBuilder;

	public MModeController(final Controller controller) {
		super(controller);
		createActions();
		createOptionPanelControls();
	}

	private void addUndoableActor(final IActor actor, final MMapModel map) {
		final IUndoHandler undoHandler = map.getUndoHandler();
		undoHandler.addActor(actor);
	}

	public void deactivateUndo(final MMapModel map) {
		final IUndoHandler undoHandler = map.getUndoHandler();
		undoHandler.deactivate();
	}

	@Override
	public void commit() {
		final MMapModel map = (MMapModel) getController().getMap();
		final IUndoHandler undoHandler = map.getUndoHandler();
		undoHandler.commit();
	}

	public void delayedCommit() {
		final MMapModel map = (MMapModel) getController().getMap();
		final IUndoHandler undoHandler = map.getUndoHandler();
		undoHandler.delayedCommit();
	}

	public void delayedRollback() {
		final MMapModel map = (MMapModel) getController().getMap();
		final IUndoHandler undoHandler = map.getUndoHandler();
		undoHandler.delayedRollback();
	}

	private void createActions() {
		final Controller controller = getController();
		undo = new UndoAction(controller);
		redo = new RedoAction(controller);
		undo.setRedo(redo);
		redo.setUndo(undo);
		addAction(undo);
		addAction(redo);
		addAction(new SelectBranchAction(controller));
		addAction(new SelectAllAction(controller));
	}

	private void createOptionPanelControls() {
		optionPanelBuilder = new OptionPanelBuilder();
		optionPanelBuilder.load(ResourceController.getResourceController().getResource("/xml/preferences.xml"));
		final LookAndFeelInfo[] lafInfo = UIManager.getInstalledLookAndFeels();
		final Vector<String> lafNames = new Vector(lafInfo.length + 1);
		final Vector<String> translatedLafNames = new Vector(lafInfo.length + 1);
		lafNames.add("default");
		translatedLafNames.add(FpStringUtils.getOptionalText("OptionPanel.default"));
		for (int i = 0; i < lafInfo.length; i++) {
			final LookAndFeelInfo info = lafInfo[i];
			final String className = info.getClassName();
			lafNames.add(className);
			translatedLafNames.add(info.getName());
		}
		optionPanelBuilder.addComboProperty("Appearance/look_and_feel/lookandfeel", "lookandfeel", lafNames,
		    translatedLafNames, IndexedTree.AS_CHILD);
		addAction(new PropertyAction(getController(), optionPanelBuilder.getRoot()));
	}

	@Override
	public void execute(final IActor actor, final MapModel map) {
		actor.act();
		addUndoableActor(actor, (MMapModel) map);
	}

	@Override
	public String getModeName() {
		return MModeController.MODENAME;
	}

	public OptionPanelBuilder getOptionPanelBuilder() {
		return optionPanelBuilder;
	}

	@Override
	public boolean isUndoAction() {
		final MapModel model = getController().getMap();
		if (!(model instanceof MMapModel)) {
			return false;
		}
		return ((MMapModel) model).getUndoHandler().isUndoActionRunning();
	}

	@Override
	public void rollback() {
		final MMapModel map = (MMapModel) getController().getMap();
		final IUndoHandler undoHandler = map.getUndoHandler();
		undoHandler.rollback();
	}

	/**
	 *
	 */
	public boolean save() {
		return ((MFileManager) UrlManager.getController(this)).save(getController().getMap());
	}

	@Override
	public void shutdown() {
		super.shutdown();
		((MNoteController) NoteController.getController(this)).shutdownController();
	}

	@Override
	public void startTransaction() {
		final MMapModel map = (MMapModel) getController().getMap();
		final IUndoHandler undoHandler = map.getUndoHandler();
		undoHandler.startTransaction();
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

	@Override
	public boolean canEdit() {
		return true;
	}
}
