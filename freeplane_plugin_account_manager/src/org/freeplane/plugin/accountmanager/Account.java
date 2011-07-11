package org.freeplane.plugin.accountmanager;

public abstract class Account {
	private String username = null;
	private String password = null;
	private String connectionString = null;
	
	public abstract String getAccountName();
	
	
	
	public String getUsername() {
		return username;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public boolean hasUsername() {
		return this.username==null;
	}
	
	
	public String getPassword() {
		return password;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
	public boolean hasPassword() {
		return this.password==null;
	}
	
	
	public String getConnectionString() {
		return connectionString;
	}
	
	public void setConnectionString(String connectionString) {
		this.connectionString = connectionString;
	}	
	
	public boolean hasConnectionString() {
		return this.connectionString==null;
	}
	
	public boolean hasVisibleProperties() {
		return this.hasConnectionString()||this.hasPassword()||this.hasUsername();
	}
	
	 
	
}
