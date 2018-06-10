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
package org.freeplane.main.headlessmode;

import java.util.Set;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.attribute.ModelessAttributeController;
import org.freeplane.features.filter.FilterController;
import org.freeplane.features.format.FormatController;
import org.freeplane.features.format.ScannerController;
import org.freeplane.features.help.HelpController;
import org.freeplane.features.highlight.HighlightController;
import org.freeplane.features.icon.IconController;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.map.MapController;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.features.styles.LogicalStyleFilterController;
import org.freeplane.features.text.TextController;
import org.freeplane.features.time.TimeController;
import org.freeplane.main.application.ApplicationResourceController;
import org.freeplane.main.application.FreeplaneGUIStarter;
import org.freeplane.main.application.FreeplaneStarter;
import org.freeplane.view.swing.features.nodehistory.NodeHistory;

public class FreeplaneHeadlessStarter implements FreeplaneStarter {

	private ApplicationResourceController applicationResourceController;
// // 	private Controller controller;
	/** allows to disable loadLastMap(s) if there already is a second instance running. */
	public FreeplaneHeadlessStarter() {
		super();
		applicationResourceController = new ApplicationResourceController();
	}

	@Override
	public void setDontLoadLastMaps() {
    }

	@Override
	public Controller createController() {
		try {
			Controller controller = new Controller(applicationResourceController);
			Controller.setCurrentController(controller);
			applicationResourceController.init();
			LogUtils.createLogger();
			FreeplaneGUIStarter.showSysInfo();
			final HeadlessMapViewController mapViewController = new HeadlessMapViewController();
			controller.setMapViewManager(mapViewController);
			controller.setViewController(new HeadlessUIController(controller, mapViewController, ""));
			controller.addExtension(HighlightController.class, new HighlightController());
			FilterController.install();
			FormatController.install(new FormatController());
	        final ScannerController scannerController = new ScannerController();
	        ScannerController.install(scannerController);
	        scannerController.addParsersForStandardFormats();
			ModelessAttributeController.install();
			TextController.install();
			TimeController.install();
			LinkController.install();
			IconController.installConditionControllers();
			HelpController.install();
			FilterController.getCurrentFilterController().getConditionFactory().addConditionController(70,
			    new LogicalStyleFilterController());
			MapController.install();

			NodeHistory.install(controller);
			return controller;
		}
		catch (final Exception e) {
			LogUtils.severe(e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public void createModeControllers(final Controller controller) {
		HeadlessMModeControllerFactory.createModeController();
		controller.getModeController(MModeController.MODENAME).getMapController().addMapChangeListener(
			applicationResourceController.getLastOpenedList());
    }

	@Override
	public void buildMenus(final Controller controller, final Set<String> plugins) {
    }


	@Override
	public void createFrame(final String[] args) {
		Controller controller = Controller.getCurrentController();
		ModeController modeController = controller.getModeController(MModeController.MODENAME);
		controller.selectModeForBuild(modeController);
		Controller.getCurrentController().fireStartupFinished();
	}

	@Override
	public void stop() {
	}

	@Override
	public ResourceController getResourceController() {
	    return applicationResourceController;
    }

	@Override
	public void loadMapsLater(String[] args) {
	    // TODO Auto-generated method stub

    }
}
