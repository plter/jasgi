package top.yunp.jasgi

import io.ktor.server.application.*
import org.graalvm.polyglot.Context
import org.graalvm.polyglot.Source
import org.graalvm.polyglot.Value
import org.slf4j.LoggerFactory
import top.yunp.jasgi.core.Project
import java.util.concurrent.CompletableFuture
import java.util.function.Function

private val LOG = LoggerFactory.getLogger("PythonRuntime")

fun Application.initPythonRuntime() {
    var context = attributes.getOrNull(ApplicationKeys.PYTHON_RUNTIME_CONTEXT)
    if (context == null) {
        context = Context.newBuilder(Constants.LANGUAGE_ID).allowAllAccess(true).allowCreateProcess(true).build()
        context.eval(
            Constants.LANGUAGE_ID,
            "import sys\n" +
                    "sys.path.append(f'${
                        Project.projectRoot.resolve(".venv").resolve("lib").absolutePath
                    }/python{sys.version_info.major}.{sys.version_info.minor}/site-packages')\n" +
                    "sys.path.append('${
                        Project.pythonAppRoot.absolutePath
                    }')\n"
        )
        context.eval(Source.newBuilder(Constants.LANGUAGE_ID, javaClass.getResource("/preload.py")).build())
        context.eval(Source.newBuilder(Constants.LANGUAGE_ID, Project.pythonAppRoot.resolve("main.py")).build())

        attributes[ApplicationKeys.PYTHON_RUNTIME_CONTEXT] = context
        val asgiApp = context.eval(Constants.LANGUAGE_ID, "application")
        attributes[ApplicationKeys.ASGI_APP] = asgiApp

        val completableToAwaitable = context.eval(Constants.LANGUAGE_ID, "completable_to_awaitable")
        attributes[ApplicationKeys.COMPLETABLE_TO_AWAITABLE] = completableToAwaitable

        attributes[ApplicationKeys.SYNC_TO_ASYNC] = context.eval(Constants.LANGUAGE_ID, "sync_to_async")
        attributes[ApplicationKeys.ASYNC_TO_SYNC] = context.eval(Constants.LANGUAGE_ID, "async_to_sync")
        attributes[ApplicationKeys.BYTES_J2P] = context.eval(Constants.LANGUAGE_ID, "bytesj2p")
        attributes[ApplicationKeys.STR_2_BSTR] = context.eval(Constants.LANGUAGE_ID, "str2bstr")
        attributes[ApplicationKeys.BSTR_2_STR] = context.eval(Constants.LANGUAGE_ID, "bstr2str")
        attributes[ApplicationKeys.WRAP_NO_ARG_JAVA_FUNCTION] = context.eval(
            Constants.LANGUAGE_ID, "wrap_no_arg_java_function"
        )

        LOG.info("Python runtime initialized")
    }
}

fun Application.completableToAwaitable(future: CompletableFuture<*>): Value {
    return attributes[ApplicationKeys.COMPLETABLE_TO_AWAITABLE].execute(future)
}

fun Application.wrapNoArgJavaFunction(f: Function<*, *>): Value {
    return attributes[ApplicationKeys.WRAP_NO_ARG_JAVA_FUNCTION].execute(f)
}

fun Application.str2bstr(s: String): Value {
    return attributes[ApplicationKeys.STR_2_BSTR].execute(s)
}

fun Application.bytesj2p(ba: ByteArray): Value {
    return attributes[ApplicationKeys.BYTES_J2P].execute(ba)
}

fun Application.bstr2str(bs: Any): String {
    return attributes[ApplicationKeys.BSTR_2_STR].execute(bs)?.asString() ?: ""
}

fun Application.bytesp2j(bytes: List<Int>): ByteArray {
    val r = ByteArray(bytes.size)
    bytes.forEachIndexed { i, byte ->
        r[i] = byte.toByte()
    }
    return r
}