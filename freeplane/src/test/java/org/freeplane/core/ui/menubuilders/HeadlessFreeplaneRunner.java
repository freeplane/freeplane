package org.freeplane.core.ui.menubuilders;

import org.freeplane.core.util.Compat;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.filter.FilterController;
import org.freeplane.features.mode.Controller;
import org.freeplane.main.application.CommandLineParser;
import org.freeplane.main.headlessmode.FreeplaneHeadlessStarter;

public class HeadlessFreeplaneRunner {
	static {
		Compat.setIsApplet(false);
		FreeplaneHeadlessStarter starter = new FreeplaneHeadlessStarter(CommandLineParser.parse());
		try {
			if (null == System.getProperty("org.freeplane.core.dir.lib", null)) {
				System.setProperty("org.freeplane.core.dir.lib", "/lib/");
			}
			final Controller controller = starter.createController();
			starter.createModeControllers(controller);
			FilterController.getController(controller).loadDefaultConditions();
			starter.createFrame();
		}
		catch (final Exception e) {
			LogUtils.severe(e);
			System.exit(1);
		}
	}
}
