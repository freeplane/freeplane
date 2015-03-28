package org.freeplane.plugin.script;

import java.util.Properties;

import org.freeplane.core.ui.ribbon.ARibbonContributor;
import org.freeplane.core.ui.ribbon.IRibbonContributorFactory;
import org.freeplane.features.mode.ModeController;

class ScriptingRibbonsContributorFactory implements IRibbonContributorFactory {
    private final ScriptingConfiguration configuration;

    ScriptingRibbonsContributorFactory(ModeController modeController, ScriptingConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public ARibbonContributor getContributor(final Properties attributes) {
        return new ScriptingRibbonsContributor(attributes, configuration);
    }
}