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
package org.freeplane.features.mode.mindmapmode;

import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Vector;

import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.resources.components.OptionPanelBuilder;
import org.freeplane.core.resources.components.ShowPreferencesAction;
import org.freeplane.core.ui.IndexedTree;
import org.freeplane.core.ui.menubuilders.generic.UserRole;
import org.freeplane.core.undo.IActor;
import org.freeplane.core.undo.IUndoHandler;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.commandsearch.PreferencesItem;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.mindmapmode.MMapModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.note.NoteController;
import org.freeplane.features.note.mindmapmode.MNoteController;
import org.freeplane.features.url.UrlManager;
import org.freeplane.features.url.mindmapmode.MFileManager;

import com.bulenkov.darcula.DarculaLaf;

public class MModeController extends ModeController {
	private static final String LOOKANDFEEL_PROPERTY = "lookandfeel";
    static public final String MODENAME = "MindMap";
	private RedoAction redo;
	public static final String RESOURCES_DELETE_NODES_WITHOUT_QUESTION = "delete_nodes_without_question";
	private UndoAction undo;

	static public MModeController getMModeController() {
		Controller controller = Controller.getCurrentController();
		return (MModeController) controller.getModeController(MODENAME);
	}

	private OptionPanelBuilder optionPanelBuilder;

	public MModeController(final Controller controller) {
		super(controller);
		createActions();
		createOptionPanelControls();
	}

	private void addUndoableActor(final IActor actor, final MapModel map) {
		final IUndoHandler undoHandler = map.getExtension(IUndoHandler.class);
		if(undoHandler != null)
			undoHandler.addActor(actor);
	}

	public void deactivateUndo(final MMapModel map) {
		final IUndoHandler undoHandler = map.getExtension(IUndoHandler.class);
		if(undoHandler != null)
			undoHandler.deactivate();
	}

	@Override
	public void commit() {
		final MapModel map = getController().getMap();
		final IUndoHandler undoHandler = map.getExtension(IUndoHandler.class);
		if(undoHandler != null)
			undoHandler.commit();
	}

	public void delayedCommit() {
		final MMapModel map = (MMapModel) getController().getMap();
		final IUndoHandler undoHandler = map.getExtension(IUndoHandler.class);
		if(undoHandler != null)
			undoHandler.delayedCommit();
	}

	public void delayedRollback() {
		final MMapModel map = (MMapModel) getController().getMap();
		final IUndoHandler undoHandler = map.getExtension(IUndoHandler.class);
		if(undoHandler != null)
			undoHandler.delayedRollback();
	}

	private void createActions() {
		undo = new UndoAction();
		redo = new RedoAction();
		undo.setRedo(redo);
		redo.setUndo(undo);
		addAction(undo);
		addAction(redo);
		addAction(new SelectBranchAction());
		addAction(new SelectAllAction());
		addAction(new SaveAcceleratorPresetsAction());
	}

	private void createOptionPanelControls() {
		optionPanelBuilder = new OptionPanelBuilder();
		final ResourceController resourceController = ResourceController.getResourceController();
		URL preferences = resourceController.getResource("/xml/preferences.xml");
		optionPanelBuilder.load(preferences);
		getController().addAction(createShowPreferencesAction(optionPanelBuilder));
	}

	public static ShowPreferencesAction createShowPreferencesAction(OptionPanelBuilder optionPanelBuilder)
	{
		return createShowPreferencesAction(optionPanelBuilder, null);
	}

	public static ShowPreferencesAction createShowPreferencesAction(OptionPanelBuilder optionPanelBuilder,
																	PreferencesItem preferencesItem) {
	    final LookAndFeelInfo[] lafInfo = UIManager.getInstalledLookAndFeels();
		final Vector<String> lafNames = new Vector<String>(lafInfo.length + 2);
		final Vector<String> translatedLafNames = new Vector<String>(lafInfo.length + 2);
		lafNames.add("default");
		translatedLafNames.add(TextUtils.getOptionalText("OptionPanel.default"));
		for (int i = 0; i < lafInfo.length; i++) {
			final LookAndFeelInfo info = lafInfo[i];
			final String className = info.getClassName();
			lafNames.add(className);
			translatedLafNames.add(info.getName());
		}
		lafNames.add(DarculaLaf.class.getName());
		translatedLafNames.add("Darcula");
		addCurrentLookAndFeelIfNecessary(lafNames, translatedLafNames);
		optionPanelBuilder.addEditableComboProperty("Appearance/look_and_feel/lookandfeel", LOOKANDFEEL_PROPERTY, lafNames,
		    translatedLafNames, IndexedTree.AS_CHILD);
		return new ShowPreferencesAction(optionPanelBuilder.getRoot(), preferencesItem);
    }

    private static void addCurrentLookAndFeelIfNecessary(Vector<String> lafNames, Vector<String> translatedLafNames) {
        final String currentLaf = ResourceController.getResourceController().getProperty(LOOKANDFEEL_PROPERTY);
        if (!lafNames.contains(currentLaf)) {
            lafNames.add(currentLaf);
            translatedLafNames.add(currentLaf.replaceFirst(".*\\.", ""));
        }
    }

	@Override
	public void execute(final IActor actor, final MapModel map) {
		if(actor.isReadonly() || canEdit(map)) {
			try {
				Controller.getCurrentController().getViewController().invokeAndWait(() -> {
					addUndoableActor(actor, map);
					actor.act();
				});
			} catch (InvocationTargetException | InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	public String getModeName() {
		return MModeController.MODENAME;
	}

	public OptionPanelBuilder getOptionPanelBuilder() {
		return optionPanelBuilder;
	}

	@Override
	public void rollback() {
		final MapModel map = getController().getMap();
		final IUndoHandler undoHandler = map.getExtension(IUndoHandler.class);
		if(undoHandler != null)
			undoHandler.rollback();
	}

	/**
	 *
	 */
	public boolean save() {
		return ((MFileManager) UrlManager.getController()).save(getController().getMap());
	}

	@Override
	public void shutdown() {
		super.shutdown();
		final MNoteController noteController = (MNoteController) NoteController.getController();
		if (noteController != null) {
			noteController.shutdownController();
		}
	}

	@Override
	public void startTransaction() {
		final MapModel map = getController().getMap();
		final IUndoHandler undoHandler = map.getExtension(IUndoHandler.class);
		if(undoHandler != null)
			undoHandler.startTransaction();
	}

	@Override
	public void forceNewTransaction() {
		final MapModel map = getController().getMap();
		final IUndoHandler undoHandler = map.getExtension(IUndoHandler.class);
		if(undoHandler != null)
			undoHandler.forceNewTransaction();
    }

	/**
	 * This method is called after and before a change of the map mapView. Use
	 * it to perform the actions that cannot be performed at creation time.
	 */
	@Override
	public void startup() {
		super.startup();
		final NoteController noteController = NoteController.getController();
		if (noteController != null) {
			((MNoteController) noteController).startupController();
		}
	}

	public void undo() {
		undo.actionPerformed(null);
	}

	public void resetRedo() {
		redo.reset();
	}

	@Override
	public boolean canEdit() {
		return true;
	}

	public UserRole userRole(MapModel map) {
		return UserRole.of(canEdit(map));
	}
	
	@Override
	public boolean supportsHookActions() {
		return true;
	}

	@Override
	public boolean shouldCenterCompactMaps() {
		return ResourceController.getResourceController().getBooleanProperty("shouldCenterSmallMaps");
	}
}
