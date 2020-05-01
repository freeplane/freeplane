package org.freeplane.core.ui.menubuilders.generic;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.freeplane.core.ui.menubuilders.generic.SubtreeProcessor;
import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.PhaseProcessor;
import org.freeplane.core.ui.menubuilders.generic.RecursiveMenuStructureProcessor;
import org.junit.Test;
import org.mockito.Mockito;


public class SubtreeProcessorTest {

    private static SubtreeProcessor createSubtreeProcessor() {
        return new SubtreeProcessor(RecursiveMenuStructureProcessor::shouldProcessOnEvent);
    }
    
	@Test
	public void doesNotBuildEntriesWithoutDelay() throws Exception {
		final SubtreeProcessor childProcessor = createSubtreeProcessor();
		final PhaseProcessor processor = mock(PhaseProcessor.class);
		when(processor.forChildren(Mockito.<Entry> any(), Mockito.<Entry> any())).thenReturn(processor);
		childProcessor.setProcessor(processor);
		final Entry entry = new Entry();
		final Entry child = new Entry();
		entry.addChild(child);
		childProcessor.childEntriesWillBecomeVisible(entry);
		verify(processor, never()).build(child);
	}

	@Test
	public void buildsEntriesWithDelay() throws Exception {
		final SubtreeProcessor childProcessor = createSubtreeProcessor();
		final PhaseProcessor processor = mock(PhaseProcessor.class);
		when(processor.forChildren(Mockito.<Entry> any(), Mockito.<Entry> any())).thenReturn(processor);
		childProcessor.setProcessor(processor);
		final Entry entry = new Entry();
		entry.setAttribute(RecursiveMenuStructureProcessor.PROCESS_ON_POPUP, true);
		childProcessor.childEntriesWillBecomeVisible(entry);
		verify(processor).buildChildren(entry);
	}

	@Test
	public void doesNotDestroyEntriesWithoutDelay() throws Exception {
		final SubtreeProcessor childProcessor = createSubtreeProcessor();
		final PhaseProcessor processor = mock(PhaseProcessor.class);
		when(processor.forChildren(Mockito.<Entry> any(), Mockito.<Entry> any())).thenReturn(processor);
		childProcessor.setProcessor(processor);
		final Entry entry = new Entry();
		final Entry child = new Entry();
		entry.addChild(child);
		childProcessor.childEntriesHidden(entry);
		verify(processor, never()).destroy(child);
	}

	@Test
	public void destroysEntriesWithDelay() throws Exception {
		final SubtreeProcessor childProcessor = createSubtreeProcessor();
		final PhaseProcessor processor = mock(PhaseProcessor.class);
		when(processor.forChildren(Mockito.<Entry> any(), Mockito.<Entry> any())).thenReturn(processor);
		childProcessor.setProcessor(processor);
		final Entry entry = new Entry();
		entry.setAttribute(RecursiveMenuStructureProcessor.PROCESS_ON_POPUP, true);
		final Entry child = new Entry();
		entry.addChild(child);
		childProcessor.childEntriesHidden(entry);
		verify(processor).destroy(child);
	}
}
