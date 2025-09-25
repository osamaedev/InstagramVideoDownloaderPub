package com.instagram.video.downloader.common

import android.content.Context
import android.os.Parcelable
import java.io.File
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import kotlin.reflect.KClass
import kotlin.reflect.safeCast

class DataReference<Payload : Serializable>(
    private val directoryName: String = "temp",
    private val sessionId: String
) : Serializable {

    private fun directory(context: Context) =
        context.applicationContext.getDir(directoryName, Context.MODE_PRIVATE)

    private fun sessionIdFile(context: Context) = File(directory(context), "sessionId")
    private fun payloadFile(context: Context) = File(directory(context), "payload")

    /**
     * if the [sessionId] is valid, reads the [Payload] from persistent memory, and returns it to
     * the caller, or null, if no existing [Payload] exists; otherwise, if [sessionId] is invalid,
     * the [Payload] is deleted from persistent memory (if any exists), and null is returned to the
     * caller.
     */
    fun load(context: Context, payloadKClass: KClass<Payload>): Payload? {
        return if (sessionId == sessionIdFile(context).readObject(String::class)) {
            payloadFile(context).readObject(payloadKClass)
        } else {
            sessionIdFile(context).deleteFileIfExists()
            payloadFile(context).deleteFileIfExists()
            null
        }
    }

    /**
     * overwrites any existing [Payload] with the new [payload], which is only accessible from
     * [load] when the [sessionId] passed into [load] matches the [sessionId] passed into this call
     * to [save].
     */
    fun save(context: Context, payload: Payload) {

        // delete files if they exist
        sessionIdFile(context).deleteFileIfExists()
        payloadFile(context).deleteFileIfExists()

        // write the payload & session id to the file
        ObjectOutputStream(sessionIdFile(context).outputStream()).use { oos ->
            oos.writeObject(sessionId)
            oos.flush()
        }
        ObjectOutputStream(payloadFile(context).outputStream()).use { oos ->
            oos.writeObject(payload)
            oos.flush()
        }
    }

    private fun File.deleteFileIfExists(): Boolean {
        isFile && delete()
        return !exists()
    }

    private fun <T : Any> File.readObject(tKClass: KClass<T>): T? = if (isFile) {
        ObjectInputStream(inputStream()).use { ois ->
            tKClass.safeCast(ois.readObject())
        }
    } else {
        null
    }
}