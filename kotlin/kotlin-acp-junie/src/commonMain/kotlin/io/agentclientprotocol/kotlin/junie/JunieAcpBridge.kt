@file:Suppress("unused")

package io.agentclientprotocol.kotlin.junie

import io.agentclientprotocol.kotlin.*
import kotlinx.serialization.json.*
import java.nio.file.Paths

/**
 * Bridge that converts between Junie API types and ACP protocol types
 * Maintains full backward compatibility while enabling rich Junie functionality
 */
class JunieAcpBridge {
    
    // === Junie to ACP Conversions ===
    
    /**
     * Convert a Junie TaskRequest to an ACP PromptRequest
     */
    fun mapTaskRequestToPromptRequest(taskRequest: TaskRequest): PromptRequest {
        val contentBlocks = mutableListOf<ContentBlock>()
        
        // Main description with Junie context in annotations
        contentBlocks.add(
            ContentBlock.Text(
                text = taskRequest.description,
                annotations = JunieAnnotations(
                    taskRequestId = taskRequest.taskRequestId.id,
                    braveMode = taskRequest.braveMode,
                    issueType = taskRequest.type
                ).toStandardAnnotations()
            )
        )
        
        // Add attached files as resource links
        taskRequest.attachedFiles.forEach { filePath ->
            contentBlocks.add(
                ContentBlock.ResourceLink(
                    name = filePath.substringAfterLast('/'),
                    uri = "file://$filePath",
                    description = "Attached file",
                    annotations = JunieAnnotations(
                        taskRequestId = taskRequest.taskRequestId.id
                    ).toStandardAnnotations()
                )
            )
        }
        
        // Add open files context
        if (taskRequest.openFiles.isNotEmpty()) {
            contentBlocks.add(
                ContentBlock.Text(
                    text = "Currently open files: ${taskRequest.openFiles.joinToString(", ")}",
                    annotations = JunieAnnotations(
                        taskRequestId = taskRequest.taskRequestId.id
                    ).toStandardAnnotations()
                )
            )
        }
        
        return PromptRequest(
            sessionId = SessionId(taskRequest.taskRequestId.id),
            prompt = contentBlocks
        )
    }
    
    /**
     * Convert a Junie TaskRequest to extended Junie PromptRequest
     */
    fun mapTaskRequestToJuniePromptRequest(taskRequest: TaskRequest): JuniePromptRequest {
        val basePrompt = mapTaskRequestToPromptRequest(taskRequest)
        
        return JuniePromptRequest(
            sessionId = basePrompt.sessionId,
            prompt = basePrompt.prompt,
            taskContext = JunieTaskContext(
                taskRequestId = taskRequest.taskRequestId.id,
                braveMode = taskRequest.braveMode,
                issueType = taskRequest.type,
                attachedFiles = taskRequest.attachedFiles,
                openFiles = taskRequest.openFiles,
                recentFiles = taskRequest.recentFiles,
                created = taskRequest.created
            )
        )
    }
    
    /**
     * Convert a JunieEvent to an ACP SessionNotification
     */
    fun mapJunieEventToSessionNotification(
        junieEvent: JunieEvent,
        sessionId: SessionId
    ): SessionNotification {
        val update = when (val agentEvent = junieEvent.agentEvent) {
            is ToolCallEvent -> SessionUpdate.ToolCallUpdate(
                toolCallId = ToolCallId("${agentEvent.toolName}-${agentEvent.timestamp}"),
                title = agentEvent.toolName,
                kind = inferToolKindFromName(agentEvent.toolName),
                status = ToolCallStatus.IN_PROGRESS,
                rawInput = agentEvent.args
            )
            
            is ToolResultEvent -> SessionUpdate.ToolCallUpdateStatus(
                toolCallId = ToolCallId("${agentEvent.toolName}-${agentEvent.timestamp}"),
                status = ToolCallStatus.COMPLETED,
                rawOutput = agentEvent.result
            )
            
            is ProgressEvent -> SessionUpdate.AgentMessageChunk(
                content = ContentBlock.Text(
                    text = agentEvent.message,
                    annotations = JunieAnnotations(
                        severity = agentEvent.level
                    ).toStandardAnnotations()
                )
            )
            
            is InputResponseEvent -> SessionUpdate.AgentMessageChunk(
                content = ContentBlock.Text(
                    text = "User: ${agentEvent.input}\nAgent: ${agentEvent.response}",
                    annotations = JunieAnnotations(
                        severity = agentEvent.level
                    ).toStandardAnnotations()
                )
            )
            
            else -> SessionUpdate.AgentMessageChunk(
                content = ContentBlock.Text(
                    text = agentEvent.toString(),
                    annotations = JunieAnnotations(
                        severity = agentEvent.level
                    ).toStandardAnnotations()
                )
            )
        }
        
        return SessionNotification(sessionId, update)
    }
    
    /**
     * Convert a JunieEvent to a rich Junie SessionNotification
     */
    fun mapJunieEventToJunieSessionNotification(
        junieEvent: JunieEvent,
        sessionId: SessionId
    ): JunieSessionNotification {
        val junieUpdate = when (val agentEvent = junieEvent.agentEvent) {
            is ProgressEvent -> JunieSessionUpdate.ProgressEvent(
                message = agentEvent.message,
                percentage = agentEvent.percentage,
                severity = agentEvent.level
            )
            
            else -> null
        }
        
        return JunieSessionNotification(
            sessionId = sessionId,
            standardUpdate = if (junieUpdate == null) mapJunieEventToSessionNotification(junieEvent, sessionId).update else null,
            junieUpdate = junieUpdate
        )
    }
    
    /**
     * Convert a StepEntity to a Junie step progress update
     */
    fun mapStepEntityToStepProgress(
        stepEntity: StepEntity,
        sessionId: SessionId
    ): JunieSessionNotification {
        val fileChanges = stepEntity.changes.map { change ->
            JunieFileChange(
                beforeAbsolutePath = change.beforeAbsolutePath.toString(),
                beforeRelativePath = change.beforeRelativePath.toString(),
                afterAbsolutePath = change.afterAbsolutePath.toString(),
                afterRelativePath = change.afterRelativePath.toString(),
                beforeContent = change.beforeContent,
                afterContent = change.afterContent
            )
        }
        
        val update = JunieSessionUpdate.StepProgress(
            stepId = stepEntity.id,
            stepType = stepEntity.type ?: StepType.Info,
            description = stepEntity.description,
            command = stepEntity.command,
            isCompleted = stepEntity.isCompleted,
            isFullCompleted = stepEntity.isFullCompleted,
            msDuration = stepEntity.msDuration,
            testRuns = stepEntity.testRuns,
            fileChanges = fileChanges
        )
        
        return JunieSessionNotification(
            sessionId = sessionId,
            standardUpdate = null,
            junieUpdate = update
        )
    }
    
    /**
     * Convert a TaskResultDto to a Junie task result update
     */
    fun mapTaskResultToTaskResult(
        taskResult: TaskResultDto,
        sessionId: SessionId
    ): JunieSessionNotification {
        val update = JunieSessionUpdate.TaskResult(
            title = taskResult.title,
            output = taskResult.output,
            stepType = taskResult.type,
            patch = taskResult.patch,
            stateBlob = taskResult.stateBlob.content,
            sessionHistoryBlob = taskResult.sessionHistoryBlob.content
        )
        
        return JunieSessionNotification(
            sessionId = sessionId,
            standardUpdate = null,
            junieUpdate = update
        )
    }
    
    /**
     * Convert FileChange to ToolCallContent.Diff (standard ACP)
     */
    fun mapFileChangeToToolCallDiff(fileChange: FileChange): ToolCallContent.Diff {
        return ToolCallContent.Diff(
            path = fileChange.afterRelativePath.toString(),
            newText = (fileChange.afterContent as? TextFileChangeContent)?.data ?: "",
            oldText = (fileChange.beforeContent as? TextFileChangeContent)?.data
        )
    }
    
    /**
     * Convert FileChange to rich Junie ToolCallContent
     */
    fun mapFileChangeToJunieContent(fileChange: FileChange): JunieToolCallContent.RichFileChange {
        return JunieToolCallContent.RichFileChange(
            beforeAbsolutePath = fileChange.beforeAbsolutePath.toString(),
            beforeRelativePath = fileChange.beforeRelativePath.toString(),
            afterAbsolutePath = fileChange.afterAbsolutePath.toString(),
            afterRelativePath = fileChange.afterRelativePath.toString(),
            beforeContent = fileChange.beforeContent,
            afterContent = fileChange.afterContent,
            isCreated = fileChange.isCreated(),
            isDeleted = fileChange.isDeleted(),
            linesAdded = calculateLinesAdded(fileChange),
            linesRemoved = calculateLinesRemoved(fileChange)
        )
    }
    
    /**
     * Convert test runs to rich test results content
     */
    fun mapTestRunsToTestResults(testRuns: List<TestRun>): JunieToolCallContent.TestResults {
        val passed = testRuns.count { it.result == TestResult.Success }
        val failed = testRuns.count { it.result == TestResult.Failure }
        
        return JunieToolCallContent.TestResults(
            testRuns = testRuns,
            summary = "Tests: $passed passed, $failed failed",
            totalPassed = passed,
            totalFailed = failed
        )
    }
    
    // === ACP to Junie Conversions ===
    
    /**
     * Extract Junie context from ACP PromptRequest
     */
    fun extractJunieContextFromPromptRequest(request: PromptRequest): JunieTaskContext? {
        // Look for Junie annotations in the prompt content
        val junieAnnotations = request.prompt
            .mapNotNull { it.annotations as? JunieAnnotations }
            .firstOrNull()
        
        return junieAnnotations?.let {
            JunieTaskContext(
                taskRequestId = it.taskRequestId ?: request.sessionId.value,
                braveMode = it.braveMode ?: false,
                issueType = it.issueType ?: IssueType.CHAT
            )
        }
    }
    
    /**
     * Convert ACP SessionUpdate to JunieEvent if possible
     */
    fun mapSessionUpdateToJunieEvent(
        update: SessionUpdate,
        sessionId: SessionId
    ): JunieEvent? {
        return when (update) {
            is SessionUpdate.ToolCallUpdate -> JunieEvent(
                state = JunieTaskState.IN_PROGRESS,
                agentEvent = ToolCallEvent(
                    toolName = update.title,
                    agentName = "acp-agent",
                    args = update.rawInput ?: JsonPrimitive("{}"),
                    timestamp = System.currentTimeMillis(),
                    level = EventLevel.INFO
                )
            )
            
            is SessionUpdate.ToolCallUpdateStatus -> when (update.status) {
                ToolCallStatus.COMPLETED -> JunieEvent(
                    state = JunieTaskState.COMPLETED,
                    agentEvent = ToolResultEvent(
                        toolName = update.title ?: "unknown",
                        agentName = "acp-agent",
                        args = update.rawInput ?: JsonPrimitive("{}"),
                        result = update.rawOutput ?: JsonPrimitive("{}"),
                        timestamp = System.currentTimeMillis(),
                        level = EventLevel.INFO
                    )
                )
                ToolCallStatus.FAILED -> JunieEvent(
                    state = JunieTaskState.FAILED,
                    agentEvent = ProgressEvent(
                        message = "Tool call failed: ${update.title}",
                        timestamp = System.currentTimeMillis(),
                        level = EventLevel.ERROR
                    )
                )
                else -> null
            }
            
            is SessionUpdate.AgentMessageChunk -> {
                val annotations = update.content.annotations as? JunieAnnotations
                JunieEvent(
                    state = JunieTaskState.IN_PROGRESS,
                    agentEvent = ProgressEvent(
                        message = when (val content = update.content) {
                            is ContentBlock.Text -> content.text
                            else -> content.toString()
                        },
                        timestamp = System.currentTimeMillis(),
                        level = annotations?.severity ?: EventLevel.INFO
                    )
                )
            }
            
            else -> null
        }
    }
    
    // === Compatibility and Utility Methods ===
    
    /**
     * Create a compatibility layer that handles both Junie and standard ACP clients
     */
    fun createCompatibleSessionNotification(
        junieUpdate: JunieSessionUpdate,
        sessionId: SessionId
    ): SessionNotification {
        // For standard ACP clients, convert to basic types they understand
        val standardUpdate = when (junieUpdate) {
            is JunieSessionUpdate.StepProgress -> SessionUpdate.AgentMessageChunk(
                content = ContentBlock.Text(
                    text = buildStepProgressMessage(junieUpdate),
                    annotations = JunieAnnotations(
                        stepType = junieUpdate.stepType,
                        msDuration = junieUpdate.msDuration,
                        testRunCount = junieUpdate.testRuns.size,
                        fileChangeCount = junieUpdate.fileChanges.size
                    ).toStandardAnnotations()
                )
            )
            
            is JunieSessionUpdate.TaskResult -> SessionUpdate.AgentMessageChunk(
                content = ContentBlock.Text(
                    text = junieUpdate.output,
                    annotations = JunieAnnotations(
                        stepType = junieUpdate.stepType
                    ).toStandardAnnotations()
                )
            )
            
            is JunieSessionUpdate.ProgressEvent -> SessionUpdate.AgentMessageChunk(
                content = ContentBlock.Text(
                    text = junieUpdate.message,
                    annotations = JunieAnnotations(
                        severity = junieUpdate.severity
                    ).toStandardAnnotations()
                )
            )
            
            is JunieSessionUpdate.AgentStateUpdate -> SessionUpdate.AgentMessageChunk(
                content = ContentBlock.Text(
                    text = "Agent state updated"
                )
            )
            
            is JunieSessionUpdate.TaskStateChange -> SessionUpdate.AgentMessageChunk(
                content = ContentBlock.Text(
                    text = "Task ${junieUpdate.taskId} changed from ${junieUpdate.previousState} to ${junieUpdate.newState}"
                )
            )
        }
        
        return SessionNotification(sessionId, standardUpdate)
    }
    
    private fun buildStepProgressMessage(step: JunieSessionUpdate.StepProgress): String {
        val status = when {
            step.isFullCompleted -> "✅ Completed"
            step.isCompleted -> "⏳ Partially Complete"
            else -> "🔄 In Progress"
        }
        
        val parts = mutableListOf<String>()
        parts.add("$status: ${step.stepType}")
        
        step.description?.let { parts.add("- $it") }
        step.command?.let { parts.add("Command: $it") }
        
        if (step.testRuns.isNotEmpty()) {
            val passed = step.testRuns.count { it.result == TestResult.Success }
            val failed = step.testRuns.count { it.result == TestResult.Failure }
            parts.add("Tests: $passed passed, $failed failed")
        }
        
        if (step.fileChanges.isNotEmpty()) {
            parts.add("Files changed: ${step.fileChanges.size}")
        }
        
        step.msDuration?.let {
            parts.add("Duration: ${it}ms")
        }
        
        return parts.joinToString("\n")
    }
    
    private fun inferToolKindFromName(toolName: String): ToolKind {
        return when {
            toolName.contains("read", ignoreCase = true) -> ToolKind.READ
            toolName.contains("write", ignoreCase = true) || 
            toolName.contains("edit", ignoreCase = true) -> ToolKind.EDIT
            toolName.contains("delete", ignoreCase = true) -> ToolKind.DELETE
            toolName.contains("search", ignoreCase = true) -> ToolKind.SEARCH
            toolName.contains("execute", ignoreCase = true) ||
            toolName.contains("run", ignoreCase = true) -> ToolKind.EXECUTE
            toolName.contains("fetch", ignoreCase = true) ||
            toolName.contains("get", ignoreCase = true) -> ToolKind.FETCH
            else -> ToolKind.OTHER
        }
    }
    
    private fun calculateLinesAdded(fileChange: FileChange): Int? {
        val beforeText = (fileChange.beforeContent as? TextFileChangeContent)?.data
        val afterText = (fileChange.afterContent as? TextFileChangeContent)?.data
        
        return if (beforeText != null && afterText != null) {
            maxOf(0, afterText.lines().size - beforeText.lines().size)
        } else null
    }
    
    private fun calculateLinesRemoved(fileChange: FileChange): Int? {
        val beforeText = (fileChange.beforeContent as? TextFileChangeContent)?.data
        val afterText = (fileChange.afterContent as? TextFileChangeContent)?.data
        
        return if (beforeText != null && afterText != null) {
            maxOf(0, beforeText.lines().size - afterText.lines().size)
        } else null
    }
}