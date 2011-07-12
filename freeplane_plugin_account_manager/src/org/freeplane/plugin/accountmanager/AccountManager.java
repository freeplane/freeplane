package org.freeplane.plugin.accountmanager;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

import org.freeplane.core.resources.IFreeplanePropertyListener;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.resources.components.OptionPanelBuilder;
import org.freeplane.core.resources.components.TabProperty;
import org.freeplane.core.ui.IndexedTree;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.mindmapmode.MModeController;

public class AccountManager implements IFreeplanePropertyListener {
	
	private static Hashtable<String, Account> accountList = new Hashtable<String, Account>();
	private static List<String> accountInitBuffer = new ArrayList<String>();
	private static boolean isInitialized = false;
	private boolean isWorking = false;
	private static String secretString;
	
	public AccountManager() {
		LogUtils.info("AccountManager started.");
		Controller.getCurrentController().getResourceController().addPropertyChangeListener(this);
		OptionPanelBuilder builder = ((MModeController) Controller.getCurrentModeController()).getOptionPanelBuilder();
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
		ResourceController rc = Controller.getCurrentController().getResourceController();
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
//			try {
//			
//				DESKeySpec keySpec = new DESKeySpec(secretString.getBytes("UTF-8"));
//				SecretKey key = SecretKeyFactory.getInstance("DES").generateSecret(keySpec);
//				
//				Cipher cipher = Cipher.getInstance("DES");
//				cipher.init(Cipher.DECRYPT_MODE, key);
//				
//				account.setPassword(ByteBuffer.wrap(cipher.doFinal(property.getBytes("UTF-8"))).asCharBuffer().toString());
//				
//			} catch (NoSuchAlgorithmException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (InvalidKeyException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (UnsupportedEncodingException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (InvalidKeySpecException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (NoSuchPaddingException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (IllegalBlockSizeException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (BadPaddingException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		}
	}
	
	private static void encryptPassword(String accountName) {
		ResourceController rc = Controller.getCurrentController().getResourceController();
				
		// write encrypted password to properties
		if(rc.getProperty(accountName + ".password", null) != null) {	
			try {				
				DESKeySpec keySpec = new DESKeySpec(secretString.getBytes("UTF-8"));
				SecretKey key = SecretKeyFactory.getInstance("DES").generateSecret(keySpec);
				
				Cipher cipher = Cipher.getInstance("DES");
				cipher.init(Cipher.ENCRYPT_MODE, key);
				
				rc.setProperty(accountName + ".password", new String(ByteBuffer.wrap(cipher.doFinal(rc.getProperty(accountName + ".password").getBytes("UTF-8"))).asCharBuffer().array())); 
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvalidKeyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvalidKeySpecException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchPaddingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalBlockSizeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (BadPaddingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}
		else
			rc.setProperty(accountName + ".password", null);
		
		
		// store properties
		//rc.saveProperties();
	}

	private static synchronized void buildOptionUI() {
		if(isInitialized) {			
			for(String accountName : accountInitBuffer) {
				LogUtils.info("build OptionPanel entry for Account(" + accountName + ")");
				OptionPanelBuilder builder = ((MModeController) Controller.getCurrentModeController()).getOptionPanelBuilder();
				
				builder.addSeparator("account_manager", accountName, IndexedTree.AS_CHILD);
				builder.addStringProperty("account_manager/" + accountName, accountName+".username", IndexedTree.AS_CHILD);
				builder.addPasswordProperty("account_manager/" + accountName, accountName+".password", IndexedTree.AS_CHILD);
				builder.addStringProperty("account_manager/" + accountName, accountName+".connection_string", IndexedTree.AS_CHILD);
				builder.addActionProperty("account_manager/" + accountName, "account_validate", IndexedTree.AS_CHILD);
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
//				if(propertyName.equals(accountName + ".password"))				
//					encryptPassword(accountName);
				inititializeAccount(accountName, accountList.get(accountName));
				LogUtils.info("Data for Account ("+ accountName +") updated.");
			}
		}
		isWorking = false;
	}
	
	

}
