package org.freeplane.api;

import java.io.File;

public interface HeadlessLoader {
	Loader associatedWith(File file);
	Loader associatedWith(String file);
	Loader savedAs(File file);
	Loader savedAs(String file);
	Loader unsaved();
	Map getMap();

}
