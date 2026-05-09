package org.freeplane.plugin.ai.service;

import java.util.Map;

/**
 * AI服务响应
 */
public class AIServiceResponse {
    private boolean success;
    private String message;
    private Map<String, Object> data;
    private String errorMessage;
    private String serviceName;
    private long processingTime;
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public Map<String, Object> getData() {
        return data;
    }
    
    public void setData(Map<String, Object> data) {
        this.data = data;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public String getServiceName() {
        return serviceName;
    }
    
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
    
    public long getProcessingTime() {
        return processingTime;
    }
    
    public void setProcessingTime(long processingTime) {
        this.processingTime = processingTime;
    }
    
    public static AIServiceResponse success(String message, Map<String, Object> data) {
        AIServiceResponse response = new AIServiceResponse();
        response.setSuccess(true);
        response.setMessage(message);
        response.setData(data);
        return response;
    }
    
    public static AIServiceResponse error(String errorMessage) {
        AIServiceResponse response = new AIServiceResponse();
        response.setSuccess(false);
        response.setErrorMessage(errorMessage);
        return response;
    }
}