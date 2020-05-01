package org.freeplane.core.ui.menubuilders.generic;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.freeplane.core.ui.menubuilders.generic.BuilderDestroyerPair.VisitorType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class RecursiveMenuStructureProcessorTest {

	private RecursiveMenuStructureProcessor recursiveMenuStructureBuilder;
	private EntryVisitor builder;
	private EntryVisitor defaultBuilder;
	private EntryVisitor emptyBuilder;
	private EntryVisitor destroyer;

	@Before
	public void setup(){
		recursiveMenuStructureBuilder = new RecursiveMenuStructureProcessor();
		defaultBuilder = Mockito.mock(EntryVisitor.class);
		builder = Mockito.mock(EntryVisitor.class);
		destroyer = Mockito.mock(EntryVisitor.class);
		recursiveMenuStructureBuilder.addBuilderPair("builder", builder, destroyer);
		emptyBuilder = EntryVisitor.EMTPY;
		recursiveMenuStructureBuilder.addBuilderPair("emptyBuilder", emptyBuilder, null);
		recursiveMenuStructureBuilder.addBuilderPair("defaultBuilder", defaultBuilder, null);
	}


	@Test
	public void explicitBuilderIsCalled() {
		final RecursiveMenuStructureProcessor recursiveMenuStructureBuilder = new RecursiveMenuStructureProcessor();
		EntryVisitor builder = Mockito.mock(EntryVisitor.class);
		recursiveMenuStructureBuilder.addBuilder("builder", builder);
		final Entry entry = new Entry();
		entry.setBuilders(asList("builder"));
		recursiveMenuStructureBuilder.build(entry);
		
		verify(builder).visit(entry);
	}

	@Test
	public void defaultBuilderIsCalled() {
		final RecursiveMenuStructureProcessor recursiveMenuStructureBuilder = new RecursiveMenuStructureProcessor();
		EntryVisitor builder = Mockito.mock(EntryVisitor.class);
		recursiveMenuStructureBuilder.setDefaultBuilder(builder);
		final Entry entry = new Entry();
		recursiveMenuStructureBuilder.build(entry);
		verify(builder).visit(entry);
	}


	@Test
	public void defaultBuilderIsCalledForChild() {
		recursiveMenuStructureBuilder.addBuilderPair("builder", EntryVisitor.EMTPY, null);
		recursiveMenuStructureBuilder.setSubtreeDefaultBuilderPair("builder", "defaultBuilder");
		final Entry entry = new Entry();
		entry.setBuilders(asList("builder"));
		final Entry childEntry = new Entry();
		entry.addChild(childEntry);
		
		recursiveMenuStructureBuilder.build(entry);
		
		verify(defaultBuilder).visit(childEntry);
	}

	@Test
	public void defaultBuilderIsRestoredAfterChildCall() {
		final RecursiveMenuStructureProcessor recursiveMenuStructureBuilder = new RecursiveMenuStructureProcessor();
		recursiveMenuStructureBuilder.addBuilderPair("builder1", EntryVisitor.EMTPY, null);
		recursiveMenuStructureBuilder.addBuilderPair("builder2", EntryVisitor.EMTPY, null);
		EntryVisitor defaultBuilder = Mockito.mock(EntryVisitor.class);
		recursiveMenuStructureBuilder.addBuilderPair("builder3", defaultBuilder, null);
		recursiveMenuStructureBuilder.setSubtreeDefaultBuilderPair("builder1", "builder3");
		recursiveMenuStructureBuilder.addBuilderPair("builder2", EntryVisitor.EMTPY, null);
		recursiveMenuStructureBuilder.setSubtreeDefaultBuilderPair("builder2", "builder2");

		final Entry entry = new Entry();
		entry.setBuilders(asList("builder1"));
		final Entry childEntry1 = new Entry();
		childEntry1.setBuilders(asList("builder2"));
		entry.addChild(childEntry1);
		
		final Entry childEntry2 = new Entry();
		entry.addChild(childEntry2);

		recursiveMenuStructureBuilder.build(entry);
		
		Mockito.verify(defaultBuilder).visit(childEntry2);
	}


	@Test
	public void defaultBuilderIsCalledForChildUsingDefaultBuilder() {
		recursiveMenuStructureBuilder.setSubtreeDefaultBuilderPair("emptyBuilder", "defaultBuilder");
		recursiveMenuStructureBuilder.addBuilderPair("emptyBuilder2", EntryVisitor.EMTPY, null);
		recursiveMenuStructureBuilder.setSubtreeDefaultBuilderPair("emptyBuilder2", "emptyBuilder");
		final Entry entry = new Entry();
		entry.setBuilders(asList("emptyBuilder2"));
		final Entry childEntry = new Entry();
		entry.addChild(childEntry);
		final Entry subChildEntry = new Entry();
		childEntry.addChild(subChildEntry);
		
		recursiveMenuStructureBuilder.build(entry);
		
		Mockito.verify(defaultBuilder).visit(subChildEntry);
	}
	
	@Test(expected = IllegalStateException.class)
	public void defaultBuilderIsNotSetException() {
		final RecursiveMenuStructureProcessor recursiveMenuStructureBuilder = new RecursiveMenuStructureProcessor();
		final Entry childEntry = new Entry();
		recursiveMenuStructureBuilder.build(childEntry);
		
	}
	@Test
	public void defaultBuilderIsNotCalledForChildIfChildProcessingIsSkipped() {
		recursiveMenuStructureBuilder.setDefaultBuilder(defaultBuilder);
		final Entry entry = new Entry();
		when(builder.shouldSkipChildren(entry)).thenReturn(true);
		entry.setBuilders(asList("builder"));
		final Entry childEntry = new Entry();
		entry.addChild(childEntry);
		
		
		recursiveMenuStructureBuilder.build(entry);
		
		Mockito.verify(defaultBuilder, never()).visit(childEntry);
	}

	@Test
	public void explicitRootBuilderExplicitSubtreeDefaultBuilderForRoot() {
		recursiveMenuStructureBuilder.addBuilderPair("builder", EntryVisitor.EMTPY, null);
		recursiveMenuStructureBuilder.setSubtreeDefaultBuilderPair("builder", "defaultBuilder");
		final Entry entry = new Entry();
		entry.setBuilders(asList("builder"));
		BuilderDestroyerPair subtreeDefaultBuilder = recursiveMenuStructureBuilder.findSubtreeChildrenDefaultBuilder(
		    entry, entry);
		assertThat(subtreeDefaultBuilder.get(VisitorType.BUILDER), equalTo(this.defaultBuilder));
	}

	@Test
	public void explicitRootBuilderImplicitSubtreeDefaultBuilderForRoot() {
		recursiveMenuStructureBuilder.addBuilderPair("builder", EntryVisitor.EMTPY, null);
		recursiveMenuStructureBuilder.setDefaultBuilder(defaultBuilder);
		final Entry entry = new Entry();
		entry.setBuilders(asList("builder"));
		BuilderDestroyerPair subtreeDefaultBuilder = recursiveMenuStructureBuilder.findSubtreeChildrenDefaultBuilder(
		    entry, entry);
		assertThat(subtreeDefaultBuilder.get(VisitorType.BUILDER), equalTo(this.defaultBuilder));
	}

	@Test
	public void explicitRootBuilderExplicitSubtreeDefaultBuilderForChild() {
		recursiveMenuStructureBuilder.addBuilderPair("builder", EntryVisitor.EMTPY, null);
		recursiveMenuStructureBuilder.setSubtreeDefaultBuilderPair("builder", "defaultBuilder");
		final Entry root = new Entry();
		root.setBuilders(asList("builder"));
		final Entry childEntry = new Entry();
		root.addChild(childEntry);
		BuilderDestroyerPair subtreeDefaultBuilder = recursiveMenuStructureBuilder.findSubtreeChildrenDefaultBuilder(
		    root, childEntry);
		assertThat(subtreeDefaultBuilder.get(VisitorType.BUILDER), equalTo(this.defaultBuilder));
	}

	@Test
	public void explicitRootBuilderImplicitBuilderForParentExplicitSubtreeDefaultBuilderForChild() {
		recursiveMenuStructureBuilder.addBuilderPair("builder", EntryVisitor.EMTPY, null);
		recursiveMenuStructureBuilder.setSubtreeDefaultBuilderPair("builder", "emptyBuilder");
		recursiveMenuStructureBuilder.setSubtreeDefaultBuilderPair("emptyBuilder", "defaultBuilder");
		final Entry root = new Entry();
		root.setBuilders(asList("builder"));
		final Entry parentEntry = new Entry();
		root.addChild(parentEntry);
		final Entry childEntry = new Entry();
		parentEntry.addChild(childEntry);
		BuilderDestroyerPair subtreeDefaultBuilder = recursiveMenuStructureBuilder.findSubtreeChildrenDefaultBuilder(
		    root, childEntry);
		assertThat(subtreeDefaultBuilder.get(VisitorType.BUILDER), equalTo(this.defaultBuilder));
	}

	@Test
	public void defaultBuilderWithoutDefaultBuilderChange() {
		recursiveMenuStructureBuilder.addBuilderPair("builder", EntryVisitor.EMTPY, null);
		recursiveMenuStructureBuilder.setSubtreeDefaultBuilderPair("builder", "emptyBuilder");
		final Entry root = new Entry();
		root.setBuilders(asList("builder"));
		final Entry parentEntry = new Entry();
		root.addChild(parentEntry);
		final Entry childEntry = new Entry();
		parentEntry.addChild(childEntry);
		BuilderDestroyerPair subtreeDefaultBuilder = recursiveMenuStructureBuilder.findSubtreeChildrenDefaultBuilder(
		    root, childEntry);
		assertThat(subtreeDefaultBuilder.get(VisitorType.BUILDER), equalTo(this.emptyBuilder));
	}

	@Test
	public void explicitBuilderWithoutDefaultBuilderChange() {
		recursiveMenuStructureBuilder.addBuilderPair("builder", EntryVisitor.EMTPY, null);
		recursiveMenuStructureBuilder.addBuilderPair("parent", EntryVisitor.EMTPY, null);
		recursiveMenuStructureBuilder.setSubtreeDefaultBuilderPair("builder", "emptyBuilder");
		final Entry root = new Entry();
		root.setBuilders(asList("builder"));
		final Entry parentEntry = new Entry();
		parentEntry.setBuilders(asList("parent"));
		root.addChild(parentEntry);
		final Entry childEntry = new Entry();
		parentEntry.addChild(childEntry);
		BuilderDestroyerPair subtreeDefaultBuilder = recursiveMenuStructureBuilder.findSubtreeChildrenDefaultBuilder(
		    root, childEntry);
		assertThat(subtreeDefaultBuilder.get(VisitorType.BUILDER), equalTo(this.emptyBuilder));
	}

	@Test
	public void defaultBuilderIsSetForSubtreeProcessor() {
		recursiveMenuStructureBuilder.addBuilderPair("builder", EntryVisitor.EMTPY, null);
		recursiveMenuStructureBuilder.setSubtreeDefaultBuilderPair("builder", "defaultBuilder");
		final Entry entry = new Entry();
		entry.setBuilders(asList("builder"));
		final Entry childEntry = new Entry();
		entry.addChild(childEntry);
		RecursiveMenuStructureProcessor subtreeProcessor = recursiveMenuStructureBuilder.forChildren(entry, childEntry);
		subtreeProcessor.build(childEntry);
		verify(defaultBuilder).visit(childEntry);
	}

	@Test
	public void explicitDestroyerIsCalled() {
		final RecursiveMenuStructureProcessor recursiveMenuStructureBuilder = new RecursiveMenuStructureProcessor();
		EntryVisitor destroyer = Mockito.mock(EntryVisitor.class);
		recursiveMenuStructureBuilder.addBuilderPair("builder", null, destroyer);
		final Entry entry = new Entry();
		entry.setBuilders(asList("builder"));
		recursiveMenuStructureBuilder.destroy(entry);
		verify(destroyer).visit(entry);
	}


	@Test
	public void processorWithoutBuilder_containsNoBuilder() {
		final RecursiveMenuStructureProcessor recursiveMenuStructureBuilder = new RecursiveMenuStructureProcessor();
		assertThat(recursiveMenuStructureBuilder.containsOneOf(Arrays.asList("builder")), equalTo(false));
	}

	@Test
	public void processorWithBuilder_containsBuilder() {
		final RecursiveMenuStructureProcessor recursiveMenuStructureBuilder = new RecursiveMenuStructureProcessor();
		EntryVisitor builder = Mockito.mock(EntryVisitor.class);
		recursiveMenuStructureBuilder.addBuilder("builder", builder);
		assertThat(recursiveMenuStructureBuilder.containsOneOf(Arrays.asList("builder")), equalTo(true));
	}


	@Test
	public void processorWithBuilder_containsOneOfBuilders() {
		final RecursiveMenuStructureProcessor recursiveMenuStructureBuilder = new RecursiveMenuStructureProcessor();
		EntryVisitor builder = Mockito.mock(EntryVisitor.class);
		recursiveMenuStructureBuilder.addBuilder("builder", builder);
		assertThat(recursiveMenuStructureBuilder.containsOneOf(Arrays.asList("builder", "builder2")), equalTo(true));
	}
}
