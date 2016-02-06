package org.freeplane.core.util;

import java.awt.event.ActionEvent;
import javax.swing.Action;
import org.freeplane.core.ui.AFreeplaneAction;

public abstract class ActionUtils {


	public static String getActionTitle(final AFreeplaneAction action) {
		String title = (String)action.getValue(Action.NAME);
		
		if(title == null || title.isEmpty()) {
			title = TextUtils.getText(action.getTextKey());
		}
		if(title == null || title.isEmpty()) {
			title = action.getTextKey();
		}
		return TextUtils.removeTranslateComment(title);
	}

	public static AFreeplaneAction getDummyAction(final String key) {
		return new AFreeplaneAction(key) {
			private static final long serialVersionUID = -5405032373977903024L;
	
			public String getTextKey() {
				return getKey();
			}
			
			public void actionPerformed(ActionEvent e) {
				//do nothing
			}
		};
	}
	
	

}
