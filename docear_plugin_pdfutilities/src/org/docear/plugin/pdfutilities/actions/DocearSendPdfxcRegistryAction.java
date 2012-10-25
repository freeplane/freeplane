package org.docear.plugin.pdfutilities.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.swing.JOptionPane;

import org.docear.plugin.pdfutilities.PdfUtilitiesController;
import org.docear.plugin.services.communications.CommunicationsController;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.util.Compat;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.mode.Controller;

import sun.net.www.ParseUtil;

public class DocearSendPdfxcRegistryAction extends AFreeplaneAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DocearSendPdfxcRegistryAction(String key) {
		super(key);		
	}

	public void actionPerformed(ActionEvent e) {
		if(Compat.isMacOsX()) return;
		int result = JOptionPane.showConfirmDialog(Controller.getCurrentController().getViewController().getJFrame(), TextUtils.getText("docear.pdfxcv.settings.warning.text"), TextUtils.getText("docear.pdfxcv.settings.warning.title"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
		if(result != JOptionPane.OK_OPTION){
			return;
		}
		try {
			new File(ResourceController.getResourceController().getFreeplaneUserDirectory()+"\\pdfxcSettings").mkdir();
			File pdfxcSettingsFile = new File(ResourceController.getResourceController().getFreeplaneUserDirectory(), "pdfxcvSettings.reg");
			PdfUtilitiesController.getController().exportRegistryKey("HKEY_CURRENT_USER\\Software\\Tracker Software", pdfxcSettingsFile);
			if(pdfxcSettingsFile.exists()){
				File pdfxcSettingsZipFile = new File(ResourceController.getResourceController().getFreeplaneUserDirectory()+"\\pdfxcSettings", "pdfxcvSettings.zip");
				File[] regFiles = new File(ResourceController.getResourceController().getFreeplaneUserDirectory()).listFiles(new FilenameFilter() {
					
					public boolean accept(File dir, String name) {						
						return name.toLowerCase().endsWith(".reg");
					}
				});
				zipFile(regFiles, pdfxcSettingsZipFile);
				if(pdfxcSettingsZipFile.exists()){
					try {
						URI mailtoUri;
						StringBuilder uriString = new StringBuilder();
						uriString.append("mailto:help@docear.org?subject=PDFXCV Settings Problem&body=");
						if(CommunicationsController.getController().getUserName() != null){
							uriString.append("Username:");
							uriString.append(CommunicationsController.getController().getUserName());
						}						
						uriString.append("%0D%0A%0D%0A");
						uriString.append(TextUtils.getRawText("docear.pdfxcv.settings.mail.text"));
					
						mailtoUri = URI.create(ParseUtil.encodePath(uriString.toString()));
						Controller.getCurrentController().getViewController().openDocument(mailtoUri);
						Controller.getCurrentController().getViewController().openDocument(Compat.fileToUrl(pdfxcSettingsZipFile.getParentFile()).toURI());
					}			
					catch (IOException e1) {
						LogUtils.warn(e1);
					}
					catch (URISyntaxException e1) {
						LogUtils.warn(e1);
					}					
				}			
			}
		}
		catch (IOException e2) {
			LogUtils.info("Read pdf xchange settings registry : "+ e2.toString());
		}		
	}
	
	private void zipFile(File[] files, File zipFile){
		try {		    
			ZipOutputStream  out = new ZipOutputStream (new FileOutputStream(zipFile));	   
			for(File file : files){
				FileInputStream in = new FileInputStream(file);
			    out.putNextEntry(new ZipEntry(file.getName()));
			    byte[] buf = new byte[1024];
			    int len;
			    while ((len = in.read(buf)) > 0) {
			        out.write(buf, 0, len);
			    }
			    out.closeEntry();
			    in.close();
			}
			out.finish();
		    out.close();
		} catch (IOException e) {
			LogUtils.warn(e);
		}	
	}

}
