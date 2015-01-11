package org.freeplane.core.ui.menubuilders;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.mockito.Mockito;

public class RecursiveMenuStructureBuilderTest {

	@Test
	public void explicitBuilderIsCalled() {
		final RecursiveMenuStructureBuilder recursiveMenuStructureBuilder = new RecursiveMenuStructureBuilder();
		Builder builder = Mockito.mock(Builder.class);
		recursiveMenuStructureBuilder.addBuilder("builder", builder);
		final Entry entry = new Entry();
		entry.setBuilders(asList("builder"));
		recursiveMenuStructureBuilder.build(entry);
		
		verify(builder).build(entry);
	}

	@Test
	public void defaultBuilderIsCalled() {
		final RecursiveMenuStructureBuilder combinedMenuStructureBuilder = new RecursiveMenuStructureBuilder();
		Builder builder = Mockito.mock(Builder.class);
		combinedMenuStructureBuilder.setDefaultBuilder(builder);
		final Entry entry = new Entry();
		combinedMenuStructureBuilder.build(entry);
		verify(builder).build(entry);
	}


	@Test
	public void defaultBuilderIsCalledForChild() {
		final RecursiveMenuStructureBuilder combinedMenuStructureBuilder = new RecursiveMenuStructureBuilder();
		Builder defaultBuilder = Mockito.mock(Builder.class);
		combinedMenuStructureBuilder.addBuilder("builder", Builder.EMTPY_BUILDER);
		combinedMenuStructureBuilder.addBuilder("defaultBuilder", defaultBuilder);
		combinedMenuStructureBuilder.addSubtreeDefaultBuilder("builder", "defaultBuilder");
		final Entry entry = new Entry();
		entry.setBuilders(asList("builder"));
		final Entry childEntry = new Entry();
		entry.addChild(childEntry);
		
		combinedMenuStructureBuilder.build(entry);
		
		verify(defaultBuilder).build(childEntry);
	}

	@Test
	public void defaultBuilderIsRestoredAfterChildCall() {
		final RecursiveMenuStructureBuilder combinedMenuStructureBuilder = new RecursiveMenuStructureBuilder();
		combinedMenuStructureBuilder.addBuilder("builder1", Builder.EMTPY_BUILDER);
		combinedMenuStructureBuilder.addBuilder("builder2", Builder.EMTPY_BUILDER);
		Builder defaultBuilder = Mockito.mock(Builder.class);
		combinedMenuStructureBuilder.addBuilder("builder3", defaultBuilder);
		combinedMenuStructureBuilder.addSubtreeDefaultBuilder("builder1", "builder3");
		combinedMenuStructureBuilder.addBuilder("builder2", Builder.EMTPY_BUILDER);
		combinedMenuStructureBuilder.addSubtreeDefaultBuilder("builder2", "builder2");

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


	@Test
	public void defaultBuilderIsCalledForChildUsingDefaultBuilder() {
		final RecursiveMenuStructureBuilder combinedMenuStructureBuilder = new RecursiveMenuStructureBuilder();
		Builder defaultBuilder = Mockito.mock(Builder.class);
		combinedMenuStructureBuilder.addBuilder("builder", Builder.EMTPY_BUILDER);
		combinedMenuStructureBuilder.addBuilder("emptyBuilder", Builder.EMTPY_BUILDER);
		combinedMenuStructureBuilder.addBuilder("defaultBuilder", defaultBuilder);
		combinedMenuStructureBuilder.addSubtreeDefaultBuilder("builder", "emptyBuilder");
		combinedMenuStructureBuilder.addSubtreeDefaultBuilder("emptyBuilder", "defaultBuilder");
		final Entry entry = new Entry();
		entry.setBuilders(asList("builder"));
		final Entry childEntry = new Entry();
		entry.addChild(childEntry);
		final Entry subChildEntry = new Entry();
		childEntry.addChild(subChildEntry);
		
		combinedMenuStructureBuilder.build(entry);
		
		Mockito.verify(defaultBuilder).build(subChildEntry);
	}
	
	@Test(expected = IllegalStateException.class)
	public void defaultBuilderIsNotSetException() {
		final RecursiveMenuStructureBuilder combinedMenuStructureBuilder = new RecursiveMenuStructureBuilder();
		final Entry childEntry = new Entry();
		combinedMenuStructureBuilder.build(childEntry);
		
	}
}
