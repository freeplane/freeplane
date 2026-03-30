package org.freeplane.plugin.ai.chat;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.SwingWorker;
import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

class AIModelSelectionController {
    private final AIProviderConfiguration configuration;
    private final AIModelCatalog modelCatalog;
    private final JComboBox<AIModelDescriptor> modelSelectionComboBox;
    private boolean isModelSelectionUpdateInProgress;
    private boolean isModelListLoadInProgress;
    private Consumer<AIModelDescriptor> modelSelectionChangeListener;

    AIModelSelectionController(AIProviderConfiguration configuration, AIModelCatalog modelCatalog) {
        this.configuration = configuration;
        this.modelCatalog = modelCatalog;
        this.modelSelectionComboBox = new JComboBox<>();
        this.modelSelectionComboBox.setRenderer(new ModelSelectionRenderer());
        this.modelSelectionComboBox.addActionListener(event -> onModelSelectionChanged());
    }

    JComboBox<AIModelDescriptor> getModelSelectionComboBox() {
        return modelSelectionComboBox;
    }

    void setModelSelectionChangeListener(Consumer<AIModelDescriptor> modelSelectionChangeListener) {
        this.modelSelectionChangeListener = modelSelectionChangeListener;
    }

    void loadInitialModelSelectionList() {
        updateModelSelectionList(true);
    }

    private void onModelSelectionChanged() {
        if (isModelSelectionUpdateInProgress) {
            return;
        }
        Object selectedValue = modelSelectionComboBox.getSelectedItem();
        if (!(selectedValue instanceof AIModelDescriptor)) {
            configuration.setSelectedModelValue("");
            notifyModelSelectionChange(null);
            return;
        }
        AIModelDescriptor selectedModel = (AIModelDescriptor) selectedValue;
        configuration.setSelectedModelValue(selectedModel.getSelectionValue());
        notifyModelSelectionChange(selectedModel);
    }

    private void updateModelSelectionList(boolean allowsRefresh) {
        if (isModelListLoadInProgress) {
            return;
        }
        isModelListLoadInProgress = true;
        modelSelectionComboBox.setEnabled(false);
        new SwingWorker<List<AIModelDescriptor>, Void>() {
            @Override
            protected List<AIModelDescriptor> doInBackground() {
                return modelCatalog.getAvailableModels(allowsRefresh);
            }

            @Override
            protected void done() {
                List<AIModelDescriptor> modelDescriptors;
                try {
                    modelDescriptors = get();
                } catch (Exception exception) {
                    modelDescriptors = Collections.emptyList();
                }
                applyModelSelectionList(modelDescriptors);
                isModelListLoadInProgress = false;
            }
        }.execute();
    }

    private void applyModelSelectionList(List<AIModelDescriptor> modelDescriptors) {
        isModelSelectionUpdateInProgress = true;
        try {
            List<AIModelDescriptor> sortedModelDescriptors = new ArrayList<>(modelDescriptors);
            sortedModelDescriptors.sort(Comparator.comparing(AIModelDescriptor::getDisplayName, String.CASE_INSENSITIVE_ORDER));
            DefaultComboBoxModel<AIModelDescriptor> comboBoxModel = new DefaultComboBoxModel<>(
                sortedModelDescriptors.toArray(new AIModelDescriptor[0])
            );
            modelSelectionComboBox.setModel(comboBoxModel);
            modelSelectionComboBox.setSelectedIndex(-1);
            applySelectionFromConfiguration(sortedModelDescriptors);
            modelSelectionComboBox.setEnabled(hasAnyProviderEnabled());
        } finally {
            isModelSelectionUpdateInProgress = false;
        }
    }

    private void applySelectionFromConfiguration(List<AIModelDescriptor> modelDescriptors) {
        String storedSelectionValue = configuration.getStoredSelectedModelValue();
        String selectionValue = configuration.getSelectedModelValue();
        AIModelSelection selection = AIModelSelection.fromSelectionValue(selectionValue);
        if (selection == null) {
            return;
        }
        for (AIModelDescriptor modelDescriptor : modelDescriptors) {
            if (selection.getProviderName().equalsIgnoreCase(modelDescriptor.getProviderName())
                && selection.getModelName().equals(modelDescriptor.getModelName())) {
                modelSelectionComboBox.setSelectedItem(modelDescriptor);
                if (storedSelectionValue == null || storedSelectionValue.isEmpty()) {
                    configuration.setSelectedModelValue(modelDescriptor.getSelectionValue());
                }
                notifyModelSelectionChange(modelDescriptor);
                return;
            }
        }
        configuration.setSelectedModelValue("");
        modelSelectionComboBox.setSelectedIndex(-1);
        notifyModelSelectionChange(null);
    }

    private boolean hasAnyProviderEnabled() {
        boolean hasOpenrouterKey = configuration.getOpenRouterKey() != null && !configuration.getOpenRouterKey().isEmpty();
        boolean hasGeminiKey = configuration.getGeminiKey() != null && !configuration.getGeminiKey().isEmpty();
        boolean hasVendorKey = configuration.hasVendorKey();
        return hasOpenrouterKey || hasGeminiKey || hasVendorKey || configuration.hasOllamaServiceAddress();
    }

    private void notifyModelSelectionChange(AIModelDescriptor modelDescriptor) {
        if (modelSelectionChangeListener != null) {
            modelSelectionChangeListener.accept(modelDescriptor);
        }
    }

    private static class ModelSelectionRenderer extends DefaultListCellRenderer {
        private String preferredSizeText;
        private boolean measuringPreferredSize;

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            preferredSizeText = null;
            if (value instanceof AIModelDescriptor) {
                AIModelDescriptor modelDescriptor = (AIModelDescriptor) value;
                preferredSizeText = index < 0 ? modelDescriptor.getDisplayName() : null;
                String renderedText = index < 0
                    ? renderSelectedModelName(modelDescriptor.getModelName())
                    : modelDescriptor.getDisplayName();
                setText(renderedText);
            }
            return component;
        }

        @Override
        public Dimension getPreferredSize() {
            boolean previousMeasuringState = measuringPreferredSize;
            measuringPreferredSize = true;
            try {
                return super.getPreferredSize();
            } finally {
                measuringPreferredSize = previousMeasuringState;
            }
        }

        @Override
        public String getText() {
            if (measuringPreferredSize && preferredSizeText != null) {
                return preferredSizeText;
            }
            return super.getText();
        }

        private String renderSelectedModelName(String modelName) {
            int separatorIndex = modelName.indexOf('/');
            if (separatorIndex >= 0 && separatorIndex < modelName.length() - 1) {
                return modelName.substring(separatorIndex + 1);
            }
            return modelName;
        }
    }
}
