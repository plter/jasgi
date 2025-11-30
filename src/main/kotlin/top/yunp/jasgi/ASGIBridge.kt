package top.yunp.jasgi

import io.ktor.server.routing.*
import org.graalvm.polyglot.Value
import top.yunp.jasgi.async.fjpCoroutine
import java.util.concurrent.CompletableFuture
import java.util.function.Function

class ASGIBridge(private val context: RoutingContext) {


    suspend fun handle() {
        fjpCoroutine {
            val loop = context.call.application.getPythonMainEventLoop()
            val app = context.call.application.getASGIApplication()
            val f = app.execute(mapOf("type" to "http"), null, object : Function<Map<String, Any>, Value> {
                override fun apply(t: Map<String, Any>): Value {
                    println(t)
                    println(t.javaClass)

                    return context.call.application.completableToAwaitable(CompletableFuture.completedFuture(Unit))
                }
            })
            loop.invokeMember("run_until_complete", f)
        }
    }
}