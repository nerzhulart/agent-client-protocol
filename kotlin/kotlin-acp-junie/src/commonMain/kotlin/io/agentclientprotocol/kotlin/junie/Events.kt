@file:Suppress("unused")

package io.agentclientprotocol.kotlin.junie

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonClassDiscriminator
import kotlinx.serialization.json.JsonElement

@Serializable
data class JunieEvent(
    val state: JunieTaskState,
    val agentEvent: AgentEvent
)

@Serializable
enum class JunieTaskState {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    FAILED
}

@Serializable
@JsonClassDiscriminator("method")
@OptIn(ExperimentalSerializationApi::class)
sealed interface AgentEvent {
    val timestamp: Long
    val level: EventLevel
}

@Serializable
enum class EventLevel {
    @SerialName("info")
    INFO,

    @SerialName("warn")
    WARN,

    @SerialName("error")
    ERROR
}

@Serializable
@SerialName("toolCall")
data class ToolCallEvent(
    val toolName: String,
    val agentName: String,
    val args: JsonElement,
    override val timestamp: Long = System.currentTimeMillis(),
    override val level: EventLevel = EventLevel.INFO
) : AgentEvent

@Serializable
@SerialName("toolResult")
data class ToolResultEvent(
    val toolName: String,
    val agentName: String,
    val args: JsonElement,
    val result: JsonElement,
    override val timestamp: Long = System.currentTimeMillis(),
    override val level: EventLevel = EventLevel.INFO
) : AgentEvent

@Serializable
@SerialName("inputResponse")
data class InputResponseEvent(
    val input: String,
    val response: String,
    override val timestamp: Long = System.currentTimeMillis(),
    override val level: EventLevel = EventLevel.INFO
) : AgentEvent

@Serializable
@SerialName("progress")
data class ProgressEvent(
    val message: String,
    val percentage: Double? = null,
    override val timestamp: Long = System.currentTimeMillis(),
    override val level: EventLevel = EventLevel.INFO
) : AgentEvent

fun InputResponseEvent.toJsonString(): String = Json.Default.encodeToString(this)
fun agentInteractionUserResponseFromString(json: String): InputResponseEvent =
    Json.decodeFromString<InputResponseEvent>(json)