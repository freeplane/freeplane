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
package org.freeplane.modes.mindmapmode;

import javax.swing.JPopupMenu;
import org.freeplane.addins.encrypt.EnterPassword;
import org.freeplane.addins.encrypt.mindmapnode.EncryptedMap;
import org.freeplane.addins.mindmapmode.ChangeNodeLevelAction;
import org.freeplane.addins.mindmapmode.FormatPaste;
import org.freeplane.addins.mindmapmode.IconSelectionPlugin;
import org.freeplane.addins.mindmapmode.NewParentNode;
import org.freeplane.addins.mindmapmode.RevisionPlugin;
import org.freeplane.addins.mindmapmode.SaveAll;
import org.freeplane.addins.mindmapmode.SortNodes;
import org.freeplane.addins.mindmapmode.SplitNode;
import org.freeplane.addins.mindmapmode.UnfoldAll;
import org.freeplane.addins.mindmapmode.export.ExportToImage;
import org.freeplane.addins.mindmapmode.export.ExportToOoWriter;
import org.freeplane.addins.mindmapmode.export.ExportWithXSLT;
import org.freeplane.addins.mindmapmode.export.ImportMindmanagerFiles;
import org.freeplane.addins.mindmapmode.nodehistory.NodeHistory;
import org.freeplane.addins.mindmapmode.styles.ApplyFormatPlugin;
import org.freeplane.addins.mindmapmode.styles.AutomaticLayout;
import org.freeplane.addins.mindmapmode.styles.ManagePatterns;
import org.freeplane.addins.mindmapmode.time.ReminderHook;
import org.freeplane.addins.misc.BlinkingNodeHook;
import org.freeplane.addins.misc.CreationModificationPlugin;
import org.freeplane.addins.misc.FitToPage;
import org.freeplane.addins.misc.HierarchicalIcons;
import org.freeplane.controller.Controller;
import org.freeplane.map.attribute.mindmapnode.MAttributeController;
import org.freeplane.map.clipboard.mindmapmode.MClipboardController;
import org.freeplane.map.cloud.mindmapmode.MCloudController;
import org.freeplane.map.edge.mindmapmode.MEdgeController;
import org.freeplane.map.icon.mindmapnode.MIconController;
import org.freeplane.map.link.mindmapmode.MLinkController;
import org.freeplane.map.nodelocation.mindmapmode.MLocationController;
import org.freeplane.map.nodestyle.mindmapmode.MNodeStyleController;
import org.freeplane.map.note.mindmapnode.MNoteController;
import org.freeplane.map.pattern.mindmapnode.MPatternController;
import org.freeplane.map.text.mindmapmode.MTextController;
import org.freeplane.map.tree.mindmapmode.MMapController;
import org.freeplane.map.url.mindmapmode.FileManager;
import org.freeplane.ui.FreeMindToolBar;
import org.freeplane.ui.MenuBuilder;

import plugins.help.FreeplaneHelpStarter;
import plugins.latex.LatexNodeHook;
import plugins.script.ScriptingRegistration;
import plugins.svg.ExportPdf;
import plugins.svg.ExportSvg;

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
		    "org/freeplane/addins/mindmapmode/export/ExportWithXSLT.xml");
		ExportToImage.createActions(modeController);
		new NodeHistory(modeController);
		menuBuilder.addAnnotatedAction(new ExportToOoWriter());
		menuBuilder.addAnnotatedAction(new ImportMindmanagerFiles());
		/* ******************* Plugins ********************* */
		new LatexNodeHook(modeController);
		new ScriptingRegistration(modeController);
		menuBuilder.addAnnotatedAction(new FreeplaneHelpStarter());
		menuBuilder.addAnnotatedAction(new ExportPdf());
		menuBuilder.addAnnotatedAction(new ExportSvg());
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
		Controller.getController().addModeController(modeController);
		modeController.setMapController(new MMapController(modeController));
		modeController.setUrlManager(new FileManager(modeController));
		modeController.setIconController(new MIconController(modeController));
		modeController.setNodeStyleController(new MNodeStyleController(modeController));
		modeController.setEdgeController(new MEdgeController(modeController));
		modeController.setCloudController(new MCloudController(modeController));
		modeController.setNoteController(new MNoteController(modeController));
		modeController.setLinkController(new MLinkController(modeController));
		modeController.setPatternController(new MPatternController(modeController));
		modeController.setTextController(new MTextController(modeController));
		modeController.setClipboardController(new MClipboardController(modeController));
		modeController.setLocationController(new MLocationController(modeController));
		modeController.setAttributeController(new MAttributeController(modeController));
		final JPopupMenu popupmenu = new JPopupMenu();
		modeController.getUserInputListenerFactory().setNodePopupMenu(popupmenu);
		final FreeMindToolBar toolbar = new FreeMindToolBar();
		modeController.getUserInputListenerFactory().setMainToolBar(toolbar);
		modeController.getUserInputListenerFactory().setLeftToolBar(
		    ((MIconController) modeController.getIconController()).getIconToolBarScrollPane());
		new RevisionPlugin(modeController);
		modeController.updateMenus("org/freeplane/modes/mindmapmode/menu.xml");
	}
}
