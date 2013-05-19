package org.freeplane.core.user;

public interface IUserAccount {
	public String getName();
	public boolean isEnabled();
	public boolean isActive();
	public void activate();
	public void setEnabled(boolean enabled);
}
