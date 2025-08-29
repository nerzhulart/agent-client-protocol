@file:Suppress("unused")

package io.agentclientprotocol.kotlin.junie

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator
import java.util.UUID

/**
 * Event representing a request for user input.
 */
@Serializable
@SerialName("inputRequest")
data class InputRequestEvent(
    val request: InputRequest,
    override val timestamp: Long = System.currentTimeMillis(),
    override val level: EventLevel = EventLevel.INFO
) : AgentEvent

@Serializable
@SerialName("inputResponse")
data class InputResponseEventComplete(
    val response: InputResponse,
    override val timestamp: Long = System.currentTimeMillis(),
    override val level: EventLevel = EventLevel.INFO
) : AgentEvent

/**
 * Event representing a request for user input.
 */
@Serializable
@SerialName("inputRequestCancelled")
data class InputRequestCancelled(
    val requestId: String,
    override val timestamp: Long = System.currentTimeMillis(),
    override val level: EventLevel = EventLevel.INFO
) : AgentEvent

/**
 * Base class for input requests.
 */
@Serializable
@JsonClassDiscriminator("method")
@OptIn(ExperimentalSerializationApi::class)
sealed interface InputRequest {
    val id: String
}

/**
 * Base class for input responses.
 */
@Serializable
@JsonClassDiscriminator("method")
@OptIn(ExperimentalSerializationApi::class)
sealed interface InputResponse {
    val requestId: String
}

/**
 * Base class for approval input request data.
 */
@Serializable
@JsonClassDiscriminator("method")
@OptIn(ExperimentalSerializationApi::class)
sealed interface ApproveInputRequestData

/**
 * Request for choosing from a set of options.
 */
@Serializable
@SerialName("choice")
data class ChoiceInputRequest(
    override val id: String = UUID.randomUUID().toString(),
    val options: List<ChoiceOption>
) : InputRequest

@Serializable
@SerialName("choice")
data class ChoiceInputResponse(
    override val requestId: String,
    val optionId: String,
) : InputResponse

@Serializable
@SerialName("cancel")
data class CancelInputRequest(
    override val id: String = UUID.randomUUID().toString(),
) : InputRequest

@Serializable
@SerialName("cancel")
data class CancelInputResponse(
    override val requestId: String,
) : InputResponse

@Serializable
@SerialName("approveTouchFileData")
data class ApproveTouchFileData(
    val path: String,
) : ApproveInputRequestData

@Serializable
@SerialName("approveEditFileData")
data class ApproveEditFileData(
    val absolutePath: String,
    val fileChange: FileChange?,
) : ApproveInputRequestData

@Serializable
@SerialName("approve")
data class ApproveInputRequest(
    override val id: String = UUID.randomUUID().toString(),
    val data: ApproveInputRequestData?,
) : InputRequest

@Serializable
@SerialName("approve")
data class ApproveInputResponse(
    override val requestId: String,
    val approve: Boolean
) : InputResponse

/**
 * Request for free-form input.
 */
@Serializable
@SerialName("ask")
data class AskInputRequest(
    override val id: String = UUID.randomUUID().toString(),
    val question: String,
) : InputRequest

@Serializable
@SerialName("ask")
data class AskInputResponse(
    override val requestId: String,
    val auto: Boolean? = null,
    val response: String? = null,
) : InputResponse

/**
 * Represents an option in a choice input request.
 */
@Serializable
data class ChoiceOption(
    val id: String,
    val description: String
)