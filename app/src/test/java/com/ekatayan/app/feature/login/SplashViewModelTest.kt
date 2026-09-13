package com.ekatayan.app.feature.login

import com.ekatayan.app.data.repository.AuthRepository
import com.ekatayan.app.viewmodel.SplashViewModel
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SplashViewModelTest {
    @Test fun authenticatedSessionIsResolvedBehindSplash() = runTest {
        assertTrue(SplashViewModel(SplashAuthRepository(true)).restoreSession())
    }

    @Test fun missingSessionRoutesToAuthentication() = runTest {
        assertFalse(SplashViewModel(SplashAuthRepository(false)).restoreSession())
    }
}

private class SplashAuthRepository(private val restored: Boolean) : AuthRepository {
    override suspend fun signIn(email: String, password: String) = Unit
    override suspend fun signUp(name: String, email: String, phone: String, password: String) = error("Not used")
    override suspend fun restoreSession() = restored
    override suspend fun refreshSession() = false
    override fun clearSession() = Unit
}
