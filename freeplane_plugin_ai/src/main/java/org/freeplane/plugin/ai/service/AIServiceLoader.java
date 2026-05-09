package org.freeplane.plugin.ai.service;

import org.freeplane.core.util.LogUtils;

import java.util.*;

/**
 * AI服务加载器
 * 负责发现和管理AI服务提供者
 * 支持智能路由和Auto模式
 */
public class AIServiceLoader {
    private static final Map<AIServiceType, List<AIService>> servicesByType = new HashMap<>();
    private static final Map<String, AIService> servicesByName = new HashMap<>();
    private static boolean initialized = false;
    private static final UserPreferenceConfig userPrefs = UserPreferenceConfig.getInstance();
    private static final PerformanceMonitor performanceMonitor = PerformanceMonitor.getInstance();

    /**
     * 初始化服务加载器
     */
    public static synchronized void initialize() {
        if (initialized) {
            return;
        }

        // 使用ServiceLoader加载所有AIService实现
        ServiceLoader<AIService> loader = ServiceLoader.load(AIService.class);
        for (AIService service : loader) {
            registerService(service);
        }

        // 如果没有通过ServiceLoader找到，注册默认服务
        if (servicesByType.isEmpty()) {
            registerDefaultServices();
        }

        initialized = true;
        LogUtils.info("AIServiceLoader: initialized with " + servicesByName.size() + " services");
    }

    /**
     * 注册默认服务
     */
    private static void registerDefaultServices() {
        try {
            // 注册默认的聊天服务
            Class<?> chatServiceClass = Class.forName("org.freeplane.plugin.ai.service.impl.DefaultChatService");
            AIService chatService = (AIService) chatServiceClass.getDeclaredConstructor().newInstance();
            registerService(chatService);

            // 注册默认的智能体服务
            Class<?> agentServiceClass = Class.forName("org.freeplane.plugin.ai.service.impl.DefaultAgentService");
            AIService agentService = (AIService) agentServiceClass.getDeclaredConstructor().newInstance();
            registerService(agentService);

        } catch (Exception e) {
            LogUtils.warn("AIServiceLoader: failed to register default services", e);
        }
    }

    /**
     * 注册服务
     * @param service 服务实例
     */
    public static void registerService(AIService service) {
        AIServiceType type = service.getServiceType();
        String name = service.getServiceName();

        // 按类型分组
        servicesByType.computeIfAbsent(type, k -> new ArrayList<>()).add(service);
        // 按名称索引
        servicesByName.put(name, service);

        LogUtils.info("AIServiceLoader: registered service - " + name + " (" + type.getCode() + ")");
    }

    /**
     * 获取指定类型的所有服务
     * @param type 服务类型
     * @return 服务列表
     */
    public static List<AIService> getServicesByType(AIServiceType type) {
        initialize();
        List<AIService> services = servicesByType.get(type);
        return services != null ? services : Collections.emptyList();
    }

    /**
     * 获取指定名称的服务
     * @param name 服务名称
     * @return 服务实例
     */
    public static AIService getServiceByName(String name) {
        initialize();
        return servicesByName.get(name);
    }

    /**
     * 根据请求选择合适的服务
     * 支持三种模式：
     * 1. auto模式 - 根据任务类型自动选择最佳服务
     * 2. 指定serviceType - 根据指定的类型选择服务
     * 3. 默认偏好 - 根据用户设置的默认偏好选择
     *
     * @param request 请求参数
     * @return 服务实例
     */
    public static AIService selectService(Map<String, Object> request) {
        initialize();

        String serviceType = (String) request.get("serviceType");
        String model = (String) request.get("model");
        String taskType = (String) request.get("action");

        // Auto模式：根据任务类型和性能选择最佳服务
        if (isAutoMode(serviceType)) {
            return selectServiceAuto(request, taskType);
        }

        // 指定了具体模型
        if (model != null && !model.isEmpty()) {
            AIService service = getServiceByModel(model);
            if (service != null && service.canHandle(request)) {
                return service;
            }
        }

        // 根据serviceType选择
        if (serviceType != null && !serviceType.isEmpty()) {
            AIServiceType type = AIServiceType.fromCode(serviceType);
            if (type != null) {
                List<AIService> services = getServicesByType(type);
                AIService selectedService = selectBestServiceByPriority(services, request);
                if (selectedService != null) {
                    return selectedService;
                }
            }
        }

        // 根据用户默认偏好选择
        String defaultServiceType = userPrefs.getDefaultServiceType();
        if (defaultServiceType != null && !defaultServiceType.isEmpty()) {
            if (isAutoMode(defaultServiceType)) {
                return selectServiceAuto(request, taskType);
            }

            AIServiceType type = AIServiceType.fromCode(defaultServiceType);
            if (type != null) {
                List<AIService> services = getServicesByType(type);
                AIService selectedService = selectBestServiceByPriority(services, request);
                if (selectedService != null) {
                    return selectedService;
                }
            }
        }

        // 遍历所有服务寻找合适的
        for (List<AIService> services : servicesByType.values()) {
            for (AIService service : services) {
                if (service.canHandle(request)) {
                    return service;
                }
            }
        }

        return null;
    }

    /**
     * 判断是否为Auto模式
     */
    private static boolean isAutoMode(String serviceType) {
        return serviceType == null || serviceType.isEmpty() ||
               UserPreferenceConfig.SERVICE_TYPE_AUTO.equalsIgnoreCase(serviceType) ||
               "auto".equalsIgnoreCase(serviceType);
    }

    /**
     * Auto模式：智能选择最佳服务
     */
    private static AIService selectServiceAuto(Map<String, Object> request, String taskType) {
        LogUtils.info("AIServiceLoader: Auto mode selecting service for task: " + taskType);

        // 根据任务类型推断服务类型
        AIServiceType inferredType = inferServiceType(taskType);
        List<AIService> candidateServices = getServicesByType(inferredType);

        if (candidateServices.isEmpty()) {
            // 如果推断类型没有服务，尝试所有类型
            candidateServices = new ArrayList<>(servicesByName.values());
        }

        // 按优先级排序并选择
        AIService selectedService = selectBestServiceByPriority(candidateServices, request);

        if (selectedService != null) {
            LogUtils.info("AIServiceLoader: Auto selected service: " + selectedService.getServiceName());
        }

        return selectedService;
    }

    /**
     * 根据任务类型推断服务类型
     */
    private static AIServiceType inferServiceType(String taskType) {
        if (taskType == null || taskType.isEmpty()) {
            return AIServiceType.CHAT;
        }

        // 智能体任务
        if (isAgentTask(taskType)) {
            return AIServiceType.AGENT;
        }

        // 聊天任务
        if (isChatTask(taskType)) {
            return AIServiceType.CHAT;
        }

        // 默认返回CHAT
        return AIServiceType.CHAT;
    }

    /**
     * 判断是否为智能体任务
     */
    private static boolean isAgentTask(String taskType) {
        if (taskType == null) return false;

        String[] agentTasks = {
            "generate-mindmap", "expand-node", "summarize", "tag",
            "generate", "analyze", "plan", "execute"
        };

        for (String agentTask : agentTasks) {
            if (taskType.toLowerCase().contains(agentTask.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断是否为聊天任务
     */
    private static boolean isChatTask(String taskType) {
        if (taskType == null) return false;

        String[] chatTasks = {
            "chat", "message", "question", "answer", "talk"
        };

        for (String chatTask : chatTasks) {
            if (taskType.toLowerCase().contains(chatTask.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 根据优先级和性能选择最佳服务
     */
    private static AIService selectBestServiceByPriority(List<AIService> services, Map<String, Object> request) {
        if (services == null || services.isEmpty()) {
            return null;
        }

        if (services.size() == 1) {
            return services.get(0);
        }

        AIService bestService = null;
        int highestPriority = -1;

        for (AIService service : services) {
            if (!service.canHandle(request)) {
                continue;
            }

            int priority = service.getPriority();

            // 如果用户设置了模型优先级，调整服务优先级
            String modelKey = getModelKeyFromService(service);
            if (modelKey != null) {
                Integer userPriority = userPrefs.getModelPriorities().get(modelKey);
                if (userPriority != null) {
                    priority = userPriority;
                }
            }

            if (priority > highestPriority) {
                highestPriority = priority;
                bestService = service;
            }
        }

        return bestService;
    }

    /**
     * 根据模型名称获取服务
     */
    private static AIService getServiceByModel(String model) {
        for (AIService service : servicesByName.values()) {
            if (service instanceof ModelAwareService) {
                ModelAwareService awareService = (ModelAwareService) service;
                if (awareService.supportsModel(model)) {
                    return service;
                }
            }
        }
        return null;
    }

    /**
     * 从服务中获取模型键
     */
    private static String getModelKeyFromService(AIService service) {
        if (service instanceof ModelAwareService) {
            return ((ModelAwareService) service).getDefaultModel();
        }
        return null;
    }

    /**
     * 获取用户偏好配置
     */
    public static UserPreferenceConfig getUserPreferences() {
        return userPrefs;
    }

    /**
     * 获取性能监控器
     */
    public static PerformanceMonitor getPerformanceMonitor() {
        return performanceMonitor;
    }

    /**
     * 获取智能路由报告
     */
    public static Map<String, Object> getRoutingReport() {
        Map<String, Object> report = new HashMap<>();
        report.put("userPreferences", userPrefs.getAllPreferences());
        report.put("performanceReport", performanceMonitor.getPerformanceReport());
        report.put("availableServices", getAllServices().stream()
            .map(s -> Map.of(
                "name", s.getServiceName(),
                "type", s.getServiceType().getCode(),
                "priority", s.getPriority()
            )).toList());
        return report;
    }

    /**
     * 获取所有服务
     * @return 服务列表
     */
    public static Collection<AIService> getAllServices() {
        initialize();
        return servicesByName.values();
    }

    /**
     * 服务是否支持指定模型
     */
    public interface ModelAwareService {
        boolean supportsModel(String model);
        String getDefaultModel();
    }
}