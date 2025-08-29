@file:Suppress("unused")

package io.agentclientprotocol.kotlin.junie

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import java.nio.file.Path

object PathListSerializer : KSerializer<List<Path>> by ListSerializer(PathSerializer)