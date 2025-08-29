@file:Suppress("unused")

package io.agentclientprotocol.kotlin.junie

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

/**
 * Base class for all attachments.
 */
@Serializable
@JsonClassDiscriminator("method")
@OptIn(ExperimentalSerializationApi::class)
sealed interface EventAttachment {
    fun toMarkdown(): String? = null
}

@Serializable
@SerialName("agentState")
data class AgentStateAttachment(
    val agentState: String? = null,
    val sessionHistory: String? = null
): EventAttachment

@Serializable
@SerialName("currentActivity")
data class CurrentActivityAttachment(
    val activity: String
): EventAttachment

@Serializable
@SerialName("fileChange")
data class FileChangeAttachment(
    val change: FileChange
): EventAttachment

@Serializable
@SerialName("name")
data class NameAttachment(
    val name: String
): EventAttachment

@Serializable
@SerialName("patch")
data class PatchAttachment(
    val patch: String
): EventAttachment

@Serializable
@SerialName("plan")
data class PlanAttachment(
    val plan: List<PlanItem>
): EventAttachment

@Serializable
@SerialName("stepType")
data class StepTypeAttachment(
    val stepType: StepType
): EventAttachment

@Serializable
@SerialName("terminalCommand")
data class TerminalCommandAttachment(
    val command: String,
    val workingDirectory: String? = null
): EventAttachment

@Serializable
@SerialName("testRun")
data class TestRunAttachment(
    val testRun: TestRun
): EventAttachment