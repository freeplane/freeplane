package org.freeplane.core.ui.menubuilders;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.features.mode.FreeplaneActions;
import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.mockito.Mockito;

public class ActionFinderTest {

	@Test
	public void createsToolbarButtonWithAction() {
		FreeplaneActions freeplaneActions = mock(FreeplaneActions.class);
		Entry entry = new Entry();
		entry.setAttribute("action", "action");
		final AFreeplaneAction someAction = Mockito.mock(AFreeplaneAction.class);
		when(freeplaneActions.getAction("action")).thenReturn(someAction);
		
		final ActionFinder actionFinder = new ActionFinder(freeplaneActions);
		actionFinder.build(entry);
		
		assertThat(entry.getAction(), CoreMatchers.equalTo(someAction));
	}

}
