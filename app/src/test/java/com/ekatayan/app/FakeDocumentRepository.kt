package com.ekatayan.app

import com.ekatayan.app.data.model.BusinessDocument
import com.ekatayan.app.data.repository.DocumentRepository

class FakeDocumentRepository : DocumentRepository {
    override suspend fun persistAndReadName(value: String): String? = null
    override suspend fun readBusinessDocument(value: String): BusinessDocument? = null
}
