package org.freeplane.plugin.ai.chat;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

class AIModelCatalog {
    private static final long OPENROUTER_REFRESH_INTERVAL_MILLISECONDS = 30L * 60L * 1000L;

    private static final Object openrouterLock = new Object();
    private static long lastOpenrouterRefreshTime;
    private static List<AIModelDescriptor> cachedOpenrouterModels = Collections.emptyList();

    private static final Object ollamaLock = new Object();
    private static long lastOllamaRefreshTime;
    private static List<AIModelDescriptor> cachedOllamaModels = Collections.emptyList();
    private static String cachedOllamaCacheKey = "";

    private final AIProviderConfiguration configuration;
    private final ObjectMapper objectMapper;

    AIModelCatalog(AIProviderConfiguration configuration) {
        this.configuration = configuration;
        this.objectMapper = new ObjectMapper();
    }

    List<AIModelDescriptor> getAvailableModels(boolean allowsRefresh) {
        List<AIModelDescriptor> modelDescriptors = new ArrayList<>();
        if (hasOpenrouterKey()) {
            List<AIModelDescriptor> openrouterModels = getOpenrouterModels(allowsRefresh);
            if (openrouterModels.isEmpty()) {
                openrouterModels = parseLiteralProviderModelList(
                    AIChatModelFactory.PROVIDER_NAME_OPENROUTER,
                    configuration.getOpenrouterModelAllowlistValue()
                );
            }
            modelDescriptors.addAll(filterModelDescriptors(openrouterModels,
                configuration.getOpenrouterModelAllowlistValue()));
        }
        if (hasGeminiKey()) {
            modelDescriptors.addAll(getGeminiModelsFromList());
        }
        if (configuration.hasOllamaServiceAddress()) {
            List<AIModelDescriptor> ollamaModels = getOllamaModels(allowsRefresh);
            if (ollamaModels.isEmpty()) {
                ollamaModels = parseLiteralProviderModelList(
                    AIChatModelFactory.PROVIDER_NAME_OLLAMA,
                    configuration.getOllamaModelAllowlistValue()
                );
            }
            modelDescriptors.addAll(filterModelDescriptors(ollamaModels,
                configuration.getOllamaModelAllowlistValue()));
        }
        if (configuration.hasDashScopeKey()) {
            modelDescriptors.addAll(getDashScopeModelsFromList());
        }
        if (configuration.hasErnieKey()) {
            modelDescriptors.addAll(getErnieModelsFromList());
        }
        return modelDescriptors;
    }

    private boolean hasOpenrouterKey() {
        String openrouterKey = configuration.getOpenRouterKey();
        return openrouterKey != null && !openrouterKey.isEmpty();
    }

    private boolean hasGeminiKey() {
        String geminiKey = configuration.getGeminiKey();
        return geminiKey != null && !geminiKey.isEmpty();
    }

    private List<AIModelDescriptor> getOpenrouterModels(boolean allowsRefresh) {
        if (!allowsRefresh) {
            return cachedOpenrouterModels;
        }
        synchronized (openrouterLock) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastOpenrouterRefreshTime < OPENROUTER_REFRESH_INTERVAL_MILLISECONDS) {
                return cachedOpenrouterModels;
            }
            List<AIModelDescriptor> refreshedModels = fetchOpenrouterModels();
            cachedOpenrouterModels = refreshedModels;
            lastOpenrouterRefreshTime = currentTime;
            return cachedOpenrouterModels;
        }
    }

    private List<AIModelDescriptor> getOllamaModels(boolean allowsRefresh) {
        String currentCacheKey = getOllamaCacheKey();
        invalidateOllamaCacheIfConfigurationChanged(currentCacheKey);
        if (!allowsRefresh) {
            return cachedOllamaModels;
        }
        synchronized (ollamaLock) {
            currentCacheKey = getOllamaCacheKey();
            invalidateOllamaCacheIfConfigurationChanged(currentCacheKey);
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastOllamaRefreshTime < OPENROUTER_REFRESH_INTERVAL_MILLISECONDS) {
                return cachedOllamaModels;
            }
            OllamaModelsFetchResult refreshedModels = fetchOllamaModels();
            if (refreshedModels.successful) {
                cachedOllamaModels = refreshedModels.models;
                lastOllamaRefreshTime = currentTime;
            }
            return cachedOllamaModels;
        }
    }

    private void invalidateOllamaCacheIfConfigurationChanged(String currentCacheKey) {
        if (!currentCacheKey.equals(cachedOllamaCacheKey)) {
            cachedOllamaModels = Collections.emptyList();
            lastOllamaRefreshTime = 0L;
            cachedOllamaCacheKey = currentCacheKey;
        }
    }

    private String getOllamaCacheKey() {
        String serviceAddress = configuration.getOllamaServiceAddress();
        Map<String, String> requestHeaders = configuration.getOllamaRequestHeaders();
        return normalize(serviceAddress) + "|" + new TreeMap<>(requestHeaders).toString();
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    List<AIModelDescriptor> fetchOpenrouterModels() {
        String serviceAddress = configuration.getOpenrouterServiceAddress();
        if (serviceAddress == null || serviceAddress.isEmpty()) {
            serviceAddress = AIChatModelFactory.DEFAULT_OPENROUTER_SERVICE_ADDRESS;
        }
        String modelsAddress = serviceAddress.endsWith("/") ? serviceAddress + "models" : serviceAddress + "/models";
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(modelsAddress).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(10000);
            connection.setRequestProperty("HTTP-Referer", "https://github.com/freeplane/freeplane");
            connection.setRequestProperty("X-Title", "Freeplane");
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                return Collections.emptyList();
            }
            try (InputStream inputStream = connection.getInputStream();
                 InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
                return parseOpenrouterModelsResponse(reader);
            }
        } catch (IOException exception) {
            return Collections.emptyList();
        }
    }

    OllamaModelsFetchResult fetchOllamaModels() {
        String serviceAddress = configuration.getOllamaServiceAddress();
        String modelsAddress = serviceAddress.endsWith("/") ? serviceAddress + "api/tags" : serviceAddress + "/api/tags";
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(modelsAddress).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(10000);
            applyRequestHeaders(connection, configuration.getOllamaRequestHeaders());
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                return OllamaModelsFetchResult.failed();
            }
            try (InputStream inputStream = connection.getInputStream();
                 InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
                return OllamaModelsFetchResult.success(parseOllamaModelsResponse(reader));
            }
        } catch (IOException exception) {
            return OllamaModelsFetchResult.failed();
        }
    }

    void applyRequestHeaders(HttpURLConnection connection, Map<String, String> requestHeaders) {
        for (Map.Entry<String, String> requestHeader : requestHeaders.entrySet()) {
            connection.setRequestProperty(requestHeader.getKey(), requestHeader.getValue());
        }
    }

    static void resetOllamaCacheForTests() {
        cachedOllamaModels = Collections.emptyList();
        lastOllamaRefreshTime = 0L;
        cachedOllamaCacheKey = "";
    }

    static void resetOpenrouterCacheForTests() {
        cachedOpenrouterModels = Collections.emptyList();
        lastOpenrouterRefreshTime = 0L;
    }

    List<AIModelDescriptor> parseOpenrouterModelsResponse(Reader reader) throws IOException {
        OpenrouterModelsResponse response = objectMapper.readValue(reader, OpenrouterModelsResponse.class);
        if (response == null || response.models == null) {
            return Collections.emptyList();
        }
        List<AIModelDescriptor> modelDescriptors = new ArrayList<>();
        for (OpenrouterModelItem modelItem : response.models) {
            if (modelItem == null || modelItem.modelIdentifier == null) {
                continue;
            }
            boolean isFreeModel = isFreePricing(modelItem.pricing);
            modelDescriptors.add(createModelDescriptor(
                AIChatModelFactory.PROVIDER_NAME_OPENROUTER,
                modelItem.modelIdentifier,
                isFreeModel
            ));
        }
        return modelDescriptors;
    }

    List<AIModelDescriptor> parseOllamaModelsResponse(Reader reader) throws IOException {
        OllamaModelsResponse response = objectMapper.readValue(reader, OllamaModelsResponse.class);
        if (response == null || response.models == null) {
            return Collections.emptyList();
        }
        List<AIModelDescriptor> modelDescriptors = new ArrayList<>();
        for (OllamaModelItem modelItem : response.models) {
            if (modelItem == null || modelItem.modelName == null || modelItem.modelName.isEmpty()) {
                continue;
            }
            modelDescriptors.add(createModelDescriptor(
                AIChatModelFactory.PROVIDER_NAME_OLLAMA,
                modelItem.modelName,
                false
            ));
        }
        return modelDescriptors;
    }

    List<AIModelDescriptor> filterModelDescriptors(List<AIModelDescriptor> modelDescriptors,
                                                   String allowlistValue) {
        if (modelDescriptors.isEmpty()) {
            return modelDescriptors;
        }
        List<Pattern> allowlistPatterns = parseModelAllowlistPatterns(allowlistValue);
        if (allowlistPatterns.isEmpty()) {
            return modelDescriptors;
        }
        List<AIModelDescriptor> filteredDescriptors = new ArrayList<>();
        for (AIModelDescriptor modelDescriptor : modelDescriptors) {
            if (modelDescriptor == null) {
                continue;
            }
            if (matchesAllowlist(modelDescriptor.getModelName(), allowlistPatterns)) {
                filteredDescriptors.add(modelDescriptor);
            }
        }
        return filteredDescriptors;
    }

    private List<Pattern> parseModelAllowlistPatterns(String allowlistValue) {
        if (allowlistValue == null || allowlistValue.trim().isEmpty()) {
            return Collections.emptyList();
        }
        List<Pattern> patterns = new ArrayList<>();
        String[] entries = allowlistValue.split("[,\\r\\n]+");
        for (String entry : entries) {
            String trimmedEntry = entry.trim();
            if (trimmedEntry.isEmpty()) {
                continue;
            }
            patterns.add(Pattern.compile(convertWildcardToRegex(trimmedEntry)));
        }
        return patterns;
    }

    List<AIModelDescriptor> parseLiteralProviderModelList(String providerName, String modelListValue) {
        if (modelListValue == null || modelListValue.trim().isEmpty()) {
            return Collections.emptyList();
        }
        List<AIModelDescriptor> modelDescriptors = new ArrayList<>();
        String[] entries = modelListValue.split("[,\\r\\n]+");
        for (String entry : entries) {
            String trimmedEntry = entry.trim();
            if (trimmedEntry.isEmpty() || containsWildcard(trimmedEntry)) {
                continue;
            }
            modelDescriptors.add(createModelDescriptor(providerName, trimmedEntry, false));
        }
        return modelDescriptors;
    }

    private boolean matchesAllowlist(String modelName, List<Pattern> allowlistPatterns) {
        if (modelName == null || modelName.isEmpty()) {
            return false;
        }
        for (Pattern pattern : allowlistPatterns) {
            if (pattern.matcher(modelName).matches()) {
                return true;
            }
        }
        return false;
    }

    private String convertWildcardToRegex(String wildcardPattern) {
        StringBuilder builder = new StringBuilder();
        for (int index = 0; index < wildcardPattern.length(); index++) {
            char character = wildcardPattern.charAt(index);
            if (character == '*') {
                builder.append(".*");
            } else if (character == '?') {
                builder.append('.');
            } else {
                if ("\\.^$|()[]{}+".indexOf(character) >= 0) {
                    builder.append('\\');
                }
                builder.append(character);
            }
        }
        return builder.toString();
    }

    private AIModelDescriptor createModelDescriptor(String providerName, String modelName, boolean isFreeModel) {
        return new AIModelDescriptor(
            providerName,
            modelName,
            buildDisplayName(providerName, modelName, isFreeModel),
            isFreeModel
        );
    }

    private String buildDisplayName(String providerName, String modelName, boolean isFreeModel) {
        String providerDisplayName;
        if (AIChatModelFactory.PROVIDER_NAME_OPENROUTER.equals(providerName)) {
            providerDisplayName = "OpenRouter";
        } else if (AIChatModelFactory.PROVIDER_NAME_GEMINI.equals(providerName)) {
            providerDisplayName = "Gemini";
        } else if (AIChatModelFactory.PROVIDER_NAME_OLLAMA.equals(providerName)) {
            providerDisplayName = "Ollama";
        } else if (AIChatModelFactory.PROVIDER_NAME_DASHSCOPE.equals(providerName)) {
            providerDisplayName = "DashScope (Qwen)";
        } else if (AIChatModelFactory.PROVIDER_NAME_ERNIE.equals(providerName)) {
            providerDisplayName = "ERNIE (Baidu)";
        } else {
            providerDisplayName = providerName;
        }
        String displayName = providerDisplayName + ": " + modelName;
        if (isFreeModel) {
            displayName = displayName + " (free)";
        }
        return displayName;
    }

    private boolean isFreePricing(OpenrouterModelPricing pricing) {
        if (pricing == null) {
            return false;
        }
        return isZeroCost(pricing.promptPrice) && isZeroCost(pricing.completionPrice);
    }

    private boolean isZeroCost(String price) {
        if (price == null || price.isEmpty()) {
            return false;
        }
        try {
            return Double.parseDouble(price) == 0.0;
        } catch (NumberFormatException exception) {
            return false;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class OpenrouterModelsResponse {
        @JsonProperty("data")
        private List<OpenrouterModelItem> models;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class OpenrouterModelItem {
        @JsonProperty("id")
        private String modelIdentifier;
        @JsonProperty("pricing")
        private OpenrouterModelPricing pricing;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class OpenrouterModelPricing {
        @JsonProperty("prompt")
        private String promptPrice;
        @JsonProperty("completion")
        private String completionPrice;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class OllamaModelsResponse {
        @JsonProperty("models")
        private List<OllamaModelItem> models;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class OllamaModelItem {
        @JsonProperty("name")
        private String modelName;
    }

    static class OllamaModelsFetchResult {
        private final boolean successful;
        private final List<AIModelDescriptor> models;

        private OllamaModelsFetchResult(boolean successful, List<AIModelDescriptor> models) {
            this.successful = successful;
            this.models = models;
        }

        static OllamaModelsFetchResult success(List<AIModelDescriptor> models) {
            return new OllamaModelsFetchResult(true, models);
        }

        static OllamaModelsFetchResult failed() {
            return new OllamaModelsFetchResult(false, Collections.<AIModelDescriptor>emptyList());
        }
    }

    List<AIModelDescriptor> parseGeminiModelList(String modelListValue) {
        if (modelListValue == null || modelListValue.trim().isEmpty()) {
            return Collections.emptyList();
        }
        List<AIModelDescriptor> modelDescriptors = new ArrayList<>();
        String[] entries = modelListValue.split("[,\\r\\n]+");
        for (String entry : entries) {
            String trimmedEntry = entry.trim();
            if (trimmedEntry.isEmpty()) {
                continue;
            }
            modelDescriptors.add(new AIModelDescriptor(
                AIChatModelFactory.PROVIDER_NAME_GEMINI,
                trimmedEntry,
                buildDisplayName(AIChatModelFactory.PROVIDER_NAME_GEMINI, trimmedEntry, false),
                false
            ));
        }
        return modelDescriptors;
    }

    private boolean containsWildcard(String value) {
        return value.indexOf('*') >= 0 || value.indexOf('?') >= 0;
    }

    private List<AIModelDescriptor> getGeminiModelsFromList() {
        return parseGeminiModelList(configuration.getGeminiModelListValue());
    }

    private List<AIModelDescriptor> getDashScopeModelsFromList() {
        return parseDashScopeModelList(configuration.getDashScopeModelListValue());
    }

    private List<AIModelDescriptor> getErnieModelsFromList() {
        return parseErnieModelList(configuration.getErnieModelListValue());
    }

    List<AIModelDescriptor> parseDashScopeModelList(String modelListValue) {
        if (modelListValue == null || modelListValue.trim().isEmpty()) {
            return Collections.emptyList();
        }
        List<AIModelDescriptor> modelDescriptors = new ArrayList<>();
        String[] entries = modelListValue.split("[,\\r\\n]+");
        for (String entry : entries) {
            String trimmedEntry = entry.trim();
            if (trimmedEntry.isEmpty()) {
                continue;
            }
            modelDescriptors.add(new AIModelDescriptor(
                AIChatModelFactory.PROVIDER_NAME_DASHSCOPE,
                trimmedEntry,
                buildDisplayName(AIChatModelFactory.PROVIDER_NAME_DASHSCOPE, trimmedEntry, false),
                false
            ));
        }
        return modelDescriptors;
    }

    List<AIModelDescriptor> parseErnieModelList(String modelListValue) {
        if (modelListValue == null || modelListValue.trim().isEmpty()) {
            return Collections.emptyList();
        }
        List<AIModelDescriptor> modelDescriptors = new ArrayList<>();
        String[] entries = modelListValue.split("[,\\r\\n]+");
        for (String entry : entries) {
            String trimmedEntry = entry.trim();
            if (trimmedEntry.isEmpty()) {
                continue;
            }
            modelDescriptors.add(new AIModelDescriptor(
                AIChatModelFactory.PROVIDER_NAME_ERNIE,
                trimmedEntry,
                buildDisplayName(AIChatModelFactory.PROVIDER_NAME_ERNIE, trimmedEntry, false),
                false
            ));
        }
        return modelDescriptors;
    }
}
