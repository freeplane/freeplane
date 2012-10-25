package org.docear.plugin.services.components.dialog;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.TreeMap;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.docear.plugin.core.Version;
import org.docear.plugin.services.features.UpdateCheck;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.swingplus.JHyperlink;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class UpdateCheckerDialogPanel extends JPanel {
	class Option {
		public String key;
		public String text;
		
		public Option(String key, String text) {
			this.key = key;
			this.text = text;
		}
		
		public String toString() {
			return text;
		}
		public String getKey() {
			return key;
		}
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JComboBox optionsComboBox;
	
	private TreeMap<String, Option> optionsMap = new TreeMap<String, Option>();
	
	/**
	 * Create the dialog.
	 */
	public UpdateCheckerDialogPanel(String selectedOption, String runningVersionString, String latestVersionString, String status) {
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
		
		JLabel lblMessage = new JLabel(TextUtils.getText("docear.update_checker.message"));
		add(lblMessage, "1, 1, 3, 1, fill, top");
		lblMessage.setVerticalAlignment(SwingConstants.TOP);
		
		try {
			JLabel lblYouCanDownload = new JLabel(TextUtils.getText("docear.update_checker.you_can_download"));
			add(lblYouCanDownload, "1, 3");
			String uri = null;
			if (status.equals(Version.StatusName.devel.name())) {
				uri = "http://www.docear.org/support/forums/docear-support-forums-group3/experimental-releases-forum8/";
			}
			else {
				uri = "http://www.docear.org/software/download/";
			}
			JHyperlink hyperlink = new JHyperlink(uri, new URI(uri));
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
		
		optionsMap.put(UpdateCheck.DOCEAR_UPDATE_CHECKER_MAJOR, new Option(UpdateCheck.DOCEAR_UPDATE_CHECKER_MAJOR, TextUtils.getText("OptionPanel.docear.update_checker.major")));
		optionsMap.put(UpdateCheck.DOCEAR_UPDATE_CHECKER_MIDDLE, new Option(UpdateCheck.DOCEAR_UPDATE_CHECKER_MIDDLE, TextUtils.getText("OptionPanel.docear.update_checker.middle")));
		optionsMap.put(UpdateCheck.DOCEAR_UPDATE_CHECKER_MINOR, new Option(UpdateCheck.DOCEAR_UPDATE_CHECKER_MINOR, TextUtils.getText("OptionPanel.docear.update_checker.minor")));
		optionsMap.put(UpdateCheck.DOCEAR_UPDATE_CHECKER_BETA, new Option(UpdateCheck.DOCEAR_UPDATE_CHECKER_BETA, TextUtils.getText("OptionPanel.docear.update_checker.beta")));
		optionsMap.put(UpdateCheck.DOCEAR_UPDATE_CHECKER_ALL, new Option(UpdateCheck.DOCEAR_UPDATE_CHECKER_ALL, TextUtils.getText("OptionPanel.docear.update_checker.all")));
		optionsMap.put(UpdateCheck.DOCEAR_UPDATE_CHECKER_DISABLE, new Option(UpdateCheck.DOCEAR_UPDATE_CHECKER_DISABLE, TextUtils.getText("OptionPanel.docear.update_checker.disable")));
		
		optionsComboBox = new JComboBox(new Option[] {				
				optionsMap.get(UpdateCheck.DOCEAR_UPDATE_CHECKER_MAJOR),
				optionsMap.get(UpdateCheck.DOCEAR_UPDATE_CHECKER_MIDDLE),
				optionsMap.get(UpdateCheck.DOCEAR_UPDATE_CHECKER_MINOR),
				optionsMap.get(UpdateCheck.DOCEAR_UPDATE_CHECKER_BETA),
				optionsMap.get(UpdateCheck.DOCEAR_UPDATE_CHECKER_ALL),
				optionsMap.get(UpdateCheck.DOCEAR_UPDATE_CHECKER_DISABLE)
		});
		optionsComboBox.setEditable(false);
		
		String choice = ResourceController.getResourceController().getProperty("docear.update_checker.options");
		try {
			optionsComboBox.setSelectedItem(optionsMap.get(choice));
		}
		catch (Exception e) {
			LogUtils.warn(e);
		}
		
		add(optionsComboBox, "3, 9, fill, default");		
	}
	
	public String getChoice() {
		Option o = (Option) optionsComboBox.getSelectedItem(); 
		return o.getKey();		
	}
	

}
