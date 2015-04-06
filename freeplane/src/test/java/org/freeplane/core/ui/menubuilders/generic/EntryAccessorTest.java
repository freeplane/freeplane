package org.freeplane.core.ui.menubuilders.generic;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.Mockito.mock;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.freeplane.core.ui.AFreeplaneAction;
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

	@Test
	public void setsTextToEntryAttributeText() throws Exception {
		final Entry entry = new Entry();
		final EntryAccessor entryAccessor = new EntryAccessor();
		entryAccessor.setText(entry, "entry text");
		final String entryText = entryAccessor.getText(entry);
		Assert.assertThat(entryText, equalTo("entry text"));
	}

	@Test
	public void setsIconToEntryAttributeIcon() throws Exception {
		final Entry entry = new Entry();
		final EntryAccessor entryAccessor = new EntryAccessor();
		final Icon icon = new ImageIcon();
		entryAccessor.setIcon(entry, icon);
		final Icon entryIcon = entryAccessor.getIcon(entry);
		Assert.assertThat(entryIcon, equalTo(icon));
	}

	@Test
	public void setsAction() throws Exception {
		final Entry entry = new Entry();
		final EntryAccessor entryAccessor = new EntryAccessor();
		final AFreeplaneAction action = mock(AFreeplaneAction.class);
		entryAccessor.setAction(entry, action);
		final AFreeplaneAction entryAction = entryAccessor.getAction(entry);
		Assert.assertThat(entryAction, equalTo(action));
	}

	@Test
	public void addsChildAction() throws Exception {
		final Entry entry = new Entry();
		final EntryAccessor entryAccessor = new EntryAccessor();
		final AFreeplaneAction action = mock(AFreeplaneAction.class);
		entryAccessor.addChildAction(entry, action);
		final AFreeplaneAction entryAction = entryAccessor.getAction(entry.getChild(0));
		Assert.assertThat(entryAction, equalTo(action));
	}
}
