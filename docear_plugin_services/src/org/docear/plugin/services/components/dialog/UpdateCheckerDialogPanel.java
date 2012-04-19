package org.docear.plugin.services.components.dialog;

import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.swingplus.JHyperlink;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class UpdateCheckerDialogPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JComboBox optionsComboBox;
	
	/**
	 * Create the dialog.
	 */
	public UpdateCheckerDialogPanel(String selectedOption, String runningVersionString, String latestVersionString) {
		setBounds(100, 100, 620, 266);
		setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("150dlu:grow"),},
			new RowSpec[] {
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				RowSpec.decode("1dlu"),
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,}));
		
		JLabel lblBlubber = new JLabel(TextUtils.getText("docear.update_checker.message"));
		add(lblBlubber, "1, 1, 3, 1, fill, top");
		lblBlubber.setVerticalAlignment(SwingConstants.TOP);
		
		try {
			JLabel lblYouCanDownload = new JLabel(TextUtils.getText("docear.update_checker.you_can_download"));
			add(lblYouCanDownload, "1, 3");
			JHyperlink hyperlink = new JHyperlink("http://www.docear.org/software/download/", new URI("http://www.docear.org/software/download/"));
			add(hyperlink, "3, 3");
		} catch (URISyntaxException e) {
			LogUtils.warn(e);
		}
		
		JLabel lblYourVersion = new JLabel(TextUtils.getText("docear.update_checker.active_version"));
		add(lblYourVersion, "1, 5");
		
		JLabel lblLinkold = new JLabel(runningVersionString);
		add(lblLinkold, "3, 5");
		
		JLabel lblNewAvailableVersion = new JLabel(TextUtils.getText("docear.update_checker.latest_version"));
		add(lblNewAvailableVersion, "1, 7");
		
		JLabel lblLinkNew = new JLabel(latestVersionString);
		add(lblLinkNew, "3, 7");
		
		JLabel lblNotify = new JLabel(TextUtils.getText("docear.update_checker.notify"));
		add(lblNotify, "1, 9");
		
		
		optionsComboBox = new JComboBox(new String[] {				
				TextUtils.getText("docear.update_checker.major"),
				TextUtils.getText("docear.update_checker.middle"),
				TextUtils.getText("docear.update_checker.minor"),
				TextUtils.getText("docear.update_checker.beta"),
				TextUtils.getText("docear.update_checker.all"),
				TextUtils.getText("docear.update_checker.disable")			
		});
		
		add(optionsComboBox, "3, 9, fill, default");		
	}
	

}
