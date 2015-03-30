package org.freeplane.core.ui.menubuilders.menu;

import static org.hamcrest.CoreMatchers.nullValue;

import java.awt.Container;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.junit.Assert;
import org.junit.Test;


public class JComponentRemoverTest {
	@Test
	public void removesComponentsFromParents() throws Exception {
		final JComponentRemover componentRemover = new JComponentRemover();
		final Entry entry = new Entry();
		JComponent parent = new JPanel();
		JComponent entryComponent = new JPanel();
		parent.add(entryComponent);
		entry.setComponent(entryComponent);
		componentRemover.visit(entry);
		
		Assert.assertThat(entryComponent.getParent(), nullValue(Container.class));
	}

	@Test
	public void ignoresEntriesWithoutComponents() throws Exception {
		final JComponentRemover componentRemover = new JComponentRemover();
		final Entry entry = new Entry();
		componentRemover.visit(entry);
	}
}
