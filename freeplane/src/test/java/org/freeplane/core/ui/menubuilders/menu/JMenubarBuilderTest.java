package org.freeplane.core.ui.menubuilders.menu;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.freeplane.core.ui.IUserInputListenerFactory;
import org.freeplane.core.ui.components.FreeplaneMenuBar;
import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.EntryAccessor;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

public class JMenubarBuilderTest {


	@Test
	public void createsEmptyToolbarComponent() {
		Entry toolbarEntry = new Entry();
		final IUserInputListenerFactory userInputListenerFactory = mock(IUserInputListenerFactory.class);
		final FreeplaneMenuBar menubar = TestMenuBarFactory.createFreeplaneMenuBar();
		when(userInputListenerFactory.getMenuBar()).thenReturn(menubar);
		final JMenubarBuilder toolbarBuilder = new JMenubarBuilder(userInputListenerFactory);
		toolbarBuilder.visit(toolbarEntry);

		assertThat(new EntryAccessor().getComponent(toolbarEntry), CoreMatchers.<Object> is(menubar));
	}
}
