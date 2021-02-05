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
import org.freeplane.plugin.script.*;
import org.freeplane.plugin.script.ScriptContext;

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
    private String errorMessage;

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
		final Object result;
        try (final PrintStream printStream = new PrintStream(new ByteArrayOutputStream())){
			result = scriptRunner.setOutStream(printStream).execute(node);
			if(result instanceof Boolean)
				return (Boolean) result;
			if(result instanceof Number)
				return ((Number) result).doubleValue() != 0;
	        printStream.println(this + ": got '" + result + "' for " + node);
            final String info = createErrorDescription(node, String.valueOf(result), SCRIPT_FILTER_ERROR_RESOURCE);
	        setErrorStatus(info);
        }
        catch (ExecuteScriptException e) {
			final String info = createErrorDescription(node, String.valueOf(e.getMessage()), SCRIPT_FILTER_EXECUTE_ERROR_RESOURCE);
			setErrorStatus(info);
        }
        return false;
	}

    private String createErrorDescription(final NodeModel node, String message, String template) {
        final String info = TextUtils.format(template, !errorReported ?  createDescription() : "...",
                node.createID() + ", " +  node.toString(), message.equals(errorMessage) ? "..." : message);
        errorMessage = message;
        return info;
    }

	@Override
	public boolean checkNodeInFormulaContext(NodeModel node){
		NodeScript nodeScript = new NodeScript(node, source);
		final ScriptContext scriptContext = new ScriptContext(nodeScript);
		if (! FormulaThreadLocalStacks.INSTANCE.push(scriptContext))
			return false;
		scriptRunner.setScriptContext(scriptContext);
		try {
			final boolean checkNode = checkNode(node);
			return checkNode;
		}
		finally {
			FormulaThreadLocalStacks.INSTANCE.pop();
			scriptRunner.setScriptContext(null);
		}
	}


	private void setErrorStatus(final String info) {
	    LogUtils.warn(info);
	    if(! errorReported){
	        errorReported = true;
	        JOptionPane.showMessageDialog(KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner(), info,
	                TextUtils.getText("error"), JOptionPane.ERROR_MESSAGE);
	        String message = info.trim().replaceAll("\\s", " ");
	        if(message.length() > 80)
	            message = message.substring(0, 80);
	        Controller.getCurrentController().getViewController().out(message);
	    }
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
