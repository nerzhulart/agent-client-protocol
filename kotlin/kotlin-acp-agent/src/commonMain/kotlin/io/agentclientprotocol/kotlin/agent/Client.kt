@file:Suppress("unused")

package io.agentclientprotocol.kotlin.agent

import io.agentclientprotocol.kotlin.*

/**
 * Interface representing a client connection from the agent's perspective.
 *
 * This interface provides methods for agents to communicate back to clients,
 * including sending session updates, requesting permissions, and accessing
 * the client's file system (if supported).
 *
 * See protocol docs: [Client](https://agentclientprotocol.com/protocol/overview#client)
 */
public interface Client {
    /**
     * Send a session update notification to the client.
     *
     * Used to stream real-time progress and results during prompt processing.
     * This includes message chunks, tool calls, and execution plans.
     *
     * See protocol docs: [Agent Reports Output](https://agentclientprotocol.com/protocol/prompt-turn#3-agent-reports-output)
     */
    public suspend fun sessionUpdate(notification: SessionNotification)

    /**
     * Request permission from the user for a tool call operation.
     *
     * Called when the agent needs user authorization before executing
     * a potentially sensitive operation.
     *
     * See protocol docs: [Requesting Permission](https://agentclientprotocol.com/protocol/tool-calls#requesting-permission)
     */
    public suspend fun requestPermission(request: RequestPermissionRequest): RequestPermissionResponse

    /**
     * Read content from a text file in the client's file system.
     *
     * Only available if the client advertises the `fs.readTextFile` capability.
     *
     * See protocol docs: [Client](https://agentclientprotocol.com/protocol/overview#client)
     */
    public suspend fun readTextFile(request: ReadTextFileRequest): ReadTextFileResponse

    /**
     * Write content to a text file in the client's file system.
     *
     * Only available if the client advertises the `fs.writeTextFile` capability.
     *
     * See protocol docs: [Client](https://agentclientprotocol.com/protocol/overview#client)
     */
    public suspend fun writeTextFile(request: WriteTextFileRequest)
}