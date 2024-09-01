package org.freeplane.main.mindmapmode.stylemode;

import org.freeplane.features.mode.Controller;

public interface ExtensionInstaller {
    enum Context {MAIN, STYLE, UNKNOWN}
	void installExtensions(Controller controller, Context context);
}
