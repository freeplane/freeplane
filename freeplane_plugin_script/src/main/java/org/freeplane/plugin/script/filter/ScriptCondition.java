package org.freeplane.plugin.script.filter;

import java.awt.Dimension;
import java.awt.KeyboardFocusManager;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import javax.swing.JComponent;
import javax.swing.JOptionPane;

import org.freeplane.core.util.HtmlUtils;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.filter.condition.ASelectableCondition;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.n3.nanoxml.XMLElement;
import org.freeplane.plugin.script.ExecuteScriptException;
import org.freeplane.plugin.script.FormulaThreadLocalStack;
import org.freeplane.plugin.script.GroovyScript;
import org.freeplane.plugin.script.NodeScript;
import org.freeplane.plugin.script.ScriptContext;
import org.freeplane.plugin.script.ScriptRunner;
import org.freeplane.plugin.script.ScriptingPermissions;

public class ScriptCondition extends ASelectableCondition {
	private static final String SCRIPT_FILTER_DESCRIPTION_RESOURCE = "plugins/script_filter";
	private static final String SCRIPT_FILTER_ERROR_RESOURCE = "plugins/script_filter_error";
	private static final String SCRIPT_FILTER_EXECUTE_ERROR_RESOURCE = "plugins/script_filter_execute_error";
	static final String NAME = "script_condition";
	static final String TAG_NAME = "script";
	static final String ATTRIB_NAME = "SCRIPT"; // for backward compatibility
	final private ScriptRunner scriptRunner;
	final private String source;
	private boolean errorReported = false;

	static ASelectableCondition load(final XMLElement element) {
	    final XMLElement child = element.getFirstChildNamed(TAG_NAME);
	    if (child != null) {
		return new ScriptCondition(child.getContent());
	    } else {
		// read attribute for backward compatibility
		return new ScriptCondition(element.getAttribute(ATTRIB_NAME, null));
	    }
	}

	@Override
    public void fillXML(final XMLElement element) {
		final XMLElement child = new XMLElement(TAG_NAME);
		super.fillXML(element);
		child.setContent(source);
		element.addChild(child);
	}

	@Override
    protected String getName() {
	    return NAME;
    }

	public ScriptCondition(final String script) {
		super();
		final ScriptingPermissions formulaPermissions = ScriptingPermissions.getFormulaPermissions();
		this.source = script;
		this.scriptRunner = new ScriptRunner(new GroovyScript(script, formulaPermissions));
	}

	@Override
    public boolean checkNode(final NodeModel node) {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
		final PrintStream printStream = new PrintStream(out);
		final Object result;
        try {
			result = scriptRunner.setOutStream(printStream).execute(node);
			if(result instanceof Boolean)
				return (Boolean) result;
			if(result instanceof Number)
				return ((Number) result).doubleValue() != 0;
	        printStream.println(this + ": got '" + result + "' for " + node);
	        printStream.close();
	        final String info = TextUtils.format(SCRIPT_FILTER_ERROR_RESOURCE, createDescription(),
	        	node.toString(), String.valueOf(result));
	        setErrorStatus(info);
        }
        catch (ExecuteScriptException e) {
        	printStream.close();
			final String info = TextUtils.format(SCRIPT_FILTER_EXECUTE_ERROR_RESOURCE, createDescription(),
			    node.toString(), e.getMessage());
			setErrorStatus(info);
        }
        return false;
	}

	@Override
	public boolean checkNodeInFormulaContext(NodeModel node){
		final ScriptContext scriptContext = new ScriptContext(new NodeScript(node, source));
		FormulaThreadLocalStack.INSTANCE.push(node, source);
		scriptRunner.setScriptContext(scriptContext);
		try {
			final boolean checkNode = checkNode(node);
			return checkNode;
		}
		finally {
			FormulaThreadLocalStack.INSTANCE.pop();
			scriptRunner.setScriptContext(null);
		}
	}


	private void setErrorStatus(final String info) {
		if(! errorReported){
			errorReported = true;
			JOptionPane.showMessageDialog(KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner(), info,
				TextUtils.getText("error"), JOptionPane.ERROR_MESSAGE);
		}
		LogUtils.warn(info);
		String message = info.trim().replaceAll("\\s", " ");
		if(message.length() > 80)
			message = message.substring(0, 80);
		Controller.getCurrentController().getViewController().out(message);
    }

	@Override
	protected String createDescription() {
		return TextUtils.format(SCRIPT_FILTER_DESCRIPTION_RESOURCE, source);
	}

	@Override
	protected JComponent createRendererComponent() {
	    final JComponent renderer = super.createRendererComponent();
	    final Dimension preferredSize = renderer.getPreferredSize();
	    if(preferredSize.width > 200) {
	        renderer.setPreferredSize(new Dimension(200, preferredSize.height));
        }
		if (preferredSize.width > 200 || source.contains("\n")) {
			renderer.setToolTipText(HtmlUtils.plainToHTML(source));
	    }
		return renderer;
    }

}
