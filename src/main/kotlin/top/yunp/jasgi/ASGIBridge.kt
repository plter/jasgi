package top.yunp.jasgi

import io.ktor.http.HttpStatusCode
import io.ktor.http.headers
import io.ktor.server.request.httpMethod
import io.ktor.server.request.queryString
import io.ktor.server.request.receive
import io.ktor.server.request.uri
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import kotlinx.coroutines.future.asCompletableFuture
import kotlinx.coroutines.launch
import org.graalvm.polyglot.Value
import org.slf4j.LoggerFactory
import top.yunp.jasgi.async.IOLauncher
import top.yunp.jasgi.async.fjpCoroutine
import top.yunp.jasgi.async.vtCoroutine
import java.util.concurrent.CompletableFuture
import java.util.function.Function

class ASGIBridge(private val context: RoutingContext) {

    companion object {
        val LOG = LoggerFactory.getLogger(ASGIBridge::class.java)
    }


    suspend fun handle() {
        vtCoroutine {
            val disconnectCompletableFuture = CompletableFuture<Any>()
            val asyncToSync = context.call.application.attributes[ApplicationKeys.ASYNC_TO_SYNC]
            val app = context.call.application.attributes[ApplicationKeys.ASGI_APP]
            val f = asyncToSync.execute(app)
            var dataSent = false

            val request = context.call.request
            var queryString = ""
            val path = request.uri.let {
                val qIndex = it.indexOf('?')
                if (qIndex == -1) {
                    return@let it
                } else {
                    queryString = it.substring(qIndex + 1)
                    return@let it.substring(0, qIndex)
                }
            }

            val headers = mutableListOf<List<Value>>()
            request.headers.forEach { key, values ->
                values.forEach { value ->
                    headers.add(
                        listOf(
                            context.call.application.str2bstr(key),
                            context.call.application.str2bstr(value)
                        )
                    )
                }
            }

            var status = 200

            f.execute(
                mapOf(
                    "type" to "http",
                    "asgi" to mapOf(
                        "version" to "3.0"
                    ),
                    "http_version" to "1.0",
                    "method" to (request.httpMethod.toString()),
                    "path" to path,
                    "raw_path" to context.call.application.str2bstr(path),
                    "query_string" to context.call.application.str2bstr(queryString),
                    "headers" to headers,
                ), context.call.application.wrapNoArgJavaFunction {
                    if (!dataSent) {
                        dataSent = true
                        val cf = CompletableFuture<Any>()
                        IOLauncher.default.launch {
                            val bytes = context.call.receive<ByteArray>()
                            cf.complete(
                                mapOf(
                                    "type" to "http.request",
                                    "more_body" to false,
                                    "body" to context.call.application.bytesj2p(bytes)
                                )
                            )
                        }
                        return@wrapNoArgJavaFunction context.call.application.completableToAwaitable(cf)
                    } else {
                        return@wrapNoArgJavaFunction context.call.application.completableToAwaitable(
                            disconnectCompletableFuture
                        )
                    }
                }, object : Function<Map<String, Any>, Value> {
                    override fun apply(t: Map<String, Any>): Value {
                        LOG.info("Send: $t")
                        if (t["type"] == "http.response.start") {
                            status = t["status"] as? Int ?: 200
                            val headers = t["headers"] as? List<List<Any>>

                            headers?.forEach { header ->
                                val key = context.call.application.bstr2str(header[0])
                                val value = context.call.application.bstr2str(header[1])
                                if (key.equals("content-length", ignoreCase = true)) {
                                    return@forEach
                                }
                                context.call.response.header(key, value)
                            }

                            return context.call.application.completableToAwaitable(
                                CompletableFuture.completedFuture(
                                    Unit
                                )
                            )
                        } else if (t["type"] == "http.response.body") {
                            val body = t["body"] as List<Int>
                            val bodyBytes = context.call.application.bytesp2j(body)

                            return context.call.application.completableToAwaitable(IOLauncher.default.launch {
                                context.call.respond(HttpStatusCode.fromValue(status), bodyBytes)
                            }.asCompletableFuture())
                        }
                        return context.call.application.completableToAwaitable(CompletableFuture.completedFuture(Unit))
                    }
                }
            )

            disconnectCompletableFuture.complete(mapOf("type" to "http.disconnect"))
        }
    }
}