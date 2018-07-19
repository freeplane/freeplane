package org.freeplane.api;

import java.io.File;

public interface HeadlessLoader {
	Loader newMapLocation(File file);
	Loader newMapLocation(String file);
	Loader unsetMapLocation();
	Loader saveAfterLoading();
	Loader selectNodeId(String nodeId);
	Map getMap();

}
