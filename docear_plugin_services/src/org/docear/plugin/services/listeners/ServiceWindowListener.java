package org.docear.plugin.services.listeners;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.SwingUtilities;

import org.docear.plugin.services.ServiceController;
import org.docear.plugin.services.recommendations.mode.DocearRecommendationsMapController;
import org.docear.plugin.services.recommendations.mode.DocearRecommendationsModeController;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;

public class ServiceWindowListener implements WindowListener {

	public void windowOpened(WindowEvent e) {
		final WindowListener wl = this;		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if(ServiceController.getController().getAutoRecommendations() != null) {
					UITools.getFrame().removeWindowListener(wl);
					ModeController modeController = Controller.getCurrentController().getModeController(DocearRecommendationsModeController.MODENAME);
					((DocearRecommendationsMapController)modeController.getMapController()).newMap(ServiceController.getController().getAutoRecommendations());
					ServiceController.getController().setAutoRecommendations(null);
				}
			}
		});					
	}
	
	public void windowIconified(WindowEvent e) {
	}
	
	public void windowDeiconified(WindowEvent e) {
	}
	
	public void windowDeactivated(WindowEvent e) {
	}
	
	public void windowClosing(WindowEvent e) {
	}
	
	public void windowClosed(WindowEvent e) {
	}
	
	public void windowActivated(WindowEvent e) {
	}	
}