package org.freeplane.core.ui.menubuilders;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class RecursiveMenuStructureBuilderTest {

	private RecursiveMenuStructureBuilder recursiveMenuStructureBuilder;
	private Builder builder;
	private Builder defaultBuilder;

	@Before
	public void setup(){
		recursiveMenuStructureBuilder = new RecursiveMenuStructureBuilder();
		defaultBuilder = Mockito.mock(Builder.class);
		builder = Mockito.mock(Builder.class);
		recursiveMenuStructureBuilder.addBuilder("builder", builder);
		recursiveMenuStructureBuilder.addBuilder("emptyBuilder", Builder.EMTPY_BUILDER);
		recursiveMenuStructureBuilder.addBuilder("defaultBuilder", defaultBuilder);
	}


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
		final RecursiveMenuStructureBuilder recursiveMenuStructureBuilder = new RecursiveMenuStructureBuilder();
		Builder builder = Mockito.mock(Builder.class);
		recursiveMenuStructureBuilder.setDefaultBuilder(builder);
		final Entry entry = new Entry();
		recursiveMenuStructureBuilder.build(entry);
		verify(builder).build(entry);
	}


	@Test
	public void defaultBuilderIsCalledForChild() {
		recursiveMenuStructureBuilder.addBuilder("builder", Builder.EMTPY_BUILDER);
		recursiveMenuStructureBuilder.addSubtreeDefaultBuilder("builder", "defaultBuilder");
		final Entry entry = new Entry();
		entry.setBuilders(asList("builder"));
		final Entry childEntry = new Entry();
		entry.addChild(childEntry);
		
		recursiveMenuStructureBuilder.build(entry);
		
		verify(defaultBuilder).build(childEntry);
	}

	@Test
	public void defaultBuilderIsRestoredAfterChildCall() {
		final RecursiveMenuStructureBuilder recursiveMenuStructureBuilder = new RecursiveMenuStructureBuilder();
		recursiveMenuStructureBuilder.addBuilder("builder1", Builder.EMTPY_BUILDER);
		recursiveMenuStructureBuilder.addBuilder("builder2", Builder.EMTPY_BUILDER);
		Builder defaultBuilder = Mockito.mock(Builder.class);
		recursiveMenuStructureBuilder.addBuilder("builder3", defaultBuilder);
		recursiveMenuStructureBuilder.addSubtreeDefaultBuilder("builder1", "builder3");
		recursiveMenuStructureBuilder.addBuilder("builder2", Builder.EMTPY_BUILDER);
		recursiveMenuStructureBuilder.addSubtreeDefaultBuilder("builder2", "builder2");

		final Entry entry = new Entry();
		entry.setBuilders(asList("builder1"));
		final Entry childEntry1 = new Entry();
		childEntry1.setBuilders(asList("builder2"));
		entry.addChild(childEntry1);
		
		final Entry childEntry2 = new Entry();
		entry.addChild(childEntry2);

		recursiveMenuStructureBuilder.build(entry);
		
		Mockito.verify(defaultBuilder).build(childEntry2);
	}


	@Test
	public void defaultBuilderIsCalledForChildUsingDefaultBuilder() {
		recursiveMenuStructureBuilder.addSubtreeDefaultBuilder("emptyBuilder", "defaultBuilder");
		recursiveMenuStructureBuilder.addBuilder("emptyBuilder2", Builder.EMTPY_BUILDER);
		recursiveMenuStructureBuilder.addSubtreeDefaultBuilder("emptyBuilder2", "emptyBuilder");
		final Entry entry = new Entry();
		entry.setBuilders(asList("emptyBuilder2"));
		final Entry childEntry = new Entry();
		entry.addChild(childEntry);
		final Entry subChildEntry = new Entry();
		childEntry.addChild(subChildEntry);
		
		recursiveMenuStructureBuilder.build(entry);
		
		Mockito.verify(defaultBuilder).build(subChildEntry);
	}
	
	@Test(expected = IllegalStateException.class)
	public void defaultBuilderIsNotSetException() {
		final RecursiveMenuStructureBuilder recursiveMenuStructureBuilder = new RecursiveMenuStructureBuilder();
		final Entry childEntry = new Entry();
		recursiveMenuStructureBuilder.build(childEntry);
		
	}
	
	@Test
	public void explicitBuilderIsNotCalledForDelayedBuildEntry() {
		final Entry childEntry = new Entry();
		childEntry.setBuilders(asList("builder"));
		final Entry rootEntry = new Entry();
		rootEntry.setAttribute("delayedBuild", true);
		rootEntry.setBuilders(asList("emptyBuilder"));
		rootEntry.addChild(childEntry);
		recursiveMenuStructureBuilder.build(rootEntry);
		
		verify(builder, never()).build(childEntry);
	}


	@Test
	public void explicitBuilderIsCalledBeforeChildEntriesBecomeVisibleForDelayedBuildEntry() {
		final Entry childEntry = new Entry();
		childEntry.setBuilders(asList("builder"));
		final Entry rootEntry = new Entry();
		rootEntry.setAttribute("delayedBuild", true);
		rootEntry.setBuilders(asList("emptyBuilder"));
		rootEntry.addChild(childEntry);
		recursiveMenuStructureBuilder.build(rootEntry);
		
		new EntryPopupListenerAccessor(rootEntry).childEntriesWillBecomeVisible();
		
		verify(builder).build(childEntry);
	}

	@Test
	public void destroyIsCalledBeforeChildEntriesBecomeInvisibleForDelayedBuildEntry() {
		final Entry childEntry = new Entry();
		childEntry.setBuilders(asList("builder"));
		final Entry rootEntry = new Entry();
		rootEntry.setAttribute("delayedBuild", true);
		rootEntry.setBuilders(asList("emptyBuilder"));
		rootEntry.addChild(childEntry);
		recursiveMenuStructureBuilder.build(rootEntry);
		
		new EntryPopupListenerAccessor(rootEntry).childEntriesWillBecomeInvisible();
		
		verify(builder).destroy(childEntry);
	}


	@Test
	public void destroyIsNotCalledBeforeChildEntriesForDestroyedRootEntries() {
		final Entry childEntry = new Entry();
		childEntry.setBuilders(asList("builder"));
		final Entry rootEntry = new Entry();
		rootEntry.setAttribute("delayedBuild", true);
		rootEntry.setBuilders(asList("emptyBuilder"));
		rootEntry.addChild(childEntry);
		recursiveMenuStructureBuilder.build(rootEntry);
		
		recursiveMenuStructureBuilder.destroy(rootEntry);
		new EntryPopupListenerAccessor(rootEntry).childEntriesWillBecomeVisible();
		
		verify(builder, never()).build(childEntry);
	}
}
