package com.ekatayan.app

import com.ekatayan.app.data.local.ProfileImageStore
import java.io.InputStream
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class TestProfileImageStore : ProfileImageStore {
    var localPath = "C:/test/profile.jpg"
    var hasLocalImage = true

    override fun importFromPicker(uriValue: String, identity: String) = localPath
    override fun saveDownloaded(input: InputStream, identity: String) = localPath
    override fun exists(path: String?) = hasLocalImage
    override fun multipart(path: String) = MultipartBody.Part.createFormData(
        "file",
        "profile.jpg",
        "image".toRequestBody("image/jpeg".toMediaType()),
    )
}
