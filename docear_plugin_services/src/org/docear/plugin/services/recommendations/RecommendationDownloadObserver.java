package org.docear.plugin.services.recommendations;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JDialog;
import javax.swing.ProgressMonitor;

import org.docear.plugin.core.DocearController;
import org.docear.plugin.core.features.DocearProgressObserver;
import org.docear.plugin.core.io.ProgressInputStream;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.TextUtils;

public class RecommendationDownloadObserver implements DocearProgressObserver {
	
	ProgressMonitor monitor;
	boolean dialogPrepared;
	private Set<Object> aborts = new HashSet<Object>();
	
	private RecommendationDownloadObserver() {
		DocearController.getController().addProgressObserver(ProgressInputStream.class, this);
	}

	public void update(final Object source, int progress, int length, String... label ) {
		if(aborts.contains(source)) {
			return;
		}
		String note = (label != null && label.length > 0) ? label[0] : null;
		if(monitor == null) {
			monitor = new ProgressMonitor(UITools.getFrame(), TextUtils.getText("recommendations.downloader.label"), note, 0, length);
			dialogPrepared = false;
		}
		else if (monitor.isCanceled()) {
			try {
				((InputStream) source).close();
			}
			catch (IOException e) {				
				e.printStackTrace();
			}
			aborts.add(source);
			monitor = null;
			return;
		}
		
		monitor.setMaximum(length);
		monitor.setProgress(progress);	
	}

	public static void install() {
		new RecommendationDownloadObserver();		
	}
}
