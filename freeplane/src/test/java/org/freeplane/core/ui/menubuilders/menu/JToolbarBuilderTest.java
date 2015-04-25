package org.freeplane.core.ui.menubuilders.menu;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.swing.JToolBar;

import org.freeplane.core.ui.IUserInputListenerFactory;
import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.EntryAccessor;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

public class JToolbarBuilderTest {


	@Test
	public void createsEmptyToolbarComponent() {
		Entry toolbarEntry = new Entry();
		
		final IUserInputListenerFactory userInputListenerFactory = mock(IUserInputListenerFactory.class);
		JToolBar toolbar = new JToolBar();
		when(userInputListenerFactory.getToolBar("/main_toolbar")).thenReturn(toolbar);		
		final JToolbarBuilder toolbarBuilder = new JToolbarBuilder(userInputListenerFactory);
		toolbarBuilder.visit(toolbarEntry);

		assertThat(new EntryAccessor().getComponent(toolbarEntry), CoreMatchers.<Object>is(toolbar));
	}
}
