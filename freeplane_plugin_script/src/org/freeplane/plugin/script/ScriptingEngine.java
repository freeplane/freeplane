/*
 * Freeplane - A Program for creating and viewing MindmapsCopyright (C) 2000-2006
 * Joerg Mueller, Daniel Polansky, Christian Foltin and others.See COPYING for
 * DetailsThis program is free software; you can redistribute it and/ormodify it
 * under the terms of the GNU General Public Licenseas published by the Free
 * Software Foundation; either version 2of the License, or (at your option) any
 * later version.This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty ofMERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See theGNU General Public License for
 * more details.You should have received a copy of the GNU General Public
 * Licensealong with this program; if not, write to the Free SoftwareFoundation,
 * Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA. Created on
 * 02.09.2006
 */
/*
 * $Id: ScriptingEngine.java,v 1.1.2.20 2008/04/18 21:18:26 christianfoltin Exp
 * $
 */
package org.freeplane.plugin.script;

import groovy.lang.Binding;
import groovy.lang.GroovyRuntimeException;
import groovy.lang.GroovyShell;
import groovy.lang.Script;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;

import javax.swing.JOptionPane;

import org.apache.commons.lang.WordUtils;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.components.OptionalDontShowMeAgainDialog;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.attribute.NodeAttributeTableModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.main.application.FreeplaneSecurityManager;
import org.freeplane.plugin.script.proxy.ProxyFactory;

/**
 * @author foltin
 */
public class ScriptingEngine {
	public interface IErrorHandler {
		void gotoLine(int pLineNumber);
	}
	public static final String RESOURCES_SCRIPT_DIRECTORIES = "script_directories";
	public static final String RESOURCES_SCRIPT_CLASSPATH = "script_classpath";
	public static final String SCRIPT_PREFIX = "script";
	private static final HashMap<String, Object> sScriptCookies = new HashMap<String, Object>();
	private static List<String> classpath;
	public static final IErrorHandler IGNORING_SCRIPT_ERROR_HANDLER = new IErrorHandler() {
    	public void gotoLine(final int pLineNumber) {
    	}
    };

	/**
	 * @param permissions if null use default scripting permissions.
	 * @return the result of the script, or null, if the user has cancelled.
	 * @throws ExecuteScriptException on errors
	 */
    public static Object executeScript(final NodeModel node, final String script, final IErrorHandler pErrorHandler,
                                final PrintStream pOutStream, final ScriptContext scriptContext,
                                ScriptingPermissions permissions) {
    	return executeScript(node, (Object)script, pErrorHandler, pOutStream, scriptContext, permissions);

    }
    static Object executeScript(final NodeModel node, final File script, final IErrorHandler pErrorHandler,
                                final PrintStream pOutStream, final ScriptContext scriptContext,
                                ScriptingPermissions permissions) {
    	return executeScript(node, (Object)script, pErrorHandler,
    		pOutStream, scriptContext,
    		permissions);
    }
    
	static private Object executeScript(final NodeModel node, final Object script, final IErrorHandler pErrorHandler,
	                            final PrintStream pOutStream, final ScriptContext scriptContext,
	                            ScriptingPermissions permissions) {
		Script compiledScript = compileScriptCheckExceptions(script, pErrorHandler, pOutStream, permissions);
		return executeScript(node, script, compiledScript, pErrorHandler, pOutStream, scriptContext, permissions);
	}
	
	public static Object executeScript(final NodeModel node, final Object script, Script compiledScript,
                                       final IErrorHandler pErrorHandler, final PrintStream pOutStream,
                                       final ScriptContext scriptContext, ScriptingPermissions permissions) {
	    try {
			final FreeplaneSecurityManager securityManager = (FreeplaneSecurityManager) System.getSecurityManager();
			final boolean needsSecurityManager = securityManager.needsFinalSecurityManager();
			final ScriptingSecurityManager scriptingSecurityManager = scriptingSecurityManager(script, pOutStream, permissions);
			ScriptingPermissions originalScriptingPermissions = new ScriptingPermissions(ResourceController.getResourceController().getProperties());
			final PrintStream oldOut = System.out;
			try {
				final Binding binding = new Binding();
				binding.setVariable("c", ProxyFactory.createController(scriptContext));
				binding.setVariable("node", ProxyFactory.createNode(node, scriptContext));
				binding.setVariable("cookies", ScriptingEngine.sScriptCookies);
				compiledScript.setBinding(binding);
				if (needsSecurityManager)
					securityManager.setFinalSecurityManager(scriptingSecurityManager);
				System.setOut(pOutStream);
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
			handleGroovyRuntimeException(e, pOutStream, pErrorHandler);
		    throw new RuntimeException(e);

		}
		catch (final Throwable e) {
			if (Controller.getCurrentController().getSelection() != null)
				Controller.getCurrentModeController().getMapController().select(node);
			throw new ExecuteScriptException(e.getMessage(), e);
		}
    }
	
	private static ScriptingSecurityManager scriptingSecurityManager(final Object script, final PrintStream pOutStream,
                                                            ScriptingPermissions permissions) {
		final FreeplaneSecurityManager securityManager = (FreeplaneSecurityManager) System.getSecurityManager();
	    final boolean needsSecurityManager = securityManager.needsFinalSecurityManager();
	    // get preferences (and store them again after the script execution,
	    // such that the scripts are not able to change them).
	    if (needsSecurityManager) {
	    	if (permissions == null){
	    		permissions = new ScriptingPermissions(ResourceController.getResourceController().getProperties());
	    	}
	    	if (!permissions.executeScriptsWithoutAsking()) {
	    		final int showResult = OptionalDontShowMeAgainDialog.show("really_execute_script", "confirmation",
	    		    ScriptingPermissions.RESOURCES_EXECUTE_SCRIPTS_WITHOUT_ASKING,
	    		    OptionalDontShowMeAgainDialog.BOTH_OK_AND_CANCEL_OPTIONS_ARE_STORED);
	    		if (showResult != JOptionPane.OK_OPTION) {
	    			throw new ExecuteScriptException(new SecurityException(TextUtils.getText("script_execution_disabled")));
	    		}
	    	}
	    }
	    final ScriptingSecurityManager scriptingSecurityManager;
	    if (needsSecurityManager) {
	    	if (permissions == null){
	    		permissions = new ScriptingPermissions(ResourceController.getResourceController().getProperties());
	    	}
	    	final boolean executeSignedScripts = permissions.isExecuteSignedScriptsWithoutRestriction();
	    	final String scriptContent;
	    	if(script instanceof String)
	    		scriptContent = (String) script;
	    	else
	    		scriptContent = null;
	    	if (executeSignedScripts && scriptContent != null && new SignedScriptHandler().isScriptSigned(scriptContent, pOutStream)) {
	            scriptingSecurityManager = permissions.getPermissiveScriptingSecurityManager();
	        }
	        else
	    		scriptingSecurityManager = permissions.getScriptingSecurityManager();
	    }
	    else {
	    	// will not be used
	    	scriptingSecurityManager = null;
	    }
	    return scriptingSecurityManager;
    }
	
	private static Script compile(Object script) throws CompilationFailedException, IOException {
		if(script instanceof Script)
			return (Script) script;
		final Binding binding = new Binding();
		binding.setVariable("c", null);
		binding.setVariable("node", null);
		binding.setVariable("cookies", ScriptingEngine.sScriptCookies);
		final ClassLoader classLoader = ScriptingEngine.class.getClassLoader();
		final GroovyShell shell = new GroovyShell(classLoader, binding, createCompilerConfiguration()); 
		final Script compiledScript;
		if(script instanceof String)
			compiledScript = shell.parse((String)script);
		else if(script instanceof File)
			compiledScript = shell.parse((File)script);
		else throw new IllegalArgumentException();
		return compiledScript;
    }
	
	public static Script compileScriptCheckExceptions(Object script,  final IErrorHandler pErrorHandler, final PrintStream pOutStream, ScriptingPermissions permissions){
		try{
			return compile(script);
		}
		catch (final GroovyRuntimeException e) {
			handleGroovyRuntimeException(e, pOutStream, pErrorHandler);
		    throw new RuntimeException(e);

		}
		catch (final Throwable e) {
			throw new ExecuteScriptException(e.getMessage(), e);
		}
	}
	
	private static void handleGroovyRuntimeException(final GroovyRuntimeException e, final PrintStream pOutStream,
                                                     final IErrorHandler pErrorHandler) {
	    final String resultString = e.getMessage();
	    pOutStream.print("message: " + resultString);
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
	    	lineNumber = ScriptingEngine.findLineNumberInString(resultString, lineNumber);
	    }
	    pOutStream.print("Line number: " + lineNumber);
	    pErrorHandler.gotoLine(lineNumber);
	    throw new ExecuteScriptException(e.getMessage() + " at line " + lineNumber, e);
    }

	private static CompilerConfiguration createCompilerConfiguration() {
		CompilerConfiguration config = new CompilerConfiguration();
		config.setScriptBaseClass(FreeplaneScriptBaseClass.class.getName());
		if (!(classpath == null || classpath.isEmpty())) {
			config.setClasspathList(classpath);
		}
		return config;
	}

	public static int findLineNumberInString(final String resultString, int lineNumber) {
		final java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(".*@ line ([0-9]+).*",
		    java.util.regex.Pattern.DOTALL);
		final Matcher matcher = pattern.matcher(resultString);
		if (matcher.matches()) {
			lineNumber = Integer.parseInt(matcher.group(1));
		}
		return lineNumber;
	}

	public static Object executeScript(final NodeModel node, final String script) {
		return ScriptingEngine.executeScript(node, script, (ScriptContext)null, (ScriptingPermissions)null);
	}

	public static Object executeScript(NodeModel node, String script, Script compiledScript, PrintStream printStream, ScriptingPermissions permissions) {
		return executeScript(node, script, compiledScript, IGNORING_SCRIPT_ERROR_HANDLER, printStream, null, permissions);
    }
	
	public static Object executeScript(NodeModel node, File script, ScriptingPermissions permissions) {
		return ScriptingEngine.executeScript(node, script, ScriptingEngine.IGNORING_SCRIPT_ERROR_HANDLER, System.out, null, permissions);
	}

	public static Object executeScript(NodeModel node, String script, ScriptingPermissions permissions) {
		return ScriptingEngine.executeScript(node, script, ScriptingEngine.IGNORING_SCRIPT_ERROR_HANDLER, System.out, null, permissions);
	}
	
	public static Object executeScript(NodeModel node, String script, PrintStream printStream) {
		return ScriptingEngine.executeScript(node, script, ScriptingEngine.IGNORING_SCRIPT_ERROR_HANDLER, printStream, null, null);
	}

	public static Object executeScript(final NodeModel node, final String script, final ScriptContext scriptContext,
	                                   final ScriptingPermissions permissions) {
		return ScriptingEngine.executeScript(node, script, IGNORING_SCRIPT_ERROR_HANDLER, System.out, scriptContext, permissions);
	}

	static Object executeScriptRecursive(final NodeModel node, final File script,
	                                     final ScriptingPermissions permissions) {
		ModeController modeController = Controller.getCurrentModeController();
		final NodeModel[] children = modeController.getMapController().childrenUnfolded(node)
		    .toArray(new NodeModel[] {});
		for (final NodeModel child : children) {
			executeScriptRecursive(child, script, permissions);
		}
		return executeScript(node, script, permissions);
	}

	static void performScriptOperationRecursive(final NodeModel node) {
		ModeController modeController = Controller.getCurrentModeController();
		for (final NodeModel child : modeController.getMapController().childrenUnfolded(node)) {
			performScriptOperationRecursive(child);
		}
		performScriptOperation(node);
	}

	static void performScriptOperation(final NodeModel node) {
		final NodeAttributeTableModel attributes = NodeAttributeTableModel.getModel(node);
		if (attributes == null) {
			return;
		}
		for (int row = 0; row < attributes.getRowCount(); ++row) {
			final String attrKey = (String) attributes.getName(row);
			final Object value = attributes.getValue(row);
			if(value instanceof String){
				final String script = (String) value;
				if (attrKey.startsWith(ScriptingEngine.SCRIPT_PREFIX)) {
					executeScript(node, script);
				}
			}
		}
		return;
	}

	/** allows to set the classpath for scripts. Due to security considerations it's not possible to set
	 * this more than once. */
	static void setClasspath(final List<String> classpath) {
		if (ScriptingEngine.classpath != null)
			throw new SecurityException("reset of script classpath is forbidden.");
		ScriptingEngine.classpath = Collections.unmodifiableList(classpath);
		if (!classpath.isEmpty())
			LogUtils.info("extending script's classpath by " + classpath);
    }

	static List<String> getClasspath() {
		return classpath;
	}
	
	public static File getUserScriptDir() {
        final String userDir = ResourceController.getResourceController().getFreeplaneUserDirectory();
    	return new File(userDir, ScriptingConfiguration.USER_SCRIPTS_DIR);
    }
	static void showScriptExceptionErrorMessage(ExecuteScriptException ex) {
        if (ex.getCause() instanceof SecurityException) {
        	final String message = WordUtils.wrap(ex.getCause().getMessage(), 80, "\n    ", false);
        	UITools.errorMessage(TextUtils.format("ExecuteScriptSecurityError.text", message));
        }
        else {
        	final String message = WordUtils.wrap(ex.getMessage(), 80, "\n    ", false);
        	UITools.errorMessage(TextUtils.format("ExecuteScriptError.text", message));
        }
    }
}
