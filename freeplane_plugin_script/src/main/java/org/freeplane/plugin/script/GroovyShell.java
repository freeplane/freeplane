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
import java.security.AccessController;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.security.PrivilegedAction;

import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.runtime.InvokerHelper;

import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyCodeSource;
import groovy.lang.GroovyObjectSupport;
import groovy.lang.GroovyRuntimeException;
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
	private Binding binding;
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
		this.binding = binding;
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

	private Object getVariable(String name) {
		return binding.getVariables().get(name);
	}

	private void setVariable(String name, Object value) {
		binding.setVariable(name, value);
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
		return InvokerHelper.createScript(parseClass(codeSource), binding);
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

