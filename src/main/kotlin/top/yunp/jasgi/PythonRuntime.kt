package top.yunp.jasgi

import io.ktor.server.application.Application
import org.graalvm.nativeimage.hosted.Feature
import org.graalvm.polyglot.Context
import org.graalvm.polyglot.Source
import org.graalvm.polyglot.Value
import org.slf4j.LoggerFactory
import java.io.File
import java.util.concurrent.CompletableFuture

private val LOG = LoggerFactory.getLogger("PythonRuntime")

fun Application.initPythonRuntime() {
    var context = attributes.getOrNull(ApplicationKeys.PYTHON_RUNTIME_CONTEXT)
    if (context == null) {
        context = Context.newBuilder(Constants.LANGUAGE_ID).allowAllAccess(true).allowCreateProcess(true).build()
        context.eval(Source.newBuilder(Constants.LANGUAGE_ID, File("pyweb/main.py")).build())
        context.eval(Source.newBuilder(Constants.LANGUAGE_ID, javaClass.getResource("/preload.py")).build())

        attributes[ApplicationKeys.PYTHON_RUNTIME_CONTEXT] = context
        val asgiApp = context.eval(Constants.LANGUAGE_ID, "application")
        attributes[ApplicationKeys.ASGI_APP] = asgiApp
        val eventLoop = context.eval(Constants.LANGUAGE_ID, "main_event_loop")
        attributes[ApplicationKeys.PYTHON_MAIN_EVENT_LOOP] = eventLoop

        val completableToAwaitable = context.eval(Constants.LANGUAGE_ID, "completable_to_awaitable")
        attributes[ApplicationKeys.COMPLETABLE_TO_AWAITABLE] = completableToAwaitable

        LOG.info("Python runtime initialized")
    }
}

fun Application.getASGIApplication(): Value {
    return attributes[ApplicationKeys.ASGI_APP]
}

fun Application.getPythonMainEventLoop(): Value {
    return attributes[ApplicationKeys.PYTHON_MAIN_EVENT_LOOP]
}

fun Application.completableToAwaitable(future: CompletableFuture<*>): Value {
    return attributes[ApplicationKeys.COMPLETABLE_TO_AWAITABLE].execute(future)
}