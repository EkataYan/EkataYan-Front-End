package com.ekatayan.app.data.remote

import com.ekatayan.app.data.repository.AuthRepository
import com.ekatayan.app.data.repository.SignUpResult
import dagger.Lazy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SessionRefreshCoordinatorTest {
    @Test fun expiredSessionRefreshesBeforeAnAuthenticatedRequest() {
        val store = TestSessionStore()
        val session = UserSessionProvider(store)
        session.setSession("expired", "refresh", 1)
        var refreshes = 0
        val auth = object : AuthRepository {
            override suspend fun signIn(email: String, password: String) = Unit
            override suspend fun signUp(name: String, email: String, phone: String, password: String) = SignUpResult.AUTHENTICATED
            override suspend fun restoreSession() = true
            override suspend fun refreshSession(): Boolean {
                refreshes++
                session.setSession("replacement", "next-refresh", Long.MAX_VALUE)
                return true
            }
            override fun clearSession() = session.clearSession()
        }

        val token = SessionRefreshCoordinator(session, Lazy { auth }).tokenForRequest()

        assertEquals("replacement", token)
        assertEquals(1, refreshes)
    }

    @Test fun missingRefreshTokenFailsWithoutInventingAuthentication() {
        val session = UserSessionProvider(TestSessionStore())
        val auth = object : AuthRepository {
            override suspend fun signIn(email: String, password: String) = Unit
            override suspend fun signUp(name: String, email: String, phone: String, password: String) = SignUpResult.AUTHENTICATED
            override suspend fun restoreSession() = false
            override suspend fun refreshSession() = false
            override fun clearSession() = Unit
        }

        assertNull(SessionRefreshCoordinator(session, Lazy { auth }).tokenForRequest())
    }
}

private class TestSessionStore : SessionStore {
    private var session: StoredSession? = null
    override fun read() = session
    override fun save(session: StoredSession) { this.session = session }
    override fun clear() { session = null }
}
