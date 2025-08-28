@file:Suppress("unused")

package io.agentclientprotocol.kotlin

/**
 * ACP method names for agent-side operations.
 * 
 * These are methods that agents can call on clients.
 */
public object AgentMethods {
    public const val INITIALIZE: String = "initialize"
    public const val AUTHENTICATE: String = "authenticate"  
    public const val SESSION_NEW: String = "session/new"
    public const val SESSION_LOAD: String = "session/load"
    public const val SESSION_PROMPT: String = "session/prompt"
    public const val SESSION_CANCEL: String = "session/cancel"
}

/**
 * ACP method names for client-side operations.
 * 
 * These are methods that clients can call on agents.
 */
public object ClientMethods {
    public const val FS_READ_TEXT_FILE: String = "fs/read_text_file"
    public const val FS_WRITE_TEXT_FILE: String = "fs/write_text_file"
    public const val SESSION_REQUEST_PERMISSION: String = "session/request_permission"
    public const val SESSION_UPDATE: String = "session/update"
}