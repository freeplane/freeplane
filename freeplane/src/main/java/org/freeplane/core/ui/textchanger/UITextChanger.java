package org.freeplane.core.ui.textchanger;

import java.awt.Component;
import java.awt.KeyEventDispatcher;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;

import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.TitledBorder;
import javax.swing.text.JTextComponent;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.LabelAndMnemonicSetter;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.FreeplaneIconUtils;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.mode.Controller;

class UITextChanger implements KeyEventDispatcher {
	private static final ImageIcon WEBLATE_ICON = FreeplaneIconUtils.createImageIcon("/images/weblate-32.png");
	private static final String TEXT_FIELD_TRANSLATION_KEY = TranslatedElement.class.getName() + ".translationKey";
	private TextChangeHotKeyAction textChangeAcceleratorAction;

	public UITextChanger(TextChangeHotKeyAction textChangeAcceleratorAction) {
		this.textChangeAcceleratorAction = textChangeAcceleratorAction;
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent e) {
		if (textChangeAcceleratorAction.shouldChangeTextOnEvent(e)) {
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
				replaceComponentTexts(componentUnderMouse);
			}
	    }
	}

	private void replaceComponentTexts(Component component) {
		if (component instanceof JComponent)
			replaceComponentTexts((JComponent) component);
		else
			showNoComponentFoundMessage();
	}

	private void replaceComponentTexts(JComponent c) {
		ArrayList<JTextField> textFields = createTextEditors(c);
		if (!textFields.isEmpty())
			replaceComponentTexts(c, textFields);
		else
			showNoComponentFoundMessage();
	}

	private void showNoComponentFoundMessage() {
		Controller.getCurrentController().getViewController().out(TextUtils.getRawText("no_translation_strings_found"));
	}

	private void replaceComponentTexts(JComponent c, ArrayList<JTextField> textFields) {
		int exitCode = showDialog(c, textFields);
		if (exitCode == JOptionPane.OK_OPTION) {
			setEditedTexts(c, textFields);
		}
	}

	private int showDialog(Component component, ArrayList<JTextField> textFields) {
		final JTextField focusOwner = textFields.get(0);
		setFocusWhenShowed(focusOwner);
		return JOptionPane.showConfirmDialog(component, createDisplayedComponents(textFields), "replace text",
		    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
	}

	private Component[] createDisplayedComponents(ArrayList<JTextField> textFields) {
		final Box[] components = new Box[textFields.size()];
		final UrlCreator urlCreator = new UrlCreator();
		for(int i = 0; i < textFields.size(); i++) {
			final Box box = Box.createHorizontalBox();
			final JTextField textField = textFields.get(i);
			box.add(textField);
			final String textKey = (String)textField.getClientProperty(TEXT_FIELD_TRANSLATION_KEY);
			final String url = urlCreator.createUrl(textKey);
			box.add(createGoToWeblateButton(url));
			components[i] = box;
		}
		return components;
	}
	
	static class UrlCreator{
		final ResourceController resourceController = ResourceController.getResourceController();
		String urlFormat = resourceController.getProperty("weblateUrlFormat");
		final MessageFormat urlCreator = new MessageFormat(urlFormat);
		final String languageCode = resourceController.getLanguageCode();
		public String createUrl(String key) {
			return urlCreator.format(new String[]{languageCode, key});
		}
	}

	private Component createGoToWeblateButton(final String url) {
		final JButton button = new JButton(WEBLATE_ICON);
		button.setToolTipText(url);
		button.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent event) {
				try {
					Controller.getCurrentController().getViewController().openDocument(new URL(url));
				} catch (Exception e) {
					LogUtils.severe(e);
				}
			}
		});
		return button;
	}

	private void setFocusWhenShowed(final JTextField focusOwner) {
		focusOwner.addHierarchyListener(new HierarchyListener() {
			@Override
			public void hierarchyChanged(HierarchyEvent e) {
				if (focusOwner.isShowing()) {
					final Window windowAncestor = SwingUtilities.getWindowAncestor(focusOwner);
					if (windowAncestor.isFocused()) {
					focusOwner.requestFocusInWindow();
					}
					else {
						windowAncestor.addWindowFocusListener(new WindowFocusListener() {
			                @Override
			                public void windowLostFocus(WindowEvent e) {
				                // intentionally left blank
			                }

			                @Override
			                public void windowGainedFocus(WindowEvent e) {
				                focusOwner.requestFocusInWindow();
				                windowAncestor.removeWindowFocusListener(this);
			                }
		                });
					}
					focusOwner.removeHierarchyListener(this);
				}
			}
		});
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
			newText = TextUtils.getRawText(translationKey, "");
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

	private ArrayList<JTextField> createTextEditors(JComponent component) {
		ArrayList<JTextField> textFields = new ArrayList<>(TranslatedElement.values().length);
		for (TranslatedElement element : TranslatedElement.values()) {
			final String translationKey = element.getKey(component);
			if (translationKey != null) {
				JTextField textField = createTextField(element, translationKey);
				textFields.add(textField);
			}
		}
		return textFields;
	}

	private static FocusAdapter textFieldTextSelector = new FocusAdapter() {
		
		@Override
		public void focusGained(FocusEvent e) {
			((JTextComponent) e.getComponent()).selectAll();
		}
		
	};

	private JTextField createTextField(TranslatedElement element, final String translationKey) {
		final JTextField textField = new JTextField(TextUtils.getRawText(translationKey, ""));
		textField.addFocusListener(textFieldTextSelector);
		String titleKey = element.getTitleKey();
		UITools.addTitledBorder(textField, TextUtils.getRawText(titleKey), Math.round(UITools.FONT_SCALE_FACTOR*10));
		textField.putClientProperty(TranslatedElement.class, element);
		textField.putClientProperty(TEXT_FIELD_TRANSLATION_KEY, translationKey);
		TranslatedElement.BORDER.setKey(textField, titleKey);
		return textField;
	}
}

