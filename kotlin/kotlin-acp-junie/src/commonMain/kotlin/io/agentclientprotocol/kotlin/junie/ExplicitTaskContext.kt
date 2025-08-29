@file:Suppress("unused")

package io.agentclientprotocol.kotlin.junie

import kotlinx.serialization.Serializable
import java.nio.file.Path

/**
 * Information about the issue, which can be useful for the generation
 */
@Serializable
data class ExplicitTaskContext(
    val type: IssueType = IssueType.ISSUE,
    val description: String,
    @Serializable(with = PathListSerializer::class)
    val explicitlySelectedContextFiles: List<Path> = emptyList(),
)