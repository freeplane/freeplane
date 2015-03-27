package org.freeplane.core.ui.menubuilders.generic;

import org.junit.Test;
import org.mockito.Mockito;

public class RecursiveMenuStructureBuilderForPathsTest {
	@Test
	public void explicitBuilderForPathIsCalled() {
		final RecursiveMenuStructureBuilderForPaths buildersForPath = new RecursiveMenuStructureBuilderForPaths();
		EntryVisitor builder = Mockito.mock(EntryVisitor.class);
		buildersForPath.addBuilder("/name", builder);
		final Entry childEntry = new Entry();
		childEntry.setName("name");
		buildersForPath.build(childEntry);
		
		Mockito.verify(builder).visit(childEntry);
	}

	@Test
	public void differentBuildersForPathAreCalled() {
		final RecursiveMenuStructureBuilderForPaths buildersForPath = new RecursiveMenuStructureBuilderForPaths();
		EntryVisitor builder = Mockito.mock(EntryVisitor.class);
		buildersForPath.addBuilder("/name", builder);
		buildersForPath.addBuilder("/name", EntryVisitor.EMTPY_VISITOR);
		final Entry childEntry = new Entry();
		childEntry.setName("name");
		buildersForPath.build(childEntry);
		
		Mockito.verify(builder).visit(childEntry);
	}

	@Test
	public void differentBuildersForDifferentPathsAreCalled() {
		final RecursiveMenuStructureBuilderForPaths buildersForPath = new RecursiveMenuStructureBuilderForPaths();
		EntryVisitor builder = Mockito.mock(EntryVisitor.class);
		buildersForPath.addBuilder("/name", builder);
		final Entry childEntry = new Entry();
		childEntry.setName("name2");
		buildersForPath.build(childEntry);
		
		Mockito.verify(builder, Mockito.never()).visit(childEntry);
	}


	@Test
	public void differentBuildersForSubtreePathsAreCalled() {
		final Entry entry = new Entry();
		entry.setName("parent");
		final RecursiveMenuStructureBuilderForPaths buildersForPath = new RecursiveMenuStructureBuilderForPaths();
		EntryVisitor builder = Mockito.mock(EntryVisitor.class);
		buildersForPath.addBuilder("/parent/child", builder);
		final Entry childEntry = new Entry();
		childEntry.setName("child");
		entry.addChild(childEntry);
		
		buildersForPath.build(entry);
		
		Mockito.verify(builder).visit(childEntry);
	}

}
