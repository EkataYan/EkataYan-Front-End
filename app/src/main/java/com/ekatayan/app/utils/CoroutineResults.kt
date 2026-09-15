package com.ekatayan.app.utils

import kotlinx.coroutines.CancellationException

suspend inline fun <T> runSuspendCatching(block: () -> T): Result<T> = try {
    Result.success(block())
} catch (cancelled: CancellationException) {
    throw cancelled
} catch (error: Throwable) {
    Result.failure(error)
}
