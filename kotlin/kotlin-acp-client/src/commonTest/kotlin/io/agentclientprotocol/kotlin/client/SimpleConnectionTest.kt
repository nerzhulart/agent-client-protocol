package io.agentclientprotocol.kotlin.client

import io.agentclientprotocol.kotlin.*
import io.agentclientprotocol.kotlin.test.TestTransport
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runTest
import kotlin.test.*

class SimpleConnectionTest {
    @Test
    fun `test basic connection`() = runTest {
        // Given
        val mockClient = MockClient()
        val (clientTransport, agentTransport) = TestTransport.createPair()
        val clientConnection = ClientSideConnection(mockClient)
        
        // When
        clientConnection.connect(clientTransport)
        clientTransport.start()
        agentTransport.start()
        
        // Then
        assertTrue(clientTransport.isConnected)
        assertTrue(agentTransport.isConnected)
        
        // Cleanup
        clientTransport.close()
        agentTransport.close()
    }
}