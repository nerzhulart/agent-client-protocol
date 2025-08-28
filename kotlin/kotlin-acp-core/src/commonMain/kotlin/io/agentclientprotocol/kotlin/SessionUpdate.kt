@file:Suppress("unused")

package io.agentclientprotocol.kotlin

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Different types of updates that can be sent during session processing.
 *
 * These updates provide real-time feedback about the agent's progress.
 *
 * See protocol docs: [Agent Reports Output](https://agentclientprotocol.com/protocol/prompt-turn#3-agent-reports-output)
 */
@Serializable
public sealed class SessionUpdate {
    /**
     * A chunk of the user's message being streamed.
     */
    @Serializable
    @SerialName("user_message_chunk")
    public data class UserMessageChunk(
        val content: ContentBlock
    ) : SessionUpdate()

    /**
     * A chunk of the agent's response being streamed.
     */
    @Serializable
    @SerialName("agent_message_chunk")
    public data class AgentMessageChunk(
        val content: ContentBlock
    ) : SessionUpdate()

    /**
     * A chunk of the agent's internal reasoning being streamed.
     */
    @Serializable
    @SerialName("agent_thought_chunk")
    public data class AgentThoughtChunk(
        val content: ContentBlock
    ) : SessionUpdate()

    /**
     * Notification that a new tool call has been initiated.
     */
    @Serializable
    @SerialName("tool_call")
    public data class ToolCallUpdate(
        val toolCallId: ToolCallId,
        val title: String,
        val kind: ToolKind? = null,
        val status: ToolCallStatus? = null,
        val content: List<ToolCallContent> = emptyList(),
        val locations: List<ToolCallLocation> = emptyList(),
        val rawInput: JsonElement? = null,
        val rawOutput: JsonElement? = null
    ) : SessionUpdate()

    /**
     * Update on the status or results of a tool call.
     */
    @Serializable
    @SerialName("tool_call_update")
    public data class ToolCallUpdateStatus(
        val toolCallId: ToolCallId,
        val title: String? = null,
        val kind: ToolKind? = null,
        val status: ToolCallStatus? = null,
        val content: List<ToolCallContent>? = null,
        val locations: List<ToolCallLocation>? = null,
        val rawInput: JsonElement? = null,
        val rawOutput: JsonElement? = null
    ) : SessionUpdate()

    /**
     * The agent's execution plan for complex tasks.
     *
     * See protocol docs: [Agent Plan](https://agentclientprotocol.com/protocol/agent-plan)
     */
    @Serializable
    @SerialName("plan")
    public data class PlanUpdate(
        val entries: List<PlanEntry>
    ) : SessionUpdate()
}