package org.freeplane.plugin.script.filter;

import java.awt.KeyboardFocusManager;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import javax.swing.JOptionPane;

import org.freeplane.core.util.TextUtils;
import org.freeplane.features.filter.FilterCancelledException;
import org.freeplane.features.filter.condition.ASelectableCondition;
import org.freeplane.features.map.NodeModel;
import org.freeplane.n3.nanoxml.XMLElement;
import org.freeplane.plugin.script.ExecuteScriptException;
import org.freeplane.plugin.script.ScriptingEngine;

public class ScriptCondition extends ASelectableCondition {
	private static final String SCRIPT_FILTER_DESCRIPTION_RESOURCE = "plugins/script_filter";
	private static final String SCRIPT_FILTER_ERROR_RESOURCE = "plugins/script_filter_error";
	static final String NAME = "script_condition";
	static final String SCRIPT = "SCRIPT";
	final private String script;

	static ASelectableCondition load(final XMLElement element) {
		return new ScriptCondition(element.getAttribute(SCRIPT, null));
	}

	public ScriptCondition(final String script) {
		super();
		this.script = script;
	}

	public boolean checkNode(final NodeModel node) {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
		final PrintStream printStream = new PrintStream(out);
		final Object result;
        try {
			result = ScriptingEngine.executeScript(node, script, printStream);
			if(result instanceof Boolean)
				return (Boolean) result;
			if(result instanceof Number)
				return ((Number) result).doubleValue() != 0;
	        printStream.println(this + ": got '" + result + "' for " + node);
	        printStream.close();
	        final String info = TextUtils.format(SCRIPT_FILTER_ERROR_RESOURCE, createDescription(),
	        	node.toString(), String.valueOf(result));
	        cancel(info, true);
        }
        catch (ExecuteScriptException e) {
        	printStream.close();
			final String info = TextUtils.format(SCRIPT_FILTER_ERROR_RESOURCE, createDescription(),
			    node.toString(), out.toString());
			cancel(info, false);
        }
        return false;
	}

	private void cancel(final String info, boolean cancel) {
		if(cancel){
			JOptionPane.showMessageDialog(KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner(), info,
				TextUtils.getText("error"), JOptionPane.ERROR_MESSAGE);
		}
		else{
			final int result = JOptionPane.showConfirmDialog(KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner(), info,
				TextUtils.getText("error"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE);
			if(result == JOptionPane.OK_OPTION)
				return;
		}
		throw new FilterCancelledException(info);
    }

	@Override
	protected String createDescription() {
		return TextUtils.format(SCRIPT_FILTER_DESCRIPTION_RESOURCE, script);
	}

	public void fillXML(final XMLElement child) {
		super.fillXML(child);
		child.setAttribute(SCRIPT, script);
	}

	@Override
    protected String getName() {
	    return NAME;
    }
}
