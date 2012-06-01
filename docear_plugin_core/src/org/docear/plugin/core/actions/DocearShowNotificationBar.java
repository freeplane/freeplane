package org.docear.plugin.core.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.docear.plugin.core.ui.NotificationBar;
import org.freeplane.core.ui.AFreeplaneAction;

public class DocearShowNotificationBar extends AFreeplaneAction {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final String key = "blabla";
	
	public DocearShowNotificationBar() {
		super(key);
		// TODO Auto-generated constructor stub
	}

	public void actionPerformed(ActionEvent e) {
		NotificationBar.showNotificationBar("Und noch ein Test....", "Update now", new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				System.out.println("Ok ich update jetzt.....");				
			}
		});
		
		NotificationBar.showNotificationBar("Zweite Message gaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaanz langes bla bla....", "Convert", new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				System.out.println("Ich tu was anderes.....");				
			}
		});
	}

}
