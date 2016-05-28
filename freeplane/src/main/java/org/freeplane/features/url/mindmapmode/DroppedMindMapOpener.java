package org.freeplane.features.url.mindmapmode;

import java.net.URL;
import java.util.Collection;

import org.freeplane.core.ui.FileOpener.Listener;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.mode.mindmapmode.MModeController;

public class DroppedMindMapOpener implements Listener{

	@Override
	public void filesDropped(Collection<URL> urls) throws Exception {
		if(urls.isEmpty())
			return;
		Controller.getCurrentController().selectMode(MModeController.MODENAME);
		ModeController modeController = Controller.getCurrentModeController();
		for(URL url :  urls){
			modeController.getMapController().newMap(url);
		}
	}
}