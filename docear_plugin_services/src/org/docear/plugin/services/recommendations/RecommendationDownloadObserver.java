package org.docear.plugin.services.recommendations;

import javax.swing.ProgressMonitor;

import org.docear.plugin.core.DocearController;
import org.docear.plugin.core.features.DocearProgressObserver;
import org.docear.plugin.core.io.ProgressInputStream;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.TextUtils;

public class RecommendationDownloadObserver implements DocearProgressObserver {
	
	ProgressMonitor monitor;
	
	
	private RecommendationDownloadObserver() {
		DocearController.getController().addProgressObserver(ProgressInputStream.class, this);
	}

	public void update(Object source, int progress, int length) {
		if(monitor == null) {
			monitor = new ProgressMonitor(UITools.getFrame(), TextUtils.getText("recommendations.downloader.label"), source.toString(), 0, length);
		}
		monitor.setMaximum(length);
		monitor.setProgress(progress);	
	}

	public static void install() {
		new RecommendationDownloadObserver();		
	}

}
