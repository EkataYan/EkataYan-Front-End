package com.ekatayan.app.data.local

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody

class InvalidProfileImageException : Exception()

interface ProfileImageStore {
    fun importFromPicker(uriValue: String, identity: String): String
    fun saveDownloaded(input: InputStream, identity: String): String
    fun exists(path: String?): Boolean
    fun multipart(path: String): MultipartBody.Part
}

@Singleton
class AndroidProfileImageStore @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : ProfileImageStore {
    override fun importFromPicker(uriValue: String, identity: String): String {
        val uri = runCatching { Uri.parse(uriValue) }.getOrNull() ?: throw InvalidProfileImageException()
        val bitmap = context.contentResolver.openInputStream(uri)?.use(::decodeScaled)
            ?: throw InvalidProfileImageException()
        return saveBitmap(bitmap, identity)
    }

    override fun saveDownloaded(input: InputStream, identity: String): String {
        val bitmap = decodeScaled(input)
        return saveBitmap(bitmap, identity)
    }

    override fun exists(path: String?): Boolean = path?.let(::File)?.isFile == true

    override fun multipart(path: String): MultipartBody.Part {
        val file = File(path).takeIf(File::isFile) ?: throw InvalidProfileImageException()
        return MultipartBody.Part.createFormData(
            "file",
            "profile.jpg",
            file.asRequestBody("image/jpeg".toMediaType()),
        )
    }

    private fun decodeScaled(input: InputStream): Bitmap {
        val bytes = input.readBounded()
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) throw InvalidProfileImageException()

        var sampleSize = 1
        while (bounds.outWidth / sampleSize > MAX_DIMENSION * 2 || bounds.outHeight / sampleSize > MAX_DIMENSION * 2) {
            sampleSize *= 2
        }
        val decoded = BitmapFactory.decodeByteArray(
            bytes,
            0,
            bytes.size,
            BitmapFactory.Options().apply { inSampleSize = sampleSize },
        ) ?: throw InvalidProfileImageException()
        val longestSide = maxOf(decoded.width, decoded.height)
        if (longestSide <= MAX_DIMENSION) return decoded

        val scale = MAX_DIMENSION.toFloat() / longestSide
        return Bitmap.createScaledBitmap(
            decoded,
            (decoded.width * scale).toInt().coerceAtLeast(1),
            (decoded.height * scale).toInt().coerceAtLeast(1),
            true,
        ).also { if (it !== decoded) decoded.recycle() }
    }

    private fun saveBitmap(bitmap: Bitmap, identity: String): String {
        val encoded = ByteArrayOutputStream().use { output ->
            if (!bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, output)) {
                throw InvalidProfileImageException()
            }
            output.toByteArray()
        }
        bitmap.recycle()
        if (encoded.isEmpty() || encoded.size > MAX_FILE_BYTES) throw InvalidProfileImageException()

        val directory = File(context.filesDir, "profile_images").apply {
            if (!exists() && !mkdirs()) throw InvalidProfileImageException()
        }
        val accountKey = MessageDigest.getInstance("SHA-256")
            .digest(identity.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
            .take(24)
        val destination = File(directory, "$accountKey.jpg")
        val temporary = File(directory, "$accountKey.tmp")
        temporary.outputStream().use { it.write(encoded) }
        if (!temporary.renameTo(destination)) {
            temporary.copyTo(destination, overwrite = true)
            temporary.delete()
        }
        return destination.absolutePath
    }

    private fun InputStream.readBounded(): ByteArray {
        val output = ByteArrayOutputStream()
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var total = 0
        while (true) {
            val count = read(buffer)
            if (count < 0) break
            total += count
            if (total > MAX_FILE_BYTES) throw InvalidProfileImageException()
            output.write(buffer, 0, count)
        }
        return output.toByteArray()
    }

    private companion object {
        const val MAX_DIMENSION = 768
        const val MAX_FILE_BYTES = 5 * 1024 * 1024
        const val JPEG_QUALITY = 88
    }
}
