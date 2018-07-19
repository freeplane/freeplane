package org.freeplane.launcher;

import java.io.File;

import org.freeplane.api.HeadlessMapCreator;
import org.freeplane.api.Map;

public class TestApp {
	public static void main(String[] args) {
		if (args.length == 2) {
			final File freeplaneInstallationDirectory = new File(args[0]);
			final File newMapFile = new File(args[1]);
			createNewMindMap(freeplaneInstallationDirectory, newMapFile);
		}
		else
			System.out.println("Parameters: <Freeplane installation directory> <File to save>");
	}

	private static void createNewMindMap(File freeplaneInstallationDirectory, final File newMapFile) {
		final Launcher launcher = Launcher.createForInstallation(freeplaneInstallationDirectory).disableSecurityManager();
		HeadlessMapCreator mapCreator = launcher.launchHeadless();
		final Map map = mapCreator.load(TestApp.class.getResource("/templateFile.mm")).saveAfterLoading().getMap();
		map.getRoot().createChild().setText("hello world");
		map.saveAs(newMapFile);
		System.out.println("Saved file " + newMapFile.getAbsolutePath());
		launcher.shutdown();
	}

}
