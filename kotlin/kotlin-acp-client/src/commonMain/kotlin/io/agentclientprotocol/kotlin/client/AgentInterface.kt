@file:Suppress("unused")

package io.agentclientprotocol.kotlin.client

import io.agentclientprotocol.kotlin.*

/**
 * Interface representing an agent connection from the client's perspective.
 *
 * This interface provides methods for clients to communicate with agents,
 * covering the full agent lifecycle from initialization through
 * session management and prompt processing.
 *
 * See protocol docs: [Agent](https://agentclientprotocol.com/protocol/overview#agent)
 */
public interface AgentInterface {
    /**
     * Initialize the agent with client capabilities and protocol version.
     *
     * This is the first method called when connecting to an agent.
     * The client should specify its capabilities and supported protocol version.
     *
     * See protocol docs: [Initialization](https://agentclientprotocol.com/protocol/initialization)
     */
    public suspend fun initialize(request: InitializeRequest): InitializeResponse

    /**
     * Authenticate with the agent using a specific method.
     *
     * Called after initialization if the agent requires authentication.
     * The method ID must be one of those advertised by the agent.
     *
     * @param request The authentication request containing the method ID
     */
    public suspend fun authenticate(request: AuthenticateRequest)

    /**
     * Create a new conversation session.
     *
     * Sessions allow multiple independent conversations with the same agent.
     * Each session maintains its own context and history.
     *
     * See protocol docs: [Creating a Session](https://agentclientprotocol.com/protocol/session-setup#creating-a-session)
     */
    public suspend fun newSession(request: NewSessionRequest): NewSessionResponse

    /**
     * Load an existing conversation session.
     *
     * Only available if the agent supports the `loadSession` capability.
     * Allows resuming previous conversations.
     *
     * See protocol docs: [Loading Sessions](https://agentclientprotocol.com/protocol/session-setup#loading-sessions)
     */
    public suspend fun loadSession(request: LoadSessionRequest)

    /**
     * Send a user prompt to the agent within a session.
     *
     * This is the main method for user interactions. The client sends
     * content blocks and receives real-time updates via session notifications.
     *
     * See protocol docs: [User Message](https://agentclientprotocol.com/protocol/prompt-turn#1-user-message)
     */
    public suspend fun prompt(request: PromptRequest): PromptResponse

    /**
     * Cancel ongoing operations for a session.
     *
     * Sends a notification to request cancellation of the current prompt turn.
     * The agent should stop processing and return a cancelled response.
     *
     * See protocol docs: [Cancellation](https://agentclientprotocol.com/protocol/prompt-turn#cancellation)
     */
    public suspend fun cancel(notification: CancelNotification)
}