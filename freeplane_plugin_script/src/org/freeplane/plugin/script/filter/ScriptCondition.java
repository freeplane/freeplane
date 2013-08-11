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
import org.freeplane.plugin.script.GroovyScript;
import org.freeplane.plugin.script.IScript;
import org.freeplane.plugin.script.ScriptingPermissions;

public class ScriptCondition extends ASelectableCondition {
	private static final String SCRIPT_FILTER_DESCRIPTION_RESOURCE = "plugins/script_filter";
	private static final String SCRIPT_FILTER_ERROR_RESOURCE = "plugins/script_filter_error";
	static final String NAME = "script_condition";
	static final String SCRIPT = "SCRIPT";
	final private IScript script;
	private boolean errorReported = false;

	static ASelectableCondition load(final XMLElement element) {
		return new ScriptCondition(element.getAttribute(SCRIPT, null));
	}

	public ScriptCondition(final String script) {
		super();
		final ScriptingPermissions formulaPermissions = ScriptingPermissions.getFormulaPermissions();
		this.script = new GroovyScript(script, formulaPermissions);
	}

	@Override
    public boolean checkNode(final NodeModel node) {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
		final PrintStream printStream = new PrintStream(out);
		final Object result;
        try {
			result = script.setOutStream(printStream).execute(node);
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
			final String info = TextUtils.format(SCRIPT_FILTER_ERROR_RESOURCE, createDescription(),
			    node.toString(), out.toString());
			setErrorStatus(info);
        }
        return false;
	}

	private void setErrorStatus(final String info) {
		if(! errorReported){
			errorReported = true;
			JOptionPane.showMessageDialog(KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner(), info,
				TextUtils.getText("error"), JOptionPane.ERROR_MESSAGE);
		}
		LogUtils.warn(info);
		Controller.getCurrentController().getViewController().out(info.trim().replaceAll("\\s", " ").substring(0, 80));
    }

	@Override
	protected String createDescription() {
		return TextUtils.format(SCRIPT_FILTER_DESCRIPTION_RESOURCE, script.getScript());
	}

	@Override
	protected JComponent createRendererComponent() {
	    final JComponent renderer = super.createRendererComponent();
	    final Dimension preferredSize = renderer.getPreferredSize();
	    if(preferredSize.width > 200) {
	        renderer.setPreferredSize(new Dimension(200, preferredSize.height));
        }
		String scriptText = (String) script.getScript();
		if (preferredSize.width > 200 || scriptText.contains("\n")) {
			renderer.setToolTipText(HtmlUtils.plainToHTML(scriptText));
	    }
		return renderer;
    }


	@Override
    public void fillXML(final XMLElement child) {
		super.fillXML(child);
		child.setAttribute(SCRIPT, script.getScript().toString());
	}

	@Override
    protected String getName() {
	    return NAME;
    }
}
