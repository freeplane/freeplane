package org.freeplane.core.ui.menubuilders;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.mockito.Mockito;


public class ChildProcessorTest {
	@Test
	public void doesNotBuildEntriesWithoutDelay() throws Exception {
		final ChildProcessor childProcessor = new ChildProcessor();
		final Processor processor = mock(Processor.class);
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
		final ChildProcessor childProcessor = new ChildProcessor();
		final Processor processor = mock(Processor.class);
		when(processor.forChildren(Mockito.<Entry> any(), Mockito.<Entry> any())).thenReturn(processor);
		childProcessor.setProcessor(processor);
		final Entry entry = new Entry();
		entry.setAttribute(RecursiveMenuStructureProcessor.PROCESS_ON_POPUP, true);
		final Entry child = new Entry();
		entry.addChild(child);
		childProcessor.childEntriesWillBecomeVisible(entry);
		verify(processor).build(child);
	}

	@Test
	public void doesNotDestroyEntriesWithoutDelay() throws Exception {
		final ChildProcessor childProcessor = new ChildProcessor();
		final Processor processor = mock(Processor.class);
		when(processor.forChildren(Mockito.<Entry> any(), Mockito.<Entry> any())).thenReturn(processor);
		childProcessor.setProcessor(processor);
		final Entry entry = new Entry();
		final Entry child = new Entry();
		entry.addChild(child);
		childProcessor.childEntriesWillBecomeInvisible(entry);
		verify(processor, never()).destroy(child);
	}

	@Test
	public void destroysEntriesWithDelay() throws Exception {
		final ChildProcessor childProcessor = new ChildProcessor();
		final Processor processor = mock(Processor.class);
		when(processor.forChildren(Mockito.<Entry> any(), Mockito.<Entry> any())).thenReturn(processor);
		childProcessor.setProcessor(processor);
		final Entry entry = new Entry();
		entry.setAttribute(RecursiveMenuStructureProcessor.PROCESS_ON_POPUP, true);
		final Entry child = new Entry();
		entry.addChild(child);
		childProcessor.childEntriesWillBecomeInvisible(entry);
		verify(processor).destroy(child);
	}
}
