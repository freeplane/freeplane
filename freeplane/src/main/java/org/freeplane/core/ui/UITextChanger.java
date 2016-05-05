package org.freeplane.core.ui;

import java.awt.Component;
import java.awt.KeyEventDispatcher;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.TitledBorder;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.TextUtils;

public class UITextChanger implements KeyEventDispatcher {
	private static final String TEXT_FIELD_TRANSLATION_KEY = TranslatedElement.class.getName() + ".translationKey";

	public enum TranslatedElement {
		BORDER, TEXT, TOOLTIP;
		public String getKey(JComponent component) {
			return (String) (component).getClientProperty(this);
		}

		public void setKey(JComponent component, String key) {
			component.putClientProperty(this, key);
		}

		public String getTitleKey() {
			return "TranslatedElement." + name();
		}
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent e) {
		final int modifiers = KeyEvent.SHIFT_DOWN_MASK | KeyEvent.CTRL_DOWN_MASK;
		if(((e.getModifiersEx() & modifiers) == modifiers) && e.getKeyCode() == KeyEvent.VK_F10) {
			if (e.getID() == KeyEvent.KEY_PRESSED) {
				replaceComponentText();
			}
			return true;
		}
		return false;
	}

	private void replaceComponentText() {
		for (Window window : Window.getWindows()) {
	        final Point mousePosition = window.getMousePosition(true);
			if (mousePosition != null) {
				final Component componentUnderMouse = SwingUtilities.getDeepestComponentAt(window, mousePosition.x, mousePosition.y);
				replaceComponentText(componentUnderMouse);
			}
	    }
	}

	private void replaceComponentText(Component component) {
		if(! (component instanceof JComponent))
			return;
		ArrayList<JTextField> textFields = createTextEditors(component);
		if (textFields.isEmpty())
			return;
		int exitCode = showDialog(component, textFields);
		if (exitCode == JOptionPane.OK_OPTION) {
			setEditedTexts((JComponent) component, textFields);
		}
	}

	private int showDialog(Component component, ArrayList<JTextField> textFields) {
		return JOptionPane.showConfirmDialog(component, textFields.toArray(), "replace text",
		    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
	}

	private void setEditedTexts(JComponent component, ArrayList<JTextField> textFields) {
		for (JTextField textField : textFields) {
			setEditedText(component, textField);
		}
	}

	private void setEditedText(JComponent component, JTextField textField) {
		String translationKey = (String) textField.getClientProperty(TEXT_FIELD_TRANSLATION_KEY);
		String newText = textField.getText();
		if (newText.isEmpty())
			newText = null;
		ResourceController.getResourceController().putUserResourceString(translationKey, newText);
		if (newText == null)
			newText = TextUtils.getRawText(translationKey);
		TranslatedElement element = (TranslatedElement) textField.getClientProperty(TranslatedElement.class);
		switch (element) {
			case TEXT:
				setNewText(component, newText);
				break;
			case BORDER:
				setNewBorderTitle(component, newText);
				break;
			case TOOLTIP:
				component.setToolTipText(newText);
				break;
		}
	}

	private void setNewBorderTitle(JComponent component, String newText) {
		Border border = component.getBorder();
		setNewTitle(border, newText);
	}

	private void setNewTitle(Border border, String newText) {
		if (border instanceof TitledBorder) {
			((TitledBorder) border).setTitle(newText);
		}
		else if (border instanceof CompoundBorder) {
			CompoundBorder compoundBorder = (CompoundBorder) border;
			setNewTitle(compoundBorder.getInsideBorder(), newText);
			setNewTitle(compoundBorder.getOutsideBorder(), newText);
		}
	}

	private void setNewText(Component component, String text) {
		if (component instanceof AbstractButton)
			LabelAndMnemonicSetter.setLabelAndMnemonic(((AbstractButton) component), text);
		else if (component instanceof JLabel)
			((JLabel) component).setText(TextUtils.removeMnemonic(text));
	}

	private ArrayList<JTextField> createTextEditors(Component component) {
		ArrayList<JTextField> textFields = new ArrayList<>(TranslatedElement.values().length);
		for (TranslatedElement element : TranslatedElement.values()) {
			final String translationKey = element.getKey((JComponent) component);
			if (translationKey != null) {
				JTextField textField = createTextField(element, translationKey);
				textFields.add(textField);
			}
		}
		return textFields;
	}

	private JTextField createTextField(TranslatedElement element, final String translationKey) {
		JTextField textField = new JTextField(TextUtils.getRawText(translationKey, ""));
		String titleKey = element.getTitleKey();
		UITools.addTitledBorder(textField, TextUtils.getRawText(titleKey), 10);
		textField.putClientProperty(TranslatedElement.class, element);
		textField.putClientProperty(TEXT_FIELD_TRANSLATION_KEY, translationKey);
		TranslatedElement.BORDER.setKey(textField, titleKey);
		return textField;
	}
}

