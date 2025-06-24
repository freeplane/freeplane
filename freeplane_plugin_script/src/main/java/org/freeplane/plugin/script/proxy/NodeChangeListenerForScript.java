package org.freeplane.plugin.script.proxy;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.function.Predicate;

import org.freeplane.api.NodeChangeListener;
import org.freeplane.api.NodeChanged;
import org.freeplane.api.NodeChanged.ChangedElement;
import org.freeplane.features.map.NodeModel;
import org.freeplane.plugin.script.ScriptContext;

@SuppressWarnings("removal")
class NodeChangeListenerForScript {
	static Predicate<? super NodeChangeListenerForScript> contains(NodeChangeListener listener) {
		return e -> e.scriptListener.equals(listener);
	}
	private static ClassLoader getContextClassLoader() {
		return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
			@Override
			public ClassLoader run() {
				return Thread.currentThread().getContextClassLoader();
			}
		});
	}
	private static void setContextClassLoader(ClassLoader cl) {
		AccessController.doPrivileged(new PrivilegedAction<Void>() {
			@Override
			public Void run() {
				Thread.currentThread().setContextClassLoader(cl);
				return null;
			}
		});
	}

	private final NodeChangeListener scriptListener;
	private final ScriptContext context;
	private final ClassLoader contextClassLoader;
	NodeChangeListenerForScript(NodeChangeListener scriptListener, ScriptContext context) {
		super();
		this.scriptListener = scriptListener;
		this.context = context;
		this.contextClassLoader = getContextClassLoader();

	}

	void fire(NodeModel node, ChangedElement element) {
		ClassLoader oldContextClassLoader = getContextClassLoader();
		try {
			setContextClassLoader(contextClassLoader);
			scriptListener.nodeChanged(new NodeChanged(new NodeProxy(node, context), element));
		}
		finally {
			setContextClassLoader(oldContextClassLoader);
		}
	}

	NodeChangeListener getListener() {
		return scriptListener;
	}

}
