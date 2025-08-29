@file:Suppress("unused")

package io.agentclientprotocol.kotlin.junie

import kotlinx.serialization.Serializable

@Serializable
data class AgentStateBlob(override val content: String) : JunieBlob

@Serializable
data class AgentSessionHistorySnapshotBlob(override val content: String) : JunieBlob

interface JunieBlob {
    val content: String
}