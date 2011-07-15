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

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.freeplane.core.ui.components.OptionalDontShowMeAgainDialog;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.attribute.AttributeController;
import org.freeplane.features.attribute.NodeAttributeTableModel;
import org.freeplane.features.attribute.mindmapmode.MAttributeController;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.text.TextController;
import org.freeplane.features.text.mindmapmode.MTextController;
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
	private static final long serialVersionUID = 1L;
	private static final HashMap<String, Object> sScriptCookies = new HashMap<String, Object>();
	private static Boolean noUserPermissionRequired = false;
	private static Pattern attributeNamePattern = Pattern.compile("^([a-zA-Z0-9_]*)=");
	private static List<String> classpath;
	private static final IErrorHandler scriptErrorHandler = new IErrorHandler() {
    	public void gotoLine(final int pLineNumber) {
    	}
    };

	/**
	 * @param restricted TODO
	 * @return the result of the script, or null, if the user has cancelled.
	 * @throws ExecuteScriptException on errors
	 */
	static Object executeScript(final NodeModel node, String script, final IErrorHandler pErrorHandler,
	                            final PrintStream pOutStream, final ScriptContext scriptContext, boolean restricted) {
		if (!noUserPermissionRequired) {
			final int showResult = OptionalDontShowMeAgainDialog.show("really_execute_script", "confirmation",
			    ScriptingPermissions.RESOURCES_EXECUTE_SCRIPTS_WITHOUT_ASKING,
			    OptionalDontShowMeAgainDialog.BOTH_OK_AND_CANCEL_OPTIONS_ARE_STORED);
			if (showResult != JOptionPane.OK_OPTION) {
				throw new ExecuteScriptException(new SecurityException(TextUtils.getText("script_execution_disabled")));
			}
		}
		noUserPermissionRequired = Boolean.TRUE;
		final Binding binding = new Binding();
		binding.setVariable("c", ProxyFactory.createController(scriptContext));
		binding.setVariable("node", ProxyFactory.createNode(node, scriptContext));
		binding.setVariable("cookies", ScriptingEngine.sScriptCookies);
		boolean assignResult = false;
		String assignTo = null;
		final Matcher matcher = attributeNamePattern.matcher(script);
		if (matcher.matches()) {
			assignResult = true;
			String attributeName = matcher.group(1);
			if (attributeName.length() == 0) {
				script = script.substring(1);
			}
			else {
				assignTo = attributeName;
				script = script.substring(matcher.end());
			}
		}
		final PrintStream oldOut = System.out;
		// get preferences (and store them again after the script execution,
		// such that the scripts are not able to change them).
		final ScriptingPermissions scriptingPermissions = new ScriptingPermissions();
		scriptingPermissions.initFromPreferences();
		final FreeplaneSecurityManager securityManager = (FreeplaneSecurityManager) System.getSecurityManager();
		final ScriptingSecurityManager scriptingSecurityManager;
		final boolean needsSecurityManager = securityManager.needsFinalSecurityManager();
		if (needsSecurityManager) {
			final boolean executeSignedScripts = scriptingPermissions.isExecuteSignedScriptsWithoutRestriction();
			if (restricted)
				scriptingSecurityManager = scriptingPermissions.getRestrictedScriptingSecurityManager();
			else if (executeSignedScripts && new SignedScriptHandler().isScriptSigned(script, pOutStream))
				scriptingSecurityManager = scriptingPermissions.getPermissiveScriptingSecurityManager();
			else
				scriptingSecurityManager = scriptingPermissions.getScriptingSecurityManager();
		}
		else {
			scriptingSecurityManager = null;
		}
		try {
			System.setOut(pOutStream);
			final GroovyShell shell = new GroovyShell(binding, createCompilerConfiguration()) {
				/**
				 * Evaluates some script against the current Binding and returns the result
				 *
				 * @param in       the stream reading the script
				 * @param fileName is the logical file name of the script (which is used to create the class name of the script)
				 */
				@Override
				public Object evaluate(final InputStream in, final String fileName) throws CompilationFailedException {
					Script script = null;
					try {
						script = parse(in, fileName);
						if (needsSecurityManager)
							securityManager.setFinalSecurityManager(scriptingSecurityManager);
						return script.run();
					}
					finally {
						if (script != null) {
							InvokerHelper.removeClass(script.getClass());
							if (needsSecurityManager)
								securityManager.removeFinalSecurityManager(scriptingSecurityManager);
						}
					}
				}
			};
			Object result = shell.evaluate(script);
			if (assignResult && result != null) {
				if (assignTo == null) {
					((MTextController) TextController.getController()).setNodeText(node, result.toString());
				}
				else {
					((MAttributeController) AttributeController.getController()).editAttribute(node, assignTo,
					    result.toString());
				}
			}
			return result;
		}
		catch (final GroovyRuntimeException e) {
			/*
			 * Cover exceptions in normal security context (ie. no problem with
			 * (log) file writing etc.)
			 */
			// LogUtils.warn(e);
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
			throw new ExecuteScriptException(e.getMessage(), e);
		}
		catch (final Throwable e) {
			if (Controller.getCurrentController().getSelection() != null)
				Controller.getCurrentModeController().getMapController().select(node);
			// LogUtils.warn(e);
			// pOutStream.print(e.getMessage());
			throw new ExecuteScriptException(e.getMessage(), e);
		}
		finally {
			System.setOut(oldOut);
			/* restore preferences (and assure that the values are unchanged!). */
			scriptingPermissions.restorePermissions();
		}
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
		return ScriptingEngine.executeScript(node, script, null, false);
	}
	
	public static Object executeScript(final NodeModel node, final String script, final ScriptContext scriptContext,
	                                   boolean restricted) {
		return ScriptingEngine.executeScript(node, script, scriptErrorHandler, System.out, scriptContext, restricted);
	}

	static Object executeScriptRecursive(final NodeModel node, final String script) {
		ModeController modeController = Controller.getCurrentModeController();
		final NodeModel[] children = modeController.getMapController().childrenUnfolded(node).toArray(new NodeModel[]{});
        for (final NodeModel child : children) {
			executeScriptRecursive(child, script);
		}
		return executeScript(node, script);
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
			final String script = (String) attributes.getValue(row);
			if (attrKey.startsWith(ScriptingEngine.SCRIPT_PREFIX)) {
				executeScript(node, script);
			}
		}
		return;
	}

	static void setNoUserPermissionRequired(final Boolean noUserPermissionRequired) {
		ScriptingEngine.noUserPermissionRequired = noUserPermissionRequired;
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
}
