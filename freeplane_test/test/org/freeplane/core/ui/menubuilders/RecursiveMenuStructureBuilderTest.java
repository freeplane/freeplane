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
		combinedMenuStructureBuilder.addBuilder("defaultBuilder", defaultBuilder);
		combinedMenuStructureBuilder.addSubtreeDefaultBuilder("builder", "defaultBuilder");
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
	
	@Test
	public void explicitBuilderForPathIsCalled() {
		final RecursiveMenuStructureBuilderForPaths buildersForPath = new RecursiveMenuStructureBuilderForPaths();
		Builder builder = Mockito.mock(Builder.class);
		buildersForPath.addBuilder("/name", builder);
		final Entry childEntry = new Entry();
		childEntry.setName("name");
		buildersForPath.build(childEntry);
		
		Mockito.verify(builder).build(childEntry);
	}

	@Test
	public void differentBuildersForPathAreCalled() {
		final RecursiveMenuStructureBuilderForPaths buildersForPath = new RecursiveMenuStructureBuilderForPaths();
		Builder builder = Mockito.mock(Builder.class);
		buildersForPath.addBuilder("/name", builder);
		buildersForPath.addBuilder("/name", Builder.EMTPY_BUILDER);
		final Entry childEntry = new Entry();
		childEntry.setName("name");
		buildersForPath.build(childEntry);
		
		Mockito.verify(builder).build(childEntry);
	}

	@Test
	public void differentBuildersForDifferentPathsAreCalled() {
		final RecursiveMenuStructureBuilderForPaths buildersForPath = new RecursiveMenuStructureBuilderForPaths();
		Builder builder = Mockito.mock(Builder.class);
		buildersForPath.addBuilder("/name", builder);
		final Entry childEntry = new Entry();
		childEntry.setName("name2");
		buildersForPath.build(childEntry);
		
		Mockito.verify(builder, Mockito.never()).build(childEntry);
	}


	@Test
	public void differentBuildersForSubtreePathsAreCalled() {
		final Entry entry = new Entry();
		entry.setName("parent");
		final RecursiveMenuStructureBuilderForPaths buildersForPath = new RecursiveMenuStructureBuilderForPaths();
		Builder builder = Mockito.mock(Builder.class);
		buildersForPath.addBuilder("/parent/child", builder);
		final Entry childEntry = new Entry();
		childEntry.setName("child");
		entry.addChild(childEntry);
		
		buildersForPath.build(entry);
		
		Mockito.verify(builder).build(childEntry);
	}

}
