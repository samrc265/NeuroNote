package com.example.neuronote.datastore

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import java.io.InputStream
import java.io.OutputStream

object MoodDataSerializer : Serializer<MoodData> {
    override val defaultValue: MoodData = MoodData.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): MoodData {
        try {
            return MoodData.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(t: MoodData, output: OutputStream) {
        t.writeTo(output)
    }
}