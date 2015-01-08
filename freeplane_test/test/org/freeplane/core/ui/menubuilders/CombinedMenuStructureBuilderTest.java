package org.freeplane.core.ui.menubuilders;

import static java.util.Arrays.asList;

import org.junit.Test;
import org.mockito.Mockito;

public class CombinedMenuStructureBuilderTest {

	@Test
	public void explicitBuilderIsCalled() {
		final CombinedMenuStructureBuilder combinedMenuStructureBuilder = new CombinedMenuStructureBuilder();
		Builder builder = Mockito.mock(Builder.class);
		combinedMenuStructureBuilder.addBuilder("builder", builder);
		final Entry childEntry = new Entry();
		childEntry.setBuilders(asList("builder"));
		combinedMenuStructureBuilder.build(childEntry);
		
		Mockito.verify(builder).build(childEntry);
	}


	@Test
	public void defaultBuilderIsCalled() {
		final CombinedMenuStructureBuilder combinedMenuStructureBuilder = new CombinedMenuStructureBuilder();
		Builder builder = Mockito.mock(Builder.class);
		combinedMenuStructureBuilder.setDefaultBuilder(builder);
		final Entry childEntry = new Entry();
		combinedMenuStructureBuilder.build(childEntry);
		
		Mockito.verify(builder).build(childEntry);
	}



	@Test(expected = IllegalStateException.class)
	public void defaultBuilderIsNotSetException() {
		final CombinedMenuStructureBuilder combinedMenuStructureBuilder = new CombinedMenuStructureBuilder();
		final Entry childEntry = new Entry();
		combinedMenuStructureBuilder.build(childEntry);
		
	}
}
