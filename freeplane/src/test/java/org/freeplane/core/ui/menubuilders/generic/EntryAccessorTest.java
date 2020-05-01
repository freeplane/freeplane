package org.freeplane.core.ui.menubuilders.generic;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JSeparator;

import org.freeplane.core.ui.AFreeplaneAction;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class EntryAccessorTest {
	Entry entry;
	EntryAccessor entryAccessor;
	ResourceAccessor resourceAccessor;
	
	@Before
	public void setup(){
		resourceAccessor = mock(ResourceAccessor.class);
		entry = new Entry();
		entryAccessor = new EntryAccessor(resourceAccessor);
	}
	
	@Test
	public void getsTextFromEntryAttributeText() throws Exception {
		entry.setAttribute("text", "entry text");
		final String entryText = entryAccessor.getText(entry);
		Assert.assertThat(entryText, equalTo("entry text"));
	}
	
	@Test
	public void getsTextFromEntryAttributeTextKey() throws Exception {
		entry.setAttribute("textKey", "entry text key");
		when(resourceAccessor.getRawText("entry text key")).thenReturn("entry text");
		final String entryText = entryAccessor.getText(entry);
		Assert.assertThat(entryText, equalTo("entry text"));
	}

	@Test
	public void getsIconFromEntryAttributeIcon() throws Exception {
		final Icon icon = new ImageIcon();
		entry.setAttribute(EntryAccessor.ICON_INSTANCE, icon);
		final Icon entryIcon = entryAccessor.getIcon(entry);
		Assert.assertThat(entryIcon, equalTo(icon));
	}

	@Test
	public void setsTextToEntryAttributeText() throws Exception {
		entryAccessor.setText(entry, "entry text");
		final String entryText = entryAccessor.getText(entry);
		Assert.assertThat(entryText, equalTo("entry text"));
	}

	@Test
	public void setsIconToEntryAttributeIcon() throws Exception {
		final Icon icon = new ImageIcon();
		entryAccessor.setIcon(entry, icon);
		final Icon entryIcon = entryAccessor.getIcon(entry);
		Assert.assertThat(entryIcon, equalTo(icon));
	}

	@Test
	public void setsAction() throws Exception {
		final AFreeplaneAction action = mock(AFreeplaneAction.class);
		entryAccessor.setAction(entry, action);
		final AFreeplaneAction entryAction = entryAccessor.getAction(entry);
		Assert.assertThat(entryAction, equalTo(action));
	}

	@Test
	public void addsChildAction() throws Exception {
		final AFreeplaneAction action = mock(AFreeplaneAction.class);
		when(action.getKey()).thenReturn("key");
		entryAccessor.addChildAction(entry, action);
		final Entry actionEntry = entry.getChild(0);
		final AFreeplaneAction entryAction = entryAccessor.getAction(actionEntry);
		Assert.assertThat(actionEntry.getName(), equalTo("key"));
		Assert.assertThat(entryAction, equalTo(action));
	}

	@Test
	public void givenEntryWithoutText_returnsEmtpyLocationDescription() throws Exception {
		entryAccessor.setComponent(entry, new JSeparator());
		
		final String entryText = entryAccessor.getLocationDescription(entry);
		Assert.assertThat(entryText, equalTo(""));
	}

	@Test
	public void givenEntryWithTextAndComponent_returnsNonEmtpyLocationDescription() throws Exception {
		String text = "entry text";
		entryAccessor.setText(entry, text);
		entryAccessor.setComponent(entry, new JSeparator());
		
		final String entryText = entryAccessor.getLocationDescription(entry);
		Assert.assertThat(entryText, equalTo(text));
	}

	@Test
	public void givenEntryWithParent_joinesTheirDescritionsSeparatedByArrow() throws Exception {
		entryAccessor.setText(entry, "entry");
		entryAccessor.setComponent(entry, new JSeparator());

		Entry parent = new Entry();
		parent.addChild(entry);
		entryAccessor.setText(parent, "parent");
		entryAccessor.setComponent(parent, new JSeparator());

		final String entryText = entryAccessor.getLocationDescription(entry);
		Assert.assertThat(entryText, equalTo("parent -> entry"));
	}
}
