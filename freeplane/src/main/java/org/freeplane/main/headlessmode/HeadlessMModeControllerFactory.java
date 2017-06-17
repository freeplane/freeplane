/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Dimitry Polivaev
 *
 *  This file author is Dimitry Polivaev
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
package org.freeplane.main.headlessmode;

import org.freeplane.features.attribute.AttributeController;
import org.freeplane.features.attribute.mindmapmode.MAttributeController;
import org.freeplane.features.clipboard.ClipboardController;
import org.freeplane.features.clipboard.mindmapmode.MClipboardController;
import org.freeplane.features.cloud.CloudController;
import org.freeplane.features.cloud.mindmapmode.MCloudController;
import org.freeplane.features.edge.AutomaticEdgeColorHook;
import org.freeplane.features.edge.EdgeController;
import org.freeplane.features.edge.mindmapmode.MEdgeController;
import org.freeplane.features.encrypt.mindmapmode.MEncryptionController;
import org.freeplane.features.export.mindmapmode.ExportController;
import org.freeplane.features.export.mindmapmode.ImportMindmanagerFiles;
import org.freeplane.features.icon.HierarchicalIcons;
import org.freeplane.features.icon.mindmapmode.MIconController;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.link.mindmapmode.MLinkController;
import org.freeplane.features.map.AlwaysUnfoldedNode;
import org.freeplane.features.map.FoldingController;
import org.freeplane.features.map.FreeNode;
import org.freeplane.features.map.SummaryNode;
import org.freeplane.features.map.mindmapmode.ChangeNodeLevelController;
import org.freeplane.features.map.mindmapmode.MMapController;
import org.freeplane.features.mapio.mindmapmode.MMapIO;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.features.nodelocation.LocationController;
import org.freeplane.features.nodelocation.mindmapmode.MLocationController;
import org.freeplane.features.nodestyle.NodeStyleController;
import org.freeplane.features.nodestyle.mindmapmode.MNodeStyleController;
import org.freeplane.features.nodestyle.mindmapmode.RevisionPlugin;
import org.freeplane.features.note.NoteController;
import org.freeplane.features.note.mindmapmode.MNoteController;
import org.freeplane.features.spellchecker.mindmapmode.SpellCheckerController;
import org.freeplane.features.styles.AutomaticLayoutController;
import org.freeplane.features.styles.LogicalStyleController;
import org.freeplane.features.styles.MapStyle;
import org.freeplane.features.styles.mindmapmode.MLogicalStyleController;
import org.freeplane.features.text.mindmapmode.MTextController;
import org.freeplane.features.time.CreationModificationPlugin;
import org.freeplane.features.url.UrlManager;
import org.freeplane.features.url.mindmapmode.MFileManager;
import org.freeplane.view.swing.features.BlinkingNodeHook;
import org.freeplane.view.swing.features.filepreview.ViewerController;
import org.freeplane.view.swing.features.nodehistory.NodeHistory;
import org.freeplane.view.swing.features.progress.mindmapmode.ProgressFactory;
import org.freeplane.view.swing.map.attribute.EditAttributesAction;
import org.freeplane.view.swing.ui.UserInputListenerFactory;

/**
 * @author Dimitry Polivaev 24.11.2008
 */
public class HeadlessMModeControllerFactory {
	private static HeadlessMModeControllerFactory instance;

	public static MModeController createModeController() {
		return HeadlessMModeControllerFactory.getInstance().createModeControllerImpl();
	}

	private static HeadlessMModeControllerFactory getInstance() {
		if (instance == null) {
			instance = new HeadlessMModeControllerFactory();
		}
		return instance;
	}

// // 	private Controller controller;
 	private MModeController modeController;

	private void createAddIns() {
		new HierarchicalIcons();
		new AutomaticLayoutController();
		new BlinkingNodeHook();
		SummaryNode.install();
		new AlwaysUnfoldedNode();
		FreeNode.install();
		new CreationModificationPlugin();
		new AutomaticEdgeColorHook();
		new ViewerController();
		MEncryptionController.install(new MEncryptionController(modeController));
		new ChangeNodeLevelController(modeController);
		NodeHistory.install(modeController);
		modeController.addAction(new ImportMindmanagerFiles());
	}

	private MModeController createModeControllerImpl() {
//		this.controller = controller;
		createStandardControllers();
		createAddIns();
		return modeController;
	}

	private void createStandardControllers() {
		final Controller controller = Controller.getCurrentController();
		modeController = new MModeController(controller);
		modeController.setUserInputListenerFactory(new UserInputListenerFactory(modeController));
		controller.addModeController(modeController);
		controller.selectModeForBuild(modeController);
		new MMapController(modeController);
		final MFileManager fileManager = new MFileManager();
		UrlManager.install(fileManager);
		MMapIO.install(modeController);
		controller.getMapViewManager().addMapViewChangeListener(fileManager);
		new MIconController(modeController).install(modeController);
		new ProgressFactory().installActions(modeController);
		EdgeController.install(new MEdgeController(modeController));
		CloudController.install(new MCloudController(modeController));
		NoteController.install(new MNoteController(modeController));
		new MTextController(modeController).install(modeController);
		LinkController.install(new MLinkController(modeController));
		NodeStyleController.install(new MNodeStyleController(modeController));
		ClipboardController.install(new MClipboardController());
		LocationController.install(new MLocationController());
		final MLogicalStyleController logicalStyleController = new MLogicalStyleController(modeController);
		LogicalStyleController.install(logicalStyleController);
		logicalStyleController.initM();
		AttributeController.install(new MAttributeController(modeController));
		modeController.addAction(new EditAttributesAction());
		SpellCheckerController.install(modeController);
		ExportController.install(new ExportController("/xml/ExportWithXSLT.xml"));
		MapStyle.install(true);
		new RevisionPlugin();
		FoldingController.install(new FoldingController());
	}
}
