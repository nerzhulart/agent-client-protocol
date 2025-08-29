@file:Suppress("unused")

package io.agentclientprotocol.kotlin.junie

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * Enhanced Progress Event with attachments
 */
@Serializable
@SerialName("progressComplete")
data class ProgressEventComplete(
    val stepId: String = UUID.randomUUID().toString(),
    val title: String? = null,
    /**
     * Markdown description
     */
    val description: String? = null,
    val attachments: List<EventAttachment> = emptyList(),
    override val timestamp: Long = System.currentTimeMillis(),
    override val level: EventLevel = EventLevel.INFO,
) : AgentEvent

/**
 * Example event for documentation purposes
 */
@Serializable
@SerialName("example")
data class EventExample(
    val exampleData: String,
    override val timestamp: Long = System.currentTimeMillis(),
    override val level: EventLevel = EventLevel.INFO
) : AgentEvent