package org.freeplane.core.ui.menubuilders.menu;

import java.awt.Component;

public abstract class JRibbonContainer {
	
	public static final int FIRST = Integer.MIN_VALUE+100;
	public static final int PREPEND = Integer.MIN_VALUE+100000;
	public static final int APPEND = Integer.MAX_VALUE-100000;
	public static final int LAST = Integer.MAX_VALUE-100;
	
	abstract public void add(Component component, Object constraints, int index);
	
	public void add(Component component, Object constraints) {
		add(component, constraints, APPEND);
	}
	
	public void add(Component component, int index) {
		add(component, null, index);
	}
	
	public void add(Component component) {
		add(component, null, APPEND);
	}
}
