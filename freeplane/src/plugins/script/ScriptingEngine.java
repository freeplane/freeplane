/*
 * FreeMind - A Program for creating and viewing MindmapsCopyright (C) 2000-2006
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
package plugins.script;

import groovy.lang.Binding;
import groovy.lang.GroovyRuntimeException;
import groovy.lang.GroovyShell;

import java.awt.event.ActionEvent;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Matcher;

import javax.swing.JOptionPane;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.freeplane.core.controller.Controller;
import org.freeplane.core.map.NodeModel;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.ActionDescriptor;
import org.freeplane.core.ui.FreeplaneAction;
import org.freeplane.core.ui.components.OptionalDontShowMeAgainDialog;
import org.freeplane.core.util.Tools;
import org.freeplane.core.util.Tools.BooleanHolder;
import org.freeplane.map.attribute.AttributeController;
import org.freeplane.map.attribute.NodeAttributeTableModel;
import org.freeplane.map.attribute.mindmapnode.MAttributeController;
import org.freeplane.map.text.TextController;
import org.freeplane.map.text.mindmapmode.MTextController;
import org.freeplane.modes.mindmapmode.MModeController;
import org.freeplane.startup.FreeMindSecurityManager;

/**
 * @author foltin
 */
@ActionDescriptor(name = "plugins/ScriptingEngine.xml_name", keyStroke = "keystroke_plugins/ScriptingEngine.keystroke.evaluate", locations = { "/menu_bar/extras/first/scripting" })
class ScriptingEngine extends FreeplaneAction {
	public interface IErrorHandler {
		void gotoLine(int pLineNumber);
	}

	public static final String SCRIPT_PREFIX = "script";
	private static final HashMap sScriptCookies = new HashMap();

	/**
	 * @param node
	 * @param pAlreadyAScriptExecuted
	 * @param script
	 * @param pMindMapController
	 * @param pScriptCookies
	 * @return true, if further scripts can be executed, false, if the user
	 *         canceled or an error occurred.
	 */
	static boolean executeScript(final NodeModel node, final BooleanHolder pAlreadyAScriptExecuted,
	                             String script, final MModeController pMindMapController,
	                             final IErrorHandler pErrorHandler, final PrintStream pOutStream,
	                             final HashMap pScriptCookies) {
		if (!pAlreadyAScriptExecuted.getValue()) {
			final int showResult = new OptionalDontShowMeAgainDialog(Controller.getController()
			    .getViewController().getJFrame(), pMindMapController.getMapController()
			    .getSelectedView(), "really_execute_script", "confirmation",
			    new OptionalDontShowMeAgainDialog.StandardPropertyHandler(
			        ResourceController.RESOURCES_EXECUTE_SCRIPTS_WITHOUT_ASKING),
			    OptionalDontShowMeAgainDialog.ONLY_OK_SELECTION_IS_STORED).show().getResult();
			if (showResult != JOptionPane.OK_OPTION) {
				return false;
			}
		}
		pAlreadyAScriptExecuted.setValue(true);
		final Binding binding = new Binding();
		binding.setVariable("c", pMindMapController);
		binding.setVariable("node", node);
		binding.setVariable("cookies", ScriptingEngine.sScriptCookies);
		final GroovyShell shell = new GroovyShell(binding);
		boolean assignResult = false;
		String assignTo = null;
		if (script.startsWith("=")) {
			script = script.substring(1);
			assignResult = true;
		}
		else {
			final int indexOfEquals = script.indexOf('=');
			if (indexOfEquals > 0) {
				final String start = script.substring(0, indexOfEquals);
				if (start.matches("[a-zA-Z0-9_]+")) {
					assignTo = start;
					script = script.substring(indexOfEquals + 1);
					assignResult = true;
				}
			}
		}
		/*
		 * get preferences (and store them again after the script execution,
		 * such that the scripts are not able to change them).
		 */
		final String executeWithoutAsking = Controller.getResourceController().getProperty(
		    ResourceController.RESOURCES_EXECUTE_SCRIPTS_WITHOUT_ASKING);
		final String executeWithoutFileRestriction = Controller.getResourceController()
		    .getProperty(ResourceController.RESOURCES_EXECUTE_SCRIPTS_WITHOUT_FILE_RESTRICTION);
		final String executeWithoutNetworkRestriction = Controller.getResourceController()
		    .getProperty(ResourceController.RESOURCES_EXECUTE_SCRIPTS_WITHOUT_NETWORK_RESTRICTION);
		final String executeWithoutExecRestriction = Controller.getResourceController()
		    .getProperty(ResourceController.RESOURCES_EXECUTE_SCRIPTS_WITHOUT_EXEC_RESTRICTION);
		final String signedScriptsWithoutRestriction = Controller.getResourceController()
		    .getProperty(ResourceController.RESOURCES_SIGNED_SCRIPT_ARE_TRUSTED);
		/* *************** */
		/* Signature */
		/* *************** */
		final PrintStream oldOut = System.out;
		Object value = null;
		GroovyRuntimeException e1 = null;
		Throwable e2 = null;
		boolean filePerm = Tools.isPreferenceTrue(executeWithoutFileRestriction);
		boolean networkPerm = Tools.isPreferenceTrue(executeWithoutNetworkRestriction);
		boolean execPerm = Tools.isPreferenceTrue(executeWithoutExecRestriction);
		if (Tools.isPreferenceTrue(signedScriptsWithoutRestriction)) {
			final boolean isSigned = new SignedScriptHandler().isScriptSigned(script, pOutStream);
			if (isSigned) {
				filePerm = true;
				networkPerm = true;
				execPerm = true;
			}
		}
		final ScriptingSecurityManager scriptingSecurityManager = new ScriptingSecurityManager(
		    filePerm, networkPerm, execPerm);
		final FreeMindSecurityManager securityManager = (FreeMindSecurityManager) System
		    .getSecurityManager();
		try {
			System.setOut(pOutStream);
			securityManager.setFinalSecurityManager(scriptingSecurityManager);
			value = shell.evaluate(script);
		}
		catch (final GroovyRuntimeException e) {
			e1 = e;
		}
		catch (final Throwable e) {
			e2 = e;
		}
		finally {
			securityManager.setFinalSecurityManager(scriptingSecurityManager);
			System.setOut(oldOut);
			/* restore preferences (and assure that the values are unchanged!). */
			Controller.getResourceController().setProperty(
			    ResourceController.RESOURCES_EXECUTE_SCRIPTS_WITHOUT_ASKING, executeWithoutAsking);
			Controller.getResourceController().setProperty(
			    ResourceController.RESOURCES_EXECUTE_SCRIPTS_WITHOUT_FILE_RESTRICTION,
			    executeWithoutFileRestriction);
			Controller.getResourceController().setProperty(
			    ResourceController.RESOURCES_EXECUTE_SCRIPTS_WITHOUT_NETWORK_RESTRICTION,
			    executeWithoutNetworkRestriction);
			Controller.getResourceController().setProperty(
			    ResourceController.RESOURCES_EXECUTE_SCRIPTS_WITHOUT_EXEC_RESTRICTION,
			    executeWithoutExecRestriction);
			Controller.getResourceController().setProperty(
			    ResourceController.RESOURCES_SIGNED_SCRIPT_ARE_TRUSTED,
			    signedScriptsWithoutRestriction);
		}
		/*
		 * Cover exceptions in normal security context (ie. no problem with
		 * (log) file writing etc.)
		 */
		if (e1 != null) {
			final String resultString = e1.getMessage();
			pOutStream.print("message: " + resultString);
			final ModuleNode module = e1.getModule();
			final ASTNode astNode = e1.getNode();
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
			return false;
		}
		if (e2 != null) {
			org.freeplane.core.util.Tools.logException(e2);
			pOutStream.print(e2.getMessage());
			final String cause = ((e2.getCause() != null) ? e2.getCause().getMessage() : "");
			final String message = ((e2.getMessage() != null) ? e2.getMessage() : "");
			Controller.getController().errorMessage(
			    e2.getClass().getName() + ": " + cause
			            + ((cause.length() != 0 && message.length() != 0) ? ", " : "") + message);
			return false;
		}
		pOutStream.print(Controller.getText("plugins/ScriptEditor/window.Result") + value);
		if (assignResult && value != null) {
			if (assignTo == null) {
				((MTextController) TextController.getController(pMindMapController)).setNodeText(
				    node, value.toString());
			}
			else {
				((MAttributeController) AttributeController.getController(pMindMapController))
				    .editAttribute(node, assignTo, value.toString());
			}
		}
		return true;
	}

	public static int findLineNumberInString(final String resultString, int lineNumber) {
		final java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
		    ".*@ line ([0-9]+).*", java.util.regex.Pattern.DOTALL);
		final Matcher matcher = pattern.matcher(resultString);
		if (matcher.matches()) {
			lineNumber = Integer.parseInt(matcher.group(1));
		}
		return lineNumber;
	}

	final private ScriptingRegistration reg;

	public ScriptingEngine(final ScriptingRegistration reg) {
		super();
		this.reg = reg;
	}

	public void actionPerformed(final ActionEvent e) {
		final NodeModel node = Controller.getController().getMap().getRootNode();
		final BooleanHolder booleanHolder = new BooleanHolder(false);
		performScriptOperation(node, booleanHolder);
	}

	private void performScriptOperation(final NodeModel node,
	                                    final BooleanHolder pAlreadyAScriptExecuted) {
		Controller.getController().getViewController().setWaitingCursor(true);
		for (final Iterator iter = node.getModeController().getMapController().childrenUnfolded(
		    node); iter.hasNext();) {
			final NodeModel element = (NodeModel) iter.next();
			performScriptOperation(element, pAlreadyAScriptExecuted);
		}
		final NodeAttributeTableModel attributes = NodeAttributeTableModel.getModel(node);
		if (attributes == null) {
			return;
		}
		for (int row = 0; row < attributes.getRowCount(); ++row) {
			final String attrKey = (String) attributes.getName(row);
			final String script = (String) attributes.getValue(row);
			if (attrKey.startsWith(ScriptingEngine.SCRIPT_PREFIX)) {
				final boolean result = ScriptingEngine.executeScript(node, pAlreadyAScriptExecuted,
				    script, (MModeController) getModeController(), new IErrorHandler() {
					    public void gotoLine(final int pLineNumber) {
					    }
				    }, System.out, reg.getScriptCookies());
				if (!result) {
					break;
				}
			}
		}
		Controller.getController().getViewController().setWaitingCursor(false);
	}
}
