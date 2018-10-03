package org.freeplane.plugin.formula.dependencies;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.features.attribute.Attribute;
import org.freeplane.features.attribute.AttributeController;
import org.freeplane.features.attribute.HighlighedAttributes;
import org.freeplane.features.attribute.NodeAttribute;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.plugin.formula.FormulaPluginUtils;
import org.freeplane.plugin.script.AccessedValues;
import org.freeplane.plugin.script.FormulaUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Collection;

class TracePrecedentCellsAction extends AFreeplaneAction {
	private static final long serialVersionUID = 1L;
	private Collection<NodeModel> precedentNodes;
	private Collection<Object> precedentValues;
	private HighlighedAttributes highlighedAttributes;

	public TracePrecedentCellsAction() {
		super(FormulaPluginUtils.getFormulaKey("TracePrecedentCellsAction"));
		highlighedAttributes = new HighlighedAttributes();
		precedentNodes = null;
		precedentValues = null;
	}

	public void actionPerformed(final ActionEvent e) {
		final AccessedValues accessedValues;
		NodeAttribute attribute = AttributeController.getSelectedAttribute();
		if(attribute != null) {
			Object value = attribute.value();
			if(FormulaUtils.containsFormula(value)) {
				accessedValues = FormulaUtils.getAccessedValues(attribute.node, (String) value);
			}
			else
				return;
		}
		else {
			NodeModel node = Controller.getCurrentController().getSelection().getSelected();
			Object userObject = node.getUserObject();
			if(FormulaUtils.containsFormula(userObject)) {
				accessedValues = FormulaUtils.getAccessedValues(attribute.node, (String) userObject);
			}
			else
				return;
		}
		precedentNodes = accessedValues.getAccessedNodes();
		precedentValues = accessedValues.getAccessedValues();
		JComponent mapViewComponent = Controller.getCurrentController().getMapViewManager().getMapViewComponent();
		precedentValues.stream().filter(x -> x instanceof Attribute).map(x -> (Attribute)x).forEach(highlighedAttributes::add);
		mapViewComponent.putClientProperty(HighlighedAttributes.class, highlighedAttributes);
	}
}
