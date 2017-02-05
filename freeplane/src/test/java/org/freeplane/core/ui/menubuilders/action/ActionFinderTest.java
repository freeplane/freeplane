package org.freeplane.core.ui.menubuilders.action;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.freeplane.core.resources.SetBooleanPropertyAction;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.EntryAccessor;
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
		actionFinder.visit(entry);
		
		assertThat((AFreeplaneAction) new EntryAccessor().getAction(entry), CoreMatchers.equalTo(someAction));
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
			protected AFreeplaneAction createAction(Class<? extends AFreeplaneAction> actionClass,
					String propertyName) {
				return setBooleanPropertyAction;
			}
			
		};
		actionFinder.visit(entry);
		
		Mockito.verify(freeplaneActions).addAction(setBooleanPropertyAction);
		assertThat(new EntryAccessor().getAction(entry), CoreMatchers.<Object> equalTo(setBooleanPropertyAction));
	}

}
