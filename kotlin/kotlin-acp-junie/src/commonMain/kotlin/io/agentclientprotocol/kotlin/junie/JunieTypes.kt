@file:Suppress("unused")

package io.agentclientprotocol.kotlin.junie

import kotlinx.serialization.Serializable

/**
 * Core Junie API types copied from the Junie codebase
 */

@Serializable
enum class IssueType {
    ISSUE,
    CHAT
}

@Serializable
enum class StepType {
    Prompt,
    Edit,
    Tests,
    Build,
    Preview,
    RunAppUser,
    Terminal,
    Report,
    Info,
    Progress,
    ChatResponse,
    Mcp,
    AskQuestion,
    AnsweredQuestion,
    Answer
}

@Serializable
enum class TestResult {
    Success,
    Failure
}

@Serializable
data class TestRun(val name: String, val result: TestResult)

@Serializable
@Suppress("PROVIDED_RUNTIME_TOO_LOW")
data class StepEntity(
    val id: String,
    val command: String?,
    val type: StepType?,
    val description: String?,
    val changes: List<FileChange>,
    val testRuns: List<TestRun>,
    val msDuration: Long?,
) {
    val isCompleted: Boolean
        get() {
            return if (type == StepType.Prompt || type == StepType.Report) {
                description != null
            } else {
                !command.isNullOrEmpty() && type != null
            }
        }

    val isFullCompleted: Boolean
        get() {
            if (description == null) return false
            return when (type) {
                StepType.Prompt, StepType.Report, StepType.ChatResponse -> {
                    return true
                }

                else -> {
                    type != null && !command.isNullOrEmpty()
                }
            }
        }
}