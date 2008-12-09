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

import org.freeplane.addins.automaticlayout.AutomaticLayout;
import org.freeplane.addins.blinkingnodehook.BlinkingNodeHook;
import org.freeplane.addins.creationmodificationplugin.CreationModificationPlugin;
import org.freeplane.addins.hierarchicalicons.HierarchicalIcons;
import org.freeplane.addins.latex.LatexNodeHook;
import org.freeplane.addins.mindmapmode.ApplyFormatPlugin;
import org.freeplane.addins.revision.RevisionPlugin;
import org.freeplane.addins.time.ReminderHook;
import org.freeplane.io.url.mindmapmode.FileManager;
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
import org.freeplane.ui.FreeMindToolBar;
import org.freeplane.ui.MenuBuilder;

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
		new LatexNodeHook(modeController);
		final MenuBuilder menuBuilder = modeController.getUserInputListenerFactory().getMenuBuilder();
		menuBuilder.addAnnotatedAction(new ApplyFormatPlugin());
		
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
		modeController.setMapController(new MMapController(modeController));
		modeController.setUrlManager(new FileManager(modeController));
		modeController.setIconController(new MIconController(modeController));
		modeController.setNodeStyleController(new MNodeStyleController(
		    modeController));
		modeController.setEdgeController(new MEdgeController(modeController));
		modeController.setCloudController(new MCloudController(modeController));
		modeController.setNoteController(new MNoteController(modeController));
		modeController.setLinkController(new MLinkController(modeController));
		modeController.setPatternController(new MPatternController(
		    modeController));
		modeController.setTextController(new MTextController(modeController));
		modeController.setClipboardController(new MClipboardController(
		    modeController));
		modeController.setLocationController(new MLocationController(
		    modeController));
		modeController.setAttributeController(new MAttributeController(
		    modeController));
		modeController.createNodeHookActions();
		final JPopupMenu popupmenu = new JPopupMenu();
		modeController.getUserInputListenerFactory()
		    .setNodePopupMenu(popupmenu);
		final FreeMindToolBar toolbar = new FreeMindToolBar();
		modeController.getUserInputListenerFactory().setMainToolBar(toolbar);
		modeController.getUserInputListenerFactory().setLeftToolBar(
		    ((MIconController) modeController.getIconController())
		        .getIconToolBarScrollPane());
		new RevisionPlugin(modeController);
		modeController.updateMenus("org/freeplane/modes/mindmapmode/menu.xml");
	}
}
