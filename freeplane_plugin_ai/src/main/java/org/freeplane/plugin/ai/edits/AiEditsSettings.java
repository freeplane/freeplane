package org.freeplane.plugin.ai.edits;

import java.util.Set;

import org.freeplane.core.resources.ResourceController;

public class AiEditsSettings {
    public static final String AI_EDITS_STATE_ICON_VISIBLE_PROPERTY = "ai_edits_state_icon_visible";
    public static final String AI_EDITS_PERSISTENCE_ENABLED_PROPERTY = "ai_edits_persistence_enabled";
    private static final Set<String> AI_EDITS_PROPERTIES = Set.of(AI_EDITS_STATE_ICON_VISIBLE_PROPERTY, AI_EDITS_PERSISTENCE_ENABLED_PROPERTY);

    public static boolean containsProperty(String property) {
        return AI_EDITS_PROPERTIES.contains(property);
    }

    private final ResourceController resourceController;

    public AiEditsSettings() {
        this(ResourceController.getResourceController());
    }

    AiEditsSettings(ResourceController resourceController) {
        this.resourceController = resourceController;
    }

    public boolean isStateIconVisible() {
        return resourceController.getBooleanProperty(AI_EDITS_STATE_ICON_VISIBLE_PROPERTY);
    }

    public boolean isPersistenceEnabled() {
        return resourceController.getBooleanProperty(AI_EDITS_PERSISTENCE_ENABLED_PROPERTY);
    }
}
