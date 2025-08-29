@file:Suppress("unused")

package io.agentclientprotocol.kotlin.junie

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class TaskRequestId(
    val id: String,
)

@Serializable
data class TaskRequest(
    val taskRequestId: TaskRequestId,
    val description: String,
    val braveMode : Boolean,
    val type: IssueType,
    val created: Long,
    val attachedFiles: List<String>,
    val openFiles: List<String>,
    val recentFiles: List<String>,
    val previousTaskInfo: PreviousTasksInfoProxy?
)

@Serializable
data class PreviousTasksInfoProxy(
    val agentState: AgentStateBlob?,
    val patch: String?,
    val sessionHistory: AgentSessionHistorySnapshotBlob?,
)

@Serializable
data class TaskResultDto(
    val title: String?,
    val output: String,
    val type: StepType,
    val stateBlob: AgentStateBlob,
    val patch: String,
    val sessionHistoryBlob: AgentSessionHistorySnapshotBlob
)

@Serializable
data class TaskResultDtoNew(
    val title: String?,
    val output: String,
)

fun TaskRequest.toJsonString(): String = Json.encodeToString(this)
fun taskRequestFromString(json: String): TaskRequest = Json.decodeFromString<TaskRequest>(json)