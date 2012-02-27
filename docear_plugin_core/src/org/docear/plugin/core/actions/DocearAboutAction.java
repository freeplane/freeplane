package org.docear.plugin.core.actions;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.Properties;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.Compat;
import org.freeplane.core.util.FreeplaneVersion;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.help.AboutAction;
import org.freeplane.features.mode.Controller;


public class DocearAboutAction extends AboutAction {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DocearAboutAction() {
		super();
	}
	
	public void actionPerformed(final ActionEvent e) {
		ResourceController resourceController = ResourceController.getResourceController();
		
		Properties about_props = new Properties();
		try {
			about_props.load(this.getClass().getResourceAsStream("/about.properties"));
		}
		catch (IOException e1) {
			LogUtils.warn("DOCEAR: could not load core \"about\" properties");
		}
		String programmer = about_props.getProperty("docear_programmer");
		String copyright = about_props.getProperty("docear_copyright");
		String version	= resourceController.getProperty("docear_version");
		String status	= resourceController.getProperty("docear_status");
		
		String aboutText = TextUtils.getRawText("docear_about");
		MessageFormat formatter;
        try {
            formatter = new MessageFormat(aboutText);
            aboutText = formatter.format(new Object[]{ version+" "+status, copyright, programmer});
        }
        catch (IllegalArgumentException ex) {
            LogUtils.severe("wrong format " + aboutText + " for property " + "docear_about", ex);
        }
		
		Box box = Box.createVerticalBox();		
		addMessage(box, aboutText);		
		addUri(box, "http://docear.org", "http://docear.org");		
		addMessage(box, "based on: ");
		addUri(box, resourceController.getProperty("homepage_url"), "Freeplane "+FreeplaneVersion.getVersion().toString());		
		addMessage(box, FreeplaneVersion.getVersion().getRevision());
		addFormattedMessage(box, "java_version", Compat.JAVA_VERSION);
		addFormattedMessage(box, "main_resource_directory", ResourceController.getResourceController().getResourceBaseDir());
		addUri(box, resourceController.getProperty("license_url"), TextUtils.getText("license"));
		addMessage(box, TextUtils.getText("license_text"));
		
		JOptionPane.showMessageDialog(Controller.getCurrentController().getViewController().getViewport(), box, TextUtils
		    .getText("AboutAction.text"), JOptionPane.INFORMATION_MESSAGE);
	}

	private void addFormattedMessage(Box box, String format, String parameter) {
		box.add(new JLabel(TextUtils.format(format, parameter)));
	}

	private void addMessage(Box box, String localMessage) {
		box.add(new JLabel(localMessage));
	}

	private void addUri(Box box, String uriString, String message) {
		try {
			URI uri = new URI(uriString);
			JButton uriButton = UITools.createHtmlLinkStyleButton(uri, message);
			uriButton.setHorizontalAlignment(SwingConstants.LEADING);
			box.add(uriButton);
		} catch (URISyntaxException e1) {
		}
	}
	
}
