package org.freeplane.core.user;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.freeplane.core.extension.IExtension;
import org.freeplane.features.mode.Controller;

public class UserAccountController implements IExtension {
	
	private IUserAccount activeUser;
	
	
	public static void install(Controller controller) {
		controller.addExtension(UserAccountController.class, new UserAccountController());
	}
	
	public static UserAccountController getController() {
		return Controller.getCurrentController().getExtension(UserAccountController.class);
	}
	
	public void setActiveUser(IUserAccount user) {
		this.activeUser = user;
	}
	
	public IUserAccount getActiveUser() {
		return this.activeUser;
	}
	
	public List<IUserAccount> getUsers() {
		if(activeUser == null) {
			return Collections.emptyList();
		}
		return Arrays.asList(new IUserAccount[]{activeUser});
	}
}
