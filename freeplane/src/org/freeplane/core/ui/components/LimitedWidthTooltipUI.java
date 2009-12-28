package org.freeplane.core.ui.components;

import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JToolTip;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicToolTipUI;

public class LimitedWidthTooltipUI extends BasicToolTipUI {
	private static int maximumWidth = Integer.MAX_VALUE;
	static LimitedWidthTooltipUI singleton = new LimitedWidthTooltipUI();

	public static ComponentUI createUI(final JComponent c) {
		return singleton;
	}

	public static void initialize() {
		// don't hardcode class name
		final String key = "ToolTipUI";
		final Class cls = singleton.getClass();
		final String name = cls.getName();
		UIManager.put(key, name);
		UIManager.put(name, cls);
	}

	/**
	 *  set maximum width
	 *  0 = no maximum width
	 */
	public static void setMaximumWidth(final int width) {
		maximumWidth = width;
	}

	private LimitedWidthTooltipUI() {
		super();
	}

	@Override
	public Dimension getPreferredSize(final JComponent c) {
		Dimension preferredSize = super.getPreferredSize(c);
		if (preferredSize.width < maximumWidth) {
			return preferredSize;
		}
		String tipText = ((JToolTip) c).getTipText();
		final String TABLE_START = "<html><table>";
		if (!tipText.startsWith(TABLE_START)) {
			return preferredSize;
		}
		tipText = "<html><table width=\"" + maximumWidth + "\">" + tipText.substring(TABLE_START.length());
		((JToolTip) c).setTipText(tipText);
		preferredSize = super.getPreferredSize(c);
		return preferredSize;
	}
}
