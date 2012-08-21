package org.freeplane.plugin.openmaps;

import java.awt.Container;

import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.mode.NodeHookDescriptor;
import org.freeplane.features.mode.PersistentNodeHook;
import org.freeplane.features.ui.INodeViewLifeCycleListener;

@NodeHookDescriptor(hookName = "plugins/openmaps/OpenMapsNodeHook.properties", 
onceForMap = false)
public class OpenMapsNodeHook extends PersistentNodeHook implements INodeViewLifeCycleListener{

	public OpenMapsNodeHook() {
		super();
		final ModeController modeController = Controller.getCurrentModeController();
		modeController.addINodeViewLifeCycleListener(this);
	}
	
	@Override
	public void onViewCreated(Container nodeView) {
		
	}

	@Override
	public void onViewRemoved(Container nodeView) {
		
	}

	public void chooseLocation() {
		MapViewer mapView = new MapViewer();
		System.out.println("Pretend I've choosen a location");
	}

}
