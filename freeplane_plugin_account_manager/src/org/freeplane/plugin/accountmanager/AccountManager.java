package org.freeplane.plugin.accountmanager;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Hashtable;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

import org.freeplane.core.resources.IFreeplanePropertyListener;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.mindmapmode.MModeController;

public class AccountManager implements IFreeplanePropertyListener {
	
	private static Hashtable<String, Account> accountList = new Hashtable<String, Account>();
	
	public AccountManager() {
		Controller.getCurrentController().getResourceController().addPropertyChangeListener(this);
		LogUtils.info("AccountManager started.");
	}
	
	
	public static void registerAccount(Account account) {
		String accountName = account.getAccountName().replaceAll("/^[a-zA-Z0-9]+/i", "_");
		inititializeAccount(accountName, account);
		accountList.put(accountName, account);
		buildOptionUI(accountName, account);
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
			try {
			
				DESKeySpec keySpec = new DESKeySpec("Docear 4 world domination!".getBytes("UTF-8"));
				SecretKey key = SecretKeyFactory.getInstance("DES").generateSecret(keySpec);
				
				Cipher cipher = Cipher.getInstance("DES");
				cipher.init(Cipher.DECRYPT_MODE, key);
				
				account.setPassword(ByteBuffer.wrap(cipher.doFinal(property.getBytes("UTF-8"))).asCharBuffer().toString());
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
	}
	
	private static void saveAccount(String accountName, Account account) {
		ResourceController rc = Controller.getCurrentController().getResourceController();
		
		// write username to properties
		if(account.hasUsername())
			rc.setProperty(accountName + ".username", account.getUsername());
		else
			rc.setProperty(accountName + ".username", null);
		
		// write encrypted password to properties
		if(account.hasPassword()) {	
			try {				
				DESKeySpec keySpec = new DESKeySpec("Docear 4 world domination!".getBytes("UTF-8"));
				SecretKey key = SecretKeyFactory.getInstance("DES").generateSecret(keySpec);
				
				Cipher cipher = Cipher.getInstance("DES");
				cipher.init(Cipher.ENCRYPT_MODE, key);
				
				rc.setProperty(accountName + ".password", ByteBuffer.wrap(cipher.doFinal(account.getPassword().getBytes("UTF-8"))).asCharBuffer().toString()); 
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
		
		// write connection string to properties 
		if(account.hasConnectionString())
			rc.setProperty(accountName + ".connection_string", account.getConnectionString());
		else
			rc.setProperty(accountName + ".connection_string", null);
		
		// store properties
		rc.saveProperties();
	}

	private static void buildOptionUI(String accountName, Account account) {
		MModeController modeController = (MModeController) Controller.getCurrentModeController();
		modeController.getOptionPanelBuilder().addSeparator("docear/test_sep", account.getAccountName(), MenuBuilder.AS_CHILD);	
	}

	public void propertyChanged(String propertyName, String newValue, String oldValue) {
		int offset = propertyName.indexOf('.');
		if( offset > -1) {
			String accountName = propertyName.substring(0, offset);
			LogUtils.info("Property ("+ accountName +") changed.");
			if(accountList.containsKey(accountName)) {
				saveAccount(accountName, accountList.get(accountName));				
				inititializeAccount(accountName, accountList.get(accountName));
				LogUtils.info("Data for Account ("+ accountName +") updated.");
			}
		}
	}
	
	

}
