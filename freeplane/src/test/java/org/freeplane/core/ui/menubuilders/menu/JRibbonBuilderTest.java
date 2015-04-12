package org.freeplane.core.ui.menubuilders.menu;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.freeplane.core.ui.IUserInputListenerFactory;
import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.EntryAccessor;
import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.pushingpixels.flamingo.api.ribbon.JRibbon;

public class JRibbonBuilderTest {


	@Test
	public void createsEmptyJRibbonComponent() {
		Entry ribbonEntry = new Entry();
		final IUserInputListenerFactory userInputListenerFactory = mock(IUserInputListenerFactory.class);
		final JRibbon ribbon = new JRibbon();
		when(userInputListenerFactory.getRibbon()).thenReturn(ribbon);
		final JRibbonBuilder ribbonBuilder = new JRibbonBuilder(userInputListenerFactory);
		ribbonBuilder.visit(ribbonEntry);

		assertThat(new EntryAccessor().getComponent(ribbonEntry), CoreMatchers.<Object> is(ribbon));
	}
}
