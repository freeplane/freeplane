package org.freeplane.core.ui.menubuilders.menu;

import java.util.Locale;

import javax.swing.KeyStroke;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.IAcceleratorChangeListener;
import org.freeplane.core.ui.menubuilders.action.EntriesForAction;
import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.EntryAccessor;
import org.freeplane.core.util.ActionUtils;
import org.freeplane.core.util.TextUtils;
import org.pushingpixels.flamingo.api.common.AbstractCommandButton;
import org.pushingpixels.flamingo.api.common.RichTooltip;

public class JRibbonAcceleratorChangeListener implements IAcceleratorChangeListener {

	private EntriesForAction entries;
	private EntryAccessor entryAccessor;

	public JRibbonAcceleratorChangeListener(EntriesForAction entries) {
		this.entries = entries;
		this.entryAccessor = new EntryAccessor();
	}

	@Override
	public void acceleratorChanged(AFreeplaneAction action, KeyStroke oldStroke, KeyStroke newStroke) {
		for(Entry entry : entries.entries(action)) {
			Object comp = entryAccessor.getComponent(entry);
			if(comp instanceof AbstractCommandButton) {
				updateRichTooltip((AbstractCommandButton)comp, action, newStroke);
			} 
			else if (comp instanceof JRibbonContainer) {
				comp = ((JRibbonContainer) comp).getParent();
				if(comp instanceof AbstractCommandButton) {
					updateRichTooltip((AbstractCommandButton)comp, action, newStroke);
				}
			}
		}
	}
	
	public static void updateRichTooltip(final AbstractCommandButton button, AFreeplaneAction action, KeyStroke ks) {
		RichTooltip tip = getRichTooltip(action, ks);
		if(tip != null) {
			button.setActionRichTooltip(tip);
		}
		else {
			button.setActionRichTooltip(null);
		}
	}
	
    private static RichTooltip getRichTooltip(AFreeplaneAction action, KeyStroke ks) {
		RichTooltip tip = null;
		final String tooltip = TextUtils.getRawText(action.getTooltipKey(), null);
		if (tooltip != null && !"".equals(tooltip)) {
			tip = new RichTooltip(ActionUtils.getActionTitle(action), TextUtils.removeTranslateComment(tooltip));
		}
		if(ks != null) {
			if(tip == null) {
				tip = new RichTooltip(ActionUtils.getActionTitle(action), "  ");
			}
			tip.addFooterSection(formatShortcut(ks));
		}
		return tip;
	}

	private static String formatShortcut(KeyStroke ks) {
		StringBuilder sb = new StringBuilder();
		if(ks != null) {
			String[] st = ks.toString().split("[\\s]+");
			for (String s : st) {
				if("pressed".equals(s.trim())) {
					continue;
				}
				if(sb.length() > 0) {
					sb.append(" + ");
				}
				sb.append(s.substring(0, 1).toUpperCase(Locale.ENGLISH));
				sb.append(s.substring(1));
			}
		}
		return sb.toString();
	}

}
