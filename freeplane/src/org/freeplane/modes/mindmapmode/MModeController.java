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
package org.freeplane.modes.mindmapmode;

import java.awt.event.MouseEvent;
import java.util.Vector;

import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import org.freeplane.controller.Controller;
import org.freeplane.controller.resources.PropertyAction;
import org.freeplane.controller.resources.ui.OptionPanelBuilder;
import org.freeplane.controller.resources.ui.OptionString;
import org.freeplane.map.attribute.IAttributeController;
import org.freeplane.map.attribute.mindmapnode.MAttributeController;
import org.freeplane.map.icon.mindmapnode.MIconController;
import org.freeplane.map.nodelocation.mindmapmode.MLocationController;
import org.freeplane.map.note.mindmapnode.MNoteController;
import org.freeplane.map.pattern.mindmapnode.MPatternController;
import org.freeplane.map.text.mindmapmode.MTextController;
import org.freeplane.map.tree.NodeModel;
import org.freeplane.map.tree.mindmapmode.MMapController;
import org.freeplane.map.tree.mindmapmode.MindMapMapModel;
import org.freeplane.map.tree.view.MainView;
import org.freeplane.map.url.mindmapmode.FileManager;
import org.freeplane.modes.ModeController;
import org.freeplane.modes.UserInputListenerFactory;
import org.freeplane.ui.FreemindMenuBar;
import org.freeplane.ui.IndexedTree;
import org.freeplane.ui.MenuBuilder;
import org.freeplane.undo.IUndoHandler;
import org.freeplane.undo.IUndoableActor;

public class MModeController extends ModeController {
	static public final String MODENAME = "MindMap";
	static private RedoAction redo;
	static private UndoAction undo;
	private MAttributeController attributeController;
	private OptionPanelBuilder optionPanelBuilder;
	private MPatternController patternController;

	MModeController() {
		super();
		createActions();
		createOptionPanelControls();
	}

	private void addUndoableActor(final IUndoableActor actor) {
		final MindMapMapModel map = (MindMapMapModel) Controller.getController().getMap();
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
		if (getSelectedNodes().size() != 1) {
			return;
		}
		final NodeModel node = ((MainView) e.getComponent()).getNodeView().getModel();
		if (!e.isAltDown() && !e.isControlDown() && !e.isShiftDown() && !e.isPopupTrigger()
		        && e.getButton() == MouseEvent.BUTTON1 && (node.getLink() == null)) {
			((MTextController) getTextController()).edit(null, false, false);
		}
	}

	@Override
	public void execute(final IUndoableActor actor) {
		actor.act();
		addUndoableActor(actor);
	}

	@Override
	public IAttributeController getAttributeController() {
		return attributeController;
	}

	@Override
	public String getModeName() {
		return MModeController.MODENAME;
	}

	public OptionPanelBuilder getOptionPanelBuilder() {
		return optionPanelBuilder;
	}

	public MPatternController getPatternController() {
		return patternController;
	}

	public boolean isUndoAction() {
		return ((MindMapMapModel) getMapView().getModel()).getUndoHandler().isUndoActionRunning();
	}

	/**
	 * @param node
	 * @param gap
	 * @param hgap
	 * @param i
	 */
	public void moveNodePosition(final NodeModel node, final int gap, final int hgap, final int i) {
		((MLocationController) getLocationController()).moveNodePosition(node, gap, hgap, i);
	}

	@Override
	public void onUpdate(final NodeModel node, final Object property, final Object oldValue,
	                     final Object newValue) {
		super.onUpdate(node, property, oldValue, newValue);
	}

	@Override
	public void plainClick(final MouseEvent e) {
		/* perform action only if one selected node. */
		if (getSelectedNodes().size() != 1) {
			return;
		}
		final MainView component = (MainView) e.getComponent();
		if (component.isInFollowLinkRegion(e.getX())) {
			getLinkController().loadURL();
		}
		else {
			final NodeModel node = (component).getNodeView().getModel();
			if (!node.getModeController().getMapController().hasChildren(node)) {
				doubleClick(e);
				return;
			}
			((MMapController) getMapController()).toggleFolded();
		}
	}

	/**
	 *
	 */
	public boolean save() {
		return ((FileManager) getUrlManager()).save(getMapView().getModel());
	}

	void setAttributeController(final MAttributeController attributeController) {
		this.attributeController = attributeController;
	}

	void setPatternController(final MPatternController patternController) {
		this.patternController = patternController;
	}

	@Override
	public void shutdown() {
		super.shutdown();
		((MNoteController) getNoteController()).shutdownController();
	}

	/**
	 * This method is called after and before a change of the map mapView. Use
	 * it to perform the actions that cannot be performed at creation time.
	 */
	@Override
	public void startup() {
		super.startup();
		((MNoteController) getNoteController()).startupController();
	}

	public void undo() {
		undo.actionPerformed(null);
		redo.reset();
	}

	/**
	 */
	@Override
	public void updateMenus(final MenuBuilder builder) {
		((MIconController) getIconController()).updateIconToolbar();
		((MIconController) getIconController()).updateMenus(builder);
		getPatternController().createPatternSubMenu(builder, UserInputListenerFactory.NODE_POPUP);
		final String formatMenuString = FreemindMenuBar.FORMAT_MENU;
		getPatternController().createPatternSubMenu(builder, formatMenuString);
	}

	@Override
	protected void updateMenus(final String resource) {
		super.updateMenus(resource);
	}
}
