package org.freeplane.core.ui.menubuilders.menu;

import java.awt.Component;

import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JToolBar.Separator;

import org.freeplane.api.LengthUnit;
import org.freeplane.api.Quantity;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.components.JAutoToggleButton;
import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.EntryAccessor;
import org.freeplane.core.ui.svgicons.FreeplaneIconFactory;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.icon.factory.IconFactory;

public class ToolbarComponentProvider implements ComponentProvider {

	/* (non-Javadoc)
	 * @see org.freeplane.core.ui.menubuilders.menu.ComponentProvider#createComponent(org.freeplane.core.ui.menubuilders.generic.Entry)
	 */
	@Override
    public Component createComponent(Entry entry) {
	    final EntryAccessor entryAccessor = new EntryAccessor();
		final Object existingComponent = entryAccessor.getComponent(entry);
		if (existingComponent != null)
			return (Component) existingComponent;
		final AFreeplaneAction action = entryAccessor.getAction(entry);
		final JComponent component;
		if(action != null){
		    AbstractButton actionComponent;
			if (action.isSelectable()) {
				actionComponent = new JAutoToggleButton(action);
				IconReplacer.replaceByImageIcon(actionComponent);
			}
			else if(entry.builders().contains("bigIcon")) {
				actionComponent = new JBigButton(action);
				Icon icon = actionComponent.getIcon();
				Icon scaledIcon = IconFactory.getInstance().getScaledIcon(icon, new Quantity<LengthUnit>(icon.getIconHeight() * 2, LengthUnit.px));
				actionComponent.setIcon(FreeplaneIconFactory.toImageIcon(scaledIcon));
			}
			else {
				actionComponent = new JButton(action);
				IconReplacer.replaceByImageIcon(actionComponent);
			}


			component = actionComponent;
		}
		else if(entry.builders().contains("separator")){
			component = new Separator();
		}
		else if(entry.builders().contains("panel")){
			component = new JUnitPanel();
		}
		else if(entry.builders().contains("dropdownMenu")){
			String textLabel = (String) entry.getAttribute("text");
			String text = textLabel != null ? TextUtils.getRawText(textLabel) +  "..."  :  "...";
			JButtonWithDropdownMenu buttonWithMenu = new JButtonWithDropdownMenu(text);
			entry.children()
			.stream()
			.map(entryAccessor::getAction)
			.filter(x -> x != null)
			.forEach(buttonWithMenu::addMenuAction);
			component = buttonWithMenu;
		}
		else
			component = null;
	    return component;
	}
}
