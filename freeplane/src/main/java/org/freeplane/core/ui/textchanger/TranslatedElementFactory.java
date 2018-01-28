package org.freeplane.core.ui.textchanger;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JToggleButton;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.LabelAndMnemonicSetter;
import org.freeplane.core.ui.components.JAutoToggleButton;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.icon.factory.IconFactory;

public class TranslatedElementFactory {
	public static JButton createButton(Action action, String labelKey) {
		final JButton component = action != null ? new JButton(action) : new JButton();
		final String text = TextUtils.getRawText(labelKey);
		LabelAndMnemonicSetter.setLabelAndMnemonic(component, text);
		TranslatedElement.TEXT.setKey(component, labelKey);
		createTooltip(component, labelKey + ".tooltip");
		return component;
	}

	public static JButton createButton(String labelKey) {
		return createButton(null, labelKey);
	}

	public static JToggleButton createToggleButton(Action action, String labelKey) {
		final JToggleButton component = action != null ? new JAutoToggleButton(action) : new JAutoToggleButton();
		final String text = TextUtils.getRawText(labelKey);
		LabelAndMnemonicSetter.setLabelAndMnemonic(component, text);
		TranslatedElement.TEXT.setKey(component, labelKey);
		createTooltip(component, labelKey + ".tooltip");
		return component;
	}

	public static JToggleButton createToggleButton(String labelKey) {
		return createToggleButton(null, labelKey);
	}

	public static JCheckBox createCheckBox(String labelKey) {
		final String text = TextUtils.getText(labelKey);
		final JCheckBox component = new JCheckBox();
		LabelAndMnemonicSetter.setLabelAndMnemonic(component, text);
		TranslatedElement.TEXT.setKey(component, labelKey);
		createTooltip(component, labelKey + ".tooltip");
		return component;
	}

	public static void createTitledBorder(JComponent component, String labelKey) {
		final String text = TextUtils.getText(labelKey);
		component.setBorder(BorderFactory.createTitledBorder(text));
		TranslatedElement.BORDER.setKey(component, labelKey);
	}

	public static JLabel createLabel(String labelKey) {
		final String text = TextUtils.getText(labelKey);
		final JLabel component = new JLabel(text);
		TranslatedElement.TEXT.setKey(component, labelKey);
		createTooltip(component, labelKey + ".tooltip");
		return component;
	}

	public static void createTooltip(JComponent component, String labelKey) {
		final String text = TextUtils.getOptionalText(labelKey, null);
		if (text != null) {
			component.setToolTipText(text);
		}
		TranslatedElement.TOOLTIP.setKey(component, labelKey);
	}

	public static JButton createButtonWithIcon(Action action, final String iconKey, final String tooltipKey) {
		final Icon icon = ResourceController.getResourceController().getIcon(iconKey,
		    IconFactory.DEFAULT_UI_ICON_HEIGHT);
		final JButton button;
		if (action == null)
			button = new JButton(icon);
		else {
			button = new JButton(action);
			button.setText(null);
			button.setIcon(icon);
		}
		createTooltip(button, tooltipKey);
		return button;
	}

	public static JButton createButtonWithIcon(final String iconKey, final String tooltipKey) {
		return createButtonWithIcon(null, iconKey, tooltipKey);
	}

	public static JToggleButton createToggleButtonWithIconAndLabel(final String iconKey, String labelKey) {
		JToggleButton button = createToggleButton(labelKey);
		return addIcon(button, iconKey);
	}

	public static JToggleButton createToggleButtonWithIcon(final String iconKey, String tooltipKey) {
		final JToggleButton button = new JAutoToggleButton();
		createTooltip(button, tooltipKey);
		return addIcon(button, iconKey);
	}

	public static <T extends AbstractButton> T addIcon(T button, final String iconKey) {
		final Icon icon = ResourceController.getResourceController().getIcon(iconKey,
		    IconFactory.DEFAULT_UI_ICON_HEIGHT);
		button.setIcon(icon);
		return button;
	}

	public static JRadioButton createRadioButton(String labelKey) {
		final JRadioButton component = new JRadioButton();
		final String text = TextUtils.getRawText(labelKey);
		LabelAndMnemonicSetter.setLabelAndMnemonic(component, text);
		TranslatedElement.TEXT.setKey(component, labelKey);
		createTooltip(component, labelKey + ".tooltip");
		return component;
	}
}
