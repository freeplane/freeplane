package org.freeplane.core.ui.menubuilders;

import static java.util.Arrays.asList;

import org.junit.Test;
import org.mockito.Mockito;

public class RecursiveMenuStructureBuilderTest {

	@Test
	public void explicitBuilderIsCalled() {
		final RecursiveMenuStructureBuilder combinedMenuStructureBuilder = new RecursiveMenuStructureBuilder();
		Builder builder = Mockito.mock(Builder.class);
		combinedMenuStructureBuilder.addBuilder("builder", builder);
		final Entry childEntry = new Entry();
		childEntry.setBuilders(asList("builder"));
		combinedMenuStructureBuilder.build(childEntry);
		
		Mockito.verify(builder).build(childEntry);
	}


	@Test
	public void defaultBuilderIsCalledForChild() {
		final RecursiveMenuStructureBuilder combinedMenuStructureBuilder = new RecursiveMenuStructureBuilder();
		Builder defaultBuilder = Mockito.mock(Builder.class);
		combinedMenuStructureBuilder.addBuilder("builder", Builder.EMTPY_BUILDER);
		combinedMenuStructureBuilder.addSubtreeDefaultBuilder("builder", defaultBuilder);
		final Entry entry = new Entry();
		entry.setBuilders(asList("builder"));
		final Entry childEntry = new Entry();
		entry.addChild(childEntry);
		
		combinedMenuStructureBuilder.build(entry);
		
		Mockito.verify(defaultBuilder).build(childEntry);
	}

	@Test
	public void defaultBuilderIsRestoredAfterChildCall() {
		final RecursiveMenuStructureBuilder combinedMenuStructureBuilder = new RecursiveMenuStructureBuilder();
		combinedMenuStructureBuilder.addBuilder("builder1", Builder.EMTPY_BUILDER);
		Builder defaultBuilder = Mockito.mock(Builder.class);
		combinedMenuStructureBuilder.addSubtreeDefaultBuilder("builder1", defaultBuilder);
		combinedMenuStructureBuilder.addBuilder("builder2", Builder.EMTPY_BUILDER);
		combinedMenuStructureBuilder.addSubtreeDefaultBuilder("builder2", Builder.EMTPY_BUILDER);

		final Entry entry = new Entry();
		entry.setBuilders(asList("builder1"));
		final Entry childEntry1 = new Entry();
		childEntry1.setBuilders(asList("builder2"));
		entry.addChild(childEntry1);
		
		final Entry childEntry2 = new Entry();
		entry.addChild(childEntry2);

		combinedMenuStructureBuilder.build(entry);
		
		Mockito.verify(defaultBuilder).build(childEntry2);
	}


	@Test(expected = IllegalStateException.class)
	public void defaultBuilderIsNotSetException() {
		final RecursiveMenuStructureBuilder combinedMenuStructureBuilder = new RecursiveMenuStructureBuilder();
		final Entry childEntry = new Entry();
		combinedMenuStructureBuilder.build(childEntry);
		
	}
}
