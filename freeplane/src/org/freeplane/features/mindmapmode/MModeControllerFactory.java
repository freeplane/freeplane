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
package org.freeplane.features.mindmapmode;

import javax.swing.JPopupMenu;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.core.ui.components.FreeplaneToolBar;
import org.freeplane.core.url.UrlManager;
import org.freeplane.features.common.addins.encrypt.EnterPassword;
import org.freeplane.features.common.addins.misc.BlinkingNodeHook;
import org.freeplane.features.common.addins.misc.CreationModificationPlugin;
import org.freeplane.features.common.addins.misc.FitToPage;
import org.freeplane.features.common.addins.misc.HierarchicalIcons;
import org.freeplane.features.common.attribute.AttributeController;
import org.freeplane.features.common.clipboard.ClipboardController;
import org.freeplane.features.common.cloud.CloudController;
import org.freeplane.features.common.edge.EdgeController;
import org.freeplane.features.common.icon.IconController;
import org.freeplane.features.common.link.LinkController;
import org.freeplane.features.common.nodelocation.LocationController;
import org.freeplane.features.common.nodestyle.NodeStyleController;
import org.freeplane.features.common.note.NoteController;
import org.freeplane.features.common.text.TextController;
import org.freeplane.features.mindmapmode.addins.ChangeNodeLevelAction;
import org.freeplane.features.mindmapmode.addins.FormatPaste;
import org.freeplane.features.mindmapmode.addins.IconSelectionPlugin;
import org.freeplane.features.mindmapmode.addins.NewParentNode;
import org.freeplane.features.mindmapmode.addins.RevisionPlugin;
import org.freeplane.features.mindmapmode.addins.SaveAll;
import org.freeplane.features.mindmapmode.addins.SortNodes;
import org.freeplane.features.mindmapmode.addins.SplitNode;
import org.freeplane.features.mindmapmode.addins.UnfoldAll;
import org.freeplane.features.mindmapmode.addins.encrypt.EncryptedMap;
import org.freeplane.features.mindmapmode.addins.export.ExportToImage;
import org.freeplane.features.mindmapmode.addins.export.ExportToOoWriter;
import org.freeplane.features.mindmapmode.addins.export.ExportWithXSLT;
import org.freeplane.features.mindmapmode.addins.export.ImportMindmanagerFiles;
import org.freeplane.features.mindmapmode.addins.nodehistory.NodeHistory;
import org.freeplane.features.mindmapmode.addins.styles.ApplyFormatPlugin;
import org.freeplane.features.mindmapmode.addins.styles.AutomaticLayout;
import org.freeplane.features.mindmapmode.addins.styles.ManagePatterns;
import org.freeplane.features.mindmapmode.addins.time.ReminderHook;
import org.freeplane.features.mindmapmode.attribute.MAttributeController;
import org.freeplane.features.mindmapmode.clipboard.MClipboardController;
import org.freeplane.features.mindmapmode.cloud.MCloudController;
import org.freeplane.features.mindmapmode.edge.MEdgeController;
import org.freeplane.features.mindmapmode.file.MFileManager;
import org.freeplane.features.mindmapmode.icon.MIconController;
import org.freeplane.features.mindmapmode.link.MLinkController;
import org.freeplane.features.mindmapmode.nodelocation.MLocationController;
import org.freeplane.features.mindmapmode.nodestyle.MNodeStyleController;
import org.freeplane.features.mindmapmode.note.MNoteController;
import org.freeplane.features.mindmapmode.text.MTextController;
import org.freeplane.features.mindmapnode.pattern.MPatternController;
import org.freeplane.features.ui.UserInputListenerFactory;
/**
 * @author Dimitry Polivaev 24.11.2008
 */
public class MModeControllerFactory {
	private static MModeControllerFactory instance;

	public static MModeController createModeController() {
		return MModeControllerFactory.getInstance().createModeControllerImpl();
	}

	private static MModeControllerFactory getInstance() {
		if (instance == null) {
			instance = new MModeControllerFactory();
		}
		return instance;
	}

	private MModeController modeController;

	private void createAddIns() {
		new HierarchicalIcons(modeController);
		new AutomaticLayout(modeController);
		new BlinkingNodeHook(modeController);
		new CreationModificationPlugin(modeController);
		new ReminderHook(modeController);
		final MenuBuilder menuBuilder = modeController.getUserInputListenerFactory()
		    .getMenuBuilder();
		menuBuilder.addAnnotatedAction(new ApplyFormatPlugin());
		new FormatPaste(menuBuilder);
		menuBuilder.addAnnotatedAction(new FitToPage());
		menuBuilder.addAnnotatedAction(new EncryptedMap(modeController));
		menuBuilder.addAnnotatedAction(new EnterPassword(modeController));
		menuBuilder.addAnnotatedAction(new IconSelectionPlugin());
		menuBuilder.addAnnotatedAction(new ManagePatterns());
		menuBuilder.addAnnotatedAction(new NewParentNode());
		menuBuilder.addAnnotatedAction(new SaveAll());
		menuBuilder.addAnnotatedAction(new SortNodes());
		menuBuilder.addAnnotatedAction(new SplitNode());
		new UnfoldAll(modeController);
		new ChangeNodeLevelAction(menuBuilder);
		ExportWithXSLT.createXSLTExportActions(modeController,
		    "/org/freeplane/features/mindmapmode/addins/export/ExportWithXSLT.xml");
		ExportToImage.createActions(modeController);
		new NodeHistory(modeController);
		menuBuilder.addAnnotatedAction(new ExportToOoWriter());
		menuBuilder.addAnnotatedAction(new ImportMindmanagerFiles());
//		new LatexNodeHook(modeController);
//		new ScriptingRegistration(modeController);
//		menuBuilder.addAnnotatedAction(new FreeplaneHelpStarter());
//		menuBuilder.addAnnotatedAction(new ExportPdf());
//		menuBuilder.addAnnotatedAction(new ExportSvg());
	}

	private MModeController createModeControllerImpl() {
		if (modeController != null) {
			return modeController;
		}
		createStandardControllers();
		createAddIns();
		return modeController;
	}

	private void createStandardControllers() {
		modeController = new MModeController();
		final UserInputListenerFactory userInputListenerFactory = new UserInputListenerFactory(
		    modeController);
		modeController.setUserInputListenerFactory(userInputListenerFactory);
		Controller.getController().addModeController(modeController);
		modeController.setMapController(new MMapController(modeController));
		UrlManager.install(modeController, new MFileManager(modeController));
		IconController.install(modeController, new MIconController(modeController));
		NodeStyleController.install(modeController, new MNodeStyleController(modeController));
		EdgeController.install(modeController, new MEdgeController(modeController));
		CloudController.install(modeController, new MCloudController(modeController));
		NoteController.install(modeController, new MNoteController(modeController));
		LinkController.install(modeController, new MLinkController(modeController));
		MPatternController.install(modeController, new MPatternController(modeController));
		TextController.install(modeController, new MTextController(modeController));
		ClipboardController.install(modeController, new MClipboardController(modeController));
		LocationController.install(modeController, new MLocationController(modeController));
		AttributeController.install(modeController, new MAttributeController(modeController));
		final JPopupMenu popupmenu = new JPopupMenu();
		userInputListenerFactory.setNodePopupMenu(popupmenu);
		final FreeplaneToolBar toolbar = new FreeplaneToolBar();
		userInputListenerFactory.setMainToolBar(toolbar);
		userInputListenerFactory.setLeftToolBar(((MIconController) IconController
		    .getController(modeController)).getIconToolBarScrollPane());
		new RevisionPlugin(modeController);
		modeController.updateMenus("/org/freeplane/features/mindmapmode/menu.xml");
	}
}
