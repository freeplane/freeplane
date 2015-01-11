package org.freeplane.core.ui.menubuilders;

import static org.junit.Assert.*;

import org.junit.Test;
import org.mockito.Mockito;

public class RecursiveMenuStructureBuilderForPathsTest {
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
