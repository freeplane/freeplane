package org.freeplane.plugin.workspace.io;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import org.freeplane.core.io.IReadCompletionListener;
import org.freeplane.core.io.ListHashTable;
import org.freeplane.features.map.NodeModel;

public class FileReadManager {
	final private ListHashTable<String, IFileHandler> fileHandlers;
	final private Collection<IReadCompletionListener> readCompletionListeners;

	public FileReadManager() {
		super();
		fileHandlers = new ListHashTable<String, IFileHandler>();
		readCompletionListeners = new LinkedList<IReadCompletionListener>();
	}

	public void addFileHandler(final String fileExtension, final IFileHandler handler) {
		fileHandlers.add(fileExtension, handler);
	}

	public void addReadCompletionListener(final IReadCompletionListener listener) {
		readCompletionListeners.add(listener);
	}

	public ListHashTable<String, IFileHandler> getElementHandlers() {
		return fileHandlers;
	}

	public void readingCompleted(final NodeModel topNode, final HashMap<String, String> newIds) {
		final Iterator<IReadCompletionListener> iterator = readCompletionListeners.iterator();
		while (iterator.hasNext()) {
			iterator.next().readingCompleted(topNode, newIds);
		}
	}

	public void removeElementHandler(final String fileExtension, final IFileHandler handler) {
		final boolean removed = fileHandlers.remove(fileExtension, handler);
		assert removed;
	}

	public void removeReadCompletionListener(final IReadCompletionListener listener) {
		final boolean removed = readCompletionListeners.remove(listener);
		assert removed;
	}
}

