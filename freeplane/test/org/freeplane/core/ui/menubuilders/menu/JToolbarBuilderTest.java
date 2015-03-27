package org.freeplane.core.ui.menubuilders.menu;

import static org.junit.Assert.assertThat;

import org.freeplane.core.ui.components.FreeplaneToolBar;
import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.menu.JToolbarBuilder;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

public class JToolbarBuilderTest {


	@Test
	public void createsEmptyToolbarComponent() {
		Entry toolbarEntry = new Entry();
		
		final JToolbarBuilder toolbarBuilder = new JToolbarBuilder();
		toolbarBuilder.visit(toolbarEntry);

		assertThat(toolbarEntry.getComponent().getClass(), CoreMatchers.<Object>is(FreeplaneToolBar.class));
	}
}
