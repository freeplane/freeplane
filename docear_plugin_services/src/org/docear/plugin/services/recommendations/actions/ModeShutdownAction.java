package org.docear.plugin.services.recommendations.actions;

import java.awt.event.ActionEvent;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.docear.plugin.bibtex.ReferencesController;
import org.docear.plugin.core.DocearController;
import org.docear.plugin.core.event.DocearEvent;
import org.docear.plugin.core.event.IDocearEventListener;
import org.docear.plugin.services.recommendations.mode.DocearRecommendationsModeController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.mode.mindmapmode.MModeController;

public class ModeShutdownAction extends AFreeplaneAction implements IDocearEventListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static String key = "ModeShutdownAction";


	public ModeShutdownAction() {
		super(key);
		DocearController.getController().addDocearEventListener(this);

	}

	public void actionPerformed(ActionEvent e) {		
	}

	public void handleEvent(DocearEvent event) {
		if ("DOCEAR_MODE_SHUTDOWN".equals(event.getEventObject())) {
			if (event.getSource() instanceof DocearRecommendationsModeController) {
				ModeController modeController = MModeController.getMModeController();
				final JTabbedPane tabs = (JTabbedPane) modeController.getUserInputListenerFactory().getToolBar("/format").getComponent(1);
				JPanel jabref = ReferencesController.getController().getJabrefWrapper().getJabrefFramePanel();
				tabs.add(TextUtils.getText("jabref"), jabref);
				tabs.setSelectedComponent(jabref);
			}
		}
	}

}
