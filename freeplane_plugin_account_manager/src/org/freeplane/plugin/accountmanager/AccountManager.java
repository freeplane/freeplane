package org.freeplane.plugin.accountmanager;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import javax.swing.tree.DefaultMutableTreeNode;

import org.freeplane.core.resources.IFreeplanePropertyListener;
import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.resources.components.IPropertyControl;
import org.freeplane.core.resources.components.IPropertyControlCreator;
import org.freeplane.core.resources.components.OptionPanelBuilder;
import org.freeplane.core.resources.components.PropertyBean;
import org.freeplane.core.ui.IndexedTree;
import org.freeplane.core.ui.OptionPanelButtonListener;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.mindmapmode.MModeController;

public class AccountManager implements IFreeplanePropertyListener, ActionListener {
	
	private static Hashtable<String, Account> accountList = new Hashtable<String, Account>();
	private static List<String> accountInitBuffer = new ArrayList<String>();
	private static boolean isInitialized = false;
	private boolean isWorking = false;
	private static String secretString;
	
	public AccountManager() {
		MModeController modeController = (MModeController) Controller.getCurrentModeController();
		OptionPanelButtonListener.addButtonListener(this);
		
		final URL preferences = this.getClass().getResource("preferences.xml");
		if (preferences == null)
			throw new RuntimeException("cannot open preferences");
				
		ResourceBundles resBundle = ((ResourceBundles)modeController.getController().getResourceController().getResources());
		
		String lang = resBundle.getLanguageCode();
		if (lang == null || lang.equals(ResourceBundles.LANGUAGE_AUTOMATIC)) {
			lang = "en";
		}
		
		final URL res = this.getClass().getResource("/translations/Resources_"+lang+".properties");
		resBundle.addResources(resBundle.getLanguageCode(), res);
		
		modeController.getOptionPanelBuilder().load(preferences);
		
		Controller.getCurrentController().getResourceController().addPropertyChangeListener(this);
		OptionPanelBuilder builder = modeController.getOptionPanelBuilder();
		builder.addTab("account_manager", "right:max(40dlu;p), 4dlu, 200dlu:grow, 7dlu", IndexedTree.PREPEND);
		isInitialized = true;
		buildOptionUI();
		secretString = "Docear 4 world domination!";
		
	}
	
	
	public static synchronized void registerAccount(Account account) {
		String accountName = account.getAccountName().replaceAll("/^[a-zA-Z0-9]+/i", "_");
		inititializeAccount(accountName, account);
		accountList.put(accountName, account);
		accountInitBuffer.add(accountName);
		buildOptionUI();
		LogUtils.info("Account ("+ accountName +") registered.");		
	}

	private static void inititializeAccount(String accountName, Account account) {
		inititializeAccount(accountName, account, Controller.getCurrentController().getResourceController().getProperties());
	}
	
	private static void inititializeAccount(String accountName, Account account, Properties rc) {
		// read username from properties
		String property = rc.getProperty(accountName + ".username", null);
		if(property != null)
			account.setUsername(property);
		
		// read connection info from properties
		property = rc.getProperty(accountName + ".connection_string", null);
		if(property != null)
			account.setConnectionString(property);
		
		// read encrypted password from properties
		property = rc.getProperty(accountName + ".password", null);
		if(property != null) {
			account.setPassword(property);
		}
	}
	
	private static synchronized void buildOptionUI() {
		if(isInitialized) {			
			for(String accountName : accountInitBuffer) {
				LogUtils.info("build OptionPanel entry for Account(" + accountName + ")");
				Account account = accountList.get(accountName);
				OptionPanelBuilder builder = ((MModeController) Controller.getCurrentModeController()).getOptionPanelBuilder();
				
				builder.addSeparator("account_manager", accountName, IndexedTree.AS_CHILD);
				builder.addStringProperty("account_manager/" + accountName, accountName+".username", IndexedTree.AS_CHILD);
				builder.addPasswordProperty("account_manager/" + accountName, accountName+".password", IndexedTree.AS_CHILD);
				builder.addStringProperty("account_manager/" + accountName, accountName+".connection_string", IndexedTree.AS_CHILD);
				if(account.wantsButtonAction()) {
					builder.addActionProperty("account_manager/" + accountName, account.getButtonText(), account.getButtonAction(), IndexedTree.AS_CHILD);
				}
			}
			accountInitBuffer.clear();
		}
	}

	public void propertyChanged(String propertyName, String newValue, String oldValue) {
		if(isWorking) return;
		isWorking = true;
		int offset = propertyName.indexOf('.');
		if( offset > -1) {
			String accountName = propertyName.substring(0, offset);
			LogUtils.info("Property ("+ accountName +") changed.");
			if(accountList.containsKey(accountName)) {				
				inititializeAccount(accountName, accountList.get(accountName));
				LogUtils.info("Data for Account ("+ accountName +") updated.");
			}
		}
		isWorking = false;
	}


	public void actionPerformed(ActionEvent e) {
		Properties props = Controller.getCurrentController().getOptionPanelController().getCurrentOptionProperties();
		for(String key : accountList.keySet()) {
			Account account = accountList.get(key);
			if (e.getActionCommand().equals(account.getButtonAction())) {
				String accountName = account.getAccountName().replaceAll("/^[a-zA-Z0-9]+/i", "_");
				inititializeAccount(accountName, account, props);				
			}
		}
	}
}
