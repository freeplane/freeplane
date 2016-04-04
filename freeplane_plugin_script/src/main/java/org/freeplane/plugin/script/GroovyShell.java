/*
 * Copyright 2003-2012 the original author or authors.
 * 
 * Modified 2016 by Dimitry Polivaev for Freeplane
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.freeplane.plugin.script;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.security.AccessController;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.security.PrivilegedAction;
import java.util.Map;

import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.plugin.GroovyRunner;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.runtime.InvokerInvocationException;

import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyCodeSource;
import groovy.lang.GroovyObjectSupport;
import groovy.lang.GroovyRuntimeException;
import groovy.lang.GroovySystem;
import groovy.lang.Script;

/**
 * Represents a groovy shell capable of running arbitrary groovy scripts
 *
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @author Guillaume Laforge
 * @author Paul King
 * @version $Revision$
 */
class GroovyShell extends GroovyObjectSupport {
	private static final String DEFAULT_CODE_BASE = "/groovy/shell";
	private Binding context;
	private int counter;
	private CompilerConfiguration config;
	private GroovyClassLoader loader;

	GroovyShell(ClassLoader parent, Binding binding, final CompilerConfiguration config) {
		if (binding == null) {
			throw new IllegalArgumentException("Binding must not be null.");
		}
		if (config == null) {
			throw new IllegalArgumentException("Compiler configuration must not be null.");
		}
		final ClassLoader parentLoader = (parent != null) ? parent : GroovyShell.class.getClassLoader();
		this.loader = AccessController.doPrivileged(new PrivilegedAction<GroovyClassLoader>() {
			@Override
			public GroovyClassLoader run() {
				return new MyGroovyClassLoader(parentLoader, config);
			}
		});
		this.context = binding;
		this.config = config;
	}


	@Override
	public Object getProperty(String property) {
		Object answer = getVariable(property);
		if (answer == null) {
			answer = super.getProperty(property);
		}
		return answer;
	}

	@Override
	public void setProperty(String property, Object newValue) {
		setVariable(property, newValue);
		try {
			super.setProperty(property, newValue);
		}
		catch (GroovyRuntimeException e) {
			// ignore, was probably a dynamic property
		}
	}

	/**
	 * if (theClass is a Script) {
	 * run it like a script
	 * } else if (theClass has a main method) {
	 * run the main method
	 * } else if (theClass instanceof GroovyTestCase) {
	 * use the test runner to run it
	 * } else if (theClass implements Runnable) {
	 * if (theClass has a constructor with String[] params)
	 * instantiate theClass with this constructor and run
	 * else if (theClass has a no-args constructor)
	 * instantiate theClass with the no-args constructor and run
	 * }
	 */
	private Object runScriptOrMainOrTestOrRunnable(Class scriptClass, String[] args) {
		if (scriptClass == null) {
			return null;
		}
		if (Script.class.isAssignableFrom(scriptClass)) {
			// treat it just like a script if it is one
			Script script = null;
			try {
				script = (Script) scriptClass.newInstance();
			}
			catch (InstantiationException e) {
				// ignore instantiation errors,, try to do main
			}
			catch (IllegalAccessException e) {
				// ignore instantiation errors, try to do main
			}
			if (script != null) {
				script.setBinding(context);
				script.setProperty("args", args);
				return script.run();
			}
		}
		try {
			// let's find a main method
			scriptClass.getMethod("main", new Class[] { String[].class });
			// if that main method exist, invoke it
			return InvokerHelper.invokeMethod(scriptClass, "main", new Object[] { args });
		}
		catch (NoSuchMethodException e) {
			// if it implements Runnable, try to instantiate it
			if (Runnable.class.isAssignableFrom(scriptClass)) {
				return runRunnable(scriptClass, args);
			}
			for (Map.Entry<String, GroovyRunner> entry : GroovySystem.RUNNER_REGISTRY.entrySet()) {
				GroovyRunner runner = entry.getValue();
				if (runner != null && runner.canRun(scriptClass, this.loader)) {
					return runner.run(scriptClass, this.loader);
				}
			}
			String message = "This script or class could not be run.\n" + "It should either:\n"
			        + "- have a main method,\n" + "- be a JUnit test or extend GroovyTestCase,\n"
			        + "- implement the Runnable interface,\n"
			        + "- or be compatible with a registered script runner. Known runners:\n";
			if (GroovySystem.RUNNER_REGISTRY.isEmpty()) {
				message += "  * <none>";
			}
			for (Map.Entry<String, GroovyRunner> entry : GroovySystem.RUNNER_REGISTRY.entrySet()) {
				message += "  * " + entry.getKey() + "\n";
			}
			throw new GroovyRuntimeException(message);
		}
	}

	private Object runRunnable(Class scriptClass, String[] args) {
		Constructor constructor = null;
		Runnable runnable = null;
		Throwable reason = null;
		try {
			// first, fetch the constructor taking String[] as parameter
			constructor = scriptClass.getConstructor(new Class[] { (new String[] {}).getClass() });
			try {
				// instantiate a runnable and run it
				runnable = (Runnable) constructor.newInstance(new Object[] { args });
			}
			catch (Throwable t) {
				reason = t;
			}
		}
		catch (NoSuchMethodException e1) {
			try {
				// otherwise, find the default constructor
				constructor = scriptClass.getConstructor(new Class[] {});
				try {
					// instantiate a runnable and run it
					runnable = (Runnable) constructor.newInstance();
				}
				catch (InvocationTargetException ite) {
					throw new InvokerInvocationException(ite.getTargetException());
				}
				catch (Throwable t) {
					reason = t;
				}
			}
			catch (NoSuchMethodException nsme) {
				reason = nsme;
			}
		}
		if (constructor != null && runnable != null) {
			runnable.run();
		}
		else {
			throw new GroovyRuntimeException("This script or class was runnable but could not be run. ", reason);
		}
		return null;
	}

	private Object getVariable(String name) {
		return context.getVariables().get(name);
	}

	private void setVariable(String name, Object value) {
		context.setVariable(name, value);
	}

	/**
	 * Parses the groovy code contained in codeSource and returns a java class.
	 */
	private Class parseClass(final GroovyCodeSource codeSource) throws CompilationFailedException {
		// Don't cache scripts
		return loader.parseClass(codeSource, false);
	}

	/**
	 * Parses the given script and returns it ready to be run.  When running in a secure environment
	 * (-Djava.security.manager) codeSource.getCodeSource() determines what policy grants should be
	 * given to the script.
	 *
	 * @param codeSource
	 * @return ready to run script
	 */
	private Script parse(final GroovyCodeSource codeSource) throws CompilationFailedException {
		return InvokerHelper.createScript(parseClass(codeSource), context);
	}

	/**
	 * Parses the given script and returns it ready to be run
	 *
	 * @param file is the file of the script (which is used to create the class name of the script)
	 */
	Script parse(File file) throws CompilationFailedException, IOException {
		return parse(new GroovyCodeSource(file, config.getSourceEncoding()));
	}

	/**
	 * Parses the given script and returns it ready to be run
	 *
	 * @param scriptText the text of the script
	 */
	Script parse(String scriptText) throws CompilationFailedException {
		return parse(scriptText, generateScriptName());
	}

	private Script parse(final String scriptText, final String fileName) throws CompilationFailedException {
		GroovyCodeSource gcs = AccessController.doPrivileged(new PrivilegedAction<GroovyCodeSource>() {
			@Override
			public GroovyCodeSource run() {
				return new GroovyCodeSource(scriptText, fileName, DEFAULT_CODE_BASE);
			}
		});
		return parse(gcs);
	}

	protected synchronized String generateScriptName() {
		return "Script" + (++counter) + ".groovy";
	}
}

class MyGroovyClassLoader extends GroovyClassLoader {
	MyGroovyClassLoader(ClassLoader loader, CompilerConfiguration config) {
		super(loader, config);
	}

	class MyInnerLoader extends InnerLoader {
		private final MyGroovyClassLoader delegate;

		MyInnerLoader(MyGroovyClassLoader delegate) {
			super(delegate);
			this.delegate = delegate;
		}

		@Override
		protected PermissionCollection getPermissions(CodeSource codeSource) {
			return delegate.getPermissions(codeSource);
		}
	}

	@Override
	protected PermissionCollection getPermissions(CodeSource codeSource) {
		PermissionCollection perms = new Permissions();
		perms.setReadOnly();
		return perms;
	}
	/**
	 * creates a ClassCollector for a new compilation.
	 *
	 * @param unit the compilationUnit
	 * @param su   the SourceUnit
	 * @return the ClassCollector
	 */
	@Override
	protected ClassCollector createCollector(CompilationUnit unit, SourceUnit su) {
		InnerLoader loader = AccessController.doPrivileged(new PrivilegedAction<InnerLoader>() {
			@Override
			public InnerLoader run() {
				return new MyInnerLoader(MyGroovyClassLoader.this);
			}
		});
		return new ClassCollector(loader, unit, su) {
			// use inner class to call protected constructor
		};
	}
}

