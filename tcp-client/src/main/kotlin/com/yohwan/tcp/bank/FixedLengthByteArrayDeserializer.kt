package com.yohwan.tcp.bank

import org.springframework.integration.ip.tcp.serializer.AbstractByteArraySerializer
import java.io.InputStream
import java.io.OutputStream

class FixedLengthByteArrayDeserializer(private val length: Int) : AbstractByteArraySerializer() {

    override fun deserialize(inputStream: InputStream): ByteArray {
        val buf = ByteArray(length)
        var readBytes = 0
        while (readBytes < length) {
            val n = inputStream.read(buf, readBytes, length - readBytes)
            if (n == -1) throw RuntimeException("Stream closed before reading full message")
            readBytes += n
        }
        return buf
    }

    override fun serialize(bytes: ByteArray, outputStream: OutputStream) {
        outputStream.write(bytes)
    }
}