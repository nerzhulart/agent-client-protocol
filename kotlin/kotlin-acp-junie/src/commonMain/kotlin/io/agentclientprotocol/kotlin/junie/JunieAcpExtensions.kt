@file:Suppress("unused")

package io.agentclientprotocol.kotlin.junie

import io.agentclientprotocol.kotlin.*
import kotlinx.serialization.*
import kotlinx.serialization.json.JsonElement

/**
 * Junie-specific extensions to ACP protocol that maintain full backward compatibility
 */

// === Junie Annotations (Approach 1) ===

/**
 * Junie-specific annotations that extend standard ACP annotations
 * These can be used in place of standard annotations to provide rich Junie metadata
 */
@Serializable
data class JunieAnnotations(
    // Standard ACP annotations
    val audience: List<Role>? = null,
    val priority: Double? = null,
    val lastModified: String? = null,
    // Junie-specific extensions
    val severity: EventLevel? = null,
    val stepType: StepType? = null,
    val agentName: String? = null,
    val taskRequestId: String? = null,
    val braveMode: Boolean? = null,
    val issueType: IssueType? = null,
    val msDuration: Long? = null,
    val testRunCount: Int? = null,
    val fileChangeCount: Int? = null
)

/**
 * Convert JunieAnnotations to standard ACP Annotations
 */
fun JunieAnnotations.toStandardAnnotations(): Annotations {
    return Annotations(
        audience = audience,
        priority = priority,
        lastModified = lastModified
    )
}

// === Junie-specific SessionUpdate types ===

/**
 * Junie-specific session updates that provide rich step-by-step execution tracking
 * These can be used alongside standard ACP SessionUpdate types
 */
@Serializable
sealed class JunieSessionUpdate {
    
    /**
     * Detailed step progress tracking with rich metadata
     */
    @Serializable
    @SerialName("junie_step_progress")
    data class StepProgress(
        val stepId: String,
        val stepType: StepType,
        val description: String?,
        val command: String?,
        val isCompleted: Boolean,
        val isFullCompleted: Boolean,
        val msDuration: Long? = null,
        val testRuns: List<TestRun> = emptyList(),
        val fileChanges: List<JunieFileChange> = emptyList()
    ) : JunieSessionUpdate()
    
    /**
     * Complete task result with all Junie-specific data
     */
    @Serializable
    @SerialName("junie_task_result")
    data class TaskResult(
        val title: String?,
        val output: String,
        val stepType: StepType,
        val patch: String,
        val stateBlob: String,
        val sessionHistoryBlob: String
    ) : JunieSessionUpdate()
    
    /**
     * Agent state updates for maintaining session continuity
     */
    @Serializable
    @SerialName("junie_agent_state")
    data class AgentStateUpdate(
        val stateBlob: String,
        val timestamp: Long = System.currentTimeMillis()
    ) : JunieSessionUpdate()
    
    /**
     * Progress events with severity levels
     */
    @Serializable
    @SerialName("junie_progress_event")
    data class ProgressEvent(
        val message: String,
        val percentage: Double? = null,
        val severity: EventLevel = EventLevel.INFO
    ) : JunieSessionUpdate()
    
    /**
     * Junie task state changes
     */
    @Serializable
    @SerialName("junie_task_state_change")
    data class TaskStateChange(
        val taskId: String,
        val previousState: JunieTaskState,
        val newState: JunieTaskState,
        val reason: String? = null
    ) : JunieSessionUpdate()
}

// === Junie-specific ToolCallContent types ===

/**
 * Junie-specific tool call content types that provide richer information than standard ACP
 */
@Serializable
sealed class JunieToolCallContent {
    
    /**
     * Rich test results with detailed pass/fail information
     */
    @Serializable
    @SerialName("junie_test_results")
    data class TestResults(
        val testRuns: List<TestRun>,
        val summary: String? = null,
        val totalPassed: Int,
        val totalFailed: Int,
        val executionTime: Long? = null
    ) : JunieToolCallContent()
    
    /**
     * Rich file change information with before/after content
     */
    @Serializable
    @SerialName("junie_rich_file_change")
    data class RichFileChange(
        val beforeAbsolutePath: String,
        val beforeRelativePath: String,
        val afterAbsolutePath: String? = null,
        val afterRelativePath: String? = null,
        val beforeContent: FileChangeContent?,
        val afterContent: FileChangeContent?,
        val isCreated: Boolean = beforeContent == null,
        val isDeleted: Boolean = afterContent == null,
        val linesAdded: Int? = null,
        val linesRemoved: Int? = null
    ) : JunieToolCallContent()
    
    /**
     * Agent reasoning/thinking content
     */
    @Serializable
    @SerialName("junie_agent_thinking")
    data class AgentThinking(
        val thoughts: String,
        val reasoning: String? = null,
        val confidence: Double? = null
    ) : JunieToolCallContent()
}

/**
 * Simplified file change representation for cross-protocol compatibility
 */
@Serializable
data class JunieFileChange(
    val beforeAbsolutePath: String,
    val beforeRelativePath: String,
    val afterAbsolutePath: String = beforeAbsolutePath,
    val afterRelativePath: String = beforeRelativePath,
    val beforeContent: FileChangeContent?,
    val afterContent: FileChangeContent?,
    val changeType: FileChangeType = when {
        beforeContent == null -> FileChangeType.CREATED
        afterContent == null -> FileChangeType.DELETED
        else -> FileChangeType.MODIFIED
    }
)

@Serializable
enum class FileChangeType {
    CREATED,
    MODIFIED,
    DELETED,
    MOVED
}

// === Extended Capabilities ===

/**
 * Junie-specific capabilities that can be negotiated during initialization
 */
@Serializable
data class JunieCapabilities(
    @EncodeDefault val stepTracking: Boolean = false,
    @EncodeDefault val richFileChanges: Boolean = false,
    @EncodeDefault val testResultTracking: Boolean = false,
    @EncodeDefault val agentStateManagement: Boolean = false,
    @EncodeDefault val severityLevels: Boolean = false,
    @EncodeDefault val progressTracking: Boolean = false,
    @EncodeDefault val taskContextTracking: Boolean = false,
    @EncodeDefault val braveMode: Boolean = false
)

/**
 * Extended agent capabilities that include Junie support
 */
@Serializable
data class JunieAgentCapabilities(
    @EncodeDefault val loadSession: Boolean = false,
    @EncodeDefault val promptCapabilities: PromptCapabilities = PromptCapabilities(),
    @EncodeDefault val junie: JunieCapabilities = JunieCapabilities()
)

/**
 * Extended client capabilities that include Junie support  
 */
@Serializable
data class JunieClientCapabilities(
    @EncodeDefault val fs: FileSystemCapability = FileSystemCapability(),
    @EncodeDefault val junie: JunieCapabilities = JunieCapabilities()
)

// === Extended Request/Response Types ===

/**
 * Extended initialization request that includes Junie capabilities
 */
@Serializable
data class JunieInitializeRequest(
    val protocolVersion: ProtocolVersion,
    val clientCapabilities: ClientCapabilities = ClientCapabilities(),
    val junieClientCapabilities: JunieClientCapabilities? = null
)

/**
 * Extended initialization response that includes Junie capabilities
 */
@Serializable
data class JunieInitializeResponse(
    val protocolVersion: ProtocolVersion,
    val agentCapabilities: AgentCapabilities = AgentCapabilities(),
    val authMethods: List<AuthMethod> = emptyList(),
    val junieAgentCapabilities: JunieAgentCapabilities? = null
)

/**
 * Extended prompt request with Junie task context
 */
@Serializable
data class JuniePromptRequest(
    val sessionId: SessionId,
    val prompt: List<ContentBlock>,
    val taskContext: JunieTaskContext? = null
)

/**
 * Junie-specific task context information
 */
@Serializable
data class JunieTaskContext(
    val taskRequestId: String,
    val braveMode: Boolean = false,
    val issueType: IssueType = IssueType.CHAT,
    val attachedFiles: List<String> = emptyList(),
    val openFiles: List<String> = emptyList(),
    val recentFiles: List<String> = emptyList(),
    val created: Long = System.currentTimeMillis()
)

/**
 * Wrapper for SessionNotification that can contain both standard and Junie updates
 */
@Serializable
data class JunieSessionNotification(
    val sessionId: SessionId,
    val standardUpdate: SessionUpdate? = null,
    val junieUpdate: JunieSessionUpdate? = null
)

// === Utility Extensions ===

/**
 * Create a ContentBlock.Text with Junie annotations
 */
fun createJunieTextContent(
    text: String,
    junieAnnotations: JunieAnnotations
): ContentBlock.Text {
    return ContentBlock.Text(
        text = text,
        annotations = junieAnnotations.toStandardAnnotations()
    )
}

/**
 * Extract Junie metadata from standard ACP annotations if present
 */
fun Annotations.extractJunieMetadata(): Map<String, Any?> {
    // This would require custom serialization logic to extract additional fields
    // For now, return empty map as we can't access unknown fields
    return emptyMap()
}