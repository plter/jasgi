package top.yunp.jasgi

import io.ktor.util.AttributeKey
import org.graalvm.polyglot.Context
import org.graalvm.polyglot.Value

object ApplicationKeys {
    val PYTHON_RUNTIME_CONTEXT = AttributeKey<Context>("PYTHON_RUNTIME_CONTEXT")
    val ASGI_APP = AttributeKey<Value>("ASGI_APP")
    val PYTHON_MAIN_EVENT_LOOP = AttributeKey<Value>("PYTHON_MAIN_EVENT_LOOP")
    val COMPLETABLE_TO_AWAITABLE = AttributeKey<Value>("COMPLETABLE_TO_AWAITABLE")
}