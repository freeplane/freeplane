package org.freeplane.features.url.mindmapmode;

import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.awt.event.ActionEvent;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.components.OptionalDontShowMeAgainDialog;
import org.freeplane.core.ui.components.OptionalDontShowMeAgainDialog.MessageType;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.map.IMapSelectionListener;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.mindmapmode.MMapController;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.ui.IMapViewManager;

public class ExternalMapChangeMonitor implements IMapSelectionListener{
    private static final String MONITOR_EXTERNAL_MIND_MAP_FILE_CHANGES_PROPERTY = "monitor_external_mind_map_file_changes";
    private static final int FIRST_CHECK_DELAY_MILLIS = 1000;
    private static final int CHECK_INTERVAL_MILLIS = 5000;
    private static final ExternalMapChangeMonitor INSTANCE = new ExternalMapChangeMonitor();
    private Timer timer;
    
    public static void install(IMapViewManager viewManager) {
        viewManager.addMapSelectionListener(INSTANCE);
    }

    private ExternalMapChangeMonitor() {
        super();
        timer = new Timer(CHECK_INTERVAL_MILLIS, this::checkCurrentMapForExternalChange);
        timer.setInitialDelay(FIRST_CHECK_DELAY_MILLIS);
        timer.start();
    }

    @Override
    public void afterMapChange(MapModel oldMap, MapModel newMap) {
        timer.restart();
    }
    
    private void checkCurrentMapForExternalChange(ActionEvent event) {
        MapModel map = Controller.getCurrentController().getMap();
        checkForExternalChange(map);
    }

    public void checkForExternalChange(MapModel map) {
        if(map == null) {
            return;
        } 
        Controller controller = Controller.getCurrentController();
        if (controller.getMap() != map) {
            return;
        }
        boolean reloadsMapAfterExternalChange = ResourceController.getResourceController().getBooleanProperty(MONITOR_EXTERNAL_MIND_MAP_FILE_CHANGES_PROPERTY, true);
        if (!reloadsMapAfterExternalChange) {
            return;
        }
        Window focusedWindow = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusedWindow();
        if(focusedWindow == null) {
            return;
        }
        JComponent mapViewComponent = controller.getMapViewManager().getMapViewComponent();
        if(mapViewComponent == null) {
            return;
        }
        if(! SwingUtilities.isDescendingFrom(mapViewComponent, focusedWindow))
            return;
        
        if (!map.hasExternalFileChanged()) {
            return;
        }
        final int shouldReload = OptionalDontShowMeAgainDialog.show("reload_mindmap_after_external_change", "confirmation",
                MONITOR_EXTERNAL_MIND_MAP_FILE_CHANGES_PROPERTY, MessageType.ONLY_CANCEL_SELECTION_IS_STORED);
        if (shouldReload != JOptionPane.OK_OPTION) {
            map.updateLastKnownFileModificationTime();
        }
        else {
            reloadMap(map);
        }
    }

    private void reloadMap(MapModel map) {
        Controller controller =Controller.getCurrentController();
        if(controller.getMap() != map)
            return;
        final MMapController mapController = (MMapController) controller.getModeController().getMapController();

        try {
            mapController.restoreCurrentMapIgnoreAlternatives();
        }
        catch (final Exception e) {
            LogUtils.severe(e);
        }
    }

}
