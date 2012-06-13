package org.docear.plugin.bibtex.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.apache.commons.io.FileUtils;
import org.docear.plugin.core.CoreConfiguration;
import org.docear.plugin.core.DocearController;
import org.docear.plugin.core.event.DocearEvent;
import org.docear.plugin.core.event.IDocearEventListener;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.plugin.workspace.WorkspaceUtils;

public class AddRecommendedDocumentAction extends AFreeplaneAction implements IDocearEventListener {

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
				URI uri = (URI) event.getSource();

				String fileName = new File(uri.toURL().getFile()).getName();
				fileName = URLDecoder.decode(fileName, "UTF-8");
				File file = getDestinationFile(uri, fileName);
				if (file == null || !file.exists()) {
					return;
				}
				
				addFileToLibrary(file);
				CoreConfiguration.repositoryPathObserver.setUri(CoreConfiguration.repositoryPathObserver.getUri());
			}
			catch (Exception e) {
				LogUtils.warn(e);
			}
		}

	}

	private void addFileToLibrary(File file) {
		
		
	}

	public File getDestinationFile(URI uri, String defaultFileName) throws URISyntaxException, MalformedURLException {
		File defaultFile = new File(WorkspaceUtils.resolveURI(CoreConfiguration.repositoryPathObserver.getUri()), defaultFileName);

		final JFileChooser fc = new JFileChooser();
		fc.approveSelection();
		fc.setSelectedFile(defaultFile);
		File file = null;
		while (fc.showOpenDialog(UITools.getFrame()) == JFileChooser.APPROVE_OPTION) {
			file = fc.getSelectedFile();
			if (file.exists()) {
				int answer = JOptionPane.showConfirmDialog(UITools.getFrame(), TextUtils.getText("docear.recommendation.replace_existing_file"));
				if (answer != JOptionPane.OK_OPTION) {
					continue;
				}
			}
			break;
		}
		
		try {
			FileUtils.copyURLToFile(uri.toURL(), file);
		}
		catch (IOException e) {
			LogUtils.warn(e);
			JOptionPane.showMessageDialog(UITools.getFrame(), TextUtils.getText("docear.recommendation.url_not_found"));
		}
		return file;
	}

}
