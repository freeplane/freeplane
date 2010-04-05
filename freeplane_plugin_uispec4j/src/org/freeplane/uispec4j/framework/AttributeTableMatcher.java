/**
 * 
 */
package org.freeplane.uispec4j.framework;

import java.awt.Component;

import javax.swing.JTable;

import org.uispec4j.finder.ComponentMatcher;

public class AttributeTableMatcher implements ComponentMatcher {
	public boolean matches(Component component) {
		return component instanceof JTable && component.getClass().getSimpleName().equals("AttributeTable");
	}
}