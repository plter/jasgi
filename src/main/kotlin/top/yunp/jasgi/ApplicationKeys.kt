package top.yunp.jasgi

import io.ktor.util.AttributeKey
import org.graalvm.polyglot.Context
import org.graalvm.polyglot.Value

object ApplicationKeys {
    val PYTHON_RUNTIME_CONTEXT = AttributeKey<Context>("PYTHON_RUNTIME_CONTEXT")
    val ASGI_APP = AttributeKey<Value>("ASGI_APP")
    val COMPLETABLE_TO_AWAITABLE = AttributeKey<Value>("COMPLETABLE_TO_AWAITABLE")
    val SYNC_TO_ASYNC = AttributeKey<Value>("SYNC_TO_ASYNC")
    val ASYNC_TO_SYNC = AttributeKey<Value>("ASYNC_TO_SYNC")
    val STR_2_BSTR = AttributeKey<Value>("STR_2_BSTR")
    val BSTR_2_STR = AttributeKey<Value>("BSTR_2_STR")
    val BYTES_J2P = AttributeKey<Value>("BYTES_J2P")
    val WRAP_NO_ARG_JAVA_FUNCTION = AttributeKey<Value>("wrap_no_arg_java_function")
}