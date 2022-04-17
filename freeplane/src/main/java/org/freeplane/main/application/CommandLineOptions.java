package org.freeplane.main.application;

import java.util.List;

public interface CommandLineOptions {

	boolean isNonInteractive();

	boolean shouldStopAfterLaunch();

	List<String> getMenuItemsToExecute();
	
	List<String> getScriptsToExecute();
	
	String[] getFilesToOpenAsArray();

	boolean hasItemsToExecute();

}
