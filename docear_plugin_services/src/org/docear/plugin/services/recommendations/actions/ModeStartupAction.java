package org.docear.plugin.services.recommendations.actions;

import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.docear.plugin.bibtex.ReferencesController;
import org.docear.plugin.core.DocearController;
import org.docear.plugin.core.event.DocearEvent;
import org.docear.plugin.core.event.IDocearEventListener;
import org.docear.plugin.services.recommendations.mode.DocearRecommendationsModeController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.mode.mindmapmode.MModeController;



public class ModeStartupAction extends AFreeplaneAction implements IDocearEventListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static String key = "ModeStartupAction";


	public ModeStartupAction() {
		super(key);
		DocearController.getController().addDocearEventListener(this);

	}

	public void actionPerformed(ActionEvent e) {		
	}

	public void handleEvent(DocearEvent event) {
		if ("DOCEAR_MODE_STARTUP".equals(event.getEventObject())) {
			if (event.getSource() instanceof DocearRecommendationsModeController) {	
				final JTabbedPane tabs = (JTabbedPane) MModeController.getMModeController().getUserInputListenerFactory().getToolBar("/format")
	                    .getComponent(1);
	            Dimension fixSize =  new Dimension(tabs.getComponent(0).getWidth(), 32000);

    			try {
    				DocearRecommendationsModeController modeController = (DocearRecommendationsModeController) event.getSource();							
    				final JComponent comp = (JComponent) modeController.getUserInputListenerFactory().getToolBar("/format").getComponent(1);
    				JPanel jabref = ReferencesController.getController().getJabrefWrapper().getJabrefFramePanel();
    				comp.add(TextUtils.getText("jabref"), jabref);
    				comp.setPreferredSize(fixSize);
    			}
    			catch (Exception ex) {
    				LogUtils.warn(ex);
    			}
			}			
		}
	}

}
