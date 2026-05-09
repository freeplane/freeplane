package org.freeplane.plugin.ai.validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 思维导图验证结果
 */
public class MindMapValidationResult {

    public enum ValidationStatus {
        /** 验证通过 */
        VALID,
        /** 验证失败 - 有错误 */
        INVALID,
        /** 验证警告 - 有问题但不影响使用 */
        WARNING
    }

    private ValidationStatus status;
    private final List<ValidationError> errors;
    private final List<ValidationWarning> warnings;
    private MindMapStatistics statistics;

    public MindMapValidationResult() {
        this.errors = new ArrayList<>();
        this.warnings = new ArrayList<>();
        this.status = ValidationStatus.VALID;
    }

    public static MindMapValidationResult success() {
        return new MindMapValidationResult();
    }

    public static MindMapValidationResult error(String code, String message) {
        MindMapValidationResult result = new MindMapValidationResult();
        result.addError(code, message);
        return result;
    }

    public static MindMapValidationResult error(String code, String message, String nodeId) {
        MindMapValidationResult result = new MindMapValidationResult();
        result.addError(code, message, nodeId);
        return result;
    }

    public void addError(String code, String message) {
        this.errors.add(new ValidationError(code, message));
        this.status = ValidationStatus.INVALID;
    }

    public void addError(String code, String message, String nodeId) {
        this.errors.add(new ValidationError(code, message, nodeId));
        this.status = ValidationStatus.INVALID;
    }

    public void addWarning(String code, String message) {
        this.warnings.add(new ValidationWarning(code, message));
        if (this.status == ValidationStatus.VALID) {
            this.status = ValidationStatus.WARNING;
        }
    }

    public void addWarning(String code, String message, String nodeId) {
        this.warnings.add(new ValidationWarning(code, message, nodeId));
        if (this.status == ValidationStatus.VALID) {
            this.status = ValidationStatus.WARNING;
        }
    }

    public boolean isValid() {
        return status == ValidationStatus.VALID || status == ValidationStatus.WARNING;
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }

    public ValidationStatus getStatus() {
        return status;
    }

    public List<ValidationError> getErrors() {
        return Collections.unmodifiableList(errors);
    }

    public List<ValidationWarning> getWarnings() {
        return Collections.unmodifiableList(warnings);
    }

    public MindMapStatistics getStatistics() {
        return statistics;
    }

    public void setStatistics(MindMapStatistics statistics) {
        this.statistics = statistics;
    }

    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("验证结果: ").append(status).append("\n");

        if (!errors.isEmpty()) {
            sb.append("错误 (").append(errors.size()).append("):\n");
            for (ValidationError error : errors) {
                sb.append("  [").append(error.getCode()).append("] ");
                if (error.getNodeId() != null) {
                    sb.append("节点(").append(error.getNodeId()).append("): ");
                }
                sb.append(error.getMessage()).append("\n");
            }
        }

        if (!warnings.isEmpty()) {
            sb.append("警告 (").append(warnings.size()).append("):\n");
            for (ValidationWarning warning : warnings) {
                sb.append("  [").append(warning.getCode()).append("] ");
                if (warning.getNodeId() != null) {
                    sb.append("节点(").append(warning.getNodeId()).append("): ");
                }
                sb.append(warning.getMessage()).append("\n");
            }
        }

        if (statistics != null) {
            sb.append("统计:\n");
            sb.append("  总节点数: ").append(statistics.getTotalNodes()).append("\n");
            sb.append("  最大深度: ").append(statistics.getMaxDepth()).append("\n");
            sb.append("  平均子节点数: ").append(String.format("%.2f", statistics.getAverageChildrenPerNode())).append("\n");
        }

        return sb.toString();
    }

    // -------------------------------------------------------------------------
    // Inner types
    // -------------------------------------------------------------------------

    /** 验证错误 */
    public static class ValidationError {
        private final String code;
        private final String message;
        private final String nodeId;

        public ValidationError(String code, String message) {
            this(code, message, null);
        }

        public ValidationError(String code, String message, String nodeId) {
            this.code = code;
            this.message = message;
            this.nodeId = nodeId;
        }

        public String getCode() { return code; }
        public String getMessage() { return message; }
        public String getNodeId() { return nodeId; }
    }

    /** 验证警告 */
    public static class ValidationWarning {
        private final String code;
        private final String message;
        private final String nodeId;

        public ValidationWarning(String code, String message) {
            this(code, message, null);
        }

        public ValidationWarning(String code, String message, String nodeId) {
            this.code = code;
            this.message = message;
            this.nodeId = nodeId;
        }

        public String getCode() { return code; }
        public String getMessage() { return message; }
        public String getNodeId() { return nodeId; }
    }

    /** 思维导图统计信息 */
    public static class MindMapStatistics {
        private int totalNodes;
        private int maxDepth;
        private int maxChildrenPerNode;
        private double averageChildrenPerNode;
        private int leafNodes;
        private int internalNodes;

        public int getTotalNodes() { return totalNodes; }
        public void setTotalNodes(int totalNodes) { this.totalNodes = totalNodes; }

        public int getMaxDepth() { return maxDepth; }
        public void setMaxDepth(int maxDepth) { this.maxDepth = maxDepth; }

        public int getMaxChildrenPerNode() { return maxChildrenPerNode; }
        public void setMaxChildrenPerNode(int maxChildrenPerNode) { this.maxChildrenPerNode = maxChildrenPerNode; }

        public double getAverageChildrenPerNode() { return averageChildrenPerNode; }
        public void setAverageChildrenPerNode(double averageChildrenPerNode) { this.averageChildrenPerNode = averageChildrenPerNode; }

        public int getLeafNodes() { return leafNodes; }
        public void setLeafNodes(int leafNodes) { this.leafNodes = leafNodes; }

        public int getInternalNodes() { return internalNodes; }
        public void setInternalNodes(int internalNodes) { this.internalNodes = internalNodes; }
    }
}
