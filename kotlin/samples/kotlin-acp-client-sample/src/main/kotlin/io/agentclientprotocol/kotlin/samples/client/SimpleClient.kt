package io.agentclientprotocol.kotlin.samples.client

import io.agentclientprotocol.kotlin.*

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.delay
import java.io.File

private val logger = KotlinLogging.logger {}

/**
 * Simple example client implementation.
 * 
 * This client demonstrates basic ACP functionality including:
 * - File system operations
 * - Permission handling
 * - Session update processing
 */
class SimpleClient(private val workingDirectory: File = File(".")) : Client {
    
    override suspend fun readTextFile(request: ReadTextFileRequest): ReadTextFileResponse {
        logger.info { "Reading file: ${request.path}" }
        
        val file = File(workingDirectory, request.path).canonicalFile
        
        // Basic security check - ensure file is within working directory
        if (!file.path.startsWith(workingDirectory.canonicalPath)) {
            throw SecurityException("File access outside working directory: ${request.path}")
        }
        
        if (!file.exists()) {
            throw NoSuchFileException(file, null, "File not found")
        }
        
        val lines = file.readLines()
        val startLine = (request.line?.toInt() ?: 1) - 1 // Convert to 0-based
        val limit = request.limit?.toInt()
        
        val selectedLines = when {
            startLine >= lines.size -> emptyList()
            limit != null -> lines.drop(startLine).take(limit)
            else -> lines.drop(startLine)
        }
        
        return ReadTextFileResponse(selectedLines.joinToString("\n"))
    }
    
    override suspend fun writeTextFile(request: WriteTextFileRequest) {
        logger.info { "Writing file: ${request.path}" }
        
        val file = File(workingDirectory, request.path).canonicalFile
        
        // Basic security check - ensure file is within working directory
        if (!file.path.startsWith(workingDirectory.canonicalPath)) {
            throw SecurityException("File access outside working directory: ${request.path}")
        }
        
        // Create parent directories if needed
        file.parentFile?.mkdirs()
        
        file.writeText(request.content)
    }
    
    override suspend fun requestPermission(request: RequestPermissionRequest): RequestPermissionResponse {
        logger.info { "Permission requested for tool call: ${request.toolCall.title}" }
        
        // For this simple example, we'll auto-approve read operations
        // and prompt for write operations
        val autoApprove = when (request.toolCall.kind) {
            ToolKind.READ, ToolKind.SEARCH -> true
            else -> false
        }
        
        if (autoApprove) {
            val allowOnceOption = request.options.find { it.kind == PermissionOptionKind.ALLOW_ONCE }
            if (allowOnceOption != null) {
                logger.info { "Auto-approving read operation" }
                return RequestPermissionResponse(
                    RequestPermissionOutcome.Selected(allowOnceOption.optionId)
                )
            }
        }
        
        // For demo purposes, simulate user interaction
        println("Agent requesting permission for: ${request.toolCall.title}")
        println("Tool kind: ${request.toolCall.kind}")
        println("Available options:")
        request.options.forEachIndexed { index, option ->
            println("  ${index + 1}. ${option.name} (${option.kind})")
        }
        
        // Simulate user delay and selection
        delay(1000)
        val selectedOption = request.options.firstOrNull() 
            ?: return RequestPermissionResponse(RequestPermissionOutcome.Cancelled)
            
        logger.info { "Selected option: ${selectedOption.name}" }
        return RequestPermissionResponse(
            RequestPermissionOutcome.Selected(selectedOption.optionId)
        )
    }
    
    override suspend fun sessionUpdate(notification: SessionNotification) {
        when (val update = notification.update) {
            is SessionUpdate.UserMessageChunk -> {
                when (val content = update.content) {
                    is ContentBlock.Text -> println("User: ${content.text}")
                    else -> println("User: [${content::class.simpleName}]")
                }
            }
            
            is SessionUpdate.AgentMessageChunk -> {
                when (val content = update.content) {
                    is ContentBlock.Text -> println("Agent: ${content.text}")
                    else -> println("Agent: [${content::class.simpleName}]")
                }
            }
            
            is SessionUpdate.AgentThoughtChunk -> {
                when (val content = update.content) {
                    is ContentBlock.Text -> println("Agent thinks: ${content.text}")
                    else -> println("Agent thinks: [${content::class.simpleName}]")
                }
            }
            
            is SessionUpdate.ToolCallUpdate -> {
                println("Tool call started: ${update.title} (${update.kind})")
                if (update.status != null) {
                    println("  Status: ${update.status}")
                }
            }
            
            is SessionUpdate.ToolCallUpdateStatus -> {
                println("Tool call updated: ${update.toolCallId}")
                if (update.status != null) {
                    println("  Status: ${update.status}")
                }
                if (update.title != null) {
                    println("  Title: ${update.title}")
                }
            }
            
            is SessionUpdate.PlanUpdate -> {
                println("Agent plan:")
                update.entries.forEach { entry ->
                    println("  [${entry.status}] ${entry.content} (${entry.priority})")
                }
            }
        }
    }
}