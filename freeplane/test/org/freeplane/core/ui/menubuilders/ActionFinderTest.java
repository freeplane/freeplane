package org.freeplane.core.ui.menubuilders;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.freeplane.core.resources.SetBooleanPropertyAction;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.features.mode.FreeplaneActions;
import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.mockito.Mockito;

public class ActionFinderTest {

	@Test
	public void attachesExistingFreeplaneAction() {
		FreeplaneActions freeplaneActions = mock(FreeplaneActions.class);
		Entry entry = new Entry();
		entry.setName("action");
		final AFreeplaneAction someAction = Mockito.mock(AFreeplaneAction.class);
		when(freeplaneActions.getAction("action")).thenReturn(someAction);
		
		final ActionFinder actionFinder = new ActionFinder(freeplaneActions);
		actionFinder.build(entry);
		
		assertThat((AFreeplaneAction) entry.getAttribute(Entry.ACTION), CoreMatchers.equalTo(someAction));
	}

	@Test
	public void attachesSetBooleanPropertyAction() {
		FreeplaneActions freeplaneActions = mock(FreeplaneActions.class);
		final SetBooleanPropertyAction setBooleanPropertyAction = Mockito.mock(SetBooleanPropertyAction.class);
		Entry entry = new Entry();
		final String propertyActionName = "SetBooleanPropertyAction.property";
		entry.setName(propertyActionName);
		when(freeplaneActions.getAction(propertyActionName)).thenReturn(null);
		
		final ActionFinder actionFinder = new ActionFinder(freeplaneActions){
			@Override
			protected SetBooleanPropertyAction createSetBooleanPropertyAction(
					String propertyName) {
				return setBooleanPropertyAction;
			}
			
		};
		actionFinder.build(entry);
		
		Mockito.verify(freeplaneActions).addAction(setBooleanPropertyAction);
		assertThat(entry.getAttribute(Entry.ACTION), CoreMatchers.<Object>equalTo(setBooleanPropertyAction));
	}


	@Test
	public void activatesSelectOnPopup() {
		FreeplaneActions freeplaneActions = mock(FreeplaneActions.class);
		Entry entry = new Entry();
		entry.setName("action");
		final AFreeplaneAction someAction = Mockito.mock(AFreeplaneAction.class);
		when(freeplaneActions.getAction("action")).thenReturn(someAction);
		when(someAction.checkSelectionOnPopup()).thenReturn(true);
		when(someAction.isEnabled()).thenReturn(true);
		
		final ActionFinder actionFinder = new ActionFinder(freeplaneActions);
		actionFinder.build(entry);
		
		new EntryPopupMenuListenerAccessor(entry).popupMenuWillBecomeVisible();
		
		verify(someAction).setSelected();
	}

}
