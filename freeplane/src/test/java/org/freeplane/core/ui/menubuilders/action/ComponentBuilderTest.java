package org.freeplane.core.ui.menubuilders.action;

import java.awt.Component;

import javax.swing.JPanel;

import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.EntryAccessor;
import org.freeplane.core.ui.menubuilders.menu.ComponentProvider;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

public class ComponentBuilderTest {
	@Test
	public void testName() throws Exception {
		final Entry entry = new Entry();
		final Component testComponent = new JPanel();
		final ComponentBuilder builder = new ComponentBuilder(new ComponentProvider() {
			@Override
			public Component createComponent(Entry entry) {
				return testComponent;
			}
		});
		builder.visit(entry);
		Assert.assertThat(new EntryAccessor().getComponent(entry), CoreMatchers.<Object> equalTo(testComponent));
	}
}
