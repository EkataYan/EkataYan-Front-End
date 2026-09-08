package com.ekatayan.app.data.repository

import com.ekatayan.app.data.model.BusinessDocument
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface DocumentRepository {
    suspend fun persistAndReadName(value: String): String?
    suspend fun readBusinessDocument(value: String): BusinessDocument?
}

class LocalDocumentRepository @Inject constructor(@param:ApplicationContext private val context: Context) : DocumentRepository {
    override suspend fun persistAndReadName(value: String): String? = withContext(Dispatchers.IO) {
        val uri = Uri.parse(value)
        val resolver = context.contentResolver
        try {
            resolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME)) else null
            }
        } catch (_: SecurityException) {
            null
        } catch (_: IllegalArgumentException) {
            null
        }
    }

    override suspend fun readBusinessDocument(value: String): BusinessDocument? = withContext(Dispatchers.IO) {
        val uri = Uri.parse(value)
        val resolver = context.contentResolver
        runCatching {
            runCatching { resolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
            resolver.openFileDescriptor(uri, "r")?.use { } ?: error("Unreadable document")
            val name = resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) cursor.getString(0) else null
            } ?: uri.lastPathSegment.orEmpty()
            BusinessDocument(value, name)
        }.getOrNull()
    }
}
