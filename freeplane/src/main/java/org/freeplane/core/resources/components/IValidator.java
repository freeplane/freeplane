package org.freeplane.core.resources.components;

import java.util.ArrayList;
import java.util.Properties;

public interface IValidator {
	public static final class ValidationResult {
		private ArrayList<String> warnings = new ArrayList<String>(0);
		private ArrayList<String> errors = new ArrayList<String>(0);

		public boolean isValid() {
			return errors.isEmpty();
		}

		public boolean hasWarnings() {
			return !warnings.isEmpty();
		}

		public void addError(String error) {
			errors.add(error);
		}

		public void addWarning(String warning) {
			warnings.add(warning);
		}

		public ArrayList<String> getWarnings() {
			return warnings;
		}

		public ArrayList<String> getErrors() {
			return errors;
		}

		@Override
		public String toString() {
			return "Validation errors: " + errors + ", warnings: " + warnings;
		}

		public void add(ValidationResult result) {
			warnings.addAll(result.warnings);
			errors.addAll(result.errors);
		}
	}

	/** validates properties. Note that these may differ from the state that the ResourceController has. */
	ValidationResult validate(Properties properties);
}
