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
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;

import org.apache.commons.lang.WordUtils;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.main.application.FreeplaneSecurityManager;
import org.freeplane.plugin.script.proxy.ProxyFactory;

/**
 * @author Dimitry Polivaev
 * 16.12.2012
 */
public class GroovyShellFreeplaneScript implements IFreeplaneScript {
	private static List<String> classpath;
	final private Object script; 
    private ScriptingPermissions specificPermissions;
    private Script compiledScript;
    private Throwable errorsInScript;
    private IFreeplaneScriptErrorHandler errorHandler; 
    private PrintStream outStream;
    private ScriptContext scriptContext; 
    
	public GroovyShellFreeplaneScript(String script){
		this((Object)script);
	}
   
	public GroovyShellFreeplaneScript(File script){
		this((Object)script);
	}
   
	public GroovyShellFreeplaneScript(String script, ScriptingPermissions permissions){
		this((Object)script, permissions);
	}
   
	public GroovyShellFreeplaneScript(File script, ScriptingPermissions permissions){
		this((Object)script, permissions);
	}
   

	private GroovyShellFreeplaneScript(Object script, ScriptingPermissions permissions) {
	    super();
	    this.script = script;
	    this.specificPermissions = permissions;
	    compiledScript = null;
	    errorsInScript = null;
	    errorHandler = IGNORING_SCRIPT_ERROR_HANDLER;
	    outStream = System.out;
	    scriptContext = null; 
    }
	
	private GroovyShellFreeplaneScript(Object script){
		this(script, null);
	}

	@Override
    public IFreeplaneScriptErrorHandler getErrorHandler() {
    	return errorHandler;
    }

	@Override
    public IFreeplaneScript setErrorHandler(IFreeplaneScriptErrorHandler pErrorHandler) {
    	this.errorHandler = pErrorHandler;
    	return this;
    }

	@Override
    public PrintStream getpOutStream() {
    	return outStream;
    }

	@Override
    public IFreeplaneScript setOutStream(PrintStream outStream) {
    	this.outStream = outStream;
    	return this;
    }

	@Override
    public ScriptContext getScriptContext() {
    	return scriptContext;
    }

	@Override
    public IFreeplaneScript setScriptContext(ScriptContext scriptContext) {
    	this.scriptContext = scriptContext;
    	return this;
    }

	@Override
    public Object getScript() {
    	return script;
    }

	@Override
    public ScriptingPermissions getSpecificPermissions() {
    	return specificPermissions;
    }

    public Script getCompiledScript() {
    	return compiledScript;
    }
	
	
	@Override
    public Object execute(final NodeModel node) {
	    try {
	    	if(errorsInScript != null)
	    		throw new ExecuteScriptException(errorsInScript.getMessage(), errorsInScript);
	    	final ScriptSecurity scriptSecurity = new ScriptSecurity(script, specificPermissions, outStream);
	    	scriptSecurity.checkScriptExecutionEnabled();
	    	ScriptingPermissions originalScriptingPermissions = new ScriptingPermissions(ResourceController.getResourceController().getProperties());
			final FreeplaneSecurityManager securityManager = (FreeplaneSecurityManager) System.getSecurityManager();
			final boolean needsSecurityManager = securityManager.needsFinalSecurityManager();
			final ScriptingSecurityManager scriptingSecurityManager = scriptSecurity.getScriptingSecurityManager();
			final PrintStream oldOut = System.out;
			try {
				compiledScript = compile();
				final Binding binding = createBinding(node);
				compiledScript.setBinding(binding);
				if (needsSecurityManager)
					securityManager.setFinalSecurityManager(scriptingSecurityManager);
				System.setOut(outStream);
				return compiledScript.run();
			}
			finally {
				if (compiledScript != null) {
					InvokerHelper.removeClass(script.getClass());
					if (needsSecurityManager)
						securityManager.removeFinalSecurityManager(scriptingSecurityManager);
				}
				System.setOut(oldOut);
				/* restore preferences (and assure that the values are unchanged!). */
				originalScriptingPermissions.restorePermissions();
			}
		}
		catch (final GroovyRuntimeException e) {
			handleGroovyRuntimeException(e);
		    throw new RuntimeException(e);

		}
		catch (final Throwable e) {
			if (Controller.getCurrentController().getSelection() != null)
				Controller.getCurrentModeController().getMapController().select(node);
			throw new ExecuteScriptException(e.getMessage(), e);
		}
    }

    private Binding createBinding(final NodeModel node) {
	    final Binding binding = new Binding();
	    binding.setVariable("c", ProxyFactory.createController(scriptContext));
	    binding.setVariable("node", ProxyFactory.createNode(node, scriptContext));
	    binding.setVariable("cookies", GenericFreeplaneScript.sScriptCookies);
	    return binding;
    }
	

	private Script compile() throws Throwable {
		if(compiledScript != null)
			return compiledScript;
		else if(errorsInScript != null)
			throw errorsInScript;
		else if(script instanceof Script)
			return (Script) script;
		else
			try{
				final Binding binding = createBindingForCompilation();
				final ClassLoader classLoader = GroovyShellFreeplaneScript.class.getClassLoader();
				final GroovyShell shell = new GroovyShell(classLoader, binding, createCompilerConfiguration()); 
				final Script compiledScript;
				if(script instanceof String)
					compiledScript = shell.parse((String)script);
				else if(script instanceof File)
					compiledScript = shell.parse((File)script);
				else throw new IllegalArgumentException();
				return compiledScript;
			}
		catch(Throwable e){
			errorsInScript = e;
			throw e;
		}

	}

    private Binding createBindingForCompilation() {
	    final Binding binding = new Binding();
	    binding.setVariable("c", null);
	    binding.setVariable("node", null);
	    binding.setVariable("cookies", GenericFreeplaneScript.sScriptCookies);
	    return binding;
    }

	private void handleGroovyRuntimeException(final GroovyRuntimeException e) {
	    final String resultString = e.getMessage();
	    outStream.print("message: " + resultString);
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
	    	lineNumber = findLineNumberInString(resultString, lineNumber);
	    }
	    outStream.print("Line number: " + lineNumber);
	    errorHandler.gotoLine(lineNumber);
	    throw new ExecuteScriptException(e.getMessage() + " at line " + lineNumber, e);
    }

	private CompilerConfiguration createCompilerConfiguration() {
		CompilerConfiguration config = new CompilerConfiguration();
		config.setScriptBaseClass(FreeplaneScriptBaseClass.class.getName());
		if (!(classpath == null || classpath.isEmpty())) {
			config.setClasspathList(classpath);
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

	/** allows to set the classpath for scripts. Due to security considerations it's not possible to set
	 * this more than once. */
	static void setClasspath(final List<String> classpath) {
		if (GroovyShellFreeplaneScript.classpath != null)
			throw new SecurityException("reset of script classpath is forbidden.");
		GroovyShellFreeplaneScript.classpath = Collections.unmodifiableList(classpath);
		if (!classpath.isEmpty())
			LogUtils.info("extending script's classpath by " + classpath);
    }

	static List<String> getClasspath() {
		return classpath;
	}
}
