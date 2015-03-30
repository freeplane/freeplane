package org.freeplane.core.ui.menubuilders.generic;

import static org.freeplane.core.ui.menubuilders.generic.PhaseProcessor.Phases.ACCELERATORS;
import static org.freeplane.core.ui.menubuilders.generic.PhaseProcessor.Phases.ACTIONS;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.InOrder;


public class PhaseProcessorTest {
	@Test
	public void onePhaseBuilder() throws Exception {
		RecursiveMenuStructureProcessor builder = mock(RecursiveMenuStructureProcessor.class);
		final PhaseProcessor phasedBuilder = new PhaseProcessor().withPhase(ACTIONS, builder);
		final Entry entry = new Entry();
		phasedBuilder.build(entry);
		verify(builder).build(entry);
	}
	@Test
	public void twoPhaseBuilder() throws Exception {
		RecursiveMenuStructureProcessor first = mock(RecursiveMenuStructureProcessor.class);
		RecursiveMenuStructureProcessor second = mock(RecursiveMenuStructureProcessor.class);
		final PhaseProcessor phasedBuilder = new PhaseProcessor().withPhase(ACTIONS, first).withPhase(ACCELERATORS,
		    second);
		final Entry entry = new Entry();
		phasedBuilder.build(entry);
		verify(second).build(entry);
	}

	@Test
	public void subtreeBuilder() throws Exception {
		RecursiveMenuStructureProcessor builder = mock(RecursiveMenuStructureProcessor.class);
		RecursiveMenuStructureProcessor childrenBuilder = mock(RecursiveMenuStructureProcessor.class);
		final PhaseProcessor phasedBuilder = new PhaseProcessor().withPhase(ACTIONS, builder);
		final Entry entry = new Entry();
		when(builder.forChildren(entry, entry)).thenReturn(childrenBuilder);
		phasedBuilder.forChildren(entry, entry).build(entry);
		verify(childrenBuilder).build(entry);
	}

	@Test
	public void onePhaseDestroy() throws Exception {
		RecursiveMenuStructureProcessor builder = mock(RecursiveMenuStructureProcessor.class);
		final PhaseProcessor phasedBuilder = new PhaseProcessor().withPhase(ACTIONS, builder);
		final Entry entry = new Entry();
		phasedBuilder.destroy(entry);
		verify(builder).destroy(entry);
	}

	@Test
	public void returnsPhase() throws Exception {
		RecursiveMenuStructureProcessor builder = mock(RecursiveMenuStructureProcessor.class);
		final PhaseProcessor phasedBuilder = new PhaseProcessor().withPhase(ACTIONS, builder);
		Assert.assertThat(phasedBuilder.phase(ACTIONS), equalTo(builder));
	}

	@Test
	public void twoPhaseDestroyInOppositeOrder() throws Exception {
		RecursiveMenuStructureProcessor first = mock(RecursiveMenuStructureProcessor.class);
		RecursiveMenuStructureProcessor second = mock(RecursiveMenuStructureProcessor.class);
		final PhaseProcessor phasedBuilder = new PhaseProcessor().withPhase(ACTIONS, first).withPhase(ACCELERATORS,
		    second);
		final Entry entry = new Entry();
		phasedBuilder.destroy(entry);
		final InOrder inOrder = inOrder(first, second);
		inOrder.verify(second).destroy(entry);
		inOrder.verify(first).destroy(entry);
	}
}
