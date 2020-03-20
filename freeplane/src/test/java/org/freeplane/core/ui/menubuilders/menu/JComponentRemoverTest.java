package org.freeplane.core.ui.menubuilders.menu;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;

import java.awt.Container;

import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JPanel;

import org.freeplane.core.ui.MenuSplitter;
import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.EntryAccessor;
import org.junit.Assert;
import org.junit.Test;


public class JComponentRemoverTest {
	@Test
	public void removesComponentsFromParents() throws Exception {
		final JComponentRemover componentRemover = JComponentRemover.INSTANCE;
		final Entry entry = new Entry();
		JComponent parent = new JPanel();
		JComponent entryComponent = new JPanel();
		parent.add(entryComponent);
		new EntryAccessor().setComponent(entry, entryComponent);
		componentRemover.visit(entry);
		
		Assert.assertThat(entryComponent.getParent(), nullValue(Container.class));
	}

	@Test
	public void ignoresEntriesWithoutComponents() throws Exception {
		final JComponentRemover componentRemover = JComponentRemover.INSTANCE;
		final Entry entry = new Entry();
		componentRemover.visit(entry);
	}

	@Test
	public void removesExtraSubmenusFromParents() throws Exception {
		final JComponentRemover componentRemover = JComponentRemover.INSTANCE;
		final Entry entry = new Entry();
		JMenu parent = new JMenu();
		JComponent entryComponent = new JMenu();
		final MenuSplitter menuSplitter = new MenuSplitter(1);
		menuSplitter.addMenuComponent(parent, new JMenu());
		menuSplitter.addMenuComponent(parent, entryComponent);
		new EntryAccessor().setComponent(entry, entryComponent);
		componentRemover.visit(entry);
		Assert.assertThat(parent.getPopupMenu().getComponentCount(), equalTo(1));
	}
}
