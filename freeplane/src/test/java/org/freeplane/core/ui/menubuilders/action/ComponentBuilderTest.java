package org.freeplane.core.ui.menubuilders.action;

import static org.hamcrest.MatcherAssert.assertThat;
import java.awt.Component;

import javax.swing.JPanel;

import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.EntryAccessor;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

public class ComponentBuilderTest {
	@Test
	public void testName() throws Exception {
		final Entry entry = new Entry();
		final Component testComponent = new JPanel();
		final ComponentBuilder builder = new ComponentBuilder(entry1 -> testComponent);
		builder.visit(entry);
		assertThat(new EntryAccessor().getComponent(entry), CoreMatchers.<Object> equalTo(testComponent));
	}
}
