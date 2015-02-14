package org.freeplane.core.ui.menubuilders;

import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.freeplane.core.resources.SetBooleanPropertyAction;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.features.mode.FreeplaneActions;

public class ActionFinder implements Builder{

	final private FreeplaneActions freeplaneActions;

	public ActionFinder(FreeplaneActions freeplaneActions) {
		this.freeplaneActions = freeplaneActions;
	}

	@Override
	public void build(final Entry target) {
		final String actionName = target.getName();
		if(actionName != null) {
			AFreeplaneAction action = freeplaneActions.getAction(actionName);
			final String setBooleanPropertyActionPrefix = SetBooleanPropertyAction.class.getSimpleName() + ".";
			if(action == null && actionName.startsWith(setBooleanPropertyActionPrefix)){
				String propertyName = actionName.substring(setBooleanPropertyActionPrefix.length());
				action = createSetBooleanPropertyAction(propertyName);
				freeplaneActions.addAction(action);
			}
			
			new EntryPopupMenuListenerAccessor(target).addPopupMenuListener(new PopupMenuListener() {
				public void popupMenuCanceled(final PopupMenuEvent e) {
				}

				public void popupMenuWillBecomeInvisible(final PopupMenuEvent e) {
				}

				public void popupMenuWillBecomeVisible(final PopupMenuEvent e) {
					final AFreeplaneAction action = target.getAction();
					if(action.isEnabled())
						action.setSelected();
				}
			});

			target.setAction(action);
		}
	}

	protected SetBooleanPropertyAction createSetBooleanPropertyAction(
			String propertyName) {
		return new SetBooleanPropertyAction(propertyName);
	}

}
