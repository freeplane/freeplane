package org.freeplane.plugin.ai.service;

import java.util.HashMap;
import java.util.Map;

public class UserPreferenceConfig {

    public static final String PREF_KEY_DEFAULT_SERVICE_TYPE = "ai_preference_default_service_type";
    public static final String PREF_KEY_DEFAULT_MODEL = "ai_preference_default_model";
    public static final String PREF_KEY_MODEL_PRIORITIES = "ai_preference_model_priorities";
    public static final String PREF_KEY_AUTO_MODE_ENABLED = "ai_preference_auto_mode";
    public static final String PREF_KEY_RESPONSE_TIME_WEIGHT = "ai_preference_response_time_weight";
    public static final String PREF_KEY_ACCURACY_WEIGHT = "ai_preference_accuracy_weight";
    public static final String PREF_KEY_COST_WEIGHT = "ai_preference_cost_weight";

    public static final String SERVICE_TYPE_CHAT = "chat";
    public static final String SERVICE_TYPE_AGENT = "agent";
    public static final String SERVICE_TYPE_AUTO = "auto";

    private final Map<String, String> preferences;
    private static UserPreferenceConfig instance;

    private UserPreferenceConfig() {
        this.preferences = new HashMap<>();
        setDefaults();
    }

    public static synchronized UserPreferenceConfig getInstance() {
        if (instance == null) {
            instance = new UserPreferenceConfig();
        }
        return instance;
    }

    private void setDefaults() {
        preferences.put(PREF_KEY_DEFAULT_SERVICE_TYPE, SERVICE_TYPE_AUTO);
        preferences.put(PREF_KEY_DEFAULT_MODEL, "");
        preferences.put(PREF_KEY_MODEL_PRIORITIES, "dashscope:qwen-max:100,openrouter:gpt-4:90,gemini:gemini-pro:85");
        preferences.put(PREF_KEY_AUTO_MODE_ENABLED, "true");
        preferences.put(PREF_KEY_RESPONSE_TIME_WEIGHT, "30");
        preferences.put(PREF_KEY_ACCURACY_WEIGHT, "50");
        preferences.put(PREF_KEY_COST_WEIGHT, "20");
    }

    public String getDefaultServiceType() {
        return preferences.getOrDefault(PREF_KEY_DEFAULT_SERVICE_TYPE, SERVICE_TYPE_AUTO);
    }

    public void setDefaultServiceType(String serviceType) {
        if (serviceType == null || serviceType.isEmpty()) {
            serviceType = SERVICE_TYPE_AUTO;
        }
        preferences.put(PREF_KEY_DEFAULT_SERVICE_TYPE, serviceType);
    }

    public String getDefaultModel() {
        return preferences.getOrDefault(PREF_KEY_DEFAULT_MODEL, "");
    }

    public void setDefaultModel(String model) {
        preferences.put(PREF_KEY_DEFAULT_MODEL, model != null ? model : "");
    }

    public Map<String, Integer> getModelPriorities() {
        Map<String, Integer> priorities = new HashMap<>();
        String priorityString = preferences.get(PREF_KEY_MODEL_PRIORITIES);
        if (priorityString == null || priorityString.isEmpty()) {
            return priorities;
        }

        String[] entries = priorityString.split(",");
        for (String entry : entries) {
            String[] parts = entry.trim().split(":");
            if (parts.length >= 3) {
                String modelKey = parts[0] + ":" + parts[1];
                try {
                    int priority = Integer.parseInt(parts[2]);
                    priorities.put(modelKey, priority);
                } catch (NumberFormatException e) {
                    // Ignore invalid priority
                }
            }
        }
        return priorities;
    }

    public void setModelPriority(String modelKey, int priority) {
        Map<String, Integer> priorities = getModelPriorities();
        priorities.put(modelKey, priority);
        saveModelPriorities(priorities);
    }

    private void saveModelPriorities(Map<String, Integer> priorities) {
        StringBuilder sb = new StringBuilder();
        priorities.forEach((key, value) -> {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(key).append(":").append(value);
        });
        preferences.put(PREF_KEY_MODEL_PRIORITIES, sb.toString());
    }

    public boolean isAutoModeEnabled() {
        return Boolean.parseBoolean(preferences.getOrDefault(PREF_KEY_AUTO_MODE_ENABLED, "true"));
    }

    public void setAutoModeEnabled(boolean enabled) {
        preferences.put(PREF_KEY_AUTO_MODE_ENABLED, String.valueOf(enabled));
    }

    public int getResponseTimeWeight() {
        return Integer.parseInt(preferences.getOrDefault(PREF_KEY_RESPONSE_TIME_WEIGHT, "30"));
    }

    public void setResponseTimeWeight(int weight) {
        preferences.put(PREF_KEY_RESPONSE_TIME_WEIGHT, String.valueOf(weight));
    }

    public int getAccuracyWeight() {
        return Integer.parseInt(preferences.getOrDefault(PREF_KEY_ACCURACY_WEIGHT, "50"));
    }

    public void setAccuracyWeight(int weight) {
        preferences.put(PREF_KEY_ACCURACY_WEIGHT, String.valueOf(weight));
    }

    public int getCostWeight() {
        return Integer.parseInt(preferences.getOrDefault(PREF_KEY_COST_WEIGHT, "20"));
    }

    public void setCostWeight(int weight) {
        preferences.put(PREF_KEY_COST_WEIGHT, String.valueOf(weight));
    }

    public Map<String, Object> getAllPreferences() {
        Map<String, Object> result = new HashMap<>();
        result.put("defaultServiceType", getDefaultServiceType());
        result.put("defaultModel", getDefaultModel());
        result.put("modelPriorities", getModelPriorities());
        result.put("autoModeEnabled", isAutoModeEnabled());
        result.put("responseTimeWeight", getResponseTimeWeight());
        result.put("accuracyWeight", getAccuracyWeight());
        result.put("costWeight", getCostWeight());
        return result;
    }

    public void updatePreferences(Map<String, Object> updates) {
        if (updates.containsKey("defaultServiceType")) {
            setDefaultServiceType((String) updates.get("defaultServiceType"));
        }
        if (updates.containsKey("defaultModel")) {
            setDefaultModel((String) updates.get("defaultModel"));
        }
        if (updates.containsKey("autoModeEnabled")) {
            setAutoModeEnabled((Boolean) updates.get("autoModeEnabled"));
        }
        if (updates.containsKey("responseTimeWeight")) {
            setResponseTimeWeight((Integer) updates.get("responseTimeWeight"));
        }
        if (updates.containsKey("accuracyWeight")) {
            setAccuracyWeight((Integer) updates.get("accuracyWeight"));
        }
        if (updates.containsKey("costWeight")) {
            setCostWeight((Integer) updates.get("costWeight"));
        }
    }

    public void resetToDefaults() {
        preferences.clear();
        setDefaults();
    }
}