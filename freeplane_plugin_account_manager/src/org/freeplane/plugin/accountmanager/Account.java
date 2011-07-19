package org.freeplane.plugin.accountmanager;

import java.security.InvalidParameterException;

public abstract class Account {
	private String username = null;
	private String password = null;
	private String connectionString = null;
	private String buttonText = null;
	private String buttonAction = null;
	
	public abstract String getAccountName();
	
	
	
	public String getUsername() {
		return username;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public boolean hasUsername() {
		return this.username!=null;
	}
	
	
	public String getPassword() {
		return password;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
	public boolean hasPassword() {
		return this.password!=null;
	}
	
	
	public String getConnectionString() {
		return connectionString;
	}
	
	public void setConnectionString(String connectionString) {
		this.connectionString = connectionString;
	}	
	
	public boolean hasConnectionString() {
		return this.connectionString!=null;
	}
	
	public boolean hasVisibleProperties() {
		return this.hasConnectionString()||this.hasPassword()||this.hasUsername();
	}
	
	public String getButtonText() {
		return this.buttonText ;
	}
	
	public String getButtonAction() {
		return this.buttonAction ;
	}
	
	public boolean wantsButtonAction() {
		return (this.getButtonText()!=null)&&(this.getButtonAction()!=null);
	}
	
	public void enableButton(final String buttonText, final String buttonAction) {
		if(buttonText == null) throw new InvalidParameterException("First parameter cannot be NULL!");
		if(buttonAction == null) throw new InvalidParameterException("Second parameter cannot be NULL!");
		this.buttonText = buttonText;
		this.buttonAction = buttonAction;
	}
	
	public String toString() {
		return "Account[name="+this.getAccountName()+";username="+this.getUsername()+";password="+this.getPassword()+";buttonText="+this.getButtonText()+";buttonAction="+this.getButtonAction()+"]\n";
	}
	
}
