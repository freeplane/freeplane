package org.freeplane.core.ui.menubuilders;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;


public class PhaseProcessorTest {
	@Test
	public void onePhaseBuilder() throws Exception {
		RecursiveMenuStructureProcessor builder = mock(RecursiveMenuStructureProcessor.class);
		final PhaseProcessor phasedBuilder = new PhaseProcessor(builder);
		final Entry entry = new Entry();
		phasedBuilder.build(entry);
		verify(builder).build(entry);
	}
	@Test
	public void twoPhaseBuilder() throws Exception {
		RecursiveMenuStructureProcessor first = mock(RecursiveMenuStructureProcessor.class);
		RecursiveMenuStructureProcessor second = mock(RecursiveMenuStructureProcessor.class);
		final PhaseProcessor phasedBuilder = new PhaseProcessor(first, second);
		final Entry entry = new Entry();
		phasedBuilder.build(entry);
		verify(second).build(entry);
	}

	@Test
	public void subtreeBuilder() throws Exception {
		RecursiveMenuStructureProcessor builder = mock(RecursiveMenuStructureProcessor.class);
		RecursiveMenuStructureProcessor childrenBuilder = mock(RecursiveMenuStructureProcessor.class);
		final PhaseProcessor phasedBuilder = new PhaseProcessor(builder);
		final Entry entry = new Entry();
		when(builder.forChildren(entry, entry)).thenReturn(childrenBuilder);
		phasedBuilder.forChildren(entry, entry).build(entry);
		verify(childrenBuilder).build(entry);
	}

	@Test
	public void onePhaseDestroy() throws Exception {
		RecursiveMenuStructureProcessor builder = mock(RecursiveMenuStructureProcessor.class);
		final PhaseProcessor phasedBuilder = new PhaseProcessor(builder);
		final Entry entry = new Entry();
		phasedBuilder.destroy(entry);
		verify(builder).destroy(entry);
	}
}
