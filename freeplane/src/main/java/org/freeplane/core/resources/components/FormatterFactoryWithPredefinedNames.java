package org.freeplane.core.resources.components;

import java.text.ParseException;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.JFormattedTextField.AbstractFormatterFactory;
import javax.swing.JSpinner;
import javax.swing.JSpinner.NumberEditor;
import javax.swing.text.NumberFormatter;

class FormatterFactoryWithPredefinedNames extends AbstractFormatterFactory {
	static class FormatterWithPredefinedNames extends NumberFormatter{
		
		private final NumberFormatter fallback;
		private final Map<String, Comparable> values;
		private final Map<Comparable, String> strings;
		

		FormatterWithPredefinedNames(NumberFormatter fallback, Map<String, Comparable> values,
				Map<Comparable, String> strings) {
			super();
			this.fallback = fallback;
			this.values = values;
			this.strings = strings;
		}

		public void setMinimum(Comparable minimum) {
			fallback.setMinimum(minimum);
		}

		public Comparable getMinimum() {
			return fallback.getMinimum();
		}

		public void setMaximum(Comparable max) {
			fallback.setMaximum(max);
		}

		public Comparable getMaximum() {
			return fallback.getMaximum();
		}

		public String valueToString(Object value) throws ParseException {
			String predefinedString = strings.get(value);
			return predefinedString != null ? predefinedString : fallback.valueToString(value);
		}

		public Object stringToValue(String text) throws ParseException {
			Object predefinedValue = values.get(text);
			return predefinedValue != null ? predefinedValue : fallback.stringToValue(text);
		}
		
	}
	public static void installFactory(JSpinner spinner, Map<String, Comparable> values) {
		JComponent editor = spinner.getEditor();
		if(! (editor instanceof NumberEditor))
			return;
		NumberEditor numberEditor = (NumberEditor) editor;
		JFormattedTextField textField = numberEditor.getTextField();
		AbstractFormatterFactory formatterFactory = textField.getFormatterFactory();
		FormatterFactoryWithPredefinedNames factoryWithNames = new FormatterFactoryWithPredefinedNames(formatterFactory, values);
		textField.setFormatterFactory(factoryWithNames);
		
	}
	private final AbstractFormatterFactory fallback;
	private final Map<String, Comparable> values;
	private final Map<Comparable, String> strings;
	public FormatterFactoryWithPredefinedNames(AbstractFormatterFactory fallback,  Map<String, Comparable> values) {
		this.fallback = fallback;
		this.values = values;
		this.strings = values.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
	}

	@Override
	public AbstractFormatter getFormatter(JFormattedTextField tf) {
		AbstractFormatter formatter = fallback.getFormatter(tf);
		return formatter instanceof NumberFormatter ? new FormatterWithPredefinedNames((NumberFormatter) formatter, values, strings) : formatter;
	}
	
}