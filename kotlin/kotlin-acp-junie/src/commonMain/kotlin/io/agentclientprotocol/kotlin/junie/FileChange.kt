@file:Suppress("unused")

package io.agentclientprotocol.kotlin.junie

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.nio.file.Path
import java.nio.file.Paths

@Serializable
sealed interface FileChangeContent

@Serializable
@SerialName("com.intellij.ml.llm.matterhorn.agent.BinaryFileChangeContent")
data class BinaryFileChangeContent(val data: ByteArray) : FileChangeContent {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        
        other as BinaryFileChangeContent
        
        return data.contentEquals(other.data)
    }
    
    override fun hashCode(): Int {
        return data.contentHashCode()
    }
}

@Serializable
@SerialName("com.intellij.ml.llm.matterhorn.agent.TextFileChangeContent")
data class TextFileChangeContent(val data: String) : FileChangeContent

@Serializable(with = FileChangeSerializer::class)
data class FileChange(
    val beforeContent: FileChangeContent?,
    val afterContent: FileChangeContent?,
    val beforeAbsolutePath: Path,
    val beforeRelativePath: Path,
    val afterAbsolutePath: Path = beforeAbsolutePath,
    val afterRelativePath: Path = beforeRelativePath,
) {
    fun isDeleted(): Boolean {
        return afterContent == null
    }

    fun isCreated(): Boolean {
        return beforeContent == null
    }
}

object FileChangeSerializer : KSerializer<FileChange> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("FileChange") {
        /* 0*/ element("beforeAbsolutePath", PathSerializer.descriptor, isOptional = true)
        /* 1*/ element("beforeRelativePath", PathSerializer.descriptor, isOptional = true)
        /* 2*/ element("afterAbsolutePath", PathSerializer.descriptor, isOptional = true)
        /* 3*/ element("afterRelativePath", PathSerializer.descriptor, isOptional = true)
        /* 4*/ element("beforeContent", FileChangeContent.serializer().nullable.descriptor, isOptional = true)
        /* 5*/ element("afterContent", FileChangeContent.serializer().nullable.descriptor, isOptional = true)

        //Obsolete fields
        /* 6*/ element("relativePath", PathSerializer.descriptor, isOptional = true)
        /* 7*/ element("absolutePath", PathSerializer.descriptor, isOptional = true)
        /* 8*/ element("path", PathSerializer.descriptor, isOptional = true)
        /* 9*/ element("initialContent", String.serializer().nullable.descriptor, isOptional = true)
        /*10*/ element("newContent", String.serializer().nullable.descriptor, isOptional = true)
        /*11*/ element("linesAdded", Int.serializer().descriptor, isOptional = true)
        /*12*/ element("linesRemoved", Int.serializer().descriptor, isOptional = true)
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun serialize(encoder: Encoder, value: FileChange) {
        val compositeEncoder = encoder.beginStructure(descriptor)
        compositeEncoder.encodeSerializableElement(descriptor, 0, PathSerializer, value.beforeAbsolutePath)
        compositeEncoder.encodeSerializableElement(descriptor, 1, PathSerializer, value.beforeRelativePath)
        compositeEncoder.encodeSerializableElement(descriptor, 2, PathSerializer, value.afterAbsolutePath)
        compositeEncoder.encodeSerializableElement(descriptor, 3, PathSerializer, value.afterRelativePath)
        compositeEncoder.encodeSerializableElement(descriptor, 4, FileChangeContent.serializer().nullable, value.beforeContent)
        compositeEncoder.encodeSerializableElement(descriptor, 5, FileChangeContent.serializer().nullable, value.afterContent)
        compositeEncoder.endStructure(descriptor)
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun deserialize(decoder: Decoder): FileChange {
        val compositeDecoder = decoder.beginStructure(descriptor)
        var beforeContent: FileChangeContent? = null
        var afterContent: FileChangeContent? = null
        var beforeAbsolutePath: Path? = null
        var beforeRelativePath: Path? = null
        var afterAbsolutePath: Path? = null
        var afterRelativePath: Path? = null

        loop@ while (true) {
            when (val index = compositeDecoder.decodeElementIndex(descriptor)) {
                CompositeDecoder.DECODE_DONE -> break@loop
                0 -> beforeAbsolutePath = compositeDecoder.decodeSerializableElement(descriptor, 0, PathSerializer)
                1 -> beforeRelativePath = compositeDecoder.decodeSerializableElement(descriptor, 1, PathSerializer)
                2 -> afterAbsolutePath = compositeDecoder.decodeSerializableElement(descriptor, 2, PathSerializer)
                3 -> afterRelativePath = compositeDecoder.decodeSerializableElement(descriptor, 3, PathSerializer)
                4 -> beforeContent = compositeDecoder.decodeNullableSerializableElement(descriptor, 4, FileChangeContent.serializer())
                5 -> afterContent = compositeDecoder.decodeNullableSerializableElement(descriptor, 5, FileChangeContent.serializer())

                //Obsolete fields
                6 /*relativePath*/ -> {
                    val relativePath = compositeDecoder.decodeSerializableElement(descriptor, 6, PathSerializer)
                    afterRelativePath = relativePath
                    beforeRelativePath = relativePath
                }
                7 /*absolutePath*/ -> {
                    val absolutePath = compositeDecoder.decodeSerializableElement(descriptor, 6, PathSerializer)
                    afterAbsolutePath = absolutePath
                    beforeAbsolutePath = absolutePath
                }
                8 /*path*/-> {
                    val path = compositeDecoder.decodeSerializableElement(descriptor, 7, PathSerializer)
                    val absolutePath = if (!path.isAbsolute)
                        Paths.get("/").resolve(path)
                    else path

                    afterRelativePath = path
                    beforeRelativePath = path
                    afterAbsolutePath = absolutePath
                    beforeAbsolutePath = absolutePath
                }
                9 /*initialContent*/ -> beforeContent = compositeDecoder.decodeNullableSerializableElement(descriptor, 8, String.serializer())?.let { TextFileChangeContent(it) }
                10 /*newContent*/ -> afterContent = compositeDecoder.decodeNullableSerializableElement(descriptor, 9, String.serializer())?.let { TextFileChangeContent(it) }
                11 -> /*linesAdded*/ compositeDecoder.decodeSerializableElement(descriptor, 10, Int.serializer())
                12 -> /*linesRemoved*/ compositeDecoder.decodeSerializableElement(descriptor, 11, Int.serializer())
                else -> throw SerializationException("Unexpected index $index")
            }
        }

        compositeDecoder.endStructure(descriptor)

        requireNotNull(beforeRelativePath) { "beforeRelativePath must not be null" }
        requireNotNull(afterRelativePath) { "afterRelativePath must not be null" }
        requireNotNull(beforeAbsolutePath) { "beforeAbsolutePath must not be null" }
        requireNotNull(afterAbsolutePath) { "afterAbsolutePath must not be null" }

        return FileChange(beforeContent, afterContent, beforeAbsolutePath, beforeRelativePath, afterAbsolutePath, afterRelativePath)
    }
}