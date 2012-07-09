package org.docear.plugin.services.recommendations;

import java.io.IOException;
import java.io.InputStream;

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
		monitor = new ProgressMonitor(UITools.getFrame(), TextUtils.getText("recommendations.downloader.label"), "", 0, 1);
	}

	public void update(final Object source, int progress, int length, String... label ) {		
		if (monitor.isCanceled()) {
			try {
				((InputStream) source).close();
			}
			catch (IOException e) {				
				e.printStackTrace();
			}
			return;
		}
		String note = (label != null && label.length > 0) ? label[0] : null;
		if (note!= null && note.length()>50) {
			note = note.substring(0, 50) + "...";
		}
		monitor.setNote(note);
		monitor.setMaximum(length);
		monitor.setProgress(progress);
	}

	public static void install() {
		new RecommendationDownloadObserver();		
	}

	public void finished(Object source, String... label) {
		monitor = new ProgressMonitor(UITools.getFrame(), TextUtils.getText("recommendations.downloader.label"), "", 0, 1);
	}
}
