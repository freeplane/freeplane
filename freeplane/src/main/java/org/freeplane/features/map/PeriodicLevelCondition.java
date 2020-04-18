package org.freeplane.features.map;

import org.freeplane.core.util.TextUtils;
import org.freeplane.features.filter.condition.ASelectableCondition;
import org.freeplane.n3.nanoxml.XMLElement;

public class PeriodicLevelCondition extends ASelectableCondition {
	public static final String NAME = "node_periodic_level_condition";
	private final int period;
	private final int remainder;
	
	
	public PeriodicLevelCondition(int period, int remainder) {
		super();
		this.period = period;
		this.remainder = remainder;
	}

	@Override
	protected String getName() {
		return NAME;
	}

	public boolean checkNode(NodeModel node) {
		final int nodeLevel = node.getNodeLevel();
		return nodeLevel > 0 && nodeLevel % period == remainder;
	}

	public static ASelectableCondition load(XMLElement element) {
		int period = Integer.valueOf(element.getAttribute("PERIOD", null));
		int remainder = Integer.valueOf(element.getAttribute("REMAINDER", null));
	    return new PeriodicLevelCondition(period, remainder);
    }
	
	

	@Override
	protected void fillXML(XMLElement element) {
		element.setAttribute("PERIOD", Integer.toString(period));
		element.setAttribute("REMAINDER", Integer.toString(remainder));
	}

	@Override
    protected String createDescription() {
	    return TextUtils.format("periodic_formula", period, remainder);
    }

	public static PeriodicLevelCondition[] createConditions(int n) {
		PeriodicLevelCondition[] conditions = new PeriodicLevelCondition[n * (n + 1) / 2 - 1];
		int k = 0;
		for(int i = 2; i <= n; i++)
			for(int j = 0; j < i; j++)
				conditions[k++] = new PeriodicLevelCondition(i, j);
		return conditions;
	}
}
