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
import java.io.PrintStream;
import java.security.AccessControlException;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Map.Entry;
import java.util.regex.Matcher;

import javax.swing.SwingUtilities;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.freeplane.features.map.IMapSelection;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.plugin.script.proxy.ProxyFactory;

import groovy.lang.Binding;
import groovy.lang.GroovyRuntimeException;
import groovy.lang.Script;
import org.freeplane.plugin.script.proxy.ScriptUtils;

/**
 * Special scripting implementation for Groovy.
 */
public class GroovyScript implements IScript {
    final private Object script;

    private final ScriptingPermissions specificPermissions;

    private FreeplaneScriptBaseClass compiledScript;

    private Throwable errorsInScript;

    private CompileTimeStrategy compileTimeStrategy;

	private ScriptClassLoader scriptClassLoader;

    public GroovyScript(String script) {
        this((Object) script);
    }

    public GroovyScript(File script) {
        this((Object) script);
        compileTimeStrategy = new CompileTimeStrategy(script);
    }

    public GroovyScript(String script, ScriptingPermissions permissions) {
        this((Object) script, permissions);
    }

    public GroovyScript(File script, ScriptingPermissions permissions) {
        this((Object) script, permissions);
        compileTimeStrategy = new CompileTimeStrategy(script);
    }

    private GroovyScript(Object script, ScriptingPermissions permissions) {
        super();
        this.script = script;
        this.specificPermissions = permissions;
        compiledScript = null;
        errorsInScript = null;
        compileTimeStrategy = new CompileTimeStrategy(null);
    }

    private GroovyScript(Object script) {
        this(script, null);
    }

    public Script getCompiledScript() {
        return compiledScript;
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
                trustedCompileAndCache(outStream);
                Thread.currentThread().setContextClassLoader(scriptClassLoader);
                updateBinding(node, scriptContext);
                System.setOut(outStream);
				final Object result = compiledScript.run();
				return result;
            } finally {
                System.setOut(oldOut);
                Thread.currentThread().setContextClassLoader(contextClassLoader);
            }
        } catch (final GroovyRuntimeException e) {
            handleScriptRuntimeException(e, outStream, errorHandler);
            // :fixme: This throw is only reached, if
            // handleScriptRuntimeException
            // does not raise an exception. Should it be here at all?
            // And if: Shouldn't it raise an ExecuteScriptException?
            throw new RuntimeException(e);
        } catch (final Throwable e) {
			IMapSelection selection = Controller.getCurrentController().getSelection();
			if (selection != null && ! node.equals(selection.getSelected()) && node.hasVisibleContent()) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						Controller.getCurrentModeController().getMapController().select(node);
					}
				});
            }
            throw new ExecuteScriptException(e.getMessage(), e);
        }
    }

    private ScriptingSecurityManager createScriptingSecurityManager(PrintStream outStream) {
        return new ScriptSecurity(script, specificPermissions, outStream)
                .getScriptingSecurityManager();
    }

    private void trustedCompileAndCache(PrintStream outStream) throws Throwable {
		AccessController.doPrivileged(new PrivilegedExceptionAction<Void>() {

			@Override
			public Void run() throws PrivilegedActionException {
				try {
					final ScriptingSecurityManager scriptingSecurityManager = createScriptingSecurityManager(outStream);
					compileAndCache(scriptingSecurityManager);
				} catch (Exception e) {
					throw new PrivilegedActionException(e);
				} catch (Error e) {
					throw e;
				} catch (Throwable e) {
					throw new RuntimeException(e);
				}
				return null;
			}
		});
	}

    private static boolean accessPermissionCheckerChecked = false;
    private Script compileAndCache(final ScriptingSecurityManager scriptingSecurityManager) throws Throwable {
    	checkAccessPermissionCheckerExists();
    	if (compileTimeStrategy.canUseOldCompiledScript()) {
			scriptClassLoader.setSecurityManager(scriptingSecurityManager);
            return compiledScript;
        }
        removeOldScript();
        errorsInScript = null;
        if (script instanceof Script) {
            return (Script) script;
        } else {
            try {
                final Binding binding = createBindingForCompilation();
				scriptClassLoader = ScriptClassLoader.createClassLoader();
				scriptClassLoader.setSecurityManager(scriptingSecurityManager);
				final GroovyShell shell = new GroovyShell(scriptClassLoader, binding,
                        createCompilerConfiguration());
                compileTimeStrategy.scriptCompileStart();
                if (script instanceof String) {
                    compiledScript = (FreeplaneScriptBaseClass) shell.parse((String) script);
                } else if (script instanceof File) {
                    compiledScript = (FreeplaneScriptBaseClass) shell.parse((File) script);
                } else {
                    throw new IllegalArgumentException();
                }
                compileTimeStrategy.scriptCompiled();
                return compiledScript;
            } catch (Throwable e) {
                errorsInScript = e;
                throw e;
            }
        }
    }

	static void checkAccessPermissionCheckerExists() {
		if(!accessPermissionCheckerChecked){
    		if(System.getSecurityManager() != null){
				try {
					GroovyScript.class.getClassLoader().loadClass("org.codehaus.groovy.reflection.AccessPermissionChecker");
				} catch (ClassNotFoundException e) {
					throw new AccessControlException("class org.codehaus.groovy.reflection.AccessPermissionChecker not found");
				}
			}
    		accessPermissionCheckerChecked = true;
    	}
	}

    private void removeOldScript() {
        if (compiledScript != null) {
            InvokerHelper.removeClass(compiledScript.getClass());
            compiledScript = null;
        }
    }

    private void updateBinding(final NodeModel node, ScriptContext scriptContext) {
    	Binding binding = compiledScript.getBinding();
        binding.setVariable("c", ProxyFactory.createController(scriptContext));
        binding.setVariable("node", ProxyFactory.createNode(node, scriptContext));
	    for (Entry<String, Object> entry : ScriptingConfiguration.getStaticProperties().entrySet()) {
            binding.setProperty(entry.getKey(), entry.getValue());
        }
        compiledScript.updateBoundVariables();
    }

    private Binding createBindingForCompilation() {
        final Binding binding = new Binding();
        binding.setVariable("script", script);
        return binding;
    }

    private void handleScriptRuntimeException(final GroovyRuntimeException e, PrintStream outStream, IFreeplaneScriptErrorHandler errorHandler) {
        outStream.print("message: " + e.getMessage());
        final ModuleNode module = e.getModule();
        final ASTNode astNode = e.getNode();
        int lineNumber = -1;
        if (module != null) {
            lineNumber = module.getLineNumber();
        } else if (astNode != null) {
            lineNumber = astNode.getLineNumber();
        } else {
            lineNumber = findLineNumberInString(e.getMessage(), lineNumber);
        }
        outStream.print("Line number: " + lineNumber);
        errorHandler.gotoLine(lineNumber);
        throw new ExecuteScriptException(e.getMessage() + " at line " + lineNumber, e);
    }

    static CompilerConfiguration createCompilerConfiguration() {
        CompilerConfiguration config = new CompilerConfiguration();
        config.setScriptBaseClass(FreeplaneScriptBaseClass.class.getName());
        if (!(ScriptResources.getClasspath() == null || ScriptResources.getClasspath().isEmpty())) {
            config.setClasspathList(ScriptResources.getClasspath());
        }
		final ImportCustomizer importCustomizer = new ImportCustomizer();
        importCustomizer.addStaticImport(ScriptUtils.class.getName(), "ignoreCycles");
		config.addCompilationCustomizers(importCustomizer);
        return config;
    }

    private int findLineNumberInString(final String resultString, int lineNumber) {
        final java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
                ".*@ line ([0-9]+).*",
                java.util.regex.Pattern.DOTALL);
        final Matcher matcher = pattern.matcher(resultString);
        if (matcher.matches()) {
            lineNumber = Integer.parseInt(matcher.group(1));
        }
        return lineNumber;
    }

    @Override
    protected void finalize() throws Throwable {
        removeOldScript();
        super.finalize();
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
