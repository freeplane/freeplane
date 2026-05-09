package org.freeplane.plugin.ai.tools;

import dev.langchain4j.data.message.UserMessage;
import org.freeplane.core.resources.ResourceController;

public class MessageBuilder {
    public static final String SYSTEM_MESSAGE_PROPERTY = "ai_system_message";
    public static final String CONTROL_INSTRUCTION_PREFIX =
        "control instruction, please confirm with \"ok\": ";
    // 角色定义
    private static final String ROLE_DEFINITION =
        "You are a Freeplane AI assistant. "
            + "Your task is to execute the user's mind map operations precisely and return results as requested. "
            + "Do NOT introduce yourself or describe your capabilities unless explicitly asked.";
    
    // 工具调用指导
    private static final String TOOL_CALL_GUIDANCE =
        "Tool Calling Instructions:\n"
            + "1. When you need to perform actions on the mind map, use the appropriate tools\n"
            + "2. All tool calls require arguments wrapped under the single parameter named request\n"
            + "3. Example: tool({ \"request\": { ... } })\n"
            + "4. Always validate tool arguments before calling\n"
            + "5. Handle tool errors gracefully and inform the user\n"
            + "6. For complex tasks, combine multiple tool calls as needed";
    
    // 响应格式指导
    private static final String RESPONSE_FORMAT_GUIDANCE =
        "Response Format Guidelines:\n"
            + "1. Respond in Markdown format for regular conversations\n"
            + "2. For tool calls, use the exact JSON format required by the tool\n"
            + "3. For mind map operations, return strictly formatted JSON as specified\n"
            + "4. Be clear, concise, and structured in your responses";
    
    // 地图选择指导
    private static final String MAP_SELECTION_GUIDANCE =
        "Map Selection Awareness:\n"
            + "- Map selection can change between messages\n"
            + "- If a request seems misaligned with prior map references, confirm the current map before proceeding\n"
            + "- Always verify you're working with the correct map context";
    
    // 配置文件控制指导
    private static final String PROFILE_CONTROL_GUIDANCE =
        "Profile Control Instructions:\n"
            + "- Control instructions start with: " + CONTROL_INSTRUCTION_PREFIX
            + "- Profile changes are communicated through these control instructions\n"
            + "- Treat the latest profile change as authoritative\n"
            + "- Older profile changes may omit profile definition and include only \"Now you have the profile <Name>.\"";
    
    // 思维链指导
    private static final String CHAIN_OF_THOUGHT_GUIDANCE =
        "Chain of Thought Process:\n"
            + "For complex tasks, think step-by-step:\n"
            + "1. Understand the user's intent and requirements\n"
            + "2. Plan your approach based on available tools\n"
            + "3. Execute tools as needed to achieve the goal\n"
            + "4. Verify results and ensure they meet requirements\n"
            + "5. Provide clear, structured output to the user";
    
    // 质量保证指导
    private static final String QUALITY_ASSURANCE_GUIDANCE =
        "Quality Assurance Checklist:\n"
            + "Before finalizing your response:\n"
            + "- Ensure all user requirements are met\n"
            + "- Verify map/node operations succeeded\n"
            + "- Check for consistency and completeness\n"
            + "- Ensure responses are clear and well-structured\n"
            + "- Confirm tool calls are properly formatted";
    @FunctionalInterface
    interface MessageTextProvider {
        String getMessageText();
    }

    private final MessageTextProvider messageTextProvider;

    public MessageBuilder() {
        this(new ResourceControllerMessageTextProvider());
    }

    MessageBuilder(MessageTextProvider messageTextProvider) {
        this.messageTextProvider = messageTextProvider;
    }

    public String buildForChat() {
        String message = messageTextProvider.getMessageText();
        String guidance = ROLE_DEFINITION + "\n\n" 
            + TOOL_CALL_GUIDANCE + "\n\n"
            + MAP_SELECTION_GUIDANCE + "\n\n"
            + PROFILE_CONTROL_GUIDANCE;
        if (message == null) {
            return guidance;
        }
        String trimmed = message.trim();
        if (trimmed.isEmpty()) {
            return guidance;
        }
        return trimmed + "\n\n" + guidance;
    }

    public static String buildAssistantProfileInstruction(String profileName,
                                                          String profileDefinition,
                                                          boolean containsProfileDefinition) {
        String marker = buildAssistantProfileMarker(profileName);
        if (!containsProfileDefinition) {
            return marker;
        }
        String definition = profileDefinition == null ? "" : profileDefinition.trim();
        if (definition.isEmpty()) {
            return marker;
        }
        return marker + "\nProfile definition: " + definition;
    }

    public static String buildAssistantProfileMarker(String profileName) {
        String name = profileName == null ? "" : profileName.trim();
        if (name.isEmpty()) {
            return "Now you have the profile.";
        }
        return "Now you have the profile " + name + ".";
    }

    public static UserMessage buildSystemInstructionUserMessage(String text) {
        return UserMessage.from(buildSystemInstructionText(text));
    }

    public static String buildSystemInstructionText(String text) {
        return CONTROL_INSTRUCTION_PREFIX + (text == null ? "" : text);
    }

    public static String buildInstructionAcknowledgementText() {
        return "ok";
    }

    private static class ResourceControllerMessageTextProvider implements MessageTextProvider {

        @Override
        public String getMessageText() {
            ResourceController resourceController = ResourceController.getResourceController();
            String message = resourceController.getProperty(SYSTEM_MESSAGE_PROPERTY);
            return message;
        }
    }
}
