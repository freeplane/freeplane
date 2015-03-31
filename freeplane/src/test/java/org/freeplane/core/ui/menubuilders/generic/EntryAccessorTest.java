package org.freeplane.core.ui.menubuilders.generic;

import static org.hamcrest.CoreMatchers.equalTo;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.EntryAccessor;
import org.junit.Assert;
import org.junit.Test;

public class EntryAccessorTest {
	@Test
	public void getsTextFromEntryAttributeText() throws Exception {
		final Entry entry = new Entry();
		entry.setAttribute("text", "entry text");
		final EntryAccessor entryAccessor = new EntryAccessor();
		final String entryText = entryAccessor.getText(entry);
		Assert.assertThat(entryText, equalTo("entry text"));
	}

	@Test
	public void getsIconFromEntryAttributeIcon() throws Exception {
		final Entry entry = new Entry();
		final Icon icon = new ImageIcon();
		entry.setAttribute("icon", icon);
		final EntryAccessor entryAccessor = new EntryAccessor();
		final Icon entryIcon = entryAccessor.getIcon(entry);
		Assert.assertThat(entryIcon, equalTo(icon));
	}
}
