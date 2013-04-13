package org.freeplane.plugin.workspace.io;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import org.freeplane.core.io.IReadCompletionListener;
import org.freeplane.core.io.ListHashTable;
import org.freeplane.features.map.NodeModel;

public class FileReadManager {
	public final static String DIRECTORY_HANDLE = "__MaG__FS_DIRECTORY__MaG__";
	public final static String DEFAULT_HANDLE = "__MaG__FS_DEFAULT__MaG__";
	
	final private ListHashTable<String, IFileTypeHandler> fileHandlers;
	final private Collection<IReadCompletionListener> readCompletionListeners;

	public FileReadManager() {
		super();
		fileHandlers = new ListHashTable<String, IFileTypeHandler>();
		readCompletionListeners = new LinkedList<IReadCompletionListener>();
	}

	public void addFileHandler(final String fileExtension, final IFileTypeHandler handler) {
		assert(fileExtension != null);
		assert(fileExtension.length()>0);
		if(fileExtension.toLowerCase().endsWith("default_handle")) {
			fileHandlers.add(DEFAULT_HANDLE, handler);
			return;
		}
		if(fileExtension.toLowerCase().endsWith("directory_handle")) {
			fileHandlers.add(DIRECTORY_HANDLE, handler);
			return;	
		}		
		fileHandlers.add(fileExtension, handler);
	}
	
	public void addFileHandler(final IFileTypeHandler handler) {
		for(Object ext : handler.getSupportedFileTypes()) {
			addFileHandler(ext.toString(), handler);
		}		
	}

	public void addReadCompletionListener(final IReadCompletionListener listener) {
		readCompletionListeners.add(listener);
	}

	public ListHashTable<String, IFileTypeHandler> getFileTypeHandlers() {
		return fileHandlers;
	}

	public void readingCompleted(final NodeModel topNode, final HashMap<String, String> newIds) {
		final Iterator<IReadCompletionListener> iterator = readCompletionListeners.iterator();
		while (iterator.hasNext()) {
			iterator.next().readingCompleted(topNode, newIds);
		}
	}

	public void removeElementHandler(final String fileExtension, final IFileTypeHandler handler) {
		final boolean removed = fileHandlers.remove(fileExtension, handler);
		assert removed;
	}

	public void removeReadCompletionListener(final IReadCompletionListener listener) {
		final boolean removed = readCompletionListeners.remove(listener);
		assert removed;
	}
}

