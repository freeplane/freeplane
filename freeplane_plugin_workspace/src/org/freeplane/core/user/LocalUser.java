package org.freeplane.core.user;

public class LocalUser implements IUserAccount {

	private final String name;
	private boolean enabled = true;

	public LocalUser(String username) {
		this.name = username;
	}

	public String getName() {
		return this.name;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public boolean isActive() {
		return this.equals(UserAccountController.getController().getActiveUser());
	}

	public void activate() {
		UserAccountController.getController().setActiveUser(this);
	}

	public void setEnabled(boolean enabled) {
		this.enabled  = enabled;
	}
	
	public String toString() {
		return "LocalUser["+getName()+(isActive() ? ";active":"")+"]";
	}

}
