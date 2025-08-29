@file:Suppress("unused")

package io.agentclientprotocol.kotlin.junie

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.nio.file.Path
import java.util.UUID

// === Speed Mode ===

@Serializable
enum class JunieSpeedMode {
    Speed,
    Quality
}

// === Plan Item ===

@Serializable
@Suppress("PROVIDED_RUNTIME_TOO_LOW")
data class PlanItem(val description: String, val status: Status)

@Serializable
enum class Status(val symbol: Char) {
    DONE('✓'),
    ERROR('!'),
    IN_PROGRESS('*'),
    PENDING(' '),
    CANCELED('x');

    companion object {
        fun fromSymbol(symbol: Char): Status {
            return entries.find { it.symbol == symbol } ?: PENDING
        }
    }
}

// === Progress ===

@Serializable
class ProgressEntity(val text: String, val level: ProgressLevel)

@Serializable
enum class ProgressLevel {
    Info,
    Warn
}

// === Task ID ===

@Serializable
data class TaskId(
    val index: Int,
) {
    override fun toString(): String = index.toString()
}

// === Task State (enhanced version) ===

enum class JunieTaskStateComplete {
    IN_PROGRESS,
    INPUT_REQUIRED,
    COMPLETED,
    FAILED,
    CANCELLED
}

// === Editor Context ===

@Suppress("PROVIDED_RUNTIME_TOO_LOW")
@Serializable
data class EditorContext(val recentFiles: List<String>, val openFiles: List<String>)

// === Task Entity Proxy ===

@Serializable
data class TaskEntityProxy(
    val id: TaskId,
    val type: IssueType = IssueType.ISSUE,
    val created: Long, // Using Long timestamp instead of Instant for simplicity
    val artifactPath: String,
    val context: ExplicitTaskContext,
    val previousTasksInfo: PreviousTasksInfoProxyComplete?,
    val finalAgentStateBlob: AgentStateBlob?,
    val isDeclined: Boolean,
    val plan: List<PlanItem>? = null,
    val patch: String? = null,
    val terminalLog: String? = null,
    val sessionHistoryBlob: AgentSessionHistorySnapshotBlob? = null,
)

val TaskEntityProxy.explicitlySelectedContextFiles: List<Path>
    get() = context.explicitlySelectedContextFiles

val TaskEntityProxy.description: String
    get() = context.description

// === Previous Tasks Info (enhanced) ===

@Serializable
data class PreviousTasksInfoProxyComplete(
    val agentState: AgentStateBlob?,
    val patch: String?,
    val sessionHistory: AgentSessionHistorySnapshotBlob?,
)

// === Step Building Events (legacy) ===

@Deprecated("Use JunieEvent instead")
@Serializable
sealed interface StepBuildingEvent {
    @Serializable
    class NameAppeared(val name: String) : StepBuildingEvent

    @Serializable
    class StepUpdated(val step: StepEntity, val index: Int) : StepBuildingEvent

    @Serializable
    class ResultAppeared(val result: TaskResultDto) : StepBuildingEvent

    @Serializable
    class ResultAppearedNew(val result: TaskResultDtoNew) : StepBuildingEvent

    @Serializable
    class ErrorOccurred(val exceptionMessage: String?) : StepBuildingEvent

    @Serializable
    class AgentStateUpdated(val agentStateBlob: AgentStateBlob) : StepBuildingEvent

    @Serializable
    class AgentSessionHistoryUpdated(val historySnapshotBlob: AgentSessionHistorySnapshotBlob) : StepBuildingEvent

    @Serializable
    class PlanUpdated(val plan: List<PlanItem>) : StepBuildingEvent

    @Serializable
    class ProgressUpdated(val progress: ProgressEntity?) : StepBuildingEvent

    @Serializable
    class TerminalSessionStarted(val uuid: String) : StepBuildingEvent

    @Serializable
    class TerminalSessionFinished(val uuid: String, val sessionLog: String) : StepBuildingEvent
}