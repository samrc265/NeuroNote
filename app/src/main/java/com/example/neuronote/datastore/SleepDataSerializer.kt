package com.example.neuronote.datastore

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import java.io.InputStream
import java.io.OutputStream

object SleepDataSerializer : Serializer<SleepData> {
    override val defaultValue: SleepData = SleepData.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): SleepData {
        try {
            return SleepData.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(t: SleepData, output: OutputStream) {
        t.writeTo(output)
    }
}