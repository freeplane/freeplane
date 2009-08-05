/**
 * 
 */
package org.freeplane.core.util;

import java.io.IOException;
import java.util.Map;

class PoorMansTemplate {
	private String template;

	public PoorMansTemplate(String classpathTemplate) {
		try {
			template = I18nReporter.loadTemplateFromClasspath(classpathTemplate);
		}
		catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	String eval(Map<String, String> context) {
		String template = this.template;
		for (String k : context.keySet()) {
			template = template.replace(k, context.get(k));
		}
		return template;
	}
}