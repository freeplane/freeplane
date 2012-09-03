package org.docear.plugin.bibtex.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.docear.plugin.core.CoreConfiguration;
import org.docear.plugin.core.DocearController;
import org.docear.plugin.core.event.DocearEvent;
import org.docear.plugin.core.event.DocearEventType;
import org.docear.plugin.core.event.IDocearEventListener;
import org.docear.plugin.core.io.ProgressInputStream;
import org.docear.plugin.core.util.FileUtilities;
import org.docear.plugin.services.communications.CommunicationsController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.plugin.workspace.WorkspaceUtils;

import sun.net.www.protocol.ftp.FtpURLConnection;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

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
		if (DocearEventType.IMPORT_TO_LIBRARY.equals(event.getType())) {
			try {
				URL url = null;
				if (event.getSource() instanceof URL) {
					url = ((URL) event.getSource());
				}
				else {
					// maybe log warning
					return;
				}
				url = new URL("ftp://ftp.cordis.lu/pub/improving/docs/ser_citizen_wiener.pdf");

				String fileName = new File(url.getFile()).getName();
				fileName = URLDecoder.decode(fileName, "UTF-8");
				String ext = FilenameUtils.getExtension(fileName);
				fileName = FileUtilities.getCleanFileName((String) event.getEventObject());
				
				if (fileName == null || fileName.isEmpty()) {
					fileName = (String) event.getEventObject();
				}
				File file = getDestinationFile(url.toURI(), fileName.trim() + "." + ext);
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
			File partFile = new File(destinationFile.getAbsoluteFile() + ".part");

			public void run() {
				try {
					CommunicationsController commController = CommunicationsController.getController();
					URL url = uri.toURL();
					InputStream inStream = null;
					int length = 0;
					if("ftp".equals(url.getProtocol())) {
						Authenticator.setDefault(new Authenticator() {
					    	protected PasswordAuthentication getPasswordAuthentication() {
					        	return new PasswordAuthentication("mag", "05m06g79".toCharArray());
					    	}
						});
						
						FtpURLConnection conn = (FtpURLConnection) url.openConnection(new Proxy(Type.SOCKS, new InetSocketAddress("141.44.129.59", 1080)));
						conn.setAllowUserInteraction(true);
						
						try {
							inStream = conn.getInputStream();
							length = inStream.available();
						}
						catch (Exception e) {
							e.printStackTrace();
						}
					}
					else {
						WebResource webResource = commController.getWebResource(uri);
						ClientResponse response = commController.get(webResource, ClientResponse.class);
						inStream = response.getEntityInputStream();
						length = response.getLength();
					}
						
					
					FileUtils.copyInputStreamToFile(new ProgressInputStream(inStream, uri.toURL(), length), partFile);
										
					if (destinationFile.exists()) {
						destinationFile.delete();
					}
					try {
						FileUtils.moveFile(partFile, destinationFile);
					}
					catch (IOException e) {
						LogUtils.warn(e);
					}

					SwingUtilities.invokeLater(new Runnable() {						
						@Override
						public void run() {
							addFileToLibrary(destinationFile);
							CoreConfiguration.repositoryPathObserver.setUri(CoreConfiguration.repositoryPathObserver.getUri());
						}
					});
				}
				catch (FileNotFoundException e) {
					SwingUtilities.invokeLater(new Runnable() {						
						@Override
						public void run() {
							JOptionPane.showMessageDialog(UITools.getFrame(), TextUtils.getText("docear.recommendation.permission_denied"),
									TextUtils.getText("docear.recommendation.error.title"), JOptionPane.ERROR_MESSAGE);
						}
					});
					
				}
				catch (InterruptedIOException e) {
					LogUtils.info("Interrupted download");
					partFile.delete();
				}
				catch (Exception e) {
					partFile.delete();
					LogUtils.info(e.getMessage());
					SwingUtilities.invokeLater(new Runnable() {						
						@Override
						public void run() {
							JOptionPane.showMessageDialog(UITools.getFrame(), TextUtils.getText("docear.recommendation.url_not_found"),
									TextUtils.getText("docear.recommendation.error.title"), JOptionPane.ERROR_MESSAGE);
						}
					});
					
				}
			}
		}).start();
	}

}
