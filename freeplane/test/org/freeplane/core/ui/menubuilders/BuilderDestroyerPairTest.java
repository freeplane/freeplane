package org.freeplane.core.ui.menubuilders;

import static org.mockito.Mockito.mock;

import org.junit.Test;

public class BuilderDestroyerPairTest {
	@Test
	public void test() {
		EntryVisitor builder = mock(EntryVisitor.class);
		EntryVisitor destroyer = mock(EntryVisitor.class);
		final BuilderDestroyerPair builderDestroyerPair = new BuilderDestroyerPair(builder, destroyer);
	}
}
