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

import groovy.lang.Binding;
import groovy.lang.GroovyRuntimeException;
import groovy.lang.GroovyShell;
import groovy.lang.Script;

import java.io.File;
import java.io.PrintStream;
import java.util.regex.Matcher;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.main.application.FreeplaneSecurityManager;
import org.freeplane.plugin.script.proxy.ProxyFactory;

/**
 * Special scripting implementation for Groovy.
 */
public class GroovyScript implements IScript {
    final private Object script;
    private final ScriptingPermissions specificPermissions;
    private Script compiledScript;
    private Throwable errorsInScript;
    private IFreeplaneScriptErrorHandler errorHandler;
    private PrintStream outStream;
    private ScriptContext scriptContext;
    private CompileTimeStrategy compileTimeStrategy;

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
        errorHandler = ScriptResources.IGNORING_SCRIPT_ERROR_HANDLER;
        outStream = System.out;
        scriptContext = null;
        compileTimeStrategy = new CompileTimeStrategy(null);
    }

    private GroovyScript(Object script) {
        this(script, null);
    }

    @Override
    public IScript setErrorHandler(IFreeplaneScriptErrorHandler pErrorHandler) {
        this.errorHandler = pErrorHandler;
        return this;
    }

    @Override
    public IScript setOutStream(PrintStream outStream) {
        this.outStream = outStream;
        return this;
    }

    @Override
    public IScript setScriptContext(ScriptContext scriptContext) {
        this.scriptContext = scriptContext;
        return this;
    }

    @Override
    public Object getScript() {
        return script;
    }

    public Script getCompiledScript() {
        return compiledScript;
    }

    @Override
    public Object execute(final NodeModel node) {
        try {
            if (errorsInScript != null)
                throw new ExecuteScriptException(errorsInScript.getMessage(), errorsInScript);
            final ScriptingSecurityManager scriptingSecurityManager = createScriptingSecurityManager();
            final ScriptingPermissions originalScriptingPermissions = new ScriptingPermissions(ResourceController
                .getResourceController().getProperties());
            final FreeplaneSecurityManager securityManager = (FreeplaneSecurityManager) System.getSecurityManager();
            final boolean needToSetFinalSecurityManager = securityManager.needToSetFinalSecurityManager();
            final PrintStream oldOut = System.out;
            try {
                compileAndCache();
                final Binding binding = createBinding(node);
                compiledScript.setBinding(binding);
                if (needToSetFinalSecurityManager)
                    securityManager.setFinalSecurityManager(scriptingSecurityManager);
                System.setOut(outStream);
                return compiledScript.run();
            }
            finally {
                if (needToSetFinalSecurityManager && securityManager.hasFinalSecurityManager())
                    securityManager.removeFinalSecurityManager(scriptingSecurityManager);
                System.setOut(oldOut);
                /* restore preferences (and assure that the values are unchanged!). */
                originalScriptingPermissions.restorePermissions();
            }
        }
        catch (final GroovyRuntimeException e) {
            handleScriptRuntimeException(e);
            throw new RuntimeException(e);
        }
        catch (final Throwable e) {
            if (Controller.getCurrentController().getSelection() != null)
                Controller.getCurrentModeController().getMapController().select(node);
            throw new ExecuteScriptException(e.getMessage(), e);
        }
    }

    private ScriptingSecurityManager createScriptingSecurityManager() {
        return new ScriptSecurity(script, specificPermissions, outStream).getScriptingSecurityManager();
    }

    private Binding createBinding(final NodeModel node) {
        final Binding binding = new Binding();
        binding.setVariable("c", ProxyFactory.createController(scriptContext));
        binding.setVariable("node", ProxyFactory.createNode(node, scriptContext));
        return binding;
    }

	private Script compileAndCache() throws Throwable {
		if (compileTimeStrategy.canUseOldCompiledScript())
			return compiledScript;
		removeOldScript();
		if (errorsInScript != null)
			throw errorsInScript;
		else if (script instanceof Script)
			return (Script) script;
		else
			try {
				final Binding binding = createBindingForCompilation();
				final ClassLoader classLoader = GroovyScript.class.getClassLoader();
				final GroovyShell shell = new GroovyShell(classLoader, binding, createCompilerConfiguration());
				if (script instanceof String)
					compiledScript = shell.parse((String) script);
				else if (script instanceof File)
					compiledScript = shell.parse((File) script);
				else
					throw new IllegalArgumentException();
				compileTimeStrategy.scriptCompiled();
				return compiledScript;
			}
			catch (Throwable e) {
				errorsInScript = e;
				throw e;
			}
	}

	private void removeOldScript() {
		if (compiledScript != null) {
			InvokerHelper.removeClass(compiledScript.getClass());
			compiledScript = null;
		}
	}

	private Binding createBindingForCompilation() {
        final Binding binding = new Binding();
        binding.setVariable("c", null);
        binding.setVariable("node", null);
        return binding;
    }

    private void handleScriptRuntimeException(final GroovyRuntimeException e) {
        outStream.print("message: " + e.getMessage());
        final ModuleNode module = e.getModule();
        final ASTNode astNode = e.getNode();
        int lineNumber = -1;
        if (module != null) {
            lineNumber = module.getLineNumber();
        }
        else if (astNode != null) {
            lineNumber = astNode.getLineNumber();
        }
        else {
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
        return config;
    }

    private int findLineNumberInString(final String resultString, int lineNumber) {
        final java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(".*@ line ([0-9]+).*",
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


}
