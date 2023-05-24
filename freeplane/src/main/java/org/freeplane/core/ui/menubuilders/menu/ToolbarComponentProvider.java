package org.freeplane.core.ui.menubuilders.menu;

import java.awt.Component;

import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JToolBar.Separator;

import org.freeplane.api.LengthUnit;
import org.freeplane.api.Quantity;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.components.JAutoToggleButton;
import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.EntryAccessor;
import org.freeplane.core.ui.menubuilders.generic.ResourceAccessor;
import org.freeplane.core.ui.svgicons.FreeplaneIconFactory;
import org.freeplane.core.ui.textchanger.TranslatedElement;
import org.freeplane.core.ui.textchanger.TranslatedElementFactory;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.icon.factory.IconFactory;

public class ToolbarComponentProvider implements ComponentProvider {
    private final EntryAccessor entryAccessor;

	public ToolbarComponentProvider(ResourceAccessor resourceAccessor) {
		entryAccessor = new EntryAccessor(resourceAccessor);
	}

	/* (non-Javadoc)
	 * @see org.freeplane.core.ui.menubuilders.menu.ComponentProvider#createComponent(org.freeplane.core.ui.menubuilders.generic.Entry)
	 */
	@Override
    public Component createComponent(Entry entry) {
		final Object existingComponent = entryAccessor.getComponent(entry);
		if (existingComponent != null)
			return (Component) existingComponent;
		final AFreeplaneAction action = entryAccessor.getAction(entry);
		final JComponent component;
		if(action != null){
		    AbstractButton actionComponent;
			if (action.isSelectable()) {
				actionComponent = new JAutoToggleButton(action);
				IconReplacer.replaceByImageIcon(entry, actionComponent, entryAccessor);
			}
			else if(entry.builders().contains("bigIcon")) {
				actionComponent = new JBigButton(action);
				Icon icon = actionComponent.getIcon();
				Icon scaledIcon = IconFactory.getInstance().getScaledIcon(icon, new Quantity<LengthUnit>(icon.getIconHeight() * 2, LengthUnit.px));
				actionComponent.setIcon(FreeplaneIconFactory.toImageIcon(scaledIcon));
			}
			else {
				actionComponent = new JButton(action);
				IconReplacer.replaceByImageIcon(entry, actionComponent, entryAccessor);
			}


			component = actionComponent;
			component.setName(action.getKey());
		}
		else if(entry.builders().contains("separator")){
			component = new Separator();
		}
		else if(entry.builders().contains("panel")){
			component = new JUnitPanel();
		}
		else if(entry.builders().contains("dropdownMenu")){
			String textKey = (String) entry.getAttribute("text");
			String text = textKey != null ? TextUtils.getText(textKey) +  "..."  :  "...";
			String iconKey = (String) entry.getAttribute("icon");
			Icon icon = ResourceController.getResourceController().getIcon(iconKey != null ? iconKey : "arrowDown.icon");
			String tooltipKey = (String) entry.getAttribute("tooltip");
			JButtonWithDropdownMenu buttonWithMenu = new JButtonWithDropdownMenu(text, icon);
			IconReplacer.replaceByScaledImageIcon(buttonWithMenu);
			if(textKey != null)
				TranslatedElement.TEXT.setKey(buttonWithMenu, textKey);
			TranslatedElementFactory.createTooltip(buttonWithMenu, tooltipKey);
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
