package org.freeplane.core.ui.menubuilders.ribbon;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import org.freeplane.core.ui.IUserInputListenerFactory;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.EntryAccessor;
import org.freeplane.core.ui.menubuilders.ribbon.JRibbonBuilder;
import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.pushingpixels.flamingo.api.ribbon.JRibbonFrame;

public class JRibbonBuilderTest {


	@Test
	public void createsEmptyJRibbonComponent() {
		Entry ribbonEntry = new Entry();
		final IUserInputListenerFactory userInputListenerFactory = mock(IUserInputListenerFactory.class);
		final JRibbonFrame jrframe = new JRibbonFrame();
		jrframe.setName(UITools.MAIN_FREEPLANE_FRAME);
		final JRibbonBuilder ribbonBuilder = new JRibbonBuilder(userInputListenerFactory);
		ribbonBuilder.visit(ribbonEntry);

		assertThat(new EntryAccessor().getComponent(ribbonEntry), CoreMatchers.<Object> is(jrframe.getRibbon()));
	}
}
