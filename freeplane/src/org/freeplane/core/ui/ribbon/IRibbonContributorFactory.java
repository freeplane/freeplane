package org.freeplane.core.ui.ribbon;

import java.util.Properties;


public interface IRibbonContributorFactory {
	public ARibbonContributor getContributor(final Properties attributes);
}