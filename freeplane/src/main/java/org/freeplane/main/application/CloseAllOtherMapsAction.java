package org.freeplane.main.application;

import java.awt.event.ActionEvent;
import java.util.ArrayList;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.view.swing.map.mindmapmode.MMapViewController;

@SuppressWarnings("serial")
class CloseAllOtherMapsAction extends AFreeplaneAction{

	private final MMapViewController mapViewController;

    public CloseAllOtherMapsAction(MMapViewController mapViewController) {
		super("CloseAllOtherMapsAction");
        this.mapViewController = mapViewController;
	}

    @Override
    public void actionPerformed(ActionEvent e) {
        if(mapViewController.saveAllModifiedMaps()) {
            final Controller controller = Controller.getCurrentController();
            final MapController mapController = controller.getModeController(MModeController.MODENAME).getMapController();
            final ArrayList<MapModel> maps = new ArrayList<>(mapViewController.getMaps().values());
            maps.remove(mapViewController.getMap());
            maps.forEach(mapController::closeWithoutSaving);
        }
    }


}
