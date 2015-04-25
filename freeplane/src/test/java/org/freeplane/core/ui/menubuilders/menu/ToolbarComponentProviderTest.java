package org.freeplane.core.ui.menubuilders.menu;

import static org.hamcrest.CoreMatchers.equalTo;

import javax.swing.JPanel;

import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.EntryAccessor;
import org.junit.Assert;
import org.junit.Test;

public class ToolbarComponentProviderTest {
	@Test
	public void testName() throws Exception {
		final ToolbarComponentProvider toolbarComponentProvider = new ToolbarComponentProvider();
		final Entry entry = new Entry();
		final EntryAccessor entryAccessor = new EntryAccessor();
		final Object testComponent = new JPanel();
		entryAccessor.setComponent(entry, testComponent);
		Assert.assertThat(toolbarComponentProvider.createComponent(entry), equalTo(testComponent));
	}
}
