package org.freeplane.core.ui.menubuilders;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Test;


public class PhaseProcessorTest {
	@Test
	public void onePhaseBuilder() throws Exception {
		RecursiveMenuStructureProcessor builder = mock(RecursiveMenuStructureProcessor.class);
		final PhaseProcessor phasedBuilder = new PhaseProcessor(builder);
		final Entry entry = new Entry();
		phasedBuilder.build(entry);
		verify(builder).process(entry);
	}
	@Test
	public void twoPhaseBuilder() throws Exception {
		RecursiveMenuStructureProcessor first = mock(RecursiveMenuStructureProcessor.class);
		RecursiveMenuStructureProcessor second = mock(RecursiveMenuStructureProcessor.class);
		final PhaseProcessor phasedBuilder = new PhaseProcessor(first, second);
		final Entry entry = new Entry();
		phasedBuilder.build(entry);
		verify(second).process(entry);
	}
}
