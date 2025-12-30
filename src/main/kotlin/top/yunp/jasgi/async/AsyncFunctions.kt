package top.yunp.jasgi.async

import java.util.concurrent.CompletableFuture
import java.util.concurrent.ForkJoinPool
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

suspend fun <R> fjpCoroutine(runner: () -> R): R {
    return suspendCoroutine { continuation ->
        ForkJoinPool.commonPool().submit {
            try {
                continuation.resume(runner())
            } catch (e: Throwable) {
                continuation.resumeWithException(e)
            }
        }
    }
}

suspend fun <R> vtCoroutine(runner: () -> R): R {
    return suspendCoroutine { continuation ->
        Thread.ofVirtual().start {
            try {
                continuation.resume(runner())
            } catch (e: Throwable) {
                continuation.resumeWithException(e)
            }
        }
    }
}

suspend fun <R> cfCoroutine(cf: CompletableFuture<R>): R {
    return suspendCoroutine { continuation ->
        cf.thenAccept {
            continuation.resume(it)
        }
        cf.exceptionally {
            continuation.resumeWithException(it)
            return@exceptionally null
        }
    }
}