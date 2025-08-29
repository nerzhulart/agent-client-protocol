package io.agentclientprotocol.kotlin.client

import io.agentclientprotocol.kotlin.*

class MockClient : Client {
    var readTextFileResult: ReadTextFileResponse = ReadTextFileResponse("test content")
    var requestPermissionResult: RequestPermissionResponse = RequestPermissionResponse(
        RequestPermissionOutcome.Selected(PermissionOptionId("allow"))
    )
    
    val readTextFileCalls = mutableListOf<ReadTextFileRequest>()
    val writeTextFileCalls = mutableListOf<WriteTextFileRequest>()
    val requestPermissionCalls = mutableListOf<RequestPermissionRequest>()
    val sessionUpdateCalls = mutableListOf<SessionNotification>()

    override suspend fun readTextFile(request: ReadTextFileRequest): ReadTextFileResponse {
        readTextFileCalls.add(request)
        return readTextFileResult
    }

    override suspend fun writeTextFile(request: WriteTextFileRequest) {
        writeTextFileCalls.add(request)
    }

    override suspend fun requestPermission(request: RequestPermissionRequest): RequestPermissionResponse {
        requestPermissionCalls.add(request)
        return requestPermissionResult
    }

    override suspend fun sessionUpdate(notification: SessionNotification) {
        sessionUpdateCalls.add(notification)
    }

    fun reset() {
        readTextFileCalls.clear()
        writeTextFileCalls.clear()
        requestPermissionCalls.clear()
        sessionUpdateCalls.clear()
    }
}