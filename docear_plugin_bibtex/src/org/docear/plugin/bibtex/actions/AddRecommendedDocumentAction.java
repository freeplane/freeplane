package org.docear.plugin.bibtex.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.apache.commons.io.FileUtils;
import org.docear.plugin.core.CoreConfiguration;
import org.docear.plugin.core.DocearController;
import org.docear.plugin.core.event.DocearEvent;
import org.docear.plugin.core.event.IDocearEventListener;
import org.docear.plugin.core.io.ProgressInputStream;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.plugin.workspace.WorkspaceUtils;

public class AddRecommendedDocumentAction extends AFreeplaneAction implements IDocearEventListener {

	private static final long serialVersionUID = 1L;
	public static String key = "AddRecommendedDocumentAction";

	public AddRecommendedDocumentAction() {
		super(key);
		DocearController.getController().addDocearEventListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void handleEvent(DocearEvent event) {
		if ("IMPORT_TO_LIBRARY".equals(event.getEventObject())) {
			try {
				URL url = null;
				if(event.getSource() instanceof URI) {
					url = ((URI) event.getSource()).toURL();
				}
				else if(event.getSource() instanceof URL) {
					url = ((URL) event.getSource());
				}
				else {
					//maybe log warning
					return;
				}

				String fileName = new File(url.getFile()).getName();
				fileName = URLDecoder.decode(fileName, "UTF-8");
				File file = getDestinationFile(url.toURI(), fileName);
				if (file == null || !file.exists()) {
					return;
				}
			}
			catch (Exception e) {
				LogUtils.warn(e);
			}
		}

	}

	private void addFileToLibrary(File file) {
		
		
	}

	public File getDestinationFile(final URI uri, String defaultFileName) throws URISyntaxException, MalformedURLException {
		File defaultFile = new File(WorkspaceUtils.resolveURI(CoreConfiguration.repositoryPathObserver.getUri()), defaultFileName);

		final JFileChooser fc = new JFileChooser();
		fc.approveSelection();
		fc.setSelectedFile(defaultFile);
		File file = null;
		while (fc.showSaveDialog(UITools.getFrame()) == JFileChooser.APPROVE_OPTION) {
			file = fc.getSelectedFile();
			if (file.exists()) {
				int answer = JOptionPane.showConfirmDialog(UITools.getFrame(), TextUtils.getText("docear.recommendation.replace_existing_file"));
				if (answer == JOptionPane.CANCEL_OPTION) {
					break;
				}
				if (answer != JOptionPane.OK_OPTION) {
					continue;
				}				
			}
			downloadFile(uri, fc);
			break;
		}		
		return file;
	}

	private void downloadFile(final URI uri, final JFileChooser fc) {
		new Thread(new Runnable() {
			File destinationFile = fc.getSelectedFile();
			File partFile = new File(destinationFile.getAbsoluteFile()+".part");
 			
			public void run() {
				try {
					FileUtils.copyInputStreamToFile(new ProgressInputStream(uri.toURL().openConnection()), partFile);
					
					if (destinationFile.exists()) {
						destinationFile.delete();
					}
					try {
						FileUtils.moveFile(partFile, destinationFile);
					}
					catch (IOException e) {
						LogUtils.warn(e);
					}
			
					addFileToLibrary(destinationFile);
					CoreConfiguration.repositoryPathObserver.setUri(CoreConfiguration.repositoryPathObserver.getUri());
				}
				catch (FileNotFoundException e) {					
					JOptionPane.showMessageDialog(UITools.getFrame(), TextUtils.getText("docear.recommendation.permission_denied"), TextUtils.getText("docear.recommendation.error.title"), JOptionPane.ERROR_MESSAGE);
				}
				catch (InterruptedIOException e) {
					LogUtils.info("Interrupted download");
					partFile.delete();
				}
				catch (IOException e) {					
					partFile.delete();
					LogUtils.info(e.getMessage());
					JOptionPane.showMessageDialog(UITools.getFrame(), TextUtils.getText("docear.recommendation.url_not_found"), TextUtils.getText("docear.recommendation.error.title"), JOptionPane.ERROR_MESSAGE);					
				}			
			}
		}).start();
	}

}
