package com.ekatayan.app.data.local

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.ekatayan.app.data.model.ProfileDetails
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

data class CachedProfile(
    val profile: ProfileDetails,
    val pendingFields: Set<String> = emptySet(),
)

interface ProfileCacheStore {
    fun read(identity: String): CachedProfile?
    fun save(identity: String, value: CachedProfile)
    fun clearMemoryForOtherUser(identity: String)
}

/** Durable, account-scoped profile cache. Cloud data remains authoritative after synchronization. */
@Singleton
class SharedPreferencesProfileCacheStore @Inject constructor(
    @ApplicationContext context: Context,
) : ProfileCacheStore {
    private val preferences = EncryptedSharedPreferences.create(
        context,
        "ekatayan_profile_cache",
        MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    override fun read(identity: String): CachedProfile? {
        if (preferences.getString(OWNER, null) != identity || !preferences.contains(NAME)) return null
        return CachedProfile(
            profile = ProfileDetails(
                name = preferences.getString(NAME, "").orEmpty(),
                location = preferences.getString(HOME_CITY, "").orEmpty(),
                email = preferences.getString(EMAIL, "").orEmpty(),
                phone = preferences.getString(PHONE, "").orEmpty(),
                bio = preferences.getString(BIO, "").orEmpty(),
                language = preferences.getString(LANGUAGE, "en").orEmpty(),
                interests = preferences.getStringSet(INTERESTS, emptySet()).orEmpty().toList().sorted(),
                avatarPath = preferences.getString(AVATAR_PATH, null),
                avatarLocalPath = preferences.getString(AVATAR_LOCAL_PATH, null),
            ),
            pendingFields = preferences.getStringSet(PENDING_FIELDS, emptySet()).orEmpty().toSet(),
        )
    }

    override fun save(identity: String, value: CachedProfile) {
        val profile = value.profile
        preferences.edit()
            .putString(OWNER, identity)
            .putString(NAME, profile.name)
            .putString(EMAIL, profile.email)
            .putString(PHONE, profile.phone)
            .putString(BIO, profile.bio)
            .putString(HOME_CITY, profile.location)
            .putString(LANGUAGE, profile.language)
            .putStringSet(INTERESTS, profile.interests.toSet())
            .putString(AVATAR_PATH, profile.avatarPath)
            .putString(AVATAR_LOCAL_PATH, profile.avatarLocalPath)
            .putStringSet(PENDING_FIELDS, value.pendingFields)
            .apply()
    }

    override fun clearMemoryForOtherUser(identity: String) {
        if (preferences.getString(OWNER, null) != identity) preferences.edit().clear().apply()
    }

    private companion object {
        const val OWNER = "owner"
        const val NAME = "display_name"
        const val EMAIL = "email"
        const val PHONE = "phone"
        const val BIO = "bio"
        const val HOME_CITY = "home_city"
        const val LANGUAGE = "language"
        const val INTERESTS = "interests"
        const val AVATAR_PATH = "avatar_path"
        const val AVATAR_LOCAL_PATH = "avatar_local_path"
        const val PENDING_FIELDS = "pending_fields"
    }
}

class MemoryProfileCacheStore : ProfileCacheStore {
    private var owner: String? = null
    private var value: CachedProfile? = null
    override fun read(identity: String) = value?.takeIf { owner == identity }
    override fun save(identity: String, value: CachedProfile) { owner = identity; this.value = value }
    override fun clearMemoryForOtherUser(identity: String) { if (owner != identity) value = null }
}
