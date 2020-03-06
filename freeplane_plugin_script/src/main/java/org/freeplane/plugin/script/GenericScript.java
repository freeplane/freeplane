/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2012 Dimitry
 *
 *  This file author is Dimitry
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.freeplane.plugin.script;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.List;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

import org.apache.commons.io.FilenameUtils;
import org.freeplane.core.util.FileUtils;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.plugin.script.proxy.ProxyFactory;

/**
 * Implements scripting via JSR233 implementation for all other languages except
 * Groovy.
 */
public class GenericScript implements IScript {
    public static final class ScriptSource {
        private final File file;

        private final String script;

        private String cachedFileContent;

        public ScriptSource(String script) {
            this.script = script;
            this.file = null;
        }

        public ScriptSource(File file) {
            this.script = null;
            this.file = file;
            this.cachedFileContent = slurpFile(file);
        }

        public boolean isFile() {
            return file != null;
        }

        public String rereadFile() {
            cachedFileContent = slurpFile(file);
            return cachedFileContent;
        }

        public String getScript() {
            return isFile() ? cachedFileContent : script;
        }

		public String getPath() {
			return isFile() ? file.getAbsolutePath() : null;
		}
    }

	private ScriptSource scriptSource;

	private ScriptingPermissions specificPermissions;

    private CompiledScript compiledScript;

    private Throwable errorsInScript;

	private ScriptEngine engine;

    private boolean compilationEnabled = true;

    private CompileTimeStrategy compileTimeStrategy;

	private ScriptClassLoader scriptClassLoader;

	private void init(ScriptSource scriptSource, ScriptingPermissions permissions) {
        this.scriptSource = scriptSource;
        this.specificPermissions = permissions;
		this.engine = null;
        compiledScript = null;
        errorsInScript = null;
		scriptClassLoader = ScriptClassLoader.createClassLoader();
    }

	private void init(String script, ScriptingPermissions permissions) {
		init(new ScriptSource(script), permissions);
        compileTimeStrategy = new CompileTimeStrategy(null);
    }

    public GenericScript(String script, String type, ScriptingPermissions permissions) {
		init(script, permissions);
		ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(scriptClassLoader);
			ScriptEngineManager scriptEngineManager = createScriptEngineManager(scriptClassLoader);
			final ScriptEngine engineByExtension = scriptEngineManager.getEngineByExtension(type);
			final ScriptEngine engine = engineByExtension != null ? engineByExtension :  scriptEngineManager.getEngineByName(type);
			this.engine = checkNotNull(engine, type);
		}
		finally {
			Thread.currentThread().setContextClassLoader(contextClassLoader);
		}
    }

    public GenericScript(File scriptFile, ScriptingPermissions permissions) {
		init(new ScriptSource(scriptFile), permissions);
		ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(scriptClassLoader);
			ScriptEngineManager scriptEngineManager = createScriptEngineManager(scriptClassLoader);
			final String extension = FilenameUtils.getExtension(scriptFile.getName());
			engine = checkNotNull(scriptEngineManager.getEngineByExtension(extension), extension);
		}
		finally {
			Thread.currentThread().setContextClassLoader(contextClassLoader);
		}
        compilationEnabled = !disableScriptCompilation(scriptFile);
        compileTimeStrategy = new CompileTimeStrategy(scriptFile);
    }


    private static String slurpFile(File scriptFile) {
        try {
            return FileUtils.slurpFile(scriptFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public Object execute(final NodeModel node, PrintStream outStream, IFreeplaneScriptErrorHandler errorHandler, ScriptContext scriptContext) {
        try {
            if (errorsInScript != null && compileTimeStrategy.canUseOldCompiledScript()) {
                throw new ExecuteScriptException(errorsInScript.getMessage(), errorsInScript);
            }
            final PrintStream oldOut = System.out;
			ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
            try {
				AccessController.doPrivileged(new PrivilegedAction<Void>() {

					@Override
					public Void run() {
						final ScriptingSecurityManager scriptingSecurityManager = createScriptingSecurityManager(outStream);
						scriptClassLoader.setSecurityManager(scriptingSecurityManager);
						return null;
					}
				});
				Thread.currentThread().setContextClassLoader(scriptClassLoader);
                final SimpleScriptContext context = createScriptContext(node, scriptContext, outStream);
                if (compilationEnabled && engine instanceof Compilable) {
                    compileAndCache((Compilable) engine);
                    System.setOut(outStream);
					return compiledScript.eval(context);
                } else {
                    System.setOut(outStream);
					return engine.eval(scriptSource.getScript(), context);
                }
            } finally {
                System.setOut(oldOut);
				Thread.currentThread().setContextClassLoader(contextClassLoader);
            }
        } catch (final ScriptException e) {
            handleScriptRuntimeException(e, outStream, errorHandler);
            // :fixme: This throw is only reached, if
            // handleScriptRuntimeException
            // does not raise an exception. Should it be here at all?
            // And if: Shouldn't it raise an ExecuteScriptException?
            throw new RuntimeException(e);
        } catch (final Throwable e) {
			if (Controller.getCurrentController().getSelection() != null && node.hasVisibleContent()) {
                Controller.getCurrentModeController().getMapController().select(node);
            }
            throw new ExecuteScriptException(e.getMessage(), e);
        }
    }


    private ScriptingSecurityManager createScriptingSecurityManager(PrintStream outStream) {
        return new ScriptSecurity(scriptSource, specificPermissions, outStream)
                .getScriptingSecurityManager();
    }

    private boolean disableScriptCompilation(File scriptFile) {
        return FilenameUtils.isExtension(scriptFile.getName(),
                ScriptResources.SCRIPT_COMPILATION_DISABLED_EXTENSIONS);
    }

    private SimpleScriptContext createScriptContext(final NodeModel node, ScriptContext scriptContext, OutputStream outStream) {
        final SimpleScriptContext context = new SimpleScriptContext();
        final OutputStreamWriter outWriter = new OutputStreamWriter(outStream);
        context.setWriter(outWriter);
		context.setErrorWriter(outWriter);
        context.setBindings(createBinding(node, scriptContext), javax.script.ScriptContext.ENGINE_SCOPE);
        return context;
    }

    private Bindings createBinding(final NodeModel node, ScriptContext scriptContext) {
        final Bindings binding = engine.createBindings();
        binding.put("c", ProxyFactory.createController(scriptContext));
        binding.put("node", ProxyFactory.createNode(node, scriptContext));
        binding.putAll(ScriptingConfiguration.getStaticProperties());
        return binding;
    }


	private static ScriptEngineManager createScriptEngineManager(final ClassLoader classLoader) {
		ScriptEngineManager scriptEngineManager = new ScriptEngineManager(classLoader);
		return scriptEngineManager;
	}

	static List<ScriptEngineFactory> createScriptEngineFactories() {
		final ClassLoader classLoader = ScriptClassLoader.createClassLoader();
		return createScriptEngineManager(classLoader).getEngineFactories();
	}


    private void compileAndCache(Compilable engine) throws Throwable {
        if (compileTimeStrategy.canUseOldCompiledScript()) {
            return;
        }
        compiledScript = null;
        errorsInScript = null;
        try {
            scriptSource.rereadFile();
            compileTimeStrategy.scriptCompileStart();
            compiledScript = engine.compile(scriptSource.getScript());
            compileTimeStrategy.scriptCompiled();
        } catch (Throwable e) {
            errorsInScript = e;
            throw e;
        }
    }

    private static ScriptEngine checkNotNull(final ScriptEngine motor, String detail) {
        if (motor == null) {
            throw new ScriptEngineNotLoadedException("can't load script engine " + detail);
        }
        return motor;
    }

    private void handleScriptRuntimeException(final ScriptException e, PrintStream outStream, IFreeplaneScriptErrorHandler errorHandler) {
        outStream.print("message: " + e.getMessage());
        int lineNumber = e.getLineNumber();
        outStream.print("Line number: " + lineNumber);
        errorHandler.gotoLine(lineNumber);
        throw new ExecuteScriptException(e.getMessage() + " at line " + lineNumber,
                // The ScriptException should have a cause. Use
                // that, it is what we want to know.
                (e.getCause() == null) ? e : e.getCause());
    }

    @Override
    public boolean hasPermissions(ScriptingPermissions permissions) {
        if (this.specificPermissions == null) {
            return this.specificPermissions == permissions;
        } else {
            return this.specificPermissions.equals(permissions);
        }
    }

}
