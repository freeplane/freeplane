package org.freeplane.main.addons;

import java.net.URL;

import org.freeplane.core.extension.IExtension;

public interface AddOnInstaller extends IExtension {
	void install(URL url);
}
