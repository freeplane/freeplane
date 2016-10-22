package org.freeplane.core.ui.textchanger;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JToggleButton;

import org.freeplane.core.ui.LabelAndMnemonicSetter;
import org.freeplane.core.util.TextUtils;

public class TranslatedElementFactory {
	
	public static JButton createButton(String labelKey) {
		final String text = TextUtils.getRawText(labelKey);
		final JButton component = new JButton();
		LabelAndMnemonicSetter.setLabelAndMnemonic(component, text);
		TranslatedElement.TEXT.setKey(component, labelKey);
		return component;
	}
	
	public static JToggleButton createToggleButton(String labelKey) {
		final String text = TextUtils.getRawText(labelKey);
		final JToggleButton component = new JToggleButton();
		LabelAndMnemonicSetter.setLabelAndMnemonic(component, text);
		TranslatedElement.TEXT.setKey(component, labelKey);
		return component;
	}
	
	public static JCheckBox createCheckBox(String labelKey) {
		final String text = TextUtils.getText(labelKey);
		final JCheckBox component = new JCheckBox();
		LabelAndMnemonicSetter.setLabelAndMnemonic(component, text);
		TranslatedElement.TEXT.setKey(component, labelKey);
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
		return component;
	}

	public static void createTooltip(JComponent component, String labelKey) {
		final String text = TextUtils.getOptionalText(labelKey);
		if(text != null){
			component.setToolTipText(text);
			TranslatedElement.TOOLTIP.setKey(component, labelKey);
		}
	}
}
